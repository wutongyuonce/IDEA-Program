package com.band.management.service.impl;

import com.band.management.common.PageResult;
import com.band.management.config.DataSourceContextHolder;
import com.band.management.entity.Album;
import com.band.management.entity.Band;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.AlbumMapper;
import com.band.management.mapper.AlbumReviewMapper;
import com.band.management.mapper.BandMapper;
import com.band.management.service.BandReviewService;
import com.band.management.vo.ReviewVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 乐队乐评服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class BandReviewServiceImpl implements BandReviewService {

    @Autowired
    private AlbumReviewMapper albumReviewMapper;

    @Autowired
    private BandMapper bandMapper;

    @Autowired
    private AlbumMapper albumMapper;

    @Override
    public List<ReviewVO> getMyBandReviews(Long bandId) {
        log.info("乐队查询自己的专辑乐评: {}", bandId);

        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        List<Album> albums = albumMapper.selectByBandId(bandId);
        if (albums.isEmpty()) {
            return List.of();
        }

        List<ReviewVO> allReviews = new ArrayList<>();
        for (Album album : albums) {
            List<ReviewVO> reviews = albumReviewMapper.selectByAlbumId(album.getAlbumId());
            allReviews.addAll(reviews);
        }

        return allReviews;
    }

    @Override
    public List<ReviewVO> getByAlbumId(Long bandId, Long albumId) {
        log.info("乐队查询专辑乐评: bandId={}, albumId={}", bandId, albumId);

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
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权查看其他乐队的乐评");
        }

        return albumReviewMapper.selectByAlbumId(albumId);
    }

    @Override
    public PageResult<ReviewVO> page(Long bandId, int pageNum, int pageSize, Long albumId) {
        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        PageHelper.startPage(pageNum, pageSize);
        
        List<ReviewVO> reviews;
        if (albumId != null) {
            Album album = albumMapper.selectById(albumId);
            if (album == null) {
                throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
            }
            if (!album.getBandId().equals(bandId)) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权查看其他乐队的乐评");
            }
            reviews = albumReviewMapper.selectByAlbumId(albumId);
        } else {
            reviews = getMyBandReviews(bandId);
        }

        return PageResult.of(reviews);
    }
}
