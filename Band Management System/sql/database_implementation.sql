-- ============================================
-- 任务5：数据库实施
-- 乐队数据库系统 - 完整实现
-- ============================================

-- 设置客户端字符集
SET NAMES utf8mb4;
SET CHARACTER_SET_CLIENT = utf8mb4;
SET CHARACTER_SET_CONNECTION = utf8mb4;
SET CHARACTER_SET_RESULTS = utf8mb4;

-- 创建数据库
DROP DATABASE IF EXISTS band_management;
CREATE DATABASE band_management 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_general_ci;

USE band_management;

-- 再次确认字符集
SET NAMES utf8mb4;

-- ============================================
-- 1. 创建基础表结构
-- ============================================

-- 乐队表
CREATE TABLE Band (
    band_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    founded_at DATE NOT NULL,
    intro TEXT,
    leader_member_id BIGINT DEFAULT NULL,
    member_count INT NOT NULL DEFAULT 0,
    is_disbanded ENUM('N', 'Y') NOT NULL DEFAULT 'N' COMMENT '是否已解散：N-未解散，Y-已解散',
    disbanded_at DATE NULL COMMENT '解散日期',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_founded_at (founded_at),
    INDEX idx_is_disbanded (is_disbanded)
) ENGINE=InnoDB COMMENT='乐队信息表';

-- 成员表
CREATE TABLE Member (
    member_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    person_id BIGINT COMMENT '人员ID，用于标识同一个人的多条成员记录',
    band_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    gender ENUM('M', 'F', 'O') NOT NULL,
    birth_date DATE NOT NULL,
    role VARCHAR(50) NOT NULL COMMENT '乐队分工：主唱/吉他手/鼓手等',
    join_date DATE NOT NULL,
    leave_date DATE NULL COMMENT '离队日期，NULL表示仍在队中',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_member_band FOREIGN KEY (band_id) REFERENCES Band(band_id) 
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT chk_member_dates CHECK (leave_date IS NULL OR leave_date >= join_date),
    INDEX idx_band_id (band_id),
    INDEX idx_join_leave (join_date, leave_date),
    INDEX idx_person_dates (person_id, join_date, leave_date)
) ENGINE=InnoDB COMMENT='乐队成员表';

-- 添加队长外键约束
ALTER TABLE Band 
ADD CONSTRAINT fk_band_leader 
FOREIGN KEY (leader_member_id) REFERENCES Member(member_id) 
ON UPDATE RESTRICT ON DELETE RESTRICT;

-- 专辑表
CREATE TABLE Album (
    album_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    band_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    release_date DATE NOT NULL,
    copywriting TEXT COMMENT '专辑文案',
    avg_score DECIMAL(3,1) DEFAULT NULL COMMENT '平均评分',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_album_band FOREIGN KEY (band_id) REFERENCES Band(band_id) 
        ON UPDATE RESTRICT ON DELETE CASCADE,
    UNIQUE KEY uk_band_title (band_id, title),
    INDEX idx_band_id (band_id),
    INDEX idx_release_date (release_date),
    INDEX idx_avg_score (avg_score)
) ENGINE=InnoDB COMMENT='专辑表';

-- 歌曲表
CREATE TABLE Song (
    song_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    album_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    lyricist VARCHAR(100) COMMENT '词作者',
    composer VARCHAR(100) COMMENT '曲作者',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_song_album FOREIGN KEY (album_id) REFERENCES Album(album_id) 
        ON UPDATE RESTRICT ON DELETE CASCADE,
    UNIQUE KEY uk_album_title (album_id, title),
    INDEX idx_album_id (album_id)
) ENGINE=InnoDB COMMENT='歌曲表';

-- 演唱会表
CREATE TABLE Concert (
    concert_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    band_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    event_time DATETIME NOT NULL,
    location VARCHAR(200) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_concert_band FOREIGN KEY (band_id) REFERENCES Band(band_id) 
        ON UPDATE RESTRICT ON DELETE CASCADE,
    UNIQUE KEY uk_band_concert (band_id, title, event_time),
    INDEX idx_band_id (band_id),
    INDEX idx_event_time (event_time)
) ENGINE=InnoDB COMMENT='演唱会表';

-- 歌迷表
CREATE TABLE Fan (
    fan_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    gender ENUM('M', 'F', 'O') NOT NULL,
    age INT NOT NULL,
    occupation VARCHAR(50),
    education VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_fan_age CHECK (age BETWEEN 0 AND 120),
    INDEX idx_age (age),
    INDEX idx_occupation (occupation),
    INDEX idx_education (education)
) ENGINE=InnoDB COMMENT='歌迷表';

-- 乐队演唱歌曲关系表
CREATE TABLE Performance (
    perf_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    band_id BIGINT NOT NULL COMMENT '演唱乐队',
    song_id BIGINT NOT NULL COMMENT '演唱歌曲',
    source_band_id BIGINT DEFAULT NULL COMMENT '原创乐队，NULL表示翻唱或未知',
    note TEXT COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_perf_band FOREIGN KEY (band_id) REFERENCES Band(band_id) 
        ON UPDATE RESTRICT ON DELETE CASCADE,
    CONSTRAINT fk_perf_song FOREIGN KEY (song_id) REFERENCES Song(song_id) 
        ON UPDATE RESTRICT ON DELETE CASCADE,
    CONSTRAINT fk_perf_source FOREIGN KEY (source_band_id) REFERENCES Band(band_id) 
        ON UPDATE RESTRICT ON DELETE SET NULL,
    UNIQUE KEY uk_band_song (band_id, song_id),
    INDEX idx_band_id (band_id),
    INDEX idx_song_id (song_id)
) ENGINE=InnoDB COMMENT='乐队演唱歌曲关系表';

