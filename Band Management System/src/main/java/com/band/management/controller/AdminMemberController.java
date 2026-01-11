package com.band.management.controller;

import com.band.management.common.PageResult;
import com.band.management.common.Result;
import com.band.management.entity.Member;
import com.band.management.service.AdminMemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 管理员成员控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/members")
public class AdminMemberController {

    @Autowired
    private AdminMemberService adminMemberService;

    /**
     * 创建成员
     */
    @PostMapping
    public Result<Long> create(@RequestBody @Valid Member member) {
        log.info("管理员创建成员: {}", member.getName());
        Long memberId = adminMemberService.create(member);
        return Result.success("创建成功", memberId);
    }

    /**
     * 删除成员
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long memberId) {
        log.info("管理员删除成员: {}", memberId);
        adminMemberService.delete(memberId);
        return Result.success("删除成功");
    }

    /**
     * 更新成员
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long memberId, @RequestBody @Valid Member member) {
        log.info("管理员更新成员: {}", memberId);
        member.setMemberId(memberId);
        adminMemberService.update(member);
        return Result.success("更新成功");
    }

    /**
     * 查询成员详情
     */
    @GetMapping("/{id}")
    public Result<Member> getById(@PathVariable("id") Long memberId) {
        Member member = adminMemberService.getById(memberId);
        return Result.success(member);
    }

    /**
     * 根据乐队ID查询成员列表
     * 支持传入 "all" 查询所有成员
     */
    @GetMapping("/band/{bandId}")
    public Result<List<Member>> getByBandId(@PathVariable("bandId") String bandIdStr) {
        if ("all".equalsIgnoreCase(bandIdStr)) {
            // 查询所有成员
            List<Member> list = adminMemberService.list();
            return Result.success(list);
        }
        
        try {
            Long bandId = Long.parseLong(bandIdStr);
            List<Member> list = adminMemberService.getByBandId(bandId);
            return Result.success(list);
        } catch (NumberFormatException e) {
            log.error("无效的乐队ID: {}", bandIdStr);
            return Result.error(400, "无效的乐队ID");
        }
    }

    /**
     * 分页查询成员列表
     */
    @GetMapping
    public Result<PageResult<Member>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            Member condition) {
        PageResult<Member> pageResult = adminMemberService.page(pageNum, pageSize, condition);
        return Result.success(pageResult);
    }

    /**
     * 查询所有成员
     */
    @GetMapping("/list")
    public Result<List<Member>> list() {
        List<Member> list = adminMemberService.list();
        return Result.success(list);
    }
}
