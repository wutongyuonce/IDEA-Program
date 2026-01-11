package com.band.management.service.impl;

import com.band.management.config.DataSourceContextHolder;
import com.band.management.entity.Fan;
import com.band.management.entity.User;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.FanMapper;
import com.band.management.mapper.UserMapper;
import com.band.management.service.FanProfileService;
import com.band.management.util.EncryptUtil;
import com.band.management.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 歌迷个人信息服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class FanProfileServiceImpl implements FanProfileService {

    @Autowired
    private FanMapper fanMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Fan getProfile(Long fanId) {
        log.info("歌迷查询个人信息: {}", fanId);

        DataSourceContextHolder.setDataSourceType("fan");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        return fan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long fanId, Fan fan) {
        log.info("歌迷更新个人信息: {}", fanId);

        DataSourceContextHolder.setDataSourceType("fan");

        Fan existFan = fanMapper.selectById(fanId);
        if (existFan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        fan.setFanId(fanId);
        int result = fanMapper.update(fan);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "更新个人信息失败");
        }

        log.info("歌迷更新个人信息成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long fanId, String oldPassword, String newPassword) {
        log.info("歌迷修改密码: {}", fanId);

        DataSourceContextHolder.setDataSourceType("fan");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        if (StringUtil.isEmpty(oldPassword) || StringUtil.isEmpty(newPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "密码不能为空");
        }

        if (newPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "新密码长度不能少于6位");
        }

        // 查找用户（通过fanId查找对应的用户）
        // 由于UserMapper没有selectByRelatedId方法，我们需要通过其他方式查找
        // 这里简化处理，假设用户名就是fan的ID
        User user = userMapper.selectById(fanId);
        if (user == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND.getCode(), "用户不存在");
        }

        if (!EncryptUtil.matchesPassword(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED.getCode(), "原密码错误");
        }

        user.setPassword(EncryptUtil.encryptPassword(newPassword));
        int result = userMapper.update(user);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "修改密码失败");
        }

        log.info("歌迷修改密码成功");
    }
}
