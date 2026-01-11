package com.band.management.controller;

import com.band.management.common.PageResult;
import com.band.management.common.Result;
import com.band.management.entity.AlbumReview;
import com.band.management.service.AdminReviewService;
import com.band.management.vo.ReviewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员乐评控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/reviews")
public class AdminReviewController {

    @Autowired
    private AdminReviewService adminReviewService;

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long reviewId) {
        log.info("管理员删除乐评: {}", reviewId);
        adminReviewService.delete(reviewId);
        return Result.success("删除成功");
    }

    @GetMapping("/{id}")
    public Result<AlbumReview> getById(@PathVariable("id") Long reviewId) {
        AlbumReview review = adminReviewService.getById(reviewId);
        return Result.success(review);
    }

    @GetMapping("/album/{albumId}")
    public Result getByAlbumId(@PathVariable("albumId") String albumIdStr) {
        if ("all".equalsIgnoreCase(albumIdStr)) {
            List<AlbumReview> reviews = adminReviewService.list();
            return Result.success(reviews);
        }
        
        try {
            Long albumId = Long.parseLong(albumIdStr);
            List<ReviewVO> reviews = adminReviewService.getByAlbumId(albumId);
            return Result.success(reviews);
        } catch (NumberFormatException e) {
            log.error("无效的专辑ID: {}", albumIdStr);
            return Result.error(400, "无效的专辑ID");
        }
    }

    @GetMapping("/fan/{fanId}")
    public Result getByFanId(@PathVariable("fanId") String fanIdStr) {
        if ("all".equalsIgnoreCase(fanIdStr)) {
            List<AlbumReview> reviews = adminReviewService.list();
            return Result.success(reviews);
        }
        
        try {
            Long fanId = Long.parseLong(fanIdStr);
            List<ReviewVO> reviews = adminReviewService.getByFanId(fanId);
            return Result.success(reviews);
        } catch (NumberFormatException e) {
            log.error("无效的歌迷ID: {}", fanIdStr);
            return Result.error(400, "无效的歌迷ID");
        }
    }

    @GetMapping
    public Result<PageResult<AlbumReview>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            AlbumReview condition) {
        PageResult<AlbumReview> pageResult = adminReviewService.page(pageNum, pageSize, condition);
        return Result.success(pageResult);
    }

    @GetMapping("/list")
    public Result<List<AlbumReview>> list() {
        List<AlbumReview> list = adminReviewService.list();
        return Result.success(list);
    }
}
