package com.band.management.service.impl;

import com.band.management.common.PageResult;
import com.band.management.entity.Band;
import com.band.management.entity.Member;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.BandMapper;
import com.band.management.mapper.MemberMapper;
import com.band.management.service.MemberService;
import com.band.management.util.StringUtil;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 成员服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private BandMapper bandMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Member member) {
        log.info("创建成员: {}", member.getName());

        // 参数校验
        if (StringUtil.isEmpty(member.getName())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "成员姓名不能为空");
        }
        if (member.getBandId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "乐队ID不能为空");
        }
        if (StringUtil.isEmpty(member.getGender())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "性别不能为空");
        }
        if (member.getJoinDate() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "加入时间不能为空");
        }

        // 业务校验：检查乐队是否存在
        Band band = bandMapper.selectById(member.getBandId());
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        // 业务校验：如果有离开时间，检查是否大于等于加入时间
        if (member.getLeaveDate() != null && member.getLeaveDate().before(member.getJoinDate())) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }

        // 插入数据
        int result = memberMapper.insert(member);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "创建成员失败");
        }

        log.info("成员创建成功，ID: {}", member.getMemberId());
        return member.getMemberId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long memberId) {
        log.info("删除成员: {}", memberId);

        // 检查是否存在
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        // 删除数据
        int result = memberMapper.deleteById(memberId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "删除成员失败");
        }

        log.info("成员删除成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Member member) {
        log.info("更新成员: {}", member.getMemberId());

        // 检查是否存在
        Member existMember = memberMapper.selectById(member.getMemberId());
        if (existMember == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        // 如果修改了乐队ID，检查新乐队是否存在
        if (member.getBandId() != null && !member.getBandId().equals(existMember.getBandId())) {
            Band band = bandMapper.selectById(member.getBandId());
            if (band == null) {
                throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
            }
        }

        // 业务校验：如果有离开时间，检查是否大于等于加入时间
        if (member.getLeaveDate() != null) {
            java.util.Date joinDate = member.getJoinDate() != null ? member.getJoinDate() : existMember.getJoinDate();
            if (member.getLeaveDate().before(joinDate)) {
                throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
            }
        }

        // 更新数据
        int result = memberMapper.update(member);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "更新成员失败");
        }

        log.info("成员更新成功");
    }

    @Override
    public Member getById(Long memberId) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }
        return member;
    }

    @Override
    public List<Member> getByBandId(Long bandId) {
        if (bandId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "乐队ID不能为空");
        }
        return memberMapper.selectByBandId(bandId);
    }

    @Override
    public PageResult<Member> page(int pageNum, int pageSize, Member condition) {
        PageHelper.startPage(pageNum, pageSize);
        List<Member> list = condition == null ? memberMapper.selectAll() : memberMapper.selectByCondition(condition);
        return PageResult.of(list);
    }

    @Override
    public List<Member> list() {
        return memberMapper.selectAll();
    }
}