-- 专辑乐评表
CREATE TABLE AlbumReview (
    review_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fan_id BIGINT NOT NULL,
    album_id BIGINT NOT NULL,
    rating DECIMAL(3,1) NOT NULL COMMENT '评分1-10，步长0.5',
    comment TEXT,
    reviewed_at DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_fan FOREIGN KEY (fan_id) REFERENCES Fan(fan_id) 
        ON UPDATE RESTRICT ON DELETE CASCADE,
    CONSTRAINT fk_review_album FOREIGN KEY (album_id) REFERENCES Album(album_id) 
        ON UPDATE RESTRICT ON DELETE CASCADE,
    CONSTRAINT chk_rating_range CHECK (rating BETWEEN 1 AND 10),
    CONSTRAINT chk_rating_step CHECK (rating * 2 = FLOOR(rating * 2)),
    UNIQUE KEY uk_fan_album (fan_id, album_id),
    INDEX idx_fan_id (fan_id),
    INDEX idx_album_id (album_id),
    INDEX idx_reviewed_at (reviewed_at)
) ENGINE=InnoDB COMMENT='专辑乐评表';

-- 歌迷参加演唱会表
CREATE TABLE ConcertAttendance (
    attend_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fan_id BIGINT NOT NULL,
    concert_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attend_fan FOREIGN KEY (fan_id) REFERENCES Fan(fan_id) 
        ON UPDATE RESTRICT ON DELETE CASCADE,
    CONSTRAINT fk_attend_concert FOREIGN KEY (concert_id) REFERENCES Concert(concert_id) 
        ON UPDATE RESTRICT ON DELETE CASCADE,
    UNIQUE KEY uk_fan_concert (fan_id, concert_id),
    INDEX idx_fan_id (fan_id),
    INDEX idx_concert_id (concert_id)
) ENGINE=InnoDB COMMENT='歌迷参加演唱会表';

-- 歌迷喜欢的乐队表
CREATE TABLE FanFavoriteBand (
    fan_id BIGINT NOT NULL,
    band_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (fan_id, band_id),
    CONSTRAINT fk_favband_fan FOREIGN KEY (fan_id) REFERENCES Fan(fan_id) 
        ON UPDATE RESTRICT ON DELETE CASCADE,
    CONSTRAINT fk_favband_band FOREIGN KEY (band_id) REFERENCES Band(band_id) 
        ON UPDATE RESTRICT ON DELETE CASCADE,
    INDEX idx_band_id (band_id)
) ENGINE=InnoDB COMMENT='歌迷喜欢的乐队表';

-- 歌迷喜欢的专辑表
CREATE TABLE FanFavoriteAlbum (
    fan_id BIGINT NOT NULL,
    album_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (fan_id, album_id),
    CONSTRAINT fk_favalbum_fan FOREIGN KEY (fan_id) REFERENCES Fan(fan_id) 
        ON UPDATE RESTRICT ON DELETE CASCADE,
    CONSTRAINT fk_favalbum_album FOREIGN KEY (album_id) REFERENCES Album(album_id) 
        ON UPDATE RESTRICT ON DELETE CASCADE,
    INDEX idx_album_id (album_id)
) ENGINE=InnoDB COMMENT='歌迷喜欢的专辑表';

-- 歌迷喜欢的歌曲表
CREATE TABLE FanFavoriteSong (
    fan_id BIGINT NOT NULL,
    song_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (fan_id, song_id),
    CONSTRAINT fk_favsong_fan FOREIGN KEY (fan_id) REFERENCES Fan(fan_id) 
        ON UPDATE RESTRICT ON DELETE CASCADE,
    CONSTRAINT fk_favsong_song FOREIGN KEY (song_id) REFERENCES Song(song_id) 
        ON UPDATE RESTRICT ON DELETE CASCADE,
    INDEX idx_song_id (song_id)
) ENGINE=InnoDB COMMENT='歌迷喜欢的歌曲表';


-- ============================================
-- 2. 初始化数据加载
-- ============================================

-- 插入乐队数据（先不设置队长）
INSERT INTO Band (name, founded_at, intro, member_count) VALUES
('逃跑计划', '2007-06-15', '中国内地流行摇滚乐队，以《夜空中最亮的星》闻名', 0),
('五月天', '1997-03-29', '台湾摇滚乐团，华语乐坛代表性乐队', 0),
('新裤子', '1995-10-01', '中国摇滚乐队，风格多元化', 0),
('痛仰', '1999-05-01', '中国摇滚乐队，以硬核朋克风格著称', 0),
('刺猬', '2005-08-01', '中国独立摇滚乐队，噪音摇滚风格', 0),
('Coldplay', '1996-09-01', '英国摇滚乐队，以抒情摇滚风格著称', 0),
('Linkin Park', '1996-01-01', '美国摇滚乐队，融合摇滚和电子音乐', 0),
('Imagine Dragons', '2008-01-01', '美国流行摇滚乐队', 0),
('Maroon 5', '1994-01-01', '美国流行摇滚乐队', 0),
('OneRepublic', '2002-01-01', '美国流行摇滚乐队', 0);

