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
import java.nio.file.AccessDeniedException;
import java.util.HashSet;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorityAuthorizationDecision;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
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
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.util.ThrowableAnalyzer;
import org.springframework.stereotype.Component;

@Configuration(proxyBeanMethods = false)
class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationEntryPoint oauth2) throws Exception {
		// @formatter:off
		http
			.authorizeHttpRequests((authz) -> authz
				.requestMatchers("/profile").hasAuthority("SCOPE_https://www.googleapis.com/auth/gmail.readonly")
				.anyRequest().authenticated()
			)
			.oauth2Login(Customizer.withDefaults())
			.exceptionHandling((exceptions) -> exceptions
				.defaultAuthenticationEntryPointFor(oauth2, "SCOPE_https://www.googleapis.com/auth/gmail.readonly")
			);
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
	static class OAuth2ScopeAuthenticationEntryPoint implements AuthenticationEntryPoint {

		private final ClientRegistration google;

		private final OAuth2AuthorizationRequestResolver authorizationRequestResolver;

		private final ThrowableAnalyzer throwableAnalyzer = new ThrowableAnalyzer();

		private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository =
			new HttpSessionOAuth2AuthorizationRequestRepository();

		private final AuthenticationEntryPoint entryPoint = new Http403ForbiddenEntryPoint();

		OAuth2ScopeAuthenticationEntryPoint(ClientRegistrationRepository clients) {
			this.google = clients.findByRegistrationId("google");
			this.authorizationRequestResolver = new DefaultOAuth2AuthorizationRequestResolver(clients);
		}

		@Override
		public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) throws IOException, ServletException {
			Throwable[] chain = this.throwableAnalyzer.determineCauseChain(ex);
			AuthorizationDeniedException denied = (AuthorizationDeniedException) this.throwableAnalyzer
				.getFirstThrowableOfType(AuthorizationDeniedException.class, chain);
			if (denied == null) {
				this.entryPoint.commence(request, response, ex);
				return;
			}
			if (!(denied.getAuthorizationResult() instanceof AuthorityAuthorizationDecision decision)) {
				this.entryPoint.commence(request, response, ex);
				return;
			}
			Set<String> needed = AuthorityUtils.authorityListToSet(decision.getAuthorities());
			Set<String> scopes = new HashSet<>();
			for (String scope : needed) {
				if (scope.startsWith("SCOPE_")) {
					scopes.add(scope.substring("SCOPE_".length()));
				}
			}
			if (scopes.isEmpty()) {
				this.entryPoint.commence(request, response, ex);
				return;
			}
			OAuth2AuthorizationRequest oauth2 = this.authorizationRequestResolver.resolve(request, this.google.getRegistrationId());
			oauth2 = OAuth2AuthorizationRequest.from(oauth2).scopes(scopes).build();
			this.authorizationRequestRepository.saveAuthorizationRequest(oauth2, request, response);
			response.sendRedirect(oauth2.getAuthorizationRequestUri());
		}
	}
}
