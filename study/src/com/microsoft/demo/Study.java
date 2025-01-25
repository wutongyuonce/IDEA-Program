package com.microsoft.demo;

public interface Study {    //使用interface表示这是一个接口
    // 常量
    String Subject = "math";

    // 抽象方法
    int getStudyTime();

    // 静态方法
    static boolean isStudyStart(int time) {
        return time>0;
    };

    // 默认方法
    default void study() {

    };
}
