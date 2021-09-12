package lava.watcher.rule;

import lava.watcher.model.Record;

/**
 * @Auther: lava
 * @Date: 2021/9/7 14:27
 * @Description: 报警规则
 */
public interface AlertRule {
    boolean isEffective(Record<?> record);
}
