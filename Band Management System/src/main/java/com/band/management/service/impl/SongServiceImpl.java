package com.band.management.service.impl;

import com.band.management.common.PageResult;
import com.band.management.entity.Album;
import com.band.management.entity.Song;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.AlbumMapper;
import com.band.management.mapper.SongMapper;
import com.band.management.service.SongService;
import com.band.management.util.StringUtil;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 歌曲服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class SongServiceImpl implements SongService {

    @Autowired
    private SongMapper songMapper;

    @Autowired
    private AlbumMapper albumMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Song song) {
        log.info("创建歌曲: {}", song.getTitle());

        // 参数校验
        if (StringUtil.isEmpty(song.getTitle())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "歌曲标题不能为空");
        }
        if (song.getAlbumId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "专辑ID不能为空");
        }

        // 业务校验：检查专辑是否存在
        Album album = albumMapper.selectById(song.getAlbumId());
        if (album == null) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }

        // 插入数据
        int result = songMapper.insert(song);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "创建歌曲失败");
        }

        log.info("歌曲创建成功，ID: {}", song.getSongId());
        return song.getSongId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long songId) {
        log.info("删除歌曲: {}", songId);

        // 检查是否存在
        Song song = songMapper.selectById(songId);
        if (song == null) {
            throw new BusinessException(ErrorCode.SONG_NOT_FOUND);
        }

        // 删除数据
        int result = songMapper.deleteById(songId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "删除歌曲失败");
        }

        log.info("歌曲删除成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Song song) {
        log.info("更新歌曲: {}", song.getSongId());

        // 检查是否存在
        Song existSong = songMapper.selectById(song.getSongId());
        if (existSong == null) {
            throw new BusinessException(ErrorCode.SONG_NOT_FOUND);
        }

        // 如果修改了专辑ID，检查新专辑是否存在
        if (song.getAlbumId() != null && !song.getAlbumId().equals(existSong.getAlbumId())) {
            Album album = albumMapper.selectById(song.getAlbumId());
            if (album == null) {
                throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
            }
        }

        // 更新数据
        int result = songMapper.update(song);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "更新歌曲失败");
        }

        log.info("歌曲更新成功");
    }

    @Override
    public Song getById(Long songId) {
        Song song = songMapper.selectById(songId);
        if (song == null) {
            throw new BusinessException(ErrorCode.SONG_NOT_FOUND);
        }
        return song;
    }

    @Override
    public List<Song> getByAlbumId(Long albumId) {
        if (albumId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "专辑ID不能为空");
        }
        return songMapper.selectByAlbumId(albumId);
    }

    @Override
    public PageResult<Song> page(int pageNum, int pageSize, Song condition) {
        PageHelper.startPage(pageNum, pageSize);
        List<Song> list = condition == null ? songMapper.selectAll() : songMapper.selectByCondition(condition);
        return PageResult.of(list);
    }

    @Override
    public List<Song> list() {
        return songMapper.selectAll();
    }
}
