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

import java.io.IOException;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

@Configuration(proxyBeanMethods = false)
class SecurityConfig {

	static final String SCOPE = "https://www.googleapis.com/auth/gmail.readonly";

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationEntryPoint oauth2) throws Exception {
		// @formatter:off
		http
			.authorizeHttpRequests((authz) -> authz
				.requestMatchers("/profile").hasAuthority("SCOPE_" + SCOPE)
				.anyRequest().authenticated()
			)
			.oauth2Login(Customizer.withDefaults())
			.exceptionHandling((exceptions) -> exceptions
				.defaultDeniedHandlerForMissingAuthority(oauth2, "SCOPE_" + SCOPE)
			);
		// @formatter:on
		return http.build();
	}

	@Bean
	ClientRegistrationRepository clients() {
		ClientRegistration google = CommonOAuth2Provider.GOOGLE.getBuilder("google")
			.clientId(System.getenv().getOrDefault("GOOGLE_CLIENT_ID", "id"))
			.clientSecret(System.getenv().getOrDefault("GOOGLE_CLIENT_SECRET", "secret"))
			.scope("openid", "profile", "email", SCOPE)
			.build();
		return new InMemoryClientRegistrationRepository(google);
	}

	@Component
	static class OAuth2ScopeAuthenticationEntryPoint implements AuthenticationEntryPoint {

		private final ClientRegistration google;

		private final OAuth2AuthorizationRequestResolver authorizationRequestResolver;

		private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository = new HttpSessionOAuth2AuthorizationRequestRepository();

		OAuth2ScopeAuthenticationEntryPoint(ClientRegistrationRepository clients) {
			this.google = clients.findByRegistrationId("google");
			this.authorizationRequestResolver = new DefaultOAuth2AuthorizationRequestResolver(clients);
		}

		@Override
		public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
				throws IOException, ServletException {
			OAuth2AuthorizationRequest oauth2 = this.authorizationRequestResolver.resolve(request,
					this.google.getRegistrationId());
			oauth2 = OAuth2AuthorizationRequest.from(oauth2).scopes(Set.of(SCOPE)).build();
			this.authorizationRequestRepository.saveAuthorizationRequest(oauth2, request, response);
			response.sendRedirect(oauth2.getAuthorizationRequestUri());
		}

	}

}
