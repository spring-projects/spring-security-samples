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
class FormLoginConfig {
    static final String PATH = "/auth/password";

    @GetMapping(PATH)
    String auth() {
        return "password";
    }

    @Bean
    Customizer<HttpSecurity> formLogin() {
        // @formatter:off
        return (http) -> http
            .authorizeHttpRequests((authz) -> authz.requestMatchers(PATH).permitAll())
            .formLogin((form) -> form.loginPage(PATH));
        // @formatter:on
    }
}