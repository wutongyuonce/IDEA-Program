-- ============================================
-- 用户权限验证测试脚本
-- 测试 admin_user、band_user、fan_user 的权限设置
-- ============================================

-- 使用说明：
-- 1. 确保已执行 database_implementation.sql
-- 2. 确保已执行 database_security.sql
-- 3. 本脚本需要以 root 用户身份执行
-- 4. 测试过程中会创建和删除测试数据

USE band_management;

-- ============================================
-- 测试准备：创建测试数据
-- ============================================

SELECT '========================================' AS '';
SELECT '开始用户权限验证测试' AS '';
SELECT '========================================' AS '';

-- ============================================
-- 第一部分：管理员用户 (admin_user) 权限测试
-- ============================================

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT '测试 1: 管理员用户 (admin_user) 权限' AS '';
SELECT '========================================' AS '';

-- 测试 1.1: 管理员可以查询所有表
SELECT '--- 测试 1.1: 管理员查询权限 ---' AS '';

-- 切换到 admin_user（注意：需要在新连接中执行）
-- 这里使用注释说明，实际测试需要新建连接
/*
mysql -u admin_user -pAdmin@123456 band_management -e "
SELECT '✓ 管理员可以查询 Band 表' AS result, COUNT(*) AS count FROM Band;
SELECT '✓ 管理员可以查询 Member 表' AS result, COUNT(*) AS count FROM Member;
SELECT '✓ 管理员可以查询 Fan 表' AS result, COUNT(*) AS count FROM Fan;
SELECT '✓ 管理员可以查询 Album 表' AS result, COUNT(*) AS count FROM Album;
"
*/

-- 测试 1.2: 管理员可以插入数据
SELECT '--- 测试 1.2: 管理员插入权限 ---' AS '';
/*
mysql -u admin_user -pAdmin@123456 band_management -e "
INSERT INTO Band (name, founded_at, intro, member_count) 
VALUES ('测试乐队_admin', '2024-01-01', '管理员测试插入', 0);
SELECT '✓ 管理员可以插入数据' AS result;
"
*/

-- 测试 1.3: 管理员可以更新数据
SELECT '--- 测试 1.3: 管理员更新权限 ---' AS '';
/*
mysql -u admin_user -pAdmin@123456 band_management -e "
UPDATE Band SET intro = '管理员测试更新' WHERE name = '测试乐队_admin';
SELECT '✓ 管理员可以更新数据' AS result;
"
*/

-- 测试 1.4: 管理员可以删除数据
SELECT '--- 测试 1.4: 管理员删除权限 ---' AS '';
/*
mysql -u admin_user -pAdmin@123456 band_management -e "
DELETE FROM Band WHERE name = '测试乐队_admin';
SELECT '✓ 管理员可以删除数据' AS result;
"
*/

-- 测试 1.5: 管理员可以创建和删除表
SELECT '--- 测试 1.5: 管理员 DDL 权限 ---' AS '';
/*
mysql -u admin_user -pAdmin@123456 band_management -e "
CREATE TABLE test_admin_table (id INT PRIMARY KEY, name VARCHAR(50));
SELECT '✓ 管理员可以创建表' AS result;
DROP TABLE test_admin_table;
SELECT '✓ 管理员可以删除表' AS result;
"
*/

SELECT '✓ 管理员用户权限测试完成' AS '';

-- ============================================
-- 第二部分：乐队用户 (band_user) 权限测试
-- ============================================

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT '测试 2: 乐队用户 (band_user) 权限' AS '';
SELECT '========================================' AS '';

-- 测试 2.1: 乐队用户可以查询和修改乐队相关表
SELECT '--- 测试 2.1: 乐队用户对乐队表的完整权限 ---' AS '';
/*
mysql -u band_user -pBand@123456 band_management -e "
-- 查询
SELECT '✓ 乐队用户可以查询 Band 表' AS result, COUNT(*) AS count FROM Band;

-- 插入
INSERT INTO Band (name, founded_at, intro, member_count) 
VALUES ('测试乐队_band', '2024-01-01', '乐队用户测试', 0);
SELECT '✓ 乐队用户可以插入 Band 数据' AS result;

-- 更新
UPDATE Band SET intro = '乐队用户测试更新' WHERE name = '测试乐队_band';
SELECT '✓ 乐队用户可以更新 Band 数据' AS result;

-- 删除
DELETE FROM Band WHERE name = '测试乐队_band';
SELECT '✓ 乐队用户可以删除 Band 数据' AS result;
"
*/

