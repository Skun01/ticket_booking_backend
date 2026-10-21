package com.example.base.config;

import java.time.Duration;

import com.example.base.config.property.JwtProperties;
import org.junit.jupiter.api.Test;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class JwtSecretTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withUserConfiguration(PropertiesConfig.class)
			.withPropertyValues("app.jwt.issuer=test", "app.jwt.access-token-ttl=15m", "app.jwt.refresh-token-ttl=7d");

	@Test
	void missingSecretFailsStartup() {
		contextRunner.run(context -> assertThat(context).getFailure()
				.rootCause()
				.hasMessageContaining("app.jwt")
				.hasMessageContaining("secret"));
	}

	@Test
	void secretThatIsNotBase64IsRejected() {
		assertThatIllegalStateException()
				.isThrownBy(() -> new SecurityConfig().jwtDecoder(properties("not_base64_secret")))
				.withMessageContaining("Base64");
	}

	@Test
	void secretShorterThan256BitsIsRejected() {
		assertThatIllegalStateException()
				.isThrownBy(() -> new SecurityConfig().jwtDecoder(properties("c2hvcnQ=")))
				.withMessageContaining("32 byte");
	}

	private static JwtProperties properties(String secret) {
		return new JwtProperties(secret, "test", Duration.ofMinutes(15), Duration.ofDays(7), true);
	}

	@Configuration(proxyBeanMethods = false)
	@EnableConfigurationProperties(JwtProperties.class)
	static class PropertiesConfig {
	}

}
