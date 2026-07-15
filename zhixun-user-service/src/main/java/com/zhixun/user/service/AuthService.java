package com.zhixun.user.service;

import com.zhixun.common.enums.UserRole;
import com.zhixun.common.enums.UserStatus;
import com.zhixun.common.exception.BusinessException;
import com.zhixun.user.domain.SysUser;
import com.zhixun.user.dto.AuthResponse;
import com.zhixun.user.dto.LoginRequest;
import com.zhixun.user.dto.RegisterRequest;
import com.zhixun.user.dto.UserProfileResponse;
import com.zhixun.user.exception.UserResultCode;
import com.zhixun.user.mapper.RoleMapper;
import com.zhixun.user.mapper.SysUserMapper;
import com.zhixun.user.mapper.UserRoleMapper;
import com.zhixun.user.security.JwtService;
import com.zhixun.user.security.JwtToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = normalizeUsername(request.username());
        String studentStaffNo = request.studentStaffNo().trim();
        String email = normalizeNullable(request.email());

        if (userMapper.existsByUsername(username)) {
            throw new BusinessException(UserResultCode.USERNAME_ALREADY_EXISTS);
        }
        if (userMapper.existsByStudentStaffNo(studentStaffNo)) {
            throw new BusinessException(UserResultCode.IDENTITY_NUMBER_ALREADY_EXISTS);
        }
        if (email != null && userMapper.existsByEmail(email)) {
            throw new BusinessException(UserResultCode.EMAIL_ALREADY_EXISTS);
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRealName(request.realName().trim());
        user.setIdentityType(request.identityType());
        user.setStudentStaffNo(studentStaffNo);
        user.setPhone(normalizeNullable(request.phone()));
        user.setEmail(email);
        user.setStatus(UserStatus.ACTIVE);
        user.setDeleted(false);
        userMapper.insert(user);

        Long roleId = roleMapper.findIdByCode(UserRole.USER.name());
        if (roleId == null) {
            throw new BusinessException(UserResultCode.DEFAULT_ROLE_NOT_FOUND);
        }
        userRoleMapper.insert(user.getId(), roleId);

        return issueTokens(user, List.of(UserRole.USER.name()));
    }

    public AuthResponse login(LoginRequest request) {
        SysUser user = userMapper.findByUsername(normalizeUsername(request.username()));
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(UserResultCode.INVALID_CREDENTIALS);
        }
        assertActive(user);

        List<String> roles = roleMapper.findCodesByUserId(user.getId());
        LocalDateTime lastLoginAt = LocalDateTime.now();
        userMapper.updateLastLoginAt(user.getId(), lastLoginAt);
        user.setLastLoginAt(lastLoginAt);
        return issueTokens(user, roles);
    }

    public AuthResponse refresh(String refreshToken) {
        Long userId = refreshTokenService.consume(refreshToken);
        SysUser user = getRequiredUser(userId);
        assertActive(user);
        return issueTokens(user, roleMapper.findCodesByUserId(userId));
    }

    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    public UserProfileResponse currentUser(Long userId) {
        SysUser user = getRequiredUser(userId);
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getIdentityType(),
                user.getStudentStaffNo(),
                user.getPhone(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getStatus(),
                roleMapper.findCodesByUserId(userId),
                user.getLastLoginAt(),
                user.getCreatedAt()
        );
    }

    private AuthResponse issueTokens(SysUser user, List<String> roles) {
        JwtToken accessToken = jwtService.createAccessToken(user, roles);
        RefreshTokenValue refreshToken = refreshTokenService.issue(user.getId());
        return new AuthResponse(
                "Bearer",
                accessToken.value(),
                jwtService.accessTokenExpiresInSeconds(),
                refreshToken.token(),
                refreshToken.expiresInSeconds()
        );
    }

    private SysUser getRequiredUser(Long userId) {
        SysUser user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(UserResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    private void assertActive(SysUser user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(UserResultCode.USER_DISABLED);
        }
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
