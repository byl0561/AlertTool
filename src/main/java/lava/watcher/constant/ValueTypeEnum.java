package lava.watcher.constant;

import java.math.BigDecimal;

/**
 * @Auther: lava
 * @Date: 2021/9/9 17:52
 * @Description:
 */
public enum ValueTypeEnum {
    NONE(Boolean.class),
    BOOLEAN(Boolean.class),
    NUMBER(BigDecimal.class),
    STRING(String.class);

    private final Class<? extends Comparable<?>> clz;

    ValueTypeEnum(Class<? extends Comparable<?>> clz) {
        this.clz = clz;
    }

    public Class<? extends Comparable<?>> getClz() {
        return clz;
    }
}
