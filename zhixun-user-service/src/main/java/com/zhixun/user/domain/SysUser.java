package com.zhixun.user.domain;

import com.zhixun.common.enums.UserIdentityType;
import com.zhixun.common.enums.UserStatus;
import com.zhixun.common.model.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SysUser extends BaseEntity {

    private String username;
    private String passwordHash;
    private String realName;
    private UserIdentityType identityType;
    private String studentStaffNo;
    private String phone;
    private String email;
    private String avatarUrl;
    private UserStatus status;
    private LocalDateTime lastLoginAt;
}
