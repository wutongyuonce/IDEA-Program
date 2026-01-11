package com.band.management.service;

import com.band.management.common.PageResult;
import com.band.management.vo.ReviewVO;
import java.util.List;

/**
 * 乐队乐评服务接口
 * 
 * @author Band Management Team
 */
public interface BandReviewService {
    
    /**
     * 查看本乐队专辑乐评
     */
    List<ReviewVO> getMyBandReviews(Long bandId);
    
    /**
     * 按专辑筛选乐评
     */
    List<ReviewVO> getByAlbumId(Long bandId, Long albumId);
    
    /**
     * 分页查询本乐队乐评
     */
    PageResult<ReviewVO> page(Long bandId, int pageNum, int pageSize, Long albumId);
}
