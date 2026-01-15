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
import com.band.management.service.AdminConcertService;
import com.band.management.util.StringUtil;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理员演唱会服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class AdminConcertServiceImpl implements AdminConcertService {

    @Autowired
    private ConcertMapper concertMapper;

    @Autowired
    private BandMapper bandMapper;

    @Autowired
    private ConcertAttendanceMapper concertAttendanceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Concert concert) {
        log.info("管理员创建演唱会: {}", concert.getTitle());

        DataSourceContextHolder.setDataSourceType("admin");

        if (StringUtil.isEmpty(concert.getTitle())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "演唱会名称不能为空");
        }
        if (concert.getBandId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "乐队ID不能为空");
        }
        if (concert.getEventTime() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "演出时间不能为空");
        }

        Band band = bandMapper.selectById(concert.getBandId());
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        // 检查演出时间不能早于乐队成立日期
        java.sql.Date eventDate = new java.sql.Date(concert.getEventTime().getTime());
        if (eventDate.before(band.getFoundedAt())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                String.format("演唱会日期（%s）需要在乐队创建日期（%s）之后", 
                    eventDate, band.getFoundedAt()));
        }

        int result = concertMapper.insert(concert);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "创建演唱会失败");
        }

        log.info("管理员创建演唱会成功，ID: {}", concert.getConcertId());
        return concert.getConcertId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long concertId) {
        log.info("管理员删除演唱会: {}", concertId);

        DataSourceContextHolder.setDataSourceType("admin");

        Concert concert = concertMapper.selectById(concertId);
        if (concert == null) {
            throw new BusinessException(ErrorCode.CONCERT_NOT_FOUND);
        }

        int result = concertMapper.deleteById(concertId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "删除演唱会失败");
        }

        log.info("管理员删除演唱会成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Concert concert) {
        log.info("管理员更新演唱会: {}", concert.getConcertId());

        DataSourceContextHolder.setDataSourceType("admin");

        Concert existConcert = concertMapper.selectById(concert.getConcertId());
        if (existConcert == null) {
            throw new BusinessException(ErrorCode.CONCERT_NOT_FOUND);
        }

        if (concert.getBandId() != null && !concert.getBandId().equals(existConcert.getBandId())) {
            Band band = bandMapper.selectById(concert.getBandId());
            if (band == null) {
                throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
            }
        }

        // 检查演出时间不能早于乐队成立日期
        if (concert.getEventTime() != null) {
            Long targetBandId = concert.getBandId() != null ? concert.getBandId() : existConcert.getBandId();
            Band targetBand = bandMapper.selectById(targetBandId);
            if (targetBand != null) {
                java.sql.Date eventDate = new java.sql.Date(concert.getEventTime().getTime());
                if (eventDate.before(targetBand.getFoundedAt())) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                        String.format("演唱会日期（%s）需要在乐队创建日期（%s）之后", 
                            eventDate, targetBand.getFoundedAt()));
                }
            }
        }

        int result = concertMapper.update(concert);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "更新演唱会失败");
        }

        log.info("管理员更新演唱会成功");
    }

    @Override
    public Concert getById(Long concertId) {
        DataSourceContextHolder.setDataSourceType("admin");

        Concert concert = concertMapper.selectById(concertId);
        if (concert == null) {
            throw new BusinessException(ErrorCode.CONCERT_NOT_FOUND);
        }
        
        // 添加参与人数统计
        int attendanceCount = concertAttendanceMapper.countByConcertId(concertId);
        concert.setAttendanceCount(attendanceCount);
        
        return concert;
    }

    @Override
    public List<Concert> getByBandId(Long bandId) {
        DataSourceContextHolder.setDataSourceType("admin");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        List<Concert> concerts = concertMapper.selectByBandId(bandId);
        
        // 为每个演唱会添加参与人数统计
        if (concerts != null && !concerts.isEmpty()) {
            for (Concert concert : concerts) {
                int attendanceCount = concertAttendanceMapper.countByConcertId(concert.getConcertId());
                concert.setAttendanceCount(attendanceCount);
            }
        }
        
        return concerts;
    }

    @Override
    public PageResult<Concert> page(int pageNum, int pageSize, Concert condition) {
        DataSourceContextHolder.setDataSourceType("admin");

        PageHelper.startPage(pageNum, pageSize);
        List<Concert> list = condition == null ? concertMapper.selectAll() : concertMapper.selectByCondition(condition);
        
        // 为每个演唱会添加参与人数统计
        if (list != null && !list.isEmpty()) {
            for (Concert concert : list) {
                int attendanceCount = concertAttendanceMapper.countByConcertId(concert.getConcertId());
                concert.setAttendanceCount(attendanceCount);
            }
        }
        
        return PageResult.of(list);
    }

    @Override
    public List<Concert> list() {
        DataSourceContextHolder.setDataSourceType("admin");
        List<Concert> concerts = concertMapper.selectAll();
        
        // 为每个演唱会添加参与人数统计
        if (concerts != null && !concerts.isEmpty()) {
            for (Concert concert : concerts) {
                int attendanceCount = concertAttendanceMapper.countByConcertId(concert.getConcertId());
                concert.setAttendanceCount(attendanceCount);
            }
        }
        
        return concerts;
    }
}
