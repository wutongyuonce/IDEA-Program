package com.band.management.controller;

import com.band.management.common.Result;
import com.band.management.entity.*;
import com.band.management.service.AuthService;
import com.band.management.service.BandUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 乐队用户控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/band")
public class BandUserController {

    @Autowired
    private BandUserService bandUserService;

    @Autowired
    private AuthService authService;

    /**
     * 获取当前登录乐队的ID
     */
    private Long getCurrentBandId() {
        User currentUser = authService.getCurrentUser();
        return currentUser.getRelatedId();
    }

    /**
     * 获取当前乐队信息
     */
    @GetMapping("/info")
    public Result<Band> getBandInfo() {
        Long bandId = getCurrentBandId();
        log.info("乐队用户获取信息: bandId={}", bandId);
        Band band = bandUserService.getBandInfo(bandId);
        return Result.success(band);
    }

    /**
     * 更新乐队信息
     */
    @PutMapping("/info")
    public Result<Void> updateBandInfo(@RequestBody Band band) {
        Long bandId = getCurrentBandId();
        log.info("乐队用户更新信息: bandId={}", bandId);
        band.setBandId(bandId);
        bandUserService.updateBandInfo(band);
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
        log.info("乐队用户修改密码: userId={}", currentUser.getUserId());
        authService.changePassword(currentUser.getUserId(), oldPassword, newPassword);
        return Result.success("密码修改成功");
    }

    /**
     * 获取统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Long bandId = getCurrentBandId();
        log.info("乐队用户获取统计数据: bandId={}", bandId);
        Map<String, Object> statistics = bandUserService.getStatistics(bandId);
        return Result.success(statistics);
    }

    // ==================== 成员管理 ====================

    /**
     * 获取成员列表
     */
    @GetMapping("/members")
    public Result<List<Member>> getMembers() {
        Long bandId = getCurrentBandId();
        log.info("乐队用户获取成员列表: bandId={}", bandId);
        List<Member> members = bandUserService.getMembers(bandId);
        return Result.success(members);
    }

    /**
     * 添加成员
     */
    @PostMapping("/members")
    public Result<Long> addMember(@RequestBody @Valid Member member) {
        Long bandId = getCurrentBandId();
        log.info("乐队用户添加成员: bandId={}, memberName={}", bandId, member.getName());
        member.setBandId(bandId);
        Long memberId = bandUserService.addMember(member);
        return Result.success("添加成功", memberId);
    }

    /**
     * 更新成员
     */
    @PutMapping("/members/{id}")
    public Result<Void> updateMember(@PathVariable("id") Long memberId, @RequestBody @Valid Member member) {
        Long bandId = getCurrentBandId();
        log.info("乐队用户更新成员: bandId={}, memberId={}", bandId, memberId);
        member.setMemberId(memberId);
        member.setBandId(bandId);
        bandUserService.updateMember(member);
        return Result.success("更新成功");
    }

    /**
     * 删除成员
     */
    @DeleteMapping("/members/{id}")
    public Result<Void> deleteMember(@PathVariable("id") Long memberId) {
        Long bandId = getCurrentBandId();
        log.info("乐队用户删除成员: bandId={}, memberId={}", bandId, memberId);
        bandUserService.deleteMember(bandId, memberId);
        return Result.success("删除成功");
    }

    // ==================== 专辑管理 ====================

    /**
     * 获取专辑列表
     */
    @GetMapping("/albums")
    public Result<List<Album>> getAlbums() {
        Long bandId = getCurrentBandId();
        log.info("乐队用户获取专辑列表: bandId={}", bandId);
        List<Album> albums = bandUserService.getAlbums(bandId);
        return Result.success(albums);
    }

    /**
     * 获取最新专辑
     */
    @GetMapping("/albums/recent")
    public Result<List<Album>> getRecentAlbums() {
        Long bandId = getCurrentBandId();
        log.info("乐队用户获取最新专辑: bandId={}", bandId);
        List<Album> albums = bandUserService.getRecentAlbums(bandId);
        return Result.success(albums);
    }

    /**
     * 添加专辑
     */
    @PostMapping("/albums")
    public Result<Long> addAlbum(@RequestBody @Valid Album album) {
        Long bandId = getCurrentBandId();
        log.info("乐队用户添加专辑: bandId={}, albumTitle={}", bandId, album.getTitle());
        album.setBandId(bandId);
        Long albumId = bandUserService.addAlbum(album);
        return Result.success("添加成功", albumId);
    }

