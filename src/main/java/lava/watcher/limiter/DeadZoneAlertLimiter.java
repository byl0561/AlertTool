package lava.watcher.limiter;

import lava.watcher.model.Record;
import lava.watcher.util.TimerUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Auther: lava
 * @Date: 2021/9/7 20:55
 * @Description:
 */
@Slf4j
public class DeadZoneAlertLimiter implements AlertLimiter{
    public DeadZoneAlertLimiter(long deadSeconds) {
        this.deadSeconds = deadSeconds;
    }

    public static final int ORDER = Integer.MAX_VALUE;

    private final AtomicBoolean flag = new AtomicBoolean(true);
    private final long deadSeconds;

    @Override
    public boolean limit(Record<?> record) {
        if (flag.compareAndSet(true, false)){
            TimerUtil.unLimitedSchedule(deadSeconds * 1000, () -> {
                flag.set(true);
            });
            log.info("[DeadZoneAlertLimiter] limiter pass, indicator:{}, value:{}", record.getIndicator(), record.getValue());
            return true;
        }
        log.info("[DeadZoneAlertLimiter] limiter block, indicator:{}, value:{}", record.getIndicator(), record.getValue());
        return false;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
