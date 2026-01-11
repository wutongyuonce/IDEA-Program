package com.band.management.service;

import com.band.management.common.PageResult;
import com.band.management.entity.Concert;
import java.util.List;

/**
 * 乐队演唱会服务接口
 * 
 * @author Band Management Team
 */
public interface BandConcertService {
    
    /**
     * 获取本乐队演唱会列表
     */
    List<Concert> getMyBandConcerts(Long bandId);
    
    /**
     * 分页查询本乐队演唱会
     */
    PageResult<Concert> page(Long bandId, int pageNum, int pageSize, Concert condition);
    
    /**
     * 发布演唱会
     */
    Long createConcert(Long bandId, Concert concert);
    
    /**
     * 更新演唱会信息
     */
    void updateConcert(Long bandId, Concert concert);
    
    /**
     * 删除演唱会
     */
    void deleteConcert(Long bandId, Long concertId);
    
    /**
     * 查询演唱会参与人数
     */
    int getAttendanceCount(Long bandId, Long concertId);
    
    /**
     * 根据ID查询演唱会
     */
    Concert getById(Long bandId, Long concertId);
}