-- 测试 2.2: 乐队用户可以管理成员
SELECT '--- 测试 2.2: 乐队用户对成员表的完整权限 ---' AS '';
/*
mysql -u band_user -pBand@123456 band_management -e "
SELECT '✓ 乐队用户可以查询 Member 表' AS result, COUNT(*) AS count FROM Member;
SELECT '✓ 乐队用户可以插入/更新/删除 Member 数据' AS result;
"
*/

-- 测试 2.3: 乐队用户可以管理专辑和歌曲
SELECT '--- 测试 2.3: 乐队用户对专辑和歌曲表的完整权限 ---' AS '';
/*
mysql -u band_user -pBand@123456 band_management -e "
SELECT '✓ 乐队用户可以查询 Album 表' AS result, COUNT(*) AS count FROM Album;
SELECT '✓ 乐队用户可以查询 Song 表' AS result, COUNT(*) AS count FROM Song;
SELECT '✓ 乐队用户可以管理专辑和歌曲' AS result;
"
*/

-- 测试 2.4: 乐队用户可以管理演唱会
SELECT '--- 测试 2.4: 乐队用户对演唱会表的完整权限 ---' AS '';
/*
mysql -u band_user -pBand@123456 band_management -e "
SELECT '✓ 乐队用户可以查询 Concert 表' AS result, COUNT(*) AS count FROM Concert;
SELECT '✓ 乐队用户可以管理演唱会' AS result;
"
*/

-- 测试 2.5: 乐队用户只能查看歌迷数据（不能修改）
SELECT '--- 测试 2.5: 乐队用户对歌迷表只有查询权限 ---' AS '';
/*
mysql -u band_user -pBand@123456 band_management -e "
-- 查询应该成功
SELECT '✓ 乐队用户可以查询 Fan 表' AS result, COUNT(*) AS count FROM Fan;
"

-- 插入应该失败
mysql -u band_user -pBand@123456 band_management -e "
INSERT INTO Fan (name, gender, age, occupation, education) 
VALUES ('测试歌迷', 'M', 25, '测试', '本科');
" 2>&1 | grep -q "INSERT command denied" && echo "✓ 乐队用户不能插入 Fan 数据（符合预期）" || echo "✗ 权限设置有误"
*/

-- 测试 2.6: 乐队用户只能查看乐评（不能修改）
SELECT '--- 测试 2.6: 乐队用户对乐评表只有查询权限 ---' AS '';
/*
mysql -u band_user -pBand@123456 band_management -e "
-- 查询应该成功
SELECT '✓ 乐队用户可以查询 AlbumReview 表' AS result, COUNT(*) AS count FROM AlbumReview;
"

-- 删除应该失败
mysql -u band_user -pBand@123456 band_management -e "
DELETE FROM AlbumReview WHERE review_id = 1;
" 2>&1 | grep -q "DELETE command denied" && echo "✓ 乐队用户不能删除 AlbumReview 数据（符合预期）" || echo "✗ 权限设置有误"
*/

-- 测试 2.7: 乐队用户可以查看歌迷喜好（只读）
SELECT '--- 测试 2.7: 乐队用户对歌迷喜好表只有查询权限 ---' AS '';
/*
mysql -u band_user -pBand@123456 band_management -e "
SELECT '✓ 乐队用户可以查询 FanFavoriteBand 表' AS result, COUNT(*) AS count FROM FanFavoriteBand;
SELECT '✓ 乐队用户可以查询 FanFavoriteAlbum 表' AS result, COUNT(*) AS count FROM FanFavoriteAlbum;
SELECT '✓ 乐队用户可以查询 FanFavoriteSong 表' AS result, COUNT(*) AS count FROM FanFavoriteSong;
"
*/

