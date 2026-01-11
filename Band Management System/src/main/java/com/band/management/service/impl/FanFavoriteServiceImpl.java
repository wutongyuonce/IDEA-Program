package com.band.management.service.impl;

import com.band.management.config.DataSourceContextHolder;
import com.band.management.entity.*;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.*;
import com.band.management.service.FanFavoriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 歌迷喜好服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class FanFavoriteServiceImpl implements FanFavoriteService {

    @Autowired
    private FanMapper fanMapper;

    @Autowired
    private BandMapper bandMapper;

    @Autowired
    private AlbumMapper albumMapper;

    @Autowired
    private SongMapper songMapper;

    @Autowired
    private ConcertMapper concertMapper;

    @Autowired
    private FanFavoriteMapper fanFavoriteMapper;

    @Autowired
    private ConcertAttendanceMapper concertAttendanceMapper;

    @Override
    public List<Band> getFavoriteBands(Long fanId) {
        log.info("歌迷查询喜欢的乐队列表: {}", fanId);

        DataSourceContextHolder.setDataSourceType("fan");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        return fanFavoriteMapper.selectFavoriteBands(fanId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFavoriteBand(Long fanId, Long bandId) {
        log.info("歌迷关注乐队: fanId={}, bandId={}", fanId, bandId);

        DataSourceContextHolder.setDataSourceType("fan");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        int check = fanFavoriteMapper.checkFavoriteBand(fanId, bandId);
        if (check > 0) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS.getCode(), "已关注该乐队");
        }

        int result = fanFavoriteMapper.insertFavoriteBand(fanId, bandId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "关注乐队失败");
        }

        log.info("歌迷关注乐队成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFavoriteBand(Long fanId, Long bandId) {
        log.info("歌迷取消关注乐队: fanId={}, bandId={}", fanId, bandId);

        DataSourceContextHolder.setDataSourceType("fan");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        int result = fanFavoriteMapper.deleteFavoriteBand(fanId, bandId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "取消关注失败");
        }

        log.info("歌迷取消关注乐队成功");
    }

    @Override
    public List<Album> getFavoriteAlbums(Long fanId) {
        log.info("歌迷查询喜欢的专辑列表: {}", fanId);

        DataSourceContextHolder.setDataSourceType("fan");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        return fanFavoriteMapper.selectFavoriteAlbums(fanId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFavoriteAlbum(Long fanId, Long albumId) {
        log.info("歌迷收藏专辑: fanId={}, albumId={}", fanId, albumId);

        DataSourceContextHolder.setDataSourceType("fan");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        Album album = albumMapper.selectById(albumId);
        if (album == null) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }

        int check = fanFavoriteMapper.checkFavoriteAlbum(fanId, albumId);
        if (check > 0) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS.getCode(), "已收藏该专辑");
        }

        int result = fanFavoriteMapper.insertFavoriteAlbum(fanId, albumId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "收藏专辑失败");
        }

        log.info("歌迷收藏专辑成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFavoriteAlbum(Long fanId, Long albumId) {
        log.info("歌迷取消收藏专辑: fanId={}, albumId={}", fanId, albumId);

        DataSourceContextHolder.setDataSourceType("fan");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        int result = fanFavoriteMapper.deleteFavoriteAlbum(fanId, albumId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "取消收藏失败");
        }

        log.info("歌迷取消收藏专辑成功");
    }

    @Override
    public List<Song> getFavoriteSongs(Long fanId) {
        log.info("歌迷查询喜欢的歌曲列表: {}", fanId);

        DataSourceContextHolder.setDataSourceType("fan");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        return fanFavoriteMapper.selectFavoriteSongs(fanId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFavoriteSong(Long fanId, Long songId) {
        log.info("歌迷收藏歌曲: fanId={}, songId={}", fanId, songId);

        DataSourceContextHolder.setDataSourceType("fan");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        Song song = songMapper.selectById(songId);
        if (song == null) {
            throw new BusinessException(ErrorCode.SONG_NOT_FOUND);
        }

        int check = fanFavoriteMapper.checkFavoriteSong(fanId, songId);
        if (check > 0) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS.getCode(), "已收藏该歌曲");
        }

        int result = fanFavoriteMapper.insertFavoriteSong(fanId, songId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "收藏歌曲失败");
        }

        log.info("歌迷收藏歌曲成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFavoriteSong(Long fanId, Long songId) {
        log.info("歌迷取消收藏歌曲: fanId={}, songId={}", fanId, songId);

        DataSourceContextHolder.setDataSourceType("fan");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        int result = fanFavoriteMapper.deleteFavoriteSong(fanId, songId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "取消收藏失败");
        }

        log.info("歌迷取消收藏歌曲成功");
    }

    @Override
    public List<Concert> getAttendedConcerts(Long fanId) {
        log.info("歌迷查询参加的演唱会列表: {}", fanId);

        DataSourceContextHolder.setDataSourceType("fan");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        return concertAttendanceMapper.selectByFanId(fanId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addConcertAttendance(Long fanId, Long concertId) {
        log.info("歌迷添加演唱会参与记录: fanId={}, concertId={}", fanId, concertId);

        DataSourceContextHolder.setDataSourceType("fan");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        Concert concert = concertMapper.selectById(concertId);
        if (concert == null) {
            throw new BusinessException(ErrorCode.CONCERT_NOT_FOUND);
        }

        int check = concertAttendanceMapper.checkAttendance(fanId, concertId);
        if (check > 0) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS.getCode(), "已添加该演唱会参与记录");
        }

        ConcertAttendance attendance = new ConcertAttendance();
        attendance.setFanId(fanId);
        attendance.setConcertId(concertId);

        int result = concertAttendanceMapper.insert(attendance);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "添加参与记录失败");
        }

        log.info("歌迷添加演唱会参与记录成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeConcertAttendance(Long fanId, Long concertId) {
        log.info("歌迷删除演唱会参与记录: fanId={}, concertId={}", fanId, concertId);

        DataSourceContextHolder.setDataSourceType("fan");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        int result = concertAttendanceMapper.delete(fanId, concertId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "删除参与记录失败");
        }

        log.info("歌迷删除演唱会参与记录成功");
    }
}
