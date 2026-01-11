package com.band.management.service.impl;

import com.band.management.config.DataSourceContextHolder;
import com.band.management.entity.AlbumRanking;
import com.band.management.entity.Band;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.*;
import com.band.management.service.AdminStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员统计服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class AdminStatisticsServiceImpl implements AdminStatisticsService {

    @Autowired
    private BandMapper bandMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private AlbumMapper albumMapper;

    @Autowired
    private SongMapper songMapper;

    @Autowired
    private FanMapper fanMapper;

    @Autowired
    private ConcertMapper concertMapper;

    @Autowired
    private AlbumReviewMapper albumReviewMapper;

    @Autowired
    private AlbumRankingMapper albumRankingMapper;

    @Override
    public Map<String, Object> getOverallStatistics() {
        log.info("管理员查询系统总体统计数据");

        DataSourceContextHolder.setDataSourceType("admin");

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("bandCount", bandMapper.count());
        statistics.put("memberCount", memberMapper.count());
        statistics.put("albumCount", albumMapper.count());
        statistics.put("songCount", songMapper.count());
        statistics.put("fanCount", fanMapper.count());
        statistics.put("concertCount", concertMapper.count());
        statistics.put("reviewCount", albumReviewMapper.count());

        log.info("系统总体统计数据: {}", statistics);
        return statistics;
    }

    @Override
    public List<AlbumRanking> getTop10Albums() {
        log.info("管理员查询专辑排行榜前10名");

        DataSourceContextHolder.setDataSourceType("admin");

        return albumRankingMapper.selectTop10();
    }

    @Override
    public List<AlbumRanking> getAllAlbumRankings() {
        log.info("管理员查询完整专辑排行榜");

        DataSourceContextHolder.setDataSourceType("admin");

        return albumRankingMapper.selectAll();
    }

    @Override
    public Map<String, Object> getBandStatistics(Long bandId) {
        log.info("管理员查询乐队统计数据: {}", bandId);

        DataSourceContextHolder.setDataSourceType("admin");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("bandId", bandId);
        statistics.put("bandName", band.getName());
        statistics.put("memberCount", memberMapper.selectByBandId(bandId).size());
        statistics.put("albumCount", albumMapper.selectByBandId(bandId).size());
        statistics.put("concertCount", concertMapper.selectByBandId(bandId).size());

        log.info("乐队统计数据: {}", statistics);
        return statistics;
    }
}
