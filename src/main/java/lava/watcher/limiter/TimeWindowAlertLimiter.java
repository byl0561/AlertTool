package lava.watcher.limiter;

import lava.watcher.alerthandler.AlertHandler;
import lava.watcher.model.Record;
import lava.watcher.util.TimerUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Auther: lava
 * @Date: 2021/9/7 20:31
 * @Description:
 */
@Slf4j
public class TimeWindowAlertLimiter implements AlertLimiter{
    public TimeWindowAlertLimiter(long periodSeconds, @NonNull AlertHandler handler) {
        counter = new AtomicInteger(0);
        this.periodSeconds = periodSeconds;
        this.handler = handler;
    }

    public static final int ORDER = 1000;

    private final AtomicInteger counter;
    private final long periodSeconds;
    private final AlertHandler handler;

    @Override
    public boolean limit(Record<?> record) {
        if (counter.getAndIncrement() == 0){
            TimerUtil.unLimitedSchedule(periodSeconds * 1000, () -> {
                int count = counter.getAndSet(0);
                log.info("[TimeWindowAlertLimiter] limiter schedule active, indicator:{}, value:{}", record.getIndicator(), record.getValue());
                handler.report(new Record<>(new Date(), record.getIndicator(), new BigDecimal(count)));
            });
        }
        log.info("[TimeWindowAlertLimiter] limiter block, indicator:{}, value:{}", record.getIndicator(), record.getValue());
        return false;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
