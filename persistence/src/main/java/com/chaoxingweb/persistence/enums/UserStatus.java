package com.chaoxingweb.persistence.enums;

import lombok.Getter;

/**
 * 用户状态枚举
 */
@Getter
public enum UserStatus {
    ACTIVE("正常"),
    INACTIVE("未激活"),
    BANNED("禁用"),
    DELETED("已删除");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }
}
