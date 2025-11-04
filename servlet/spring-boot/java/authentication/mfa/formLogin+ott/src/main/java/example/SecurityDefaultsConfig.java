/*
 * Copyright 2024 the original author or authors.
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

import java.io.IOException;
import java.time.Duration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagerFactories;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authorization.EnableMultiFactorAuthentication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;

import static org.springframework.security.core.authority.FactorGrantedAuthority.OTT_AUTHORITY;
import static org.springframework.security.core.authority.FactorGrantedAuthority.PASSWORD_AUTHORITY;

@Configuration(proxyBeanMethods = false)
@EnableMultiFactorAuthentication(authorities = { PASSWORD_AUTHORITY, OTT_AUTHORITY })
class SecurityDefaultsConfig {

	@Bean
	SecurityFilterChain app(HttpSecurity http, AuthorizationManager<Object> passwordIn5m) {
		http.authorizeHttpRequests(
				(authz) -> authz.requestMatchers("/profile").access(passwordIn5m).anyRequest().authenticated())
			.formLogin(Customizer.withDefaults())
			.oneTimeTokenLogin(Customizer.withDefaults());
		return http.build();
	}

	@Bean
	AuthorizationManager<Object> passwordIn5m() {
		return AuthorizationManagerFactories.multiFactor()
			.requireFactor((f) -> f.passwordAuthority().validDuration(Duration.ofMinutes(5)))
			.requireFactor((f) -> f.ottAuthority())
			.build()
			.authenticated();
	}

	@Bean
	UserDetailsService users() {
		return new InMemoryUserDetailsManager(
				User.withDefaultPasswordEncoder().username("user").password("password").authorities("app").build());
	}

	@Bean
	OneTimeTokenGenerationSuccessHandler ottSuccessHandler() {
		return new LoggingOneTimeTokenGenerationSuccessHandler();
	}

	static final class LoggingOneTimeTokenGenerationSuccessHandler implements OneTimeTokenGenerationSuccessHandler {

		private static final String TOKEN_TEMPLATE = """
				********************************************************

				Use this one-time token: %s

				********************************************************""";

		private final Log logger = LogFactory.getLog(this.getClass());

		@Override
		public void handle(HttpServletRequest request, HttpServletResponse response, OneTimeToken oneTimeToken)
				throws IOException {
			this.logger.info(String.format(TOKEN_TEMPLATE, oneTimeToken.getTokenValue()));
			response.sendRedirect("/login/ott");
		}

	}

}
