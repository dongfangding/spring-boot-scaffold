package com.ddf.scaffold;

import com.ddf.scaffold.fw.log.EnableLogAspect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * @author DDf on 2018/12/1
 */
@SpringBootApplication
@EntityScan("com.ddf.scaffold.logic.entity")
@EnableLogAspect
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
