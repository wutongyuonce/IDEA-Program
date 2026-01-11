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
import com.band.management.service.AdminAlbumService;
import com.band.management.util.StringUtil;
import com.band.management.vo.AlbumDetailVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class AdminAlbumServiceImpl implements AdminAlbumService {

    @Autowired
    private AlbumMapper albumMapper;

    @Autowired
    private BandMapper bandMapper;

    @Autowired
    private SongMapper songMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Album album) {
        log.info("管理员创建专辑: {}", album.getTitle());
        DataSourceContextHolder.setDataSourceType("admin");

        if (StringUtil.isEmpty(album.getTitle())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "专辑标题不能为空");
        }
        if (album.getBandId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "乐队ID不能为空");
        }
        if (album.getReleaseDate() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "发行日期不能为空");
        }

        Band band = bandMapper.selectById(album.getBandId());
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        int result = albumMapper.insert(album);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "创建专辑失败");
        }

        log.info("管理员创建专辑成功，ID: {}", album.getAlbumId());
        return album.getAlbumId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long albumId) {
        log.info("管理员删除专辑: {}", albumId);
        DataSourceContextHolder.setDataSourceType("admin");

        Album album = albumMapper.selectById(albumId);
        if (album == null) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }

        int result = albumMapper.deleteById(albumId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "删除专辑失败");
        }

        log.info("管理员删除专辑成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Album album) {
        log.info("管理员更新专辑: {}", album.getAlbumId());
        DataSourceContextHolder.setDataSourceType("admin");

        Album existAlbum = albumMapper.selectById(album.getAlbumId());
        if (existAlbum == null) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }

        if (album.getBandId() != null && !album.getBandId().equals(existAlbum.getBandId())) {
            Band band = bandMapper.selectById(album.getBandId());
            if (band == null) {
                throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
            }
        }

        int result = albumMapper.update(album);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "更新专辑失败");
        }

        log.info("管理员更新专辑成功");
    }

    @Override
    public Album getById(Long albumId) {
        DataSourceContextHolder.setDataSourceType("admin");
        Album album = albumMapper.selectById(albumId);
        if (album == null) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }
        return album;
    }

    @Override
    public AlbumDetailVO getDetailById(Long albumId) {
        DataSourceContextHolder.setDataSourceType("admin");
        
        Album album = getById(albumId);
        List<Song> songs = songMapper.selectByAlbumId(albumId);

        AlbumDetailVO albumDetailVO = new AlbumDetailVO();
        BeanUtils.copyProperties(album, albumDetailVO);
        albumDetailVO.setSongs(songs);

        return albumDetailVO;
    }

    @Override
    public List<Album> getByBandId(Long bandId) {
        DataSourceContextHolder.setDataSourceType("admin");
        
        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        return albumMapper.selectByBandId(bandId);
    }

    @Override
    public PageResult<Album> page(int pageNum, int pageSize, Album condition) {
        DataSourceContextHolder.setDataSourceType("admin");
        
        PageHelper.startPage(pageNum, pageSize);
        List<Album> list = condition == null ? albumMapper.selectAll() : albumMapper.selectByCondition(condition);
        return PageResult.of(list);
    }

    @Override
    public List<Album> list() {
        DataSourceContextHolder.setDataSourceType("admin");
        return albumMapper.selectAll();
    }
}
