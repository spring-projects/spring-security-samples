package org.example.magiclink;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Profile("elevated-security")
@Configuration(proxyBeanMethods = false)
public class ElevatedSecurityPageSecurityConfig {

	@Controller
	@Profile("elevated-security")
	static class LoginController {
		@GetMapping("/login/form")
		public String login() {
			return "login";
		}

		@GetMapping("/login/ott")
		public String ott() {
			return "ott";
		}
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthorizationManagerFactory authz) throws Exception {
		// @formatter:off
		http
			.authorizeHttpRequests((authorize) -> authorize
				.anyRequest().access(authz.authenticated())
			)
			.formLogin((form) -> form.loginPage("/login/form").permitAll())
			.oneTimeTokenLogin((ott) -> ott.loginPage("/login/ott").permitAll());

		// @formatter:on
		return http.build();
	}

	@Bean
	AuthorizationManagerFactory authz() {
		return new AuthorizationManagerFactory("FACTOR_PASSWORD", "FACTOR_OTT");
	}
}
