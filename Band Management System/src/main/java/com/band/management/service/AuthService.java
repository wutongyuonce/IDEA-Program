package com.band.management.service;

import com.band.management.dto.LoginRequest;
import com.band.management.entity.User;
import com.band.management.vo.LoginResponse;
import com.band.management.vo.RegisterResponse;

/**
 * 认证服务接口
 * 
 * @author Band Management Team
 */
public interface AuthService {

    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * 用户登出
     */
    void logout();

    /**
     * 获取当前登录用户
     * 
     * @return 当前用户
     */
    User getCurrentUser();

    /**
     * 注册新用户
     * 
     * @param user 用户信息
     * @return 用户ID
     */
    Long register(User user);

    /**
     * 修改密码
     * 
     * @param userId 用户ID
     * @param oldPassword 原密码
     * @param newPassword 新密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 乐队用户注册
     * 
     * @param name 乐队名称
     * @param foundedAt 成立时间
     * @param intro 简介
     * @param password 密码
     * @return 注册响应
     */
    RegisterResponse registerBand(String name, String foundedAt, String intro, String password);

    /**
     * 歌迷用户注册
     * 
     * @param name 姓名
     * @param gender 性别
     * @param age 年龄
     * @param occupation 职业
     * @param education 学历
     * @param password 密码
     * @return 注册响应
     */
    RegisterResponse registerFan(String name, String gender, Integer age, String occupation, String education, String password);
}
