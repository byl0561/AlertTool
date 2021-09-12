package lava.watcher.filtration;

import lava.watcher.model.OperationCondition;
import lava.watcher.model.Record;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * @Auther: lava
 * @Date: 2021/9/6 17:33
 * @Description: Record符合指定范围
 */
@Slf4j
public class ValueRangeFiltration<T extends Comparable<T>> extends ReportFiltration {
    public ValueRangeFiltration(@NonNull OperationCondition<T> condition) {
        super();
        this.condition = condition;
    }

    private final OperationCondition<T> condition;

    @Override
    protected boolean doFilter(@NonNull Record<?> record) {
        if (condition.isCondition(record)) {
            log.info("[ValueRangeFiltration] filter pass, indicator:{}, value:{}", record.getIndicator(), record.getValue());
            return true;
        }
        else {
            log.info("[ValueRangeFiltration] filter block, indicator:{}, value:{}", record.getIndicator(), record.getValue());
            return false;
        }
    }
}
