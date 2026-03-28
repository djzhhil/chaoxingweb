package com.chaoxingweb.auth.vo;

import com.chaoxingweb.auth.enums.UserRole;
import com.chaoxingweb.auth.enums.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户 VO
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String phone;
    private String email;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;
}
