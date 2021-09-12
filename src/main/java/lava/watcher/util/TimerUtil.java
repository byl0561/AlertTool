package lava.watcher.util;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Auther: lava
 * @Date: 2021/9/7 16:54
 * @Description: 定时器
 */
@Slf4j
public class TimerUtil {
    public static final int MAX_CONCURRENT_EACH_ID = 20;

    private static final ScheduledExecutorService limitedScheduler = new ScheduledThreadPoolExecutor(2);
    private static final Map<String, AtomicInteger> limitedCounter = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService unLimitedScheduler = new ScheduledThreadPoolExecutor(2);

    private TimerUtil() {
    }

    public static void limitedSchedule(@NonNull String id, long delay, @NonNull Runnable task, Runnable compensateTask){
        if (checkCount(id)){
            limitedScheduler.schedule(() -> {
                log.info("[TimerUtil] limited schedule run, id:{}", id);
                task.run();
                if (compensateCount(id) && Objects.nonNull(compensateTask)){
                    log.info("[TimerUtil] compensate schedule submit, id:{}", id);
                    limitedSchedule(id, delay, compensateTask, compensateTask);
                }
            }, delay, TimeUnit.MILLISECONDS);
        }
    }

    public static void unLimitedSchedule(long delay, @NonNull Runnable task){
        unLimitedScheduler.schedule(task, delay, TimeUnit.MILLISECONDS);
    }

    public static void unLimitedScheduleAtFixedRate(long period, @NonNull Runnable task){
        unLimitedScheduler.scheduleAtFixedRate(task, 0, period, TimeUnit.MILLISECONDS);
    }

    private static boolean checkCount(@NonNull String id){
        if (!limitedCounter.containsKey(id)){
            limitedCounter.putIfAbsent(id, new AtomicInteger(0));
        }
        int count = limitedCounter.get(id).updateAndGet(x -> x > MAX_CONCURRENT_EACH_ID ? x : x + 1);
        if (count <= MAX_CONCURRENT_EACH_ID){
            log.info("[TimerUtil] limited schedule submit, id:{}", id);
            return true;
        }
        else {
            log.info("[TimerUtil] limited schedule block, id:{}", id);
            return false;
        }
    }

    private static boolean compensateCount(@NonNull String id){
        int count = limitedCounter.get(id).getAndUpdate(x -> x > MAX_CONCURRENT_EACH_ID ? MAX_CONCURRENT_EACH_ID - 1 : x - 1);
        return count > MAX_CONCURRENT_EACH_ID;
    }
}
