package com.chaoxingweb.auth.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.chaoxingweb.auth.dto.ChangePasswordDTO;
import com.chaoxingweb.auth.dto.UserLoginDTO;
import com.chaoxingweb.auth.dto.UserRegisterDTO;
import com.chaoxingweb.auth.dto.UserUpdateDTO;
import com.chaoxingweb.auth.entity.User;
import com.chaoxingweb.auth.enums.UserRole;
import com.chaoxingweb.auth.enums.UserStatus;
import com.chaoxingweb.auth.exception.BusinessException;
import com.chaoxingweb.auth.repository.UserRepository;
import com.chaoxingweb.auth.service.UserService;
import com.chaoxingweb.auth.util.JwtTokenProvider;
import com.chaoxingweb.auth.vo.LoginResult;
import com.chaoxingweb.auth.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现
 */
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
        // TODO: 从 SecurityContext 获取当前用户
        // 暂时返回 null，后续实现
        throw new BusinessException("未登录");
    }

    /**
     * 获取当前用户实体（内部使用）
     */
    private User getCurrentUserEntity() {
        // TODO: 从 SecurityContext 获取当前用户
        // 暂时返回 null，后续实现
        throw new BusinessException("未登录");
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
}
