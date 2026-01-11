package com.band.management.service;

import com.band.management.common.PageResult;
import com.band.management.entity.AlbumReview;
import com.band.management.vo.ReviewVO;

import java.util.List;

/**
 * 专辑乐评服务接口
 * 
 * @author Band Management Team
 */
public interface AlbumReviewService {

    /**
     * 创建乐评
     */
    Long create(AlbumReview albumReview);

    /**
     * 删除乐评
     */
    void delete(Long reviewId);

    /**
     * 更新乐评信息
     */
    void update(AlbumReview albumReview);

    /**
     * 根据ID查询乐评
     */
    AlbumReview getById(Long reviewId);

    /**
     * 根据专辑ID查询乐评列表（包含歌迷和乐队信息）
     */
    List<ReviewVO> getByAlbumId(Long albumId);

    /**
     * 根据歌迷ID查询乐评列表（包含专辑和乐队信息）
     */
    List<ReviewVO> getByFanId(Long fanId);

    /**
     * 分页查询乐评列表
     */
    PageResult<AlbumReview> page(int pageNum, int pageSize, AlbumReview condition);
}
