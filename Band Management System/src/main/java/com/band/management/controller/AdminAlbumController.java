package com.band.management.controller;

import com.band.management.common.PageResult;
import com.band.management.common.Result;
import com.band.management.entity.Album;
import com.band.management.service.AdminAlbumService;
import com.band.management.vo.AlbumDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/albums")
public class AdminAlbumController {

    @Autowired
    private AdminAlbumService adminAlbumService;

    @PostMapping
    public Result<Long> create(@RequestBody @Valid Album album) {
        log.info("管理员创建专辑: {}", album.getTitle());
        Long albumId = adminAlbumService.create(album);
        return Result.success("创建成功", albumId);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long albumId) {
        log.info("管理员删除专辑: {}", albumId);
        adminAlbumService.delete(albumId);
        return Result.success("删除成功");
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long albumId, @RequestBody @Valid Album album) {
        log.info("管理员更新专辑: {}", albumId);
        album.setAlbumId(albumId);
        adminAlbumService.update(album);
        return Result.success("更新成功");
    }

    @GetMapping("/{id}")
    public Result<Album> getById(@PathVariable("id") Long albumId) {
        Album album = adminAlbumService.getById(albumId);
        return Result.success(album);
    }

    @GetMapping("/{id}/detail")
    public Result<AlbumDetailVO> getDetailById(@PathVariable("id") Long albumId) {
        AlbumDetailVO albumDetailVO = adminAlbumService.getDetailById(albumId);
        return Result.success(albumDetailVO);
    }

    @GetMapping("/band/{bandId}")
    public Result<List<Album>> getByBandId(@PathVariable("bandId") String bandIdStr) {
        if ("all".equalsIgnoreCase(bandIdStr)) {
            List<Album> list = adminAlbumService.list();
            return Result.success(list);
        }
        
        try {
            Long bandId = Long.parseLong(bandIdStr);
            List<Album> list = adminAlbumService.getByBandId(bandId);
            return Result.success(list);
        } catch (NumberFormatException e) {
            log.error("无效的乐队ID: {}", bandIdStr);
            return Result.error(400, "无效的乐队ID");
        }
    }

    @GetMapping
    public Result<PageResult<Album>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            Album condition) {
        PageResult<Album> pageResult = adminAlbumService.page(pageNum, pageSize, condition);
        return Result.success(pageResult);
    }

    @GetMapping("/list")
    public Result<List<Album>> list() {
        List<Album> list = adminAlbumService.list();
        return Result.success(list);
    }
}
