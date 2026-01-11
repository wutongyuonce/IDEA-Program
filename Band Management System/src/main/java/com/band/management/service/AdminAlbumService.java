package com.band.management.service;

import com.band.management.common.PageResult;
import com.band.management.entity.Album;
import com.band.management.vo.AlbumDetailVO;

import java.util.List;

/**
 * 管理员专辑服务接口
 * 
 * @author Band Management Team
 */
public interface AdminAlbumService {

    Long create(Album album);

    void delete(Long albumId);

    void update(Album album);

    Album getById(Long albumId);

    AlbumDetailVO getDetailById(Long albumId);

    List<Album> getByBandId(Long bandId);

    PageResult<Album> page(int pageNum, int pageSize, Album condition);

    List<Album> list();
}
