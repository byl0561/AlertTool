package lava.watcher.filtration;

import lava.watcher.model.Record;
import lombok.Data;
import lombok.NonNull;

import java.util.Objects;

/**
 * @Auther: lava
 * @Date: 2021/9/6 16:31
 * @Description: 埋点数据过滤器
 */
@Data
public abstract class ReportFiltration {
    private ReportFiltration nextFiltration;

    // 返回true即为通过过滤
    public boolean filter(@NonNull Record<?> record){
        // 责任链，and逻辑
        if (doFilter(record)){
            return filterNext(record);
        }
        return false;
    }

    protected abstract boolean doFilter(@NonNull Record<?> record);

    public boolean filterNext(Record<?> record){
        if (Objects.isNull(nextFiltration)){
            return true;
        }
        return nextFiltration.filter(record);
    }
}
