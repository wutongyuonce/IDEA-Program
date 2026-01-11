package com.band.management.service.impl;

import com.band.management.common.PageResult;
import com.band.management.config.DataSourceContextHolder;
import com.band.management.entity.Album;
import com.band.management.entity.Song;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.AlbumMapper;
import com.band.management.mapper.SongMapper;
import com.band.management.service.AdminSongService;
import com.band.management.util.StringUtil;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理员歌曲服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class AdminSongServiceImpl implements AdminSongService {

    @Autowired
    private SongMapper songMapper;

    @Autowired
    private AlbumMapper albumMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Song song) {
        log.info("管理员创建歌曲: {}", song.getTitle());

        DataSourceContextHolder.setDataSourceType("admin");

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

        int result = songMapper.insert(song);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "创建歌曲失败");
        }

        log.info("管理员创建歌曲成功，ID: {}", song.getSongId());
        return song.getSongId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long songId) {
        log.info("管理员删除歌曲: {}", songId);

        DataSourceContextHolder.setDataSourceType("admin");

        Song song = songMapper.selectById(songId);
        if (song == null) {
            throw new BusinessException(ErrorCode.SONG_NOT_FOUND);
        }

        int result = songMapper.deleteById(songId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "删除歌曲失败");
        }

        log.info("管理员删除歌曲成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Song song) {
        log.info("管理员更新歌曲: {}", song.getSongId());

        DataSourceContextHolder.setDataSourceType("admin");

        Song existSong = songMapper.selectById(song.getSongId());
        if (existSong == null) {
            throw new BusinessException(ErrorCode.SONG_NOT_FOUND);
        }

        if (song.getAlbumId() != null && !song.getAlbumId().equals(existSong.getAlbumId())) {
            Album album = albumMapper.selectById(song.getAlbumId());
            if (album == null) {
                throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
            }
        }

        int result = songMapper.update(song);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "更新歌曲失败");
        }

        log.info("管理员更新歌曲成功");
    }

    @Override
    public Song getById(Long songId) {
        DataSourceContextHolder.setDataSourceType("admin");

        Song song = songMapper.selectById(songId);
        if (song == null) {
            throw new BusinessException(ErrorCode.SONG_NOT_FOUND);
        }
        return song;
    }

    @Override
    public List<Song> getByAlbumId(Long albumId) {
        DataSourceContextHolder.setDataSourceType("admin");

        Album album = albumMapper.selectById(albumId);
        if (album == null) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }

        return songMapper.selectByAlbumId(albumId);
    }

    @Override
    public PageResult<Song> page(int pageNum, int pageSize, Song condition) {
        DataSourceContextHolder.setDataSourceType("admin");

        PageHelper.startPage(pageNum, pageSize);
        List<Song> list = condition == null ? songMapper.selectAll() : songMapper.selectByCondition(condition);
        return PageResult.of(list);
    }

    @Override
    public List<Song> list() {
        DataSourceContextHolder.setDataSourceType("admin");
        return songMapper.selectAll();
    }
}
