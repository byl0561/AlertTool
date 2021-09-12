package lava.watcher.builder;

import lava.watcher.alerthandler.AlertHandler;
import lava.watcher.client.AlertWatcherClient;
import lava.watcher.constant.ValueTypeEnum;
import lava.watcher.filtration.ReportFiltration;
import lava.watcher.storage.JDKRecordBase;
import lava.watcher.storage.RecordBase;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @Auther: lava
 * @Date: 2021/9/9 11:17
 * @Description: AlertWatcher构造器
 */
public class AlertWatcherBuilder {
    private final long DEFAULT_CLEAN_TIME = 60;

    private final Map<String, List<AlertDefinitionBuilder>> alertDefinitions = new HashMap<>();
    private ExecutorService executorService = new ThreadPoolExecutor(6, 8, 5, TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(128), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
    private RecordBase recordBase = new JDKRecordBase();

    public static AlertWatcherBuilder newAlertWatcherBuilder(){
        return new AlertWatcherBuilder();
    }

    public AlertDefinitionBuilder newAlertHandler(@NonNull String indicator, @NonNull String alertName, @NonNull ValueTypeEnum valueType){
        if (!alertDefinitions.containsKey(indicator)){
            alertDefinitions.putIfAbsent(indicator, new ArrayList<>());
        }
        AlertDefinitionBuilder alertHandlerBuilder = new AlertDefinitionBuilder(alertName, valueType, defaultRecordBase());
        alertDefinitions.get(indicator).add(alertHandlerBuilder);
        return alertHandlerBuilder;
    }

    public AlertWatcherBuilder setExecutorService(@NonNull ExecutorService executorService){
        this.executorService = executorService;
        return this;
    }

    public AlertWatcherBuilder setRecordBase(@NonNull RecordBase recordBase){
        this.recordBase = recordBase;
        return this;
    }

    public AlertWatcherClient build(){
        Map<String, List<AlertHandler>> alertHandlerMap = alertDefinitions.entrySet().stream().
                collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue().stream().map(AlertDefinitionBuilder::buildAlertHandler).collect(Collectors.toList())));
        Map<String, List<ReportFiltration>> filterMap = alertDefinitions.entrySet().stream().
                collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue().stream().map(AlertDefinitionBuilder::buildReportFilter).collect(Collectors.toList())));
        Map<String, Long> timeTable = alertDefinitions.entrySet().stream().
                collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    long maxTime = DEFAULT_CLEAN_TIME;
                    for (AlertDefinitionBuilder definitionBuilder : entry.getValue()){
                        maxTime = Math.max(maxTime, definitionBuilder.buildScheduleTime());
                    }
                    return maxTime;
                }));
        return new AlertWatcherClient(filterMap, alertHandlerMap, timeTable, defaultRecordBase(), executorService);
    }

    private RecordBase defaultRecordBase(){
        return recordBase;
    }
}
