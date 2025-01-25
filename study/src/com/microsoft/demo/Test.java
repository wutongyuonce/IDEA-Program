package com.microsoft.demo;

public class Test {
    private final String name;

    public Test(String name){
        this.name = name;
    }
    public class Inner {
        public void test(){
            System.out.println("我是成员内部类："+name);
        }
    }
    public static void main(String[] args) {
        Test outer = new Test("Hello");
        Inner inner = outer.new Inner();
        inner.test();
    }
}