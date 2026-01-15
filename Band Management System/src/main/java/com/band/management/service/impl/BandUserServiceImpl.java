package com.band.management.service.impl;

import com.band.management.config.DataSourceContextHolder;
import com.band.management.entity.*;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.*;
import com.band.management.service.BandUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 乐队用户服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class BandUserServiceImpl implements BandUserService {

    @Autowired
    private BandMapper bandMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private AlbumMapper albumMapper;

    @Autowired
    private SongMapper songMapper;

    @Autowired
    private ConcertMapper concertMapper;

    @Autowired
    private FanFavoriteMapper fanFavoriteMapper;

    @Autowired
    private FanMapper fanMapper;

    @Autowired
    private AlbumReviewMapper albumReviewMapper;

    @Autowired
    private ConcertAttendanceMapper concertAttendanceMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Band getBandInfo(Long bandId) {
        log.info("获取乐队信息: bandId={}", bandId);
        DataSourceContextHolder.setDataSourceType("band");
        
        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }
        return band;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBandInfo(Band band) {
        log.info("更新乐队信息: bandId={}", band.getBandId());
        DataSourceContextHolder.setDataSourceType("band");
        
        Band existBand = bandMapper.selectById(band.getBandId());
        if (existBand == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }
        
        int result = bandMapper.update(band);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "更新乐队信息失败");
        }
    }

    @Override
    public Map<String, Object> getStatistics(Long bandId) {
        log.info("获取统计数据: bandId={}", bandId);
        DataSourceContextHolder.setDataSourceType("band");
        
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取专辑数量
        Album albumCondition = new Album();
        albumCondition.setBandId(bandId);
        List<Album> albums = albumMapper.selectByCondition(albumCondition);
        statistics.put("albumCount", albums.size());
        
        // 获取歌曲数量 - 通过专辑统计
        int songCount = 0;
        for (Album album : albums) {
            List<Song> songs = songMapper.selectByAlbumId(album.getAlbumId());
            if (songs != null) {
                songCount += songs.size();
            }
        }
        statistics.put("songCount", songCount);
        
        // 获取关注歌迷数量
        int fanCount = fanFavoriteMapper.countFansByBandId(bandId);
        statistics.put("fanCount", fanCount);
        
        return statistics;
    }

    // ==================== 成员管理 ====================

    @Override
    public List<Member> getMembers(Long bandId) {
        log.info("获取成员列表: bandId={}", bandId);
        DataSourceContextHolder.setDataSourceType("band");
        
        return memberMapper.selectByBandId(bandId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addMember(Member member) {
        log.info("添加成员: bandId={}, name={}", member.getBandId(), member.getName());
        DataSourceContextHolder.setDataSourceType("band");
        
        // 检查乐队是否已解散
        Band band = bandMapper.selectById(member.getBandId());
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }
        if ("Y".equals(band.getIsDisbanded())) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "该乐队已解散，无法添加成员");
        }
        
        int result = memberMapper.insert(member);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "添加成员失败");
        }
        return member.getMemberId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMember(Member member) {
        log.info("更新成员: memberId={}", member.getMemberId());
        DataSourceContextHolder.setDataSourceType("band");
        
        // 验证成员属于该乐队
        Member existMember = memberMapper.selectById(member.getMemberId());
        if (existMember == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }
        if (!existMember.getBandId().equals(member.getBandId())) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "无权操作其他乐队的成员");
        }
        
        int result = memberMapper.update(member);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "更新成员失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMember(Long bandId, Long memberId) {
        log.info("删除成员: bandId={}, memberId={}", bandId, memberId);
        DataSourceContextHolder.setDataSourceType("band");
        
        // 验证成员属于该乐队
        Member existMember = memberMapper.selectById(memberId);
        if (existMember == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }
        if (!existMember.getBandId().equals(bandId)) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "无权操作其他乐队的成员");
        }
        
        int result = memberMapper.deleteById(memberId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "删除成员失败");
        }
    }

    // ==================== 专辑管理 ====================

    @Override
    public List<Album> getAlbums(Long bandId) {
        log.info("获取专辑列表: bandId={}", bandId);
        DataSourceContextHolder.setDataSourceType("band");
        
        Album condition = new Album();
        condition.setBandId(bandId);
        return albumMapper.selectByCondition(condition);
    }

    @Override
    public List<Album> getRecentAlbums(Long bandId) {
        log.info("获取最新专辑: bandId={}", bandId);
        DataSourceContextHolder.setDataSourceType("band");
        
        return albumMapper.selectRecentByBandId(bandId, 5);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addAlbum(Album album) {
        log.info("添加专辑: bandId={}, title={}", album.getBandId(), album.getTitle());
        DataSourceContextHolder.setDataSourceType("band");
        
        // 检查乐队是否已解散
        Band band = bandMapper.selectById(album.getBandId());
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }
        if ("Y".equals(band.getIsDisbanded())) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "该乐队已解散，无法添加专辑");
        }
        
        int result = albumMapper.insert(album);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "添加专辑失败");
        }
        return album.getAlbumId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAlbum(Album album) {
        log.info("更新专辑: albumId={}", album.getAlbumId());
        DataSourceContextHolder.setDataSourceType("band");
        
        // 验证专辑属于该乐队
        Album existAlbum = albumMapper.selectById(album.getAlbumId());
        if (existAlbum == null) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }
        if (!existAlbum.getBandId().equals(album.getBandId())) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "无权操作其他乐队的专辑");
        }
        
        int result = albumMapper.update(album);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "更新专辑失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAlbum(Long bandId, Long albumId) {
        log.info("删除专辑: bandId={}, albumId={}", bandId, albumId);
        DataSourceContextHolder.setDataSourceType("band");
        
        // 验证专辑属于该乐队
        Album existAlbum = albumMapper.selectById(albumId);
        if (existAlbum == null) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }
        if (!existAlbum.getBandId().equals(bandId)) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "无权操作其他乐队的专辑");
        }
        
        int result = albumMapper.deleteById(albumId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "删除专辑失败");
        }
    }

    @Override
    public List<Song> getAlbumSongs(Long bandId, Long albumId) {
        log.info("获取专辑歌曲: bandId={}, albumId={}", bandId, albumId);
        DataSourceContextHolder.setDataSourceType("band");
        
        // 验证专辑属于该乐队
        Album existAlbum = albumMapper.selectById(albumId);
        if (existAlbum == null) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }
        if (!existAlbum.getBandId().equals(bandId)) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "无权查看其他乐队的专辑");
        }
        
        return songMapper.selectByAlbumId(albumId);
    }

    @Override
    public List<AlbumReview> getAlbumReviews(Long bandId, Long albumId) {
        log.info("获取专辑评论: bandId={}, albumId={}", bandId, albumId);
        DataSourceContextHolder.setDataSourceType("band");
        
        // 验证专辑属于该乐队
        Album existAlbum = albumMapper.selectById(albumId);
        if (existAlbum == null) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }
        if (!existAlbum.getBandId().equals(bandId)) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "无权查看其他乐队的专辑");
        }
        
        // 查询该专辑的所有评论
        AlbumReview condition = new AlbumReview();
        condition.setAlbumId(albumId);
        return albumReviewMapper.selectByCondition(condition);
    }

    // ==================== 歌曲管理 ====================

    @Override
    public List<Song> getSongs(Long bandId) {
        log.info("获取歌曲列表: bandId={}", bandId);
        DataSourceContextHolder.setDataSourceType("band");
        
        // 歌曲通过专辑关联到乐队，需要先获取该乐队的所有专辑
        Album albumCondition = new Album();
        albumCondition.setBandId(bandId);
        List<Album> albums = albumMapper.selectByCondition(albumCondition);
        
        // 如果没有专辑，返回空列表
        if (albums == null || albums.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 获取所有专辑的歌曲，使用selectByCondition来获取包含albumTitle的完整数据
        List<Song> allSongs = new ArrayList<>();
        for (Album album : albums) {
            Song songCondition = new Song();
            songCondition.setAlbumId(album.getAlbumId());
            List<Song> songs = songMapper.selectByCondition(songCondition);
            if (songs != null) {
                allSongs.addAll(songs);
            }
        }
        
        return allSongs;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addSong(Long bandId, Song song) {
        log.info("添加歌曲: bandId={}, title={}", bandId, song.getTitle());
        DataSourceContextHolder.setDataSourceType("band");
        
        // 检查乐队是否已解散
        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }
        if ("Y".equals(band.getIsDisbanded())) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "该乐队已解散，无法添加歌曲");
        }
        
        // 验证专辑属于该乐队
        if (song.getAlbumId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "专辑ID不能为空");
        }
        
        Album album = albumMapper.selectById(song.getAlbumId());
        if (album == null || !album.getBandId().equals(bandId)) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "专辑不存在或不属于该乐队");
        }
        
        int result = songMapper.insert(song);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "添加歌曲失败");
        }
        return song.getSongId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSong(Long bandId, Song song) {
        log.info("更新歌曲: songId={}", song.getSongId());
        DataSourceContextHolder.setDataSourceType("band");
        
        // 验证歌曲存在
        Song existSong = songMapper.selectById(song.getSongId());
        if (existSong == null) {
            throw new BusinessException(ErrorCode.SONG_NOT_FOUND);
        }
        
        // 验证原专辑属于该乐队
        Album existAlbum = albumMapper.selectById(existSong.getAlbumId());
        if (existAlbum == null || !existAlbum.getBandId().equals(bandId)) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "无权操作其他乐队的歌曲");
        }
        
        // 如果修改了专辑，验证新专辑也属于该乐队
        if (song.getAlbumId() != null && !song.getAlbumId().equals(existSong.getAlbumId())) {
            Album newAlbum = albumMapper.selectById(song.getAlbumId());
            if (newAlbum == null || !newAlbum.getBandId().equals(bandId)) {
                throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "专辑不存在或不属于该乐队");
            }
        }
        
        int result = songMapper.update(song);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "更新歌曲失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSong(Long bandId, Long songId) {
        log.info("删除歌曲: bandId={}, songId={}", bandId, songId);
        DataSourceContextHolder.setDataSourceType("band");
        
        // 验证歌曲存在
        Song existSong = songMapper.selectById(songId);
        if (existSong == null) {
            throw new BusinessException(ErrorCode.SONG_NOT_FOUND);
        }
        
        // 验证专辑属于该乐队
        Album album = albumMapper.selectById(existSong.getAlbumId());
        if (album == null || !album.getBandId().equals(bandId)) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "无权操作其他乐队的歌曲");
        }
        
        int result = songMapper.deleteById(songId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "删除歌曲失败");
        }
    }

    // ==================== 演唱会管理 ====================

    @Override
    public List<Concert> getConcerts(Long bandId) {
        log.info("获取演唱会列表: bandId={}", bandId);
        DataSourceContextHolder.setDataSourceType("band");
        
        Concert condition = new Concert();
        condition.setBandId(bandId);
        List<Concert> concerts = concertMapper.selectByCondition(condition);
        
        // 为每个演唱会添加参与人数统计
        if (concerts != null && !concerts.isEmpty()) {
            for (Concert concert : concerts) {
                int attendanceCount = concertAttendanceMapper.countByConcertId(concert.getConcertId());
                concert.setAttendanceCount(attendanceCount);
            }
        }
        
        return concerts;
    }

    @Override
    public List<Concert> getRecentConcerts(Long bandId) {
        log.info("获取最新演唱会: bandId={}", bandId);
        DataSourceContextHolder.setDataSourceType("band");
        
        List<Concert> concerts = concertMapper.selectRecentByBandId(bandId, 3);
        
        // 为每个演唱会添加参与人数统计
        if (concerts != null && !concerts.isEmpty()) {
            for (Concert concert : concerts) {
                int attendanceCount = concertAttendanceMapper.countByConcertId(concert.getConcertId());
                concert.setAttendanceCount(attendanceCount);
            }
        }
        
        return concerts;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addConcert(Concert concert) {
        log.info("添加演唱会: bandId={}, title={}", concert.getBandId(), concert.getTitle());
        DataSourceContextHolder.setDataSourceType("band");
        
        // 检查乐队是否已解散
        Band band = bandMapper.selectById(concert.getBandId());
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }
        if ("Y".equals(band.getIsDisbanded())) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "该乐队已解散，无法添加演唱会");
        }
        
        int result = concertMapper.insert(concert);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "添加演唱会失败");
        }
        return concert.getConcertId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConcert(Concert concert) {
        log.info("更新演唱会: concertId={}", concert.getConcertId());
        DataSourceContextHolder.setDataSourceType("band");
        
        // 验证演唱会属于该乐队
        Concert existConcert = concertMapper.selectById(concert.getConcertId());
        if (existConcert == null) {
            throw new BusinessException(ErrorCode.CONCERT_NOT_FOUND);
        }
        if (!existConcert.getBandId().equals(concert.getBandId())) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "无权操作其他乐队的演唱会");
        }
        
        int result = concertMapper.update(concert);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "更新演唱会失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConcert(Long bandId, Long concertId) {
        log.info("删除演唱会: bandId={}, concertId={}", bandId, concertId);
        DataSourceContextHolder.setDataSourceType("band");
        
        // 验证演唱会属于该乐队
        Concert existConcert = concertMapper.selectById(concertId);
        if (existConcert == null) {
            throw new BusinessException(ErrorCode.CONCERT_NOT_FOUND);
        }
        if (!existConcert.getBandId().equals(bandId)) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "无权操作其他乐队的演唱会");
        }
        
        int result = concertMapper.deleteById(concertId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "删除演唱会失败");
        }
    }

    // ==================== 歌迷数据 ====================

    @Override
    public List<Fan> getFans(Long bandId) {
        log.info("获取关注歌迷列表: bandId={}", bandId);
        DataSourceContextHolder.setDataSourceType("band");
        
        return fanFavoriteMapper.selectFansByBandId(bandId);
    }

    @Override
    public Map<String, Object> getFanStatistics(Long bandId) {
        log.info("获取歌迷统计数据: bandId={}", bandId);
        DataSourceContextHolder.setDataSourceType("band");
        
        List<Fan> fans = fanFavoriteMapper.selectFansByBandId(bandId);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalFans", fans.size());
        
        // 统计男女比例
        long maleFans = fans.stream().filter(f -> "M".equals(f.getGender())).count();
        long femaleFans = fans.stream().filter(f -> "F".equals(f.getGender())).count();
        statistics.put("maleFans", maleFans);
        statistics.put("femaleFans", femaleFans);
        
        // 计算平均年龄
        if (!fans.isEmpty()) {
            double avgAge = fans.stream()
                .mapToInt(Fan::getAge)
                .average()
                .orElse(0.0);
            statistics.put("avgAge", Math.round(avgAge));
        } else {
            statistics.put("avgAge", 0);
        }
        
        return statistics;
    }

    @Override
    public List<Map<String, Object>> getFanAgeDistribution(Long bandId) {
        log.info("获取歌迷年龄分布: bandId={}", bandId);
        DataSourceContextHolder.setDataSourceType("band");
        
        List<Fan> fans = fanFavoriteMapper.selectFansByBandId(bandId);
        
        // 定义年龄段
        Map<String, Integer> ageRanges = new java.util.LinkedHashMap<>();
        ageRanges.put("18岁以下", 0);
        ageRanges.put("18-25岁", 0);
        ageRanges.put("26-30岁", 0);
        ageRanges.put("31-40岁", 0);
        ageRanges.put("41-50岁", 0);
        ageRanges.put("50岁以上", 0);
        
        // 统计各年龄段人数
        for (Fan fan : fans) {
            int age = fan.getAge();
            if (age < 18) {
                ageRanges.put("18岁以下", ageRanges.get("18岁以下") + 1);
            } else if (age <= 25) {
                ageRanges.put("18-25岁", ageRanges.get("18-25岁") + 1);
            } else if (age <= 30) {
                ageRanges.put("26-30岁", ageRanges.get("26-30岁") + 1);
            } else if (age <= 40) {
                ageRanges.put("31-40岁", ageRanges.get("31-40岁") + 1);
            } else if (age <= 50) {
                ageRanges.put("41-50岁", ageRanges.get("41-50岁") + 1);
            } else {
                ageRanges.put("50岁以上", ageRanges.get("50岁以上") + 1);
            }
        }
        
        // 转换为列表格式
        List<Map<String, Object>> distribution = new ArrayList<>();
        int total = fans.size();
        for (Map.Entry<String, Integer> entry : ageRanges.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("ageRange", entry.getKey());
            item.put("count", entry.getValue());
            if (total > 0) {
                double percentage = (entry.getValue() * 100.0) / total;
                item.put("percentage", Math.round(percentage * 10) / 10.0);
            } else {
                item.put("percentage", 0.0);
            }
            distribution.add(item);
        }
        
        return distribution;
    }

    @Override
    public List<Map<String, Object>> getFanEducationDistribution(Long bandId) {
        log.info("获取歌迷学历分布: bandId={}", bandId);
        DataSourceContextHolder.setDataSourceType("band");
        
        List<Fan> fans = fanFavoriteMapper.selectFansByBandId(bandId);
        
        // 统计各学历人数
        Map<String, Integer> educationCount = new HashMap<>();
        for (Fan fan : fans) {
            String education = fan.getEducation();
            if (education != null && !education.isEmpty()) {
                educationCount.put(education, educationCount.getOrDefault(education, 0) + 1);
            }
        }
        
        // 转换为列表格式
        List<Map<String, Object>> distribution = new ArrayList<>();
        int total = fans.size();
        for (Map.Entry<String, Integer> entry : educationCount.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("education", entry.getKey());
            item.put("count", entry.getValue());
            if (total > 0) {
                double percentage = (entry.getValue() * 100.0) / total;
                item.put("percentage", Math.round(percentage * 10) / 10.0);
            } else {
                item.put("percentage", 0.0);
            }
            distribution.add(item);
        }
        
        // 按人数降序排序
        distribution.sort((a, b) -> ((Integer) b.get("count")).compareTo((Integer) a.get("count")));
        
        return distribution;
    }

    @Override
    public Map<String, Object> getFansByAlbums(Long bandId) {
        log.info("获取喜欢乐队专辑的歌迷统计: bandId={}", bandId);
        DataSourceContextHolder.setDataSourceType("band");
        
        List<Map<String, Object>> albumStats = fanFavoriteMapper.countFansByAlbums(bandId);
        
        // 计算总人数（去重）
        int totalFans = 0;
        if (albumStats != null && !albumStats.isEmpty()) {
            // 使用Set去重，统计喜欢任意专辑的歌迷总数
            java.util.Set<Long> uniqueFanIds = new java.util.HashSet<>();
            
            // 需要重新查询获取fan_id进行去重
            for (Map<String, Object> stat : albumStats) {
                Long albumId = ((Number) stat.get("albumId")).longValue();
                // 查询喜欢该专辑的所有歌迷ID
                List<Long> fanIds = fanFavoriteMapper.selectFanIdsByAlbumId(albumId);
                uniqueFanIds.addAll(fanIds);
            }
            totalFans = uniqueFanIds.size();
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalFans", totalFans);
        result.put("albums", albumStats);
        
        return result;
    }

    @Override
    public Map<String, Object> getFansBySongs(Long bandId) {
        log.info("获取喜欢乐队歌曲的歌迷统计: bandId={}", bandId);
        DataSourceContextHolder.setDataSourceType("band");
        
        List<Map<String, Object>> songStats = fanFavoriteMapper.countFansBySongs(bandId);
        
        // 计算总人数（去重）
        int totalFans = 0;
        if (songStats != null && !songStats.isEmpty()) {
            // 使用Set去重，统计喜欢任意歌曲的歌迷总数
            java.util.Set<Long> uniqueFanIds = new java.util.HashSet<>();
            
            // 需要重新查询获取fan_id进行去重
            for (Map<String, Object> stat : songStats) {
                Long songId = ((Number) stat.get("songId")).longValue();
                // 查询喜欢该歌曲的所有歌迷ID
                List<Long> fanIds = fanFavoriteMapper.selectFanIdsBySongId(songId);
                uniqueFanIds.addAll(fanIds);
            }
            totalFans = uniqueFanIds.size();
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalFans", totalFans);
        result.put("songs", songStats);
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBand(Long bandId) {
        log.info("删除乐队: bandId={}", bandId);
        
        // 先使用band数据源检查乐队是否存在
        DataSourceContextHolder.setDataSourceType("band");
        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND.getCode(), "乐队不存在");
        }

        // 检查是否有成员
        if (band.getMemberCount() != null && band.getMemberCount() > 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "需要先删除所有成员数据");
        }

        // 切换到admin数据源进行删除操作
        DataSourceContextHolder.setDataSourceType("admin");
        
        // 删除乐队（会级联删除专辑、歌曲、演唱会、歌迷关注数据）
        int result = bandMapper.deleteById(bandId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "删除乐队失败");
        }

        // 删除对应的用户记录
        userMapper.deleteByRoleAndRelatedId("BAND", bandId);

        log.info("乐队删除成功: bandId={}", bandId);
    }
}
