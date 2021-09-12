package lava.watcher.rule;

import lava.watcher.alerthandler.AlertHandler;
import lava.watcher.model.OperationCondition;
import lava.watcher.model.Record;
import lava.watcher.storage.RecordBase;
import lava.watcher.util.TimerUtil;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

/**
 * @Auther: lava
 * @Date: 2021/9/7 19:49
 * @Description:
 */
@Slf4j
public class OperatorAndTimeRangeMixedAlertRule<T extends Comparable<T>> implements AlertRule, TimeRangeable, Operateable{
    public OperatorAndTimeRangeMixedAlertRule(long seconds, long times, RecordBase recordBase, AlertHandler handler, OperationCondition<T> condition) {
        this.seconds = seconds;
        this.times = times;
        this.recordBase = recordBase;
        this.handler = handler;
        this.condition = condition;
    }

    private final long seconds;
    private final long times;
    private final RecordBase recordBase;
    private final AlertHandler handler;
    private final OperationCondition<T> condition;

    @Override
    public boolean isEffective(Record<?> record) {
        TimerUtil.limitedSchedule("#operatorAndTimeRangeAlertRule_" + record.getIndicator(), seconds * 1000, () -> {
            Collection<Record<?>> result = recordBase.query(record.getIndicator(),
                    record.getTimeStamp(), record.getTimeStamp() + seconds * 1000, condition);
            if (result.size() >= times){
                log.info("[OperatorAndTimeRangeMixedAlertRule] rule schedule active, indicator:{}, value:{}", record.getIndicator(), record.getValue());
                handler.report(new Record<>(new Date(record.getTimeStamp() + seconds * 1000), record.getIndicator(), new BigDecimal(result.size())));
            }
        }, null);
        log.info("[OperatorAndTimeRangeMixedAlertRule] rule block, indicator:{}, value:{}", record.getIndicator(), record.getValue());
        return false;
    }

    @Override
    public long getTimeRange() {
        return seconds;
    }

    @Override
    public OperationCondition<?> getOperationCondition() {
        return condition;
    }
}
