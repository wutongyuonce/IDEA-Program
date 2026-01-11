package com.band.management.mapper;

import com.band.management.entity.Album;
import com.band.management.vo.AlbumDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 专辑Mapper接口
 * 
 * @author Band Management Team
 */
@Mapper
public interface AlbumMapper {

    /**
     * 插入专辑
     */
    int insert(Album album);

    /**
     * 根据ID删除专辑
     */
    int deleteById(@Param("albumId") Long albumId);

    /**
     * 更新专辑信息
     */
    int update(Album album);

    /**
     * 根据ID查询专辑
     */
    Album selectById(@Param("albumId") Long albumId);

    /**
     * 查询所有专辑
     */
    List<Album> selectAll();

    /**
     * 根据乐队ID查询专辑列表
     */
    List<Album> selectByBandId(@Param("bandId") Long bandId);

    /**
     * 查询专辑详情（包含歌曲列表）
     */
    AlbumDetailVO selectDetailById(@Param("albumId") Long albumId);

    /**
     * 条件查询专辑列表
     */
    List<Album> selectByCondition(Album album);

    /**
     * 统计专辑数量
     */
    int count();

    /**
     * 根据条件统计数量
     */
    int countByCondition(Album album);

    /**
     * 查询乐队最新专辑
     */
    List<Album> selectRecentByBandId(@Param("bandId") Long bandId, @Param("limit") int limit);

    /**
     * 查询所有专辑（包含乐队名称）
     */
    List<Album> selectAllWithBandName();
}
