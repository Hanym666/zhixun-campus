package com.zhixun.user.service;

import com.zhixun.common.enums.UserIdentityType;
import com.zhixun.common.exception.BusinessException;
import com.zhixun.user.domain.SysUser;
import com.zhixun.user.dto.AuthResponse;
import com.zhixun.user.dto.LoginRequest;
import com.zhixun.user.dto.RegisterRequest;
import com.zhixun.user.exception.UserResultCode;
import com.zhixun.user.mapper.RoleMapper;
import com.zhixun.user.mapper.SysUserMapper;
import com.zhixun.user.mapper.UserRoleMapper;
import com.zhixun.user.security.JwtService;
import com.zhixun.user.security.JwtToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private SysUserMapper userMapper;
    @Mock
    private RoleMapper roleMapper;
    @Mock
    private UserRoleMapper userRoleMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterUserAndAssignDefaultRole() {
        RegisterRequest request = new RegisterRequest(
                "Campus_User",
                "Password123!",
                "测试用户",
                UserIdentityType.STUDENT,
                "S20260001",
                "",
                "test@example.com"
        );
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        doAnswer(invocation -> {
            SysUser user = invocation.getArgument(0);
            user.setId(42L);
            return 1;
        }).when(userMapper).insert(any(SysUser.class));
        when(roleMapper.findIdByCode("USER")).thenReturn(1L);
        when(jwtService.createAccessToken(any(SysUser.class), eq(List.of("USER"))))
                .thenReturn(new JwtToken("access-token", Instant.now().plusSeconds(7200)));
        when(jwtService.accessTokenExpiresInSeconds()).thenReturn(7200L);
        when(refreshTokenService.issue(42L))
                .thenReturn(new RefreshTokenValue("refresh-token", 604800L));

        AuthResponse response = authService.register(request);

        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        verify(userRoleMapper).insert(42L, 1L);
    }

    @Test
    void shouldHideWhetherUsernameExistsWhenLoginFails() {
        when(userMapper.findByUsername("missing_user")).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.login(new LoginRequest("missing_user", "wrong-password"))
        );

        assertEquals(UserResultCode.INVALID_CREDENTIALS.code(), exception.getCode());
    }
}
