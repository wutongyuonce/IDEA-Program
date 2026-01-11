package com.band.management.controller;

import com.band.management.common.PageResult;
import com.band.management.common.Result;
import com.band.management.entity.Album;
import com.band.management.service.BandAlbumService;
import com.band.management.vo.AlbumDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 乐队专辑控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/band/{bandId}/albums")
public class BandAlbumController {

    @Autowired
    private BandAlbumService bandAlbumService;

    /**
     * 获取本乐队专辑列表
     */
    @GetMapping("/list")
    public Result<List<Album>> getMyBandAlbums(@PathVariable("bandId") Long bandId) {
        log.info("乐队查询自己的专辑列表: {}", bandId);
        List<Album> albums = bandAlbumService.getMyBandAlbums(bandId);
        return Result.success(albums);
    }

    /**
     * 分页查询本乐队专辑
     */
    @GetMapping
    public Result<PageResult<Album>> page(
            @PathVariable("bandId") Long bandId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            Album condition) {
        PageResult<Album> pageResult = bandAlbumService.page(bandId, pageNum, pageSize, condition);
        return Result.success(pageResult);
    }

    /**
     * 发布新专辑
     */
    @PostMapping
    public Result<Long> createAlbum(
            @PathVariable("bandId") Long bandId,
            @RequestBody @Valid Album album) {
        log.info("乐队发布新专辑: bandId={}, albumTitle={}", bandId, album.getTitle());
        Long albumId = bandAlbumService.createAlbum(bandId, album);
        return Result.success("发布成功", albumId);
    }

    /**
     * 更新专辑信息
     */
    @PutMapping("/{albumId}")
    public Result<Void> updateAlbum(
            @PathVariable("bandId") Long bandId,
            @PathVariable("albumId") Long albumId,
            @RequestBody @Valid Album album) {
        log.info("乐队更新专辑信息: bandId={}, albumId={}", bandId, albumId);
        album.setAlbumId(albumId);
        bandAlbumService.updateAlbum(bandId, album);
        return Result.success("更新成功");
    }

    /**
     * 删除专辑
     */
    @DeleteMapping("/{albumId}")
    public Result<Void> deleteAlbum(
            @PathVariable("bandId") Long bandId,
            @PathVariable("albumId") Long albumId) {
        log.info("乐队删除专辑: bandId={}, albumId={}", bandId, albumId);
        bandAlbumService.deleteAlbum(bandId, albumId);
        return Result.success("删除成功");
    }

    /**
     * 查询专辑详情（包含歌曲列表和评分）
     */
    @GetMapping("/{albumId}/detail")
    public Result<AlbumDetailVO> getAlbumDetail(
            @PathVariable("bandId") Long bandId,
            @PathVariable("albumId") Long albumId) {
        AlbumDetailVO albumDetailVO = bandAlbumService.getAlbumDetail(bandId, albumId);
        return Result.success(albumDetailVO);
    }

    /**
     * 查询专辑基本信息
     */
    @GetMapping("/{albumId}")
    public Result<Album> getById(
            @PathVariable("bandId") Long bandId,
            @PathVariable("albumId") Long albumId) {
        Album album = bandAlbumService.getById(bandId, albumId);
        return Result.success(album);
    }
}
