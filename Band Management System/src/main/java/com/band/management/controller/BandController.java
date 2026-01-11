package com.band.management.controller;

import com.band.management.common.PageResult;
import com.band.management.common.Result;
import com.band.management.entity.Band;
import com.band.management.service.BandService;
import com.band.management.vo.BandVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 乐队控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/bands")
public class BandController {

    @Autowired
    private BandService bandService;

    /**
     * 创建乐队
     */
    @PostMapping
    public Result<Long> create(@RequestBody @Valid Band band) {
        log.info("创建乐队: {}", band.getName());
        Long bandId = bandService.create(band);
        return Result.success("创建成功", bandId);
    }

    /**
     * 删除乐队
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long bandId) {
        log.info("删除乐队: {}", bandId);
        bandService.delete(bandId);
        return Result.success("删除成功");
    }

    /**
     * 更新乐队
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long bandId, @RequestBody @Valid Band band) {
        log.info("更新乐队: {}", bandId);
        band.setBandId(bandId);
        bandService.update(band);
        return Result.success("更新成功");
    }

    /**
     * 查询乐队详情
     */
    @GetMapping("/{id}")
    public Result<Band> getById(@PathVariable("id") Long bandId) {
        Band band = bandService.getById(bandId);
        return Result.success(band);
    }

    /**
     * 查询乐队详情（包含成员列表）
     */
    @GetMapping("/{id}/detail")
    public Result<BandVO> getDetailById(@PathVariable("id") Long bandId) {
        BandVO bandVO = bandService.getDetailById(bandId);
        return Result.success(bandVO);
    }

    /**
     * 分页查询乐队列表
     */
    @GetMapping
    public Result<PageResult<Band>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            Band condition) {
        PageResult<Band> pageResult = bandService.page(pageNum, pageSize, condition);
        return Result.success(pageResult);
    }

    /**
     * 查询所有乐队
     */
    @GetMapping("/list")
    public Result<List<Band>> list() {
        List<Band> list = bandService.list();
        return Result.success(list);
    }

    /**
     * 根据名称查询乐队
     */
    @GetMapping("/name/{name}")
    public Result<Band> getByName(@PathVariable("name") String name) {
        Band band = bandService.getByName(name);
        return Result.success(band);
    }
}
