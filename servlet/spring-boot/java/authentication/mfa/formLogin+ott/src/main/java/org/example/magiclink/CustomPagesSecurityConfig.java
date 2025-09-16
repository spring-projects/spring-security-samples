package org.example.magiclink;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
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

		@GetMapping("/commence/ott")
		public String ott(HttpServletRequest request, Authentication authentication) {
			if (authentication != null) {
				request.setAttribute("username", authentication.getName());
			}
			return "ott";
		}
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// @formatter:off
		http
			.authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated())
			.formLogin((form) -> form.loginPage("/login/form").permitAll())
			.oneTimeTokenLogin((ott) -> ott.loginPage("/commence/ott").permitAll());
		// @formatter:on
		return http.build();
	}

	@Bean
	FactorAuthorizationManagerFactory authz() {
		return new FactorAuthorizationManagerFactory("FACTOR_PASSWORD", "FACTOR_OTT");
	}
}
