package lava.watcher.filtration;

import lava.watcher.constant.ValueTypeEnum;
import lava.watcher.model.Record;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * @Auther: lava
 * @Date: 2021/9/7 11:59
 * @Description: Record类型
 */
@Slf4j
public class ValueTypeFiltration extends ReportFiltration{
    public ValueTypeFiltration(@NonNull ValueTypeEnum valueType) {
        super();
        this.valueType = valueType.getClz();
    }

    private final Class<?> valueType;

    @Override
    protected boolean doFilter(@NonNull Record<?> record) {
        if (valueType.equals(record.getValue().getClass())){
            log.info("[ValueTypeFiltration] filter pass, indicator:{}, value:{}", record.getIndicator(), record.getValue());
            return true;
        }
        else {
            log.info("[ValueTypeFiltration] filter block, indicator:{}, value:{}", record.getIndicator(), record.getValue());
            return false;
        }
    }
}
