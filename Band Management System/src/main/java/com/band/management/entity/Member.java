package com.band.management.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 成员实体类
 * 
 * @author Band Management Team
 */
@Data
public class Member implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成员ID
     */
    private Long memberId;

    /**
     * 人员ID（用于标识同一个人的多条成员记录）
     */
    private Long personId;

    /**
     * 乐队ID
     */
    private Long bandId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别 (M-男, F-女, O-其他)
     */
    private String gender;

    /**
     * 出生日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date birthDate;

    /**
     * 乐队分工
     */
    private String role;

    /**
     * 加入时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date joinDate;

    /**
     * 离开时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date leaveDate;

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
     * 乐队名称（非数据库字段，用于显示）
     */
    private String bandName;
}
