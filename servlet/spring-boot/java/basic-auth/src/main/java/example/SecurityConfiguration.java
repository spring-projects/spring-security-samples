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
// tag::sans-header[]
package example;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration.
 *
 * @author Steve Riesenberg
 */
@EnableWebSecurity
public class SecurityConfiguration {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// @formatter:off
		http
			.authorizeHttpRequests((authorize) -> authorize     // <1>
				.mvcMatchers("/", "/home").permitAll()          // <2>
				.anyRequest().authenticated()                   // <3>
			)
			.formLogin((formLogin) -> formLogin                 // <4>
				.loginPage("/login")                            // <5>
				.permitAll()
			)
			.logout(LogoutConfigurer::permitAll);               // <6>
		// @formatter:on

		return http.build();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		// @formatter:off
		UserDetails userDetails =                               // <7>
			User.withDefaultPasswordEncoder()                   // <8>
				.username("user")                               // <9>
				.password("password")                           // <10>
				.roles("USER")                                  // <11>
				.build();                                       // <12>
		// @formatter:on

		return new InMemoryUserDetailsManager(userDetails);
	}

}
// end::sans-header[]
