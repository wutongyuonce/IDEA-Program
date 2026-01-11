package com.band.management.exception;

import lombok.Getter;

/**
 * 错误码枚举
 * 
 * @author Band Management Team
 */
@Getter
public enum ErrorCode {

    // 系统错误 1xxx
    SYSTEM_ERROR(1000, "系统错误"),
    PARAM_ERROR(1001, "参数错误"),
    DATA_NOT_FOUND(1002, "数据不存在"),
    DATA_ALREADY_EXISTS(1003, "数据已存在"),
    OPERATION_FAILED(1004, "操作失败"),

    // 认证授权错误 2xxx
    UNAUTHORIZED(2000, "未登录或登录已过期"),
    FORBIDDEN(2001, "没有权限访问"),
    LOGIN_FAILED(2002, "用户名或密码错误"),
    ACCOUNT_DISABLED(2003, "账号已被禁用"),
    TOKEN_INVALID(2004, "Token无效"),

    // 业务错误 3xxx
    BAND_NOT_FOUND(3001, "乐队不存在"),
    MEMBER_NOT_FOUND(3002, "成员不存在"),
    ALBUM_NOT_FOUND(3003, "专辑不存在"),
    SONG_NOT_FOUND(3004, "歌曲不存在"),
    CONCERT_NOT_FOUND(3005, "演唱会不存在"),
    FAN_NOT_FOUND(3006, "歌迷不存在"),
    REVIEW_NOT_FOUND(3007, "乐评不存在"),
    
    REVIEW_ALREADY_EXISTS(3101, "您已经评论过该专辑"),
    MEMBER_TIME_OVERLAP(3102, "成员在同一时间只能加入一个乐队"),
    LEADER_NOT_IN_BAND(3103, "队长必须是该乐队的成员"),
    INVALID_RATING(3104, "评分必须在1-10之间，步长为0.5"),
    INVALID_DATE_RANGE(3105, "离开日期必须大于等于加入日期"),
    
    PERMISSION_DENIED(3201, "您没有权限操作该数据"),
    BAND_PERMISSION_DENIED(3202, "您只能操作自己的乐队数据"),
    FAN_PERMISSION_DENIED(3203, "您只能操作自己的数据");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
