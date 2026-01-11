package com.band.management.service;

import com.band.management.entity.*;
import com.github.pagehelper.PageInfo;

import java.util.Map;

/**
 * 歌迷用户服务接口
 * 
 * @author Band Management Team
 */
public interface FanUserService {

    /**
     * 获取歌迷信息
     */
    Fan getFanInfo(Long fanId);

    /**
     * 更新歌迷信息
     */
    void updateFanInfo(Fan fan);

    /**
     * 获取统计数据
     */
    Map<String, Object> getStatistics(Long fanId);

    /**
     * 获取推荐乐队列表（分页）
     * 排除已关注的乐队，按关注人数排序
     * @param name 乐队名称（可选，用于搜索）
     */
    PageInfo<Band> getRecommendedBands(Long fanId, Integer page, Integer size, String name);

    /**
     * 获取专辑排行榜（分页）
     * @param fanId 歌迷ID，用于判断是否已收藏
     */
    PageInfo<Album> getTopAlbums(Long fanId, Integer page, Integer size);

    /**
     * 获取所有专辑列表（用于选择）
     * @param title 专辑标题（可选，用于搜索）
     */
    PageInfo<Album> getAllAlbums(Integer page, Integer size, String title);

    /**
     * 获取所有歌曲列表（用于发现）
     * @param title 歌曲标题（可选，用于搜索）
     */
    PageInfo<Song> getAllSongs(Integer page, Integer size, String title);

    /**
     * 获取我的乐评列表（分页）
     */
    PageInfo<AlbumReview> getMyReviews(Long fanId, Integer page, Integer size);

    /**
     * 关注乐队
     */
    void followBand(Long fanId, Long bandId);

    /**
     * 取消关注乐队
     */
    void unfollowBand(Long fanId, Long bandId);

    /**
     * 获取关注的乐队列表
     */
    PageInfo<Band> getFavoriteBands(Long fanId, Integer page, Integer size);

    /**
     * 收藏专辑
     */
    void favoriteAlbum(Long fanId, Long albumId);

    /**
     * 取消收藏专辑
     */
    void unfavoriteAlbum(Long fanId, Long albumId);

    /**
     * 获取收藏的专辑列表
     */
    PageInfo<Album> getFavoriteAlbums(Long fanId, Integer page, Integer size);

    /**
     * 收藏歌曲
     */
    void favoriteSong(Long fanId, Long songId);

    /**
     * 取消收藏歌曲
     */
    void unfavoriteSong(Long fanId, Long songId);

    /**
     * 获取收藏的歌曲列表
     */
    PageInfo<Song> getFavoriteSongs(Long fanId, Integer page, Integer size);

    /**
     * 获取参加的演唱会列表
     */
    PageInfo<Concert> getAttendedConcerts(Long fanId, Integer page, Integer size);

    /**
     * 添加演唱会参与记录
     */
    void addConcertAttendance(Long fanId, Long concertId);

    /**
     * 删除演唱会参与记录
     */
    void removeConcertAttendance(Long fanId, Long concertId);

    /**
     * 获取所有演唱会列表（用于选择）
     */
    PageInfo<Concert> getAllConcerts(Integer page, Integer size);

    /**
     * 添加乐评
     */
    Long addReview(AlbumReview review);

    /**
     * 更新乐评
     */
    void updateReview(AlbumReview review);

    /**
     * 删除乐评
     */
    void deleteReview(Long fanId, Long reviewId);

    /**
     * 删除歌迷用户
     * 注意：删除歌迷会级联删除关注的乐队、收藏的专辑、收藏的歌曲、参加的演唱会记录以及发表的乐评
     * 
     * @param fanId 歌迷ID
     */
    void deleteFan(Long fanId);
}
