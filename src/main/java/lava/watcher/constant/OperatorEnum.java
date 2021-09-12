package lava.watcher.constant;

import lombok.NonNull;

/**
 * @Auther: lava
 * @Date: 2021/9/6 17:35
 * @Description:
 */
public enum OperatorEnum {
    EQUAL("="),
    GREATER_THAN(">"),
    LESS_THAN("<"),
    NO_GREATER_THAN("<="),
    NO_LESS_THAN(">=");

    private final String operator;

    OperatorEnum(String operator) {
        this.operator = operator;
    }

    public static OperatorEnum parse(@NonNull String operator){
        for (OperatorEnum operatorEnum : OperatorEnum.values()){
            if (operatorEnum.operator.equals(operator)){
                return operatorEnum;
            }
        }
        return null;
    }
}
