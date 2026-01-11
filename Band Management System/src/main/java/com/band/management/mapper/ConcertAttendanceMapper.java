package com.band.management.mapper;

import com.band.management.entity.Concert;
import com.band.management.entity.ConcertAttendance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 演唱会参与Mapper接口
 * 
 * @author Band Management Team
 */
@Mapper
public interface ConcertAttendanceMapper {

    /**
     * 插入参与记录
     */
    int insert(ConcertAttendance concertAttendance);

    /**
     * 删除参与记录
     */
    int delete(@Param("fanId") Long fanId, @Param("concertId") Long concertId);

    /**
     * 根据歌迷ID查询参与的演唱会列表
     */
    List<Concert> selectByFanId(@Param("fanId") Long fanId);

    /**
     * 根据演唱会ID查询参与的歌迷列表
     */
    List<ConcertAttendance> selectByConcertId(@Param("concertId") Long concertId);

    /**
     * 检查是否参加该演唱会
     */
    int checkAttendance(@Param("fanId") Long fanId, @Param("concertId") Long concertId);

    /**
     * 统计演唱会参与人数
     */
    int countByConcertId(@Param("concertId") Long concertId);
}
