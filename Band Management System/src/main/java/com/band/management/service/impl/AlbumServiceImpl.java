package com.band.management.service.impl;

import com.band.management.common.PageResult;
import com.band.management.entity.Album;
import com.band.management.entity.Band;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.AlbumMapper;
import com.band.management.mapper.BandMapper;
import com.band.management.service.AlbumService;
import com.band.management.util.StringUtil;
import com.band.management.vo.AlbumDetailVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 专辑服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class AlbumServiceImpl implements AlbumService {

    @Autowired
    private AlbumMapper albumMapper;

    @Autowired
    private BandMapper bandMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Album album) {
        log.info("创建专辑: {}", album.getTitle());

        // 参数校验
        if (StringUtil.isEmpty(album.getTitle())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "专辑标题不能为空");
        }
        if (album.getBandId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "乐队ID不能为空");
        }
        if (album.getReleaseDate() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "发行日期不能为空");
        }

        // 业务校验：检查乐队是否存在
        Band band = bandMapper.selectById(album.getBandId());
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        // 插入数据
        int result = albumMapper.insert(album);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "创建专辑失败");
        }

        log.info("专辑创建成功，ID: {}", album.getAlbumId());
        return album.getAlbumId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long albumId) {
        log.info("删除专辑: {}", albumId);

        // 检查是否存在
        Album album = albumMapper.selectById(albumId);
        if (album == null) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }

        // 删除数据（级联删除由数据库外键处理）
        int result = albumMapper.deleteById(albumId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "删除专辑失败");
        }

        log.info("专辑删除成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Album album) {
        log.info("更新专辑: {}", album.getAlbumId());

        // 检查是否存在
        Album existAlbum = albumMapper.selectById(album.getAlbumId());
        if (existAlbum == null) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }

        // 如果修改了乐队ID，检查新乐队是否存在
        if (album.getBandId() != null && !album.getBandId().equals(existAlbum.getBandId())) {
            Band band = bandMapper.selectById(album.getBandId());
            if (band == null) {
                throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
            }
        }

        // 更新数据
        int result = albumMapper.update(album);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "更新专辑失败");
        }

        log.info("专辑更新成功");
    }

    @Override
    public Album getById(Long albumId) {
        Album album = albumMapper.selectById(albumId);
        if (album == null) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }
        return album;
    }

    @Override
    public AlbumDetailVO getDetailById(Long albumId) {
        AlbumDetailVO albumDetailVO = albumMapper.selectDetailById(albumId);
        if (albumDetailVO == null) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }
        return albumDetailVO;
    }

    @Override
    public List<Album> getByBandId(Long bandId) {
        if (bandId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "乐队ID不能为空");
        }
        return albumMapper.selectByBandId(bandId);
    }

    @Override
    public PageResult<Album> page(int pageNum, int pageSize, Album condition) {
        PageHelper.startPage(pageNum, pageSize);
        List<Album> list = condition == null ? albumMapper.selectAll() : albumMapper.selectByCondition(condition);
        return PageResult.of(list);
    }

    @Override
    public List<Album> list() {
        return albumMapper.selectAll();
    }
}
