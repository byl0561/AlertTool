package lava.watcher.executor;

import lava.watcher.constant.HTTPRequestTypeEnum;
import lava.watcher.model.Record;
import lava.watcher.util.DateUtil;
import lava.watcher.util.HttpJsonUtil;
import lava.watcher.util.JsonUtil;
import com.google.gson.reflect.TypeToken;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;

/**
 * @Auther: lava
 * @Date: 2021/9/8 11:41
 * @Description:
 */
@Slf4j
public class LarkAlertExecutor implements AlertExecutor{
    public LarkAlertExecutor(@NonNull String webhookUri, @NonNull String ruleName) {
        this.webhookUri = webhookUri;
        this.ruleName = ruleName;
    }

    private final String webhookUri;
    private final String ruleName;

    @Override
    public void execute(Record<?> record) {
        String message = buildMessage(ruleName, record.getValue().toString(), DateUtil.format(record.getTimeStamp()));
        HttpJsonUtil.HTTPResponse response = HttpJsonUtil.send(HTTPRequestTypeEnum.POST, webhookUri, null, message, null);
        if (!response.isSuccess()){
            log.error("[LarkAlertExecutor] http request failed: httpCode:{}, error:{}, ruleName:{}, value:{}",
                    response.getHttpCode(), Objects.isNull(response.getCause()) ? null : response.getCause().getMessage(), ruleName, record.getValue().toString());
            return;
        }
        Map<String, String> rspMap = JsonUtil.parseCollection(response.getJson(), new TypeToken<Map<String, String>>() {});
        if (!rspMap.get("StatusCode").equals("0")){
            log.error("[LarkAlertExecutor] http response fail: code:{}, message:{}", rspMap.get("StatusCode"), rspMap.get("StatusMessage"));
        }
    }

    private String buildMessage(@NonNull String ruleName, @NonNull String value, @NonNull String time){
        return "{\n" +
                "    \"msg_type\": \"post\",\n" +
                "    \"content\": {\n" +
                "        \"post\": {\n" +
                "            \"zh_cn\": {\n" +
                "                \"title\": \"告警通知\",\n" +
                "                \"content\": [\n" +
                "                    [\n" +
                "                        {\n" +
                "                            \"tag\": \"text\",\n" +
                "                            \"text\": \"触发规则: \"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"tag\": \"text\",\n" +
                "                            \"text\": \"" + ruleName + "\"\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    [\n" +
                "                        {\n" +
                "                            \"tag\": \"text\",\n" +
                "                            \"text\": \"触发值: \"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"tag\": \"text\",\n" +
                "                            \"text\": \"" + value + "\"\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    [\n" +
                "                        {\n" +
                "                            \"tag\": \"text\",\n" +
                "                            \"text\": \"触发时间: \"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"tag\": \"text\",\n" +
                "                            \"text\": \"" + time + "\"\n" +
                "                        }\n" +
                "                    ]\n" +
                "                ]\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
    }
}

// example:
//{
//    "msg_type": "post",
//    "content": {
//        "post": {
//            "zh_cn": {
//                "title": "告警通知",
//                "content": [
//                    [
//                        {
//                            "tag": "text",
//                            "text": "项目有更新: "
//                        },
//                        {
//                            "tag": "a",
//                            "text": "请查看",
//                            "href": "http://www.example.com/"
//                        },
//                        {
//                            "tag": "at",
//                            "user_id": "ou_18eac8********17ad4f02e8bbbb"
//                        }
//                    ]
//                ]
//            }
//        }
//    }
//}

//{
//    "msg_type": "post",
//    "content": {
//        "post": {
//            "zh_cn": {
//                "title": "告警通知",
//                "content": [
//                    [
//                        {
//                            "tag": "text",
//                            "text": "触发规则: "
//                        },
//                        {
//                            "tag": "text",
//                            "text": "rule"
//                        }
//                    ]
//                    [
//                        {
//                            "tag": "text",
//                            "text": "触发值: "
//                        },
//                        {
//                            "tag": "text",
//                            "text": "value"
//                        }
//                    ]
//                ]
//            }
//        }
//    }
//}