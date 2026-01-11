package com.band.management.util;

/**
 * 加密工具类
 * 临时修改：不使用加密，直接比对明文密码
 * 
 * @author Band Management Team
 */
public class EncryptUtil {

    /**
     * 不加密，直接返回原密码
     */
    public static String encryptPassword(String rawPassword) {
        if (StringUtil.isEmpty(rawPassword)) {
            return null;
        }
        return rawPassword;
    }

    /**
     * 直接比对明文密码
     */
    public static boolean matchesPassword(String rawPassword, String storedPassword) {
        if (StringUtil.isEmpty(rawPassword) || StringUtil.isEmpty(storedPassword)) {
            return false;
        }
        return rawPassword.equals(storedPassword);
    }
}
