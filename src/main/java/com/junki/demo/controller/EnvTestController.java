package com.junki.demo.controller;

import com.junki.demo.config.AppConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/env")
public class EnvTestController {

	private final AppConfig appConfig;

	@Value("${app.name}")
	private String appName;

	@Value("${app.version}")
	private String appVersion;

	@Value("${app.env}")
	private String appEnv;

	@Value("${MYSQL_DATABASE:spubgv2}")
	private String mysqlDatabase;

	public EnvTestController(AppConfig appConfig) {
		this.appConfig = appConfig;
	}

	@GetMapping
	public Map<String, Object> getEnvInfo() {
		Map<String, Object> info = new HashMap<>();
		info.put("message", "환경변수 테스트 API");
		info.put("endpoints", Map.of(
			"test", "/api/env/test",
			"root", "/api/env"
		));
		return info;
	}

	@GetMapping("/test")
	public Map<String, Object> testEnvVariables() {
		Map<String, Object> envVars = new HashMap<>();
		
		// @Value 어노테이션으로 직접 주입받은 값
		envVars.put("appName", appName);
		envVars.put("appVersion", appVersion);
		envVars.put("appEnv", appEnv);
		envVars.put("mysqlDatabase", mysqlDatabase);
		
		// Config 클래스를 통한 값
		envVars.put("configAppName", appConfig.getAppName());
		envVars.put("configAppVersion", appConfig.getAppVersion());
		envVars.put("configAppEnv", appConfig.getAppEnv());
		
		return envVars;
	}
}