-- 插入成员数据
INSERT INTO Member (person_id, band_id, name, gender, birth_date, role, join_date, leave_date) VALUES
-- 逃跑计划成员
(1, 1, '毛川', 'M', '1982-05-12', '主唱/吉他', '2007-06-15', NULL),
(2, 1, '马晓东', 'M', '1983-08-20', '吉他', '2007-06-15', NULL),
(3, 1, '红桃', 'M', '1984-03-15', '贝斯', '2007-06-15', NULL),
(4, 1, '刚昂', 'M', '1985-11-08', '鼓手', '2007-06-15', NULL),

-- 五月天成员
(5, 2, '阿信', 'M', '1975-12-06', '主唱', '1997-03-29', NULL),
(6, 2, '怪兽', 'M', '1976-11-28', '吉他', '1997-03-29', NULL),
(7, 2, '石头', 'M', '1975-12-11', '吉他', '1997-03-29', NULL),
(8, 2, '玛莎', 'M', '1977-04-25', '贝斯', '1997-03-29', NULL),
(9, 2, '冠佑', 'M', '1973-07-28', '鼓手', '1997-03-29', NULL),

-- 新裤子成员
(10, 3, '彭磊', 'M', '1974-06-11', '主唱/吉他', '1995-10-01', NULL),
(11, 3, '庞宽', 'M', '1975-09-18', '贝斯', '1995-10-01', NULL),
(12, 3, '赵梦', 'M', '1988-02-14', '鼓手', '2009-01-01', NULL),

-- 痛仰成员
(13, 4, '高虎', 'M', '1977-10-20', '主唱/吉他', '1999-05-01', NULL),
(14, 4, '刘韧', 'M', '1978-03-15', '吉他', '1999-05-01', NULL),
(15, 4, '张静', 'M', '1979-07-22', '贝斯', '1999-05-01', NULL),
(16, 4, '于磊', 'M', '1980-01-10', '鼓手', '1999-05-01', NULL),

-- 刺猬成员
(17, 5, '赵子健', 'M', '1986-04-18', '主唱/吉他', '2005-08-01', NULL),
(18, 5, '石璐', 'F', '1987-09-25', '贝斯/和声', '2005-08-01', NULL),
(19, 5, '何一帆', 'M', '1988-12-03', '鼓手', '2005-08-01', NULL),

-- Coldplay成员
(20, 6, 'Chris Martin', 'M', '1977-03-02', '主唱/键盘', '1996-09-01', NULL),
(21, 6, 'Jonny Buckland', 'M', '1977-09-11', '吉他手', '1996-09-01', NULL),
(22, 6, 'Guy Berryman', 'M', '1978-04-12', '贝斯手', '1996-09-01', NULL),
(23, 6, 'Will Champion', 'M', '1978-07-31', '鼓手', '1996-09-01', NULL),

-- Linkin Park成员
(24, 7, 'Chester Bennington', 'M', '1976-03-20', '主唱', '1996-01-01', '2017-07-20'),
(25, 7, 'Mike Shinoda', 'M', '1977-02-11', '主唱/说唱', '1996-01-01', NULL),
(26, 7, 'Brad Delson', 'M', '1977-12-01', '吉他手', '1996-01-01', NULL),
(27, 7, 'Rob Bourdon', 'M', '1979-01-20', '鼓手', '1996-01-01', NULL),

-- Imagine Dragons成员
(28, 8, 'Dan Reynolds', 'M', '1987-07-14', '主唱', '2008-01-01', NULL),
(29, 8, 'Wayne Sermon', 'M', '1984-06-15', '吉他手', '2008-01-01', NULL),
(30, 8, 'Ben McKee', 'M', '1985-04-07', '贝斯手', '2008-01-01', NULL),
(31, 8, 'Daniel Platzman', 'M', '1986-09-28', '鼓手', '2008-01-01', NULL),

-- Maroon 5成员
(32, 9, 'Adam Levine', 'M', '1979-03-18', '主唱', '1994-01-01', NULL),
(33, 9, 'Jesse Carmichael', 'M', '1979-04-02', '键盘手', '1994-01-01', NULL),
(34, 9, 'Mickey Madden', 'M', '1979-05-13', '贝斯手', '1994-01-01', NULL),
(35, 9, 'James Valentine', 'M', '1978-10-05', '吉他手', '2001-01-01', NULL),

-- OneRepublic成员
(36, 10, 'Ryan Tedder', 'M', '1979-06-26', '主唱/键盘', '2002-01-01', NULL),
(37, 10, 'Zach Filkins', 'M', '1978-09-15', '吉他手', '2002-01-01', NULL),
(38, 10, 'Drew Brown', 'M', '1984-01-20', '吉他手', '2002-01-01', NULL),
(39, 10, 'Brent Kutzle', 'M', '1985-08-03', '贝斯手', '2007-01-01', NULL);

