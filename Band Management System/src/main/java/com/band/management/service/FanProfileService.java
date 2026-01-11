package com.band.management.service;

import com.band.management.entity.Fan;

/**
 * 歌迷个人信息服务接口
 * 
 * @author Band Management Team
 */
public interface FanProfileService {
    
    /**
     * 获取个人信息
     */
    Fan getProfile(Long fanId);
    
    /**
     * 更新个人信息
     */
    void updateProfile(Long fanId, Fan fan);
    
    /**
     * 修改密码
     */
    void changePassword(Long fanId, String oldPassword, String newPassword);
}
