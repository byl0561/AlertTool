package lava.watcher.storage;

import lava.watcher.model.OperationCondition;
import lava.watcher.model.Record;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

/**
 * @Auther: lava
 * @Date: 2021/9/6 18:40
 * @Description:
 */
@Slf4j
public class JDKRecordBase implements RecordBase{

    private final Map<String, NavigableMap<Long, Record<?>>> recordBase = new ConcurrentHashMap<>();

    @Override
    public boolean append(@NonNull Record<?> record) {
        if (!recordBase.containsKey(record.getIndicator())){
            recordBase.putIfAbsent(record.getIndicator(), new ConcurrentSkipListMap<>());
        }
        Record<?> before = recordBase.get(record.getIndicator()).putIfAbsent(record.getTimeStamp(), record);
        if (Objects.isNull(before)){
            return true;
        }
        else{
            log.warn("[JDKRecordBase] try append record false, indicator:{}, value:{}", record.getIndicator(), record.getValue());
            return false;
        }
    }

    @Override
    public boolean append(Record<?> record, int retryTimes) {
        while (retryTimes > 0){
            if (append(record)){
                return true;
            }
            record.setTimeStamp(record.getTimeStamp() + 1);
            retryTimes--;
        }
        log.error("[JDKRecordBase] record append false, indicator:{}, value:{}", record.getIndicator(), record.getValue());
        return false;
    }

    @Override
    public boolean clean(@NonNull String indicator, long endTimeStamp) {
        if (recordBase.containsKey(indicator)){
            NavigableMap<Long, Record<?>> indicatorBase = recordBase.get(indicator);
            if (Objects.nonNull(indicatorBase)){
                while (!indicatorBase.isEmpty() && indicatorBase.firstKey() < endTimeStamp){
                    indicatorBase.pollFirstEntry();
                }
            }
        }
        log.info("[JDKRecordBase] record cleaned, indicator:{}, cleanedTime:{}, nowTime:{}", indicator, endTimeStamp, new Date().getTime());
        return true;
    }

    @Override
    public Collection<Record<?>> query(@NonNull String indicator, long startTimeStamp, long endTimeStamp) {
        if (recordBase.containsKey(indicator)){
            List<Record<?>> records = new ArrayList<>(recordBase.get(indicator).subMap(startTimeStamp, true, endTimeStamp, true).values());
            log.info("[JDKRecordBase] record query, indicator:{}, size:{}", indicator, records.size());
            return records;
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<Record<?>> query(@NonNull String indicator, long startTimeStamp, long endTimeStamp, @NonNull OperationCondition<?> condition) {
        List<Record<?>> rangeList = (List<Record<?>>) query(indicator, startTimeStamp, endTimeStamp);
        List<Record<?>> records = rangeList.stream().filter(condition::isCondition).collect(Collectors.toList());
        log.info("[JDKRecordBase] record condition query, indicator:{}, size:{}", indicator, records.size());
        return records;
    }
}