-- 更新乐队队长
UPDATE Band SET leader_member_id = 1 WHERE band_id = 1;  -- 逃跑计划：毛川
UPDATE Band SET leader_member_id = 5 WHERE band_id = 2;  -- 五月天：阿信
UPDATE Band SET leader_member_id = 10 WHERE band_id = 3; -- 新裤子：彭磊
UPDATE Band SET leader_member_id = 13 WHERE band_id = 4; -- 痛仰：高虎
UPDATE Band SET leader_member_id = 17 WHERE band_id = 5; -- 刺猬：赵子健
UPDATE Band SET leader_member_id = 20 WHERE band_id = 6; -- Coldplay：Chris Martin
UPDATE Band SET leader_member_id = 25 WHERE band_id = 7; -- Linkin Park：Mike Shinoda
UPDATE Band SET leader_member_id = 28 WHERE band_id = 8; -- Imagine Dragons：Dan Reynolds
UPDATE Band SET leader_member_id = 32 WHERE band_id = 9; -- Maroon 5：Adam Levine
UPDATE Band SET leader_member_id = 36 WHERE band_id = 10; -- OneRepublic：Ryan Tedder

-- 插入专辑数据
INSERT INTO Album (band_id, title, release_date, copywriting) VALUES
-- 逃跑计划专辑
(1, '世界', '2011-12-01', '首张专辑，收录《夜空中最亮的星》等经典曲目'),
(1, '逃跑计划', '2014-06-20', '同名专辑，展现乐队成熟风格'),
(1, '化身孤岛的鲸', '2017-11-10', '第三张专辑，探索更多音乐可能性'),

-- 五月天专辑
(2, '第二人生', '2011-12-16', '第八张专辑，探讨人生第二次机会'),
(2, '自传', '2016-07-21', '第九张专辑，回顾乐队20年历程'),
(2, '步步', '2022-12-30', '第十张专辑，记录疫情时代的思考'),

-- 新裤子专辑
(3, '龙虎人丹', '2018-01-12', '复古电子摇滚风格专辑'),
(3, 'BYOB', '2020-11-06', '充满创意和实验性的作品'),

-- 痛仰专辑
(4, '不要停止我的音乐', '2005-09-01', '早期代表作'),
(4, '愿爱无忧', '2017-05-19', '成熟期作品，展现人文关怀'),

-- 刺猬专辑
(5, '甜蜜与杀害', '2009-10-01', '首张专辑，奠定噪音摇滚风格'),
(5, '火车驶向云外，梦安魂于九霄', '2014-12-05', '第二张专辑，更加成熟的创作'),

-- Coldplay专辑
(6, 'Parachutes', '2000-07-10', 'Coldplay首张专辑，包含经典曲目Yellow'),
(6, 'A Rush of Blood to the Head', '2002-08-26', '获得格莱美奖的专辑，奠定乐队地位'),
(6, 'X&Y', '2005-06-06', '第三张录音室专辑，探索更宏大的音乐风格'),

-- Linkin Park专辑
(7, 'Hybrid Theory', '2000-10-24', 'Linkin Park首张专辑，全球销量超3000万'),
(7, 'Meteora', '2003-03-25', '第二张录音室专辑，延续新金属风格'),
(7, 'Minutes to Midnight', '2007-05-14', '音乐风格转型之作，更加成熟'),

-- Imagine Dragons专辑
(8, 'Night Visions', '2012-09-04', '包含热门单曲Radioactive的首张专辑'),
(8, 'Smoke + Mirrors', '2015-02-17', '第二张录音室专辑，音乐更加多元'),

-- Maroon 5专辑
(9, 'Songs About Jane', '2002-06-25', 'Maroon 5首张专辑，流行摇滚经典'),
(9, 'It Won\'t Be Soon Before Long', '2007-05-16', '第二张录音室专辑，商业成功'),

-- OneRepublic专辑
(10, 'Dreaming Out Loud', '2007-11-20', 'OneRepublic首张专辑，包含热门单曲Apologize'),
(10, 'Waking Up', '2009-11-17', '第二张录音室专辑，延续流行摇滚风格');

-- 插入歌曲数据
INSERT INTO Song (album_id, title, lyricist, composer) VALUES
-- 逃跑计划《世界》专辑
(1, '夜空中最亮的星', '逃跑计划', '逃跑计划'),
(1, '一万次悲伤', '逃跑计划', '逃跑计划'),
(1, '再见再见', '逃跑计划', '逃跑计划'),

-- 逃跑计划《逃跑计划》专辑
(2, '是你吗', '逃跑计划', '逃跑计划'),
(2, '叫醒我', '逃跑计划', '逃跑计划'),

-- 逃跑计划《化身孤岛的鲸》专辑
(3, '化身孤岛的鲸', '逃跑计划', '逃跑计划'),
(3, '一首歌', '逃跑计划', '逃跑计划'),

-- 五月天《第二人生》专辑
(4, '诺亚方舟', '阿信', '怪兽'),
(4, '第二人生', '阿信', '怪兽'),
(4, '你不是真正的快乐', '阿信', '阿信'),

-- 五月天《自传》专辑
(5, '成名在望', '阿信', '怪兽'),
(5, '好好', '阿信', '怪兽'),
(5, '人生海海', '阿信', '阿信'),

-- 五月天《步步》专辑
(6, '好不好', '阿信', '怪兽'),
(6, '少年他的奇幻漂流', '阿信', '怪兽'),

-- 新裤子《龙虎人丹》专辑
(7, '你要跳舞吗', '彭磊', '彭磊'),
(7, 'Do You Wanna Dance', '彭磊', '彭磊'),
(7, '生命因你而火热', '彭磊', '彭磊'),

-- 新裤子《BYOB》专辑
(8, 'BYOB', '彭磊', '彭磊'),
(8, '海浪', '彭磊', '彭磊'),

-- 痛仰《不要停止我的音乐》专辑
(9, '公路之歌', '高虎', '痛仰'),
(9, '再见杰克', '高虎', '痛仰'),

