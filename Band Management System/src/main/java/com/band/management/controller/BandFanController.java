package com.band.management.controller;

import com.band.management.common.PageResult;
import com.band.management.common.Result;
import com.band.management.entity.Fan;
import com.band.management.service.BandFanService;
import com.band.management.vo.FanStatisticsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 乐队歌迷控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/band/{bandId}/fans")
public class BandFanController {

    @Autowired
    private BandFanService bandFanService;

    /**
     * 查询本乐队歌迷列表
     */
    @GetMapping("/list")
    public Result<List<Fan>> getMyBandFans(@PathVariable("bandId") Long bandId) {
        log.info("乐队查询自己的歌迷列表: {}", bandId);
        List<Fan> fans = bandFanService.getMyBandFans(bandId);
        return Result.success(fans);
    }

    /**
     * 分页查询本乐队歌迷
     */
    @GetMapping
    public Result<PageResult<Fan>> page(
            @PathVariable("bandId") Long bandId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            Fan condition) {
        PageResult<Fan> pageResult = bandFanService.page(bandId, pageNum, pageSize, condition);
        return Result.success(pageResult);
    }

    /**
     * 获取本乐队歌迷统计分析
     */
    @GetMapping("/statistics")
    public Result<FanStatisticsVO> getStatistics(@PathVariable("bandId") Long bandId) {
        log.info("乐队查询歌迷统计分析: {}", bandId);
        FanStatisticsVO statistics = bandFanService.getStatistics(bandId);
        return Result.success(statistics);
    }

    /**
     * 获取专辑歌迷列表
     */
    @GetMapping("/album/{albumId}")
    public Result<List<Fan>> getAlbumFans(
            @PathVariable("bandId") Long bandId,
            @PathVariable("albumId") Long albumId) {
        List<Fan> fans = bandFanService.getAlbumFans(bandId, albumId);
        return Result.success(fans);
    }

    /**
     * 获取歌曲歌迷列表
     */
    @GetMapping("/song/{songId}")
    public Result<List<Fan>> getSongFans(
            @PathVariable("bandId") Long bandId,
            @PathVariable("songId") Long songId) {
        List<Fan> fans = bandFanService.getSongFans(bandId, songId);
        return Result.success(fans);
    }

    /**
     * 获取演唱会参与歌迷列表
     */
    @GetMapping("/concert/{concertId}")
    public Result<List<Fan>> getConcertFans(
            @PathVariable("bandId") Long bandId,
            @PathVariable("concertId") Long concertId) {
        List<Fan> fans = bandFanService.getConcertFans(bandId, concertId);
        return Result.success(fans);
    }
}
