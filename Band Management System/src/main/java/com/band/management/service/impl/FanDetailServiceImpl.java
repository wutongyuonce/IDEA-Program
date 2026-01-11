package com.band.management.service.impl;

import com.band.management.config.DataSourceContextHolder;
import com.band.management.entity.*;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.*;
import com.band.management.service.FanDetailService;
import com.band.management.vo.AlbumDetailVO;
import com.band.management.vo.BandVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 歌迷详情查看服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class FanDetailServiceImpl implements FanDetailService {

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
    private MemberMapper memberMapper;

    @Override
    public BandVO getBandDetail(Long fanId, Long bandId) {
        log.info("歌迷查看乐队详情: fanId={}, bandId={}", fanId, bandId);

        DataSourceContextHolder.setDataSourceType("fan");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        BandVO bandVO = new BandVO();
        bandVO.setBandId(band.getBandId());
        bandVO.setName(band.getName());
        bandVO.setFoundedAt(band.getFoundedAt());
        bandVO.setIntro(band.getIntro());
        bandVO.setLeaderMemberId(band.getLeaderMemberId());
        bandVO.setMemberCount(band.getMemberCount());
        bandVO.setCreatedAt(band.getCreatedAt());
        bandVO.setUpdatedAt(band.getUpdatedAt());

        List<Member> members = memberMapper.selectByBandId(bandId);
        bandVO.setMembers(members);

        return bandVO;
    }

    @Override
    public AlbumDetailVO getAlbumDetail(Long fanId, Long albumId) {
        log.info("歌迷查看专辑详情: fanId={}, albumId={}", fanId, albumId);

        DataSourceContextHolder.setDataSourceType("fan");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        Album album = albumMapper.selectById(albumId);
        if (album == null) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }

        AlbumDetailVO albumDetailVO = new AlbumDetailVO();
        albumDetailVO.setAlbumId(album.getAlbumId());
        albumDetailVO.setBandId(album.getBandId());
        albumDetailVO.setTitle(album.getTitle());
        albumDetailVO.setReleaseDate(album.getReleaseDate());
        albumDetailVO.setCopywriting(album.getCopywriting());
        albumDetailVO.setAvgScore(album.getAvgScore());
        albumDetailVO.setCreatedAt(album.getCreatedAt());
        albumDetailVO.setUpdatedAt(album.getUpdatedAt());

        List<Song> songs = songMapper.selectByAlbumId(albumId);
        albumDetailVO.setSongs(songs);
        albumDetailVO.setSongCount(songs.size());

        return albumDetailVO;
    }

    @Override
    public Song getSongDetail(Long fanId, Long songId) {
        log.info("歌迷查看歌曲详情: fanId={}, songId={}", fanId, songId);

        DataSourceContextHolder.setDataSourceType("fan");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        Song song = songMapper.selectById(songId);
        if (song == null) {
            throw new BusinessException(ErrorCode.SONG_NOT_FOUND);
        }

        return song;
    }

    @Override
    public Concert getConcertDetail(Long fanId, Long concertId) {
        log.info("歌迷查看演唱会详情: fanId={}, concertId={}", fanId, concertId);

        DataSourceContextHolder.setDataSourceType("fan");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        Concert concert = concertMapper.selectById(concertId);
        if (concert == null) {
            throw new BusinessException(ErrorCode.CONCERT_NOT_FOUND);
        }

        return concert;
    }
}
