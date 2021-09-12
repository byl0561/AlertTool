package lava.watcher.limiter;

import lava.watcher.model.Record;

/**
 * @Auther: lava
 * @Date: 2021/9/7 14:41
 * @Description: 报警限流器
 */
public interface AlertLimiter {
    boolean limit(Record<?> record);
    int getOrder();
}