-- 痛仰《愿爱无忧》专辑
(10, '愿爱无忧', '高虎', '痛仰'),
(10, '西湖', '高虎', '痛仰'),

-- 刺猬《甜蜜与杀害》专辑
(11, '火车驶向云外', '赵子健', '刺猬'),
(11, '生之响往', '赵子健', '刺猬'),

-- 刺猬《火车驶向云外，梦安魂于九霄》专辑
(12, '白日梦蓝', '赵子健', '刺猬'),
(12, '29', '赵子健', '刺猬'),

-- Coldplay《Parachutes》专辑
(13, 'Don\'t Panic', 'Coldplay', 'Coldplay'),
(13, 'Shiver', 'Coldplay', 'Coldplay'),
(13, 'Spies', 'Coldplay', 'Coldplay'),
(13, 'Sparks', 'Coldplay', 'Coldplay'),
(13, 'Yellow', 'Coldplay', 'Coldplay'),

-- Coldplay《A Rush of Blood to the Head》专辑
(14, 'Politik', 'Coldplay', 'Coldplay'),
(14, 'In My Place', 'Coldplay', 'Coldplay'),
(14, 'The Scientist', 'Coldplay', 'Coldplay'),
(14, 'Clocks', 'Coldplay', 'Coldplay'),

-- Coldplay《X&Y》专辑
(15, 'Square One', 'Coldplay', 'Coldplay'),
(15, 'What If', 'Coldplay', 'Coldplay'),
(15, 'Fix You', 'Coldplay', 'Coldplay'),

-- Linkin Park《Hybrid Theory》专辑
(16, 'Papercut', 'Linkin Park', 'Linkin Park'),
(16, 'One Step Closer', 'Linkin Park', 'Linkin Park'),
(16, 'With You', 'Linkin Park', 'Linkin Park'),
(16, 'Points of Authority', 'Linkin Park', 'Linkin Park'),
(16, 'Crawling', 'Linkin Park', 'Linkin Park'),
(16, 'In the End', 'Linkin Park', 'Linkin Park'),

-- Linkin Park《Meteora》专辑
(17, 'Foreword', 'Linkin Park', 'Linkin Park'),
(17, 'Don\'t Stay', 'Linkin Park', 'Linkin Park'),
(17, 'Somewhere I Belong', 'Linkin Park', 'Linkin Park'),
(17, 'Numb', 'Linkin Park', 'Linkin Park'),

-- Linkin Park《Minutes to Midnight》专辑
(18, 'Wake', 'Linkin Park', 'Linkin Park'),
(18, 'Given Up', 'Linkin Park', 'Linkin Park'),
(18, 'Leave Out All the Rest', 'Linkin Park', 'Linkin Park'),
(18, 'Bleed It Out', 'Linkin Park', 'Linkin Park'),

-- Imagine Dragons《Night Visions》专辑
(19, 'Radioactive', 'Imagine Dragons', 'Imagine Dragons'),
(19, 'Tiptoe', 'Imagine Dragons', 'Imagine Dragons'),
(19, 'It\'s Time', 'Imagine Dragons', 'Imagine Dragons'),
(19, 'Demons', 'Imagine Dragons', 'Imagine Dragons'),

-- Imagine Dragons《Smoke + Mirrors》专辑
(20, 'Shots', 'Imagine Dragons', 'Imagine Dragons'),
(20, 'Gold', 'Imagine Dragons', 'Imagine Dragons'),
(20, 'Smoke and Mirrors', 'Imagine Dragons', 'Imagine Dragons'),

-- Maroon 5《Songs About Jane》专辑
(21, 'Harder to Breathe', 'Maroon 5', 'Maroon 5'),
(21, 'This Love', 'Maroon 5', 'Maroon 5'),
(21, 'She Will Be Loved', 'Maroon 5', 'Maroon 5'),
(21, 'Sunday Morning', 'Maroon 5', 'Maroon 5'),

-- Maroon 5《It Won't Be Soon Before Long》专辑
(22, 'If I Never See Your Face Again', 'Maroon 5', 'Maroon 5'),
(22, 'Makes Me Wonder', 'Maroon 5', 'Maroon 5'),
(22, 'Little of Your Time', 'Maroon 5', 'Maroon 5'),

-- OneRepublic《Dreaming Out Loud》专辑
(23, 'Say (All I Need)', 'OneRepublic', 'OneRepublic'),
(23, 'Mercy', 'OneRepublic', 'OneRepublic'),
(23, 'Stop and Stare', 'OneRepublic', 'OneRepublic'),
(23, 'Apologize', 'OneRepublic', 'OneRepublic'),

-- OneRepublic《Waking Up》专辑
(24, 'Made for You', 'OneRepublic', 'OneRepublic'),
(24, 'All the Right Moves', 'OneRepublic', 'OneRepublic'),
(24, 'Secrets', 'OneRepublic', 'OneRepublic');

