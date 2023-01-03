package lava.watcher.alerthandler;

import lava.watcher.executor.AlertExecutor;
import lava.watcher.limiter.AlertLimiter;
import lava.watcher.model.Record;
import lava.watcher.rule.AlertRule;

import java.util.List;
import java.util.Objects;

/**
 * @Auther: lava
 * @Date: 2021/9/9 11:00
 * @Description:
 */
public class DefaultAlertHandler implements AlertHandler{
    public DefaultAlertHandler(AlertRule alertRule, List<AlertLimiter> alertLimiterList, List<AlertExecutor> alertExecutors) {
        this.alertRule = alertRule;
        this.alertLimiterList = alertLimiterList;
        this.alertExecutors = alertExecutors;
    }

    private final AlertRule alertRule;
    private final List<AlertLimiter> alertLimiterList;
    private final List<AlertExecutor> alertExecutors;

    @Override
    public void report(Record<?> record) {
        Record<?> alertRecord = alertRule.alertRecord(record);
        if (Objects.isNull(alertRecord)){
            return;
        }
        for (AlertLimiter alertLimiter : alertLimiterList){
            if (!alertLimiter.limit(alertRecord)){
                return;
            }
        }
        alertExecutors.forEach(alertExecutor -> alertExecutor.execute(alertRecord));
    }
}
