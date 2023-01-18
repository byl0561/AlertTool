package lava.watcher.builder;

import lava.watcher.handler.AlertHandler;
import lava.watcher.handler.DefaultAlertHandler;
import lava.watcher.constant.*;
import lava.watcher.executor.AlertExecutor;
import lava.watcher.executor.LarkAlertExecutor;
import lava.watcher.filtration.ReportFiltration;
import lava.watcher.filtration.ValueRangeFiltration;
import lava.watcher.filtration.ValueTypeFiltration;
import lava.watcher.limiter.AlertLimiter;
import lava.watcher.limiter.DeadZoneAlertLimiter;
import lava.watcher.limiter.ImmediateAlertLimiter;
import lava.watcher.limiter.TimeWindowAlertLimiter;
import lava.watcher.model.OperationCondition;
import lava.watcher.rule.*;
import lava.watcher.storage.RecordBase;
import lava.watcher.util.AssertUtil;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Auther: lava
 * @Date: 2021/9/9 11:20
 * @Description: AlertHandler构造器
 */
public class AlertDefinitionBuilder {
    public AlertDefinitionBuilder(@NonNull String name, @NonNull ValueTypeEnum valueTypeEnum, @NonNull RecordBase recordBase) {
        this.name = name;
        this.valueTypeEnum = valueTypeEnum;
        this.recordBase = recordBase;
    }

    private final String name;
    private final RecordBase recordBase;
    private final ValueTypeEnum valueTypeEnum;

    private AlertRule alertRule;
    private List<AlertLimiter> alertLimiters;
    private List<AlertExecutor> alertExecutors;
    private ReportFiltration reportFiltration;
    private Long cleanScheduleTime;

    private RuleTypeEnum ruleTypeEnum = RuleTypeEnum.IMMEDIATE;
    private OperationCondition<?> operationCondition;
    private Long seconds;
    private Long counts;

    private List<LimiterTypeEnum> limiterTypeList = new ArrayList<>();
    private Long timeWindow;
    private Long deadZone;

    private List<ExecutorTypeEnum> executorTypeList = new ArrayList<>();
    private String larkWebhookUri;

    public AlertDefinitionBuilder setImmediateAlertRule(){
        ruleTypeEnum = RuleTypeEnum.IMMEDIATE;
        return this;
    }

    public <T extends Comparable<T>> AlertDefinitionBuilder setOperatorAlertRule(@NonNull OperatorEnum operator, @NonNull T value){
        ruleTypeEnum = RuleTypeEnum.OPERATOR;
        AssertUtil.isTrue(ValueTypeEnum.STRING.equals(valueTypeEnum) || ValueTypeEnum.BOOLEAN.equals(valueTypeEnum) ||
                ValueTypeEnum.NUMBER.equals(valueTypeEnum));
        switch (valueTypeEnum){
            case STRING:
                operationCondition = new OperationCondition<>(operator, value.toString());
                break;
            case NUMBER:
                operationCondition = new OperationCondition<>(operator, value instanceof BigDecimal ? (BigDecimal) value : new BigDecimal(value.toString()));
                break;
            case BOOLEAN:
                operationCondition = new OperationCondition<>(operator, (Boolean) value);
        }
        return this;
    }

    public AlertDefinitionBuilder setTimeRangeAlertRule(long seconds, long counts){
        ruleTypeEnum = RuleTypeEnum.TIME_RANGE;
        this.seconds = seconds;
        this.counts = counts;
        return this;
    }

    public <T extends Comparable<T>> AlertDefinitionBuilder setOperatorAndTimeRangeAlertRule(
            @NonNull OperatorEnum operator, @NonNull T value, long seconds, long counts){
        ruleTypeEnum = RuleTypeEnum.OPERATOR_AND_TIME_RANGE;
        AssertUtil.isTrue(ValueTypeEnum.STRING.equals(valueTypeEnum) || ValueTypeEnum.BOOLEAN.equals(valueTypeEnum) ||
                ValueTypeEnum.NUMBER.equals(valueTypeEnum));
        switch (valueTypeEnum){
            case STRING:
                operationCondition = new OperationCondition<>(operator, value.toString());
                break;
            case NUMBER:
                operationCondition = new OperationCondition<>(operator, value instanceof BigDecimal ? (BigDecimal) value : new BigDecimal(value.toString()));
                break;
            case BOOLEAN:
                operationCondition = new OperationCondition<>(operator, (Boolean) value);
        }
        this.seconds = seconds;
        this.counts = counts;
        return this;
    }

    public AlertDefinitionBuilder addImmediateAlertLimiter(){
        limiterTypeList.add(LimiterTypeEnum.IMMEDIATE);
        return this;
    }

    public AlertDefinitionBuilder addTimeWindowAlertLimiter(long timeWindow){
        limiterTypeList.add(LimiterTypeEnum.TIME_WINDOW);
        this.timeWindow = timeWindow;
        return this;
    }

    public AlertDefinitionBuilder addDeadZoneAlertLimiter(long deadZone){
        limiterTypeList.add(LimiterTypeEnum.DEAD_ZONE);
        this.deadZone = deadZone;
        return this;
    }

