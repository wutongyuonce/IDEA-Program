package com.band.management.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册响应
 * 
 * @author Band Management Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 生成的用户名
     */
    private String username;
    
    /**
     * 关联ID（乐队ID或歌迷ID）
     */
    private Long relatedId;
    
    /**
     * 角色
     */
    private String role;
}
