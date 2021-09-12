package lava.watcher.limiter;

import lava.watcher.model.Record;
import lombok.extern.slf4j.Slf4j;

/**
 * @Auther: lava
 * @Date: 2021/9/7 20:26
 * @Description:
 */
@Slf4j
public class ImmediateAlertLimiter implements AlertLimiter{
    public static final int ORDER = 0;

    @Override
    public boolean limit(Record<?> record) {
        log.info("[ImmediateAlertLimiter] limiter pass, indicator:{}, value:{}", record.getIndicator(), record.getValue());
        return true;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
