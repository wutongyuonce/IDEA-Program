package com.band.management.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 拼音转换工具类测试
 * 
 * @author Band Management Team
 */
public class PinyinUtilTest {

    @Test
    public void testToPinyin_ChineseOnly() {
        // 测试纯中文
        assertEquals("zhangyu", PinyinUtil.toPinyin("张宇"));
        assertEquals("lina", PinyinUtil.toPinyin("李娜"));
        assertEquals("wangfang", PinyinUtil.toPinyin("王芳"));
    }

    @Test
    public void testToPinyin_EnglishOnly() {
        // 测试纯英文
        assertEquals("hello", PinyinUtil.toPinyin("Hello"));
        assertEquals("world", PinyinUtil.toPinyin("WORLD"));
        assertEquals("test123", PinyinUtil.toPinyin("Test123"));
    }

    @Test
    public void testToPinyin_Mixed() {
        // 测试中英文混合
        assertEquals("thebeijingledui", PinyinUtil.toPinyin("The北京乐队"));
        assertEquals("rock2024", PinyinUtil.toPinyin("Rock2024"));
        assertEquals("nihaoledui", PinyinUtil.toPinyin("你好乐队"));
    }

    @Test
    public void testToPinyin_WithSpecialChars() {
        // 测试包含特殊字符（应该被移除）
        assertEquals("zhangyu", PinyinUtil.toPinyin("张 宇"));
        assertEquals("theband", PinyinUtil.toPinyin("The-Band"));
        assertEquals("hello", PinyinUtil.toPinyin("Hello!@#"));
    }

    @Test
    public void testToPinyin_Empty() {
        // 测试空字符串
        assertEquals("", PinyinUtil.toPinyin(""));
        assertEquals("", PinyinUtil.toPinyin(null));
    }

    @Test
    public void testGetFirstLetters() {
        // 测试获取首字母
        assertEquals("ZY", PinyinUtil.getFirstLetters("张宇"));
        assertEquals("BJLD", PinyinUtil.getFirstLetters("北京乐队"));
        assertEquals("HELLO", PinyinUtil.getFirstLetters("Hello"));
    }

    @Test
    public void testBandUsername() {
        // 模拟乐队用户名生成
        String bandName1 = "你好乐队";
        String username1 = "band_" + PinyinUtil.toPinyin(bandName1);
        assertEquals("band_nihaoledui", username1);

        String bandName2 = "The Beatles";
        String username2 = "band_" + PinyinUtil.toPinyin(bandName2);
        assertEquals("band_thebeatles", username2);

        String bandName3 = "五月天";
        String username3 = "band_" + PinyinUtil.toPinyin(bandName3);
        assertEquals("band_wuyuetian", username3);
    }

    @Test
    public void testFanUsername() {
        // 模拟歌迷用户名生成
        String fanName1 = "张宇";
        String username1 = "fan_" + PinyinUtil.toPinyin(fanName1);
        assertEquals("fan_zhangyu", username1);

        String fanName2 = "李娜";
        String username2 = "fan_" + PinyinUtil.toPinyin(fanName2);
        assertEquals("fan_lina", username2);

        String fanName3 = "John Smith";
        String username3 = "fan_" + PinyinUtil.toPinyin(fanName3);
        assertEquals("fan_johnsmith", username3);
    }
}
