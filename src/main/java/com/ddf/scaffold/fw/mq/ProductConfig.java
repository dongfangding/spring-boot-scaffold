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

import java.util.HashMap;
import java.util.Map;

/**
 * @author DDf on 2019/4/21
 */
@Configuration
public class ProductConfig implements RabbitTemplate.ConfirmCallback {

	private static final Logger logger = LoggerFactory.getLogger(ProductConfig.class);

	public static final String HELLO_QUEUE = "HELLO_QUEUE";

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
		Map<String, Object> args = new HashMap<>(2);
		/**
		 * x-dead-letter-exchange    声明  死信交换机
		 * 消息被拒绝（basic.reject/ basic.nack）并且不再重新投递 requeue=false
		 * 消息超期 (rabbitmq  Time-To-Live -> messageProperties.setExpiration())
		 * 队列超载
		 * 以上三种情况如果在该队列中出现的话，那么会将消息通过定义的死信交换器来路由到指定的队列中，
		 * 死信队列的应用方式通常可以为
		 * 1. 做延时处理，定义为死信队列的queue不要定义消费方，而是等消息变死信后，消费转发后的队列
		 * 2. 死信队列正常消费，在开启了手动ack功能的时候由于某些消息不能重复投递，可以通过死信队列将无法消费的消息转发到一个指定交换器的队列中来保存，后续针对无法
		 *    正常消费的消息可以消费转发后的queue来处理
		 */
		// 该队列定位为死信队列，将满足死信的消息转发到私信交换器中
		args.put("x-dead-letter-exchange", EXCHANGE_DEAD);
		return new Queue(HELLO_QUEUE, true, false, false, args);
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
	public FanoutExchange deadExchange() {
		return new FanoutExchange(EXCHANGE_DEAD);
	}

	@Bean
	public Binding deadQueueExchange() {
		return BindingBuilder.bind(deadQueue()).to(deadExchange());
	}

}
