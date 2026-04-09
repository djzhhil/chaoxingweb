package com.chaoxingweb.chaoxing.tiku;

import com.chaoxingweb.chaoxing.dto.QuestionDTO;
import com.chaoxingweb.chaoxing.dto.TikuConfigDTO;

/**
 * 题库服务接口
 *
 * @author 小克 🐕💎
 * @since 2026-04-09
 */
public interface TikuService {

    /**
     * 查询题目答案
     *
     * @param question 题目信息
     * @return 答案字符串，null表示未找到答案
     */
    String query(QuestionDTO question);

    /**
     * 判断题答案选择
     * 将答案文本转换为布尔值（true=正确，false=错误）
     *
     * @param answer 答案文本
     * @return true=正确，false=错误
     */
    boolean judgementSelect(String answer);

    /**
     * 获取提交参数（pyFlag）
     * 空字符串=直接提交，"1"=保存但不提交
     *
     * @param currentCoverRate 当前覆盖率（0.0-1.0）
     * @return pyFlag值
     */
    String getPyFlag(double currentCoverRate);

    /**
     * 初始化题库
     * 加载配置、验证Token等
     */
    void init();

    /**
     * 获取题库配置
     *
     * @return 题库配置
     */
    TikuConfigDTO getConfig();

    /**
     * 设置题库配置
     *
     * @param config 题库配置
     */
    void setConfig(TikuConfigDTO config);

    /**
     * 检查题库是否可用
     *
     * @return 是否可用
     */
    boolean isEnabled();
}
