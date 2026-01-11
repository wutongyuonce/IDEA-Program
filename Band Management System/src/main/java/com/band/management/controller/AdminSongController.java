package com.band.management.controller;

import com.band.management.common.PageResult;
import com.band.management.common.Result;
import com.band.management.entity.Song;
import com.band.management.service.AdminSongService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 管理员歌曲控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/songs")
public class AdminSongController {

    @Autowired
    private AdminSongService adminSongService;

    @PostMapping
    public Result<Long> create(@RequestBody @Valid Song song) {
        log.info("管理员创建歌曲: {}", song.getTitle());
        Long songId = adminSongService.create(song);
        return Result.success("创建成功", songId);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long songId) {
        log.info("管理员删除歌曲: {}", songId);
        adminSongService.delete(songId);
        return Result.success("删除成功");
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long songId, @RequestBody @Valid Song song) {
        log.info("管理员更新歌曲: {}", songId);
        song.setSongId(songId);
        adminSongService.update(song);
        return Result.success("更新成功");
    }

    @GetMapping("/{id}")
    public Result<Song> getById(@PathVariable("id") Long songId) {
        Song song = adminSongService.getById(songId);
        return Result.success(song);
    }

    @GetMapping("/album/{albumId}")
    public Result<List<Song>> getByAlbumId(@PathVariable("albumId") String albumIdStr) {
        if ("all".equalsIgnoreCase(albumIdStr)) {
            List<Song> songs = adminSongService.list();
            return Result.success(songs);
        }
        
        try {
            Long albumId = Long.parseLong(albumIdStr);
            List<Song> songs = adminSongService.getByAlbumId(albumId);
            return Result.success(songs);
        } catch (NumberFormatException e) {
            log.error("无效的专辑ID: {}", albumIdStr);
            return Result.error(400, "无效的专辑ID");
        }
    }

    @GetMapping
    public Result<PageResult<Song>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            Song condition) {
        PageResult<Song> pageResult = adminSongService.page(pageNum, pageSize, condition);
        return Result.success(pageResult);
    }

    @GetMapping("/list")
    public Result<List<Song>> list() {
        List<Song> list = adminSongService.list();
        return Result.success(list);
    }
}
