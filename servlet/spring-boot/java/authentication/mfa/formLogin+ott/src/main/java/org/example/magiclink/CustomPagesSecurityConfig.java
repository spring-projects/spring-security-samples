package org.example.magiclink;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Profile("custom-pages")
@Configuration(proxyBeanMethods = false)
public class CustomPagesSecurityConfig {

	@Controller
	@Profile("custom-pages")
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
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// @formatter:off
		http
			.authorizeHttpRequests((authz) -> authz.anyRequest().authenticated())
			.formLogin((form) -> form
				.loginPage("/login/form").permitAll()
				.factor(Customizer.withDefaults())
			)
			.oneTimeTokenLogin((ott) -> ott
				.loginPage("/login/ott").permitAll()
				.factor(Customizer.withDefaults())
			);
		// @formatter:on
		return http.build();
	}
}
