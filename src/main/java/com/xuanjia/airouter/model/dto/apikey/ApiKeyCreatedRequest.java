package com.xuanjia.airouter.model.dto.apikey;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ApiKeyCreatedRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Key 名称/备注
     */
    private String keyName;
}
