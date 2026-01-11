package com.band.management.mapper;

import com.band.management.entity.AlbumReview;
import com.band.management.vo.ReviewVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 专辑乐评Mapper接口
 * 
 * @author Band Management Team
 */
@Mapper
public interface AlbumReviewMapper {

    /**
     * 插入乐评
     */
    int insert(AlbumReview albumReview);

    /**
     * 根据ID删除乐评
     */
    int deleteById(@Param("reviewId") Long reviewId);

    /**
     * 更新乐评信息
     */
    int update(AlbumReview albumReview);

    /**
     * 根据ID查询乐评
     */
    AlbumReview selectById(@Param("reviewId") Long reviewId);

    /**
     * 查询所有乐评
     */
    List<AlbumReview> selectAll();

    /**
     * 根据专辑ID查询乐评列表
     */
    List<ReviewVO> selectByAlbumId(@Param("albumId") Long albumId);

    /**
     * 根据歌迷ID查询乐评列表
     */
    List<ReviewVO> selectByFanId(@Param("fanId") Long fanId);

    /**
     * 根据歌迷ID和专辑ID查询乐评（检查是否已评论）
     */
    AlbumReview selectByFanIdAndAlbumId(@Param("fanId") Long fanId, @Param("albumId") Long albumId);

    /**
     * 条件查询乐评列表
     */
    List<AlbumReview> selectByCondition(AlbumReview albumReview);

    /**
     * 统计乐评数量
     */
    int count();

    /**
     * 根据条件统计数量
     */
    int countByCondition(AlbumReview albumReview);
}
