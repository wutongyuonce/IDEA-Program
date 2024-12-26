package com.microsoft.demo;

import java.sql.SQLOutput;
import java.util.Locale;

public class new1 {
    public static void main(String[] args){
        String str_1="hello";
        String str_2="world";

        System.out.println("str_1 = "+str_1);
        System.out.println("str_2 = "+str_2);
        System.out.println("我是".concat("jackson"));
        System.out.println("我是"+"jackson");
        System.out.println("str_1 -> length:"+str_1.length());
        for (int i = 0; i < str_1.length(); i++) {
        }

        int a=3;
        System.out.printf("数字是：%d\n",a);
        String fs = String.format("数字是：%d",a);
        System.out.println(fs);

        char[] chars=str_1.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            System.out.println(chars[i]);
        }

        System.out.println(str_1.toLowerCase());
    }
}
