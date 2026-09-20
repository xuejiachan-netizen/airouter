package com.xuanjia.airouter.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ChatThinkingType {
    ENABLED,
    DISABLED,
    ;

    @JsonValue
    public String getValue(){
        return this.name().toLowerCase();
    }
}