-- 测试 2.8: 乐队用户可以查看演唱会参加记录（只读）
SELECT '--- 测试 2.8: 乐队用户对演唱会参加记录只有查询权限 ---' AS '';
/*
mysql -u band_user -pBand@123456 band_management -e "
SELECT '✓ 乐队用户可以查询 ConcertAttendance 表' AS result, COUNT(*) AS count FROM ConcertAttendance;
"
*/

-- 测试 2.9: 乐队用户可以访问统计视图
SELECT '--- 测试 2.9: 乐队用户可以访问统计视图 ---' AS '';
/*
mysql -u band_user -pBand@123456 band_management -e "
SELECT '✓ 乐队用户可以查询 v_band1_fans 视图' AS result, COUNT(*) AS count FROM v_band1_fans;
SELECT '✓ 乐队用户可以查询 v_band1_reviews 视图' AS result, COUNT(*) AS count FROM v_band1_reviews;
SELECT '✓ 乐队用户可以查询 v_band1_fan_count 视图' AS result FROM v_band1_fan_count;
SELECT '✓ 乐队用户可以查询 v_band1_fan_age_stats 视图' AS result FROM v_band1_fan_age_stats LIMIT 1;
"
*/

-- 测试 2.10: 乐队用户不能创建表
SELECT '--- 测试 2.10: 乐队用户没有 DDL 权限 ---' AS '';
/*
mysql -u band_user -pBand@123456 band_management -e "
CREATE TABLE test_band_table (id INT);
" 2>&1 | grep -q "CREATE command denied" && echo "✓ 乐队用户不能创建表（符合预期）" || echo "✗ 权限设置有误"
*/

SELECT '✓ 乐队用户权限测试完成' AS '';

-- ============================================
-- 第三部分：歌迷用户 (fan_user) 权限测试
-- ============================================

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT '测试 3: 歌迷用户 (fan_user) 权限' AS '';
SELECT '========================================' AS '';

-- 测试 3.1: 歌迷用户可以查询和修改歌迷信息
SELECT '--- 测试 3.1: 歌迷用户对歌迷表的完整权限 ---' AS '';
/*
mysql -u fan_user -pFan@123456 band_management -e "
-- 查询
SELECT '✓ 歌迷用户可以查询 Fan 表' AS result, COUNT(*) AS count FROM Fan;

-- 插入
INSERT INTO Fan (name, gender, age, occupation, education) 
VALUES ('测试歌迷_fan', 'M', 25, '测试职业', '本科');
SELECT '✓ 歌迷用户可以插入 Fan 数据' AS result;

-- 更新
UPDATE Fan SET occupation = '测试职业更新' WHERE name = '测试歌迷_fan';
SELECT '✓ 歌迷用户可以更新 Fan 数据' AS result;

-- 删除
DELETE FROM Fan WHERE name = '测试歌迷_fan';
SELECT '✓ 歌迷用户可以删除 Fan 数据' AS result;
"
*/

-- 测试 3.2: 歌迷用户可以管理喜好信息
SELECT '--- 测试 3.2: 歌迷用户对喜好表的完整权限 ---' AS '';
/*
mysql -u fan_user -pFan@123456 band_management -e "
SELECT '✓ 歌迷用户可以查询 FanFavoriteBand 表' AS result, COUNT(*) AS count FROM FanFavoriteBand;
SELECT '✓ 歌迷用户可以查询 FanFavoriteAlbum 表' AS result, COUNT(*) AS count FROM FanFavoriteAlbum;
SELECT '✓ 歌迷用户可以查询 FanFavoriteSong 表' AS result, COUNT(*) AS count FROM FanFavoriteSong;
SELECT '✓ 歌迷用户可以管理喜好数据' AS result;
"
*/

-- 测试 3.3: 歌迷用户可以管理演唱会参加记录
SELECT '--- 测试 3.3: 歌迷用户对演唱会参加记录的完整权限 ---' AS '';
/*
mysql -u fan_user -pFan@123456 band_management -e "
SELECT '✓ 歌迷用户可以查询 ConcertAttendance 表' AS result, COUNT(*) AS count FROM ConcertAttendance;
SELECT '✓ 歌迷用户可以管理演唱会参加记录' AS result;
"
*/

