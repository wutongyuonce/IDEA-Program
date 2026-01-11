-- ============================================
-- 用户表创建和初始化
-- 乐队数据库系统 - 应用层用户管理
-- ============================================

USE band_management;

-- ============================================
-- 1. 创建用户表
-- ============================================

-- 用户表（应用层用户）
CREATE TABLE IF NOT EXISTS User (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（明文存储，实际应用中应加密）',
    role ENUM('ADMIN', 'BAND', 'FAN') NOT NULL COMMENT '用户角色',
    related_id BIGINT DEFAULT NULL COMMENT '关联ID：乐队ID或歌迷ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态：1-启用，0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_related_id (related_id)
) ENGINE=InnoDB COMMENT='应用层用户表';

-- ============================================
-- 2. 插入初始用户数据
-- ============================================

-- 插入管理员用户
INSERT INTO User (username, password, role, related_id, status) VALUES
('admin', '123456', 'ADMIN', NULL, 1);

-- 插入乐队用户（关联到Band表）
-- 注意：related_id对应Band表的band_id
INSERT INTO User (username, password, role, related_id, status) VALUES
('band_escape', '123456', 'BAND', 1, 1),        -- 逃跑计划
('band_mayday', '123456', 'BAND', 2, 1),        -- 五月天
('band_newpants', '123456', 'BAND', 3, 1),      -- 新裤子
('band_tongyang', '123456', 'BAND', 4, 1),      -- 痛仰
('band_hedgehog', '123456', 'BAND', 5, 1),      -- 刺猬
('band_coldplay', '123456', 'BAND', 6, 1),      -- Coldplay
('band_linkinpark', '123456', 'BAND', 7, 1),    -- Linkin Park
('band_imagine', '123456', 'BAND', 8, 1),       -- Imagine Dragons
('band_maroon5', '123456', 'BAND', 9, 1),       -- Maroon 5
('band_onerepublic', '123456', 'BAND', 10, 1);  -- OneRepublic

-- 插入歌迷用户（关联到Fan表）
-- 注意：related_id对应Fan表的fan_id
INSERT INTO User (username, password, role, related_id, status) VALUES
('fan_zhangwei', '123456', 'FAN', 1, 1),        -- 张伟
('fan_lina', '123456', 'FAN', 2, 1),            -- 李娜
('fan_wangqiang', '123456', 'FAN', 3, 1),       -- 王强
('fan_liufang', '123456', 'FAN', 4, 1),         -- 刘芳
('fan_chenming', '123456', 'FAN', 5, 1),        -- 陈明
('fan_zhaoli', '123456', 'FAN', 6, 1),          -- 赵丽
('fan_sunjie', '123456', 'FAN', 7, 1),          -- 孙杰
('fan_zhoumin', '123456', 'FAN', 8, 1),         -- 周敏
('fan_wutao', '123456', 'FAN', 9, 1),           -- 吴涛
('fan_zhenghong', '123456', 'FAN', 10, 1),      -- 郑红
('fan_qianlei', '123456', 'FAN', 11, 1),        -- 钱磊
('fan_fengjing', '123456', 'FAN', 12, 1),       -- 冯静
('fan_hanbing', '123456', 'FAN', 13, 1),        -- 韩冰
('fan_jiangxue', '123456', 'FAN', 14, 1),       -- 蒋雪
('fan_shenyang', '123456', 'FAN', 15, 1),       -- 沈阳
('fan_machao', '123456', 'FAN', 16, 1),         -- 马超
('fan_linjing', '123456', 'FAN', 17, 1),        -- 林静
('fan_huanglei', '123456', 'FAN', 18, 1),       -- 黄磊
('fan_yangxue', '123456', 'FAN', 19, 1),        -- 杨雪
('fan_zhuming', '123456', 'FAN', 20, 1),        -- 朱明
('fan_xuli', '123456', 'FAN', 21, 1),           -- 徐丽
('fan_heqiang', '123456', 'FAN', 22, 1),        -- 何强
('fan_songmin', '123456', 'FAN', 23, 1),        -- 宋敏
('fan_tangtao', '123456', 'FAN', 24, 1),        -- 唐涛
('fan_xuhong', '123456', 'FAN', 25, 1);         -- 许红

-- ============================================
-- 3. 验证数据
-- ============================================

SELECT '=== 用户表创建完成 ===' AS status;
SELECT '管理员用户数量：', COUNT(*) FROM User WHERE role = 'ADMIN';
SELECT '乐队用户数量：', COUNT(*) FROM User WHERE role = 'BAND';
SELECT '歌迷用户数量：', COUNT(*) FROM User WHERE role = 'FAN';
SELECT '总用户数量：', COUNT(*) FROM User;

-- 显示所有用户概览
SELECT 
    user_id,
    username,
    role,
    related_id,
    CASE 
        WHEN role = 'ADMIN' THEN '管理员'
        WHEN role = 'BAND' THEN CONCAT('乐队: ', (SELECT name FROM Band WHERE band_id = related_id))
        WHEN role = 'FAN' THEN CONCAT('歌迷: ', (SELECT name FROM Fan WHERE fan_id = related_id))
    END AS description,
    status,
    created_at
FROM User
ORDER BY role, user_id;

-- ============================================
-- 4. 使用说明
-- ============================================

/*
用户登录说明：
1. 所有用户的默认密码都是：123456
2. 用户类型：
   - ADMIN：管理员用户，可以管理所有数据
   - BAND：乐队用户，只能管理自己乐队的数据
   - FAN：歌迷用户，可以浏览、收藏、评论

3. 测试账号示例：
   管理员：
   - 用户名：admin，密码：123456
   
   乐队用户：
   - 用户名：band_escape，密码：123456（逃跑计划）
   - 用户名：band_mayday，密码：123456（五月天）
   - 用户名：band_coldplay，密码：123456（Coldplay）
   
   歌迷用户：
   - 用户名：fan_zhangwei，密码：123456（张伟）
   - 用户名：fan_lina，密码：123456（李娜）

4. 注意事项：
   - 密码采用明文存储（仅用于开发测试）
   - 实际生产环境应使用BCrypt等加密算法
   - related_id字段关联到Band表或Fan表的主键
*/
