package example;

import java.io.IOException;
import java.time.Duration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagerFactories;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authorization.EnableGlobalMultiFactorAuthentication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;

import static org.springframework.security.core.authority.FactorGrantedAuthority.OTT_AUTHORITY;
import static org.springframework.security.core.authority.FactorGrantedAuthority.PASSWORD_AUTHORITY;

@Configuration(proxyBeanMethods = false)
@EnableGlobalMultiFactorAuthentication(authorities = { PASSWORD_AUTHORITY, OTT_AUTHORITY })
class SecurityDefaultsConfig {

	@Bean
	SecurityFilterChain app(HttpSecurity http, AuthorizationManager<Object> passwordIn5m) {
		http.authorizeHttpRequests(
				(authz) -> authz.requestMatchers("/profile").access(passwordIn5m).anyRequest().authenticated())
			.formLogin(Customizer.withDefaults())
			.oneTimeTokenLogin(Customizer.withDefaults());
		return http.build();
	}

	@Bean
	AuthorizationManager<Object> passwordIn5m() {
		return AuthorizationManagerFactories.multiFactor()
			.requireFactor((f) -> f.passwordAuthority().validDuration(Duration.ofMinutes(5)))
			.requireFactor((f) -> f.ottAuthority())
			.build()
			.authenticated();
	}

	@Bean
	UserDetailsService users() {
		return new InMemoryUserDetailsManager(
				User.withDefaultPasswordEncoder().username("user").password("password").authorities("app").build());
	}

	@Bean
	OneTimeTokenGenerationSuccessHandler ottSuccessHandler() {
		return new LoggingOneTimeTokenGenerationSuccessHandler();
	}

	static final class LoggingOneTimeTokenGenerationSuccessHandler implements OneTimeTokenGenerationSuccessHandler {

		private static final String TOKEN_TEMPLATE = """
				********************************************************

				Use this one-time token: %s

				********************************************************""";

		private final Log logger = LogFactory.getLog(this.getClass());

		@Override
		public void handle(HttpServletRequest request, HttpServletResponse response, OneTimeToken oneTimeToken)
				throws IOException {
			this.logger.info(String.format(TOKEN_TEMPLATE, oneTimeToken.getTokenValue()));
			response.sendRedirect("/login/ott");
		}

	}

}
