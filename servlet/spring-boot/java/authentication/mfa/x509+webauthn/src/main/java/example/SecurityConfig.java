/*
 * Copyright 2021 the original author or authors.
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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Configuration
public class SecurityConfig {

	@Controller
	static class LoginController {
		@GetMapping("/webauthn")
		String webauthn() {
			return "webauthn";
		}
	}

	@Bean
	SecurityFilterChain web(HttpSecurity http) throws Exception {
		// @formatter:off
		http
			.authorizeHttpRequests((authorize) -> authorize
				.requestMatchers("/webauthn/**").permitAll()
				.anyRequest().authenticated())
			.x509(Customizer.withDefaults())
			.formLogin(Customizer.withDefaults())
			.webAuthn((webauthn) -> webauthn
				.rpId("api.127.0.0.1.nip.io")
				.rpName("X.509+WebAuthn MFA Sample")
				.allowedOrigins("https://api.127.0.0.1.nip.io:8443")
			)
			.exceptionHandling((exceptions) -> exceptions
				.defaultAuthenticationEntryPointFor(new LoginUrlAuthenticationEntryPoint("/webauthn"), "FACTOR_WEBAUTHN")
			);
		// @formatter:on
		return http.build();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		return new InMemoryUserDetailsManager(
			User.withDefaultPasswordEncoder()
				.username("josh")
				.password("password")
				.authorities("app")
				.build()
		);
	}

	@Bean
	FactorAuthorizationManagerFactory authorizationManagerFactory() {
		return new FactorAuthorizationManagerFactory("FACTOR_X509", "FACTOR_WEBAUTHN");
	}
}
