/*
 * Copyright 2020 the original author or authors.
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
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Minimal method security configuration.
 *
 * @author Rob Winch
 * @since 5.0
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {

	@Bean
	SecurityFilterChain springFilterChain(HttpSecurity http) throws Exception {
		// @formatter:off
		http
			// Demonstrate that method security works
			// Best practice to use both for defense in depth
			.authorizeHttpRequests((authorize) -> authorize
				.anyRequest().permitAll()
			)
			.httpBasic(withDefaults());
		// @formatter:on
		return http.build();
	}

	@Bean
	UserDetailsService userDetailsService() {
		// @formatter:off
		UserDetails user = User.withDefaultPasswordEncoder()
			.username("user")
			.password("password")
			.roles("USER")
			.build();
		UserDetails admin = User.withDefaultPasswordEncoder()
			.username("admin")
			.password("password")
			.roles("ADMIN", "USER")
			.build();
		// @formatter:on
		return new InMemoryUserDetailsManager(user, admin);
	}

}
