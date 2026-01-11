package com.band.management.controller;

import com.band.management.common.Result;
import com.band.management.entity.*;
import com.band.management.service.AuthService;
import com.band.management.service.FanUserService;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 歌迷用户控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/fan")
public class FanUserController {

    @Autowired
    private FanUserService fanUserService;

    @Autowired
    private AuthService authService;

    /**
     * 获取当前登录歌迷的ID
     */
    private Long getCurrentFanId() {
        User currentUser = authService.getCurrentUser();
        return currentUser.getRelatedId();
    }

    /**
     * 获取当前歌迷信息
     */
    @GetMapping("/info")
    public Result<Fan> getFanInfo() {
        Long fanId = getCurrentFanId();
        log.info("歌迷用户获取信息: fanId={}", fanId);
        Fan fan = fanUserService.getFanInfo(fanId);
        return Result.success(fan);
    }

    /**
     * 更新歌迷信息
     */
    @PutMapping("/info")
    public Result<Void> updateFanInfo(@RequestBody Fan fan) {
        Long fanId = getCurrentFanId();
        log.info("歌迷用户更新信息: fanId={}", fanId);
        fan.setFanId(fanId);
        fanUserService.updateFanInfo(fan);
        return Result.success("更新成功");
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> params) {
        User currentUser = authService.getCurrentUser();
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        log.info("歌迷用户修改密码: userId={}", currentUser.getUserId());
        authService.changePassword(currentUser.getUserId(), oldPassword, newPassword);
        return Result.success("密码修改成功");
    }

    /**
     * 获取统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Long fanId = getCurrentFanId();
        log.info("歌迷用户获取统计数据: fanId={}", fanId);
        Map<String, Object> statistics = fanUserService.getStatistics(fanId);
        return Result.success(statistics);
    }

    // ==================== 发现页面 ====================

    /**
     * 获取推荐乐队列表
     */
    @GetMapping("/discovery/bands")
    public Result<PageInfo<Band>> getRecommendedBands(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name) {
        Long fanId = getCurrentFanId();
        log.info("歌迷用户获取推荐乐队: fanId={}, page={}, size={}, name={}", fanId, page, size, name);
        PageInfo<Band> bands = fanUserService.getRecommendedBands(fanId, page, size, name);
        return Result.success(bands);
    }

    /**
     * 获取专辑排行榜
     */
    @GetMapping("/discovery/albums/ranking")
    public Result<PageInfo<Album>> getTopAlbums(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long fanId = getCurrentFanId();
        log.info("歌迷用户获取专辑排行榜: fanId={}, page={}, size={}", fanId, page, size);
        PageInfo<Album> albums = fanUserService.getTopAlbums(fanId, page, size);
        return Result.success(albums);
    }

