package com.easy.web.pojo.vo;

public class BanjiCountVO {
    private String name;
    private Integer value;

    public BanjiCountVO() {
    }

    public BanjiCountVO(String name, Integer value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "BanjiCountVO{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
