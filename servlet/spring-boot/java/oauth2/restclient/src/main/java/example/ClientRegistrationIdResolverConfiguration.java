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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor.ClientRegistrationIdResolver;
import org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver;

/**
 * Configuration for demonstrating custom strategies for resolving a
 * {@code clientRegistrationId} via the {@link ClientRegistrationIdResolver}. This sample
 * uses the following profiles to demonstrate multiple configurations:
 *
 * <ol>
 * <li>{@code default} - Demonstrates the default setup with
 * {@link RequestAttributeClientRegistrationIdResolver}. Uses {@code login-client} as the
 * {@code clientRegistrationId} to log in and {@code messaging-client} for
 * authorization.</li>
 * <li>{@code current-user} - Demonstrates a custom {@link ClientRegistrationIdResolver}
 * that simply resolves the {@code clientRegistrationId} from the current user. Uses
 * {@code login-client-with-messaging} to log in.</li>
 * <li>{@code composite} - Demonstrates a composite {@link ClientRegistrationIdResolver}
 * that tries multiple ways of resolving a {@code clientRegistrationId}. Uses
 * {@code login-client-with-messaging} to log in.</li>
 * <li>{@code authentication-required} - Demonstrates a custom
 * {@link ClientRegistrationIdResolver} that requires authentication using OAuth 2.0 or
 * Open ID Connect 1.0. Uses {@code login-client-with-messaging} to log in.</li>
 * </ol>
 *
 * @author Steve Riesenberg
 */
@Configuration
public class ClientRegistrationIdResolverConfiguration {

	/**
	 * This demonstrates a custom {@link ClientRegistrationIdResolver} that simply
	 * resolves the {@code clientRegistrationId} from the current user.
	 * @return a custom {@link ClientRegistrationIdResolver}
	 */
	private static ClientRegistrationIdResolver currentUserClientRegistrationIdResolver() {
		SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
		return (request) -> {
			Authentication authentication = securityContextHolderStrategy.getContext().getAuthentication();
			return (authentication instanceof OAuth2AuthenticationToken principal)
					? principal.getAuthorizedClientRegistrationId() : null;
		};
	}

	/**
	 * This demonstrates a composite {@link ClientRegistrationIdResolver} that tries to
	 * resolve the {@code clientRegistrationId} in multiple ways.
	 * <p>
	 * <ol>
	 * <li>resolve the {@code clientRegistrationId} from attributes</li>
	 * <li>use the {@code clientRegistrationId} from OAuth 2.0 or OpenID Connect 1.0
	 * Login</li>
	 * <li>use the default {@code clientRegistrationId}</li>
	 * </ol>
	 * @param defaultClientRegistrationId the default {@code clientRegistrationId}
	 * @return a custom {@link ClientRegistrationIdResolver}
	 */
	private static ClientRegistrationIdResolver compositeClientRegistrationIdResolver(
			String defaultClientRegistrationId) {
		ClientRegistrationIdResolver requestAttributes = new RequestAttributeClientRegistrationIdResolver();
		ClientRegistrationIdResolver currentUser = currentUserClientRegistrationIdResolver();
		return (request) -> {
			String clientRegistrationId = requestAttributes.resolve(request);
			if (clientRegistrationId == null) {
				clientRegistrationId = currentUser.resolve(request);
			}
			if (clientRegistrationId == null) {
				clientRegistrationId = defaultClientRegistrationId;
			}
			return clientRegistrationId;
		};
	}

	/**
	 * This demonstrates a custom {@link ClientRegistrationIdResolver} that requires
	 * authentication using OAuth 2.0 or Open ID Connect 1.0. If the user is not logged
	 * in, they are sent to the login page prior to obtaining an access token.
	 * @return a custom {@link ClientRegistrationIdResolver}
	 */
	private static ClientRegistrationIdResolver authenticationRequiredClientRegistrationIdResolver() {
		ClientRegistrationIdResolver currentUser = currentUserClientRegistrationIdResolver();
		return (request) -> {
			String clientRegistrationId = currentUser.resolve(request);
			if (clientRegistrationId == null) {
				throw new AccessDeniedException(
						"Authentication with OAuth 2.0 or OpenID Connect 1.0 Login is required");
			}
			return clientRegistrationId;
		};
	}

	@Configuration
	@Profile("current-user")
	public static class CurrentUserConfiguration {

		@Bean
		public ClientRegistrationIdResolver clientRegistrationIdResolver() {
			return currentUserClientRegistrationIdResolver();
		}

	}

	@Configuration
	@Profile("composite")
	public static class CompositeConfiguration {

		@Bean
		public ClientRegistrationIdResolver clientRegistrationIdResolver() {
			return compositeClientRegistrationIdResolver("messaging-client");
		}

	}

	@Configuration
	@Profile("authentication-required")
	public static class AuthenticationRequiredConfiguration {

		@Bean
		public ClientRegistrationIdResolver clientRegistrationIdResolver() {
			return authenticationRequiredClientRegistrationIdResolver();
		}

	}

}
