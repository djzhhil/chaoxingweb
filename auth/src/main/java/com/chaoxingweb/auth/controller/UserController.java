package com.chaoxingweb.auth.controller;

import com.chaoxingweb.common.Result;
import com.chaoxingweb.auth.dto.ChangePasswordDTO;
import com.chaoxingweb.auth.dto.UserLoginDTO;
import com.chaoxingweb.auth.dto.UserRegisterDTO;
import com.chaoxingweb.auth.dto.UserUpdateDTO;
import com.chaoxingweb.auth.service.UserService;
import com.chaoxingweb.auth.vo.LoginResult;
import com.chaoxingweb.auth.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户注册、登录、信息管理相关接口")
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册账号")
    public Result<UserVO> register(
            @Parameter(description = "用户注册信息", required = true)
            @Valid @RequestBody UserRegisterDTO dto) {
        UserVO userVO = userService.register(dto);
        return Result.success(userVO);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录获取 JWT Token")
    public Result<LoginResult> login(
            @Parameter(description = "用户登录信息", required = true)
            @Valid @RequestBody UserLoginDTO dto) {
        LoginResult result = userService.login(dto);
        return Result.success(result);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "根据 JWT Token 获取当前登录用户的信息")
    public Result<UserVO> getCurrentUser() {
        UserVO userVO = userService.getCurrentUser();
        return Result.success(userVO);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/me")
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的信息")
    public Result<UserVO> updateUser(
            @Parameter(description = "用户更新信息", required = true)
            @Valid @RequestBody UserUpdateDTO dto) {
        UserVO userVO = userService.updateUser(dto);
        return Result.success(userVO);
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    @Operation(summary = "修改密码", description = "修改当前登录用户的密码")
    public Result<Void> changePassword(
            @Parameter(description = "密码修改信息", required = true)
            @Valid @RequestBody ChangePasswordDTO dto) {
        userService.changePassword(dto);
        return Result.success();
    }
}
