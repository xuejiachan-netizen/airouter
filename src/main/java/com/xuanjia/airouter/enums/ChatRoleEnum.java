package com.xuanjia.airouter.enums;

public enum ChatRoleEnum {
    ASSISTANT("assistant"),
    USER("user"),
    SYSTEM("system"),
    FUNCTION("function");

    private final String value;

    ChatRoleEnum(final String value){
        this.value = value;
    }

    public String value(){
        return this.value;
    }
}
