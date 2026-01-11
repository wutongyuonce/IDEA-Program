package com.band.management.controller;

import com.band.management.common.PageResult;
import com.band.management.common.Result;
import com.band.management.entity.Concert;
import com.band.management.service.AdminConcertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 管理员演唱会控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/concerts")
public class AdminConcertController {

    @Autowired
    private AdminConcertService adminConcertService;

    @PostMapping
    public Result<Long> create(@RequestBody @Valid Concert concert) {
        log.info("管理员创建演唱会: {}", concert.getTitle());
        Long concertId = adminConcertService.create(concert);
        return Result.success("创建成功", concertId);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long concertId) {
        log.info("管理员删除演唱会: {}", concertId);
        adminConcertService.delete(concertId);
        return Result.success("删除成功");
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long concertId, @RequestBody @Valid Concert concert) {
        log.info("管理员更新演唱会: {}", concertId);
        concert.setConcertId(concertId);
        adminConcertService.update(concert);
        return Result.success("更新成功");
    }

    @GetMapping("/{id}")
    public Result<Concert> getById(@PathVariable("id") Long concertId) {
        Concert concert = adminConcertService.getById(concertId);
        return Result.success(concert);
    }

    @GetMapping("/band/{bandId}")
    public Result<List<Concert>> getByBandId(@PathVariable("bandId") String bandIdStr) {
        if ("all".equalsIgnoreCase(bandIdStr)) {
            List<Concert> concerts = adminConcertService.list();
            return Result.success(concerts);
        }
        
        try {
            Long bandId = Long.parseLong(bandIdStr);
            List<Concert> concerts = adminConcertService.getByBandId(bandId);
            return Result.success(concerts);
        } catch (NumberFormatException e) {
            log.error("无效的乐队ID: {}", bandIdStr);
            return Result.error(400, "无效的乐队ID");
        }
    }

    @GetMapping
    public Result<PageResult<Concert>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            Concert condition) {
        PageResult<Concert> pageResult = adminConcertService.page(pageNum, pageSize, condition);
        return Result.success(pageResult);
    }

    @GetMapping("/list")
    public Result<List<Concert>> list() {
        List<Concert> list = adminConcertService.list();
        return Result.success(list);
    }
}
