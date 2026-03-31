package com.chaoxingweb.auth.controller;

import com.chaoxingweb.auth.dto.BindChaoxingDTO;
import com.chaoxingweb.common.result.Result;
import com.chaoxingweb.auth.dto.ChangePasswordDTO;
import com.chaoxingweb.auth.dto.UserLoginDTO;
import com.chaoxingweb.auth.dto.UserRegisterDTO;
import com.chaoxingweb.auth.dto.UserUpdateDTO;
import com.chaoxingweb.auth.service.UserService;
import com.chaoxingweb.auth.vo.LoginResult;
import com.chaoxingweb.auth.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody UserRegisterDTO dto) {
        UserVO userVO = userService.register(dto);
        return Result.success(userVO);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResult> login(@Valid @RequestBody UserLoginDTO dto) {
        LoginResult result = userService.login(dto);
        return Result.success(result);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Result<UserVO> getCurrentUser() {
        UserVO userVO = userService.getCurrentUser();
        return Result.success(userVO);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/me")
    public Result<UserVO> updateUser(@Valid @RequestBody UserUpdateDTO dto) {
        UserVO userVO = userService.updateUser(dto);
        return Result.success(userVO);
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        userService.changePassword(dto);
        return Result.success();
    }

    /**
     * 绑定超星账号
     */
    @PostMapping("/bind-chaoxing")
    public Result<Void> bindChaoxingAccount(@Valid @RequestBody BindChaoxingDTO dto) {
        userService.bindChaoxingAccount(dto);
        return Result.success();
    }

    /**
     * 解绑超星账号
     */
    @PostMapping("/unbind-chaoxing")
    public Result<Void> unbindChaoxingAccount() {
        userService.unbindChaoxingAccount();
        return Result.success();
    }
}
