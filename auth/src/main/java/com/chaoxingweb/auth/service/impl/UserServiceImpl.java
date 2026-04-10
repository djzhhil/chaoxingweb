package com.chaoxingweb.auth.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.chaoxingweb.auth.dto.ChangePasswordDTO;
import com.chaoxingweb.auth.dto.UserLoginDTO;
import com.chaoxingweb.auth.dto.UserRegisterDTO;
import com.chaoxingweb.auth.dto.UserUpdateDTO;
import com.chaoxingweb.persistence.entity.User;
import com.chaoxingweb.persistence.enums.UserRole;
import com.chaoxingweb.persistence.enums.UserStatus;
import com.chaoxingweb.common.exception.BusinessException;
import com.chaoxingweb.persistence.repository.UserRepository;
import com.chaoxingweb.auth.service.UserService;
import com.chaoxingweb.auth.util.JwtTokenProvider;
import com.chaoxingweb.auth.vo.LoginResult;
import com.chaoxingweb.auth.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(UserRegisterDTO dto) {
        // 1. 检查用户名是否已存在
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 检查手机号是否已存在
        if (dto.getPhone() != null && userRepository.existsByPhone(dto.getPhone())) {
            throw new BusinessException("手机号已存在");
        }

        // 3. 检查邮箱是否已存在
        if (dto.getEmail() != null && userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("邮箱已存在");
        }

        // 4. 创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);

        // 5. 保存用户
        user = userRepository.save(user);

        // 6. 返回用户信息
        return BeanUtil.copyProperties(user, UserVO.class);
    }

    @Override
    public LoginResult login(UserLoginDTO dto) {
        // 1. 查找用户
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        // 2. 检查用户状态
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("用户已被禁用");
        }

        // 3. 校验密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 4. 生成 JWT Token
        String token = jwtTokenProvider.generateToken(user);

        // 5. 返回登录结果
        return LoginResult.builder()
                .token(token)
                .user(BeanUtil.copyProperties(user, UserVO.class))
                .build();
    }

    @Override
    public UserVO getCurrentUser() {
        User user = getCurrentUserEntity();
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);

        // 设置是否已绑定超星账号
        userVO.setChaoxingBound(user.getChaoxingUsername() != null && !user.getChaoxingUsername().isEmpty());

        return userVO;
    }

    /**
     * 获取当前用户实体（内部使用）
     */
    private User getCurrentUserEntity() {
        // 1. 从 SecurityContext 获取认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. 检查是否已认证
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("未登录");
        }

        // 3. 获取用户名
        String username = authentication.getName();

        // 4. 查找用户
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateUser(UserUpdateDTO dto) {
        User user = getCurrentUserEntity();

        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }

        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }

        user = userRepository.save(user);

        return BeanUtil.copyProperties(user, UserVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(ChangePasswordDTO dto) {
        User user = getCurrentUserEntity();

        // 校验旧密码
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    // TODO: 绑定超星账号功能已移动到 course 模块的 AccountBindingService
    // @Override
    // @Transactional(rollbackFor = Exception.class)
    // public void bindChaoxingAccount(BindChaoxingDTO dto) {
    //     log.info("开始绑定超星账号: useCookie={}", dto.isUseCookie());
    //
    //     // 1. 获取当前用户
    //     User user = getCurrentUserEntity();
    //
    //     // 2. 检查是否已绑定超星账号
    //     if (user.getChaoxingUsername() != null && !user.getChaoxingUsername().isEmpty()) {
    //         throw new BusinessException("已绑定超星账号，请先解绑");
    //     }
    //
    //     // 3. 构造超星登录 DTO
    //     ChaoxingLoginDTO chaoxingLoginDTO = new ChaoxingLoginDTO();
    //     chaoxingLoginDTO.setUseCookie(dto.isUseCookie());
    //
    //     if (dto.isUseCookie()) {
    //         // Cookie 登录
    //         chaoxingLoginDTO.setCookie(dto.getChaoxingCookie());
    //     } else {
    //         // 账号密码登录
    //         chaoxingLoginDTO.setUsername(dto.getChaoxingUsername());
    //         chaoxingLoginDTO.setPassword(dto.getChaoxingPassword());
    //     }
    //
    //     // 4. 调用超星登录验证
    //     ChaoxingLoginResult chaoxingResult = chaoxingFacade.login(chaoxingLoginDTO);
    //
    //     // 5. 检查登录结果
    //     if (!chaoxingResult.isSuccess()) {
    //         log.error("超星登录失败: {}", chaoxingResult.getErrorMessage());
    //         throw new BusinessException("超星登录失败: " + chaoxingResult.getErrorMessage());
    //     }
    //
    //     // 6. 保存超星账号信息
    //     if (dto.isUseCookie()) {
    //         user.setChaoxingCookie(dto.getChaoxingCookie());
    //         // Cookie 登录时，从登录结果中提取用户名（如果有的话）
    //         if (chaoxingResult.getChaoxingUsername() != null && !chaoxingResult.getChaoxingUsername().isEmpty()) {
    //             user.setChaoxingUsername(chaoxingResult.getChaoxingUsername());
    //         } else {
    //             user.setChaoxingUsername("COOKIE_USER_" + user.getId());
    //         }
    //     } else {
    //         user.setChaoxingUsername(dto.getChaoxingUsername());
    //         user.setChaoxingCookie(chaoxingResult.getCookie());
    //     }
    //
    //     // 7. 保存用户
    //     userRepository.save(user);
    //
    //     log.info("超星账号绑定成功: chaoxingUsername={}", user.getChaoxingUsername());
    // }

    // TODO: 解绑超星账号功能已移动到 course 模块的 AccountBindingService
    // @Override
    // @Transactional(rollbackFor = Exception.class)
    // public void unbindChaoxingAccount() {
    //     log.info("开始解绑超星账号");
    //
    //     // 1. 获取当前用户
    //     User user = getCurrentUserEntity();
    //
    //     // 2. 检查是否已绑定超星账号
    //     if (user.getChaoxingUsername() == null || user.getChaoxingUsername().isEmpty()) {
    //         throw new BusinessException("未绑定超星账号");
    //     }
    //
    //     // 3. 清空超星账号信息
    //     String oldChaoxingUsername = user.getChaoxingUsername();
    //     user.setChaoxingUsername(null);
    //     user.setChaoxingCookie(null);
    //
    //     // 4. 保存用户
    //     userRepository.save(user);
    //
    //     log.info("超星账号解绑成功: chaoxingUsername={}", oldChaoxingUsername);
    // }
}
