package com.chaoxingweb.course.service.impl;

import com.chaoxingweb.chaoxing.dto.ChaoxingLoginDTO;
import com.chaoxingweb.chaoxing.facade.ChaoxingFacade;
import com.chaoxingweb.chaoxing.vo.ChaoxingLoginResult;
import com.chaoxingweb.course.service.AccountBindingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 账号绑定服务实现
 *
 * @author 小克 🐕💎
 * @since 2026-03-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountBindingServiceImpl implements AccountBindingService {

    private final ChaoxingFacade chaoxingFacade;

    @Override
    public void bindChaoxingAccount(Long userId, String username, String password, String cookie, boolean useCookie) {
        log.info("开始绑定超星账号: userId={}, useCookie={}", userId, useCookie);

        // 1. 构造超星登录 DTO
        ChaoxingLoginDTO chaoxingLoginDTO = new ChaoxingLoginDTO();
        chaoxingLoginDTO.setUseCookie(useCookie);

        if (useCookie) {
            // Cookie 登录
            chaoxingLoginDTO.setCookie(cookie);
        } else {
            // 账号密码登录
            chaoxingLoginDTO.setUsername(username);
            chaoxingLoginDTO.setPassword(password);
        }

        // 2. 调用超星登录验证
        ChaoxingLoginResult chaoxingResult = chaoxingFacade.login(chaoxingLoginDTO);

        // 3. 检查登录结果
        if (!chaoxingResult.isSuccess()) {
            log.error("超星登录失败: {}", chaoxingResult.getErrorMessage());
            throw new RuntimeException("超星登录失败: " + chaoxingResult.getErrorMessage());
        }

        // 4. TODO: 保存超星账号信息到数据库
        // 这里应该将超星账号信息保存到数据库
        // 例如：user.setChaoxingUsername(username);
        //       user.setChaoxingCookie(chaoxingResult.getCookie());
        //       userRepository.save(user);

        log.info("超星账号绑定成功: userId={}", userId);
    }

    @Override
    public void unbindChaoxingAccount(Long userId) {
        log.info("开始解绑超星账号: userId={}", userId);

        // TODO: 清空超星账号信息
        // 这里应该清空超星账号信息
        // 例如：user.setChaoxingUsername(null);
        //       user.setChaoxingCookie(null);
        //       userRepository.save(user);

        log.info("超星账号解绑成功: userId={}", userId);
    }

    @Override
    public boolean validateChaoxingAccount(String username, String password, String cookie, boolean useCookie) {
        log.info("开始验证超星账号: useCookie={}", useCookie);

        try {
            // 1. 构造超星登录 DTO
            ChaoxingLoginDTO chaoxingLoginDTO = new ChaoxingLoginDTO();
            chaoxingLoginDTO.setUseCookie(useCookie);

            if (useCookie) {
                // Cookie 登录
                chaoxingLoginDTO.setCookie(cookie);
            } else {
                // 账号密码登录
                chaoxingLoginDTO.setUsername(username);
                chaoxingLoginDTO.setPassword(password);
            }

            // 2. 调用超星登录验证
            ChaoxingLoginResult chaoxingResult = chaoxingFacade.login(chaoxingLoginDTO);

            // 3. 返回验证结果
            return chaoxingResult.isSuccess();

        } catch (Exception e) {
            log.error("验证超星账号失败", e);
            return false;
        }
    }
}
