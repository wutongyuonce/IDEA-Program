package com.band.management.controller;

import com.band.management.common.PageResult;
import com.band.management.common.Result;
import com.band.management.entity.Fan;
import com.band.management.service.AdminFanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 管理员歌迷控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/fans")
public class AdminFanController {

    @Autowired
    private AdminFanService adminFanService;

    @PostMapping
    public Result<Long> create(@RequestBody @Valid Fan fan) {
        log.info("管理员创建歌迷: {}", fan.getName());
        Long fanId = adminFanService.create(fan);
        return Result.success("创建成功", fanId);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long fanId) {
        log.info("管理员删除歌迷: {}", fanId);
        adminFanService.delete(fanId);
        return Result.success("删除成功");
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long fanId, @RequestBody @Valid Fan fan) {
        log.info("管理员更新歌迷: {}", fanId);
        fan.setFanId(fanId);
        adminFanService.update(fan);
        return Result.success("更新成功");
    }

    @GetMapping("/{id}")
    public Result<Fan> getById(@PathVariable("id") Long fanId) {
        Fan fan = adminFanService.getById(fanId);
        return Result.success(fan);
    }

    @GetMapping
    public Result<PageResult<Fan>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            Fan condition) {
        PageResult<Fan> pageResult = adminFanService.page(pageNum, pageSize, condition);
        return Result.success(pageResult);
    }

    @GetMapping("/list")
    public Result<List<Fan>> list() {
        List<Fan> list = adminFanService.list();
        return Result.success(list);
    }
}
