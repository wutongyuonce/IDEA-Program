package com.band.management.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 乐队实体类
 * 
 * @author Band Management Team
 */
@Data
public class Band implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 乐队ID
     */
    private Long bandId;

    /**
     * 乐队名称
     */
    private String name;

    /**
     * 成立时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date foundedAt;

    /**
     * 乐队简介
     */
    private String intro;

    /**
     * 队长成员ID
     */
    private Long leaderMemberId;

    /**
     * 成员人数
     */
    private Integer memberCount;

    /**
     * 是否已解散：N-未解散，Y-已解散
     */
    private String isDisbanded;

    /**
     * 解散日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date disbandedAt;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;

    /**
     * 队长姓名（非数据库字段，用于显示）
     */
    private String leaderName;
}
