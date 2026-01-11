package com.band.management.mapper;

import com.band.management.entity.Band;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 乐队Mapper接口
 * 
 * @author Band Management Team
 */
@Mapper
public interface BandMapper {

    /**
     * 插入乐队
     */
    int insert(Band band);

    /**
     * 根据ID删除乐队
     */
    int deleteById(@Param("bandId") Long bandId);

    /**
     * 更新乐队信息
     */
    int update(Band band);

    /**
     * 根据ID查询乐队
     */
    Band selectById(@Param("bandId") Long bandId);

    /**
     * 查询所有乐队
     */
    List<Band> selectAll();

    /**
     * 根据名称查询乐队
     */
    Band selectByName(@Param("name") String name);

    /**
     * 条件查询乐队列表
     */
    List<Band> selectByCondition(Band band);

    /**
     * 统计乐队数量
     */
    int count();

    /**
     * 根据条件统计数量
     */
    int countByCondition(Band band);

    /**
     * 查询未关注的乐队，按关注人数降序排序
     */
    List<Band> selectUnfollowedBandsByFanCount(@Param("fanId") Long fanId);
}