    /**
     * 更新专辑
     */
    @PutMapping("/albums/{id}")
    public Result<Void> updateAlbum(@PathVariable("id") Long albumId, @RequestBody @Valid Album album) {
        Long bandId = getCurrentBandId();
        log.info("乐队用户更新专辑: bandId={}, albumId={}", bandId, albumId);
        album.setAlbumId(albumId);
        album.setBandId(bandId);
        bandUserService.updateAlbum(album);
        return Result.success("更新成功");
    }

    /**
     * 删除专辑
     */
    @DeleteMapping("/albums/{id}")
    public Result<Void> deleteAlbum(@PathVariable("id") Long albumId) {
        Long bandId = getCurrentBandId();
        log.info("乐队用户删除专辑: bandId={}, albumId={}", bandId, albumId);
        bandUserService.deleteAlbum(bandId, albumId);
        return Result.success("删除成功");
    }

    /**
     * 获取专辑的歌曲列表
     */
    @GetMapping("/albums/{id}/songs")
    public Result<List<Song>> getAlbumSongs(@PathVariable("id") Long albumId) {
        Long bandId = getCurrentBandId();
        log.info("乐队用户获取专辑歌曲: bandId={}, albumId={}", bandId, albumId);
        List<Song> songs = bandUserService.getAlbumSongs(bandId, albumId);
        return Result.success(songs);
    }

    /**
     * 获取专辑的评论列表
     */
    @GetMapping("/albums/{id}/reviews")
    public Result<List<AlbumReview>> getAlbumReviews(@PathVariable("id") Long albumId) {
        Long bandId = getCurrentBandId();
        log.info("乐队用户获取专辑评论: bandId={}, albumId={}", bandId, albumId);
        List<AlbumReview> reviews = bandUserService.getAlbumReviews(bandId, albumId);
        return Result.success(reviews);
    }

    // ==================== 歌曲管理 ====================

    /**
     * 获取歌曲列表
     */
    @GetMapping("/songs")
    public Result<List<Song>> getSongs() {
        Long bandId = getCurrentBandId();
        log.info("乐队用户获取歌曲列表: bandId={}", bandId);
        List<Song> songs = bandUserService.getSongs(bandId);
        return Result.success(songs);
    }

    /**
     * 添加歌曲
     */
    @PostMapping("/songs")
    public Result<Long> addSong(@RequestBody @Valid Song song) {
        Long bandId = getCurrentBandId();
        log.info("乐队用户添加歌曲: bandId={}, songTitle={}", bandId, song.getTitle());
        Long songId = bandUserService.addSong(bandId, song);
        return Result.success("添加成功", songId);
    }

    /**
     * 更新歌曲
     */
    @PutMapping("/songs/{id}")
    public Result<Void> updateSong(@PathVariable("id") Long songId, @RequestBody @Valid Song song) {
        Long bandId = getCurrentBandId();
        log.info("乐队用户更新歌曲: bandId={}, songId={}", bandId, songId);
        song.setSongId(songId);
        bandUserService.updateSong(bandId, song);
        return Result.success("更新成功");
    }

    /**
     * 删除歌曲
     */
    @DeleteMapping("/songs/{id}")
    public Result<Void> deleteSong(@PathVariable("id") Long songId) {
        Long bandId = getCurrentBandId();
        log.info("乐队用户删除歌曲: bandId={}, songId={}", bandId, songId);
        bandUserService.deleteSong(bandId, songId);
        return Result.success("删除成功");
    }

    // ==================== 演唱会管理 ====================

    /**
     * 获取演唱会列表
     */
    @GetMapping("/concerts")
    public Result<List<Concert>> getConcerts() {
        Long bandId = getCurrentBandId();
        log.info("乐队用户获取演唱会列表: bandId={}", bandId);
        List<Concert> concerts = bandUserService.getConcerts(bandId);
        return Result.success(concerts);
    }

    /**
     * 获取最新演唱会
     */
    @GetMapping("/concerts/recent")
    public Result<List<Concert>> getRecentConcerts() {
        Long bandId = getCurrentBandId();
        log.info("乐队用户获取最新演唱会: bandId={}", bandId);
        List<Concert> concerts = bandUserService.getRecentConcerts(bandId);
        return Result.success(concerts);
    }

