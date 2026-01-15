package com.band.management.service.impl;

import com.band.management.common.PageResult;
import com.band.management.config.DataSourceContextHolder;
import com.band.management.entity.Album;
import com.band.management.entity.AlbumRanking;
import com.band.management.entity.Band;
import com.band.management.entity.Song;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.AlbumMapper;
import com.band.management.mapper.AlbumRankingMapper;
import com.band.management.mapper.BandMapper;
import com.band.management.mapper.SongMapper;
import com.band.management.service.BandAlbumService;
import com.band.management.util.StringUtil;
import com.band.management.vo.AlbumDetailVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 乐队专辑服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class BandAlbumServiceImpl implements BandAlbumService {

    @Autowired
    private AlbumMapper albumMapper;

    @Autowired
    private BandMapper bandMapper;

    @Autowired
    private SongMapper songMapper;

    @Autowired
    private AlbumRankingMapper albumRankingMapper;

    @Override
    public List<Album> getMyBandAlbums(Long bandId) {
        log.info("乐队查询自己的专辑列表: {}", bandId);

        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        return albumMapper.selectByBandId(bandId);
    }

    @Override
    public PageResult<Album> page(Long bandId, int pageNum, int pageSize, Album condition) {
        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        if (condition == null) {
            condition = new Album();
        }
        condition.setBandId(bandId);

        PageHelper.startPage(pageNum, pageSize);
        List<Album> list = albumMapper.selectByCondition(condition);
        return PageResult.of(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAlbum(Long bandId, Album album) {
        log.info("乐队发布新专辑: bandId={}, albumTitle={}", bandId, album.getTitle());

        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        if (StringUtil.isEmpty(album.getTitle())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "专辑标题不能为空");
        }
        if (album.getReleaseDate() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "发行日期不能为空");
        }

        // 检查发行日期不能早于乐队成立日期
        if (album.getReleaseDate().before(band.getFoundedAt())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                String.format("专辑发行日期（%s）需要在乐队创建日期（%s）之后", 
                    album.getReleaseDate(), band.getFoundedAt()));
        }

        album.setBandId(bandId);
        int result = albumMapper.insert(album);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "发布专辑失败");
        }

        log.info("乐队发布专辑成功，ID: {}", album.getAlbumId());
        return album.getAlbumId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAlbum(Long bandId, Album album) {
        log.info("乐队更新专辑信息: bandId={}, albumId={}", bandId, album.getAlbumId());

        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        Album existAlbum = albumMapper.selectById(album.getAlbumId());
        if (existAlbum == null) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }

        if (!existAlbum.getBandId().equals(bandId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权操作其他乐队的专辑");
        }

        // 检查发行日期不能早于乐队成立日期
        if (album.getReleaseDate() != null && album.getReleaseDate().before(band.getFoundedAt())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                String.format("专辑发行日期（%s）需要在乐队创建日期（%s）之后", 
                    album.getReleaseDate(), band.getFoundedAt()));
        }

        int result = albumMapper.update(album);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "更新专辑信息失败");
        }

        log.info("乐队更新专辑信息成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAlbum(Long bandId, Long albumId) {
        log.info("乐队删除专辑: bandId={}, albumId={}", bandId, albumId);

        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        Album album = albumMapper.selectById(albumId);
        if (album == null) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }

        if (!album.getBandId().equals(bandId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权删除其他乐队的专辑");
        }

        int result = albumMapper.deleteById(albumId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "删除专辑失败");
        }

        log.info("乐队删除专辑成功");
    }

    @Override
    public AlbumDetailVO getAlbumDetail(Long bandId, Long albumId) {
        log.info("乐队查询专辑详情: bandId={}, albumId={}", bandId, albumId);

        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        Album album = albumMapper.selectById(albumId);
        if (album == null) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }

        if (!album.getBandId().equals(bandId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权查看其他乐队的专辑");
        }

        List<Song> songs = songMapper.selectByAlbumId(albumId);

        AlbumDetailVO albumDetailVO = new AlbumDetailVO();
        BeanUtils.copyProperties(album, albumDetailVO);
        albumDetailVO.setSongs(songs);
        albumDetailVO.setBandName(band.getName());

        List<AlbumRanking> rankings = albumRankingMapper.selectAll();
        for (int i = 0; i < rankings.size(); i++) {
            if (rankings.get(i).getAlbumId().equals(albumId)) {
                albumDetailVO.setRanking(i + 1);
                break;
            }
        }

        return albumDetailVO;
    }

    @Override
    public Album getById(Long bandId, Long albumId) {
        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        Album album = albumMapper.selectById(albumId);
        if (album == null) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }

        if (!album.getBandId().equals(bandId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权查看其他乐队的专辑");
        }

        return album;
    }
}
