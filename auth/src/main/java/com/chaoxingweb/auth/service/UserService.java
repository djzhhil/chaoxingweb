package com.chaoxingweb.auth.service;

import com.chaoxingweb.auth.dto.BindChaoxingDTO;
import com.chaoxingweb.auth.dto.ChangePasswordDTO;
import com.chaoxingweb.auth.dto.UserLoginDTO;
import com.chaoxingweb.auth.dto.UserRegisterDTO;
import com.chaoxingweb.auth.dto.UserUpdateDTO;
import com.chaoxingweb.auth.entity.User;
import com.chaoxingweb.auth.vo.LoginResult;
import com.chaoxingweb.auth.vo.UserVO;

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
    UserVO getCurrentUser();

    /**
     * 更新用户信息
     */
    UserVO updateUser(UserUpdateDTO dto);

    /**
     * 修改密码
     */
    void changePassword(ChangePasswordDTO dto);

    /**
     * 绑定超星账号
     */
    void bindChaoxingAccount(BindChaoxingDTO dto);

    /**
     * 解绑超星账号
     */
    void unbindChaoxingAccount();
}
