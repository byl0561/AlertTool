package lava.watcher.rule;

import lava.watcher.model.OperationCondition;
import lava.watcher.model.Record;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * @Auther: lava
 * @Date: 2021/9/7 15:40
 * @Description:
 */
@Slf4j
public class OperatorAlertRule<T extends Comparable<T>> implements AlertRule, Operateable {
    public OperatorAlertRule(@NonNull OperationCondition<T> condition) {
        this.condition = condition;
    }

    private final OperationCondition<T> condition;

    @Override
    public boolean isEffective(@NonNull Record<?> record) {
        if (condition.isCondition(record)) {
            log.info("[OperatorAlertRule] rule pass, indicator:{}, value:{}", record.getIndicator(), record.getValue());
            return true;
        }
        else {
            log.info("[OperatorAlertRule] rule block, indicator:{}, value:{}", record.getIndicator(), record.getValue());
            return false;
        }
    }

    @Override
    public OperationCondition<?> getOperationCondition() {
        return condition;
    }
}
