package com.band.management.controller;

import com.band.management.common.Result;
import com.band.management.entity.Album;
import com.band.management.entity.Band;
import com.band.management.entity.Concert;
import com.band.management.entity.Song;
import com.band.management.service.FanFavoriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 歌迷喜好控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/fan/{fanId}/favorites")
public class FanFavoriteController {

    @Autowired
    private FanFavoriteService fanFavoriteService;

    // ==================== 乐队喜好 ====================

    @GetMapping("/bands")
    public Result<List<Band>> getFavoriteBands(@PathVariable("fanId") Long fanId) {
        List<Band> bands = fanFavoriteService.getFavoriteBands(fanId);
        return Result.success(bands);
    }

    @PostMapping("/bands/{bandId}")
    public Result<Void> addFavoriteBand(
            @PathVariable("fanId") Long fanId,
            @PathVariable("bandId") Long bandId) {
        log.info("歌迷关注乐队: fanId={}, bandId={}", fanId, bandId);
        fanFavoriteService.addFavoriteBand(fanId, bandId);
        return Result.success("关注成功");
    }

    @DeleteMapping("/bands/{bandId}")
    public Result<Void> removeFavoriteBand(
            @PathVariable("fanId") Long fanId,
            @PathVariable("bandId") Long bandId) {
        log.info("歌迷取消关注乐队: fanId={}, bandId={}", fanId, bandId);
        fanFavoriteService.removeFavoriteBand(fanId, bandId);
        return Result.success("取消关注成功");
    }

    // ==================== 专辑喜好 ====================

    @GetMapping("/albums")
    public Result<List<Album>> getFavoriteAlbums(@PathVariable("fanId") Long fanId) {
        List<Album> albums = fanFavoriteService.getFavoriteAlbums(fanId);
        return Result.success(albums);
    }

    @PostMapping("/albums/{albumId}")
    public Result<Void> addFavoriteAlbum(
            @PathVariable("fanId") Long fanId,
            @PathVariable("albumId") Long albumId) {
        log.info("歌迷收藏专辑: fanId={}, albumId={}", fanId, albumId);
        fanFavoriteService.addFavoriteAlbum(fanId, albumId);
        return Result.success("收藏成功");
    }

    @DeleteMapping("/albums/{albumId}")
    public Result<Void> removeFavoriteAlbum(
            @PathVariable("fanId") Long fanId,
            @PathVariable("albumId") Long albumId) {
        log.info("歌迷取消收藏专辑: fanId={}, albumId={}", fanId, albumId);
        fanFavoriteService.removeFavoriteAlbum(fanId, albumId);
        return Result.success("取消收藏成功");
    }

    // ==================== 歌曲喜好 ====================

    @GetMapping("/songs")
    public Result<List<Song>> getFavoriteSongs(@PathVariable("fanId") Long fanId) {
        List<Song> songs = fanFavoriteService.getFavoriteSongs(fanId);
        return Result.success(songs);
    }

    @PostMapping("/songs/{songId}")
    public Result<Void> addFavoriteSong(
            @PathVariable("fanId") Long fanId,
            @PathVariable("songId") Long songId) {
        log.info("歌迷收藏歌曲: fanId={}, songId={}", fanId, songId);
        fanFavoriteService.addFavoriteSong(fanId, songId);
        return Result.success("收藏成功");
    }

    @DeleteMapping("/songs/{songId}")
    public Result<Void> removeFavoriteSong(
            @PathVariable("fanId") Long fanId,
            @PathVariable("songId") Long songId) {
        log.info("歌迷取消收藏歌曲: fanId={}, songId={}", fanId, songId);
        fanFavoriteService.removeFavoriteSong(fanId, songId);
        return Result.success("取消收藏成功");
    }

    // ==================== 演唱会参与 ====================

    @GetMapping("/concerts")
    public Result<List<Concert>> getAttendedConcerts(@PathVariable("fanId") Long fanId) {
        List<Concert> concerts = fanFavoriteService.getAttendedConcerts(fanId);
        return Result.success(concerts);
    }

    @PostMapping("/concerts/{concertId}")
    public Result<Void> addConcertAttendance(
            @PathVariable("fanId") Long fanId,
            @PathVariable("concertId") Long concertId) {
        log.info("歌迷添加演唱会参与记录: fanId={}, concertId={}", fanId, concertId);
        fanFavoriteService.addConcertAttendance(fanId, concertId);
        return Result.success("添加成功");
    }

    @DeleteMapping("/concerts/{concertId}")
    public Result<Void> removeConcertAttendance(
            @PathVariable("fanId") Long fanId,
            @PathVariable("concertId") Long concertId) {
        log.info("歌迷删除演唱会参与记录: fanId={}, concertId={}", fanId, concertId);
        fanFavoriteService.removeConcertAttendance(fanId, concertId);
        return Result.success("删除成功");
    }
}
