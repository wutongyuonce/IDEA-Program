-- ============================================
-- 数据库完整性要求
-- ============================================

USE band_management;

-- ============================================
-- 1. 成员人数维护触发器
-- 修改触发器逻辑，只统计在队成员（leave_date IS NULL）
-- ============================================

-- 1.1 成员插入时更新乐队成员人数
DROP TRIGGER IF EXISTS trg_member_insert_update_count;
DELIMITER $$
CREATE TRIGGER trg_member_insert_update_count
AFTER INSERT ON Member
FOR EACH ROW
BEGIN
    -- 更新乐队成员人数（只统计在队成员）
    UPDATE Band 
    SET member_count = (
        SELECT COUNT(*) 
        FROM Member 
        WHERE band_id = NEW.band_id AND leave_date IS NULL
    )
    WHERE band_id = NEW.band_id;
END$$
DELIMITER ;

-- 1.2 成员删除时更新乐队成员人数
DROP TRIGGER IF EXISTS trg_member_delete_update_count;
DELIMITER $$
CREATE TRIGGER trg_member_delete_update_count
AFTER DELETE ON Member
FOR EACH ROW
BEGIN
    -- 更新乐队成员人数（只统计在队成员）
    UPDATE Band 
    SET member_count = (
        SELECT COUNT(*) 
        FROM Member 
        WHERE band_id = OLD.band_id AND leave_date IS NULL
    )
    WHERE band_id = OLD.band_id;
END$$
DELIMITER ;

-- 1.3 成员更新时更新相关乐队成员人数
DROP TRIGGER IF EXISTS trg_member_update_band;
DELIMITER $$
CREATE TRIGGER trg_member_update_band
AFTER UPDATE ON Member
FOR EACH ROW
BEGIN
    -- 如果成员更换了乐队或离队状态改变
    IF OLD.band_id != NEW.band_id OR 
       (OLD.leave_date IS NULL AND NEW.leave_date IS NOT NULL) OR
       (OLD.leave_date IS NOT NULL AND NEW.leave_date IS NULL) THEN
        
        -- 更新旧乐队成员人数
        UPDATE Band 
        SET member_count = (
            SELECT COUNT(*) 
            FROM Member 
            WHERE band_id = OLD.band_id AND leave_date IS NULL
        )
        WHERE band_id = OLD.band_id;
        
        -- 如果换了乐队，也更新新乐队成员人数
        IF OLD.band_id != NEW.band_id THEN
            UPDATE Band 
            SET member_count = (
                SELECT COUNT(*) 
                FROM Member 
                WHERE band_id = NEW.band_id AND leave_date IS NULL
            )
            WHERE band_id = NEW.band_id;
        END IF;
    END IF;
END$$
DELIMITER ;

-- ============================================
-- 2. 专辑排行榜表和触发器
-- ============================================

