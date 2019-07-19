package com.ddf.scaffold.fw.keepalive.server;

import com.ddf.scaffold.fw.util.RSAUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.List;

/**
 * 编解码器
 * @author dongfang.ding
 * @date 2019/7/5 15:01
 */
@Slf4j
public class RequestContentCodec extends ByteToMessageCodec<Object> {

    private final Charset charset;

    /**
     * 编码器给服务端使用还是给客户端使用
     * 不同的模式下，下面处理方式不同
     */
    private boolean serverMode;

    public RequestContentCodec(Charset charset, boolean serverMode) {
        if (charset == null) {
            throw new NullPointerException("charset");
        }
        this.charset = charset;
        this.serverMode = serverMode;
    }

    public RequestContentCodec(boolean serverMode) {
        this.charset = CharsetUtil.UTF_8;
        this.serverMode = serverMode;
    }

    /**
     * 服务端操作数据使用对象{@link RequestContent}，最终写入到客户端的时候编码成字节
     * @param ctx
     * @param msg
     * @param out
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (msg instanceof RequestContent) {
            RequestContent requestContent = (RequestContent) msg;
            if (serverMode) {
                // 服务端使用自己的私钥对body加签
                requestContent.sign(RSAUtil.signByServerPrivateKey(requestContent.getBody()));
                // 服务端使用自己的私钥对body加密
                requestContent.setBody(RSAUtil.encryptByServerPrivateKey(requestContent.getBody()));
            } else {
                // 客户端使用自己的私钥对body加签
                requestContent.sign(RSAUtil.signByClientPrivateKey(requestContent.getBody()));
                // 客户端使用自己的私钥对body加密
                requestContent.setBody(RSAUtil.encryptByClientPrivateKey(requestContent.getBody()));
            }
            out.writeBytes(Unpooled.copiedBuffer(new ObjectMapper().writeValueAsString(requestContent).getBytes(charset)));
            out.writeBytes("\r\n".getBytes());
        }
    }


    /**
     * 将客户端传入的解码成服务端使用的{@link RequestContent}
     * 注意TCP的粘包和拆包问题，这里已经使用了{@link io.netty.handler.codec.LineBasedFrameDecoder}解码器来解决，要求客户端比如以换行符结尾
     * @param ctx
     * @param in
     * @param out
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            log.info("开始解码=========================");
            ObjectMapper objectMapper = new ObjectMapper();
            byte[] content = new byte[in.readableBytes()];
            in.readBytes(content);
            RequestContent requestContent = objectMapper.readValue(content, RequestContent.class);
            String signStr = requestContent.getSign();
            String body;
            boolean verify;
            if (serverMode) {
                // 服务端的模式，是解密与验签客户端的数据
                body = RSAUtil.decryptByClientPublicKey(requestContent.getBody());
                verify = RSAUtil.verifyByClientPublicKey(body, signStr);
                requestContent.setBody(body);
            } else {
                // 客户端模式，是解密与验签服务端的数据
                body = RSAUtil.decryptByServerPublicKey(requestContent.getBody());
                verify = RSAUtil.verifyByServerPublicKey(body, signStr);
            }
            log.info("解密后body数据: {}", body);
            log.info("验签结果: {}", verify);
            if (!verify) {
                if (serverMode) {
                    // 如果服务端模式，客户端的数据不能被识别，关闭对方连接
                    ctx.close();
                }
                return;
            }
            requestContent.setBody(body);
            // 解析扩展字段
            requestContent.parseExtra();
            out.add(requestContent);
            log.info("解码完成: {}", RequestContent.serial(requestContent));
            // TODO 对RequestContent参数进行校验
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
