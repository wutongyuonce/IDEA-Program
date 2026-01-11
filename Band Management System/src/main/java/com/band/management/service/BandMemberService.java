package com.band.management.service;

import com.band.management.common.PageResult;
import com.band.management.entity.Member;
import java.util.List;

/**
 * 乐队成员服务接口
 * 
 * @author Band Management Team
 */
public interface BandMemberService {
    
    /**
     * 获取本乐队成员列表
     */
    List<Member> getMyBandMembers(Long bandId);
    
    /**
     * 分页查询本乐队成员
     */
    PageResult<Member> page(Long bandId, int pageNum, int pageSize, Member condition);
    
    /**
     * 添加成员
     */
    Long addMember(Long bandId, Member member);
    
    /**
     * 更新成员信息
     */
    void updateMember(Long bandId, Member member);
    
    /**
     * 删除成员
     */
    void deleteMember(Long bandId, Long memberId);
    
    /**
     * 设置队长
     */
    void setLeader(Long bandId, Long memberId);
    
    /**
     * 根据ID查询成员
     */
    Member getById(Long bandId, Long memberId);
}
