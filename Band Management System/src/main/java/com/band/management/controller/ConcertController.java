package com.band.management.controller;

import com.band.management.common.PageResult;
import com.band.management.common.Result;
import com.band.management.entity.Concert;
import com.band.management.service.ConcertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 演唱会控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/concerts")
public class ConcertController {

    @Autowired
    private ConcertService concertService;

    /**
     * 创建演唱会
     */
    @PostMapping
    public Result<Long> create(@RequestBody @Valid Concert concert) {
        log.info("创建演唱会: {}", concert.getTitle());
        Long concertId = concertService.create(concert);
        return Result.success("创建成功", concertId);
    }

    /**
     * 删除演唱会
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long concertId) {
        log.info("删除演唱会: {}", concertId);
        concertService.delete(concertId);
        return Result.success("删除成功");
    }

    /**
     * 更新演唱会
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long concertId, @RequestBody @Valid Concert concert) {
        log.info("更新演唱会: {}", concertId);
        concert.setConcertId(concertId);
        concertService.update(concert);
        return Result.success("更新成功");
    }

    /**
     * 查询演唱会详情
     */
    @GetMapping("/{id}")
    public Result<Concert> getById(@PathVariable("id") Long concertId) {
        Concert concert = concertService.getById(concertId);
        return Result.success(concert);
    }

    /**
     * 根据乐队ID查询演唱会列表
     */
    @GetMapping("/band/{bandId}")
    public Result<List<Concert>> getByBandId(@PathVariable("bandId") Long bandId) {
        List<Concert> list = concertService.getByBandId(bandId);
        return Result.success(list);
    }

    /**
     * 分页查询演唱会列表
     */
    @GetMapping
    public Result<PageResult<Concert>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            Concert condition) {
        PageResult<Concert> pageResult = concertService.page(pageNum, pageSize, condition);
        return Result.success(pageResult);
    }

    /**
     * 查询所有演唱会
     */
    @GetMapping("/list")
    public Result<List<Concert>> list() {
        List<Concert> list = concertService.list();
        return Result.success(list);
    }
}
