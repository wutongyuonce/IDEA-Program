package com.band.management.controller;

import com.band.management.common.PageResult;
import com.band.management.common.Result;
import com.band.management.entity.Member;
import com.band.management.service.BandMemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 乐队成员控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/band/{bandId}/members")
public class BandMemberController {

    @Autowired
    private BandMemberService bandMemberService;

    /**
     * 获取本乐队成员列表
     */
    @GetMapping("/list")
    public Result<List<Member>> getMyBandMembers(@PathVariable("bandId") Long bandId) {
        log.info("乐队查询自己的成员列表: {}", bandId);
        List<Member> members = bandMemberService.getMyBandMembers(bandId);
        return Result.success(members);
    }

    /**
     * 分页查询本乐队成员
     */
    @GetMapping
    public Result<PageResult<Member>> page(
            @PathVariable("bandId") Long bandId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            Member condition) {
        PageResult<Member> pageResult = bandMemberService.page(bandId, pageNum, pageSize, condition);
        return Result.success(pageResult);
    }

    /**
     * 添加成员
     */
    @PostMapping
    public Result<Long> addMember(
            @PathVariable("bandId") Long bandId,
            @RequestBody @Valid Member member) {
        log.info("乐队添加成员: bandId={}, memberName={}", bandId, member.getName());
        Long memberId = bandMemberService.addMember(bandId, member);
        return Result.success("添加成功", memberId);
    }

    /**
     * 更新成员信息
     */
    @PutMapping("/{memberId}")
    public Result<Void> updateMember(
            @PathVariable("bandId") Long bandId,
            @PathVariable("memberId") Long memberId,
            @RequestBody @Valid Member member) {
        log.info("乐队更新成员信息: bandId={}, memberId={}", bandId, memberId);
        member.setMemberId(memberId);
        bandMemberService.updateMember(bandId, member);
        return Result.success("更新成功");
    }

    /**
     * 删除成员
     */
    @DeleteMapping("/{memberId}")
    public Result<Void> deleteMember(
            @PathVariable("bandId") Long bandId,
            @PathVariable("memberId") Long memberId) {
        log.info("乐队删除成员: bandId={}, memberId={}", bandId, memberId);
        bandMemberService.deleteMember(bandId, memberId);
        return Result.success("删除成功");
    }

    /**
     * 设置队长
     */
    @PutMapping("/{memberId}/leader")
    public Result<Void> setLeader(
            @PathVariable("bandId") Long bandId,
            @PathVariable("memberId") Long memberId) {
        log.info("乐队设置队长: bandId={}, memberId={}", bandId, memberId);
        bandMemberService.setLeader(bandId, memberId);
        return Result.success("设置队长成功");
    }

    /**
     * 查询成员详情
     */
    @GetMapping("/{memberId}")
    public Result<Member> getById(
            @PathVariable("bandId") Long bandId,
            @PathVariable("memberId") Long memberId) {
        Member member = bandMemberService.getById(bandId, memberId);
        return Result.success(member);
    }
}
