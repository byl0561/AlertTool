package lava.watcher.client;

import lava.watcher.handler.AlertHandler;
import lava.watcher.filtration.ReportFiltration;
import lava.watcher.model.Record;
import lava.watcher.storage.RecordBase;
import lava.watcher.util.AssertUtil;
import lava.watcher.util.TimerUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * @Auther: lava
 * @Date: 2021/9/6 16:24
 * @Description: 报警工具实现类
 */
@Slf4j
public class AlertWatcherClient {
    public AlertWatcherClient(@NonNull Map<String, List<ReportFiltration>> reportFiltrationMap, @NonNull Map<String, List<AlertHandler>> alertHandlerMap,
                              Map<String, Long> cleanScheduleTable, @NonNull RecordBase recordBase, @NonNull ExecutorService executorService) {
        this.reportFiltrationMap = reportFiltrationMap;
        this.alertHandlerMap = alertHandlerMap;
        this.cleanScheduleTable = cleanScheduleTable;
        this.recordBase = recordBase;
        this.executorService = executorService;
        if (Objects.nonNull(cleanScheduleTable)){
            registerAlertTableCleanSchedule(this.cleanScheduleTable, 10, 1);
        }
    }


    private final Map<String, List<ReportFiltration>> reportFiltrationMap;
    private final Map<String, List<AlertHandler>> alertHandlerMap;
    private final Map<String, Long> cleanScheduleTable;
    private final RecordBase recordBase;

    private final ExecutorService executorService;

    public boolean report(@NonNull String indicator){
        Record<Boolean> record = new Record<>(new Date(), indicator, true);
        if (!appendRecord(record)){
            return false;
        }
        submit(record);
        return true;
    }

    public boolean report(@NonNull String indicator, @NonNull Boolean value){
        Record<Boolean> record = new Record<>(new Date(), indicator, value);
        if (!appendRecord(record)){
            return false;
        }
        submit(record);
        return true;
    }

    public boolean report(@NonNull String indicator, @NonNull String value){
        Record<String> record = new Record<>(new Date(), indicator, value);
        if (!appendRecord(record)){
            return false;
        }
        submit(record);
        return true;
    }

    public boolean report(@NonNull String indicator, @NonNull BigDecimal value){
        Record<BigDecimal> record = new Record<>(new Date(), indicator, value);
        if (!appendRecord(record)){
            return false;
        }
        submit(record);
        return true;
    }

    private void registerAlertTableCleanSchedule(@NonNull Map<String, Long> cleanScheduleTable, long gap, int rate){
        cleanScheduleTable.forEach((key, value) -> TimerUtil.unLimitedScheduleAtFixedRate((rate + 1) * (value + gap) * 1000,
                () -> recordBase.clean(key, new Date().getTime() - (value + gap) * 1000)));
    }

    private boolean appendRecord(@NonNull Record<?> record){
        AssertUtil.isTrue(reportFiltrationMap.containsKey(record.getIndicator()));
        List<ReportFiltration> filtrations = reportFiltrationMap.get(record.getIndicator());
        if (Objects.nonNull(filtrations) && filtrations.stream().noneMatch(filtration -> filtration.filter(record))){
            return false;
        }
        return recordBase.append(record, 3);
    }

    private void submit(@NonNull Record<?> record){
        AssertUtil.isTrue(alertHandlerMap.containsKey(record.getIndicator()));
        List<AlertHandler> handlers = alertHandlerMap.get(record.getIndicator());
        if (Objects.nonNull(handlers)){
            handlers.forEach(handler -> {
                try {
                    executorService.execute(() ->
                            handler.report(new Record<>(new Date(record.getTimeStamp()), record.getIndicator(), record.getValue())));
                }
                catch (RejectedExecutionException e) {
                    log.error("[AlertWatcherClient] thread pool is full, indicator:{}, value:{}", record.getIndicator(), record.getValue());
                }
            });
        }
    }
}
