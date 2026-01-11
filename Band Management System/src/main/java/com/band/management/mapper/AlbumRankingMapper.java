package com.band.management.mapper;

import com.band.management.entity.AlbumRanking;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 专辑排行榜Mapper接口
 * 
 * @author Band Management Team
 */
@Mapper
public interface AlbumRankingMapper {

    /**
     * 查询排行榜前10名
     */
    List<AlbumRanking> selectTop10();

    /**
     * 查询所有排行榜数据
     */
    List<AlbumRanking> selectAll();
}
