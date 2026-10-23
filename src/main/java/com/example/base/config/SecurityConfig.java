package com.example.base.config;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.example.base.config.property.JwtProperties;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private static final String AUTH_PATH = "/api/auth/**";
	private static final String[] SWAGGER_PATHS = {
			"/v3/api-docs/**",
			"/swagger-ui/**",
			"/swagger-ui.html"
	};

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http,
			@Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) throws Exception {
		AuthenticationEntryPoint authenticationEntryPoint = authenticationEntryPoint(exceptionResolver);
		AccessDeniedHandler accessDeniedHandler = accessDeniedHandler(exceptionResolver);
		http
			.cors(Customizer.withDefaults())
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
					.requestMatchers(AUTH_PATH, "/error").permitAll()
					.requestMatchers(SWAGGER_PATHS).permitAll()
					.requestMatchers(HttpMethod.GET, "/api/events", "/api/events/{id}").permitAll()
					.requestMatchers(HttpMethod.GET, "/api/categories", "/api/venues").permitAll()
					.anyRequest().authenticated())
			.exceptionHandling(exceptions -> exceptions
					.authenticationEntryPoint(authenticationEntryPoint)
					.accessDeniedHandler(accessDeniedHandler))
			.oauth2ResourceServer(oauth2 -> oauth2
					.bearerTokenResolver(bearerTokenResolver())
					.authenticationEntryPoint(authenticationEntryPoint)
					.accessDeniedHandler(accessDeniedHandler)
					.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

		return http.build();
	}

	@Bean
	JwtEncoder jwtEncoder(JwtProperties properties) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey(properties)));
	}

	@Bean
	JwtDecoder jwtDecoder(JwtProperties properties) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey(properties))
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
		decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.issuer()));

		return decoder;
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// get roles and thêm prefix ROLE_
	private JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
		authoritiesConverter.setAuthoritiesClaimName("roles");
		authoritiesConverter.setAuthorityPrefix("ROLE_");

		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

		return converter;
	}

	private BearerTokenResolver bearerTokenResolver() {
		DefaultBearerTokenResolver defaultResolver = new DefaultBearerTokenResolver();
		RequestMatcher authEndpoints = PathPatternRequestMatcher.withDefaults()
				.matcher(AUTH_PATH);

		return request -> authEndpoints.matches(request) ?
				null : defaultResolver.resolve(request);
	}

	// handle exception ném ra từ các filter của spring security
	private AuthenticationEntryPoint authenticationEntryPoint(HandlerExceptionResolver exceptionResolver) {
		AuthenticationEntryPoint bearerEntryPoint = new BearerTokenAuthenticationEntryPoint();
		return (request, response, ex) -> {
			bearerEntryPoint.commence(request, response, ex);
			exceptionResolver.resolveException(request, response, null, ex);
		};
	}

	private AccessDeniedHandler accessDeniedHandler(HandlerExceptionResolver exceptionResolver) {
		AccessDeniedHandler bearerHandler = new BearerTokenAccessDeniedHandler();
		return (request, response, ex) -> {
			bearerHandler.handle(request, response, ex);
			exceptionResolver.resolveException(request, response, null, ex);
		};
	}

	private SecretKey secretKey(JwtProperties properties) {
		byte[] keyBytes;
		try {
			keyBytes = Base64.getDecoder().decode(properties.secret());
		}
		catch (IllegalArgumentException ex) {
			throw new IllegalStateException("app.jwt.secret phải là chuỗi Base64, tạo bằng lệnh: openssl rand -base64 32", ex);
		}
		if (keyBytes.length < 32) {
			throw new IllegalStateException("app.jwt.secret phải dài ít nhất 32 byte (256 bit) cho HS256");
		}

		return new SecretKeySpec(keyBytes, "HmacSHA256");
	}

}
