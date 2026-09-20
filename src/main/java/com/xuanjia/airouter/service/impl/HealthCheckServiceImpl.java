package com.xuanjia.airouter.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.mybatisflex.core.query.QueryWrapper;
import com.xuanjia.airouter.enums.HealthStatusEnum;
import com.xuanjia.airouter.mapper.RequestLogMapper;
import com.xuanjia.airouter.model.entity.Model;
import com.xuanjia.airouter.model.entity.ModelProvider;
import com.xuanjia.airouter.service.HealthCheckService;
import com.xuanjia.airouter.service.ModelProviderService;
import com.xuanjia.airouter.service.ModelService;
import jakarta.annotation.Resource;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
//todo 此处代码需要复现
public class HealthCheckServiceImpl implements HealthCheckService  {

    private static final int TIME_OUT = 10000;

    private static final int MAX_SIZE = 100;

    private static final int STARTTIME = 24;

    //用于计算成本权重
    private static final double COST_WEIGHT = 0.3;

    private static final double LATENCY_WEIGHT = 0.3;

    private static final double RATE_WEIGHT = 0.2;

    private static final double PRIORITY_WEIGHT = 0.2;

    private final ConcurrentHashMap<Long, List<Boolean>> historyMap = new ConcurrentHashMap<>();

    @Resource
    private ModelProviderService modelProviderService;

    @Resource
    private RequestLogMapper requestLogMapper;

    @Resource
    private ModelService modelService;
    @Override
    public boolean checkProviderHealth(ModelProvider modelProvider) {
        try {
            //1、发送健康请求
            long start = System.currentTimeMillis();
            boolean healthCheck = this.sendHealthCheck(modelProvider);
            long end = System.currentTimeMillis();
            //2、检查健康应用
            Long providerId = modelProvider.getId();
            this.checkHealthProvider(providerId, healthCheck);
            BigDecimal successRate = this.calculateHealthRate(providerId);
            //3、确定健康状态
            String healthStatus = this.determineHealthStatus(successRate);
            //4、记录健康内容
            modelProviderService.updateHealthStatus(providerId,healthStatus,end - start, successRate);
            return healthCheck;
        }catch (Exception e){
            Long providerId = modelProvider.getId();
            BigDecimal successRate = this.calculateHealthRate(providerId);
            modelProviderService.updateHealthStatus(providerId,HealthStatusEnum.UNHEALTHY.getValue(),0, successRate);
            log.error("检查健康状态出现异常,{}",e.getMessage());
            return false;
        }
    }

    private BigDecimal calculateHealthRate(Long providerId) {
        List<Boolean> list = historyMap.get(providerId);
        List<Boolean> healthList = list.stream().filter(success -> success).toList();
        double successRate = (double) healthList.size() / list.size();
        
        return BigDecimal.valueOf(successRate).setScale(2, RoundingMode.HALF_UP);
    }


    @Override
    public void checkAllProviders() {
        List<ModelProvider> providers = modelProviderService.list();
        if (providers == null){
            return;
        }

        providers.parallelStream()
                .filter(provider -> HealthStatusEnum.HEALTHY.getValue().equals(provider.getHealthStatus()))
                .forEach(this::checkProviderHealth);

        this.syncModelMetricsFromRequestLog();
    }

