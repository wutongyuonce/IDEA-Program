package com.band.management.controller;

import com.band.management.common.Result;
import com.band.management.entity.Album;
import com.band.management.entity.AlbumRanking;
import com.band.management.entity.Band;
import com.band.management.entity.Concert;
import com.band.management.entity.Song;
import com.band.management.service.FanDiscoveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 歌迷发现与浏览控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/fan/{fanId}/discovery")
public class FanDiscoveryController {

    @Autowired
    private FanDiscoveryService fanDiscoveryService;

    @GetMapping("/bands")
    public Result<List<Band>> discoverBands(@PathVariable("fanId") Long fanId) {
        log.info("歌迷发现乐队: {}", fanId);
        List<Band> bands = fanDiscoveryService.discoverBands(fanId);
        return Result.success(bands);
    }

    @GetMapping("/albums")
    public Result<List<Album>> discoverAlbums(@PathVariable("fanId") Long fanId) {
        log.info("歌迷发现专辑: {}", fanId);
        List<Album> albums = fanDiscoveryService.discoverAlbums(fanId);
        return Result.success(albums);
    }

    @GetMapping("/songs")
    public Result<List<Song>> discoverSongs(@PathVariable("fanId") Long fanId) {
        log.info("歌迷发现歌曲: {}", fanId);
        List<Song> songs = fanDiscoveryService.discoverSongs(fanId);
        return Result.success(songs);
    }

    @GetMapping("/concerts")
    public Result<List<Concert>> discoverConcerts(@PathVariable("fanId") Long fanId) {
        log.info("歌迷发现演唱会: {}", fanId);
        List<Concert> concerts = fanDiscoveryService.discoverConcerts(fanId);
        return Result.success(concerts);
    }

    @GetMapping("/search")
    public Result<List<Object>> search(
            @PathVariable("fanId") Long fanId,
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "type", required = false) String type) {
        log.info("歌迷搜索: fanId={}, keyword={}, type={}", fanId, keyword, type);
        List<Object> results = fanDiscoveryService.search(fanId, keyword, type);
        return Result.success(results);
    }

    @GetMapping("/ranking")
    public Result<List<AlbumRanking>> getAlbumRanking(@PathVariable("fanId") Long fanId) {
        log.info("查看专辑排行榜");
        List<AlbumRanking> rankings = fanDiscoveryService.getAlbumRanking();
        return Result.success(rankings);
    }
}
