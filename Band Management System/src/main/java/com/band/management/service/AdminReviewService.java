package com.band.management.service;

import com.band.management.common.PageResult;
import com.band.management.entity.AlbumReview;
import com.band.management.vo.ReviewVO;
import java.util.List;

public interface AdminReviewService {
    void delete(Long reviewId);
    AlbumReview getById(Long reviewId);
    List<ReviewVO> getByAlbumId(Long albumId);
    List<ReviewVO> getByFanId(Long fanId);
    PageResult<AlbumReview> page(int pageNum, int pageSize, AlbumReview condition);
    List<AlbumReview> list();
}
