package com.xxl.tool.test.excel.model;

public enum UserSexEnum {

    MALE("男"),

    FEMALE("女");

    private final String desc;

    UserSexEnum(String desc){
        this.desc = desc;
    }

    public String getDesc(){
        return desc;
    }

}
