package com.ddf.scaffold.fw.mq.controller;

import com.ddf.scaffold.fw.mq.ProductConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author DDf on 2019/4/21
 */
@RestController
@RequestMapping("mq")
public class HelloQueueController {
	public final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@RequestMapping("toHelloQueue")
	public void toHelloQueue(@RequestParam String msg) {
		Map<String, String> obj = new HashMap<>();
		obj.put("key", msg);
		rabbitTemplate.convertAndSend(ProductConfig.HELLO_QUEUE, msg);
		logger.info("发送消息: {}", obj);
	}

	/**
	 * 发送一个exchange类型为fanout类型的消息，即所有绑定到该exchange的queue都会收到消息
	 * @param msg
	 */
	@RequestMapping("toFanoutQueue")
	public void toFanoutQueue(@RequestParam String msg) {
		// fanout类型的routingKey无意义不需要指定，一般为空字符串即可
		for (int i = 0; i < 10; i++) {
			rabbitTemplate.convertAndSend(ProductConfig.EXCHANGE_FANOUT, "", msg + i);
			logger.info("发送消息: {}", msg + i);
		}
	}

}
