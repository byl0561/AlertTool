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
    private static final ScheduledExecutorService unLimitedScheduler = new ScheduledThreadPoolExecutor(2);

    private TimerUtil() {
    }

    public static void unLimitedSchedule(long delay, @NonNull Runnable task){
        unLimitedScheduler.schedule(task, delay, TimeUnit.MILLISECONDS);
    }

    public static void unLimitedScheduleAtFixedRate(long period, @NonNull Runnable task){
        unLimitedScheduler.scheduleAtFixedRate(task, 0, period, TimeUnit.MILLISECONDS);
    }
}
