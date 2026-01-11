package com.band.management.controller;

import com.band.management.common.PageResult;
import com.band.management.common.Result;
import com.band.management.entity.Album;
import com.band.management.service.AlbumService;
import com.band.management.vo.AlbumDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 专辑控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    /**
     * 创建专辑
     */
    @PostMapping
    public Result<Long> create(@RequestBody @Valid Album album) {
        log.info("创建专辑: {}", album.getTitle());
        Long albumId = albumService.create(album);
        return Result.success("创建成功", albumId);
    }

    /**
     * 删除专辑
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long albumId) {
        log.info("删除专辑: {}", albumId);
        albumService.delete(albumId);
        return Result.success("删除成功");
    }

    /**
     * 更新专辑
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long albumId, @RequestBody @Valid Album album) {
        log.info("更新专辑: {}", albumId);
        album.setAlbumId(albumId);
        albumService.update(album);
        return Result.success("更新成功");
    }

    /**
     * 查询专辑详情
     */
    @GetMapping("/{id}")
    public Result<Album> getById(@PathVariable("id") Long albumId) {
        Album album = albumService.getById(albumId);
        return Result.success(album);
    }

    /**
     * 查询专辑详情（包含歌曲列表）
     */
    @GetMapping("/{id}/detail")
    public Result<AlbumDetailVO> getDetailById(@PathVariable("id") Long albumId) {
        AlbumDetailVO albumDetailVO = albumService.getDetailById(albumId);
        return Result.success(albumDetailVO);
    }

    /**
     * 根据乐队ID查询专辑列表
     */
    @GetMapping("/band/{bandId}")
    public Result<List<Album>> getByBandId(@PathVariable("bandId") Long bandId) {
        List<Album> list = albumService.getByBandId(bandId);
        return Result.success(list);
    }

    /**
     * 分页查询专辑列表
     */
    @GetMapping
    public Result<PageResult<Album>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            Album condition) {
        PageResult<Album> pageResult = albumService.page(pageNum, pageSize, condition);
        return Result.success(pageResult);
    }

    /**
     * 查询所有专辑
     */
    @GetMapping("/list")
    public Result<List<Album>> list() {
        List<Album> list = albumService.list();
        return Result.success(list);
    }
}
