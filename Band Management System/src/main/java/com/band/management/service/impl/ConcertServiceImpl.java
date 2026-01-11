package com.band.management.service.impl;

import com.band.management.common.PageResult;
import com.band.management.entity.Band;
import com.band.management.entity.Concert;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.BandMapper;
import com.band.management.mapper.ConcertMapper;
import com.band.management.service.ConcertService;
import com.band.management.util.StringUtil;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 演唱会服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class ConcertServiceImpl implements ConcertService {

    @Autowired
    private ConcertMapper concertMapper;

    @Autowired
    private BandMapper bandMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Concert concert) {
        log.info("创建演唱会: {}", concert.getTitle());

        // 参数校验
        if (StringUtil.isEmpty(concert.getTitle())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "演唱会标题不能为空");
        }
        if (concert.getBandId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "乐队ID不能为空");
        }
        if (concert.getEventTime() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "演出时间不能为空");
        }
        if (StringUtil.isEmpty(concert.getLocation())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "演出地点不能为空");
        }

        // 业务校验：检查乐队是否存在
        Band band = bandMapper.selectById(concert.getBandId());
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        // 插入数据
        int result = concertMapper.insert(concert);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "创建演唱会失败");
        }

        log.info("演唱会创建成功，ID: {}", concert.getConcertId());
        return concert.getConcertId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long concertId) {
        log.info("删除演唱会: {}", concertId);

        // 检查是否存在
        Concert concert = concertMapper.selectById(concertId);
        if (concert == null) {
            throw new BusinessException(ErrorCode.CONCERT_NOT_FOUND);
        }

        // 删除数据（级联删除由数据库外键处理）
        int result = concertMapper.deleteById(concertId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "删除演唱会失败");
        }

        log.info("演唱会删除成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Concert concert) {
        log.info("更新演唱会: {}", concert.getConcertId());

        // 检查是否存在
        Concert existConcert = concertMapper.selectById(concert.getConcertId());
        if (existConcert == null) {
            throw new BusinessException(ErrorCode.CONCERT_NOT_FOUND);
        }

        // 如果修改了乐队ID，检查新乐队是否存在
        if (concert.getBandId() != null && !concert.getBandId().equals(existConcert.getBandId())) {
            Band band = bandMapper.selectById(concert.getBandId());
            if (band == null) {
                throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
            }
        }

        // 更新数据
        int result = concertMapper.update(concert);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "更新演唱会失败");
        }

        log.info("演唱会更新成功");
    }

    @Override
    public Concert getById(Long concertId) {
        Concert concert = concertMapper.selectById(concertId);
        if (concert == null) {
            throw new BusinessException(ErrorCode.CONCERT_NOT_FOUND);
        }
        return concert;
    }

    @Override
    public List<Concert> getByBandId(Long bandId) {
        if (bandId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "乐队ID不能为空");
        }
        return concertMapper.selectByBandId(bandId);
    }

    @Override
    public PageResult<Concert> page(int pageNum, int pageSize, Concert condition) {
        PageHelper.startPage(pageNum, pageSize);
        List<Concert> list = condition == null ? concertMapper.selectAll() : concertMapper.selectByCondition(condition);
        return PageResult.of(list);
    }

    @Override
    public List<Concert> list() {
        return concertMapper.selectAll();
    }
}
