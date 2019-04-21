package com.ddf.scaffold.fw.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author DDf on 2019/4/21
 */
@Configuration
public class ProductConfig implements RabbitTemplate.ConfirmCallback {

	private static final Logger logger = LoggerFactory.getLogger(ProductConfig.class);

	public static final String HELLO_QUEUE = "helloQueue";

	public static final String FANOUT_QUEUE = "FANOUT_QUEUE";

	public static final String FANOUT_QUEUE2 = "FANOUT_QUEUE2";

	public static final String EXCHANGE_FANOUT = "EXCHANGE_FANOUT";

	public static final String DEAD_QUEUE = "DEAD_QUEUE";

	public static final String EXCHANGE_DEAD = "EXCHANGE_DEAD";

	/**
	 * 创建一个hello队列，点对点
	 */
	@Bean
	public Queue helloQueue() {
		return new Queue(HELLO_QUEUE);
	}

	/**
	 * 创建一个fanout类型的任务队列
	 */
	@Bean
	public Queue fanoutQueue() {
		return new Queue(FANOUT_QUEUE);
	}

	/**
	 * 创建一个fanout类型的任务队列
	 */
	@Bean
	public Queue fanoutQueue2() {
		return new Queue(FANOUT_QUEUE2);
	}

	/**
	 * 广播交换机
	 * @return
	 */
	@Bean
	public FanoutExchange fanoutExchange() {
		return new FanoutExchange(EXCHANGE_FANOUT);
	}


	/**
	 * 将fanoutExchange交换机绑定到fanoutQueue队列中
	 * @param fanoutExchange
	 * @param fanoutQueue
	 * @return
	 */
	@Bean
	public Binding fanoutQueueExchange(FanoutExchange fanoutExchange, Queue fanoutQueue) {
		return BindingBuilder.bind(fanoutQueue).to(fanoutExchange);
	}

	/**
	 * 将fanoutExchange交换机绑定到fanoutQueue队列中
	 * @param fanoutExchange
	 * @param fanoutQueue
	 * @return
	 */
	@Bean
	public Binding fanoutQueue2Exchange(FanoutExchange fanoutExchange, Queue fanoutQueue2) {
		return BindingBuilder.bind(fanoutQueue2).to(fanoutExchange);
	}


	/**
	 * 设置rabbitmq的序列化机制为application/json
	 * @return
	 */
	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}


	/**
	 * 发送端开启消息确认机制的回调处理
	 * @param correlationData
	 * @param ack
	 * @param cause
	 */
	@Override
	public void confirm(CorrelationData correlationData, boolean ack, String cause) {
		logger.info("correlationData: {}", correlationData);
		logger.info("ack: {}", ack);
		logger.info("cause: {}", cause);
	}


	/**
	 * 创建一个死信队列
	 * @return
	 */
	@Bean
	public Queue deadQueue() {
		return new Queue(DEAD_QUEUE);
	}


	/**
	 * 创建一个Direct类型的交换器用作作为处理死信队列的交换器
	 * @return
	 */
	@Bean
	public DirectExchange deadExchange() {
		return new DirectExchange(EXCHANGE_DEAD);
	}

	@Bean
	public Binding deadQueueExchange() {
		return BindingBuilder.bind(deadQueue()).to(deadExchange()).with(DEAD_QUEUE);
	}
}
