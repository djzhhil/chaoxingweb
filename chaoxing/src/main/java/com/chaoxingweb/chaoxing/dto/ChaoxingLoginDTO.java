package com.chaoxingweb.chaoxing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 超星登录 DTO
 */
@Data
public class ChaoxingLoginDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
