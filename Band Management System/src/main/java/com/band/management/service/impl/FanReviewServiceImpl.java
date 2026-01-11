package com.band.management.service.impl;

import com.band.management.config.DataSourceContextHolder;
import com.band.management.entity.Album;
import com.band.management.entity.AlbumReview;
import com.band.management.entity.Fan;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.AlbumMapper;
import com.band.management.mapper.AlbumReviewMapper;
import com.band.management.mapper.FanMapper;
import com.band.management.service.FanReviewService;
import com.band.management.vo.ReviewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 歌迷乐评服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class FanReviewServiceImpl implements FanReviewService {

    @Autowired
    private FanMapper fanMapper;

    @Autowired
    private AlbumMapper albumMapper;

    @Autowired
    private AlbumReviewMapper albumReviewMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createReview(Long fanId, AlbumReview review) {
        log.info("歌迷发表乐评: fanId={}, albumId={}", fanId, review.getAlbumId());

        DataSourceContextHolder.setDataSourceType("fan");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        if (review.getAlbumId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "专辑ID不能为空");
        }

        Album album = albumMapper.selectById(review.getAlbumId());
        if (album == null) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }

        if (review.getRating() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "评分不能为空");
        }

        BigDecimal rating = review.getRating();
        if (rating.compareTo(BigDecimal.ONE) < 0 || rating.compareTo(BigDecimal.TEN) > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "评分必须在1-10之间");
        }

        // 检查是否已评论（唯一约束）
        AlbumReview existReview = albumReviewMapper.selectByFanIdAndAlbumId(fanId, review.getAlbumId());
        if (existReview != null) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS.getCode(), "您已评论过该专辑");
        }

        review.setFanId(fanId);
        review.setReviewedAt(new Date());
        review.setCreatedAt(new Date());

        int result = albumReviewMapper.insert(review);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "发表乐评失败");
        }

        log.info("歌迷发表乐评成功，触发器将自动更新专辑平均分和排行榜");
    }

    @Override
    public List<ReviewVO> getMyReviews(Long fanId) {
        log.info("歌迷查看我的乐评列表: {}", fanId);

        DataSourceContextHolder.setDataSourceType("fan");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        return albumReviewMapper.selectByFanId(fanId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReview(Long fanId, Long reviewId, AlbumReview review) {
        log.info("歌迷修改乐评: fanId={}, reviewId={}", fanId, reviewId);

        DataSourceContextHolder.setDataSourceType("fan");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        AlbumReview existReview = albumReviewMapper.selectById(reviewId);
        if (existReview == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND.getCode(), "乐评不存在");
        }

        if (!existReview.getFanId().equals(fanId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "只能修改自己的乐评");
        }

        if (review.getRating() != null) {
            BigDecimal rating = review.getRating();
            if (rating.compareTo(BigDecimal.ONE) < 0 || rating.compareTo(BigDecimal.TEN) > 0) {
                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "评分必须在1-10之间");
            }
        }

        review.setReviewId(reviewId);
        review.setFanId(fanId);
        review.setAlbumId(existReview.getAlbumId());

        int result = albumReviewMapper.update(review);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "修改乐评失败");
        }

        log.info("歌迷修改乐评成功，触发器将自动更新专辑平均分和排行榜");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReview(Long fanId, Long reviewId) {
        log.info("歌迷删除乐评: fanId={}, reviewId={}", fanId, reviewId);

        DataSourceContextHolder.setDataSourceType("fan");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        AlbumReview existReview = albumReviewMapper.selectById(reviewId);
        if (existReview == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND.getCode(), "乐评不存在");
        }

        if (!existReview.getFanId().equals(fanId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "只能删除自己的乐评");
        }

        int result = albumReviewMapper.deleteById(reviewId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "删除乐评失败");
        }

        log.info("歌迷删除乐评成功，触发器将自动更新专辑平均分和排行榜");
    }

    @Override
    public List<ReviewVO> getAlbumReviews(Long fanId, Long albumId) {
        log.info("歌迷查看专辑乐评: fanId={}, albumId={}", fanId, albumId);

        DataSourceContextHolder.setDataSourceType("fan");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        Album album = albumMapper.selectById(albumId);
        if (album == null) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }

        return albumReviewMapper.selectByAlbumId(albumId);
    }
}
