package example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authorization.EnableGlobalMultiFactorAuthentication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import static org.springframework.security.core.GrantedAuthorities.FACTOR_OTT_AUTHORITY;
import static org.springframework.security.core.GrantedAuthorities.FACTOR_PASSWORD_AUTHORITY;

@Profile("custom-pages")
@Configuration(proxyBeanMethods = false)
@EnableGlobalMultiFactorAuthentication(authorities = { FACTOR_PASSWORD_AUTHORITY, FACTOR_OTT_AUTHORITY })
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

}
