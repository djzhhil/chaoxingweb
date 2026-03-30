package com.chaoxingweb.chaoxing.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 超星登录结果 VO
 *
 * 职责：
 * - 外部视图展示（API 返回）
 * - 不用于内部数据传输
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChaoxingLoginResult {

    /**
     * 登录是否成功
     */
    private boolean success;

    /**
     * 错误信息（如果失败）
     */
    private String errorMessage;

    /**
     * 超星用户 ID
     */
    private String chaoxingUserId;

    /**
     * 超星用户名
     */
    private String chaoxingUsername;

    /**
     * 超星 Cookie
     */
    private String cookie;

    /**
     * 创建成功结果
     */
    public static ChaoxingLoginResult success(String chaoxingUserId, String chaoxingUsername, String cookie) {
        return ChaoxingLoginResult.builder()
                .success(true)
                .chaoxingUserId(chaoxingUserId)
                .chaoxingUsername(chaoxingUsername)
                .cookie(cookie)
                .build();
    }

    /**
     * 创建失败结果
     */
    public static ChaoxingLoginResult failure(String errorMessage) {
        return ChaoxingLoginResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
