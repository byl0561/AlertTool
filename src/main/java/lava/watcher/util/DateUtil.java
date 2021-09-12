package lava.watcher.util;

import lombok.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Auther: lava
 * @Date: 2021/9/10 15:47
 * @Description:
 */
public class DateUtil {
    private static final SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private DateUtil(){
    }

    public static String format(@NonNull Date date){
        return f.format(date);
    }

    public static String format(long timeStamp){
        return format(new Date(timeStamp));
    }
}
