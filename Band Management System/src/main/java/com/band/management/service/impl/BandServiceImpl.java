package com.band.management.service.impl;

import com.band.management.common.PageResult;
import com.band.management.entity.Band;
import com.band.management.entity.Member;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.BandMapper;
import com.band.management.mapper.MemberMapper;
import com.band.management.service.BandService;
import com.band.management.util.StringUtil;
import com.band.management.vo.BandVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 乐队服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class BandServiceImpl implements BandService {

    @Autowired
    private BandMapper bandMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Band band) {
        log.info("创建乐队: {}", band.getName());

        // 参数校验
        if (StringUtil.isEmpty(band.getName())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "乐队名称不能为空");
        }
        if (band.getFoundedAt() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "成立时间不能为空");
        }

        // 检查名称是否已存在
        Band existBand = bandMapper.selectByName(band.getName());
        if (existBand != null) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS.getCode(), "乐队名称已存在");
        }

        // 插入数据
        int result = bandMapper.insert(band);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "创建乐队失败");
        }

        log.info("乐队创建成功，ID: {}", band.getBandId());
        return band.getBandId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long bandId) {
        log.info("删除乐队: {}", bandId);

        // 检查是否存在
        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        // 删除数据（级联删除由数据库外键处理）
        int result = bandMapper.deleteById(bandId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "删除乐队失败");
        }

        log.info("乐队删除成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Band band) {
        log.info("更新乐队: {}", band.getBandId());

        // 检查是否存在
        Band existBand = bandMapper.selectById(band.getBandId());
        if (existBand == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        // 如果修改了名称，检查新名称是否已被使用
        if (StringUtil.isNotEmpty(band.getName()) && !band.getName().equals(existBand.getName())) {
            Band nameCheckBand = bandMapper.selectByName(band.getName());
            if (nameCheckBand != null) {
                throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS.getCode(), "乐队名称已存在");
            }
        }

        // 更新数据
        int result = bandMapper.update(band);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "更新乐队失败");
        }

        log.info("乐队更新成功");
    }

    @Override
    public Band getById(Long bandId) {
        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }
        return band;
    }

    @Override
    public BandVO getDetailById(Long bandId) {
        // 查询乐队基本信息
        Band band = getById(bandId);

        // 查询成员列表
        List<Member> members = memberMapper.selectByBandId(bandId);

        // 组装VO
        BandVO bandVO = new BandVO();
        BeanUtils.copyProperties(band, bandVO);
        bandVO.setMembers(members);

        // 查找队长姓名
        if (band.getLeaderMemberId() != null) {
            members.stream()
                    .filter(m -> m.getMemberId().equals(band.getLeaderMemberId()))
                    .findFirst()
                    .ifPresent(leader -> bandVO.setLeaderName(leader.getName()));
        }

        return bandVO;
    }

    @Override
    public PageResult<Band> page(int pageNum, int pageSize, Band condition) {
        PageHelper.startPage(pageNum, pageSize);
        List<Band> list = condition == null ? bandMapper.selectAll() : bandMapper.selectByCondition(condition);
        return PageResult.of(list);
    }

    @Override
    public List<Band> list() {
        return bandMapper.selectAll();
    }

    @Override
    public Band getByName(String name) {
        if (StringUtil.isEmpty(name)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "乐队名称不能为空");
        }
        return bandMapper.selectByName(name);
    }
}
