package com.band.management.service;

import com.band.management.common.PageResult;
import com.band.management.entity.Member;

import java.util.List;

/**
 * 管理员成员服务接口
 * 
 * @author Band Management Team
 */
public interface AdminMemberService {

    /**
     * 创建成员
     * 
     * @param member 成员信息
     * @return 成员ID
     */
    Long create(Member member);

    /**
     * 删除成员
     * 
     * @param memberId 成员ID
     */
    void delete(Long memberId);

    /**
     * 更新成员信息
     * 
     * @param member 成员信息
     */
    void update(Member member);

    /**
     * 根据ID查询成员
     * 
     * @param memberId 成员ID
     * @return 成员信息
     */
    Member getById(Long memberId);

    /**
     * 根据乐队ID查询成员列表
     * 
     * @param bandId 乐队ID
     * @return 成员列表
     */
    List<Member> getByBandId(Long bandId);

    /**
     * 分页查询成员列表
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param condition 查询条件
     * @return 分页结果
     */
    PageResult<Member> page(int pageNum, int pageSize, Member condition);

    /**
     * 查询所有成员
     * 
     * @return 成员列表
     */
    List<Member> list();
}
