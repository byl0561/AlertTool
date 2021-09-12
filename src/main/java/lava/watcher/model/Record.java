package lava.watcher.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.Date;

/**
 * @Auther: lava
 * @Date: 2021/9/6 16:35
 * @Description: 报警记录
 */
@Data
public class Record<T> {
    public Record(@NonNull Date date, @NonNull String indicator, @NonNull T value) {
        this.timeStamp = date.getTime();
        this.indicator = indicator;
        this.value = value;
    }

    private Long timeStamp;
    private String indicator;
    private T value;
}
