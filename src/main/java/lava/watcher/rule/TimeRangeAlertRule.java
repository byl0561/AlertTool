package lava.watcher.rule;

import lava.watcher.alerthandler.AlertHandler;
import lava.watcher.model.Record;
import lava.watcher.storage.RecordBase;
import lava.watcher.util.TimerUtil;
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
    public TimeRangeAlertRule(long seconds, long times, @NonNull RecordBase recordBase, @NonNull AlertHandler handler) {
        this.seconds = seconds;
        this.times = times;
        this.recordBase = recordBase;
        this.handler = handler;
    }

    private final long seconds;
    private final long times;
    private final RecordBase recordBase;
    private final AlertHandler handler;

    @Override
    public boolean isEffective(Record<?> record) {
        TimerUtil.limitedSchedule("#timeRangeAlertRule_" + record.getIndicator(), seconds * 1000, () -> {
            Collection<Record<?>> result = recordBase.query(record.getIndicator(),
                    record.getTimeStamp(), record.getTimeStamp() + seconds * 1000);
            if (result.size() >= times){
                log.info("[TimeRangeAlertRule] rule schedule active, indicator:{}, value:{}", record.getIndicator(), record.getValue());
                handler.report(new Record<>(new Date(record.getTimeStamp() + seconds * 1000), record.getIndicator(), new BigDecimal(result.size())));
            }
        }, null);
        log.info("[TimeRangeAlertRule] rule block, indicator:{}, value:{}", record.getIndicator(), record.getValue());
        return false;
    }

    @Override
    public long getTimeRange() {
        return seconds;
    }
}
