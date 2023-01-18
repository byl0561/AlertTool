package lava.watcher.handler;

import lava.watcher.model.Record;

/**
 * @Auther: lava
 * @Date: 2021/9/7 18:32
 * @Description: 报警流程处理器
 */
public interface AlertHandler {
    void report(Record<?> record);
}
