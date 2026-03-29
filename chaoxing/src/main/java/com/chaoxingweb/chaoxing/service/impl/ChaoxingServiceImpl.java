package com.chaoxingweb.chaoxing.service.impl;

import com.chaoxingweb.chaoxing.dto.ChaoxingLoginDTO;
import com.chaoxingweb.chaoxing.service.ChaoxingService;
import com.chaoxingweb.chaoxing.vo.ChaoxingLoginResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 超星服务实现
 */
@Slf4j
@Service
public class ChaoxingServiceImpl implements ChaoxingService {

    @Override
    public ChaoxingLoginResult login(ChaoxingLoginDTO dto) {
        log.info("执行超星登录逻辑: username={}", dto.getUsername());

        // TODO: 实现超星登录逻辑
        // 1. 调用超星登录接口
        // 2. 处理验证码（如果需要）
        // 3. 获取 Cookie
        // 4. 返回登录结果

        // 暂时返回成功结果（模拟）
        return ChaoxingLoginResult.success(
                "mock-chaoxing-user-id",
                dto.getUsername(),
                "mock-cookie"
        );
    }
}
