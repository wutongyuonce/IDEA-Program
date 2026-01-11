package com.band.management.controller;

import com.band.management.common.PageResult;
import com.band.management.common.Result;
import com.band.management.entity.Song;
import com.band.management.service.BandSongService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 乐队歌曲控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/band/{bandId}/songs")
public class BandSongController {

    @Autowired
    private BandSongService bandSongService;

    /**
     * 获取本乐队歌曲列表
     */
    @GetMapping("/list")
    public Result<List<Song>> getMyBandSongs(@PathVariable("bandId") Long bandId) {
        log.info("乐队查询自己的歌曲列表: {}", bandId);
        List<Song> songs = bandSongService.getMyBandSongs(bandId);
        return Result.success(songs);
    }

    /**
     * 根据专辑ID获取歌曲列表
     */
    @GetMapping("/album/{albumId}")
    public Result<List<Song>> getByAlbumId(
            @PathVariable("bandId") Long bandId,
            @PathVariable("albumId") Long albumId) {
        List<Song> songs = bandSongService.getByAlbumId(bandId, albumId);
        return Result.success(songs);
    }

    /**
     * 分页查询本乐队歌曲
     */
    @GetMapping
    public Result<PageResult<Song>> page(
            @PathVariable("bandId") Long bandId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            Song condition) {
        PageResult<Song> pageResult = bandSongService.page(bandId, pageNum, pageSize, condition);
        return Result.success(pageResult);
    }

    /**
     * 添加歌曲
     */
    @PostMapping
    public Result<Long> createSong(
            @PathVariable("bandId") Long bandId,
            @RequestBody @Valid Song song) {
        log.info("乐队添加歌曲: bandId={}, songTitle={}", bandId, song.getTitle());
        Long songId = bandSongService.createSong(bandId, song);
        return Result.success("添加成功", songId);
    }

    /**
     * 更新歌曲信息
     */
    @PutMapping("/{songId}")
    public Result<Void> updateSong(
            @PathVariable("bandId") Long bandId,
            @PathVariable("songId") Long songId,
            @RequestBody @Valid Song song) {
        log.info("乐队更新歌曲信息: bandId={}, songId={}", bandId, songId);
        song.setSongId(songId);
        bandSongService.updateSong(bandId, song);
        return Result.success("更新成功");
    }

    /**
     * 删除歌曲
     */
    @DeleteMapping("/{songId}")
    public Result<Void> deleteSong(
            @PathVariable("bandId") Long bandId,
            @PathVariable("songId") Long songId) {
        log.info("乐队删除歌曲: bandId={}, songId={}", bandId, songId);
        bandSongService.deleteSong(bandId, songId);
        return Result.success("删除成功");
    }

    /**
     * 查询歌曲详情
     */
    @GetMapping("/{songId}")
    public Result<Song> getById(
            @PathVariable("bandId") Long bandId,
            @PathVariable("songId") Long songId) {
        Song song = bandSongService.getById(bandId, songId);
        return Result.success(song);
    }
}
