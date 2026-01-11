package com.band.management.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 * 
 * @author Band Management Team
 */
@Data
public class LoginRequest {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 角色类型：ADMIN, BAND, FAN
     */
    @NotBlank(message = "角色类型不能为空")
    private String role;
}
