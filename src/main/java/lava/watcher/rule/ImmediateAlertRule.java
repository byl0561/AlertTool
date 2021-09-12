package lava.watcher.rule;

import lava.watcher.model.Record;
import lombok.extern.slf4j.Slf4j;

/**
 * @Auther: lava
 * @Date: 2021/9/9 16:14
 * @Description:
 */
@Slf4j
public class ImmediateAlertRule implements AlertRule{
    @Override
    public boolean isEffective(Record<?> record) {
        log.info("[ImmediateAlertRule] rule pass, indicator:{}, value:{}", record.getIndicator(), record.getValue());
        return true;
    }
}
