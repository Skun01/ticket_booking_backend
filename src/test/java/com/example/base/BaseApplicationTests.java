package com.example.base;

import com.example.base.support.IntegrationTest;
import org.junit.jupiter.api.Test;

// Context khởi động được nghĩa là Liquibase chạy xong và entity khớp schema (ddl-auto: validate)
@IntegrationTest
class BaseApplicationTests {

	@Test
	void contextLoads() {
	}

}
