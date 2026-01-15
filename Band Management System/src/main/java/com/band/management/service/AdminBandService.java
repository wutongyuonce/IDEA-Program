package com.band.management.service;

import com.band.management.common.PageResult;
import com.band.management.entity.Band;
import com.band.management.vo.BandVO;

import java.util.List;

/**
 * 管理员乐队服务接口
 * 
 * @author Band Management Team
 */
public interface AdminBandService {

    /**
     * 创建乐队
     * 
     * @param band 乐队信息
     * @return 乐队ID
     */
    Long create(Band band);

    /**
     * 删除乐队
     * 
     * @param bandId 乐队ID
     */
    void delete(Long bandId);

    /**
     * 更新乐队信息
     * 
     * @param band 乐队信息
     */
    void update(Band band);

    /**
     * 根据ID查询乐队
     * 
     * @param bandId 乐队ID
     * @return 乐队信息
     */
    Band getById(Long bandId);

    /**
     * 查询乐队详情（包含成员列表）
     * 
     * @param bandId 乐队ID
     * @return 乐队详情
     */
    BandVO getDetailById(Long bandId);

    /**
     * 分页查询乐队列表
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param condition 查询条件
     * @return 分页结果
     */
    PageResult<Band> page(int pageNum, int pageSize, Band condition);

    /**
     * 查询所有乐队
     * 
     * @return 乐队列表
     */
    List<Band> list();

    /**
     * 设置乐队队长
     * 
     * @param bandId 乐队ID
     * @param memberId 成员ID
     */
    void setLeader(Long bandId, Long memberId);

    /**
     * 解散乐队
     * 设置乐队为已解散状态，并将所有未离队成员设置为已离队
     * 
     * @param bandId 乐队ID
     */
    void disband(Long bandId);
}
