-- ============================================
-- 数据库安全性实施
-- ============================================

USE band_management;

-- ============================================
-- 1. 创建数据库用户
-- ============================================

-- 删除已存在的用户（如果有）
DROP USER IF EXISTS 'admin_user'@'localhost';
DROP USER IF EXISTS 'band_user'@'localhost';
DROP USER IF EXISTS 'fan_user'@'localhost';

-- 创建管理员用户（拥有所有权限）
CREATE USER 'admin_user'@'localhost' IDENTIFIED BY 'Admin@123456';
GRANT ALL PRIVILEGES ON band_management.* TO 'admin_user'@'localhost' WITH GRANT OPTION;

-- 创建乐队用户
CREATE USER 'band_user'@'localhost' IDENTIFIED BY 'Band@123456';

-- 创建歌迷用户
CREATE USER 'fan_user'@'localhost' IDENTIFIED BY 'Fan@123456';

-- 刷新权限
FLUSH PRIVILEGES;

-- ============================================
-- 2. 乐队用户权限设置
-- ============================================

-- 乐队用户可以维护：乐队信息、成员信息、专辑信息、歌曲信息、演唱会信息
GRANT SELECT, INSERT, UPDATE, DELETE ON band_management.Band TO 'band_user'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON band_management.Member TO 'band_user'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON band_management.Album TO 'band_user'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON band_management.Song TO 'band_user'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON band_management.Concert TO 'band_user'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON band_management.Performance TO 'band_user'@'localhost';

-- 乐队用户可以查看：歌迷信息、乐评信息、演唱会参加记录、歌迷喜好
GRANT SELECT ON band_management.Fan TO 'band_user'@'localhost';
GRANT SELECT ON band_management.AlbumReview TO 'band_user'@'localhost';
GRANT SELECT ON band_management.ConcertAttendance TO 'band_user'@'localhost';
GRANT SELECT ON band_management.FanFavoriteBand TO 'band_user'@'localhost';
GRANT SELECT ON band_management.FanFavoriteAlbum TO 'band_user'@'localhost';
GRANT SELECT ON band_management.FanFavoriteSong TO 'band_user'@'localhost';

-- 乐队用户可以查询 User 表（用于登录验证）
GRANT SELECT ON band_management.User TO 'band_user'@'localhost';

-- ============================================
-- 3. 歌迷用户权限设置
-- ============================================

-- 歌迷用户可以维护：歌迷信息、喜好信息、演唱会参加记录、乐评
GRANT SELECT, INSERT, UPDATE, DELETE ON band_management.Fan TO 'fan_user'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON band_management.FanFavoriteBand TO 'fan_user'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON band_management.FanFavoriteAlbum TO 'fan_user'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON band_management.FanFavoriteSong TO 'fan_user'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON band_management.ConcertAttendance TO 'fan_user'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON band_management.AlbumReview TO 'fan_user'@'localhost';

-- 歌迷用户可以查看：乐队信息、成员信息、专辑信息、歌曲信息、演唱会信息、专辑排行榜
GRANT SELECT ON band_management.Band TO 'fan_user'@'localhost';
GRANT SELECT ON band_management.Member TO 'fan_user'@'localhost';
GRANT SELECT ON band_management.Album TO 'fan_user'@'localhost';
GRANT SELECT ON band_management.Song TO 'fan_user'@'localhost';
GRANT SELECT ON band_management.Concert TO 'fan_user'@'localhost';
GRANT SELECT ON band_management.Performance TO 'fan_user'@'localhost';
GRANT SELECT ON band_management.AlbumRanking TO 'fan_user'@'localhost';

-- 歌迷用户可以查询 User 表（用于登录验证）
GRANT SELECT ON band_management.User TO 'fan_user'@'localhost';

-- 刷新权限
FLUSH PRIVILEGES;

-- ============================================
-- 4. 通用视图：所有乐队共用，通过 band_id 过滤
-- ============================================

-- 4.1 通用歌迷信息视图（包含 band_id）
CREATE OR REPLACE VIEW v_band_fans AS
SELECT DISTINCT 
    ffb.band_id,
    f.fan_id,
    f.name,
    f.gender,
    f.age,
    f.occupation,
    f.education
FROM Fan f
LEFT JOIN FanFavoriteBand ffb ON f.fan_id = ffb.fan_id
LEFT JOIN FanFavoriteAlbum ffa ON f.fan_id = ffa.fan_id
LEFT JOIN Album a1 ON ffa.album_id = a1.album_id
LEFT JOIN FanFavoriteSong ffs ON f.fan_id = ffs.fan_id
LEFT JOIN Song s ON ffs.song_id = s.song_id
LEFT JOIN Album a2 ON s.album_id = a2.album_id
LEFT JOIN ConcertAttendance ca ON f.fan_id = ca.fan_id
LEFT JOIN Concert c ON ca.concert_id = c.concert_id
LEFT JOIN AlbumReview ar ON f.fan_id = ar.fan_id
LEFT JOIN Album a3 ON ar.album_id = a3.album_id
WHERE ffb.band_id IS NOT NULL 
   OR a1.band_id IS NOT NULL 
   OR a2.band_id IS NOT NULL 
   OR c.band_id IS NOT NULL 
   OR a3.band_id IS NOT NULL;

-- 使用方式：
-- SELECT * FROM v_band_fans WHERE band_id = 1;  -- 查询逃跑计划的歌迷


