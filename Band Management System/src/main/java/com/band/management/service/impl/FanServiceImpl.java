package com.band.management.service.impl;

import com.band.management.common.PageResult;
import com.band.management.entity.Fan;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.FanMapper;
import com.band.management.service.FanService;
import com.band.management.util.StringUtil;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 歌迷服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class FanServiceImpl implements FanService {

    @Autowired
    private FanMapper fanMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Fan fan) {
        log.info("创建歌迷: {}", fan.getName());

        // 参数校验
        if (StringUtil.isEmpty(fan.getName())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "歌迷姓名不能为空");
        }
        if (StringUtil.isEmpty(fan.getGender())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "性别不能为空");
        }
        if (fan.getAge() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "年龄不能为空");
        }

        // 插入数据
        int result = fanMapper.insert(fan);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "创建歌迷失败");
        }

        log.info("歌迷创建成功，ID: {}", fan.getFanId());
        return fan.getFanId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long fanId) {
        log.info("删除歌迷: {}", fanId);

        // 检查是否存在
        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        // 删除数据（级联删除由数据库外键处理）
        int result = fanMapper.deleteById(fanId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "删除歌迷失败");
        }

        log.info("歌迷删除成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Fan fan) {
        log.info("更新歌迷: {}", fan.getFanId());

        // 检查是否存在
        Fan existFan = fanMapper.selectById(fan.getFanId());
        if (existFan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        // 更新数据
        int result = fanMapper.update(fan);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "更新歌迷失败");
        }

        log.info("歌迷更新成功");
    }

    @Override
    public Fan getById(Long fanId) {
        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }
        return fan;
    }

    @Override
    public PageResult<Fan> page(int pageNum, int pageSize, Fan condition) {
        PageHelper.startPage(pageNum, pageSize);
        List<Fan> list = condition == null ? fanMapper.selectAll() : fanMapper.selectByCondition(condition);
        return PageResult.of(list);
    }

    @Override
    public List<Fan> list() {
        return fanMapper.selectAll();
    }
}
