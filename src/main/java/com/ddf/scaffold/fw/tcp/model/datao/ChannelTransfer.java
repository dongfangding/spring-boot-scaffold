package com.ddf.scaffold.fw.tcp.model.datao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ddf.scaffold.fw.entity.BaseDomain;
import com.ddf.scaffold.fw.entity.EntityGenerateUtil;
import com.ddf.scaffold.fw.tcp.server.RequestContent;
import io.netty.channel.ChannelHandlerContext;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 通道传输报文日志记录
 *
 * @author {@link EntityGenerateUtil} Tue Jul 23 15:37:11 CST 2019
 */
@TableName(value = "boot_log_channel_transfer")
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@ApiModel("通道传输报文日志记录")
public class ChannelTransfer extends BaseDomain {

    /**
     * 未验签通过
     */
    private static final Byte STATUS_NOT_SIGN = 0;
    /**
     * 验签通过未处理
     */
    private static final Byte STATUS_VERIFIED = 1;
    /**
     * 验签通过并上传成功
     */
    private static final Byte STATUS_VERIFIED_FINISHED = 2;

    @ApiModelProperty("请求id，用来响应和判断客户端是否重复请求")
    private String requestId;

    @ApiModelProperty("安卓设备号")
    private String deviceNo;

    @ApiModelProperty("服务端地址")
    private String serverAddress;

    @ApiModelProperty("客户端地址")
    private String clientAddress;

    @ApiModelProperty("传输内容")
    private String content;

    @ApiModelProperty("0 未验签通过 1 验签通过未处理 2  验签通过并上传成功")
    private Byte status;

    /**
     * 该报文对应的已经解析反序列化后的报文数据，记录报文后，如果需要处理数据，这个值保存后不用二次解析
     */
    @TableField(exist = false)
    private RequestContent requestContent;

    /**
     * 默认构造函数
     *
     * @param requestId
     * @param deviceNo
     * @param serverAddress
     * @param clientAddress
     * @param content
     * @param requestContent
     */
    public ChannelTransfer(String requestId, String deviceNo, String serverAddress, String clientAddress, String content, RequestContent requestContent) {
        this.requestId = requestId;
        this.deviceNo = deviceNo;
        this.serverAddress = serverAddress;
        this.clientAddress = clientAddress;
        this.content = content;
        this.requestContent = requestContent;
    }

    /**
     * 构建默认收到的传输对象
     *
     * @param ctx
     * @param requestContent
     * @return
     */
    public static ChannelTransfer build(ChannelHandlerContext ctx, RequestContent requestContent) {
        return new ChannelTransfer(requestContent.getRequestId(), requestContent.getDeviceNo(), ctx.channel().localAddress().toString(),
                ctx.channel().remoteAddress().toString(), RequestContent.serial(requestContent), requestContent);
    }


    /**
     * 构建已验签通过的传输对象
     *
     * @param ctx
     * @param requestContent
     * @return
     */
    public static ChannelTransfer buildVerified(ChannelHandlerContext ctx, RequestContent requestContent) {
        ChannelTransfer channelTransfer = new ChannelTransfer(requestContent.getRequestId(), requestContent.getDeviceNo(),
                ctx.channel().localAddress().toString(), ctx.channel().remoteAddress().toString(),
                RequestContent.serial(requestContent), requestContent);
        channelTransfer.setStatusVerified();
        return channelTransfer;
    }

    public void setStatusVerified() {
        this.status = STATUS_VERIFIED;
    }

    public void setStatusVerifiedAndFinished() {
        this.status = STATUS_VERIFIED_FINISHED;
    }


    /**
     * 返回需要去处理上传的状态
     *
     * @return
     */
    public static Byte waitToDeal() {
        return STATUS_VERIFIED;
    }
}