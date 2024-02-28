/*
 * Copyright 2023 the original author or authors.
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

package com.example.maximumsessions;

import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.session.InMemoryReactiveSessionRegistry;
import org.springframework.security.core.session.ReactiveSessionRegistry;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.InvalidateLeastUsedServerMaximumSessionsExceededHandler;
import org.springframework.security.web.server.authentication.PreventLoginServerMaximumSessionsExceededHandler;
import org.springframework.security.web.server.authentication.ServerMaximumSessionsExceededHandler;
import org.springframework.security.web.server.authentication.SessionLimit;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import org.springframework.web.server.session.DefaultWebSessionManager;
import org.springframework.web.server.session.WebSessionManager;

@Configuration(proxyBeanMethods = false)
@EnableWebFluxSecurity
public class SecurityConfig {

	@Value("${prevent-login:false}")
	private boolean preventLogin;

	@Bean
	SecurityWebFilterChain filterChain(ServerHttpSecurity http,
			ServerMaximumSessionsExceededHandler maximumSessionsExceededHandler) {
		// @formatter:off
		http
				.authorizeExchange((exchanges) -> exchanges.anyExchange().authenticated())
				.formLogin(Customizer.withDefaults())
				.sessionManagement((sessions) -> sessions
						.concurrentSessions((concurrency) -> concurrency
							.maximumSessions(maxSessions())
							.maximumSessionsExceededHandler(maximumSessionsExceededHandler))
				);
		return http.build();
		// @formatter:on
	}

	@Bean
	ReactiveUserDetailsService userDetailsService() {
		UserDetails user = User.withDefaultPasswordEncoder()
			.username("user")
			.password("password")
			.roles("USER")
			.build();
		UserDetails admin = User.withDefaultPasswordEncoder()
			.username("admin")
			.password("password")
			.roles("ADMIN")
			.build();
		UserDetails unlimitedSessions = User.withDefaultPasswordEncoder()
			.username("unlimited")
			.password("password")
			.roles("UNLIMITED_SESSIONS")
			.build();
		return new MapReactiveUserDetailsService(user, admin, unlimitedSessions);
	}

	@Bean
	ReactiveSessionRegistry reactiveSessionRegistry() {
		return new InMemoryReactiveSessionRegistry();
	}

	private SessionLimit maxSessions() {
		return (authentication) -> {
			if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_UNLIMITED_SESSIONS"))) {
				return Mono.empty();
			}
			if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
				return Mono.just(2);
			}
			return Mono.just(1);
		};
	}

	@Bean
	ServerMaximumSessionsExceededHandler maximumSessionsExceededHandler(
			@Qualifier(WebHttpHandlerBuilder.WEB_SESSION_MANAGER_BEAN_NAME) WebSessionManager webSessionManager) {
		if (this.preventLogin) {
			return new PreventLoginServerMaximumSessionsExceededHandler();
		}
		return new InvalidateLeastUsedServerMaximumSessionsExceededHandler(
				((DefaultWebSessionManager) webSessionManager).getSessionStore());
	}

}
