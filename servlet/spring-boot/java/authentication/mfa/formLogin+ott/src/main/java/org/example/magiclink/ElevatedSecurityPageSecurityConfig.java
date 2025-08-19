package org.example.magiclink;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.MfaConfigurer;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import static org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.pathPattern;

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
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// @formatter:off
		http
			.authorizeHttpRequests((authz) -> authz
				.requestMatchers("/profile").hasAuthority("profile:read")
				.anyRequest().authenticated()
			)
			.formLogin((form) -> form
				.loginPage("/login/form").permitAll()
				.factor((f) -> f.grants(Duration.ofMinutes(1), "profile:read"))
			)
			.oneTimeTokenLogin((ott) -> ott
				.loginPage("/login/ott").permitAll()
				.factor(Customizer.withDefaults())
			);

		// @formatter:on
		return http.build();
	}

}
