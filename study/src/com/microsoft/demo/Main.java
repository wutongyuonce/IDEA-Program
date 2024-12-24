package com.microsoft.demo; //定位你的Java代码放在src文件夹下的位置

//公共的  类   类名（开头字母必须大写和文件名相同）

/**
 * @author jackson
 * @since 1.8
 * @version 1.0
 */
public class Main{
    //main函数的固定格式
    public static void main(String[] args){
        int number_a=1, number_b=2;
        int sum= sum(number_a,number_b);
        System.out.println("sum = " + sum);
        System.out.println("Hello"+sum); //ln相当于c语言中的\n换行
        System.out.println(Math.random());
    }

    /**
     * 该方法传递两个int类型参数，用来求和
     * @param number_a 第一个加数
     * @param number_b 第二个加数
     * @return 返回一个求和后的结果
     */
    public static int sum(int number_a,int number_b){
        return number_a+number_b;
    }
    //函数的调用和C是一样的
}