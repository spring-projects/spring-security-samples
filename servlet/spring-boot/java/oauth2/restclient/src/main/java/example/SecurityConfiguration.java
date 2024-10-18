/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Security configuration for {@link OAuth2RestClientApplication}.
 *
 * @author Steve Riesenberg
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	private final String loginPage;

	public SecurityConfiguration(@Value("${app.login-page}") String loginPage) {
		this.loginPage = loginPage;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// @formatter:off
		http
			.authorizeHttpRequests((authorize) -> authorize
				.requestMatchers("/", "/public/**", "/error").permitAll()
				.anyRequest().authenticated()
			)
			.oauth2Login((oauth2Login) -> oauth2Login
				/*
				 * Customize the login page used in the redirect when the AuthenticationEntryPoint
				 * is triggered. This sample switches the URL based on the profile.
				 *
				 * See application.yml.
				 */
				.loginPage(this.loginPage)
			)
			.oauth2Client(Customizer.withDefaults());
		// @formatter:on

		return http.build();
	}

	@Bean
	public OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> clientCredentialsAccessTokenResponseClient() {
		RequestMatcher requestMatcher = (request) -> false;
		Converter<OAuth2ClientCredentialsGrantRequest, MultiValueMap<String, String>> parametersConverter = (grantRequest) -> {
			LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
			if (grantRequest.getClientRegistration().getRegistrationId().equals("okta")) {
				parameters.set(OAuth2ParameterNames.CLIENT_ID, "my-client");
			}
			return parameters;
		};

		RestClientClientCredentialsTokenResponseClient accessTokenResponseClient =
			new RestClientClientCredentialsTokenResponseClient();
		accessTokenResponseClient.setParametersConverter(parametersConverter);

		return accessTokenResponseClient;
	}

	private static Converter<OAuth2ClientCredentialsGrantRequest, MultiValueMap<String, String>> parametersConverter() {
		RequestMatcher requestMatcher = (request) -> false;
		return (grantRequest) -> {
			ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
			if (requestMatcher.matches(requestAttributes.getRequest())) {
				parameters.set(OAuth2ParameterNames.SCOPE, "scope-1 scope-2");
			}
			return parameters;
		};
	}

	@Bean
	public RestClient restClient() {
		OAuth2AccessTokenResponseHttpMessageConverter messageConverter =
			new OAuth2AccessTokenResponseHttpMessageConverter();
		messageConverter.setAccessTokenResponseConverter((parameters) -> {
			// ...
			return OAuth2AccessTokenResponse.withToken("custom-token")
				// ...
				.build();
		});

		return RestClient.builder()
			.messageConverters((messageConverters) -> {
				messageConverters.clear();
				messageConverters.add(new FormHttpMessageConverter());
				messageConverters.add(messageConverter);
			})
			.defaultStatusHandler(new OAuth2ErrorResponseErrorHandler())
			.build();
	}

}
