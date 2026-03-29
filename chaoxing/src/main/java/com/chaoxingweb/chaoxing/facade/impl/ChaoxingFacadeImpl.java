package com.chaoxingweb.chaoxing.facade.impl;

import com.chaoxingweb.chaoxing.dto.ChaoxingLoginDTO;
import com.chaoxingweb.chaoxing.facade.ChaoxingFacade;
import com.chaoxingweb.chaoxing.service.ChaoxingService;
import com.chaoxingweb.chaoxing.vo.ChaoxingLoginResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 超星模块 Facade 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChaoxingFacadeImpl implements ChaoxingFacade {

    private final ChaoxingService chaoxingService;

    @Override
    public ChaoxingLoginResult login(ChaoxingLoginDTO dto) {
        log.info("超星登录请求: username={}", dto.getUsername());

        try {
            // 调用 chaoxing 模块内部的业务逻辑
            return chaoxingService.login(dto);
        } catch (Exception e) {
            log.error("超星登录失败: {}", e.getMessage(), e);
            return ChaoxingLoginResult.failure("超星登录失败: " + e.getMessage());
        }
    }
}
