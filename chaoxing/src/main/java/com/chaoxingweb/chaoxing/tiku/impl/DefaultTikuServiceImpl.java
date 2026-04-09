package com.chaoxingweb.chaoxing.tiku.impl;

import com.chaoxingweb.chaoxing.dto.QuestionDTO;
import com.chaoxingweb.chaoxing.dto.TikuConfigDTO;
import com.chaoxingweb.chaoxing.tiku.AbstractTiku;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 默认题库实现（禁用状态）
 * 
 * 当没有配置具体题库时，使用此实现
 * 所有查询返回null，触发随机答题策略
 *
 * @author 小克 🐕💎
 * @since 2026-04-09
 */
@Slf4j
@Service
public class DefaultTikuServiceImpl extends AbstractTiku {

    public DefaultTikuServiceImpl() {
        this.name = "默认题库（未配置）";
        this.config = TikuConfigDTO.builder()
                .enabled(false)  // 默认禁用
                .build();
    }

    @Override
    protected void initTiku() {
        log.info("默认题库已初始化（未配置具体题库提供商，将使用随机答题策略）");
    }

    @Override
    protected String doQuery(QuestionDTO question) {
        log.debug("默认题库不查询答案，将使用随机策略");
        return null;
    }

    @Override
    public boolean isEnabled() {
        return false; // 始终返回false，表示未启用
    }
}
