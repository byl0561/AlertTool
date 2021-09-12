package lava.watcher.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.NonNull;

import java.util.Collection;
import java.util.Objects;

/**
 * @Auther: lava
 * @Date: 2021/9/8 14:32
 * @Description:
 */
public class JsonUtil {
    private static final Gson gson = new Gson();

    private JsonUtil() {
    }

    public static String serialize(Object object){
        if (Objects.isNull(object) || String.class.equals(object.getClass())){
            return (String) object;
        }
        return gson.toJson(object);
    }

    public static <T> T parseObject(@NonNull String str, @NonNull Class<T> clz){
        return gson.fromJson(str, clz);
    }

    public static <T> T parseCollection(@NonNull String str, @NonNull TypeToken<T> typeToken){
        return gson.fromJson(str, typeToken.getType());
    }
}
