package com.band.management.service;

import com.band.management.common.PageResult;
import com.band.management.entity.Album;
import com.band.management.vo.AlbumDetailVO;
import java.util.List;

/**
 * 乐队专辑服务接口
 * 
 * @author Band Management Team
 */
public interface BandAlbumService {
    
    /**
     * 获取本乐队专辑列表
     */
    List<Album> getMyBandAlbums(Long bandId);
    
    /**
     * 分页查询本乐队专辑
     */
    PageResult<Album> page(Long bandId, int pageNum, int pageSize, Album condition);
    
    /**
     * 发布新专辑
     */
    Long createAlbum(Long bandId, Album album);
    
    /**
     * 更新专辑信息
     */
    void updateAlbum(Long bandId, Album album);
    
    /**
     * 删除专辑
     */
    void deleteAlbum(Long bandId, Long albumId);
    
    /**
     * 查询专辑详情（包含歌曲列表和评分）
     */
    AlbumDetailVO getAlbumDetail(Long bandId, Long albumId);
    
    /**
     * 根据ID查询专辑
     */
    Album getById(Long bandId, Long albumId);
}