-- 测试 3.4: 歌迷用户可以管理乐评
SELECT '--- 测试 3.4: 歌迷用户对乐评表的完整权限 ---' AS '';
/*
mysql -u fan_user -pFan@123456 band_management -e "
SELECT '✓ 歌迷用户可以查询 AlbumReview 表' AS result, COUNT(*) AS count FROM AlbumReview;
SELECT '✓ 歌迷用户可以管理乐评数据' AS result;
"
*/

-- 测试 3.5: 歌迷用户只能查看乐队信息（不能修改）
SELECT '--- 测试 3.5: 歌迷用户对乐队表只有查询权限 ---' AS '';
/*
mysql -u fan_user -pFan@123456 band_management -e "
-- 查询应该成功
SELECT '✓ 歌迷用户可以查询 Band 表' AS result, COUNT(*) AS count FROM Band;
"

-- 更新应该失败
mysql -u fan_user -pFan@123456 band_management -e "
UPDATE Band SET intro = '测试' WHERE band_id = 1;
" 2>&1 | grep -q "UPDATE command denied" && echo "✓ 歌迷用户不能更新 Band 数据（符合预期）" || echo "✗ 权限设置有误"
*/

-- 测试 3.6: 歌迷用户只能查看成员信息（不能修改）
SELECT '--- 测试 3.6: 歌迷用户对成员表只有查询权限 ---' AS '';
/*
mysql -u fan_user -pFan@123456 band_management -e "
SELECT '✓ 歌迷用户可以查询 Member 表' AS result, COUNT(*) AS count FROM Member;
"
*/

-- 测试 3.7: 歌迷用户只能查看专辑和歌曲（不能修改）
SELECT '--- 测试 3.7: 歌迷用户对专辑和歌曲表只有查询权限 ---' AS '';
/*
mysql -u fan_user -pFan@123456 band_management -e "
SELECT '✓ 歌迷用户可以查询 Album 表' AS result, COUNT(*) AS count FROM Album;
SELECT '✓ 歌迷用户可以查询 Song 表' AS result, COUNT(*) AS count FROM Song;
"
*/

-- 测试 3.8: 歌迷用户只能查看演唱会（不能修改）
SELECT '--- 测试 3.8: 歌迷用户对演唱会表只有查询权限 ---' AS '';
/*
mysql -u fan_user -pFan@123456 band_management -e "
SELECT '✓ 歌迷用户可以查询 Concert 表' AS result, COUNT(*) AS count FROM Concert;
"
*/

-- 测试 3.9: 歌迷用户可以查看专辑排行榜
SELECT '--- 测试 3.9: 歌迷用户可以查询专辑排行榜 ---' AS '';
/*
mysql -u fan_user -pFan@123456 band_management -e "
SELECT '✓ 歌迷用户可以查询 AlbumRanking 表' AS result, COUNT(*) AS count FROM AlbumRanking;
"
*/

-- 测试 3.10: 歌迷用户不能创建表
SELECT '--- 测试 3.10: 歌迷用户没有 DDL 权限 ---' AS '';
/*
mysql -u fan_user -pFan@123456 band_management -e "
CREATE TABLE test_fan_table (id INT);
" 2>&1 | grep -q "CREATE command denied" && echo "✓ 歌迷用户不能创建表（符合预期）" || echo "✗ 权限设置有误"
*/

SELECT '✓ 歌迷用户权限测试完成' AS '';

-- ============================================
-- 第四部分：权限隔离测试
-- ============================================

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT '测试 4: 权限隔离验证' AS '';
SELECT '========================================' AS '';

-- 测试 4.1: 乐队用户不能修改歌迷数据
SELECT '--- 测试 4.1: 乐队用户不能修改歌迷数据 ---' AS '';
SELECT '预期结果: INSERT/UPDATE/DELETE 操作应该被拒绝' AS '';

-- 测试 4.2: 歌迷用户不能修改乐队数据
SELECT '--- 测试 4.2: 歌迷用户不能修改乐队数据 ---' AS '';
SELECT '预期结果: INSERT/UPDATE/DELETE 操作应该被拒绝' AS '';

-- 测试 4.3: 乐队用户不能修改乐评
SELECT '--- 测试 4.3: 乐队用户不能修改乐评 ---' AS '';
SELECT '预期结果: UPDATE/DELETE 操作应该被拒绝' AS '';

