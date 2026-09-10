/*
 * Copyright 2002-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Security configuration for OAuth2 Login and Resource Server.
 */
@Configuration
@EnableWebSecurity
public class OAuth2ResourceServerLoginSecurityConfiguration {

	@Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
	String jwkSetUri;

	@Bean
	@Order(1)
	SecurityFilterChain resourceServerFilterChain(HttpSecurity http) throws Exception {
		// @formatter:off
		http
				.securityMatcher("/api/**")
				.authorizeHttpRequests((authorize) -> authorize
						.requestMatchers(HttpMethod.GET, "/api/message/**").hasAuthority("SCOPE_message:read")
						.requestMatchers(HttpMethod.POST, "/api/message/**").hasAuthority("SCOPE_message:write")
						.anyRequest().authenticated()
				)
				.csrf((csrf) -> csrf.disable())
				.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.oauth2ResourceServer((oauth2) -> oauth2.jwt(withDefaults()));
		// @formatter:on
		return http.build();
	}

	@Bean
	@Order(2)
	SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
		// @formatter:off
		http
				.authorizeHttpRequests((authorize) -> authorize
						.requestMatchers("/", "/login**", "/webjars/**", "/assets/**", "/error").permitAll()
						.anyRequest().authenticated()
				)
				.oauth2Login(withDefaults());
		// @formatter:on
		return http.build();
	}

	@Bean
	JwtDecoder jwtDecoder() {
		return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri).build();
	}

}
