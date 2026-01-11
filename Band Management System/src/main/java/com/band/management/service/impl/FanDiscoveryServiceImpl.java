package com.band.management.service.impl;

import com.band.management.config.DataSourceContextHolder;
import com.band.management.entity.*;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.*;
import com.band.management.service.FanDiscoveryService;
import com.band.management.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 歌迷发现与浏览服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class FanDiscoveryServiceImpl implements FanDiscoveryService {

    @Autowired
    private FanMapper fanMapper;

    @Autowired
    private BandMapper bandMapper;

    @Autowired
    private AlbumMapper albumMapper;

    @Autowired
    private SongMapper songMapper;

    @Autowired
    private ConcertMapper concertMapper;

    @Autowired
    private FanFavoriteMapper fanFavoriteMapper;

    @Autowired
    private ConcertAttendanceMapper concertAttendanceMapper;

    @Autowired
    private AlbumRankingMapper albumRankingMapper;

    @Override
    public List<Band> discoverBands(Long fanId) {
        log.info("歌迷发现乐队: {}", fanId);

        DataSourceContextHolder.setDataSourceType("fan");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        List<Band> allBands = bandMapper.selectAll();
        List<Band> favoriteBands = fanFavoriteMapper.selectFavoriteBands(fanId);
        
        List<Long> favoriteBandIds = favoriteBands.stream()
                .map(Band::getBandId)
                .collect(Collectors.toList());

        return allBands.stream()
                .filter(band -> !favoriteBandIds.contains(band.getBandId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Album> discoverAlbums(Long fanId) {
        log.info("歌迷发现专辑: {}", fanId);

        DataSourceContextHolder.setDataSourceType("fan");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        List<Album> allAlbums = albumMapper.selectAll();
        List<Album> favoriteAlbums = fanFavoriteMapper.selectFavoriteAlbums(fanId);
        
        List<Long> favoriteAlbumIds = favoriteAlbums.stream()
                .map(Album::getAlbumId)
                .collect(Collectors.toList());

        return allAlbums.stream()
                .filter(album -> !favoriteAlbumIds.contains(album.getAlbumId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Song> discoverSongs(Long fanId) {
        log.info("歌迷发现歌曲: {}", fanId);

        DataSourceContextHolder.setDataSourceType("fan");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        List<Song> allSongs = songMapper.selectAll();
        List<Song> favoriteSongs = fanFavoriteMapper.selectFavoriteSongs(fanId);
        
        List<Long> favoriteSongIds = favoriteSongs.stream()
                .map(Song::getSongId)
                .collect(Collectors.toList());

        return allSongs.stream()
                .filter(song -> !favoriteSongIds.contains(song.getSongId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Concert> discoverConcerts(Long fanId) {
        log.info("歌迷发现演唱会: {}", fanId);

        DataSourceContextHolder.setDataSourceType("fan");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        List<Concert> allConcerts = concertMapper.selectAll();
        List<Concert> attendedConcerts = concertAttendanceMapper.selectByFanId(fanId);
        
        List<Long> attendedConcertIds = attendedConcerts.stream()
                .map(Concert::getConcertId)
                .collect(Collectors.toList());

        return allConcerts.stream()
                .filter(concert -> !attendedConcertIds.contains(concert.getConcertId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Object> search(Long fanId, String keyword, String type) {
        log.info("歌迷搜索: fanId={}, keyword={}, type={}", fanId, keyword, type);

        DataSourceContextHolder.setDataSourceType("fan");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        if (StringUtil.isEmpty(keyword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "搜索关键词不能为空");
        }

        List<Object> results = new ArrayList<>();

        if (StringUtil.isEmpty(type) || "band".equalsIgnoreCase(type)) {
            List<Band> bands = bandMapper.selectAll().stream()
                    .filter(band -> band.getName() != null && band.getName().contains(keyword))
                    .collect(Collectors.toList());
            results.addAll(bands);
        }

        if (StringUtil.isEmpty(type) || "album".equalsIgnoreCase(type)) {
            List<Album> albums = albumMapper.selectAll().stream()
                    .filter(album -> album.getTitle() != null && album.getTitle().contains(keyword))
                    .collect(Collectors.toList());
            results.addAll(albums);
        }

        if (StringUtil.isEmpty(type) || "song".equalsIgnoreCase(type)) {
            List<Song> songs = songMapper.selectAll().stream()
                    .filter(song -> song.getTitle() != null && song.getTitle().contains(keyword))
                    .collect(Collectors.toList());
            results.addAll(songs);
        }

        if (StringUtil.isEmpty(type) || "concert".equalsIgnoreCase(type)) {
            List<Concert> concerts = concertMapper.selectAll().stream()
                    .filter(concert -> concert.getTitle() != null && concert.getTitle().contains(keyword))
                    .collect(Collectors.toList());
            results.addAll(concerts);
        }

        return results;
    }

    @Override
    public List<AlbumRanking> getAlbumRanking() {
        log.info("查看专辑排行榜");

        DataSourceContextHolder.setDataSourceType("fan");

        return albumRankingMapper.selectTop10();
    }
}
