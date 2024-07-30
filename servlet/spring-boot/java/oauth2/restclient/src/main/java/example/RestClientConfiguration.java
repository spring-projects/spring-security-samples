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
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizationFailureHandler;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver;
import org.springframework.web.client.RestClient;

/**
 * Configuration for providing a {@link RestClient} bean.
 *
 * @author Steve Riesenberg
 */
@Configuration
public class RestClientConfiguration {

	private final String baseUrl;

	public RestClientConfiguration(@Value("${messages.base-url}") String baseUrl) {
		this.baseUrl = baseUrl;
	}

	@Bean
	public RestClient restClient(OAuth2AuthorizedClientManager authorizedClientManager,
			OAuth2AuthorizedClientRepository authorizedClientRepository,
			OAuth2ClientHttpRequestInterceptor.ClientRegistrationIdResolver clientRegistrationIdResolver,
			RestClient.Builder builder) {

		OAuth2ClientHttpRequestInterceptor requestInterceptor = new OAuth2ClientHttpRequestInterceptor(
				authorizedClientManager, clientRegistrationIdResolver);

		OAuth2AuthorizationFailureHandler authorizationFailureHandler = OAuth2ClientHttpRequestInterceptor
			.authorizationFailureHandler(authorizedClientRepository);
		requestInterceptor.setAuthorizationFailureHandler(authorizationFailureHandler);

		return builder.baseUrl(this.baseUrl).requestInterceptor(requestInterceptor).build();
	}

	/**
	 * This sample uses profiles to demonstrate additional strategies for resolving the
	 * {@code clientRegistrationId}. See {@link ClientRegistrationIdResolverConfiguration}
	 * for alternate implementations.
	 * @return the default {@link ClientRegistrationIdResolverConfiguration}
	 * @see ClientRegistrationIdResolverConfiguration
	 */
	@Bean
	@ConditionalOnMissingBean
	public OAuth2ClientHttpRequestInterceptor.ClientRegistrationIdResolver clientRegistrationIdResolver() {
		return new RequestAttributeClientRegistrationIdResolver();
	}

}
