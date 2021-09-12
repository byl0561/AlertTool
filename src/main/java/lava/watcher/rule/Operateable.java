package lava.watcher.rule;

import lava.watcher.model.OperationCondition;

/**
 * @Auther: lava
 * @Date: 2021/9/9 16:45
 * @Description:
 */
public interface Operateable {
    OperationCondition<?> getOperationCondition();
}
