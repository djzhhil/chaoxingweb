package com.chaoxing.auth.vo;

import com.chaoxing.auth.enums.UserRole;
import com.chaoxing.auth.enums.UserStatus;
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
