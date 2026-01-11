package com.band.management.service;

import com.band.management.common.PageResult;
import com.band.management.entity.Song;

import java.util.List;

/**
 * 歌曲服务接口
 * 
 * @author Band Management Team
 */
public interface SongService {

    /**
     * 创建歌曲
     */
    Long create(Song song);

    /**
     * 删除歌曲
     */
    void delete(Long songId);

    /**
     * 更新歌曲信息
     */
    void update(Song song);

    /**
     * 根据ID查询歌曲
     */
    Song getById(Long songId);

    /**
     * 根据专辑ID查询歌曲列表
     */
    List<Song> getByAlbumId(Long albumId);

    /**
     * 分页查询歌曲列表
     */
    PageResult<Song> page(int pageNum, int pageSize, Song condition);

    /**
     * 查询所有歌曲
     */
    List<Song> list();
}
