package com.band.management.controller;

import com.band.management.common.PageResult;
import com.band.management.common.Result;
import com.band.management.entity.Band;
import com.band.management.service.AdminBandService;
import com.band.management.vo.BandVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 管理员乐队控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/bands")
public class AdminBandController {

    @Autowired
    private AdminBandService adminBandService;

    /**
     * 创建乐队
     */
    @PostMapping
    public Result<Long> create(@RequestBody @Valid Band band) {
        log.info("管理员创建乐队: {}", band.getName());
        Long bandId = adminBandService.create(band);
        return Result.success("创建成功", bandId);
    }

    /**
     * 删除乐队
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long bandId) {
        log.info("管理员删除乐队: {}", bandId);
        adminBandService.delete(bandId);
        return Result.success("删除成功");
    }

    /**
     * 更新乐队
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long bandId, @RequestBody @Valid Band band) {
        log.info("管理员更新乐队: {}", bandId);
        band.setBandId(bandId);
        adminBandService.update(band);
        return Result.success("更新成功");
    }

    /**
     * 查询乐队详情
     */
    @GetMapping("/{id}")
    public Result<Band> getById(@PathVariable("id") Long bandId) {
        Band band = adminBandService.getById(bandId);
        return Result.success(band);
    }

    /**
     * 查询乐队详情（包含成员列表）
     */
    @GetMapping("/{id}/detail")
    public Result<BandVO> getDetailById(@PathVariable("id") Long bandId) {
        BandVO bandVO = adminBandService.getDetailById(bandId);
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
        PageResult<Band> pageResult = adminBandService.page(pageNum, pageSize, condition);
        return Result.success(pageResult);
    }

    /**
     * 查询所有乐队
     */
    @GetMapping("/list")
    public Result<List<Band>> list() {
        List<Band> list = adminBandService.list();
        return Result.success(list);
    }

    /**
     * 设置乐队队长
     */
    @PutMapping("/{bandId}/leader/{memberId}")
    public Result<Void> setLeader(
            @PathVariable("bandId") Long bandId,
            @PathVariable("memberId") Long memberId) {
        log.info("管理员设置队长: bandId={}, memberId={}", bandId, memberId);
        adminBandService.setLeader(bandId, memberId);
        return Result.success("设置队长成功");
    }

    /**
     * 解散乐队
     */
    @PutMapping("/{bandId}/disband")
    public Result<Void> disband(@PathVariable("bandId") Long bandId) {
        log.info("管理员解散乐队: {}", bandId);
        adminBandService.disband(bandId);
        return Result.success("解散乐队成功");
    }
}
