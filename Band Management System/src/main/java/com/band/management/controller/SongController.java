package com.band.management.controller;

import com.band.management.common.PageResult;
import com.band.management.common.Result;
import com.band.management.entity.Song;
import com.band.management.service.SongService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 歌曲控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/songs")
public class SongController {

    @Autowired
    private SongService songService;

    /**
     * 创建歌曲
     */
    @PostMapping
    public Result<Long> create(@RequestBody @Valid Song song) {
        log.info("创建歌曲: {}", song.getTitle());
        Long songId = songService.create(song);
        return Result.success("创建成功", songId);
    }

    /**
     * 删除歌曲
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long songId) {
        log.info("删除歌曲: {}", songId);
        songService.delete(songId);
        return Result.success("删除成功");
    }

    /**
     * 更新歌曲
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long songId, @RequestBody @Valid Song song) {
        log.info("更新歌曲: {}", songId);
        song.setSongId(songId);
        songService.update(song);
        return Result.success("更新成功");
    }

    /**
     * 查询歌曲详情
     */
    @GetMapping("/{id}")
    public Result<Song> getById(@PathVariable("id") Long songId) {
        Song song = songService.getById(songId);
        return Result.success(song);
    }

    /**
     * 根据专辑ID查询歌曲列表
     */
    @GetMapping("/album/{albumId}")
    public Result<List<Song>> getByAlbumId(@PathVariable("albumId") Long albumId) {
        List<Song> list = songService.getByAlbumId(albumId);
        return Result.success(list);
    }

    /**
     * 分页查询歌曲列表
     */
    @GetMapping
    public Result<PageResult<Song>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            Song condition) {
        PageResult<Song> pageResult = songService.page(pageNum, pageSize, condition);
        return Result.success(pageResult);
    }

    /**
     * 查询所有歌曲
     */
    @GetMapping("/list")
    public Result<List<Song>> list() {
        List<Song> list = songService.list();
        return Result.success(list);
    }
}
