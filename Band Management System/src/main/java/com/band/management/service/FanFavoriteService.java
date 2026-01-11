package com.band.management.service;

import com.band.management.entity.Album;
import com.band.management.entity.Band;
import com.band.management.entity.Concert;
import com.band.management.entity.Song;
import java.util.List;

/**
 * 歌迷喜好服务接口
 * 
 * @author Band Management Team
 */
public interface FanFavoriteService {
    
    // 乐队喜好
    List<Band> getFavoriteBands(Long fanId);
    void addFavoriteBand(Long fanId, Long bandId);
    void removeFavoriteBand(Long fanId, Long bandId);
    
    // 专辑喜好
    List<Album> getFavoriteAlbums(Long fanId);
    void addFavoriteAlbum(Long fanId, Long albumId);
    void removeFavoriteAlbum(Long fanId, Long albumId);
    
    // 歌曲喜好
    List<Song> getFavoriteSongs(Long fanId);
    void addFavoriteSong(Long fanId, Long songId);
    void removeFavoriteSong(Long fanId, Long songId);
    
    // 演唱会参与
    List<Concert> getAttendedConcerts(Long fanId);
    void addConcertAttendance(Long fanId, Long concertId);
    void removeConcertAttendance(Long fanId, Long concertId);
}
