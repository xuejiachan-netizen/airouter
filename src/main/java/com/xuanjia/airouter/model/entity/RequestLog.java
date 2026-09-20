package com.xuanjia.airouter.model.entity;


import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("request_log")
public class RequestLog implements Serializable {
    @Serial
    private static  final long serialVersionUID = 1L;

    @Id(keyType =KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 用户 ID
     */
    @Column("userId")
    private Long userId;

    /**
     * API KEY id
     */
    @Column("apiKeyId")
    private Long apiKeyId;

    /**
     * 使用的模型名称
     */
    @Column("modelName")
    private String modelName;

    /**
     * 输入 token 数
     */
    @Column("promptTokens")
    private Integer promptTokens;

    /**
     * 输出 token 数
     */
    @Column("completionTokens")
    private Integer completionTokens;

    @Column("totalTokens")
    private Integer totalTokens;

    /**
     * 请求耗时（毫秒）
     */
    @Column("duration")
    private Integer duration;

    /**
     * 状态: success/ failed
     */
    @Column("status")
    private String status;

    /**
     * 错误信息
     */
    @Column("errorMessage")
    private String errorMessage;

    /**
     * 创建时间
     */
    @Column("userId")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("updateTime")
    private LocalDateTime updateTime;
}
