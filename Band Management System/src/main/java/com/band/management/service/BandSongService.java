package com.band.management.service;

import com.band.management.common.PageResult;
import com.band.management.entity.Song;
import java.util.List;

/**
 * 乐队歌曲服务接口
 * 
 * @author Band Management Team
 */
public interface BandSongService {
    
    /**
     * 获取本乐队歌曲列表
     */
    List<Song> getMyBandSongs(Long bandId);
    
    /**
     * 根据专辑ID获取歌曲列表
     */
    List<Song> getByAlbumId(Long bandId, Long albumId);
    
    /**
     * 分页查询本乐队歌曲
     */
    PageResult<Song> page(Long bandId, int pageNum, int pageSize, Song condition);
    
    /**
     * 添加歌曲
     */
    Long createSong(Long bandId, Song song);
    
    /**
     * 更新歌曲信息
     */
    void updateSong(Long bandId, Song song);
    
    /**
     * 删除歌曲
     */
    void deleteSong(Long bandId, Long songId);
    
    /**
     * 根据ID查询歌曲
     */
    Song getById(Long bandId, Long songId);
}
