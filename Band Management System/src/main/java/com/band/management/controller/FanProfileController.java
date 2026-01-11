package com.band.management.controller;

import com.band.management.common.Result;
import com.band.management.entity.Fan;
import com.band.management.service.FanProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 歌迷个人信息控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/fan/{fanId}/profile")
public class FanProfileController {

    @Autowired
    private FanProfileService fanProfileService;

    /**
     * 获取个人信息
     */
    @GetMapping
    public Result<Fan> getProfile(@PathVariable("fanId") Long fanId) {
        log.info("歌迷查询个人信息: {}", fanId);
        Fan fan = fanProfileService.getProfile(fanId);
        return Result.success(fan);
    }

    /**
     * 更新个人信息
     */
    @PutMapping
    public Result<Void> updateProfile(
            @PathVariable("fanId") Long fanId,
            @RequestBody @Valid Fan fan) {
        log.info("歌迷更新个人信息: {}", fanId);
        fanProfileService.updateProfile(fanId, fan);
        return Result.success("更新成功");
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<Void> changePassword(
            @PathVariable("fanId") Long fanId,
            @RequestBody Map<String, String> passwords) {
        log.info("歌迷修改密码: {}", fanId);
        String oldPassword = passwords.get("oldPassword");
        String newPassword = passwords.get("newPassword");
        fanProfileService.changePassword(fanId, oldPassword, newPassword);
        return Result.success("修改密码成功");
    }
}
