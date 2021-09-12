package lava.watcher.util;


import lava.watcher.constant.OperatorEnum;
import lombok.NonNull;

/**
 * @Auther: lava
 * @Date: 2021/9/6 17:55
 * @Description: 比较工具
 */
public class OperatorUtil {

    private OperatorUtil() {
    }

    public static <T extends Comparable<T>> boolean operate(@NonNull OperatorEnum operator, @NonNull T v1, @NonNull T v2){
        switch (operator){
            case EQUAL:
                return isEqual(v1, v2);
            case GREATER_THAN:
                return isGreaterThan(v1, v2);
            case LESS_THAN:
                return isLessThan(v1, v2);
            case NO_GREATER_THAN:
                return nonGreaterThan(v1, v2);
            case NO_LESS_THAN:
                return nonLessThan(v1, v2);
        }
        return false;
    }

    private static <T extends Comparable<T>> boolean isEqual(T v1, T v2){
        return v1.equals(v2);
    }
    private static <T extends Comparable<T>> boolean isGreaterThan(T v1, T v2){
        return v1.compareTo(v2) > 0;
    }
    private static <T extends Comparable<T>> boolean isLessThan(T v1, T v2){
        return v1.compareTo(v2) < 0;
    }
    private static <T extends Comparable<T>> boolean nonGreaterThan(T v1, T v2){
        return !(v1.compareTo(v2) > 0);
    }
    private static <T extends Comparable<T>> boolean nonLessThan(T v1, T v2){
        return !(v1.compareTo(v2) < 0);
    }
}
