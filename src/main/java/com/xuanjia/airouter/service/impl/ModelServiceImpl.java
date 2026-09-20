package com.xuanjia.airouter.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xuanjia.airouter.enums.HealthStatusEnum;
import com.xuanjia.airouter.mapper.ModelMapper;
import com.xuanjia.airouter.model.entity.Model;
import com.xuanjia.airouter.service.ModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ModelServiceImpl extends ServiceImpl<ModelMapper, Model> implements ModelService {

    @Override
    public List<Model> listHealthModel() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("status", HealthStatusEnum.HEALTHY.getValue())
                .orderBy("priority", true);
        return this.list(queryWrapper);
    }

    @Override
    public void updateModelMetircs(Model model) {
        if (model.getId() == null){
            log.warn("传入的模型 id 为空，无法保存从请求！");
            return;
        }

        UpdateChain.of(Model.class)
                .set("id", model.getId())
                .set("healthStatus",model.getSuccessRate())
                .set("avgLatency",model.getAvgLatency())
                .set("successRate",model.getSuccessRate())
                .set("score",model.getScore())
                .set("status", model.getStatus());

        log.info("从同步指标模型成功！");
    }
}
