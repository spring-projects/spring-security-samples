package org.example.magiclink;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authorization.AuthorizationManagerFactory;
import org.springframework.security.authorization.DefaultAuthorizationManagerFactory;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Profile("custom-pages")
@Configuration(proxyBeanMethods = false)
public class CustomPagesSecurityConfig {

	@Controller
	@Profile("custom-pages")
	static class LoginController {
		@GetMapping("/auth/{path}")
		public String auth(@PathVariable("path") String path) {
			return path;
		}
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// @formatter:off
		http
			.authorizeHttpRequests((authorize) -> authorize
				.requestMatchers("/auth/**").permitAll()
				.anyRequest().authenticated()
			)
			.formLogin((form) -> form.loginPage("/auth/password"))
			.oneTimeTokenLogin((ott) -> ott.loginPage("/auth/ott"));
		// @formatter:on
		return http.build();
	}

	@Bean
	AuthorizationManagerFactory<Object> factors() {
		return DefaultAuthorizationManagerFactory.builder()
			.requireAdditionalAuthorities("FACTOR_PASSWORD", "FACTOR_OTT").build();
	}
}
