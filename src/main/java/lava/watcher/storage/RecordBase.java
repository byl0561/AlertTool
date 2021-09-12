package lava.watcher.storage;

import lava.watcher.model.OperationCondition;
import lava.watcher.model.Record;

import java.util.Collection;

/**
 * @Auther: lava
 * @Date: 2021/9/6 18:34
 * @Description: 报警Record存储库
 */
public interface RecordBase {

    boolean append(Record<?> record);
    boolean append(Record<?> record, int retryTimes);
    boolean clean(String id, long endTimeStamp);

    Collection<Record<?>> query(String indicator, long startTimeStamp, long endTimeStamp);
    Collection<Record<?>> query(String indicator, long startTimeStamp, long endTimeStamp, OperationCondition<?> condition);
}
