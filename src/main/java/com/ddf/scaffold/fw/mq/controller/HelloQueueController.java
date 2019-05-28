package com.ddf.scaffold.fw.mq.controller;

import com.ddf.scaffold.fw.mq.ProductConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * @author DDf on 2019/4/21
 */
@RestController
@RequestMapping("mq")
@Api(description = "发送消息的请求类")
public class HelloQueueController {
	public final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private RabbitTemplate rabbitTemplate;
	@Autowired
	@Qualifier("taskExecutor")
	private Executor taskExecutor;

	@ApiOperation("发送一个点对点的消息到HELLO_QUEUE")
	@GetMapping("toHelloQueue")
	public void toHelloQueue(@RequestParam @ApiParam(value = "发送内容") String msg) {
		Map<String, String> obj = new HashMap<>();
		obj.put("value", msg);
		rabbitTemplate.convertAndSend(ProductConfig.HELLO_QUEUE, obj);
		logger.info("发送消息: {}", obj);
	}

	/**
	 * 发送一个exchange类型为fanout类型的消息，即所有绑定到该exchange的queue都会收到消息
	 * @param msg
	 */
	@GetMapping("toFanoutQueue")
	@ApiOperation("发送一个exchange类型为fanout类型的消息")
	public void toFanoutQueue(@RequestParam @ApiParam(value = "发送内容") String msg) {
		// fanout类型的routingKey无意义不需要指定，一般为空字符串即可
		Map<String, String> obj = new HashMap<>();
		obj.put("value", msg);
		rabbitTemplate.convertAndSend(ProductConfig.EXCHANGE_FANOUT, "", obj);
		logger.info("发送消息: {}", msg);
	}


	/**
	 * 发送一个exchange类型为fanout类型的消息，即所有绑定到该exchange的queue都会收到消息
	 * @param msg
	 */
	@GetMapping("concurrentToFanoutQueue")
	@ApiOperation("并发发送一个exchange类型为fanout类型的消息")
	public void concurrentToFanoutQueue(@RequestParam @ApiParam(value = "发送内容") String msg) {
		// fanout类型的routingKey无意义不需要指定，一般为空字符串即可
		Map<String, String> obj = new HashMap<>();
		for (int i = 0; i < 10000; i++) {
			final int random = i;
			taskExecutor.execute(() -> {
				obj.put("value", msg + random);
				rabbitTemplate.convertAndSend(ProductConfig.EXCHANGE_FANOUT, "", obj);
				logger.info("发送消息: {}", msg + random);
				obj.clear();
			});
		}
	}

}