    public AlertDefinitionBuilder addLarkAlertExecutor(@NonNull String larkWebhookUri){
        executorTypeList.add(ExecutorTypeEnum.LARK);
        this.larkWebhookUri = larkWebhookUri;
        return this;
    }

    public AlertHandler buildAlertHandler(){
        return new DefaultAlertHandler(buildAlertRule(), buildAlertLimiters(), buildAlertExcutors());
    }

    private AlertRule buildAlertRule(){
        if (Objects.nonNull(alertRule)){
            return alertRule;
        }
        if (Objects.isNull(ruleTypeEnum)){
            ruleTypeEnum = RuleTypeEnum.IMMEDIATE;
        }
        switch (ruleTypeEnum){
            case IMMEDIATE:
                alertRule = new ImmediateAlertRule();
                break;
            case OPERATOR:
                AssertUtil.isTrue(!ValueTypeEnum.NONE.equals(valueTypeEnum));
                AssertUtil.isTrue(!(ValueTypeEnum.BOOLEAN.equals(valueTypeEnum) && !OperatorEnum.EQUAL.equals(operationCondition.getOperator())));
                AssertUtil.isTrue(!(ValueTypeEnum.STRING.equals(valueTypeEnum) && !OperatorEnum.EQUAL.equals(operationCondition.getOperator())));
                alertRule = new OperatorAlertRule<>(operationCondition);
                break;
            case TIME_RANGE:
                alertRule = new TimeRangeAlertRule(seconds, counts, recordBase);
                break;
            case OPERATOR_AND_TIME_RANGE:
                AssertUtil.isTrue(!ValueTypeEnum.NONE.equals(valueTypeEnum));
                AssertUtil.isTrue(!(ValueTypeEnum.BOOLEAN.equals(valueTypeEnum) && !OperatorEnum.EQUAL.equals(operationCondition.getOperator())));
                AssertUtil.isTrue(!(ValueTypeEnum.STRING.equals(valueTypeEnum) && !OperatorEnum.EQUAL.equals(operationCondition.getOperator())));
                alertRule = new OperatorAndTimeRangeMixedAlertRule<>(seconds, counts, recordBase, operationCondition);
                break;
        }
        return alertRule;
    }

    private List<AlertLimiter> buildAlertLimiters() {
        if (Objects.nonNull(alertLimiters)){
            return alertLimiters;
        }
        if (limiterTypeList.isEmpty()){
            limiterTypeList.add(LimiterTypeEnum.IMMEDIATE);
        }
        limiterTypeList = limiterTypeList.stream().distinct().collect(Collectors.toList());
        List<AlertLimiter> alertLimiters = new ArrayList<>();
        limiterTypeList.forEach(limiterTypeEnum -> {
            switch (limiterTypeEnum){
                case IMMEDIATE:
                    alertLimiters.add(new ImmediateAlertLimiter());
                    break;
                case TIME_WINDOW:
                    alertLimiters.add(new TimeWindowAlertLimiter(timeWindow, r -> {
                        for (AlertLimiter limiter : alertLimiters){
                            if (limiter.getOrder() > TimeWindowAlertLimiter.ORDER && !limiter.limit(r)){
                                return;
                            }
                        }
                        buildAlertExcutors().forEach(alertExecutor -> alertExecutor.execute(r));
                    }));
                    break;
                case DEAD_ZONE:
                    alertLimiters.add(new DeadZoneAlertLimiter(deadZone));
                    break;
            }
        });
        alertLimiters.sort(Comparator.comparingInt(AlertLimiter::getOrder));
        this.alertLimiters = alertLimiters;
        return alertLimiters;
    }

    private List<AlertExecutor> buildAlertExcutors() {
        if (Objects.nonNull(alertExecutors)){
            return alertExecutors;
        }
        AssertUtil.isTrue(!executorTypeList.isEmpty());
        executorTypeList = executorTypeList.stream().distinct().collect(Collectors.toList());
        List<AlertExecutor> alertExecutors = new ArrayList<>();
        executorTypeList.forEach(executorTypeEnum -> {
            switch (executorTypeEnum){
                case LARK:
                    alertExecutors.add(new LarkAlertExecutor(larkWebhookUri, name));
                    break;
            }
        });
        this.alertExecutors = alertExecutors;
        return alertExecutors;
    }

    public ReportFiltration buildReportFilter(){
        if (Objects.nonNull(reportFiltration)){
            return reportFiltration;
        }
        ReportFiltration head = new ValueTypeFiltration(valueTypeEnum);
        ReportFiltration node = head;
        if (ValueTypeEnum.NONE.equals(valueTypeEnum)){
            node.setNextFiltration(new ValueRangeFiltration<>(new OperationCondition<>(OperatorEnum.EQUAL, true)));
            node = node.getNextFiltration();
        }
        if (buildAlertRule() instanceof Operateable){
            node.setNextFiltration(new ValueRangeFiltration<>(operationCondition));
            node = node.getNextFiltration();
        }
        reportFiltration = head;
        return head;
    }

    public Long buildScheduleTime(){
        if (Objects.nonNull(cleanScheduleTime)){
            return cleanScheduleTime;
        }
        if (buildAlertRule() instanceof TimeRangeable){
            cleanScheduleTime = seconds;
        }
        else {
            cleanScheduleTime = 0L;
        }
        return cleanScheduleTime;
    }
}
