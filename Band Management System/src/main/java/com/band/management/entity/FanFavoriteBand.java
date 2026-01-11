package com.band.management.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 歌迷喜欢的乐队实体类
 * 
 * @author Band Management Team
 */
@Data
public class FanFavoriteBand implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 歌迷ID
     */
    private Long fanId;

    /**
     * 乐队ID
     */
    private Long bandId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;
}
