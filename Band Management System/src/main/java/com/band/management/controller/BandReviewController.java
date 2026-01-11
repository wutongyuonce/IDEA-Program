package com.band.management.controller;

import com.band.management.common.PageResult;
import com.band.management.common.Result;
import com.band.management.service.BandReviewService;
import com.band.management.vo.ReviewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 乐队乐评控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/band/{bandId}/reviews")
public class BandReviewController {

    @Autowired
    private BandReviewService bandReviewService;

    /**
     * 查看本乐队专辑乐评
     */
    @GetMapping("/list")
    public Result<List<ReviewVO>> getMyBandReviews(@PathVariable("bandId") Long bandId) {
        log.info("乐队查询自己的专辑乐评: {}", bandId);
        List<ReviewVO> reviews = bandReviewService.getMyBandReviews(bandId);
        return Result.success(reviews);
    }

    /**
     * 按专辑筛选乐评
     */
    @GetMapping("/album/{albumId}")
    public Result<List<ReviewVO>> getByAlbumId(
            @PathVariable("bandId") Long bandId,
            @PathVariable("albumId") Long albumId) {
        List<ReviewVO> reviews = bandReviewService.getByAlbumId(bandId, albumId);
        return Result.success(reviews);
    }

    /**
     * 分页查询本乐队乐评
     */
    @GetMapping
    public Result<PageResult<ReviewVO>> page(
            @PathVariable("bandId") Long bandId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Long albumId) {
        PageResult<ReviewVO> pageResult = bandReviewService.page(bandId, pageNum, pageSize, albumId);
        return Result.success(pageResult);
    }
}
