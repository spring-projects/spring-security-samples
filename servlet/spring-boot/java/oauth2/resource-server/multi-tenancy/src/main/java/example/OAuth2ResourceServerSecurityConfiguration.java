/*
 * Copyright 2002-2024 the original author or authors.
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtBearerTokenAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.web.SecurityFilterChain;

/**
 * OAuth Resource Security configuration.
 *
 * @author Josh Cummings
 */
@Configuration
public class OAuth2ResourceServerSecurityConfiguration {

	@Bean
	SecurityFilterChain apiSecurity(HttpSecurity http,
			AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver) throws Exception {
		// @formatter:off
		http
			.authorizeHttpRequests((authorize) -> authorize
				.requestMatchers("/**/message/**").hasAuthority("SCOPE_message:read")
				.anyRequest().authenticated()
			)
			.oauth2ResourceServer((oauth2) -> oauth2
				.authenticationManagerResolver(authenticationManagerResolver)
			);
		// @formatter:on

		return http.build();
	}

	@Bean
	AuthenticationManagerResolver<HttpServletRequest> multitenantAuthenticationManager(JwtDecoder jwtDecoder,
			OpaqueTokenIntrospector opaqueTokenIntrospector) {
		Map<String, AuthenticationManager> authenticationManagers = new HashMap<>();
		authenticationManagers.put("tenantOne", jwt(jwtDecoder));
		authenticationManagers.put("tenantTwo", opaque(opaqueTokenIntrospector));
		return (request) -> {
			String[] pathParts = request.getRequestURI().split("/");
			String tenantId = (pathParts.length > 0) ? pathParts[1] : null;
			// @formatter:off
			return Optional.ofNullable(tenantId)
					.map(authenticationManagers::get)
					.orElseThrow(() -> new IllegalArgumentException("unknown tenant"));
			// @formatter:on
		};
	}

	AuthenticationManager jwt(JwtDecoder jwtDecoder) {
		JwtAuthenticationProvider authenticationProvider = new JwtAuthenticationProvider(jwtDecoder);
		authenticationProvider.setJwtAuthenticationConverter(new JwtBearerTokenAuthenticationConverter());
		return new ProviderManager(authenticationProvider);
	}

	AuthenticationManager opaque(OpaqueTokenIntrospector introspectionClient) {
		return new ProviderManager(new OpaqueTokenAuthenticationProvider(introspectionClient));
	}

}
