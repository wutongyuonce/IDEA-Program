package com.band.management.mapper;

import com.band.management.entity.Song;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 歌曲Mapper接口
 * 
 * @author Band Management Team
 */
@Mapper
public interface SongMapper {

    /**
     * 插入歌曲
     */
    int insert(Song song);

    /**
     * 根据ID删除歌曲
     */
    int deleteById(@Param("songId") Long songId);

    /**
     * 更新歌曲信息
     */
    int update(Song song);

    /**
     * 根据ID查询歌曲
     */
    Song selectById(@Param("songId") Long songId);

    /**
     * 查询所有歌曲
     */
    List<Song> selectAll();

    /**
     * 根据专辑ID查询歌曲列表
     */
    List<Song> selectByAlbumId(@Param("albumId") Long albumId);

    /**
     * 条件查询歌曲列表
     */
    List<Song> selectByCondition(Song song);

    /**
     * 统计歌曲数量
     */
    int count();

    /**
     * 根据条件统计数量
     */
    int countByCondition(Song song);

    /**
     * 查询所有歌曲（包含专辑和乐队信息）
     */
    List<Song> selectAllWithDetails();
}
