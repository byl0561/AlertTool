package lava.watcher.rule;

import lava.watcher.model.Record;
import lava.watcher.storage.RecordBase;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

/**
 * @Auther: lava
 * @Date: 2021/9/7 16:38
 * @Description:
 */
@Slf4j
public class TimeRangeAlertRule implements AlertRule, TimeRangeable{
    public TimeRangeAlertRule(long seconds, long times, @NonNull RecordBase recordBase) {
        this.seconds = seconds;
        this.times = times;
        this.recordBase = recordBase;
    }

    private final long seconds;
    private final long times;
    private final RecordBase recordBase;
    @Override
    public long getTimeRange() {
        return seconds;
    }

    @Override
    public Record<?> alertRecord(Record<?> record) {
        Collection<Record<?>> result = recordBase.query(record.getIndicator(),
                record.getTimeStamp() - seconds * 1000, record.getTimeStamp());
        if (result.size() >= times){
            log.info("[TimeRangeAlertRule] rule schedule active, indicator:{}, value:{}", record.getIndicator(), record.getValue());
            return new Record<>(new Date(record.getTimeStamp()), record.getIndicator(), new BigDecimal(result.size()));
        }
        else {
            log.info("[TimeRangeAlertRule] rule block, indicator:{}, value:{}", record.getIndicator(), record.getValue());
            return null;
        }
    }
}
