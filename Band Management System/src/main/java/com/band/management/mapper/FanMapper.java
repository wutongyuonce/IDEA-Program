package com.band.management.mapper;

import com.band.management.entity.Fan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 歌迷Mapper接口
 * 
 * @author Band Management Team
 */
@Mapper
public interface FanMapper {

    /**
     * 插入歌迷
     */
    int insert(Fan fan);

    /**
     * 根据ID删除歌迷
     */
    int deleteById(@Param("fanId") Long fanId);

    /**
     * 更新歌迷信息
     */
    int update(Fan fan);

    /**
     * 根据ID查询歌迷
     */
    Fan selectById(@Param("fanId") Long fanId);

    /**
     * 查询所有歌迷
     */
    List<Fan> selectAll();

    /**
     * 条件查询歌迷列表
     */
    List<Fan> selectByCondition(Fan fan);

    /**
     * 统计歌迷数量
     */
    int count();

    /**
     * 根据条件统计数量
     */
    int countByCondition(Fan fan);
}
