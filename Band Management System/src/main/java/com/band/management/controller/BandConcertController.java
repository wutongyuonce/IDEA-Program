package com.band.management.controller;

import com.band.management.common.PageResult;
import com.band.management.common.Result;
import com.band.management.entity.Concert;
import com.band.management.service.BandConcertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 乐队演唱会控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/band/{bandId}/concerts")
public class BandConcertController {

    @Autowired
    private BandConcertService bandConcertService;

    /**
     * 获取本乐队演唱会列表
     */
    @GetMapping("/list")
    public Result<List<Concert>> getMyBandConcerts(@PathVariable("bandId") Long bandId) {
        log.info("乐队查询自己的演唱会列表: {}", bandId);
        List<Concert> concerts = bandConcertService.getMyBandConcerts(bandId);
        return Result.success(concerts);
    }

    /**
     * 分页查询本乐队演唱会
     */
    @GetMapping
    public Result<PageResult<Concert>> page(
            @PathVariable("bandId") Long bandId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            Concert condition) {
        PageResult<Concert> pageResult = bandConcertService.page(bandId, pageNum, pageSize, condition);
        return Result.success(pageResult);
    }

    /**
     * 发布演唱会
     */
    @PostMapping
    public Result<Long> createConcert(
            @PathVariable("bandId") Long bandId,
            @RequestBody @Valid Concert concert) {
        log.info("乐队发布演唱会: bandId={}, concertTitle={}", bandId, concert.getTitle());
        Long concertId = bandConcertService.createConcert(bandId, concert);
        return Result.success("发布成功", concertId);
    }

    /**
     * 更新演唱会信息
     */
    @PutMapping("/{concertId}")
    public Result<Void> updateConcert(
            @PathVariable("bandId") Long bandId,
            @PathVariable("concertId") Long concertId,
            @RequestBody @Valid Concert concert) {
        log.info("乐队更新演唱会信息: bandId={}, concertId={}", bandId, concertId);
        concert.setConcertId(concertId);
        bandConcertService.updateConcert(bandId, concert);
        return Result.success("更新成功");
    }

    /**
     * 删除演唱会
     */
    @DeleteMapping("/{concertId}")
    public Result<Void> deleteConcert(
            @PathVariable("bandId") Long bandId,
            @PathVariable("concertId") Long concertId) {
        log.info("乐队删除演唱会: bandId={}, concertId={}", bandId, concertId);
        bandConcertService.deleteConcert(bandId, concertId);
        return Result.success("删除成功");
    }

    /**
     * 查询演唱会参与人数
     */
    @GetMapping("/{concertId}/attendance")
    public Result<Integer> getAttendanceCount(
            @PathVariable("bandId") Long bandId,
            @PathVariable("concertId") Long concertId) {
        int count = bandConcertService.getAttendanceCount(bandId, concertId);
        return Result.success(count);
    }

    /**
     * 查询演唱会详情
     */
    @GetMapping("/{concertId}")
    public Result<Concert> getById(
            @PathVariable("bandId") Long bandId,
            @PathVariable("concertId") Long concertId) {
        Concert concert = bandConcertService.getById(bandId, concertId);
        return Result.success(concert);
    }
}
