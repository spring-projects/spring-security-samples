package com.spring.sample.b2c.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${logout-success-url}")
    private String logoutSuccessUrl;

    @Value("#{${additional-Parameters}}")
    private Map<String,String> additionalParameter;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
                .authorizeRequests()
                    .regexMatchers("/login").permitAll()
                    .anyRequest().authenticated()
                    .and()
                .logout()
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                    .logoutSuccessUrl(this.logoutSuccessUrl)
                    .and()
                .oauth2Login()
                    .authorizationEndpoint()
                    .authorizationRequestResolver(
                            new CustomAuthorizationRequestResolver(
                                    this.clientRegistrationRepository));
        // @formatter:off
    }

    public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
        private final OAuth2AuthorizationRequestResolver defaultAuthorizationRequestResolver;

        public CustomAuthorizationRequestResolver(
                ClientRegistrationRepository clientRegistrationRepository) {

            this.defaultAuthorizationRequestResolver =
                    new DefaultOAuth2AuthorizationRequestResolver(
                            clientRegistrationRepository, "/oauth2/authorization");
        }

        @Override
        public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
            OAuth2AuthorizationRequest authorizationRequest =
                    this.defaultAuthorizationRequestResolver.resolve(request);

            return authorizationRequest != null ?
                    customAuthorizationRequest(authorizationRequest) :
                    null;
        }

        @Override
        public OAuth2AuthorizationRequest resolve(
                HttpServletRequest request, String clientRegistrationId) {

            OAuth2AuthorizationRequest authorizationRequest =
                    this.defaultAuthorizationRequestResolver.resolve(request, clientRegistrationId);

            return authorizationRequest != null ?
                    customAuthorizationRequest(authorizationRequest) :
                    null;
        }

        private OAuth2AuthorizationRequest customAuthorizationRequest(
                OAuth2AuthorizationRequest authorizationRequest) {

            Map<String, Object> additionalParameters =
                    new LinkedHashMap<>(authorizationRequest.getAdditionalParameters());
            additionalParameters.putAll(WebSecurityConfiguration.this.additionalParameter);

            return OAuth2AuthorizationRequest.from(authorizationRequest)
                    .additionalParameters(additionalParameters)
                    .build();
        }
    }
}
