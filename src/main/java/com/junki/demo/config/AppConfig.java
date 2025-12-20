package com.junki.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

	@Value("${app.name}")
	private String appName;

	@Value("${app.version}")
	private String appVersion;

	@Value("${app.env}")
	private String appEnv;

	@Value("${MYSQL_DATABASE:spubgv2}")
	private String mysqlDatabase;

	@Value("${MYSQL_USER:root}")
	private String mysqlUser;

	// Getter methods
	public String getAppName() {
		return appName;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public String getAppEnv() {
		return appEnv;
	}

	public String getMysqlDatabase() {
		return mysqlDatabase;
	}

	public String getMysqlUser() {
		return mysqlUser;
	}
}

