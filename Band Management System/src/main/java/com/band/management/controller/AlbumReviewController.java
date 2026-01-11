package com.band.management.controller;

import com.band.management.common.PageResult;
import com.band.management.common.Result;
import com.band.management.entity.AlbumReview;
import com.band.management.service.AlbumReviewService;
import com.band.management.vo.ReviewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 专辑乐评控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/reviews")
public class AlbumReviewController {

    @Autowired
    private AlbumReviewService albumReviewService;

    /**
     * 创建乐评
     */
    @PostMapping
    public Result<Long> create(@RequestBody @Valid AlbumReview albumReview) {
        log.info("创建乐评: 歌迷ID={}, 专辑ID={}", albumReview.getFanId(), albumReview.getAlbumId());
        Long reviewId = albumReviewService.create(albumReview);
        return Result.success("创建成功", reviewId);
    }

    /**
     * 删除乐评
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long reviewId) {
        log.info("删除乐评: {}", reviewId);
        albumReviewService.delete(reviewId);
        return Result.success("删除成功");
    }

    /**
     * 更新乐评
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long reviewId, @RequestBody @Valid AlbumReview albumReview) {
        log.info("更新乐评: {}", reviewId);
        albumReview.setReviewId(reviewId);
        albumReviewService.update(albumReview);
        return Result.success("更新成功");
    }

    /**
     * 查询乐评详情
     */
    @GetMapping("/{id}")
    public Result<AlbumReview> getById(@PathVariable("id") Long reviewId) {
        AlbumReview albumReview = albumReviewService.getById(reviewId);
        return Result.success(albumReview);
    }

    /**
     * 根据专辑ID查询乐评列表
     */
    @GetMapping("/album/{albumId}")
    public Result<List<ReviewVO>> getByAlbumId(@PathVariable("albumId") Long albumId) {
        List<ReviewVO> list = albumReviewService.getByAlbumId(albumId);
        return Result.success(list);
    }

    /**
     * 根据歌迷ID查询乐评列表
     */
    @GetMapping("/fan/{fanId}")
    public Result<List<ReviewVO>> getByFanId(@PathVariable("fanId") Long fanId) {
        List<ReviewVO> list = albumReviewService.getByFanId(fanId);
        return Result.success(list);
    }

    /**
     * 分页查询乐评列表
     */
    @GetMapping
    public Result<PageResult<AlbumReview>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            AlbumReview condition) {
        PageResult<AlbumReview> pageResult = albumReviewService.page(pageNum, pageSize, condition);
        return Result.success(pageResult);
    }
}
