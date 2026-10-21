package com.example.base.support;

import org.testcontainers.mysql.MySQLContainer;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;

// Chạy MySQL thật trong Docker cho test. Spring cache context nên mọi test class dùng chung một container
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	MySQLContainer mysqlContainer() {
		return new MySQLContainer("mysql:8.4");
	}

}
