package com.band.management.controller;

import com.band.management.common.Result;
import com.band.management.entity.Concert;
import com.band.management.entity.Song;
import com.band.management.service.FanDetailService;
import com.band.management.vo.AlbumDetailVO;
import com.band.management.vo.BandVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 歌迷详情查看控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/fan/{fanId}/detail")
public class FanDetailController {

    @Autowired
    private FanDetailService fanDetailService;

    @GetMapping("/band/{bandId}")
    public Result<BandVO> getBandDetail(
            @PathVariable("fanId") Long fanId,
            @PathVariable("bandId") Long bandId) {
        log.info("歌迷查看乐队详情: fanId={}, bandId={}", fanId, bandId);
        BandVO bandVO = fanDetailService.getBandDetail(fanId, bandId);
        return Result.success(bandVO);
    }

    @GetMapping("/album/{albumId}")
    public Result<AlbumDetailVO> getAlbumDetail(
            @PathVariable("fanId") Long fanId,
            @PathVariable("albumId") Long albumId) {
        log.info("歌迷查看专辑详情: fanId={}, albumId={}", fanId, albumId);
        AlbumDetailVO albumDetailVO = fanDetailService.getAlbumDetail(fanId, albumId);
        return Result.success(albumDetailVO);
    }

    @GetMapping("/song/{songId}")
    public Result<Song> getSongDetail(
            @PathVariable("fanId") Long fanId,
            @PathVariable("songId") Long songId) {
        log.info("歌迷查看歌曲详情: fanId={}, songId={}", fanId, songId);
        Song song = fanDetailService.getSongDetail(fanId, songId);
        return Result.success(song);
    }

    @GetMapping("/concert/{concertId}")
    public Result<Concert> getConcertDetail(
            @PathVariable("fanId") Long fanId,
            @PathVariable("concertId") Long concertId) {
        log.info("歌迷查看演唱会详情: fanId={}, concertId={}", fanId, concertId);
        Concert concert = fanDetailService.getConcertDetail(fanId, concertId);
        return Result.success(concert);
    }
}
