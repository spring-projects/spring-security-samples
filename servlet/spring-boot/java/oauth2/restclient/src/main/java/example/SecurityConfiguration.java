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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for {@link OAuth2RestClientApplication}.
 *
 * @author Steve Riesenberg
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	private final String loginPage;

	public SecurityConfiguration(@Value("${app.login-page}") String loginPage) {
		this.loginPage = loginPage;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// @formatter:off
		http
			.authorizeHttpRequests((authorize) -> authorize
				.requestMatchers("/", "/public/**", "/error").permitAll()
				.anyRequest().authenticated()
			)
			.oauth2Login((oauth2Login) -> oauth2Login
				/*
				 * Customize the login page used in the redirect when the AuthenticationEntryPoint
				 * is triggered. This sample switches the URL based on the profile.
				 *
				 * See application.yml.
				 */
				.loginPage(this.loginPage)
			)
			.oauth2Client(Customizer.withDefaults());
		// @formatter:on

		return http.build();
	}

}
