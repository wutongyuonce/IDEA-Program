package com.band.management.service;

import com.band.management.entity.*;

import java.util.List;
import java.util.Map;

/**
 * 乐队用户服务接口
 * 
 * @author Band Management Team
 */
public interface BandUserService {

    /**
     * 获取乐队信息
     */
    Band getBandInfo(Long bandId);

    /**
     * 更新乐队信息
     */
    void updateBandInfo(Band band);

    /**
     * 获取统计数据
     */
    Map<String, Object> getStatistics(Long bandId);

    // ==================== 成员管理 ====================

    /**
     * 获取成员列表
     */
    List<Member> getMembers(Long bandId);

    /**
     * 添加成员
     */
    Long addMember(Member member);

    /**
     * 更新成员
     */
    void updateMember(Member member);

    /**
     * 删除成员
     */
    void deleteMember(Long bandId, Long memberId);

    // ==================== 专辑管理 ====================

    /**
     * 获取专辑列表
     */
    List<Album> getAlbums(Long bandId);

    /**
     * 获取最新专辑
     */
    List<Album> getRecentAlbums(Long bandId);

    /**
     * 添加专辑
     */
    Long addAlbum(Album album);

    /**
     * 更新专辑
     */
    void updateAlbum(Album album);

    /**
     * 删除专辑
     */
    void deleteAlbum(Long bandId, Long albumId);

    /**
     * 获取专辑的歌曲列表
     */
    List<Song> getAlbumSongs(Long bandId, Long albumId);

    /**
     * 获取专辑的评论列表
     */
    List<AlbumReview> getAlbumReviews(Long bandId, Long albumId);

    // ==================== 歌曲管理 ====================

    /**
     * 获取歌曲列表
     */
    List<Song> getSongs(Long bandId);

    /**
     * 添加歌曲
     */
    Long addSong(Long bandId, Song song);

    /**
     * 更新歌曲
     */
    void updateSong(Long bandId, Song song);

    /**
     * 删除歌曲
     */
    void deleteSong(Long bandId, Long songId);

    // ==================== 演唱会管理 ====================

    /**
     * 获取演唱会列表
     */
    List<Concert> getConcerts(Long bandId);

    /**
     * 获取最新演唱会
     */
    List<Concert> getRecentConcerts(Long bandId);

    /**
     * 添加演唱会
     */
    Long addConcert(Concert concert);

    /**
     * 更新演唱会
     */
    void updateConcert(Concert concert);

    /**
     * 删除演唱会
     */
    void deleteConcert(Long bandId, Long concertId);

    // ==================== 歌迷数据 ====================

    /**
     * 获取关注的歌迷列表
     */
    List<Fan> getFans(Long bandId);

    /**
     * 获取歌迷统计数据
     */
    Map<String, Object> getFanStatistics(Long bandId);

    /**
     * 获取歌迷年龄分布
     */
    List<Map<String, Object>> getFanAgeDistribution(Long bandId);

    /**
     * 获取歌迷学历分布
     */
    List<Map<String, Object>> getFanEducationDistribution(Long bandId);

    /**
     * 获取喜欢乐队专辑的歌迷统计（按专辑分组）
     */
    Map<String, Object> getFansByAlbums(Long bandId);

    /**
     * 获取喜欢乐队歌曲的歌迷统计（按歌曲分组）
     */
    Map<String, Object> getFansBySongs(Long bandId);

    /**
     * 删除乐队
     * 注意：删除乐队会级联删除专辑、歌曲、演唱会以及歌迷关注数据
     * 如果乐队有成员，则无法删除
     * 
     * @param bandId 乐队ID
     */
    void deleteBand(Long bandId);
}
