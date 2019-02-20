package org.alalgo.configclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class HelloController {
	@Value("${alalgo.hello}")
	private String conf_hello;
	
	@GetMapping
	public String getConf() {
		return conf_hello;
	}
}
