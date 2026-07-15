package com.zhixun.user.controller;

import com.zhixun.common.api.ApiResponse;
import com.zhixun.user.dto.UserProfileResponse;
import com.zhixun.user.security.AuthenticatedUser;
import com.zhixun.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> currentUser(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ApiResponse.success(authService.currentUser(user.userId()));
    }
}
