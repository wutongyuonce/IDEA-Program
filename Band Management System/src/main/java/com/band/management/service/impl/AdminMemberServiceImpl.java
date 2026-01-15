package com.band.management.service.impl;

import com.band.management.common.PageResult;
import com.band.management.config.DataSourceContextHolder;
import com.band.management.entity.Band;
import com.band.management.entity.Member;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.BandMapper;
import com.band.management.mapper.MemberMapper;
import com.band.management.service.AdminMemberService;
import com.band.management.util.StringUtil;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理员成员服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class AdminMemberServiceImpl implements AdminMemberService {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private BandMapper bandMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Member member) {
        log.info("管理员创建成员: {}", member.getName());

        // 设置管理员数据源
        DataSourceContextHolder.setDataSourceType("admin");

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
        if (member.getBirthDate() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "出生日期不能为空");
        }
        if (StringUtil.isEmpty(member.getRole())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "分工不能为空");
        }
        if (member.getJoinDate() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "加入时间不能为空");
        }

        // 检查乐队是否存在
        Band band = bandMapper.selectById(member.getBandId());
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        // 如果乐队已解散，必须填写离队日期，且离队日期不能晚于解散日期
        if ("Y".equals(band.getIsDisbanded())) {
            if (member.getLeaveDate() == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                    "该乐队已解散，添加成员时必须填写离队日期");
            }
            
            if (band.getDisbandedAt() != null) {
                // 将java.util.Date转换为java.sql.Date进行比较
                java.sql.Date disbandedSqlDate = new java.sql.Date(band.getDisbandedAt().getTime());
                if (member.getLeaveDate().after(disbandedSqlDate)) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                        String.format("该乐队已于 %s 解散，成员离队日期不能晚于解散日期", band.getDisbandedAt()));
                }
            }
        }

        // 检查日期逻辑
        if (member.getLeaveDate() != null && member.getLeaveDate().before(member.getJoinDate())) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }

        // 检查加入日期不能早于乐队成立日期
        if (member.getJoinDate().before(band.getFoundedAt())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                String.format("成员加入日期（%s）需要在乐队创建日期（%s）之后", 
                    member.getJoinDate(), band.getFoundedAt()));
        }

        // 插入数据
        int result = memberMapper.insert(member);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "创建成员失败");
        }

        log.info("管理员创建成员成功，ID: {}", member.getMemberId());
        return member.getMemberId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long memberId) {
        log.info("管理员删除成员: {}", memberId);

        // 设置管理员数据源
        DataSourceContextHolder.setDataSourceType("admin");

        // 检查是否存在
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        // 检查该成员是否是某个乐队的队长，如果是则清除队长引用
        Band band = bandMapper.selectById(member.getBandId());
        if (band != null && band.getLeaderMemberId() != null && band.getLeaderMemberId().equals(memberId)) {
            log.info("成员是队长，清除队长引用");
            band.setLeaderMemberId(null);
            bandMapper.update(band);
        }

        // 删除数据
        int result = memberMapper.deleteById(memberId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "删除成员失败");
        }

        log.info("管理员删除成员成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Member member) {
        log.info("管理员更新成员: {}", member.getMemberId());

        // 设置管理员数据源
        DataSourceContextHolder.setDataSourceType("admin");

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

        // 检查日期逻辑
        if (member.getLeaveDate() != null && member.getJoinDate() != null 
                && member.getLeaveDate().before(member.getJoinDate())) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }

        // 检查加入日期不能早于乐队成立日期
        if (member.getJoinDate() != null) {
            Long targetBandId = member.getBandId() != null ? member.getBandId() : existMember.getBandId();
            Band targetBand = bandMapper.selectById(targetBandId);
            if (targetBand != null && member.getJoinDate().before(targetBand.getFoundedAt())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                    String.format("成员加入日期（%s）需要在乐队创建日期（%s）之后", 
                        member.getJoinDate(), targetBand.getFoundedAt()));
            }
        }

        // 更新数据
        int result = memberMapper.update(member);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "更新成员失败");
        }

        log.info("管理员更新成员成功");
    }

    @Override
    public Member getById(Long memberId) {
        // 设置管理员数据源
        DataSourceContextHolder.setDataSourceType("admin");

        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }
        return member;
    }

    @Override
    public List<Member> getByBandId(Long bandId) {
        // 设置管理员数据源
        DataSourceContextHolder.setDataSourceType("admin");

        // 检查乐队是否存在
        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        return memberMapper.selectByBandId(bandId);
    }

    @Override
    public PageResult<Member> page(int pageNum, int pageSize, Member condition) {
        // 设置管理员数据源
        DataSourceContextHolder.setDataSourceType("admin");

        PageHelper.startPage(pageNum, pageSize);
        List<Member> list = condition == null ? memberMapper.selectAll() : memberMapper.selectByCondition(condition);
        return PageResult.of(list);
    }

    @Override
    public List<Member> list() {
        // 设置管理员数据源
        DataSourceContextHolder.setDataSourceType("admin");

        return memberMapper.selectAll();
    }
}
