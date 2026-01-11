package com.band.management.controller;

import com.band.management.common.PageResult;
import com.band.management.common.Result;
import com.band.management.entity.Fan;
import com.band.management.service.FanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 歌迷控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/fans")
public class FanController {

    @Autowired
    private FanService fanService;

    /**
     * 创建歌迷
     */
    @PostMapping
    public Result<Long> create(@RequestBody @Valid Fan fan) {
        log.info("创建歌迷: {}", fan.getName());
        Long fanId = fanService.create(fan);
        return Result.success("创建成功", fanId);
    }

    /**
     * 删除歌迷
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long fanId) {
        log.info("删除歌迷: {}", fanId);
        fanService.delete(fanId);
        return Result.success("删除成功");
    }

    /**
     * 更新歌迷
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long fanId, @RequestBody @Valid Fan fan) {
        log.info("更新歌迷: {}", fanId);
        fan.setFanId(fanId);
        fanService.update(fan);
        return Result.success("更新成功");
    }

    /**
     * 查询歌迷详情
     */
    @GetMapping("/{id}")
    public Result<Fan> getById(@PathVariable("id") Long fanId) {
        Fan fan = fanService.getById(fanId);
        return Result.success(fan);
    }

    /**
     * 分页查询歌迷列表
     */
    @GetMapping
    public Result<PageResult<Fan>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            Fan condition) {
        PageResult<Fan> pageResult = fanService.page(pageNum, pageSize, condition);
        return Result.success(pageResult);
    }

    /**
     * 查询所有歌迷
     */
    @GetMapping("/list")
    public Result<List<Fan>> list() {
        List<Fan> list = fanService.list();
        return Result.success(list);
    }
}
