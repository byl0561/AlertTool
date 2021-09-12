package lava.watcher.model;

import lava.watcher.constant.OperatorEnum;
import lava.watcher.util.AssertUtil;
import lava.watcher.util.OperatorUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Auther: lava
 * @Date: 2021/9/7 16:23
 * @Description: 查询条件
 */
@Data
@AllArgsConstructor
public class OperationCondition<T extends Comparable<T>> {
    private OperatorEnum operator;
    private T value;

    public boolean isCondition(Record<?> record){
        AssertUtil.isTrue(value.getClass().equals(record.getValue().getClass()));
        return OperatorUtil.operate(operator, (T) record.getValue(), value);
    }
}
