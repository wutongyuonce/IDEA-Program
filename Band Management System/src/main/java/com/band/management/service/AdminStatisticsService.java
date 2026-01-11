package com.band.management.service;

import com.band.management.entity.AlbumRanking;
import java.util.Map;
import java.util.List;

/**
 * 管理员统计服务接口
 * 
 * @author Band Management Team
 */
public interface AdminStatisticsService {
    
    /**
     * 获取系统总体统计数据
     */
    Map<String, Object> getOverallStatistics();
    
    /**
     * 获取专辑排行榜前10名
     */
    List<AlbumRanking> getTop10Albums();
    
    /**
     * 获取完整专辑排行榜
     */
    List<AlbumRanking> getAllAlbumRankings();
    
    /**
     * 获取乐队统计数据
     */
    Map<String, Object> getBandStatistics(Long bandId);
}
