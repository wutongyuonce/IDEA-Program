package com.band.management.service.impl;

import com.band.management.common.PageResult;
import com.band.management.config.DataSourceContextHolder;
import com.band.management.entity.Album;
import com.band.management.entity.AlbumReview;
import com.band.management.entity.Fan;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.AlbumMapper;
import com.band.management.mapper.AlbumReviewMapper;
import com.band.management.mapper.FanMapper;
import com.band.management.service.AdminReviewService;
import com.band.management.vo.ReviewVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理员乐评服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class AdminReviewServiceImpl implements AdminReviewService {

    @Autowired
    private AlbumReviewMapper albumReviewMapper;

    @Autowired
    private AlbumMapper albumMapper;

    @Autowired
    private FanMapper fanMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long reviewId) {
        log.info("管理员删除乐评: {}", reviewId);

        DataSourceContextHolder.setDataSourceType("admin");

        AlbumReview review = albumReviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_FOUND);
        }

        int result = albumReviewMapper.deleteById(reviewId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "删除乐评失败");
        }

        log.info("管理员删除乐评成功");
    }

    @Override
    public AlbumReview getById(Long reviewId) {
        DataSourceContextHolder.setDataSourceType("admin");

        AlbumReview review = albumReviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_FOUND);
        }
        return review;
    }

    @Override
    public List<ReviewVO> getByAlbumId(Long albumId) {
        DataSourceContextHolder.setDataSourceType("admin");

        Album album = albumMapper.selectById(albumId);
        if (album == null) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }

        return albumReviewMapper.selectByAlbumId(albumId);
    }

    @Override
    public List<ReviewVO> getByFanId(Long fanId) {
        DataSourceContextHolder.setDataSourceType("admin");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        return albumReviewMapper.selectByFanId(fanId);
    }

    @Override
    public PageResult<AlbumReview> page(int pageNum, int pageSize, AlbumReview condition) {
        DataSourceContextHolder.setDataSourceType("admin");

        PageHelper.startPage(pageNum, pageSize);
        List<AlbumReview> list = condition == null ? albumReviewMapper.selectAll() : albumReviewMapper.selectByCondition(condition);
        return PageResult.of(list);
    }

    @Override
    public List<AlbumReview> list() {
        DataSourceContextHolder.setDataSourceType("admin");
        return albumReviewMapper.selectAll();
    }
}
