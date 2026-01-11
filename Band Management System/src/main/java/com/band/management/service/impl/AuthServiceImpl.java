package com.band.management.service.impl;

import com.band.management.config.DataSourceContextHolder;
import com.band.management.dto.LoginRequest;
import com.band.management.entity.User;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.UserMapper;
import com.band.management.service.AuthService;
import com.band.management.util.EncryptUtil;
import com.band.management.util.StringUtil;
import com.band.management.vo.LoginResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;

/**
 * 认证服务实现类
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private com.band.management.mapper.BandMapper bandMapper;

    @Autowired
    private com.band.management.mapper.FanMapper fanMapper;

    private static final String SESSION_USER_KEY = "current_user";

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("用户登录: username={}, role={}", loginRequest.getUsername(), loginRequest.getRole());

        // 参数校验
        if (StringUtil.isEmpty(loginRequest.getUsername())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "用户名不能为空");
        }
        if (StringUtil.isEmpty(loginRequest.getPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "密码不能为空");
        }
        if (StringUtil.isEmpty(loginRequest.getRole())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "角色不能为空");
        }

        // 验证角色是否合法
        String role = loginRequest.getRole().toUpperCase();
        if (!role.equals("ADMIN") && !role.equals("BAND") && !role.equals("FAN")) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "角色类型不正确");
        }

        // 根据用户角色设置数据源
        // 注意：band_user 和 fan_user 现在也有 User 表的 SELECT 权限
        DataSourceContextHolder.setDataSourceType(role);

        // 查询用户
        User user = userMapper.selectByUsernameAndRole(loginRequest.getUsername(), role);
        if (user == null) {
            log.error("用户不存在: username={}, role={}", loginRequest.getUsername(), role);
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        
        log.debug("找到用户: userId={}, username={}, role={}, passwordLength={}", 
                  user.getUserId(), user.getUsername(), user.getRole(), 
                  user.getPassword() != null ? user.getPassword().length() : 0);

        // 验证密码
        // 先尝试加密验证（兼容旧的加密密码）
        boolean passwordMatches = EncryptUtil.matchesPassword(loginRequest.getPassword(), user.getPassword());
        
        // 如果加密验证失败，尝试明文比较（兼容新的明文密码）
        if (!passwordMatches) {
            passwordMatches = loginRequest.getPassword().equals(user.getPassword());
        }
        
        log.debug("密码验证结果: {}, 输入密码长度: {}, 存储密码长度: {}", 
                  passwordMatches, loginRequest.getPassword().length(), 
                  user.getPassword() != null ? user.getPassword().length() : 0);
        
        if (!passwordMatches) {
            log.error("密码不匹配: username={}", loginRequest.getUsername());
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        // 检查账号状态
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        // 保存用户信息到Session
        HttpSession session = getSession();
        session.setAttribute(SESSION_USER_KEY, user);
        session.setAttribute("user_role", role);

        log.info("用户登录成功: userId={}, role={}", user.getUserId(), role);

        // 构建响应
        return LoginResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .role(role)
                .relatedId(user.getRelatedId())
                .build();
    }

    @Override
    public void logout() {
        log.info("用户登出");
        HttpSession session = getSession();
        session.removeAttribute(SESSION_USER_KEY);
        session.removeAttribute("user_role");
        DataSourceContextHolder.clearDataSourceType();
    }

    @Override
    public User getCurrentUser() {
        HttpSession session = getSession();
        User user = (User) session.getAttribute(SESSION_USER_KEY);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(User user) {
        log.info("注册新用户: username={}, role={}", user.getUsername(), user.getRole());

        // 参数校验
        if (StringUtil.isEmpty(user.getUsername())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "用户名不能为空");
        }
        if (StringUtil.isEmpty(user.getPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "密码不能为空");
        }
        if (StringUtil.isEmpty(user.getRole())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "角色不能为空");
        }

        // 注册操作必须使用 admin 数据源，因为只有 admin_user 有 INSERT 权限
        DataSourceContextHolder.setDataSourceType("ADMIN");

        // 检查用户名是否已存在
        User existUser = userMapper.selectByUsernameAndRole(user.getUsername(), user.getRole());
        if (existUser != null) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS.getCode(), "用户名已存在");
        }

        // 加密密码
        user.setPassword(EncryptUtil.encryptPassword(user.getPassword()));

        // 设置默认状态
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        // 插入数据
        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "注册失败");
        }

        log.info("用户注册成功: userId={}", user.getUserId());
        return user.getUserId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("修改密码: userId={}", userId);

        // 参数校验
        if (StringUtil.isEmpty(oldPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "原密码不能为空");
        }
        if (StringUtil.isEmpty(newPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "新密码不能为空");
        }
        if (newPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "新密码长度至少6位");
        }

        // 修改密码需要使用 ADMIN 数据源，因为只有 admin_user 有 UPDATE 权限
        DataSourceContextHolder.setDataSourceType("ADMIN");

        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND.getCode(), "用户不存在");
        }

        // 验证原密码
        if (!EncryptUtil.matchesPassword(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "原密码不正确");
        }

        // 直接使用新密码，不加密
        user.setPassword(newPassword);
        int result = userMapper.update(user);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "密码修改失败");
        }

        log.info("密码修改成功: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public com.band.management.vo.RegisterResponse registerBand(String name, String foundedAt, String intro, String password) {
        log.info("乐队用户注册: name={}", name);

        // 参数校验
        if (StringUtil.isEmpty(name)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "乐队名称不能为空");
        }
        if (StringUtil.isEmpty(password)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "密码不能为空");
        }
        if (password.length() < 6) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "密码长度至少6位");
        }

        // 自动生成username: band_名称拼音
        String username = generateBandUsername(name);

        // 使用 ADMIN 数据源进行注册
        DataSourceContextHolder.setDataSourceType("ADMIN");

        // 检查用户名是否已存在
        User existingUser = userMapper.selectByUsername(username);
        if (existingUser != null) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS.getCode(), "用户名已存在，请使用不同的乐队名称");
        }

        // 创建乐队记录
        com.band.management.entity.Band band = new com.band.management.entity.Band();
        band.setName(name);
        band.setFoundedAt(java.sql.Date.valueOf(foundedAt));
        band.setIntro(intro);
        band.setMemberCount(0);
        
        int bandResult = bandMapper.insert(band);
        if (bandResult <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "乐队创建失败");
        }

        // 创建用户记录
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // 明文存储
        user.setRole("BAND");
        user.setRelatedId(band.getBandId());
        user.setStatus(1); // 设置状态为启用
        
        int userResult = userMapper.insert(user);
        if (userResult <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "用户创建失败");
        }

        log.info("乐队用户注册成功: bandId={}, userId={}, username={}", band.getBandId(), user.getUserId(), username);
        
        // 返回注册响应
        return com.band.management.vo.RegisterResponse.builder()
                .userId(user.getUserId())
                .username(username)
                .relatedId(band.getBandId())
                .role("BAND")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public com.band.management.vo.RegisterResponse registerFan(String name, String gender, Integer age, String occupation, String education, String password) {
        log.info("歌迷用户注册: name={}", name);

        // 参数校验
        if (StringUtil.isEmpty(name)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "姓名不能为空");
        }
        if (StringUtil.isEmpty(password)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "密码不能为空");
        }
        if (password.length() < 6) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "密码长度至少6位");
        }

        // 自动生成username: fan_姓名拼音
        String username = generateFanUsername(name);

        // 使用 ADMIN 数据源进行注册
        DataSourceContextHolder.setDataSourceType("ADMIN");

        // 检查用户名是否已存在
        User existingUser = userMapper.selectByUsername(username);
        if (existingUser != null) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS.getCode(), "用户名已存在，请使用不同的姓名");
        }

        // 创建歌迷记录
        com.band.management.entity.Fan fan = new com.band.management.entity.Fan();
        fan.setName(name);
        fan.setGender(gender);
        fan.setAge(age);
        fan.setOccupation(occupation);
        fan.setEducation(education);
        
        int fanResult = fanMapper.insert(fan);
        if (fanResult <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "歌迷创建失败");
        }

        // 创建用户记录
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // 明文存储
        user.setRole("FAN");
        user.setRelatedId(fan.getFanId());
        user.setStatus(1); // 设置状态为启用
        
        int userResult = userMapper.insert(user);
        if (userResult <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "用户创建失败");
        }

        log.info("歌迷用户注册成功: fanId={}, userId={}, username={}", fan.getFanId(), user.getUserId(), username);
        
        // 返回注册响应
        return com.band.management.vo.RegisterResponse.builder()
                .userId(user.getUserId())
                .username(username)
                .relatedId(fan.getFanId())
                .role("FAN")
                .build();
    }

    /**
     * 生成乐队用户名: band_名称拼音
     */
    private String generateBandUsername(String name) {
        String pinyin = convertToPinyin(name);
        return "band_" + pinyin;
    }

    /**
     * 生成歌迷用户名: fan_姓名拼音
     */
    private String generateFanUsername(String name) {
        String pinyin = convertToPinyin(name);
        return "fan_" + pinyin;
    }

    /**
     * 中文转拼音（使用 pinyin4j 库）
     * 中文字符转换为拼音，英文和数字保持不变，其他字符移除
     */
    private String convertToPinyin(String text) {
        if (StringUtil.isEmpty(text)) {
            return "";
        }
        return com.band.management.util.PinyinUtil.toPinyin(text);
    }

    /**
     * 获取当前HTTP Session
     */
    private HttpSession getSession() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "无法获取Session");
        }
        return attributes.getRequest().getSession();
    }
}