-- 2.1 创建专辑排行榜表
DROP TABLE IF EXISTS AlbumRanking;
CREATE TABLE AlbumRanking (
    ranking_id INT AUTO_INCREMENT PRIMARY KEY,
    album_id BIGINT NOT NULL,
    band_id BIGINT NOT NULL,
    album_title VARCHAR(200) NOT NULL,
    band_name VARCHAR(100) NOT NULL,
    avg_score DECIMAL(3,1) NOT NULL,
    review_count INT NOT NULL DEFAULT 0,
    release_date DATE NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_album (album_id),
    INDEX idx_avg_score (avg_score DESC),
    CONSTRAINT fk_ranking_album FOREIGN KEY (album_id) REFERENCES Album(album_id) 
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_ranking_band FOREIGN KEY (band_id) REFERENCES Band(band_id) 
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='专辑排行榜（前10名）';

-- 授予 fan_user 对 AlbumRanking 表的查询权限
GRANT SELECT ON band_management.AlbumRanking TO 'fan_user'@'localhost';

-- 2.2 初始化排行榜数据
INSERT INTO AlbumRanking (album_id, band_id, album_title, band_name, avg_score, review_count, release_date)
SELECT 
    a.album_id,
    a.band_id,
    a.title,
    b.name,
    COALESCE(a.avg_score, 0),
    COUNT(ar.review_id),
    a.release_date
FROM Album a
JOIN Band b ON a.band_id = b.band_id
LEFT JOIN AlbumReview ar ON a.album_id = ar.album_id
GROUP BY a.album_id, a.band_id, a.title, b.name, a.avg_score, a.release_date
HAVING COALESCE(a.avg_score, 0) > 0
ORDER BY COALESCE(a.avg_score, 0) DESC, COUNT(ar.review_id) DESC
LIMIT 10;

-- 2.3 创建更新排行榜的存储过程
DROP PROCEDURE IF EXISTS sp_update_album_ranking;
DELIMITER $$
CREATE PROCEDURE sp_update_album_ranking()
BEGIN
    -- 清空排行榜（使用DELETE而不是TRUNCATE，避免触发器中的隐式提交问题）
    DELETE FROM AlbumRanking;
    
    -- 重新插入前10名
    INSERT INTO AlbumRanking (album_id, band_id, album_title, band_name, avg_score, review_count, release_date)
    SELECT 
        a.album_id,
        a.band_id,
        a.title,
        b.name,
        COALESCE(a.avg_score, 0),
        COUNT(ar.review_id),
        a.release_date
    FROM Album a
    JOIN Band b ON a.band_id = b.band_id
    LEFT JOIN AlbumReview ar ON a.album_id = ar.album_id
    GROUP BY a.album_id, a.band_id, a.title, b.name, a.avg_score, a.release_date
    HAVING COALESCE(a.avg_score, 0) > 0
    ORDER BY COALESCE(a.avg_score, 0) DESC, COUNT(ar.review_id) DESC
    LIMIT 10;
END$$
DELIMITER ;

-- 2.4 乐评插入时更新专辑平均分和排行榜
DROP TRIGGER IF EXISTS trg_review_insert_update_album;
DELIMITER $$
CREATE TRIGGER trg_review_insert_update_album
AFTER INSERT ON AlbumReview
FOR EACH ROW
BEGIN
    -- 更新专辑平均分
    UPDATE Album 
    SET avg_score = (
        SELECT ROUND(AVG(rating), 1)
        FROM AlbumReview
        WHERE album_id = NEW.album_id
    )
    WHERE album_id = NEW.album_id;
    
    -- 更新排行榜
    CALL sp_update_album_ranking();
END$$
DELIMITER ;

-- 2.5 乐评更新时更新专辑平均分和排行榜
DROP TRIGGER IF EXISTS trg_review_update_album;
DELIMITER $$
CREATE TRIGGER trg_review_update_album
AFTER UPDATE ON AlbumReview
FOR EACH ROW
BEGIN
    -- 更新专辑平均分
    UPDATE Album 
    SET avg_score = (
        SELECT ROUND(AVG(rating), 1)
        FROM AlbumReview
        WHERE album_id = NEW.album_id
    )
    WHERE album_id = NEW.album_id;
    
    -- 如果评论涉及不同专辑，也更新旧专辑
    IF OLD.album_id != NEW.album_id THEN
        UPDATE Album 
        SET avg_score = (
            SELECT ROUND(AVG(rating), 1)
            FROM AlbumReview
            WHERE album_id = OLD.album_id
        )
        WHERE album_id = OLD.album_id;
    END IF;
    
    -- 更新排行榜
    CALL sp_update_album_ranking();
END$$
DELIMITER ;

-- 2.6 乐评删除时更新专辑平均分和排行榜
DROP TRIGGER IF EXISTS trg_review_delete_update_album;
DELIMITER $$
CREATE TRIGGER trg_review_delete_update_album
AFTER DELETE ON AlbumReview
FOR EACH ROW
BEGIN
    -- 更新专辑平均分
    UPDATE Album 
    SET avg_score = (
        SELECT ROUND(AVG(rating), 1)
        FROM AlbumReview
        WHERE album_id = OLD.album_id
    )
    WHERE album_id = OLD.album_id;
    
    -- 更新排行榜
    CALL sp_update_album_ranking();
END$$
DELIMITER ;

-- ============================================
-- 3. 额外的完整性约束触发器
-- ============================================

-- 3.1 确保队长是该乐队的成员
DROP TRIGGER IF EXISTS trg_band_check_leader;
DELIMITER $$
CREATE TRIGGER trg_band_check_leader
BEFORE UPDATE ON Band
FOR EACH ROW
BEGIN
    DECLARE leader_band_id BIGINT;
    
    -- 如果设置了队长
    IF NEW.leader_member_id IS NOT NULL THEN
        -- 检查队长是否属于该乐队
        SELECT band_id INTO leader_band_id
        FROM Member
        WHERE member_id = NEW.leader_member_id;
        
        IF leader_band_id IS NULL OR leader_band_id != NEW.band_id THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '队长必须是该乐队的成员';
        END IF;
    END IF;
END$$
DELIMITER ;

-- 3.2 防止成员在同一时间加入多个乐队（INSERT）
DROP TRIGGER IF EXISTS trg_member_check_person_overlap_insert;
DELIMITER $$
CREATE TRIGGER trg_member_check_person_overlap_insert
BEFORE INSERT ON Member
FOR EACH ROW
BEGIN
    DECLARE overlap_count INT;
    
    SELECT COUNT(*) INTO overlap_count
    FROM Member
    WHERE person_id = NEW.person_id
      AND band_id != NEW.band_id
      AND (
          (NEW.join_date >= join_date AND (leave_date IS NULL OR NEW.join_date <= leave_date))
          OR
          (NEW.leave_date IS NOT NULL AND NEW.leave_date >= join_date AND (leave_date IS NULL OR NEW.leave_date <= leave_date))
          OR
          (join_date >= NEW.join_date AND (NEW.leave_date IS NULL OR join_date <= NEW.leave_date))
          OR
          (leave_date IS NOT NULL AND leave_date >= NEW.join_date AND (NEW.leave_date IS NULL OR leave_date <= NEW.leave_date))
      );
    
    IF overlap_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'A person cannot join multiple bands at the same time';
    END IF;
END$$
DELIMITER ;

-- 3.3 防止成员在同一时间加入多个乐队（UPDATE）
DROP TRIGGER IF EXISTS trg_member_check_person_overlap_update;
DELIMITER $$
CREATE TRIGGER trg_member_check_person_overlap_update
BEFORE UPDATE ON Member
FOR EACH ROW
BEGIN
    DECLARE overlap_count INT;
    
    IF NEW.person_id != OLD.person_id
       OR NEW.join_date != OLD.join_date
       OR (NEW.leave_date IS NULL AND OLD.leave_date IS NOT NULL)
       OR (NEW.leave_date IS NOT NULL AND OLD.leave_date IS NULL)
       OR (NEW.leave_date IS NOT NULL AND OLD.leave_date IS NOT NULL AND NEW.leave_date != OLD.leave_date)
       OR NEW.band_id != OLD.band_id THEN
        
        SELECT COUNT(*) INTO overlap_count
        FROM Member
        WHERE person_id = NEW.person_id
          AND band_id != NEW.band_id
          AND member_id != OLD.member_id
          AND (
              (NEW.join_date >= join_date AND (leave_date IS NULL OR NEW.join_date <= leave_date))
              OR
              (NEW.leave_date IS NOT NULL AND NEW.leave_date >= join_date AND (leave_date IS NULL OR NEW.leave_date <= leave_date))
              OR
              (join_date >= NEW.join_date AND (NEW.leave_date IS NULL OR join_date <= NEW.leave_date))
              OR
              (leave_date IS NOT NULL AND leave_date >= NEW.join_date AND (NEW.leave_date IS NULL OR leave_date <= NEW.leave_date))
          );
        
        IF overlap_count > 0 THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'A person cannot join multiple bands at the same time';
        END IF;
    END IF;
END$$
DELIMITER ;

-- ============================================
-- 4. 验证触发器（可选测试）
-- ============================================
-- 注意：以下测试代码仅用于验证触发器功能，不是必须执行的
-- 如果只是初始化数据库，可以跳过此部分

-- 4.1 验证成员人数触发器
-- 测试说明：添加和删除成员时，乐队的member_count应该自动更新
/*
SELECT '=== 测试1: 成员人数触发器 ===' AS test_name;

-- 查看逃跑计划当前成员数
SELECT band_id, name, member_count FROM Band WHERE band_id = 1;

-- 添加测试成员（在队）
INSERT INTO Member (person_id, band_id, name, gender, birth_date, role, join_date, leave_date)
VALUES (9998, 1, '测试成员A', 'M', '1990-01-01', '键盘手', '2024-01-01', NULL);

-- 应该看到member_count增加1
SELECT '添加在队成员后:' AS action, band_id, name, member_count FROM Band WHERE band_id = 1;

-- 添加已离队成员
INSERT INTO Member (person_id, band_id, name, gender, birth_date, role, join_date, leave_date)
VALUES (9997, 1, '测试成员B', 'F', '1992-01-01', '和声', '2020-01-01', '2023-12-31');

-- member_count不应该变化（因为已离队）
SELECT '添加离队成员后:' AS action, band_id, name, member_count FROM Band WHERE band_id = 1;

-- 清理测试数据
DELETE FROM Member WHERE person_id IN (9998, 9997);
SELECT '清理完成' AS status;
*/

-- 4.2 验证专辑排行榜触发器
-- 测试说明：添加、修改、删除乐评时，专辑平均分和排行榜应该自动更新
/*
SELECT '=== 测试2: 专辑排行榜触发器 ===' AS test_name;

-- 查看当前排行榜前5名
SELECT 
    ROW_NUMBER() OVER (ORDER BY avg_score DESC, review_count DESC) AS ranking,
    album_title,
    band_name,
    avg_score,
    review_count
FROM AlbumRanking
ORDER BY avg_score DESC, review_count DESC
LIMIT 5;

-- 查看《世界》专辑当前评分
SELECT album_id, title, avg_score FROM Album WHERE album_id = 1;

-- 添加一条高分乐评
INSERT INTO AlbumReview (fan_id, album_id, rating, comment, reviewed_at)
VALUES (11, 1, 10.0, '[测试] 非常棒的专辑！', NOW());

-- 应该看到平均分提高，排行榜更新
SELECT '添加乐评后:' AS action, album_id, title, avg_score FROM Album WHERE album_id = 1;

-- 删除测试乐评
DELETE FROM AlbumReview WHERE comment = '[测试] 非常棒的专辑！';
SELECT '清理完成' AS status;
*/

-- 4.3 验证队长约束触发器
-- 测试说明：队长必须是本乐队的成员
/*
SELECT '=== 测试3: 队长约束触发器 ===' AS test_name;

-- 尝试将其他乐队的成员设为队长（应该失败）
-- UPDATE Band SET leader_member_id = 10 WHERE band_id = 1;
-- 预期错误: ERROR 1644 (45000): 队长必须是该乐队的成员

SELECT '如果执行上面注释的UPDATE，会报错：队长必须是该乐队的成员' AS expected_error;
*/

-- 4.4 验证成员时间重叠约束触发器
-- 测试说明：同一个人不能同时在多个乐队
/*
SELECT '=== 测试4: 成员时间重叠约束触发器 ===' AS test_name;

-- 创建测试成员（已离队）
INSERT INTO Member (person_id, band_id, name, gender, birth_date, role, join_date, leave_date)
VALUES (9999, 1, '测试成员C', 'M', '1990-01-01', '吉他手', '2020-01-01', '2023-12-31');

-- 让同一人加入另一个乐队（时间不重叠，应该成功）
INSERT INTO Member (person_id, band_id, name, gender, birth_date, role, join_date, leave_date)
VALUES (9999, 2, '测试成员C', 'M', '1990-01-01', '贝斯手', '2024-01-01', NULL);

SELECT '成功：同一人在不同时间段加入不同乐队' AS result;

-- 尝试让同一人在重叠时间加入第三个乐队（应该失败）
-- INSERT INTO Member (person_id, band_id, name, gender, birth_date, role, join_date, leave_date)
-- VALUES (9999, 3, '测试成员C', 'M', '1990-01-01', '鼓手', '2024-06-01', NULL);
-- 预期错误: ERROR 1644 (45000): A person cannot join multiple bands at the same time

SELECT '如果执行上面注释的INSERT，会报错：A person cannot join multiple bands at the same time' AS expected_error;

-- 清理测试数据
DELETE FROM Member WHERE person_id = 9999;
SELECT '清理完成' AS status;
*/

-- ============================================
-- 5. 修正现有数据的成员人数
-- ============================================
SELECT '=== 修正现有数据的成员人数 ===' AS title;

-- 更新所有乐队的成员人数（只统计在队成员）
UPDATE Band b
SET member_count = (
    SELECT COUNT(*) 
    FROM Member m 
    WHERE m.band_id = b.band_id AND m.leave_date IS NULL
);

SELECT '✓ 所有乐队的成员人数已重新计算（只统计在队成员）' AS status;

-- 验证结果
SELECT 
    b.band_id,
    b.name,
    b.member_count AS stored_count,
    (SELECT COUNT(*) FROM Member m WHERE m.band_id = b.band_id AND m.leave_date IS NULL) AS actual_count,
    CASE 
        WHEN b.member_count = (SELECT COUNT(*) FROM Member m WHERE m.band_id = b.band_id AND m.leave_date IS NULL)
        THEN '✓ 一致'
        ELSE '✗ 不一致'
    END AS status
FROM Band b
ORDER BY b.band_id;

SELECT '=== 数据库完整性约束配置完成 ===' AS final_status;
SELECT '✓ 成员人数自动维护触发器已创建' AS item
UNION ALL SELECT '✓ 专辑排行榜自动更新触发器已创建'
UNION ALL SELECT '✓ 队长约束触发器已创建'
UNION ALL SELECT '✓ 成员时间重叠约束触发器已创建'
UNION ALL SELECT '✓ 现有数据已修正';

-- ============================================
-- 使用说明
-- ============================================
/*
触发器功能说明：

1. 成员人数自动维护
   - 添加成员时：自动更新乐队的member_count（只统计在队成员）
   - 删除成员时：自动更新乐队的member_count
   - 更新成员时：如果改变乐队或离队状态，自动更新相关乐队的member_count

2. 专辑排行榜自动更新
   - 添加乐评时：自动更新专辑平均分和排行榜
   - 修改乐评时：自动更新专辑平均分和排行榜
   - 删除乐评时：自动更新专辑平均分和排行榜

3. 队长约束
   - 设置队长时：自动检查队长是否是本乐队成员
   - 如果不是，抛出错误：队长必须是该乐队的成员

4. 成员时间重叠约束
   - 添加成员时：检查同一人是否在同一时间段加入多个乐队
   - 更新成员时：检查修改后是否导致时间重叠
   - 如果重叠，抛出错误：A person cannot join multiple bands at the same time

测试方法：
- 取消注释第4部分的测试代码，逐个执行测试场景
- 每个测试场景都有详细说明和预期结果
*/
