package com.ddf.scaffold.fw.tcp.server;

import com.ddf.scaffold.fw.util.JsonUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 通道传输数据格式定义类
 *
 * @author dongfang.ding
 * @date 2019/7/5 14:59
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestContent implements Serializable {
    /**
     * 扩展头每个键值对之间的分隔符，注意空格
     */
    private transient static final String SPLIT_LINE = "; ";
    /**
     * 扩展头键值对之间的分隔符，注意空格
     */
    private transient static final String SPLIT_KEY_VALUE = ": ";

    /**
     * 加签字符串在扩展头的标识字段
     */
    private transient static final String SIGN_HEADER = "sign";

    /**
     * 服务端发送数据身份标识
     */
    private transient static final String MODE_SERVER = "server";

    /**
     * 客户端发送数据身份标识
     */
    private transient static final String MODE_CLIENT = "client";

    /**
     * 唯一标识此次请求，一个随机数
     */
    @JsonInclude
    private String requestId;

    /**
     * 发送方，标识发送数据的身份
     * server - 服务端
     * client - 客户端发
     */
    @JsonInclude
    private String mode;

    /**
     * REQUEST 请求 RESPONSE 应答
     *
     * @see Type
     */
    @JsonInclude
    private String type;
    /**
     * 本次请求要做什么事情,比如心跳包还是业务处理，不同的业务要做的事情不一样，处理主体数据格式也不一样
     */
    @JsonInclude
    private String cmd;

    /**
     * 请求时间
     */
    private Long requestTime;

    /**
     * 响应时间
     */
    private Long responseTime;

    /**
     * 主体数据
     */
    private String body;

    /**
     * 扩展字段
     * 类似http请求头，解析格式为key1: value1; key2: value2，注意是有空格的
     */
    private String extra;

    @JsonIgnore
    private transient Map<String, String> extraMap;

    /**
     * 这个字段本身是不需要的，只是想提供获取和设置设备号的方法，但因为用了get和set，序列化的时候反而会生成这个
     * 字段的序列化，因此只能把字段摘出来，设置成序列化时忽略掉
     */
    @JsonIgnore
    private transient String deviceNo;

    /**
     * 同deviceNo
     */
    @JsonIgnore
    private transient String sign;

    public RequestContent() {

    }

    public RequestContent(String requestId, String mode, String type, String cmd, Long requestTime, String content) {
        this.requestId = requestId;
        this.mode = mode;
        this.type = type;
        this.cmd = cmd;
        this.requestTime = requestTime;
        this.body = content;
    }

    /**
     * 客户端主动发起请求推送短信上传报文
     *
     * @param smsContentList
     * @return
     */
    public static RequestContent clientSmsUpload(List<SmsContent> smsContentList) {
        if (smsContentList == null || smsContentList.isEmpty()) {
            throw new RuntimeException("短信内容不全!");
        }
        for (SmsContent smsContent : smsContentList) {
            if (smsContent == null || StringUtils.isAnyBlank(smsContent.getMessageId(), smsContent.getSender(),
                    smsContent.getContent(), smsContent.getReceiver())) {
                throw new RuntimeException("短信内容不全!");
            }
        }
        return new RequestContent(UUID.randomUUID().toString(), MODE_CLIENT, Type.REQUEST.name(), Cmd.SMS_UPLOAD.name(),
                System.currentTimeMillis(), JsonUtil.asString(smsContentList));
    }

    /**
     * 客户端主动发起请求推送设备注册报文
     *
     * @param deviceRegistry
     * @return
     */
    public static RequestContent clientDeviceRegistry(DeviceRegistry deviceRegistry) {
        if (deviceRegistry == null || StringUtils.isAnyBlank(deviceRegistry.getDeviceNo(), deviceRegistry.getDeviceModel(),
                deviceRegistry.getDeviceVersion())) {
            throw new RuntimeException("设备信息不全！");
        }
        RequestContent requestContent = new RequestContent(UUID.randomUUID().toString(), MODE_CLIENT, Type.REQUEST.name(),
                Cmd.DEVICE_REGISTRY.name(), System.currentTimeMillis(), JsonUtil.asString(deviceRegistry));
        requestContent.setDeviceNo(deviceRegistry.getDeviceNo());
        return requestContent;
    }


    /**
     * 对收到的请求应答数据已收到
     *
     * @param requestContent
     * @return
     */
    public static RequestContent responseAccept(RequestContent requestContent) {
        return response(requestContent, "202");
    }

    /**
     * 对收到的请求应答业务处理成功
     *
     * @param requestContent
     * @return
     */
    public static RequestContent responseOK(RequestContent requestContent) {
        return response(requestContent, "200");
    }

    /**
     * 服务端向客户端发送心跳检测命令
     *
     * @return
     */
    public static RequestContent serverHeart() {
        return new RequestContent(UUID.randomUUID().toString(), MODE_SERVER, Type.REQUEST.name(), Cmd.HEART.name(), System.currentTimeMillis(), "ping");
    }

    /**
     * 客户端向服务端发送心跳检测命令
     *
     * @return
     */
    public static RequestContent clientHeart() {
        return new RequestContent(UUID.randomUUID().toString(), MODE_CLIENT, Type.REQUEST.name(), Cmd.HEART.name(), System.currentTimeMillis(), "ping");
    }


    /**
     * 添加扩展字段
     *
     * @param key
     * @param value
     * @return
     */
    public RequestContent addExtra(String key, String value) {
        if (extra == null) {
            extra = "";
        } else if (extra.length() > 0) {
            extra += SPLIT_LINE;
        }
        extra += key + SPLIT_KEY_VALUE + value;
        parseExtra();
        return this;
    }

    /**
     * 序列化RequestContent
     *
     * @param requestContent
     * @return
     * @throws JsonProcessingException
     */
    public static String serial(RequestContent requestContent) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(requestContent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化失败!");
        }
    }

    /**
     * 将加签数据添加到扩展头中
     *
     * @param signStr
     */
    public void sign(String signStr) {
        addExtra(SIGN_HEADER, signStr);
    }

    /**
     * 获取扩展头中的加签数据
     *
     * @return
     */
    public String getSign() {
        return extraMap == null ? "" : extraMap.get(SIGN_HEADER);
    }

    /**
     * 从扩展头中获取设备头的快捷方法
     *
     * @return
     */
    public String getDeviceNo() {
        return extraMap == null ? null : extraMap.get("deviceNo");
    }


    /**
     * 将设备号添加到扩展头的快捷方法
     *
     * @param deviceNo
     */
    public void setDeviceNo(String deviceNo) {
        addExtra("deviceNo", deviceNo);
    }

    /**
     * 根据请求数据构造响应数据
     *
     * @param requestContent
     * @param code
     * @return
     */
    private static RequestContent response(RequestContent requestContent, String code) {
        RequestContent response = new RequestContent();
        response.setMode(MODE_SERVER);
        response.setType(Type.RESPONSE.name());
        response.setRequestTime(requestContent.getRequestTime());
        response.setRequestId(requestContent.getRequestId());
        response.setCmd(requestContent.getCmd());
        response.setResponseTime(System.currentTimeMillis());
        response.setDeviceNo(requestContent.getDeviceNo());
        response.setBody(code);
        return response;
    }

    /**
     * 解析扩展字段,注意空格
     */
    public RequestContent parseExtra() {
        if (null != extra && !"".equals(extra)) {
            try {
                String[] keyValueArr = extra.split(SPLIT_LINE);
                if (keyValueArr.length > 0) {
                    Map<String, String> extraMap = getExtraMap() == null ? new HashMap<>() : getExtraMap();
                    String[] keyValue;
                    for (String s : keyValueArr) {
                        keyValue = s.split(SPLIT_KEY_VALUE);
                        extraMap.put(keyValue[0], keyValue[1]);
                    }
                    setExtraMap(extraMap);
                }
            } catch (Exception e) {
                extraMap = null;
            }
        }
        return this;
    }

    public String getRequestId() {
        return requestId;
    }

    public RequestContent setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public String getType() {
        return type;
    }

    public RequestContent setType(String type) {
        this.type = type;
        return this;
    }

    public String getCmd() {
        return cmd;
    }

    public RequestContent setCmd(String cmd) {
        this.cmd = cmd;
        return this;
    }

    public Long getRequestTime() {
        return requestTime;
    }

    public RequestContent setRequestTime(Long requestTime) {
        this.requestTime = requestTime;
        return this;
    }

    public Long getResponseTime() {
        return responseTime;
    }

    public RequestContent setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
        return this;
    }

    public String getBody() {
        return body;
    }

    public RequestContent setBody(String body) {
        this.body = body;
        return this;
    }

    public String getExtra() {
        return extra;
    }


    /**
     * 由于set添加扩展值容易出错，因此不对外提供，进攻解码器使用
     *
     * @param extra
     * @return
     */
    private RequestContent setExtra(String extra) {
        this.extra = extra;
        return parseExtra();
    }

    public Map<String, String> getExtraMap() {
        return extraMap;
    }

    public void setExtraMap(Map<String, String> extraMap) {
        this.extraMap = extraMap;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * 连接请求类型
     */
    public enum Type {
        /**
         * 请求
         */
        REQUEST,
        /**
         * 响应
         */
        RESPONSE
    }

    /**
     * 命名
     */
    public enum Cmd {
        /**
         * 心跳检测
         */
        HEART,
        /**
         * 短信上传
         */
        SMS_UPLOAD,

        /**
         * 设备注册
         */
        DEVICE_REGISTRY
    }
}
