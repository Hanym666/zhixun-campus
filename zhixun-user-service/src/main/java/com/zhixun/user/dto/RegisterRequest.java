package com.zhixun.user.dto;

import com.zhixun.common.enums.UserIdentityType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 4, max = 50, message = "用户名长度必须为 4-50 位")
        @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 8, max = 72, message = "密码长度必须为 8-72 位")
        String password,

        @NotBlank(message = "姓名不能为空")
        @Size(max = 50, message = "姓名不能超过 50 个字符")
        String realName,

        @NotNull(message = "身份类型不能为空")
        UserIdentityType identityType,

        @NotBlank(message = "学号或工号不能为空")
        @Size(max = 50, message = "学号或工号不能超过 50 个字符")
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "学号或工号格式不正确")
        String studentStaffNo,

        @Pattern(regexp = "^$|^1\\d{10}$", message = "手机号格式不正确")
        String phone,

        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱不能超过 100 个字符")
        String email
) {
}
