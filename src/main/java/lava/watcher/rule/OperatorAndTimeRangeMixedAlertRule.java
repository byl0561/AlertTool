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
    public OperatorAndTimeRangeMixedAlertRule(long seconds, long times, RecordBase recordBase, OperationCondition<T> condition) {
        this.seconds = seconds;
        this.times = times;
        this.recordBase = recordBase;
        this.condition = condition;
    }

    private final long seconds;
    private final long times;
    private final RecordBase recordBase;
    private final OperationCondition<T> condition;

    @Override
    public long getTimeRange() {
        return seconds;
    }

    @Override
    public OperationCondition<?> getOperationCondition() {
        return condition;
    }

    @Override
    public Record<?> alertRecord(Record<?> record) {
        Collection<Record<?>> result = recordBase.query(record.getIndicator(),
                record.getTimeStamp() - seconds * 1000, record.getTimeStamp(), condition);
        if (result.size() >= times){
            log.info("[OperatorAndTimeRangeMixedAlertRule] rule schedule active, indicator:{}, value:{}", record.getIndicator(), record.getValue());
            return new Record<>(new Date(record.getTimeStamp()), record.getIndicator(), new BigDecimal(result.size()));
        }
        else {
            log.info("[OperatorAndTimeRangeMixedAlertRule] rule block, indicator:{}, value:{}", record.getIndicator(), record.getValue());
            return null;
        }
    }
}
