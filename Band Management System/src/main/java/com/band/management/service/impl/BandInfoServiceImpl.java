package com.band.management.service.impl;

import com.band.management.config.DataSourceContextHolder;
import com.band.management.entity.Band;
import com.band.management.entity.Member;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.BandMapper;
import com.band.management.mapper.MemberMapper;
import com.band.management.service.BandInfoService;
import com.band.management.util.StringUtil;
import com.band.management.vo.BandVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 乐队信息服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class BandInfoServiceImpl implements BandInfoService {

    @Autowired
    private BandMapper bandMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Override
    public Band getCurrentBandInfo(Long bandId) {
        log.info("乐队查询自己的信息: {}", bandId);

        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        return band;
    }

    @Override
    public BandVO getCurrentBandDetail(Long bandId) {
        log.info("乐队查询自己的详细信息: {}", bandId);

        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        List<Member> members = memberMapper.selectByBandId(bandId);

        BandVO bandVO = new BandVO();
        BeanUtils.copyProperties(band, bandVO);
        bandVO.setMembers(members);

        if (band.getLeaderMemberId() != null) {
            members.stream()
                    .filter(m -> m.getMemberId().equals(band.getLeaderMemberId()))
                    .findFirst()
                    .ifPresent(leader -> bandVO.setLeaderName(leader.getName()));
        }

        return bandVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBandInfo(Long bandId, Band band) {
        log.info("乐队更新自己的信息: {}", bandId);

        DataSourceContextHolder.setDataSourceType("band");

        Band existBand = bandMapper.selectById(bandId);
        if (existBand == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        if (StringUtil.isNotEmpty(band.getName()) && !band.getName().equals(existBand.getName())) {
            Band nameCheckBand = bandMapper.selectByName(band.getName());
            if (nameCheckBand != null) {
                throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS.getCode(), "乐队名称已存在");
            }
        }

        band.setBandId(bandId);
        int result = bandMapper.update(band);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "更新乐队信息失败");
        }

        log.info("乐队更新信息成功");
    }
}