    /**
     * 添加演唱会
     */
    @PostMapping("/concerts")
    public Result<Long> addConcert(@RequestBody @Valid Concert concert) {
        Long bandId = getCurrentBandId();
        log.info("乐队用户添加演唱会: bandId={}, concertTitle={}", bandId, concert.getTitle());
        concert.setBandId(bandId);
        Long concertId = bandUserService.addConcert(concert);
        return Result.success("添加成功", concertId);
    }

    /**
     * 更新演唱会
     */
    @PutMapping("/concerts/{id}")
    public Result<Void> updateConcert(@PathVariable("id") Long concertId, @RequestBody @Valid Concert concert) {
        Long bandId = getCurrentBandId();
        log.info("乐队用户更新演唱会: bandId={}, concertId={}", bandId, concertId);
        concert.setConcertId(concertId);
        concert.setBandId(bandId);
        bandUserService.updateConcert(concert);
        return Result.success("更新成功");
    }

    /**
     * 删除演唱会
     */
    @DeleteMapping("/concerts/{id}")
    public Result<Void> deleteConcert(@PathVariable("id") Long concertId) {
        Long bandId = getCurrentBandId();
        log.info("乐队用户删除演唱会: bandId={}, concertId={}", bandId, concertId);
        bandUserService.deleteConcert(bandId, concertId);
        return Result.success("删除成功");
    }

    // ==================== 歌迷数据 ====================

    /**
     * 获取关注的歌迷列表
     */
    @GetMapping("/fans")
    public Result<List<Fan>> getFans() {
        Long bandId = getCurrentBandId();
        log.info("乐队用户获取歌迷列表: bandId={}", bandId);
        List<Fan> fans = bandUserService.getFans(bandId);
        return Result.success(fans);
    }

    /**
     * 获取歌迷统计数据
     */
    @GetMapping("/fans/statistics")
    public Result<Map<String, Object>> getFanStatistics() {
        Long bandId = getCurrentBandId();
        log.info("乐队用户获取歌迷统计数据: bandId={}", bandId);
        Map<String, Object> statistics = bandUserService.getFanStatistics(bandId);
        return Result.success(statistics);
    }

    /**
     * 获取歌迷年龄分布
     */
    @GetMapping("/fans/age-distribution")
    public Result<List<Map<String, Object>>> getFanAgeDistribution() {
        Long bandId = getCurrentBandId();
        log.info("乐队用户获取歌迷年龄分布: bandId={}", bandId);
        List<Map<String, Object>> distribution = bandUserService.getFanAgeDistribution(bandId);
        return Result.success(distribution);
    }

    /**
     * 获取歌迷学历分布
     */
    @GetMapping("/fans/education-distribution")
    public Result<List<Map<String, Object>>> getFanEducationDistribution() {
        Long bandId = getCurrentBandId();
        log.info("乐队用户获取歌迷学历分布: bandId={}", bandId);
        List<Map<String, Object>> distribution = bandUserService.getFanEducationDistribution(bandId);
        return Result.success(distribution);
    }

    /**
     * 获取喜欢乐队专辑的歌迷统计
     */
    @GetMapping("/fans/albums")
    public Result<Map<String, Object>> getFansByAlbums() {
        Long bandId = getCurrentBandId();
        log.info("乐队用户获取喜欢专辑的歌迷统计: bandId={}", bandId);
        Map<String, Object> result = bandUserService.getFansByAlbums(bandId);
        return Result.success(result);
    }

    /**
     * 获取喜欢乐队歌曲的歌迷统计
     */
    @GetMapping("/fans/songs")
    public Result<Map<String, Object>> getFansBySongs() {
        Long bandId = getCurrentBandId();
        log.info("乐队用户获取喜欢歌曲的歌迷统计: bandId={}", bandId);
        Map<String, Object> result = bandUserService.getFansBySongs(bandId);
        return Result.success(result);
    }

    /**
     * 删除乐队
     * 注意：删除乐队会级联删除专辑、歌曲、演唱会以及歌迷关注数据
     * 如果乐队有成员，则无法删除
     */
    @DeleteMapping("/delete")
    public Result<Void> deleteBand() {
        Long bandId = getCurrentBandId();
        log.info("乐队用户删除乐队: bandId={}", bandId);
        bandUserService.deleteBand(bandId);
        return Result.success("乐队删除成功");
    }
}