-- 4.2 通用乐评信息视图
CREATE OR REPLACE VIEW v_band_reviews AS
SELECT 
    a.band_id,
    ar.review_id,
    ar.fan_id,
    f.name AS fan_name,
    a.album_id,
    a.title AS album_title,
    ar.rating,
    ar.comment,
    ar.reviewed_at
FROM AlbumReview ar
JOIN Fan f ON ar.fan_id = f.fan_id
JOIN Album a ON ar.album_id = a.album_id
ORDER BY ar.reviewed_at DESC;

-- 使用方式：
-- SELECT * FROM v_band_reviews WHERE band_id = 1;


-- 4.3 通用歌迷年龄段统计视图
CREATE OR REPLACE VIEW v_band_fan_age_stats AS
SELECT 
    band_id,
    CASE 
        WHEN age < 20 THEN '20岁以下'
        WHEN age BETWEEN 20 AND 25 THEN '20-25岁'
        WHEN age BETWEEN 26 AND 30 THEN '26-30岁'
        WHEN age BETWEEN 31 AND 35 THEN '31-35岁'
        ELSE '35岁以上'
    END AS age_range,
    COUNT(*) AS fan_count
FROM v_band_fans
GROUP BY band_id, age_range;

-- 使用方式：
-- SELECT * FROM v_band_fan_age_stats WHERE band_id = 1;


-- 4.4 通用歌迷性别统计视图
CREATE OR REPLACE VIEW v_band_fan_gender_stats AS
SELECT 
    band_id,
    CASE gender
        WHEN 'M' THEN '男'
        WHEN 'F' THEN '女'
        ELSE '其他'
    END AS gender_name,
    COUNT(*) AS fan_count
FROM v_band_fans
GROUP BY band_id, gender;


-- 4.5 通用歌迷职业统计视图
CREATE OR REPLACE VIEW v_band_fan_occupation_stats AS
SELECT 
    band_id,
    occupation,
    COUNT(*) AS fan_count
FROM v_band_fans
GROUP BY band_id, occupation
ORDER BY band_id, fan_count DESC;


-- 4.6 通用歌迷学历统计视图
CREATE OR REPLACE VIEW v_band_fan_education_stats AS
SELECT 
    band_id,
    education,
    COUNT(*) AS fan_count
FROM v_band_fans
GROUP BY band_id, education;


-- 4.7 通用最受欢迎歌曲统计视图
CREATE OR REPLACE VIEW v_band_popular_songs AS
SELECT 
    a.band_id,
    s.song_id,
    s.title AS song_title,
    a.title AS album_title,
    COUNT(DISTINCT ffs.fan_id) AS favorite_count
FROM Song s
JOIN Album a ON s.album_id = a.album_id
LEFT JOIN FanFavoriteSong ffs ON s.song_id = ffs.song_id
GROUP BY a.band_id, s.song_id, s.title, a.title
ORDER BY a.band_id, favorite_count DESC;


-- 4.8 通用演唱会参与统计视图
CREATE OR REPLACE VIEW v_band_popular_concerts AS
SELECT 
    c.band_id,
    c.concert_id,
    c.title AS concert_title,
    c.event_time,
    c.location,
    COUNT(DISTINCT ca.fan_id) AS attendance_count
FROM Concert c
LEFT JOIN ConcertAttendance ca ON c.concert_id = ca.concert_id
GROUP BY c.band_id, c.concert_id, c.title, c.event_time, c.location
ORDER BY c.band_id, attendance_count DESC;


-- 4.9 通用专辑评分统计视图
CREATE OR REPLACE VIEW v_band_album_ratings AS
SELECT 
    a.band_id,
    a.album_id,
    a.title AS album_title,
    a.release_date,
    COUNT(ar.review_id) AS review_count,
    ROUND(AVG(ar.rating), 1) AS avg_rating,
    MIN(ar.rating) AS min_rating,
    MAX(ar.rating) AS max_rating
FROM Album a
LEFT JOIN AlbumReview ar ON a.album_id = ar.album_id
GROUP BY a.band_id, a.album_id, a.title, a.release_date
ORDER BY a.band_id, avg_rating DESC;


-- 4.10 通用歌迷总数统计视图
CREATE OR REPLACE VIEW v_band_fan_count AS
SELECT 
    band_id,
    COUNT(DISTINCT fan_id) AS total_fans
FROM v_band_fans
GROUP BY band_id;


-- ============================================
-- 5. 授予乐队用户访问通用视图的权限
-- ============================================

GRANT SELECT ON band_management.v_band_fans TO 'band_user'@'localhost';
GRANT SELECT ON band_management.v_band_reviews TO 'band_user'@'localhost';
GRANT SELECT ON band_management.v_band_fan_count TO 'band_user'@'localhost';
GRANT SELECT ON band_management.v_band_fan_age_stats TO 'band_user'@'localhost';
GRANT SELECT ON band_management.v_band_fan_gender_stats TO 'band_user'@'localhost';
GRANT SELECT ON band_management.v_band_fan_occupation_stats TO 'band_user'@'localhost';
GRANT SELECT ON band_management.v_band_fan_education_stats TO 'band_user'@'localhost';
GRANT SELECT ON band_management.v_band_popular_songs TO 'band_user'@'localhost';
GRANT SELECT ON band_management.v_band_popular_concerts TO 'band_user'@'localhost';
GRANT SELECT ON band_management.v_band_album_ratings TO 'band_user'@'localhost';

FLUSH PRIVILEGES;

