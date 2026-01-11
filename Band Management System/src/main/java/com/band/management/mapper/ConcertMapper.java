package com.band.management.mapper;

import com.band.management.entity.Concert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 演唱会Mapper接口
 * 
 * @author Band Management Team
 */
@Mapper
public interface ConcertMapper {

    /**
     * 插入演唱会
     */
    int insert(Concert concert);

    /**
     * 根据ID删除演唱会
     */
    int deleteById(@Param("concertId") Long concertId);

    /**
     * 更新演唱会信息
     */
    int update(Concert concert);

    /**
     * 根据ID查询演唱会
     */
    Concert selectById(@Param("concertId") Long concertId);

    /**
     * 查询所有演唱会
     */
    List<Concert> selectAll();

    /**
     * 根据乐队ID查询演唱会列表
     */
    List<Concert> selectByBandId(@Param("bandId") Long bandId);

    /**
     * 条件查询演唱会列表
     */
    List<Concert> selectByCondition(Concert concert);

    /**
     * 统计演唱会数量
     */
    int count();

    /**
     * 根据条件统计数量
     */
    int countByCondition(Concert concert);

    /**
     * 查询乐队最新演唱会
     */
    List<Concert> selectRecentByBandId(@Param("bandId") Long bandId, @Param("limit") int limit);
}
