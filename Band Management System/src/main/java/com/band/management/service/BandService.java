package com.band.management.service;

import com.band.management.common.PageResult;
import com.band.management.entity.Band;
import com.band.management.vo.BandVO;

import java.util.List;

/**
 * 乐队服务接口
 * 
 * @author Band Management Team
 */
public interface BandService {

    /**
     * 创建乐队
     */
    Long create(Band band);

    /**
     * 删除乐队
     */
    void delete(Long bandId);

    /**
     * 更新乐队信息
     */
    void update(Band band);

    /**
     * 根据ID查询乐队
     */
    Band getById(Long bandId);

    /**
     * 查询乐队详情（包含成员列表）
     */
    BandVO getDetailById(Long bandId);

    /**
     * 分页查询乐队列表
     */
    PageResult<Band> page(int pageNum, int pageSize, Band condition);

    /**
     * 查询所有乐队
     */
    List<Band> list();

    /**
     * 根据名称查询乐队
     */
    Band getByName(String name);
}
