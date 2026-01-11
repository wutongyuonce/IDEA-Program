package com.band.management.mapper;

import com.band.management.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 成员Mapper接口
 * 
 * @author Band Management Team
 */
@Mapper
public interface MemberMapper {

    /**
     * 插入成员
     */
    int insert(Member member);

    /**
     * 根据ID删除成员
     */
    int deleteById(@Param("memberId") Long memberId);

    /**
     * 更新成员信息
     */
    int update(Member member);

    /**
     * 根据ID查询成员
     */
    Member selectById(@Param("memberId") Long memberId);

    /**
     * 查询所有成员
     */
    List<Member> selectAll();

    /**
     * 根据乐队ID查询成员列表
     */
    List<Member> selectByBandId(@Param("bandId") Long bandId);

    /**
     * 条件查询成员列表
     */
    List<Member> selectByCondition(Member member);

    /**
     * 统计成员数量
     */
    int count();

    /**
     * 根据条件统计数量
     */
    int countByCondition(Member member);
}
