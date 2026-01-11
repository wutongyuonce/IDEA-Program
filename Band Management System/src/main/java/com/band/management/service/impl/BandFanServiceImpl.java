package com.band.management.service.impl;

import com.band.management.common.PageResult;
import com.band.management.config.DataSourceContextHolder;
import com.band.management.entity.*;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.*;
import com.band.management.service.BandFanService;
import com.band.management.vo.FanStatisticsVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 乐队歌迷服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class BandFanServiceImpl implements BandFanService {

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

    @Override
    public List<Fan> getMyBandFans(Long bandId) {
        log.info("乐队查询自己的歌迷列表: {}", bandId);

        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        List<Band> favoriteBands = new ArrayList<>();
        List<Fan> allFans = fanMapper.selectAll();
        Set<Long> fanIds = new HashSet<>();

        for (Fan fan : allFans) {
            favoriteBands = fanFavoriteMapper.selectFavoriteBands(fan.getFanId());
            for (Band favBand : favoriteBands) {
                if (favBand.getBandId().equals(bandId)) {
                    fanIds.add(fan.getFanId());
                    break;
                }
            }
        }

        return allFans.stream()
                .filter(fan -> fanIds.contains(fan.getFanId()))
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<Fan> page(Long bandId, int pageNum, int pageSize, Fan condition) {
        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        PageHelper.startPage(pageNum, pageSize);
        List<Fan> fans = getMyBandFans(bandId);
        
        if (condition != null) {
            fans = fans.stream()
                    .filter(fan -> {
                        if (condition.getName() != null && !fan.getName().contains(condition.getName())) {
                            return false;
                        }
                        if (condition.getGender() != null && !condition.getGender().equals(fan.getGender())) {
                            return false;
                        }
                        if (condition.getOccupation() != null && !condition.getOccupation().equals(fan.getOccupation())) {
                            return false;
                        }
                        if (condition.getEducation() != null && !condition.getEducation().equals(fan.getEducation())) {
                            return false;
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
        }

        return PageResult.of(fans);
    }

    @Override
    public FanStatisticsVO getStatistics(Long bandId) {
        log.info("乐队查询歌迷统计分析: {}", bandId);

        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        List<Fan> fans = getMyBandFans(bandId);

        FanStatisticsVO statistics = new FanStatisticsVO();
        statistics.setTotalFans(fans.size());

        Map<String, Integer> genderDist = fans.stream()
                .filter(fan -> fan.getGender() != null)
                .collect(Collectors.groupingBy(
                        Fan::getGender,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        statistics.setGenderDistribution(genderDist);

        Map<String, Integer> ageDist = fans.stream()
                .filter(fan -> fan.getAge() != null)
                .collect(Collectors.groupingBy(
                        fan -> getAgeGroup(fan.getAge()),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        statistics.setAgeDistribution(ageDist);

        Map<String, Integer> occupationDist = fans.stream()
                .filter(fan -> fan.getOccupation() != null)
                .collect(Collectors.groupingBy(
                        Fan::getOccupation,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        statistics.setOccupationDistribution(occupationDist);

        Map<String, Integer> educationDist = fans.stream()
                .filter(fan -> fan.getEducation() != null)
                .collect(Collectors.groupingBy(
                        Fan::getEducation,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        statistics.setEducationDistribution(educationDist);

        return statistics;
    }

    @Override
    public List<Fan> getAlbumFans(Long bandId, Long albumId) {
        log.info("乐队查询专辑歌迷: bandId={}, albumId={}", bandId, albumId);

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
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权查看其他乐队的专辑歌迷");
        }

        List<Fan> allFans = fanMapper.selectAll();
        Set<Long> fanIds = new HashSet<>();

        for (Fan fan : allFans) {
            List<Album> favoriteAlbums = fanFavoriteMapper.selectFavoriteAlbums(fan.getFanId());
            for (Album favAlbum : favoriteAlbums) {
                if (favAlbum.getAlbumId().equals(albumId)) {
                    fanIds.add(fan.getFanId());
                    break;
                }
            }
        }

        return allFans.stream()
                .filter(fan -> fanIds.contains(fan.getFanId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Fan> getSongFans(Long bandId, Long songId) {
        log.info("乐队查询歌曲歌迷: bandId={}, songId={}", bandId, songId);

        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        Song song = songMapper.selectById(songId);
        if (song == null) {
            throw new BusinessException(ErrorCode.SONG_NOT_FOUND);
        }

        Album album = albumMapper.selectById(song.getAlbumId());
        if (album == null || !album.getBandId().equals(bandId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权查看其他乐队的歌曲歌迷");
        }

        List<Fan> allFans = fanMapper.selectAll();
        Set<Long> fanIds = new HashSet<>();

        for (Fan fan : allFans) {
            List<Song> favoriteSongs = fanFavoriteMapper.selectFavoriteSongs(fan.getFanId());
            for (Song favSong : favoriteSongs) {
                if (favSong.getSongId().equals(songId)) {
                    fanIds.add(fan.getFanId());
                    break;
                }
            }
        }

        return allFans.stream()
                .filter(fan -> fanIds.contains(fan.getFanId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Fan> getConcertFans(Long bandId, Long concertId) {
        log.info("乐队查询演唱会参与歌迷: bandId={}, concertId={}", bandId, concertId);

        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        Concert concert = concertMapper.selectById(concertId);
        if (concert == null) {
            throw new BusinessException(ErrorCode.CONCERT_NOT_FOUND);
        }

        if (!concert.getBandId().equals(bandId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权查看其他乐队的演唱会歌迷");
        }

        List<ConcertAttendance> attendances = concertAttendanceMapper.selectByConcertId(concertId);
        List<Fan> fans = new ArrayList<>();

        for (ConcertAttendance attendance : attendances) {
            Fan fan = fanMapper.selectById(attendance.getFanId());
            if (fan != null) {
                fans.add(fan);
            }
        }

        return fans;
    }

    /**
     * 获取年龄段
     */
    private String getAgeGroup(Integer age) {
        if (age < 18) {
            return "18岁以下";
        } else if (age < 25) {
            return "18-24岁";
        } else if (age < 35) {
            return "25-34岁";
        } else if (age < 45) {
            return "35-44岁";
        } else if (age < 55) {
            return "45-54岁";
        } else {
            return "55岁以上";
        }
    }
}
