package com.band.management.service;

import com.band.management.entity.Album;
import com.band.management.entity.AlbumRanking;
import com.band.management.entity.Band;
import com.band.management.entity.Concert;
import com.band.management.entity.Song;

import java.util.List;

/**
 * 歌迷发现与浏览服务接口
 * 
 * @author Band Management Team
 */
public interface FanDiscoveryService {

    /**
     * 发现乐队（排除已关注）
     */
    List<Band> discoverBands(Long fanId);

    /**
     * 发现专辑（排除已收藏）
     */
    List<Album> discoverAlbums(Long fanId);

    /**
     * 发现歌曲（排除已收藏）
     */
    List<Song> discoverSongs(Long fanId);

    /**
     * 发现演唱会（排除已参加）
     */
    List<Concert> discoverConcerts(Long fanId);

    /**
     * 搜索（多条件）
     */
    List<Object> search(Long fanId, String keyword, String type);

    /**
     * 查看专辑排行榜
     */
    List<AlbumRanking> getAlbumRanking();
}
