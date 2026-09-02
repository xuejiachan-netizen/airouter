package com.xuanjia.airouter.model.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String role;

    private String content;

}
