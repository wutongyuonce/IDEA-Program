package com.band.management.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 歌迷实体类
 * 
 * @author Band Management Team
 */
@Data
public class Fan implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 歌迷ID
     */
    private Long fanId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别 (M-男, F-女, O-其他)
     */
    private String gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 职业
     */
    private String occupation;

    /**
     * 学历
     */
    private String education;

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
