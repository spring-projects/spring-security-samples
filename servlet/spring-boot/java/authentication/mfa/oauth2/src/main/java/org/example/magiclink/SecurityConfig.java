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

package org.example.magiclink;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationRequest;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.AuthorizationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

import static org.springframework.security.oauth2.core.authorization.OAuth2AuthorizationManagers.hasScope;

@Configuration(proxyBeanMethods = false)
class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, OAuth2ScopeAuthorizationEntryPoint oauth2) throws Exception {
		// @formatter:off
		http
			.authorizeHttpRequests((authz) -> authz
				.requestMatchers("/profile").access(hasScope("https://www.googleapis.com/auth/gmail.readonly"))
				.anyRequest().authenticated()
			)
			.oauth2Login(Customizer.withDefaults())
			.exceptionHandling((exceptions) -> exceptions.authorizationEntryPoint((a) -> a.add(oauth2)));
		// @formatter:on
		return http.build();
	}

	@Bean
	ClientRegistrationRepository clients() {
		ClientRegistration google = CommonOAuth2Provider.GOOGLE.getBuilder("google")
			.clientId(System.getenv("GOOGLE_CLIENT_ID"))
			.clientSecret(System.getenv("GOOGLE_CLIENT_SECRET"))
			.scope("openid", "profile", "email", "https://www.googleapis.com/auth/gmail.readonly")
			.build();
		return new InMemoryClientRegistrationRepository(google);
	}

	@Component
	static class OAuth2ScopeAuthorizationEntryPoint implements AuthorizationEntryPoint {

		private final ClientRegistration google;

		private final OAuth2AuthorizationRequestResolver authorizationRequestResolver;

		private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository =
			new HttpSessionOAuth2AuthorizationRequestRepository();

		public OAuth2ScopeAuthorizationEntryPoint(ClientRegistrationRepository clients) {
			this.google = clients.findByRegistrationId("google");
			this.authorizationRequestResolver = new DefaultOAuth2AuthorizationRequestResolver(clients);
		}

		@Override
		public boolean commence(HttpServletRequest request, HttpServletResponse response, AuthorizationRequest authorizationRequest) throws IOException, ServletException {
			Set<String> needed = AuthorityUtils.authorityListToSet(authorizationRequest.getAuthorities());
			Set<String> scopes = new HashSet<>();
			for (String scope : needed) {
				if (scope.startsWith("SCOPE_")) {
					scopes.add(scope.substring("SCOPE_".length()));
				}
			}
			if (scopes.isEmpty()) {
				return false;
			}
			OAuth2AuthorizationRequest oauth2 = this.authorizationRequestResolver.resolve(request, this.google.getRegistrationId());
			oauth2 = OAuth2AuthorizationRequest.from(oauth2).scopes(scopes).build();
			this.authorizationRequestRepository.saveAuthorizationRequest(oauth2, request, response);
			response.sendRedirect(oauth2.getAuthorizationRequestUri());
			return true;
		}
	}
}
