package com.band.management.service;

import com.band.management.entity.AlbumReview;
import com.band.management.vo.ReviewVO;

import java.util.List;

/**
 * 歌迷乐评服务接口
 * 
 * @author Band Management Team
 */
public interface FanReviewService {

    /**
     * 发表乐评
     */
    void createReview(Long fanId, AlbumReview review);

    /**
     * 查看我的乐评列表
     */
    List<ReviewVO> getMyReviews(Long fanId);

    /**
     * 修改乐评
     */
    void updateReview(Long fanId, Long reviewId, AlbumReview review);

    /**
     * 删除乐评
     */
    void deleteReview(Long fanId, Long reviewId);

    /**
     * 查看专辑所有乐评
     */
    List<ReviewVO> getAlbumReviews(Long fanId, Long albumId);
}
