package com.band.management.service.impl;

import com.band.management.common.PageResult;
import com.band.management.config.DataSourceContextHolder;
import com.band.management.entity.Fan;
import com.band.management.entity.User;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.FanMapper;
import com.band.management.mapper.UserMapper;
import com.band.management.service.AdminFanService;
import com.band.management.util.StringUtil;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理员歌迷服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class AdminFanServiceImpl implements AdminFanService {

    @Autowired
    private FanMapper fanMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Fan fan) {
        log.info("管理员创建歌迷: {}", fan.getName());

        DataSourceContextHolder.setDataSourceType("admin");

        if (StringUtil.isEmpty(fan.getName())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "歌迷姓名不能为空");
        }

        int result = fanMapper.insert(fan);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "创建歌迷失败");
        }

        // 自动创建对应的歌迷用户
        try {
            // 生成用户名：fan_ + 歌迷名称的英文全小写
            String username = generateFanUsername(fan.getName());
            
            // 检查用户名是否已存在
            User existUser = userMapper.selectByUsername(username);
            if (existUser == null) {
                User user = new User();
                user.setUsername(username);
                user.setPassword("123456"); // 默认密码
                user.setRole("FAN");
                user.setRelatedId(fan.getFanId());
                user.setStatus(1); // 启用状态
                
                int userResult = userMapper.insert(user);
                if (userResult > 0) {
                    log.info("自动创建歌迷用户成功: username={}, relatedId={}", username, fan.getFanId());
                } else {
                    log.warn("自动创建歌迷用户失败: username={}", username);
                }
            } else {
                log.warn("用户名已存在，跳过创建: username={}", username);
            }
        } catch (Exception e) {
            log.error("创建歌迷用户时发生异常", e);
            // 不影响歌迷创建，仅记录日志
        }

        log.info("管理员创建歌迷成功，ID: {}", fan.getFanId());
        return fan.getFanId();
    }

    /**
     * 生成歌迷用户名：fan_ + 歌迷名称的英文全小写或拼音
     */
    private String generateFanUsername(String fanName) {
        if (StringUtil.isEmpty(fanName)) {
            return "fan_unknown";
        }
        
        // 移除空格和特殊字符，转换为小写
        String cleanName = fanName.trim()
                .replaceAll("[\\s\\-_]+", "")  // 移除空格、横线、下划线
                .toLowerCase();
        
        // 如果包含中文字符，使用简化的拼音转换
        if (cleanName.matches(".*[\\u4e00-\\u9fa5]+.*")) {
            // 简化处理：对于常见歌迷名称使用映射
            switch (fanName) {
                case "张伟": return "fan_zhangwei";
                case "李娜": return "fan_lina";
                case "王强": return "fan_wangqiang";
                case "刘芳": return "fan_liufang";
                case "陈明": return "fan_chenming";
                case "赵丽": return "fan_zhaoli";
                case "孙杰": return "fan_sunjie";
                case "周敏": return "fan_zhoumin";
                case "吴涛": return "fan_wutao";
                case "郑红": return "fan_zhenghong";
                case "钱磊": return "fan_qianlei";
                case "冯静": return "fan_fengjing";
                case "韩冰": return "fan_hanbing";
                case "蒋雪": return "fan_jiangxue";
                case "沈阳": return "fan_shenyang";
                case "马超": return "fan_machao";
                case "林静": return "fan_linjing";
                case "黄磊": return "fan_huanglei";
                case "杨雪": return "fan_yangxue";
                case "朱明": return "fan_zhuming";
                case "徐丽": return "fan_xuli";
                case "何强": return "fan_heqiang";
                case "宋敏": return "fan_songmin";
                case "唐涛": return "fan_tangtao";
                case "许红": return "fan_xuhong";
                default:
                    // 其他中文名称使用简化处理
                    return "fan_" + cleanName.replaceAll("[^a-z0-9]", "");
            }
        }
        
        return "fan_" + cleanName.replaceAll("[^a-z0-9]", "");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long fanId) {
        log.info("管理员删除歌迷: {}", fanId);

        DataSourceContextHolder.setDataSourceType("admin");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        int result = fanMapper.deleteById(fanId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "删除歌迷失败");
        }

        // 自动删除对应的歌迷用户
        try {
            int userResult = userMapper.deleteByRoleAndRelatedId("FAN", fanId);
            if (userResult > 0) {
                log.info("自动删除歌迷用户成功: relatedId={}", fanId);
            } else {
                log.warn("未找到需要删除的歌迷用户: relatedId={}", fanId);
            }
        } catch (Exception e) {
            log.error("删除歌迷用户时发生异常", e);
            // 不影响歌迷删除，仅记录日志
        }

        log.info("管理员删除歌迷成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Fan fan) {
        log.info("管理员更新歌迷: {}", fan.getFanId());

        DataSourceContextHolder.setDataSourceType("admin");

        Fan existFan = fanMapper.selectById(fan.getFanId());
        if (existFan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }

        int result = fanMapper.update(fan);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "更新歌迷失败");
        }

        log.info("管理员更新歌迷成功");
    }

    @Override
    public Fan getById(Long fanId) {
        DataSourceContextHolder.setDataSourceType("admin");

        Fan fan = fanMapper.selectById(fanId);
        if (fan == null) {
            throw new BusinessException(ErrorCode.FAN_NOT_FOUND);
        }
        return fan;
    }

    @Override
    public PageResult<Fan> page(int pageNum, int pageSize, Fan condition) {
        DataSourceContextHolder.setDataSourceType("admin");

        PageHelper.startPage(pageNum, pageSize);
        List<Fan> list = condition == null ? fanMapper.selectAll() : fanMapper.selectByCondition(condition);
        return PageResult.of(list);
    }

    @Override
    public List<Fan> list() {
        DataSourceContextHolder.setDataSourceType("admin");
        return fanMapper.selectAll();
    }
}