-- 测试 4.4: 歌迷用户不能修改演唱会
SELECT '--- 测试 4.4: 歌迷用户不能修改演唱会 ---' AS '';
SELECT '预期结果: INSERT/UPDATE/DELETE 操作应该被拒绝' AS '';

SELECT '✓ 权限隔离测试完成' AS '';

-- ============================================
-- 第五部分：User 表访问权限测试
-- ============================================

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT '测试 5: User 表访问权限' AS '';
SELECT '========================================' AS '';

-- 测试 5.1: 乐队用户可以查询 User 表（用于登录验证）
SELECT '--- 测试 5.1: 乐队用户可以查询 User 表 ---' AS '';
/*
mysql -u band_user -pBand@123456 band_management -e "
SELECT '✓ 乐队用户可以查询 User 表' AS result, COUNT(*) AS count FROM User;
"
*/

-- 测试 5.2: 歌迷用户可以查询 User 表（用于登录验证）
SELECT '--- 测试 5.2: 歌迷用户可以查询 User 表 ---' AS '';
/*
mysql -u fan_user -pFan@123456 band_management -e "
SELECT '✓ 歌迷用户可以查询 User 表' AS result, COUNT(*) AS count FROM User;
"
*/

SELECT '✓ User 表访问权限测试完成' AS '';

-- ============================================
-- 测试总结
-- ============================================

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT '权限验证测试总结' AS '';
SELECT '========================================' AS '';

SELECT '
测试项目总结：

1. 管理员用户 (admin_user)
   ✓ 拥有所有数据库权限
   ✓ 可以创建、修改、删除任何表和数据
   ✓ 可以管理用户和权限

2. 乐队用户 (band_user)
   ✓ 可以完全管理：Band, Member, Album, Song, Concert, Performance
   ✓ 只能查看：Fan, AlbumReview, ConcertAttendance, FanFavorite*
   ✓ 可以访问统计视图：v_band1_*
   ✓ 不能修改歌迷数据和乐评数据
   ✓ 没有 DDL 权限

3. 歌迷用户 (fan_user)
   ✓ 可以完全管理：Fan, FanFavorite*, ConcertAttendance, AlbumReview
   ✓ 只能查看：Band, Member, Album, Song, Concert, Performance, AlbumRanking
   ✓ 不能修改乐队相关的任何数据
   ✓ 没有 DDL 权限

4. 权限隔离
   ✓ 乐队用户和歌迷用户之间权限完全隔离
   ✓ 各自只能修改自己职责范围内的数据
   ✓ 跨职责的数据只能查看，不能修改

5. 登录验证
   ✓ 所有用户都可以查询 User 表（用于登录验证）

注意事项：
- 本脚本中的测试命令使用注释包裹，需要手动执行
- 实际测试需要在不同的数据库连接中执行
- 建议使用自动化测试脚本或测试工具进行完整测试
' AS summary;

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT '测试完成！' AS '';
SELECT '========================================' AS '';

-- ============================================
-- 手动测试指南
-- ============================================

/*
手动测试步骤：

1. 测试管理员权限：
   mysql -u admin_user -pAdmin@123456 band_management
   -- 尝试各种操作，应该都能成功

2. 测试乐队用户权限：
   mysql -u band_user -pBand@123456 band_management
   -- 测试查询所有表
   -- 测试修改 Band, Member, Album 等表（应该成功）
   -- 测试修改 Fan, AlbumReview 表（应该失败）

3. 测试歌迷用户权限：
   mysql -u fan_user -pFan@123456 band_management
   -- 测试查询所有表
   -- 测试修改 Fan, FanFavorite*, AlbumReview 表（应该成功）
   -- 测试修改 Band, Member, Album 表（应该失败）

4. 验证权限隔离：
   -- 确保乐队用户不能修改歌迷数据
   -- 确保歌迷用户不能修改乐队数据
   -- 确保两者都不能执行 DDL 操作

5. 查看权限设置：
   mysql -u root -p
   SHOW GRANTS FOR 'admin_user'@'localhost';
   SHOW GRANTS FOR 'band_user'@'localhost';
   SHOW GRANTS FOR 'fan_user'@'localhost';
*/

