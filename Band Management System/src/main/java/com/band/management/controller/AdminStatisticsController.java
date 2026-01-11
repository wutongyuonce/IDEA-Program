package com.band.management.controller;

import com.band.management.common.Result;
import com.band.management.entity.AlbumRanking;
import com.band.management.service.AdminStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员统计控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/statistics")
public class AdminStatisticsController {

    @Autowired
    private AdminStatisticsService adminStatisticsService;

    /**
     * 获取系统总体统计数据
     */
    @GetMapping("/overall")
    public Result<Map<String, Object>> getOverallStatistics() {
        log.info("管理员查询系统总体统计");
        Map<String, Object> statistics = adminStatisticsService.getOverallStatistics();
        return Result.success(statistics);
    }

    /**
     * 获取专辑排行榜前10名
     */
    @GetMapping("/albums/top10")
    public Result<List<AlbumRanking>> getTop10Albums() {
        log.info("管理员查询专辑排行榜前10名");
        List<AlbumRanking> rankings = adminStatisticsService.getTop10Albums();
        return Result.success(rankings);
    }

    /**
     * 获取完整专辑排行榜
     */
    @GetMapping("/albums/rankings")
    public Result<List<AlbumRanking>> getAllAlbumRankings() {
        log.info("管理员查询完整专辑排行榜");
        List<AlbumRanking> rankings = adminStatisticsService.getAllAlbumRankings();
        return Result.success(rankings);
    }

    /**
     * 获取乐队统计数据
     */
    @GetMapping("/bands/{bandId}")
    public Result<Map<String, Object>> getBandStatistics(@PathVariable("bandId") Long bandId) {
        log.info("管理员查询乐队统计: {}", bandId);
        Map<String, Object> statistics = adminStatisticsService.getBandStatistics(bandId);
        return Result.success(statistics);
    }
}
