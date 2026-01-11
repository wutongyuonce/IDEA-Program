package com.band.management.controller;

import com.band.management.common.Result;
import com.band.management.entity.AlbumReview;
import com.band.management.service.FanReviewService;
import com.band.management.vo.ReviewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 歌迷乐评控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/fan/{fanId}/reviews")
public class FanReviewController {

    @Autowired
    private FanReviewService fanReviewService;

    @PostMapping
    public Result<Void> createReview(
            @PathVariable("fanId") Long fanId,
            @RequestBody AlbumReview review) {
        log.info("歌迷发表乐评: fanId={}, albumId={}", fanId, review.getAlbumId());
        fanReviewService.createReview(fanId, review);
        return Result.success("发表乐评成功");
    }

    @GetMapping
    public Result<List<ReviewVO>> getMyReviews(@PathVariable("fanId") Long fanId) {
        log.info("歌迷查看我的乐评列表: {}", fanId);
        List<ReviewVO> reviews = fanReviewService.getMyReviews(fanId);
        return Result.success(reviews);
    }

    @PutMapping("/{reviewId}")
    public Result<Void> updateReview(
            @PathVariable("fanId") Long fanId,
            @PathVariable("reviewId") Long reviewId,
            @RequestBody AlbumReview review) {
        log.info("歌迷修改乐评: fanId={}, reviewId={}", fanId, reviewId);
        fanReviewService.updateReview(fanId, reviewId, review);
        return Result.success("修改乐评成功");
    }

    @DeleteMapping("/{reviewId}")
    public Result<Void> deleteReview(
            @PathVariable("fanId") Long fanId,
            @PathVariable("reviewId") Long reviewId) {
        log.info("歌迷删除乐评: fanId={}, reviewId={}", fanId, reviewId);
        fanReviewService.deleteReview(fanId, reviewId);
        return Result.success("删除乐评成功");
    }

    @GetMapping("/album/{albumId}")
    public Result<List<ReviewVO>> getAlbumReviews(
            @PathVariable("fanId") Long fanId,
            @PathVariable("albumId") Long albumId) {
        log.info("歌迷查看专辑乐评: fanId={}, albumId={}", fanId, albumId);
        List<ReviewVO> reviews = fanReviewService.getAlbumReviews(fanId, albumId);
        return Result.success(reviews);
    }
}
