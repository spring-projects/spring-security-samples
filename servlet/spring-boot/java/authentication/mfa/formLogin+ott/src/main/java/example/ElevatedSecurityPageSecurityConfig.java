package example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Profile("elevated-security")
@Configuration(proxyBeanMethods = false)
public class ElevatedSecurityPageSecurityConfig {

	@Controller
	@Profile("elevated-security")
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
				.requestMatchers("/profile").hasAuthority("FACTOR_OTT")
				.anyRequest().authenticated()
			)
			.formLogin((form) -> form.loginPage("/auth/password"))
			.oneTimeTokenLogin((ott) -> ott.loginPage("/auth/ott"));

		// @formatter:on
		return http.build();
	}

}
