package com.band.management.vo;

import com.band.management.entity.Member;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 乐队信息VO（包含成员列表）
 * 
 * @author Band Management Team
 */
@Data
public class BandVO implements Serializable {

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
     * 队长姓名
     */
    private String leaderName;

    /**
     * 成员人数
     */
    private Integer memberCount;

    /**
     * 成员列表
     */
    private List<Member> members;

    /**
     * 专辑数量
     */
    private Integer albumCount;

    /**
     * 歌曲数量
     */
    private Integer songCount;

    /**
     * 演唱会数量
     */
    private Integer concertCount;

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
}
