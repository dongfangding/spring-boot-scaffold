package com.ddf.scaffold.fw.mq;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 *
 * 如果配置了多个queues那么如何在接收消息的时候判断是从哪个queue中读取的呢，{@link com.ddf.scaffold.fw.mq.ConsumerMessage#receiveStringFromQueue(java.lang.String, org.springframework.amqp.core.Queue)}
 *
 * @author DDf on 2019/4/21
 */
@Component
@RabbitListener(queues = {ProductConfig.HELLO_QUEUE, ProductConfig.FANOUT_QUEUE, ProductConfig.FANOUT_QUEUE2})
public class ConsumerMessage {
	private static final Logger logger = LoggerFactory.getLogger(ConsumerMessage.class);


	/**
	 *
	 * 开启手动ack之后，如果消费端不调用basicAck方法，则消息会一直处理unack状态，而如果处理失败之后调用basicNack或basicReject将requeue的值设置为
	 * true之后消息会被自动设置回队列，而且是队列头部，这样就会导致如果该条消息会一直报错，那么就会造成无限重投和失败，而如果设置为false，则该条消息会直接删除;
	 * 而如果不调用的话，该消息的状态会为unack
	 *
	 * 解决方案之一：
	 *  最好不要重新投递，消费成功的就直接ack，而如果消费失败的，那么就将消费失败的消息保存到本地数据库中或者什么的业务逻辑处理，然后再将消息删除
	 *
	 *
	 * @param msg
	 * @param queue
	 */
	@RabbitHandler
	@Async("taskExecutor")
	public void receiveFromQueue(Map msg, Channel channel, Message message) throws IOException {
		try {
			if (true) {
				throw new RuntimeException("处理任务失败！");
			}
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			// 开启了手动确认之后，要自己编码确认消息已收到,如果有自己的业务逻辑，则处理完业务逻辑之后再手动确认？
			logger.info("receiveFromQueue队列消费到消息.....{}", msg);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("--------------------{}--------------------", message.getMessageProperties().getDeliveryTag());
			channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
			throw new RuntimeException(e);
		}
	}
}
