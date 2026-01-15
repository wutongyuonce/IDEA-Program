package com.band.management.service.impl;

import com.band.management.common.PageResult;
import com.band.management.config.DataSourceContextHolder;
import com.band.management.entity.Band;
import com.band.management.entity.Member;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.BandMapper;
import com.band.management.mapper.MemberMapper;
import com.band.management.service.BandMemberService;
import com.band.management.util.StringUtil;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 乐队成员服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class BandMemberServiceImpl implements BandMemberService {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private BandMapper bandMapper;

    @Override
    public List<Member> getMyBandMembers(Long bandId) {
        log.info("乐队查询自己的成员列表: {}", bandId);

        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        return memberMapper.selectByBandId(bandId);
    }

    @Override
    public PageResult<Member> page(Long bandId, int pageNum, int pageSize, Member condition) {
        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        if (condition == null) {
            condition = new Member();
        }
        condition.setBandId(bandId);

        PageHelper.startPage(pageNum, pageSize);
        List<Member> list = memberMapper.selectByCondition(condition);
        return PageResult.of(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addMember(Long bandId, Member member) {
        log.info("乐队添加成员: bandId={}, memberName={}", bandId, member.getName());

        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        // 检查乐队是否已解散
        if ("Y".equals(band.getIsDisbanded())) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "该乐队已解散，无法添加成员");
        }

        if (StringUtil.isEmpty(member.getName())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "成员姓名不能为空");
        }
        if (StringUtil.isEmpty(member.getRole())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "成员角色不能为空");
        }
        if (member.getJoinDate() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "加入时间不能为空");
        }

        if (member.getJoinDate().after(new Date())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "加入时间不能晚于当前时间");
        }

        // 检查加入日期不能早于乐队成立日期
        if (member.getJoinDate().before(band.getFoundedAt())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                String.format("成员加入日期（%s）需要在乐队创建日期（%s）之后", 
                    member.getJoinDate(), band.getFoundedAt()));
        }

        member.setBandId(bandId);
        int result = memberMapper.insert(member);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "添加成员失败");
        }

        log.info("乐队添加成员成功，ID: {}", member.getMemberId());
        return member.getMemberId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMember(Long bandId, Member member) {
        log.info("乐队更新成员信息: bandId={}, memberId={}", bandId, member.getMemberId());

        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        Member existMember = memberMapper.selectById(member.getMemberId());
        if (existMember == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        if (!existMember.getBandId().equals(bandId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权操作其他乐队的成员");
        }

        if (member.getJoinDate() != null && member.getJoinDate().after(new Date())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "加入时间不能晚于当前时间");
        }

        // 检查加入日期不能早于乐队成立日期
        if (member.getJoinDate() != null && member.getJoinDate().before(band.getFoundedAt())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                String.format("成员加入日期（%s）需要在乐队创建日期（%s）之后", 
                    member.getJoinDate(), band.getFoundedAt()));
        }

        if (member.getLeaveDate() != null) {
            if (member.getJoinDate() != null && member.getLeaveDate().before(member.getJoinDate())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "离开时间不能早于加入时间");
            }
            if (existMember.getJoinDate() != null && member.getLeaveDate().before(existMember.getJoinDate())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "离开时间不能早于加入时间");
            }
        }

        int result = memberMapper.update(member);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "更新成员信息失败");
        }

        log.info("乐队更新成员信息成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMember(Long bandId, Long memberId) {
        log.info("乐队删除成员: bandId={}, memberId={}", bandId, memberId);

        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        if (!member.getBandId().equals(bandId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权删除其他乐队的成员");
        }

        int result = memberMapper.deleteById(memberId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "删除成员失败");
        }

        log.info("乐队删除成员成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setLeader(Long bandId, Long memberId) {
        log.info("乐队设置队长: bandId={}, memberId={}", bandId, memberId);

        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        if (!member.getBandId().equals(bandId)) {
            throw new BusinessException(ErrorCode.LEADER_NOT_IN_BAND);
        }

        band.setLeaderMemberId(memberId);
        int result = bandMapper.update(band);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "设置队长失败");
        }

        log.info("乐队设置队长成功");
    }

    @Override
    public Member getById(Long bandId, Long memberId) {
        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        if (!member.getBandId().equals(bandId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权查看其他乐队的成员");
        }

        return member;
    }
}
