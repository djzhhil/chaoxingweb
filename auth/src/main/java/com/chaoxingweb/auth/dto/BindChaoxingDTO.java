package com.chaoxingweb.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 绑定超星账号 DTO
 *
 * @author 小克 🐕💎
 * @since 2026-03-31
 */
@Data
public class BindChaoxingDTO {

    /**
     * 是否使用 Cookie 登录
     */
    private boolean useCookie;

    /**
     * 超星用户名（账号密码登录时使用）
     */
    @NotBlank(message = "超星用户名不能为空", groups = {PasswordLogin.class})
    private String chaoxingUsername;

    /**
     * 超星密码（账号密码登录时使用）
     */
    @NotBlank(message = "超星密码不能为空", groups = {PasswordLogin.class})
    private String chaoxingPassword;

    /**
     * 超星 Cookie（Cookie 登录时使用）
     */
    @NotBlank(message = "超星 Cookie 不能为空", groups = {CookieLogin.class})
    private String chaoxingCookie;

    /**
     * 账号密码登录验证组
     */
    public interface PasswordLogin {}

    /**
     * Cookie 登录验证组
     */
    public interface CookieLogin {}
}
