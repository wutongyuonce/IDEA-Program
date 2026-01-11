package com.band.management.service.impl;

import com.band.management.common.PageResult;
import com.band.management.config.DataSourceContextHolder;
import com.band.management.entity.Album;
import com.band.management.entity.Band;
import com.band.management.entity.Song;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.AlbumMapper;
import com.band.management.mapper.BandMapper;
import com.band.management.mapper.SongMapper;
import com.band.management.service.BandSongService;
import com.band.management.util.StringUtil;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 乐队歌曲服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class BandSongServiceImpl implements BandSongService {

    @Autowired
    private SongMapper songMapper;

    @Autowired
    private BandMapper bandMapper;

    @Autowired
    private AlbumMapper albumMapper;

    @Override
    public List<Song> getMyBandSongs(Long bandId) {
        log.info("乐队查询自己的歌曲列表: {}", bandId);

        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        List<Album> albums = albumMapper.selectByBandId(bandId);
        if (albums.isEmpty()) {
            return List.of();
        }

        return songMapper.selectAll().stream()
                .filter(song -> albums.stream().anyMatch(album -> album.getAlbumId().equals(song.getAlbumId())))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<Song> getByAlbumId(Long bandId, Long albumId) {
        log.info("乐队查询专辑歌曲: bandId={}, albumId={}", bandId, albumId);

        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        Album album = albumMapper.selectById(albumId);
        if (album == null) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }

        if (!album.getBandId().equals(bandId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权查看其他乐队的歌曲");
        }

        return songMapper.selectByAlbumId(albumId);
    }

    @Override
    public PageResult<Song> page(Long bandId, int pageNum, int pageSize, Song condition) {
        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        PageHelper.startPage(pageNum, pageSize);
        List<Song> allSongs = condition == null ? songMapper.selectAll() : songMapper.selectByCondition(condition);
        
        List<Album> albums = albumMapper.selectByBandId(bandId);
        List<Song> bandSongs = allSongs.stream()
                .filter(song -> albums.stream().anyMatch(album -> album.getAlbumId().equals(song.getAlbumId())))
                .collect(java.util.stream.Collectors.toList());

        return PageResult.of(bandSongs);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSong(Long bandId, Song song) {
        log.info("乐队添加歌曲: bandId={}, songTitle={}", bandId, song.getTitle());

        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        if (StringUtil.isEmpty(song.getTitle())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "歌曲标题不能为空");
        }
        if (song.getAlbumId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "专辑ID不能为空");
        }

        Album album = albumMapper.selectById(song.getAlbumId());
        if (album == null) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }

        if (!album.getBandId().equals(bandId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权在其他乐队的专辑中添加歌曲");
        }

        int result = songMapper.insert(song);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "添加歌曲失败");
        }

        log.info("乐队添加歌曲成功，ID: {}", song.getSongId());
        return song.getSongId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSong(Long bandId, Song song) {
        log.info("乐队更新歌曲信息: bandId={}, songId={}", bandId, song.getSongId());

        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        Song existSong = songMapper.selectById(song.getSongId());
        if (existSong == null) {
            throw new BusinessException(ErrorCode.SONG_NOT_FOUND);
        }

        Album existAlbum = albumMapper.selectById(existSong.getAlbumId());
        if (existAlbum == null || !existAlbum.getBandId().equals(bandId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权操作其他乐队的歌曲");
        }

        if (song.getAlbumId() != null && !song.getAlbumId().equals(existSong.getAlbumId())) {
            Album newAlbum = albumMapper.selectById(song.getAlbumId());
            if (newAlbum == null) {
                throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
            }
            if (!newAlbum.getBandId().equals(bandId)) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权将歌曲移动到其他乐队的专辑");
            }
        }

        int result = songMapper.update(song);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "更新歌曲信息失败");
        }

        log.info("乐队更新歌曲信息成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSong(Long bandId, Long songId) {
        log.info("乐队删除歌曲: bandId={}, songId={}", bandId, songId);

        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        Song song = songMapper.selectById(songId);
        if (song == null) {
            throw new BusinessException(ErrorCode.SONG_NOT_FOUND);
        }

        Album album = albumMapper.selectById(song.getAlbumId());
        if (album == null || !album.getBandId().equals(bandId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权删除其他乐队的歌曲");
        }

        int result = songMapper.deleteById(songId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "删除歌曲失败");
        }

        log.info("乐队删除歌曲成功");
    }

    @Override
    public Song getById(Long bandId, Long songId) {
        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        Song song = songMapper.selectById(songId);
        if (song == null) {
            throw new BusinessException(ErrorCode.SONG_NOT_FOUND);
        }

        Album album = albumMapper.selectById(song.getAlbumId());
        if (album == null || !album.getBandId().equals(bandId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权查看其他乐队的歌曲");
        }

        return song;
    }
}