-- 插入演唱会数据
INSERT INTO Concert (band_id, title, event_time, location) VALUES
(1, '逃跑计划2023巡回演唱会-北京站', '2023-05-20 19:30:00', '北京工人体育场'),
(1, '逃跑计划2023巡回演唱会-上海站', '2023-06-10 19:30:00', '上海梅赛德斯奔驰文化中心'),
(2, '五月天人生无限公司巡回演唱会-北京站', '2023-04-15 19:00:00', '北京国家体育场'),
(2, '五月天人生无限公司巡回演唱会-上海站', '2023-05-01 19:00:00', '上海体育场'),
(2, '五月天人生无限公司巡回演唱会-广州站', '2023-05-20 19:00:00', '广州天河体育中心'),
(3, '新裤子乐队2023巡演-北京站', '2023-07-08 20:00:00', '北京凯迪拉克中心'),
(4, '痛仰乐队20周年纪念演唱会', '2023-09-15 19:30:00', '北京工人体育馆'),
(5, '刺猬乐队火车巡演-成都站', '2023-08-12 20:00:00', '成都东郊记忆音乐公园'),
(6, 'Coldplay Live 2023-北京站', '2023-11-04 19:30:00', '北京鸟巢'),
(6, 'Coldplay Music of the Spheres Tour-上海站', '2023-11-08 19:30:00', '上海体育场'),
(7, 'Linkin Park Reunion Tour-深圳站', '2024-09-15 20:00:00', '深圳湾体育中心'),
(8, 'Imagine Dragons World Tour-广州站', '2024-05-20 19:30:00', '广州体育馆'),
(9, 'Maroon 5 Asia Tour-成都站', '2024-07-12 19:00:00', '成都体育中心'),
(10, 'OneRepublic Live-杭州站', '2024-08-25 19:30:00', '杭州奥体中心');

-- 插入歌迷数据
INSERT INTO Fan (name, gender, age, occupation, education) VALUES
('张伟', 'M', 28, '软件工程师', '本科'),
('李娜', 'F', 25, '设计师', '本科'),
('王强', 'M', 32, '产品经理', '硕士'),
('刘芳', 'F', 23, '学生', '本科'),
('陈明', 'M', 35, '教师', '硕士'),
('赵丽', 'F', 29, '医生', '博士'),
('孙杰', 'M', 26, '销售', '本科'),
('周敏', 'F', 31, '律师', '硕士'),
('吴涛', 'M', 24, '学生', '本科'),
('郑红', 'F', 27, '会计', '本科'),
('钱磊', 'M', 30, '工程师', '硕士'),
('冯静', 'F', 22, '学生', '本科'),
('韩冰', 'M', 33, '创业者', '本科'),
('蒋雪', 'F', 28, '编辑', '硕士'),
('沈阳', 'M', 26, '摄影师', '本科'),
('马超', 'M', 29, '程序员', '本科'),
('林静', 'F', 24, '护士', '大专'),
('黄磊', 'M', 31, '建筑师', '硕士'),
('杨雪', 'F', 26, '记者', '本科'),
('朱明', 'M', 27, '运营', '本科'),
('徐丽', 'F', 30, 'HR', '本科'),
('何强', 'M', 25, '学生', '本科'),
('宋敏', 'F', 32, '财务', '硕士'),
('唐涛', 'M', 28, '市场', '本科'),
('许红', 'F', 23, '学生', '本科');

-- 插入歌迷喜欢的乐队
INSERT INTO FanFavoriteBand (fan_id, band_id) VALUES
(1, 1), (1, 2), (1, 5),
(2, 2), (2, 3),
(3, 1), (3, 4),
(4, 2), (4, 5),
(5, 1), (5, 2), (5, 3),
(6, 3), (6, 4),
(7, 1), (7, 5),
(8, 2), (8, 3), (8, 4),
(9, 1), (9, 2),
(10, 3), (10, 5),
(11, 2), (11, 4),
(12, 1), (12, 3), (12, 5),
(13, 2), (13, 4),
(14, 1), (14, 2), (14, 3),
(15, 4), (15, 5),
(16, 6), (16, 7),
(17, 6), (17, 8),
(18, 7), (18, 9),
(19, 8), (19, 10),
(20, 6), (20, 9),
(21, 7), (21, 10),
(22, 6), (22, 8),
(23, 9), (23, 10),
(24, 6), (24, 7), (24, 8),
(25, 7), (25, 9), (25, 10);

-- 插入歌迷喜欢的专辑
INSERT INTO FanFavoriteAlbum (fan_id, album_id) VALUES
(1, 1), (1, 4), (1, 11),
(2, 5), (2, 7),
(3, 1), (3, 9),
(4, 4), (4, 12),
(5, 2), (5, 5), (5, 7),
(6, 7), (6, 10),
(7, 3), (7, 11),
(8, 5), (8, 7), (8, 9),
(9, 1), (9, 6),
(10, 8), (10, 12),
(11, 13), (11, 16),
(12, 14), (12, 19),
(13, 16), (13, 17),
(14, 19), (14, 21),
(15, 13), (15, 23),
(16, 16), (16, 21),
(17, 19), (17, 23),
(18, 13), (18, 16),
(19, 21), (19, 24),
(20, 14), (20, 22);

-- 插入歌迷喜欢的歌曲
INSERT INTO FanFavoriteSong (fan_id, song_id) VALUES
(1, 1), (1, 8), (1, 23),
(2, 11), (2, 17),
(3, 1), (3, 21),
(4, 9), (4, 26),
(5, 4), (5, 12), (5, 17),
(6, 17), (6, 22),
(7, 6), (7, 23),
(8, 11), (8, 18), (8, 21),
(9, 2), (9, 14),
(10, 19), (10, 25),
(11, 33), (11, 42),
(12, 36), (12, 55),
(13, 42), (13, 48),
(14, 55), (14, 63),
(15, 33), (15, 67),
(16, 42), (16, 63),
(17, 55), (17, 67),
(18, 33), (18, 42),
(19, 63), (19, 71),
(20, 36), (20, 65);

