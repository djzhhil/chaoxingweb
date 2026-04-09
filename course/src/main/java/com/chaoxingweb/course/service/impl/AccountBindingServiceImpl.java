package com.chaoxingweb.course.service.impl;

import com.chaoxingweb.auth.entity.User;
import com.chaoxingweb.auth.repository.UserRepository;
import com.chaoxingweb.chaoxing.dto.ChaoxingLoginDTO;
import com.chaoxingweb.chaoxing.facade.ChaoxingFacade;
import com.chaoxingweb.chaoxing.vo.ChaoxingLoginResult;
import com.chaoxingweb.course.service.AccountBindingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final UserRepository userRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindChaoxingAccount(Long userId, String username, String password, String cookie, boolean useCookie) {
        log.info("开始绑定超星账号: userId={}, useCookie={}", userId, useCookie);

        // 1. 查询用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));

        // 2. 检查是否已绑定超星账号
        if (user.getChaoxingUsername() != null && !user.getChaoxingUsername().isEmpty()) {
            throw new RuntimeException("已绑定超星账号: " + user.getChaoxingUsername() + "，请先解绑");
        }

        // 3. 构造超星登录 DTO
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

        // 4. 调用超星登录验证
        ChaoxingLoginResult chaoxingResult = chaoxingFacade.login(chaoxingLoginDTO);

        // 5. 检查登录结果
        if (!chaoxingResult.isSuccess()) {
            log.error("超星登录失败: {}", chaoxingResult.getErrorMessage());
            throw new RuntimeException("超星登录失败: " + chaoxingResult.getErrorMessage());
        }

        // 6. 保存超星账号信息到数据库
        if (useCookie) {
            // Cookie 登录时，从登录结果中提取用户名（如果有的话）
            if (chaoxingResult.getChaoxingUsername() != null && !chaoxingResult.getChaoxingUsername().isEmpty()) {
                user.setChaoxingUsername(chaoxingResult.getChaoxingUsername());
            } else {
                user.setChaoxingUsername("COOKIE_USER_" + user.getId());
            }
            user.setChaoxingCookie(cookie);
        } else {
            // 账号密码登录
            user.setChaoxingUsername(username);
            user.setChaoxingCookie(chaoxingResult.getCookie());
        }

        userRepository.save(user);

        log.info("超星账号绑定成功: userId={}, chaoxingUsername={}", userId, user.getChaoxingUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindChaoxingAccount(Long userId) {
        log.info("开始解绑超星账号: userId={}", userId);

        // 1. 查询用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));

        // 2. 检查是否已绑定超星账号
        if (user.getChaoxingUsername() == null || user.getChaoxingUsername().isEmpty()) {
            throw new RuntimeException("未绑定超星账号");
        }

        // 3. 清空超星账号信息
        String oldChaoxingUsername = user.getChaoxingUsername();
        user.setChaoxingUsername(null);
        user.setChaoxingCookie(null);

        // 4. 保存用户
        userRepository.save(user);

        log.info("超星账号解绑成功: userId={}, chaoxingUsername={}", userId, oldChaoxingUsername);
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
