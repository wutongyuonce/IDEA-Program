package com.band.management.service.impl;

import com.band.management.common.PageResult;
import com.band.management.config.DataSourceContextHolder;
import com.band.management.entity.Band;
import com.band.management.entity.Member;
import com.band.management.entity.User;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.BandMapper;
import com.band.management.mapper.MemberMapper;
import com.band.management.mapper.UserMapper;
import com.band.management.service.AdminBandService;
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
 * 管理员乐队服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class AdminBandServiceImpl implements AdminBandService {

    @Autowired
    private BandMapper bandMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Band band) {
        log.info("管理员创建乐队: {}", band.getName());

        // 设置管理员数据源
        DataSourceContextHolder.setDataSourceType("admin");

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

        // 自动创建对应的乐队用户
        try {
            // 生成用户名：band_ + 乐队名称的英文全小写
            String username = generateBandUsername(band.getName());
            
            // 检查用户名是否已存在
            User existUser = userMapper.selectByUsername(username);
            if (existUser == null) {
                User user = new User();
                user.setUsername(username);
                user.setPassword("123456"); // 默认密码
                user.setRole("BAND");
                user.setRelatedId(band.getBandId());
                user.setStatus(1); // 启用状态
                
                int userResult = userMapper.insert(user);
                if (userResult > 0) {
                    log.info("自动创建乐队用户成功: username={}, relatedId={}", username, band.getBandId());
                } else {
                    log.warn("自动创建乐队用户失败: username={}", username);
                }
            } else {
                log.warn("用户名已存在，跳过创建: username={}", username);
            }
        } catch (Exception e) {
            log.error("创建乐队用户时发生异常", e);
            // 不影响乐队创建，仅记录日志
        }

        log.info("管理员创建乐队成功，ID: {}", band.getBandId());
        return band.getBandId();
    }

    /**
     * 生成乐队用户名：band_ + 乐队名称的英文全小写
     * 如果是中文名称，转换为拼音；如果是英文名称，直接转小写
     */
    private String generateBandUsername(String bandName) {
        if (StringUtil.isEmpty(bandName)) {
            return "band_unknown";
        }
        
        // 移除空格和特殊字符，转换为小写
        String cleanName = bandName.trim()
                .replaceAll("[\\s\\-_]+", "")  // 移除空格、横线、下划线
                .toLowerCase();
        
        // 如果包含中文字符，使用简化的拼音转换（这里简化处理，实际应使用拼音库）
        if (cleanName.matches(".*[\\u4e00-\\u9fa5]+.*")) {
            // 简化处理：对于常见乐队名称使用映射
            switch (bandName) {
                case "逃跑计划": return "band_escape";
                case "五月天": return "band_mayday";
                case "新裤子": return "band_newpants";
                case "痛仰": return "band_tongyang";
                case "刺猬": return "band_hedgehog";
                default:
                    // 其他中文名称使用ID作为后缀（需要在调用处传入ID）
                    return "band_" + cleanName.replaceAll("[^a-z0-9]", "");
            }
        }
        
        return "band_" + cleanName.replaceAll("[^a-z0-9]", "");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long bandId) {
        log.info("管理员删除乐队: {}", bandId);

        // 设置管理员数据源
        DataSourceContextHolder.setDataSourceType("admin");

        // 检查是否存在
        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        // 检查是否有成员，如果有成员则不允许删除
        List<Member> members = memberMapper.selectByBandId(bandId);
        if (members != null && !members.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "请删除所有成员后再操作");
        }

        // 先清除队长引用，避免外键约束问题
        if (band.getLeaderMemberId() != null) {
            band.setLeaderMemberId(null);
            bandMapper.update(band);
        }

        // 删除乐队（其他关联数据如专辑、演唱会等会被级联删除）
        int result = bandMapper.deleteById(bandId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "删除乐队失败");
        }

        // 自动删除对应的乐队用户
        try {
            int userResult = userMapper.deleteByRoleAndRelatedId("BAND", bandId);
            if (userResult > 0) {
                log.info("自动删除乐队用户成功: relatedId={}", bandId);
            } else {
                log.warn("未找到需要删除的乐队用户: relatedId={}", bandId);
            }
        } catch (Exception e) {
            log.error("删除乐队用户时发生异常", e);
            // 不影响乐队删除，仅记录日志
        }

        log.info("管理员删除乐队成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Band band) {
        log.info("管理员更新乐队: {}", band.getBandId());

        // 设置管理员数据源
        DataSourceContextHolder.setDataSourceType("admin");

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

        log.info("管理员更新乐队成功");
    }

    @Override
    public Band getById(Long bandId) {
        // 设置管理员数据源
        DataSourceContextHolder.setDataSourceType("admin");

        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }
        return band;
    }

    @Override
    public BandVO getDetailById(Long bandId) {
        // 设置管理员数据源
        DataSourceContextHolder.setDataSourceType("admin");

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
        // 设置管理员数据源
        DataSourceContextHolder.setDataSourceType("admin");

        PageHelper.startPage(pageNum, pageSize);
        List<Band> list = condition == null ? bandMapper.selectAll() : bandMapper.selectByCondition(condition);
        return PageResult.of(list);
    }

    @Override
    public List<Band> list() {
        // 设置管理员数据源
        DataSourceContextHolder.setDataSourceType("admin");

        return bandMapper.selectAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setLeader(Long bandId, Long memberId) {
        log.info("管理员设置乐队队长: bandId={}, memberId={}", bandId, memberId);

        // 设置管理员数据源
        DataSourceContextHolder.setDataSourceType("admin");

        // 检查乐队是否存在
        Band band = bandMapper.selectById(bandId);
        if (band == null) {
            throw new BusinessException(ErrorCode.BAND_NOT_FOUND);
        }

        // 检查成员是否存在且属于该乐队
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }
        if (!member.getBandId().equals(bandId)) {
            throw new BusinessException(ErrorCode.LEADER_NOT_IN_BAND);
        }

        // 更新队长
        band.setLeaderMemberId(memberId);
        int result = bandMapper.update(band);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "设置队长失败");
        }

        log.info("管理员设置队长成功");
    }
}
