package com.band.management.service;

import com.band.management.common.PageResult;
import com.band.management.entity.Song;
import java.util.List;

public interface AdminSongService {
    Long create(Song song);
    void delete(Long songId);
    void update(Song song);
    Song getById(Long songId);
    List<Song> getByAlbumId(Long albumId);
    PageResult<Song> page(int pageNum, int pageSize, Song condition);
    List<Song> list();
}
