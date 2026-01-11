package com.band.management.service;

import com.band.management.common.PageResult;
import com.band.management.entity.Album;
import com.band.management.vo.AlbumDetailVO;

import java.util.List;

/**
 * 专辑服务接口
 * 
 * @author Band Management Team
 */
public interface AlbumService {

    /**
     * 创建专辑
     */
    Long create(Album album);

    /**
     * 删除专辑
     */
    void delete(Long albumId);

    /**
     * 更新专辑信息
     */
    void update(Album album);

    /**
     * 根据ID查询专辑
     */
    Album getById(Long albumId);

    /**
     * 查询专辑详情（包含歌曲列表）
     */
    AlbumDetailVO getDetailById(Long albumId);

    /**
     * 根据乐队ID查询专辑列表
     */
    List<Album> getByBandId(Long bandId);

    /**
     * 分页查询专辑列表
     */
    PageResult<Album> page(int pageNum, int pageSize, Album condition);

    /**
     * 查询所有专辑
     */
    List<Album> list();
}
