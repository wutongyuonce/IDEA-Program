package com.band.management.service;

import com.band.management.entity.Band;
import com.band.management.entity.Concert;
import com.band.management.entity.Song;
import com.band.management.vo.AlbumDetailVO;
import com.band.management.vo.BandVO;

/**
 * 歌迷详情查看服务接口
 * 
 * @author Band Management Team
 */
public interface FanDetailService {

    /**
     * 查看乐队详情
     */
    BandVO getBandDetail(Long fanId, Long bandId);

    /**
     * 查看专辑详情（含歌曲列表）
     */
    AlbumDetailVO getAlbumDetail(Long fanId, Long albumId);

    /**
     * 查看歌曲详情
     */
    Song getSongDetail(Long fanId, Long songId);

    /**
     * 查看演唱会详情
     */
    Concert getConcertDetail(Long fanId, Long concertId);
}
