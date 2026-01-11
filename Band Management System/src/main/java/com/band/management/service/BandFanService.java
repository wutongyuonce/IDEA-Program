package com.band.management.service;

import com.band.management.common.PageResult;
import com.band.management.entity.Fan;
import com.band.management.vo.FanStatisticsVO;
import java.util.List;

/**
 * 乐队歌迷服务接口
 * 
 * @author Band Management Team
 */
public interface BandFanService {
    
    /**
     * 查询本乐队歌迷列表
     */
    List<Fan> getMyBandFans(Long bandId);
    
    /**
     * 分页查询本乐队歌迷
     */
    PageResult<Fan> page(Long bandId, int pageNum, int pageSize, Fan condition);
    
    /**
     * 获取本乐队歌迷统计分析
     */
    FanStatisticsVO getStatistics(Long bandId);
    
    /**
     * 获取专辑歌迷列表
     */
    List<Fan> getAlbumFans(Long bandId, Long albumId);
    
    /**
     * 获取歌曲歌迷列表
     */
    List<Fan> getSongFans(Long bandId, Long songId);
    
    /**
     * 获取演唱会参与歌迷列表
     */
    List<Fan> getConcertFans(Long bandId, Long concertId);
}
