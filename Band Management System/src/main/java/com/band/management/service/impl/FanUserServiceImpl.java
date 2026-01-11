package com.band.management.service.impl;

import com.band.management.config.DataSourceContextHolder;
import com.band.management.entity.*;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.*;
import com.band.management.service.FanUserService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 歌迷用户服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class FanUserServiceImpl implements FanUserService {

    @Autowired
    private FanMapper fanMapper;

    @Autowired
    private BandMapper bandMapper;

    @Autowired
    private AlbumMapper albumMapper;

    @Autowired
    private SongMapper songMapper;

    @Autowired
    private AlbumReviewMapper albumReviewMapper;

    @Autowired
    private FanFavoriteMapper fanFavoriteMapper;

    @Autowired
    private AlbumRankingMapper albumRankingMapper;

    @Autowired
    private ConcertMapper concertMapper;

    @Autowired
    private ConcertAttendanceMapper concertAttendanceMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Fan getFanInfo(Long fanId) {
        DataSourceContextHolder.setDataSourceType("FAN");
        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }
        return fan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFanInfo(Fan fan) {
        DataSourceContextHolder.setDataSourceType("FAN");
        Fan existFan = fanMapper.selectById(fan.getFanId());
        if (existFan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }
        int result = fanMapper.update(fan);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED);
        }
    }

    @Override
    public Map<String, Object> getStatistics(Long fanId) {
        DataSourceContextHolder.setDataSourceType("FAN");
        Map<String, Object> statistics = new HashMap<>();

        // 统计关注的乐队数量
        List<Band> favoriteBands = fanFavoriteMapper.selectFavoriteBands(fanId);
        int favoriteBandCount = favoriteBands != null ? favoriteBands.size() : 0;

        // 统计收藏的专辑数量
        List<Album> favoriteAlbums = fanFavoriteMapper.selectFavoriteAlbums(fanId);
        int favoriteAlbumCount = favoriteAlbums != null ? favoriteAlbums.size() : 0;

        // 统计收藏的歌曲数量
        List<Song> favoriteSongs = fanFavoriteMapper.selectFavoriteSongs(fanId);
        int favoriteSongCount = favoriteSongs != null ? favoriteSongs.size() : 0;

        // 统计乐评数量
        AlbumReview reviewCondition = new AlbumReview();
        reviewCondition.setFanId(fanId);
        int reviewCount = albumReviewMapper.countByCondition(reviewCondition);

        statistics.put("favoriteBandCount", favoriteBandCount);
        statistics.put("favoriteAlbumCount", favoriteAlbumCount);
        statistics.put("favoriteSongCount", favoriteSongCount);
        statistics.put("reviewCount", reviewCount);

        return statistics;
    }

    @Override
    public PageInfo<Band> getRecommendedBands(Long fanId, Integer page, Integer size, String name) {
        DataSourceContextHolder.setDataSourceType("FAN");
        PageHelper.startPage(page, size);
        
        // 如果有搜索条件，使用条件查询
        if (name != null && !name.trim().isEmpty()) {
            Band condition = new Band();
            condition.setName(name);
            List<Band> allBands = bandMapper.selectByCondition(condition);
            
            // 过滤掉已关注的乐队
            List<Band> unfollowedBands = new ArrayList<>();
            for (Band band : allBands) {
                int count = fanFavoriteMapper.checkFavoriteBand(fanId, band.getBandId());
                if (count == 0) {
                    unfollowedBands.add(band);
                }
            }
            return new PageInfo<>(unfollowedBands);
        } else {
            // 没有搜索条件，查询未关注的乐队，按关注人数降序排序
            List<Band> bands = bandMapper.selectUnfollowedBandsByFanCount(fanId);
            return new PageInfo<>(bands);
        }
    }

    @Override
    public PageInfo<Album> getTopAlbums(Long fanId, Integer page, Integer size) {
        log.info("获取专辑排行榜: fanId={}, page={}, size={}", fanId, page, size);
        DataSourceContextHolder.setDataSourceType("FAN");
        
        try {
            // 直接返回排行榜前10名，不分页
            List<AlbumRanking> rankings = albumRankingMapper.selectTop10();
            log.info("查询到排行榜数据: {} 条", rankings != null ? rankings.size() : 0);
            
            // 转换为 Album 对象（保持接口兼容性）
            List<Album> albums = new ArrayList<>();
            if (rankings != null && !rankings.isEmpty()) {
                for (AlbumRanking ranking : rankings) {
                    Album album = new Album();
                    album.setAlbumId(ranking.getAlbumId());
                    album.setTitle(ranking.getAlbumTitle());
                    album.setBandId(ranking.getBandId());
                    album.setReleaseDate(ranking.getReleaseDate());
                    album.setAvgScore(ranking.getAvgScore());
                    // 设置 bandName（非数据库字段）
                    album.setBandName(ranking.getBandName());
                    
                    // 检查是否已收藏
                    if (fanId != null) {
                        int count = fanFavoriteMapper.checkFavoriteAlbum(fanId, ranking.getAlbumId());
                        album.setIsFavorited(count > 0);
                    } else {
                        album.setIsFavorited(false);
                    }
                    
                    albums.add(album);
                }
            }
            
            log.info("返回专辑列表: {} 条", albums.size());
            return new PageInfo<>(albums);
        } catch (Exception e) {
            log.error("获取专辑排行榜失败: fanId={}", fanId, e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "获取专辑排行榜失败: " + e.getMessage());
        }
    }

    @Override
    public PageInfo<Album> getAllAlbums(Integer page, Integer size, String title) {
        DataSourceContextHolder.setDataSourceType("FAN");
        PageHelper.startPage(page, size);
        
        // 如果有搜索条件，使用条件查询
        if (title != null && !title.trim().isEmpty()) {
            Album condition = new Album();
            condition.setTitle(title);
            List<Album> albums = albumMapper.selectByCondition(condition);
            return new PageInfo<>(albums);
        } else {
            // 没有搜索条件，查询所有专辑，包含乐队名称
            List<Album> albums = albumMapper.selectAllWithBandName();
            return new PageInfo<>(albums);
        }
    }

    @Override
    public PageInfo<Song> getAllSongs(Integer page, Integer size, String title) {
        DataSourceContextHolder.setDataSourceType("FAN");
        PageHelper.startPage(page, size);
        
        // 如果有搜索条件，使用条件查询
        if (title != null && !title.trim().isEmpty()) {
            Song condition = new Song();
            condition.setTitle(title);
            List<Song> songs = songMapper.selectByCondition(condition);
            return new PageInfo<>(songs);
        } else {
            // 没有搜索条件，查询所有歌曲，包含专辑和乐队信息
            List<Song> songs = songMapper.selectAllWithDetails();
            return new PageInfo<>(songs);
        }
    }

    @Override
    public PageInfo<AlbumReview> getMyReviews(Long fanId, Integer page, Integer size) {
        DataSourceContextHolder.setDataSourceType("FAN");
        PageHelper.startPage(page, size);
        AlbumReview condition = new AlbumReview();
        condition.setFanId(fanId);
        List<AlbumReview> reviews = albumReviewMapper.selectByCondition(condition);
        return new PageInfo<>(reviews);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void followBand(Long fanId, Long bandId) {
        DataSourceContextHolder.setDataSourceType("FAN");
        
        // 检查乐队是否存在
        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        // 检查是否已关注
        int existing = fanFavoriteMapper.checkFavoriteBand(fanId, bandId);
        if (existing > 0) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS.getCode(), "已经关注过该乐队");
        }

        // 添加关注
        int result = fanFavoriteMapper.insertFavoriteBand(fanId, bandId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollowBand(Long fanId, Long bandId) {
        DataSourceContextHolder.setDataSourceType("FAN");
        
        int result = fanFavoriteMapper.deleteFavoriteBand(fanId, bandId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND.getCode(), "未找到关注记录");
        }
    }

    @Override
    public PageInfo<Band> getFavoriteBands(Long fanId, Integer page, Integer size) {
        DataSourceContextHolder.setDataSourceType("FAN");
        PageHelper.startPage(page, size);
        List<Band> bands = fanFavoriteMapper.selectFavoriteBands(fanId);
        return new PageInfo<>(bands);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void favoriteAlbum(Long fanId, Long albumId) {
        DataSourceContextHolder.setDataSourceType("FAN");
        
        // 检查专辑是否存在
        Album album = albumMapper.selectById(albumId);
        if (album == null) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }

        // 检查是否已收藏
        int existing = fanFavoriteMapper.checkFavoriteAlbum(fanId, albumId);
        if (existing > 0) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS.getCode(), "已经收藏过该专辑");
        }

        // 添加收藏
        int result = fanFavoriteMapper.insertFavoriteAlbum(fanId, albumId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfavoriteAlbum(Long fanId, Long albumId) {
        DataSourceContextHolder.setDataSourceType("FAN");
        
        int result = fanFavoriteMapper.deleteFavoriteAlbum(fanId, albumId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND.getCode(), "未找到收藏记录");
        }
    }

    @Override
    public PageInfo<Album> getFavoriteAlbums(Long fanId, Integer page, Integer size) {
        DataSourceContextHolder.setDataSourceType("FAN");
        PageHelper.startPage(page, size);
        List<Album> albums = fanFavoriteMapper.selectFavoriteAlbums(fanId);
        return new PageInfo<>(albums);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void favoriteSong(Long fanId, Long songId) {
        DataSourceContextHolder.setDataSourceType("FAN");
        
        // 检查歌曲是否存在
        Song song = songMapper.selectById(songId);
        if (song == null) {
            throw new BusinessException(ErrorCode.SONG_NOT_FOUND);
        }

        // 检查是否已收藏
        int existing = fanFavoriteMapper.checkFavoriteSong(fanId, songId);
        if (existing > 0) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS.getCode(), "已经收藏过该歌曲");
        }

        // 添加收藏
        int result = fanFavoriteMapper.insertFavoriteSong(fanId, songId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfavoriteSong(Long fanId, Long songId) {
        DataSourceContextHolder.setDataSourceType("FAN");
        
        int result = fanFavoriteMapper.deleteFavoriteSong(fanId, songId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND.getCode(), "未找到收藏记录");
        }
    }

    @Override
    public PageInfo<Song> getFavoriteSongs(Long fanId, Integer page, Integer size) {
        DataSourceContextHolder.setDataSourceType("FAN");
        PageHelper.startPage(page, size);
        List<Song> songs = fanFavoriteMapper.selectFavoriteSongs(fanId);
        return new PageInfo<>(songs);
    }

    @Override
    public PageInfo<Concert> getAttendedConcerts(Long fanId, Integer page, Integer size) {
        DataSourceContextHolder.setDataSourceType("FAN");
        PageHelper.startPage(page, size);
        List<Concert> concerts = concertAttendanceMapper.selectByFanId(fanId);
        return new PageInfo<>(concerts);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addConcertAttendance(Long fanId, Long concertId) {
        DataSourceContextHolder.setDataSourceType("FAN");
        
        // 检查演唱会是否存在
        Concert concert = concertMapper.selectById(concertId);
        if (concert == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND.getCode(), "演唱会不存在");
        }

        // 检查是否已参加
        int existing = concertAttendanceMapper.checkAttendance(fanId, concertId);
        if (existing > 0) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS.getCode(), "已添加该演唱会参与记录");
        }

        // 添加参与记录
        ConcertAttendance attendance = new ConcertAttendance();
        attendance.setFanId(fanId);
        attendance.setConcertId(concertId);
        
        int result = concertAttendanceMapper.insert(attendance);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeConcertAttendance(Long fanId, Long concertId) {
        DataSourceContextHolder.setDataSourceType("FAN");
        
        int result = concertAttendanceMapper.delete(fanId, concertId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND.getCode(), "未找到参与记录");
        }
    }

    @Override
    public PageInfo<Concert> getAllConcerts(Integer page, Integer size) {
        DataSourceContextHolder.setDataSourceType("FAN");
        PageHelper.startPage(page, size);
        
        // 查询所有演唱会
        List<Concert> concerts = concertMapper.selectAll();
        return new PageInfo<>(concerts);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addReview(AlbumReview review) {
        DataSourceContextHolder.setDataSourceType("FAN");
        
        // 检查专辑是否存在
        Album album = albumMapper.selectById(review.getAlbumId());
        if (album == null) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }

        // 检查是否已评论过
        AlbumReview condition = new AlbumReview();
        condition.setFanId(review.getFanId());
        condition.setAlbumId(review.getAlbumId());
        List<AlbumReview> existing = albumReviewMapper.selectByCondition(condition);
        if (!existing.isEmpty()) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        // 设置评论时间为当前时间
        if (review.getReviewedAt() == null) {
            review.setReviewedAt(new java.util.Date());
        }

        // 添加乐评
        int result = albumReviewMapper.insert(review);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED);
        }
        
        return review.getReviewId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReview(AlbumReview review) {
        DataSourceContextHolder.setDataSourceType("FAN");
        
        AlbumReview existReview = albumReviewMapper.selectById(review.getReviewId());
        if (existReview == null) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_FOUND);
        }

        // 验证权限：只能修改自己的乐评
        if (!existReview.getFanId().equals(review.getFanId())) {
            throw new BusinessException(ErrorCode.FAN_PERMISSION_DENIED);
        }

        int result = albumReviewMapper.update(review);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReview(Long fanId, Long reviewId) {
        DataSourceContextHolder.setDataSourceType("FAN");
        
        AlbumReview existReview = albumReviewMapper.selectById(reviewId);
        if (existReview == null) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_FOUND);
        }

        // 验证权限：只能删除自己的乐评
        if (!existReview.getFanId().equals(fanId)) {
            throw new BusinessException(ErrorCode.FAN_PERMISSION_DENIED);
        }

        int result = albumReviewMapper.deleteById(reviewId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFan(Long fanId) {
        log.info("删除歌迷: fanId={}", fanId);
        
        // 先使用FAN数据源检查歌迷是否存在
        DataSourceContextHolder.setDataSourceType("FAN");
        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND.getCode(), "歌迷不存在");
        }

        // 切换到admin数据源进行删除操作
        DataSourceContextHolder.setDataSourceType("admin");
        
        // 删除歌迷（会级联删除关注的乐队、收藏的专辑、收藏的歌曲、参加的演唱会记录、发表的乐评）
        int result = fanMapper.deleteById(fanId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "删除歌迷失败");
        }

        // 删除对应的用户记录
        userMapper.deleteByRoleAndRelatedId("FAN", fanId);

        log.info("歌迷删除成功: fanId={}", fanId);
    }
}
