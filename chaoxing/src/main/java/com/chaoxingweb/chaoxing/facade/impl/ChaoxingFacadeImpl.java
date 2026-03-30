package com.chaoxingweb.chaoxing.facade.impl;

import com.chaoxingweb.chaoxing.auth.LoginService;
import com.chaoxingweb.chaoxing.dto.ChaoxingLoginDTO;
import com.chaoxingweb.chaoxing.facade.ChaoxingFacade;
import com.chaoxingweb.chaoxing.vo.ChaoxingLoginResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 超星模块 Facade 实现
 *
 * 职责：
 * - 对外提供统一接口
 * - 协调各个模块
 * - 不包含具体业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChaoxingFacadeImpl implements ChaoxingFacade {

    private final LoginService loginService;

    @Override
    public ChaoxingLoginResult login(ChaoxingLoginDTO dto) {
        log.info("超星登录请求: username={}", dto.getUsername());

        try {
            // 调用登录服务
            LoginService.LoginResult result;

            if (dto.isUseCookie()) {
                // Cookie 登录
                result = loginService.loginWithCookie(dto.getCookie());
            } else {
                // 账号密码登录
                result = loginService.loginWithPassword(dto.getUsername(), dto.getPassword());
            }

            // 转换为 VO
            if (result.isSuccess()) {
                return ChaoxingLoginResult.success(
                        result.getToken(),
                        result.getMessage(),
                        result.getCookie()
                );
            } else {
                return ChaoxingLoginResult.failure(result.getMessage());
            }
        } catch (Exception e) {
            log.error("超星登录失败: {}", e.getMessage(), e);
            return ChaoxingLoginResult.failure("超星登录失败: " + e.getMessage());
        }
    }
}
