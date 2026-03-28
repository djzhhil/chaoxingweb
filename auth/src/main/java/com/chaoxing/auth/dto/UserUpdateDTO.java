package com.chaoxing.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户更新 DTO
 */
@Data
public class UserUpdateDTO {

    @Size(min = 11, max = 11, message = "手机号长度必须为11位")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;
}
