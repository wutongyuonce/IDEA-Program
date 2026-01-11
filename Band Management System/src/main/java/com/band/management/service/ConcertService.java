package com.band.management.service;

import com.band.management.common.PageResult;
import com.band.management.entity.Concert;

import java.util.List;

/**
 * 演唱会服务接口
 * 
 * @author Band Management Team
 */
public interface ConcertService {

    /**
     * 创建演唱会
     */
    Long create(Concert concert);

    /**
     * 删除演唱会
     */
    void delete(Long concertId);

    /**
     * 更新演唱会信息
     */
    void update(Concert concert);

    /**
     * 根据ID查询演唱会
     */
    Concert getById(Long concertId);

    /**
     * 根据乐队ID查询演唱会列表
     */
    List<Concert> getByBandId(Long bandId);

    /**
     * 分页查询演唱会列表
     */
    PageResult<Concert> page(int pageNum, int pageSize, Concert condition);

    /**
     * 查询所有演唱会
     */
    List<Concert> list();
}