-- 插入演唱会参加记录
INSERT INTO ConcertAttendance (fan_id, concert_id) VALUES
(1, 1),
(1, 3),
(2, 4),
(2, 6),
(3, 1),
(3, 7),
(4, 3),
(4, 8),
(5, 2),
(5, 4),
(6, 6),
(6, 7),
(7, 1),
(7, 8),
(8, 3),
(8, 5),
(9, 1),
(9, 4),
(10, 6),
(10, 8),
(11, 9),
(12, 10),
(13, 11),
(14, 12),
(15, 13),
(16, 14),
(17, 9),
(18, 10),
(19, 11),
(20, 12);

-- 插入专辑乐评数据
INSERT INTO AlbumReview (fan_id, album_id, rating, comment, reviewed_at) VALUES
(1, 1, 9.5, '《夜空中最亮的星》太经典了，每次听都很感动！', '2023-06-01 10:30:00'),
(1, 4, 9.0, '五月天的专辑从不让人失望，诺亚方舟很有深度', '2023-06-05 14:20:00'),
(2, 5, 8.5, '自传这张专辑记录了五月天的成长历程，很有意义', '2023-06-10 16:45:00'),
(2, 7, 8.0, '新裤子的复古电子风格很独特', '2023-07-01 11:00:00'),
(3, 1, 10.0, '逃跑计划的首张专辑堪称完美！', '2023-06-15 09:30:00'),
(3, 9, 8.5, '痛仰的公路之歌让人热血沸腾', '2023-09-20 20:15:00'),
(4, 4, 8.0, '第二人生这张专辑很有启发性', '2023-05-20 13:40:00'),
(4, 12, 9.0, '刺猬的噪音摇滚风格很有特色', '2023-08-25 18:30:00'),
(5, 2, 7.5, '逃跑计划的第二张专辑也不错', '2023-07-10 15:20:00'),
(5, 5, 9.5, '自传是五月天最好的专辑之一', '2023-06-20 12:00:00'),
(6, 7, 9.0, '龙虎人丹这张专辑太有趣了', '2023-07-15 14:30:00'),
(6, 10, 8.0, '愿爱无忧充满人文关怀', '2023-09-25 16:45:00'),
(7, 3, 8.5, '化身孤岛的鲸这首歌很有画面感', '2023-11-20 10:00:00'),
(7, 11, 9.0, '刺猬的首张专辑很有冲击力', '2023-08-30 19:20:00'),
(8, 5, 8.5, '五月天20年的积淀都在这张专辑里', '2023-07-25 11:30:00'),
(8, 7, 7.5, '新裤子的音乐很有创意', '2023-07-20 13:15:00'),
(9, 1, 9.0, '逃跑计划的世界专辑陪伴了我的青春', '2023-06-25 17:00:00'),
(9, 6, 8.0, '步步这张专辑记录了特殊时期的思考', '2023-01-10 14:45:00'),
(10, 8, 7.0, 'BYOB这张专辑实验性很强', '2023-11-15 12:30:00'),
(10, 12, 8.5, '白日梦蓝这首歌很有意境', '2023-09-05 15:50:00'),
(11, 13, 9.5, 'Parachutes是Coldplay最纯粹的作品，每首歌都很动听', '2024-01-15 10:00:00'),
(12, 13, 9.0, 'Yellow这首歌太经典了，百听不厌', '2024-01-20 14:30:00'),
(13, 14, 9.5, 'A Rush of Blood to the Head是我最喜欢的Coldplay专辑', '2024-02-10 11:20:00'),
(14, 16, 10.0, 'Hybrid Theory改变了我对摇滚乐的认识，经典中的经典', '2024-02-15 16:00:00'),
(15, 16, 9.5, 'In the End永远是我的最爱', '2024-02-20 13:45:00'),
(16, 17, 9.0, 'Meteora延续了Hybrid Theory的风格，非常棒', '2024-03-01 15:30:00'),
(17, 19, 9.5, 'Radioactive太燃了，Imagine Dragons的代表作', '2024-03-10 10:15:00'),
(18, 19, 9.0, 'Night Visions整张专辑质量都很高', '2024-03-15 14:00:00'),
(19, 21, 9.5, 'Songs About Jane是流行摇滚的教科书', '2024-04-01 11:30:00'),
(20, 21, 9.0, 'This Love和She Will Be Loved都是神曲', '2024-04-05 16:20:00'),
(21, 23, 9.0, 'Apologize太好听了，OneRepublic的成名作', '2024-04-10 13:00:00'),
(22, 14, 9.5, 'Coldplay的每张专辑都值得反复听', '2024-04-15 15:45:00');

-- 插入乐队演唱歌曲关系（包括翻唱）
INSERT INTO Performance (band_id, song_id, source_band_id, note) VALUES
-- 逃跑计划演唱自己的歌
(1, 1, 1, '原创'),
(1, 2, 1, '原创'),
(1, 3, 1, '原创'),
(1, 4, 1, '原创'),
(1, 5, 1, '原创'),
(1, 6, 1, '原创'),
(1, 7, 1, '原创'),

