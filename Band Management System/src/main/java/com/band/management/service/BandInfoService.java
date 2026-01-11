package com.band.management.service;

import com.band.management.entity.Band;
import com.band.management.vo.BandVO;

/**
 * 乐队信息服务接口
 * 
 * @author Band Management Team
 */
public interface BandInfoService {
    
    /**
     * 获取当前乐队信息
     */
    Band getCurrentBandInfo(Long bandId);
    
    /**
     * 获取当前乐队详细信息（包含成员列表）
     */
    BandVO getCurrentBandDetail(Long bandId);
    
    /**
     * 更新乐队信息
     */
    void updateBandInfo(Long bandId, Band band);
}
