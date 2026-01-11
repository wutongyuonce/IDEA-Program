package com.band.management.controller;

import com.band.management.common.Result;
import com.band.management.dto.LoginRequest;
import com.band.management.entity.User;
import com.band.management.service.AuthService;
import com.band.management.vo.LoginResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 认证控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        log.info("用户登录请求: username={}, role={}", loginRequest.getUsername(), loginRequest.getRole());
        LoginResponse response = authService.login(loginRequest);
        return Result.success("登录成功", response);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        log.info("用户登出请求");
        authService.logout();
        return Result.success("登出成功");
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/current")
    public Result<User> getCurrentUser() {
        User user = authService.getCurrentUser();
        // 清除密码字段
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 注册新用户
     */
    @PostMapping("/register")
    public Result<Long> register(@RequestBody @Valid User user) {
        log.info("用户注册请求: username={}, role={}", user.getUsername(), user.getRole());
        Long userId = authService.register(user);
        return Result.success("注册成功", userId);
    }

    /**
     * 乐队用户注册
     */
    @PostMapping("/register/band")
    public Result<com.band.management.vo.RegisterResponse> registerBand(@RequestBody java.util.Map<String, Object> params) {
        String name = (String) params.get("name");
        String foundedAt = (String) params.get("foundedAt");
        String intro = (String) params.get("intro");
        String password = (String) params.get("password");
        
        log.info("乐队用户注册请求: name={}", name);
        com.band.management.vo.RegisterResponse response = authService.registerBand(name, foundedAt, intro, password);
        return Result.success("注册成功", response);
    }

    /**
     * 歌迷用户注册
     */
    @PostMapping("/register/fan")
    public Result<com.band.management.vo.RegisterResponse> registerFan(@RequestBody java.util.Map<String, Object> params) {
        String name = (String) params.get("name");
        String gender = (String) params.get("gender");
        Integer age = (Integer) params.get("age");
        String occupation = (String) params.get("occupation");
        String education = (String) params.get("education");
        String password = (String) params.get("password");
        
        log.info("歌迷用户注册请求: name={}", name);
        com.band.management.vo.RegisterResponse response = authService.registerFan(name, gender, age, occupation, education, password);
        return Result.success("注册成功", response);
    }
}
