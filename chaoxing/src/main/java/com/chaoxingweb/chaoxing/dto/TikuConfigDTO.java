package com.chaoxingweb.chaoxing.dto;

import com.chaoxingweb.chaoxing.enums.TikuProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 题库配置DTO
 *
 * @author 小克 🐕💎
 * @since 2026-04-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TikuConfigDTO {

    /**
     * 是否启用题库
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * 题库提供商
     */
    private TikuProvider provider;

    /**
     * API Tokens（多个用逗号分隔）
     */
    private List<String> tokens;

    /**
     * 覆盖率阈值（0.0-1.0），达到此值才提交
     */
    @Builder.Default
    private Double coverRate = 0.8;

    /**
     * 查询延迟（秒），避免请求过快
     */
    @Builder.Default
    private Double queryDelay = 0.0;

    /**
     * 是否自动提交（true=直接提交，false=仅保存）
     */
    @Builder.Default
    private boolean autoSubmit = false;

    /**
     * 正确选项列表（用于判断题）
     */
    @Builder.Default
    private List<String> trueList = List.of("正确", "对", "√", "是");

    /**
     * 错误选项列表（用于判断题）
     */
    @Builder.Default
    private List<String> falseList = List.of("错误", "错", "×", "否", "不对", "不正确");

    // ==================== 各题库专属配置 ====================

    /**
     * TikuAdapter URL
     */
    private String adapterUrl;

    /**
     * LIKE知识库 - 是否启用联网搜索
     */
    private Boolean likeSearch;

    /**
     * LIKE知识库 - 是否启用视觉能力
     */
    @Builder.Default
    private Boolean likeVision = true;

    /**
     * LIKE知识库 - 模型选择
     */
    private String likeModel;

    /**
     * LIKE知识库 - 是否自动重试
     */
    @Builder.Default
    private Boolean likeRetry = true;

    /**
     * LIKE知识库 - 重试次数
     */
    @Builder.Default
    private Integer likeRetryTimes = 3;

    /**
     * SiliconFlow API Key
     */
    private String siliconKey;

    /**
     * SiliconFlow 模型
     */
    private String siliconModel;

    /**
     * AI大模型 - API Endpoint
     */
    private String aiEndpoint;

    /**
     * AI大模型 - API Key
     */
    private String aiKey;

    /**
     * AI大模型 - 模型名称
     */
    private String aiModel;

    /**
     * AI大模型 - 请求间隔（秒）
     */
    @Builder.Default
    private Integer aiInterval = 3;

    /**
     * 验证配置是否有效
     *
     * @return 是否有效
     */
    public boolean isValid() {
        if (!enabled) {
            return true; // 未启用也算有效
        }

        if (provider == null || provider == TikuProvider.UNKNOWN) {
            return false;
        }

        // 根据不同提供商检查必要配置
        return switch (provider) {
            case YANXI, LIKE -> tokens != null && !tokens.isEmpty();
            case ADAPTER -> adapterUrl != null && !adapterUrl.isEmpty();
            case SILICON_FLOW -> siliconKey != null && !siliconKey.isEmpty();
            case AI -> aiEndpoint != null && !aiEndpoint.isEmpty() 
                    && aiKey != null && !aiKey.isEmpty()
                    && aiModel != null && !aiModel.isEmpty();
            default -> false;
        };
    }

    /**
     * 获取提交参数（pyFlag）
     * 空字符串=直接提交，"1"=保存但不提交
     *
     * @param currentCoverRate 当前覆盖率
     * @return pyFlag值
     */
    public String getPyFlag(double currentCoverRate) {
        if (!enabled) {
            return "1"; // 未启用题库，不提交
        }

        if (autoSubmit) {
            return ""; // 自动提交模式
        }

        // 根据覆盖率决定是否提交
        if (currentCoverRate >= coverRate) {
            return ""; // 达到覆盖率，提交
        } else {
            return "1"; // 未达到覆盖率，仅保存
        }
    }
}
