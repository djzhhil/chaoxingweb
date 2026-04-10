package com.chaoxingweb.persistence.enums;

import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum UserRole {
    ADMIN("管理员"),
    USER("普通用户"),
    VIP("VIP用户");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }
}