    @Override
    public Integer measureLatency(ModelProvider modelProvider) {
        try {
            long start = System.currentTimeMillis();
            this.sendHealthCheck(modelProvider);
            long end = System.currentTimeMillis();
            return (int)( end - start );
        }catch (Exception e){
            log.info("测试延迟时间失败！模型:{}",modelProvider.getProviderName());
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public void syncModelMetricsFromRequestLog() {
        //开启从同步指标模型
        log.info("开启从同步指标模型");
        List<Model> healthModels = modelService.listHealthModel();
        if (CollUtil.isEmpty(healthModels)){
            log.warn("没有相关可使用的健康模型！");
            return;
        }

        LocalDateTime startTime = LocalDateTime.now().minusHours(STARTTIME);
        //聚合函数批量查询所有模型的统计数据
        List<ModelStats> statsList = this.normalizationParameters(startTime);

        Map<Long, ModelStats> statsMap = statsList.stream().collect(Collectors.toMap(model -> model.modelId, model -> model, (a, b) -> a));

        //归一化处理
        NormalizationParams normalizationParams = this.calculateNormalizationParams(healthModels, statsMap);

        for (Model healthModel : healthModels) {
            ModelStats modelStats = statsMap.getOrDefault(healthModel.getId(), new ModelStats());

            BigDecimal score = this.calculateScore(healthModel, normalizationParams, modelStats);

            String healthStatus = this.determineHealthStatusBySuccessRate(modelStats.successRate);

            Model model = Model.builder()
                    .id(healthModel.getId())
                    .score(score)
                    .status(healthStatus)
                    .avgLatency(modelStats.avgLatency)
                    .successRate(modelStats.successRate)
                    .build();


            modelService.updateModelMetircs(model);

            log.info("模型{}指标更新，延迟={},成功率={}，状态={}", healthModel.getModelKey(), modelStats.avgLatency, modelStats.successRate, healthStatus);
        }

        //同步更新提供者的汇总指标(基于模型统计)
        this.syncProviderMetircsFromStats(healthModels, statsMap);
        log.info("同步更新提供者汇总指标成功！");
    }



    private boolean sendHealthCheck(ModelProvider modelProvider){
        try {
            String baseUrl = modelProvider.getBaseUrl();
            String apiKey = modelProvider.getApiKey();

            String url = baseUrl + "models/";
            HttpResponse response = HttpRequest.get(url)
                    .header("Authorization", "Bearer" + apiKey)
                    .timeout(TIME_OUT)
                    .execute();

            return response.getStatus() == 200 || response.getStatus() == 401;
        }catch (Exception e){
            return false;
        }
    }

    private void checkHealthProvider(Long providerId, boolean healthStatus) {
        List<Boolean> history = historyMap.computeIfAbsent(providerId, k -> new ArrayList<>());
        history.add(healthStatus);

        if (history.size() == MAX_SIZE){
            history.removeFirst();
        }
    }

    private String determineHealthStatus(BigDecimal successRate) {
        double rate = successRate.doubleValue();
        if (rate >= 80.00){
            return HealthStatusEnum.HEALTHY.getValue();
        }else if (rate >= 50.00){
            return HealthStatusEnum.UNHEALTHY.getValue();
        }else {
            return HealthStatusEnum.UNHEALTHY.getValue();
        }
    }

    private String determineHealthStatusBySuccessRate(BigDecimal successRate) {
        if (successRate == null){
            return HealthStatusEnum.UNKNOWN.getValue();
        }
        double rate = successRate.doubleValue();
        if (rate >= 80.00){
            return HealthStatusEnum.HEALTHY.getValue();
        }else if (rate >= 50.00){
            return HealthStatusEnum.UNHEALTHY.getValue();
        }else {
            return HealthStatusEnum.UNHEALTHY.getValue();
        }
    }

    private List<ModelStats> normalizationParameters(LocalDateTime createTime) {
        /**
         * select
         *      modelId,
         *      count(*) as totalRequests,
         *      avg(case when status = 'success' then duration else null end) avgLatency,
         *      ifnull( sum(when status = 'success' then 1 else 0 end) / ifnull(count(*),0) ,100)
         * from
         *      request_log
         * where createTime >= ? and modelId is not null
         * group by
         *      modelId
         */
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select("modelId",
                        "count(*) as totalRequests",
                        "avg(case when status = 'success' then duration else null end) avgLatency",
                        "ifnull( sum(when status = 'success' then 1 else 0 end) / ifnull(count(*),0) ,100) as successRate")
                .ge("createTime", createTime)
                .isNotNull("modelId")
                .groupBy("modelId");

        //todo 代码 review
        List<Map<String, Object>> results = (List<Map<String, Object>>)(List<?>)requestLogMapper.selectListByQueryAs(queryWrapper, Map.class);

        List<ModelStats> modelStatsList = new ArrayList<>();
        for (Map<String, Object> result : results) {
            Long modelId = ((Number)result.get("modelId")).longValue();
            int totalRequests = ((Number) result.get("totalRequests")).intValue();
            int avgLatency = ((Number) result.get("avgLatency")).intValue();
            BigDecimal successRate = BigDecimal.valueOf(((Number) result.get("successRate")).doubleValue());
            ModelStats modelStats = ModelStats.builder()
                    .modelId(modelId)
                    .totalRequests(totalRequests)
                    .avgLatency(avgLatency)
                    .successRate(successRate)
                    .build();
            modelStatsList.add(modelStats);
        }

        return modelStatsList;
    }


    /**
     * 对于参数的归一化处理，计算模型的 延迟 、消费 、 优先级 范围
     * @param models
     * @param modelStatsMap
     * @return
     */
    private NormalizationParams calculateNormalizationParams(List<Model> models, Map<Long, ModelStats> modelStatsMap){
        BigDecimal mincost = models.stream()
                .map(model -> model.getInputPrice().add(model.getOutputPrice()))
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal maxcost = models.stream()
                .map(model -> model.getInputPrice().add(model.getOutputPrice()))
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ONE);

        int minpriority = models.stream()
                .mapToInt(model -> model.getPriority())
                .min()
                .orElse(0);

        int maxpriority = models.stream()
                .mapToInt(model -> model.getPriority())
                .max()
                .orElse(100);

        int minlatency = modelStatsMap.values().stream()
                .mapToInt(s -> s.avgLatency)
                .min()
                .orElse(0);

        int maxLatency = modelStatsMap.values().stream()
                .mapToInt(s -> s.avgLatency)
                .min()
                .orElse(10000);

        return NormalizationParams.builder()
                .maxcost(maxcost)
                .mincost(mincost)
                .maxLatency(maxLatency)
                .minLatency(minlatency)
                .maxpriority(maxpriority)
                .minpriority(minpriority)
                .build();
    }

    private BigDecimal calculateScore(Model healthModel, NormalizationParams normalizationParams, ModelStats modelStats) {
        //1、成本得分（归一化 0-1）
        BigDecimal modelcost = healthModel.getInputPrice().add(healthModel.getOutputPrice());
        double costscore = this.normalize(modelcost, normalizationParams.mincost, normalizationParams.maxcost);

        //2、延迟得分（归一化0 - 1）
        int latency = modelStats.avgLatency > 0 ? modelStats.avgLatency : 5000;  //默认5s
        double latencyscore = this.normalize(latency, normalizationParams.minLatency, normalizationParams.maxLatency);

        //3、优先级得分(归一化 0-1 )
        double priorityscore = 1.0 - this.normalize(healthModel.getPriority(), normalizationParams.minLatency, normalizationParams.maxLatency);

        //4、成功率得分(归一化 0-1)
        double ratescore = 1.0 - (healthModel.getSuccessRate().doubleValue() / 100.00);

        double score = costscore * COST_WEIGHT + latency * LATENCY_WEIGHT
                + priorityscore * PRIORITY_WEIGHT + ratescore * RATE_WEIGHT;

        return BigDecimal.valueOf(score).setScale(4, RoundingMode.HALF_UP);

    }

    //归一化算法 将数值归一化到( 0 - 1)
    private double normalize(BigDecimal value, BigDecimal mincost, BigDecimal maxcost){
        BigDecimal range = maxcost.subtract(mincost);
        if (range.compareTo(BigDecimal.ZERO) == 0){
            return 0.0;
        }
        return value.subtract(mincost).divide(range).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }

    //归一化算法 将数值归一化到( 0 - 1)
    private double normalize(int latency, int mincost, int maxcost){
        int range = maxcost - mincost;
        if (range == 0){
            return 0.0;
        }
        return (double) (latency - mincost) / range;
    }

    /**
     * 基于模型统计数据同步更新提供者的汇总指标
     */
    private void syncProviderMetircsFromStats(List<Model> healthModels, Map<Long, ModelStats> statsMap) {
        Map<Long, List<Model>> providerModels = healthModels.stream()
                .collect(Collectors.groupingBy(Model::getId));

        for (Map.Entry<Long, List<Model>> modelEntry : providerModels.entrySet()) {
            Long modelKey = modelEntry.getKey();
            List<Model> modelValue = modelEntry.getValue();

            //计算提供者的平均成功率和延迟率
            int totalLatency = 0;
            int latencyCount = 0;
            double totalSuccessRate = 0.0;
            int successRateCount = 0;

            for (Model model : modelValue) {
                ModelStats modelStats = statsMap.get(model.getId());
                if (modelStats != null){
                    if (modelStats.avgLatency != 0){
                        totalLatency += modelStats.avgLatency;
                        latencyCount++;
                    }
                    if (modelStats.successRate != null){
                        totalSuccessRate += modelStats.successRate.doubleValue();
                        successRateCount++;
                    }
                }

                int avgLatency = latencyCount == 0 ? 0 : totalLatency / latencyCount;
                double avgSuccessRate = successRateCount == 0 ? 0.00 : totalSuccessRate / successRateCount;
                BigDecimal decimalRate = BigDecimal.valueOf(avgSuccessRate);

                String status = this.determineHealthStatusBySuccessRate(decimalRate);

                modelProviderService.updateHealthStatus(model.getProviderId(),status,avgLatency,decimalRate);

                log.info("提供者 {} 指标更新: 延迟={}ms, 成功率={}%, 健康状态={}",
                        model.getProviderId(), avgLatency, avgSuccessRate, status);
            }
        }
    }

    /**
     * 模型统计数据内部类
     */
    @Builder
    @Data
    private static class ModelStats{
        Long modelId;
        int totalRequests = 0;
        int avgLatency = 0;
        BigDecimal successRate = BigDecimal.valueOf(100);

        public ModelStats() {

        }
    }

    @Builder
    private  static class NormalizationParams{
        BigDecimal mincost = BigDecimal.ZERO;
        BigDecimal maxcost = BigDecimal.ONE;
        int minLatency = 0;
        int maxLatency = 10000;
        int minpriority = 0;
        int maxpriority = 100;
    }

}
