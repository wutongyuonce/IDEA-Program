package com.band.management.service.impl;

import com.band.management.common.PageResult;
import com.band.management.config.DataSourceContextHolder;
import com.band.management.entity.Band;
import com.band.management.entity.Concert;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.BandMapper;
import com.band.management.mapper.ConcertAttendanceMapper;
import com.band.management.mapper.ConcertMapper;
import com.band.management.service.BandConcertService;
import com.band.management.util.StringUtil;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 乐队演唱会服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class BandConcertServiceImpl implements BandConcertService {

    @Autowired
    private ConcertMapper concertMapper;

    @Autowired
    private BandMapper bandMapper;

    @Autowired
    private ConcertAttendanceMapper concertAttendanceMapper;

    @Override
    public List<Concert> getMyBandConcerts(Long bandId) {
        log.info("乐队查询自己的演唱会列表: {}", bandId);

        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        return concertMapper.selectByBandId(bandId);
    }

    @Override
    public PageResult<Concert> page(Long bandId, int pageNum, int pageSize, Concert condition) {
        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        if (condition == null) {
            condition = new Concert();
        }
        condition.setBandId(bandId);

        PageHelper.startPage(pageNum, pageSize);
        List<Concert> list = concertMapper.selectByCondition(condition);
        return PageResult.of(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createConcert(Long bandId, Concert concert) {
        log.info("乐队发布演唱会: bandId={}, concertTitle={}", bandId, concert.getTitle());

        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        if (StringUtil.isEmpty(concert.getTitle())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "演唱会名称不能为空");
        }
        if (concert.getEventTime() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "演出时间不能为空");
        }
        if (StringUtil.isEmpty(concert.getLocation())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "演出地点不能为空");
        }

        concert.setBandId(bandId);
        int result = concertMapper.insert(concert);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "发布演唱会失败");
        }

        log.info("乐队发布演唱会成功，ID: {}", concert.getConcertId());
        return concert.getConcertId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConcert(Long bandId, Concert concert) {
        log.info("乐队更新演唱会信息: bandId={}, concertId={}", bandId, concert.getConcertId());

        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        Concert existConcert = concertMapper.selectById(concert.getConcertId());
        if (existConcert == null) {
            throw new BusinessException(ErrorCode.CONCERT_NOT_FOUND);
        }

        if (!existConcert.getBandId().equals(bandId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权操作其他乐队的演唱会");
        }

        int result = concertMapper.update(concert);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "更新演唱会信息失败");
        }

        log.info("乐队更新演唱会信息成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConcert(Long bandId, Long concertId) {
        log.info("乐队删除演唱会: bandId={}, concertId={}", bandId, concertId);

        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        Concert concert = concertMapper.selectById(concertId);
        if (concert == null) {
            throw new BusinessException(ErrorCode.CONCERT_NOT_FOUND);
        }

        if (!concert.getBandId().equals(bandId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权删除其他乐队的演唱会");
        }

        int result = concertMapper.deleteById(concertId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "删除演唱会失败");
        }

        log.info("乐队删除演唱会成功");
    }

    @Override
    public int getAttendanceCount(Long bandId, Long concertId) {
        log.info("乐队查询演唱会参与人数: bandId={}, concertId={}", bandId, concertId);

        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        Concert concert = concertMapper.selectById(concertId);
        if (concert == null) {
            throw new BusinessException(ErrorCode.CONCERT_NOT_FOUND);
        }

        if (!concert.getBandId().equals(bandId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权查看其他乐队的演唱会");
        }

        return concertAttendanceMapper.countByConcertId(concertId);
    }

    @Override
    public Concert getById(Long bandId, Long concertId) {
        DataSourceContextHolder.setDataSourceType("band");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        Concert concert = concertMapper.selectById(concertId);
        if (concert == null) {
            throw new BusinessException(ErrorCode.CONCERT_NOT_FOUND);
        }

        if (!concert.getBandId().equals(bandId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权查看其他乐队的演唱会");
        }

        return concert;
    }
}
