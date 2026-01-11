package com.band.management.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 歌迷参加演唱会实体类
 * 
 * @author Band Management Team
 */
@Data
public class ConcertAttendance implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 参加记录ID
     */
    private Long attendId;

    /**
     * 歌迷ID
     */
    private Long fanId;

    /**
     * 演唱会ID
     */
    private Long concertId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;
}