-- 五月天演唱自己的歌
(2, 8, 2, '原创'),
(2, 9, 2, '原创'),
(2, 10, 2, '原创'),
(2, 11, 2, '原创'),
(2, 12, 2, '原创'),
(2, 13, 2, '原创'),
(2, 14, 2, '原创'),
(2, 15, 2, '原创'),

-- 新裤子演唱自己的歌
(3, 16, 3, '原创'),
(3, 17, 3, '原创'),
(3, 18, 3, '原创'),
(3, 19, 3, '原创'),
(3, 20, 3, '原创'),

-- 痛仰演唱自己的歌
(4, 21, 4, '原创'),
(4, 22, 4, '原创'),
(4, 23, 4, '原创'),
(4, 24, 4, '原创'),

-- 刺猬演唱自己的歌
(5, 25, 5, '原创'),
(5, 26, 5, '原创'),
(5, 27, 5, '原创'),
(5, 28, 5, '原创'),

-- Coldplay演唱自己的歌
(6, 29, 6, '原创'),
(6, 30, 6, '原创'),
(6, 31, 6, '原创'),
(6, 32, 6, '原创'),
(6, 33, 6, '原创'),
(6, 34, 6, '原创'),
(6, 35, 6, '原创'),
(6, 36, 6, '原创'),
(6, 37, 6, '原创'),
(6, 38, 6, '原创'),
(6, 39, 6, '原创'),
(6, 40, 6, '原创'),

-- Linkin Park演唱自己的歌
(7, 41, 7, '原创'),
(7, 42, 7, '原创'),
(7, 43, 7, '原创'),
(7, 44, 7, '原创'),
(7, 45, 7, '原创'),
(7, 46, 7, '原创'),
(7, 47, 7, '原创'),
(7, 48, 7, '原创'),
(7, 49, 7, '原创'),
(7, 50, 7, '原创'),
(7, 51, 7, '原创'),
(7, 52, 7, '原创'),
(7, 53, 7, '原创'),
(7, 54, 7, '原创'),

-- Imagine Dragons演唱自己的歌
(8, 55, 8, '原创'),
(8, 56, 8, '原创'),
(8, 57, 8, '原创'),
(8, 58, 8, '原创'),
(8, 59, 8, '原创'),
(8, 60, 8, '原创'),
(8, 61, 8, '原创'),

-- Maroon 5演唱自己的歌
(9, 62, 9, '原创'),
(9, 63, 9, '原创'),
(9, 64, 9, '原创'),
(9, 65, 9, '原创'),
(9, 66, 9, '原创'),
(9, 67, 9, '原创'),
(9, 68, 9, '原创'),

-- OneRepublic演唱自己的歌
(10, 69, 10, '原创'),
(10, 70, 10, '原创'),
(10, 71, 10, '原创'),
(10, 72, 10, '原创'),
(10, 73, 10, '原创'),
(10, 74, 10, '原创'),
(10, 75, 10, '原创'),

-- 翻唱示例：新裤子翻唱逃跑计划的歌
(3, 1, 1, '翻唱版本'),
-- 刺猬翻唱五月天的歌
(5, 10, 2, '噪音摇滚版本'),
-- Coldplay翻唱逃跑计划的歌
(6, 1, 1, '英文翻唱版本');

-- 更新专辑平均分（基于已有乐评）
UPDATE Album a SET avg_score = (
    SELECT ROUND(AVG(rating), 1)
    FROM AlbumReview ar
    WHERE ar.album_id = a.album_id
) WHERE album_id IN (SELECT DISTINCT album_id FROM AlbumReview);

-- ============================================
-- 3. 添加解散状态字段（用于现有数据库升级）
-- ============================================

-- 检查字段是否已存在，如果不存在则添加
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = 'band_management' 
  AND TABLE_NAME = 'Band' 
  AND COLUMN_NAME = 'is_disbanded';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE Band ADD COLUMN is_disbanded ENUM(''N'', ''Y'') NOT NULL DEFAULT ''N'' COMMENT ''是否已解散：N-未解散，Y-已解散'' AFTER member_count',
    'SELECT ''字段 is_disbanded 已存在，跳过添加'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = 'band_management' 
  AND TABLE_NAME = 'Band' 
  AND COLUMN_NAME = 'disbanded_at';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE Band ADD COLUMN disbanded_at DATE NULL COMMENT ''解散日期'' AFTER is_disbanded',
    'SELECT ''字段 disbanded_at 已存在，跳过添加'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加索引（如果不存在）
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists 
FROM information_schema.STATISTICS 
WHERE TABLE_SCHEMA = 'band_management' 
  AND TABLE_NAME = 'Band' 
  AND INDEX_NAME = 'idx_is_disbanded';

SET @sql = IF(@index_exists = 0,
    'ALTER TABLE Band ADD INDEX idx_is_disbanded (is_disbanded)',
    'SELECT ''索引 idx_is_disbanded 已存在，跳过添加'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT '✓ 乐队解散状态字段添加完成' AS status;

-- 验证数据
SELECT '=== 数据库创建完成 ===' AS status;
SELECT '乐队数量：', COUNT(*) FROM Band;
SELECT '成员数量：', COUNT(*) FROM Member;
SELECT '专辑数量：', COUNT(*) FROM Album;
SELECT '歌曲数量：', COUNT(*) FROM Song;
SELECT '演唱会数量：', COUNT(*) FROM Concert;
SELECT '歌迷数量：', COUNT(*) FROM Fan;
SELECT '乐评数量：', COUNT(*) FROM AlbumReview;

