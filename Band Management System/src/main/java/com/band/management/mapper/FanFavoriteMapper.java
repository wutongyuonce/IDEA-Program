package com.band.management.mapper;

import com.band.management.entity.Album;
import com.band.management.entity.Band;
import com.band.management.entity.Song;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 歌迷喜好Mapper接口（统一管理乐队、专辑、歌曲喜好）
 * 
 * @author Band Management Team
 */
@Mapper
public interface FanFavoriteMapper {

    // ==================== 乐队喜好 ====================
    
    /**
     * 添加喜欢的乐队
     */
    int insertFavoriteBand(@Param("fanId") Long fanId, @Param("bandId") Long bandId);

    /**
     * 取消喜欢的乐队
     */
    int deleteFavoriteBand(@Param("fanId") Long fanId, @Param("bandId") Long bandId);

    /**
     * 查询歌迷喜欢的乐队列表
     */
    List<Band> selectFavoriteBands(@Param("fanId") Long fanId);

    /**
     * 检查是否喜欢该乐队
     */
    int checkFavoriteBand(@Param("fanId") Long fanId, @Param("bandId") Long bandId);

    // ==================== 专辑喜好 ====================
    
    /**
     * 添加喜欢的专辑
     */
    int insertFavoriteAlbum(@Param("fanId") Long fanId, @Param("albumId") Long albumId);

    /**
     * 取消喜欢的专辑
     */
    int deleteFavoriteAlbum(@Param("fanId") Long fanId, @Param("albumId") Long albumId);

    /**
     * 查询歌迷喜欢的专辑列表
     */
    List<Album> selectFavoriteAlbums(@Param("fanId") Long fanId);

    /**
     * 检查是否喜欢该专辑
     */
    int checkFavoriteAlbum(@Param("fanId") Long fanId, @Param("albumId") Long albumId);

    // ==================== 歌曲喜好 ====================
    
    /**
     * 添加喜欢的歌曲
     */
    int insertFavoriteSong(@Param("fanId") Long fanId, @Param("songId") Long songId);

    /**
     * 取消喜欢的歌曲
     */
    int deleteFavoriteSong(@Param("fanId") Long fanId, @Param("songId") Long songId);

    /**
     * 查询歌迷喜欢的歌曲列表
     */
    List<Song> selectFavoriteSongs(@Param("fanId") Long fanId);

    /**
     * 检查是否喜欢该歌曲
     */
    int checkFavoriteSong(@Param("fanId") Long fanId, @Param("songId") Long songId);

    // ==================== 统计查询 ====================

    /**
     * 统计关注某乐队的歌迷数量
     */
    int countFansByBandId(@Param("bandId") Long bandId);

    /**
     * 查询关注某乐队的歌迷列表
     */
    List<com.band.management.entity.Fan> selectFansByBandId(@Param("bandId") Long bandId);

    /**
     * 统计喜欢某乐队专辑的歌迷数量（按专辑分组）
     */
    List<java.util.Map<String, Object>> countFansByAlbums(@Param("bandId") Long bandId);

    /**
     * 统计喜欢某乐队歌曲的歌迷数量（按歌曲分组）
     */
    List<java.util.Map<String, Object>> countFansBySongs(@Param("bandId") Long bandId);

    /**
     * 查询喜欢某专辑的歌迷ID列表
     */
    List<Long> selectFanIdsByAlbumId(@Param("albumId") Long albumId);

    /**
     * 查询喜欢某歌曲的歌迷ID列表
     */
    List<Long> selectFanIdsBySongId(@Param("songId") Long songId);
}
