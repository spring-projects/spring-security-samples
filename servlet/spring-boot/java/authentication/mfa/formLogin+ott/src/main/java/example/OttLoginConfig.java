package example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Configuration(proxyBeanMethods = false)
@Controller
@Profile("custom-pages")
class OttLoginConfig {

	static final String PATH = "/auth/ott";

	@GetMapping(PATH)
	String auth() {
		return "ott";
	}

	@Bean
	Customizer<HttpSecurity> ottLogin() {
		// @formatter:off
        return (http) -> http
            .authorizeHttpRequests((authz) -> authz.requestMatchers(PATH).permitAll())
            .oneTimeTokenLogin((ott) -> ott.loginPage(PATH));
        // @formatter:on
	}

}