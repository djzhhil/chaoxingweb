package com.chaoxingweb.chaoxing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 超星登录 DTO
 *
 * 职责：
 * - 内部数据传输（模块之间）
 * - 不用于外部视图展示
 */
@Data
public class ChaoxingLoginDTO {

    /**
     * 是否使用 Cookie 登录
     */
    private boolean useCookie;

    /**
     * 用户名（账号密码登录时使用）
     */
    private String username;

    /**
     * 密码（账号密码登录时使用）
     */
    private String password;

    /**
     * Cookie（Cookie 登录时使用）
     */
    private String cookie;
}
