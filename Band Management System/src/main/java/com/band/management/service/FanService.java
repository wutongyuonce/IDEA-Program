package com.band.management.service;

import com.band.management.common.PageResult;
import com.band.management.entity.Fan;

import java.util.List;

/**
 * 歌迷服务接口
 * 
 * @author Band Management Team
 */
public interface FanService {

    /**
     * 创建歌迷
     */
    Long create(Fan fan);

    /**
     * 删除歌迷
     */
    void delete(Long fanId);

    /**
     * 更新歌迷信息
     */
    void update(Fan fan);

    /**
     * 根据ID查询歌迷
     */
    Fan getById(Long fanId);

    /**
     * 分页查询歌迷列表
     */
    PageResult<Fan> page(int pageNum, int pageSize, Fan condition);

    /**
     * 查询所有歌迷
     */
    List<Fan> list();
}
