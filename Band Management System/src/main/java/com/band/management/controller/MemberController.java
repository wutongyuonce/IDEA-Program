package com.band.management.controller;

import com.band.management.common.PageResult;
import com.band.management.common.Result;
import com.band.management.entity.Member;
import com.band.management.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 成员控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/members")
public class MemberController {

    @Autowired
    private MemberService memberService;

    /**
     * 创建成员
     */
    @PostMapping
    public Result<Long> create(@RequestBody @Valid Member member) {
        log.info("创建成员: {}", member.getName());
        Long memberId = memberService.create(member);
        return Result.success("创建成功", memberId);
    }

    /**
     * 删除成员
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long memberId) {
        log.info("删除成员: {}", memberId);
        memberService.delete(memberId);
        return Result.success("删除成功");
    }

    /**
     * 更新成员
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long memberId, @RequestBody @Valid Member member) {
        log.info("更新成员: {}", memberId);
        member.setMemberId(memberId);
        memberService.update(member);
        return Result.success("更新成功");
    }

    /**
     * 查询成员详情
     */
    @GetMapping("/{id}")
    public Result<Member> getById(@PathVariable("id") Long memberId) {
        Member member = memberService.getById(memberId);
        return Result.success(member);
    }

    /**
     * 根据乐队ID查询成员列表
     */
    @GetMapping("/band/{bandId}")
    public Result<List<Member>> getByBandId(@PathVariable("bandId") Long bandId) {
        List<Member> list = memberService.getByBandId(bandId);
        return Result.success(list);
    }

    /**
     * 分页查询成员列表
     */
    @GetMapping
    public Result<PageResult<Member>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            Member condition) {
        PageResult<Member> pageResult = memberService.page(pageNum, pageSize, condition);
        return Result.success(pageResult);
    }

    /**
     * 查询所有成员
     */
    @GetMapping("/list")
    public Result<List<Member>> list() {
        List<Member> list = memberService.list();
        return Result.success(list);
    }
}
