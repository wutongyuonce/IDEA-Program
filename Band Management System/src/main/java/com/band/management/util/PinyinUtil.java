package com.band.management.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * 拼音转换工具类
 * 
 * @author Band Management Team
 */
public class PinyinUtil {

    /**
     * 将中文转换为拼音
     * 中文字符转换为拼音，英文和数字保持不变，其他字符移除
     * 
     * @param text 输入文本
     * @return 拼音字符串（小写，无音调）
     */
    public static String toPinyin(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE); // 小写
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE); // 无音调
        format.setVCharType(HanyuPinyinVCharType.WITH_V); // ü用v表示

        StringBuilder result = new StringBuilder();

        for (char c : text.toCharArray()) {
            // 如果是中文字符
            if (isChinese(c)) {
                try {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        // 取第一个拼音（多音字取第一个）
                        result.append(pinyinArray[0]);
                    } else {
                        // 如果转换失败，保留原字符
                        result.append(c);
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    // 转换失败，保留原字符
                    result.append(c);
                }
            }
            // 如果是英文字母或数字，保留
            else if (isEnglishOrDigit(c)) {
                result.append(Character.toLowerCase(c));
            }
            // 其他字符（空格、特殊符号等）忽略
        }

        return result.toString();
    }

    /**
     * 判断字符是否为中文
     * 
     * @param c 字符
     * @return 是否为中文
     */
    private static boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;
    }

    /**
     * 判断字符是否为英文字母或数字
     * 
     * @param c 字符
     * @return 是否为英文字母或数字
     */
    private static boolean isEnglishOrDigit(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
    }

    /**
     * 获取中文首字母
     * 
     * @param text 输入文本
     * @return 首字母字符串（大写）
     */
    public static String getFirstLetters(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);

        StringBuilder result = new StringBuilder();

        for (char c : text.toCharArray()) {
            if (isChinese(c)) {
                try {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        result.append(pinyinArray[0].charAt(0));
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    // 转换失败，忽略
                }
            } else if (isEnglishOrDigit(c)) {
                result.append(Character.toUpperCase(c));
            }
        }

        return result.toString();
    }
}