    /**
     * 获取所有专辑列表（用于选择）
     */
    @GetMapping("/discovery/albums")
    public Result<PageInfo<Album>> getAllAlbums(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "1000") Integer size,
            @RequestParam(required = false) String title) {
        log.info("歌迷用户获取所有专辑: page={}, size={}, title={}", page, size, title);
        PageInfo<Album> albums = fanUserService.getAllAlbums(page, size, title);
        return Result.success(albums);
    }

    /**
     * 获取所有歌曲列表（用于发现）
     */
    @GetMapping("/discovery/songs")
    public Result<PageInfo<Song>> getAllSongs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String title) {
        log.info("歌迷用户获取所有歌曲: page={}, size={}, title={}", page, size, title);
        PageInfo<Song> songs = fanUserService.getAllSongs(page, size, title);
        return Result.success(songs);
    }

    // ==================== 收藏管理 ====================

    /**
     * 关注乐队
     */
    @PostMapping("/favorites/bands")
    public Result<Void> followBand(@RequestBody Map<String, Long> params) {
        Long fanId = getCurrentFanId();
        Long bandId = params.get("bandId");
        log.info("歌迷用户关注乐队: fanId={}, bandId={}", fanId, bandId);
        fanUserService.followBand(fanId, bandId);
        return Result.success("关注成功");
    }

    /**
     * 取消关注乐队
     */
    @DeleteMapping("/favorites/bands/{bandId}")
    public Result<Void> unfollowBand(@PathVariable Long bandId) {
        Long fanId = getCurrentFanId();
        log.info("歌迷用户取消关注乐队: fanId={}, bandId={}", fanId, bandId);
        fanUserService.unfollowBand(fanId, bandId);
        return Result.success("取消关注成功");
    }

    /**
     * 获取关注的乐队列表
     */
    @GetMapping("/favorites/bands")
    public Result<PageInfo<Band>> getFavoriteBands(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long fanId = getCurrentFanId();
        log.info("歌迷用户获取关注乐队列表: fanId={}, page={}, size={}", fanId, page, size);
        PageInfo<Band> bands = fanUserService.getFavoriteBands(fanId, page, size);
        return Result.success(bands);
    }

    /**
     * 收藏专辑
     */
    @PostMapping("/favorites/albums")
    public Result<Void> favoriteAlbum(@RequestBody Map<String, Long> params) {
        Long fanId = getCurrentFanId();
        Long albumId = params.get("albumId");
        log.info("歌迷用户收藏专辑: fanId={}, albumId={}", fanId, albumId);
        fanUserService.favoriteAlbum(fanId, albumId);
        return Result.success("收藏成功");
    }

    /**
     * 取消收藏专辑
     */
    @DeleteMapping("/favorites/albums/{albumId}")
    public Result<Void> unfavoriteAlbum(@PathVariable Long albumId) {
        Long fanId = getCurrentFanId();
        log.info("歌迷用户取消收藏专辑: fanId={}, albumId={}", fanId, albumId);
        fanUserService.unfavoriteAlbum(fanId, albumId);
        return Result.success("取消收藏成功");
    }

    /**
     * 获取收藏的专辑列表
     */
    @GetMapping("/favorites/albums")
    public Result<PageInfo<Album>> getFavoriteAlbums(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long fanId = getCurrentFanId();
        log.info("歌迷用户获取收藏专辑列表: fanId={}, page={}, size={}", fanId, page, size);
        PageInfo<Album> albums = fanUserService.getFavoriteAlbums(fanId, page, size);
        return Result.success(albums);
    }

    /**
     * 收藏歌曲
     */
    @PostMapping("/favorites/songs")
    public Result<Void> favoriteSong(@RequestBody Map<String, Long> params) {
        Long fanId = getCurrentFanId();
        Long songId = params.get("songId");
        log.info("歌迷用户收藏歌曲: fanId={}, songId={}", fanId, songId);
        fanUserService.favoriteSong(fanId, songId);
        return Result.success("收藏成功");
    }

    /**
     * 取消收藏歌曲
     */
    @DeleteMapping("/favorites/songs/{songId}")
    public Result<Void> unfavoriteSong(@PathVariable Long songId) {
        Long fanId = getCurrentFanId();
        log.info("歌迷用户取消收藏歌曲: fanId={}, songId={}", fanId, songId);
        fanUserService.unfavoriteSong(fanId, songId);
        return Result.success("取消收藏成功");
    }

    /**
     * 获取收藏的歌曲列表
     */
    @GetMapping("/favorites/songs")
    public Result<PageInfo<Song>> getFavoriteSongs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long fanId = getCurrentFanId();
        log.info("歌迷用户获取收藏歌曲列表: fanId={}, page={}, size={}", fanId, page, size);
        PageInfo<Song> songs = fanUserService.getFavoriteSongs(fanId, page, size);
        return Result.success(songs);
    }

    // ==================== 演唱会参与管理 ====================

    /**
     * 获取参加的演唱会列表
     */
    @GetMapping("/favorites/concerts")
    public Result<PageInfo<Concert>> getAttendedConcerts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long fanId = getCurrentFanId();
        log.info("歌迷用户获取参加演唱会列表: fanId={}, page={}, size={}", fanId, page, size);
        PageInfo<Concert> concerts = fanUserService.getAttendedConcerts(fanId, page, size);
        return Result.success(concerts);
    }

    /**
     * 添加演唱会参与记录
     */
    @PostMapping("/favorites/concerts")
    public Result<Void> addConcertAttendance(@RequestBody Map<String, Long> params) {
        Long fanId = getCurrentFanId();
        Long concertId = params.get("concertId");
        log.info("歌迷用户添加演唱会参与记录: fanId={}, concertId={}", fanId, concertId);
        fanUserService.addConcertAttendance(fanId, concertId);
        return Result.success("添加成功");
    }

    /**
     * 删除演唱会参与记录
     */
    @DeleteMapping("/favorites/concerts/{concertId}")
    public Result<Void> removeConcertAttendance(@PathVariable Long concertId) {
        Long fanId = getCurrentFanId();
        log.info("歌迷用户删除演唱会参与记录: fanId={}, concertId={}", fanId, concertId);
        fanUserService.removeConcertAttendance(fanId, concertId);
        return Result.success("删除成功");
    }

    /**
     * 获取所有演唱会列表（用于选择）
     */
    @GetMapping("/concerts/all")
    public Result<PageInfo<Concert>> getAllConcerts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "1000") Integer size) {
        log.info("歌迷用户获取所有演唱会: page={}, size={}", page, size);
        PageInfo<Concert> concerts = fanUserService.getAllConcerts(page, size);
        return Result.success(concerts);
    }

    // ==================== 乐评管理 ====================

    /**
     * 获取我的乐评列表
     */
    @GetMapping("/reviews/my")
    public Result<PageInfo<AlbumReview>> getMyReviews(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long fanId = getCurrentFanId();
        log.info("歌迷用户获取乐评列表: fanId={}, page={}, size={}", fanId, page, size);
        PageInfo<AlbumReview> reviews = fanUserService.getMyReviews(fanId, page, size);
        return Result.success(reviews);
    }

    /**
     * 添加乐评
     */
    @PostMapping("/reviews")
    public Result<Long> addReview(@RequestBody @Valid AlbumReview review) {
        Long fanId = getCurrentFanId();
        log.info("歌迷用户添加乐评: fanId={}, albumId={}", fanId, review.getAlbumId());
        review.setFanId(fanId);
        Long reviewId = fanUserService.addReview(review);
        return Result.success("添加成功", reviewId);
    }

    /**
     * 更新乐评
     */
    @PutMapping("/reviews/{id}")
    public Result<Void> updateReview(@PathVariable("id") Long reviewId, @RequestBody @Valid AlbumReview review) {
        Long fanId = getCurrentFanId();
        log.info("歌迷用户更新乐评: fanId={}, reviewId={}", fanId, reviewId);
        review.setReviewId(reviewId);
        review.setFanId(fanId);
        fanUserService.updateReview(review);
        return Result.success("更新成功");
    }

    /**
     * 删除乐评
     */
    @DeleteMapping("/reviews/{id}")
    public Result<Void> deleteReview(@PathVariable("id") Long reviewId) {
        Long fanId = getCurrentFanId();
        log.info("歌迷用户删除乐评: fanId={}, reviewId={}", fanId, reviewId);
        fanUserService.deleteReview(fanId, reviewId);
        return Result.success("删除成功");
    }

    /**
     * 删除歌迷用户
     * 注意：删除歌迷会级联删除关注的乐队、收藏的专辑、收藏的歌曲、参加的演唱会记录以及发表的乐评
     */
    @DeleteMapping("/delete")
    public Result<Void> deleteFan() {
        Long fanId = getCurrentFanId();
        log.info("歌迷用户删除账号: fanId={}", fanId);
        fanUserService.deleteFan(fanId);
        return Result.success("账号删除成功");
    }
}
