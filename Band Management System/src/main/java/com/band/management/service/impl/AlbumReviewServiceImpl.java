package com.band.management.service.impl;

import com.band.management.common.PageResult;
import com.band.management.entity.Album;
import com.band.management.entity.AlbumReview;
import com.band.management.entity.Fan;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.AlbumMapper;
import com.band.management.mapper.AlbumReviewMapper;
import com.band.management.mapper.FanMapper;
import com.band.management.service.AlbumReviewService;
import com.band.management.vo.ReviewVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 专辑乐评服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class AlbumReviewServiceImpl implements AlbumReviewService {

    @Autowired
    private AlbumReviewMapper albumReviewMapper;

    @Autowired
    private AlbumMapper albumMapper;

    @Autowired
    private FanMapper fanMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(AlbumReview albumReview) {
        log.info("创建乐评: 歌迷ID={}, 专辑ID={}", albumReview.getFanId(), albumReview.getAlbumId());

        // 参数校验
        if (albumReview.getFanId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "歌迷ID不能为空");
        }
        if (albumReview.getAlbumId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "专辑ID不能为空");
        }
        if (albumReview.getRating() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "评分不能为空");
        }

        // 评分范围验证：1-10，步长0.5
        BigDecimal rating = albumReview.getRating();
        if (rating.compareTo(BigDecimal.ONE) < 0 || rating.compareTo(BigDecimal.TEN) > 0) {
            throw new BusinessException(ErrorCode.INVALID_RATING);
        }
        // 检查步长是否为0.5
        BigDecimal remainder = rating.remainder(new BigDecimal("0.5"));
        if (remainder.compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException(ErrorCode.INVALID_RATING);
        }

        // 业务校验：检查专辑是否存在
        Album album = albumMapper.selectById(albumReview.getAlbumId());
        if (album == null) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }

        // 业务校验：检查歌迷是否存在
        Fan fan = fanMapper.selectById(albumReview.getFanId());
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        // 业务校验：检查是否已评论
        AlbumReview existReview = albumReviewMapper.selectByFanIdAndAlbumId(
                albumReview.getFanId(), albumReview.getAlbumId());
        if (existReview != null) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        // 插入数据（触发器会自动更新专辑平均分和排行榜）
        int result = albumReviewMapper.insert(albumReview);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "创建乐评失败");
        }

        log.info("乐评创建成功，ID: {}，触发器将自动更新专辑平均分", albumReview.getReviewId());
        return albumReview.getReviewId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long reviewId) {
        log.info("删除乐评: {}", reviewId);

        // 检查是否存在
        AlbumReview albumReview = albumReviewMapper.selectById(reviewId);
        if (albumReview == null) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_FOUND);
        }

        // 删除数据（触发器会自动更新专辑平均分和排行榜）
        int result = albumReviewMapper.deleteById(reviewId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "删除乐评失败");
        }

        log.info("乐评删除成功，触发器将自动更新专辑平均分");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(AlbumReview albumReview) {
        log.info("更新乐评: {}", albumReview.getReviewId());

        // 检查是否存在
        AlbumReview existReview = albumReviewMapper.selectById(albumReview.getReviewId());
        if (existReview == null) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_FOUND);
        }

        // 如果修改了评分，进行评分范围验证
        if (albumReview.getRating() != null) {
            BigDecimal rating = albumReview.getRating();
            if (rating.compareTo(BigDecimal.ONE) < 0 || rating.compareTo(BigDecimal.TEN) > 0) {
                throw new BusinessException(ErrorCode.INVALID_RATING);
            }
            // 检查步长是否为0.5
            BigDecimal remainder = rating.remainder(new BigDecimal("0.5"));
            if (remainder.compareTo(BigDecimal.ZERO) != 0) {
                throw new BusinessException(ErrorCode.INVALID_RATING);
            }
        }

        // 更新数据（触发器会自动更新专辑平均分和排行榜）
        int result = albumReviewMapper.update(albumReview);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "更新乐评失败");
        }

        log.info("乐评更新成功，触发器将自动更新专辑平均分");
    }

    @Override
    public AlbumReview getById(Long reviewId) {
        AlbumReview albumReview = albumReviewMapper.selectById(reviewId);
        if (albumReview == null) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_FOUND);
        }
        return albumReview;
    }

    @Override
    public List<ReviewVO> getByAlbumId(Long albumId) {
        if (albumId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "专辑ID不能为空");
        }
        return albumReviewMapper.selectByAlbumId(albumId);
    }

    @Override
    public List<ReviewVO> getByFanId(Long fanId) {
        if (fanId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "歌迷ID不能为空");
        }
        return albumReviewMapper.selectByFanId(fanId);
    }

    @Override
    public PageResult<AlbumReview> page(int pageNum, int pageSize, AlbumReview condition) {
        PageHelper.startPage(pageNum, pageSize);
        List<AlbumReview> list = condition == null ? albumReviewMapper.selectAll() : albumReviewMapper.selectByCondition(condition);
        return PageResult.of(list);
    }
}
