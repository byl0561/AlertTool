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
    private DateUtil(){
    }

    public static String format(@NonNull Date date, String formater){
        return new SimpleDateFormat(formater).format(date);
    }

    public static String format(long timeStamp, String formater){
        return format(new Date(timeStamp), formater);
    }
}
