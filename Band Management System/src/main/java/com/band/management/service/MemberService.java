package com.band.management.service;

import com.band.management.common.PageResult;
import com.band.management.entity.Member;

import java.util.List;

/**
 * 成员服务接口
 * 
 * @author Band Management Team
 */
public interface MemberService {

    /**
     * 创建成员
     */
    Long create(Member member);

    /**
     * 删除成员
     */
    void delete(Long memberId);

    /**
     * 更新成员信息
     */
    void update(Member member);

    /**
     * 根据ID查询成员
     */
    Member getById(Long memberId);

    /**
     * 根据乐队ID查询成员列表
     */
    List<Member> getByBandId(Long bandId);

    /**
     * 分页查询成员列表
     */
    PageResult<Member> page(int pageNum, int pageSize, Member condition);

    /**
     * 查询所有成员
     */
    List<Member> list();
}
