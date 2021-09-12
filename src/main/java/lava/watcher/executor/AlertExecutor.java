package lava.watcher.executor;

import lava.watcher.model.Record;

/**
 * @Auther: lava
 * @Date: 2021/9/7 14:50
 * @Description: 报警执行器
 */
public interface AlertExecutor {
    void execute(Record<?> record);
}
