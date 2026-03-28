package com.chaoxing.auth.service;

import com.chaoxing.auth.dto.ChangePasswordDTO;
import com.chaoxing.auth.dto.UserLoginDTO;
import com.chaoxing.auth.dto.UserRegisterDTO;
import com.chaoxing.auth.dto.UserUpdateDTO;
import com.chaoxing.auth.entity.User;
import com.chaoxing.auth.vo.LoginResult;
import com.chaoxing.auth.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    UserVO register(UserRegisterDTO dto);

    /**
     * 用户登录
     */
    LoginResult login(UserLoginDTO dto);

    /**
     * 获取当前用户
     */
    User getCurrentUser();

    /**
     * 更新用户信息
     */
    UserVO updateUser(UserUpdateDTO dto);

    /**
     * 修改密码
     */
    void changePassword(ChangePasswordDTO dto);
}
