/*
 * Copyright 2020 the original author or authors.
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

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtBearerTokenAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.introspection.NimbusOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

/**
 * OAuth Resource Security configuration.
 *
 * @author Josh Cummings
 */
@EnableWebSecurity
public class OAuth2ResourceServerSecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Value("${tenantOne.jwk-set-uri}")
	String jwkSetUri;

	@Value("${tenantTwo.introspection-uri}")
	String introspectionUri;

	@Value("${tenantTwo.introspection-client-id}")
	String introspectionClientId;

	@Value("${tenantTwo.introspection-client-secret}")
	String introspectionClientSecret;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// @formatter:off
		http
			.authorizeRequests((requests) -> requests
					.mvcMatchers("/**/message/**").hasAuthority("SCOPE_message:read")
					.anyRequest().authenticated()
			)
			.oauth2ResourceServer((resourceServer) -> resourceServer
					.authenticationManagerResolver(multitenantAuthenticationManager())
			);
		// @formatter:on
	}

	@Bean
	AuthenticationManagerResolver<HttpServletRequest> multitenantAuthenticationManager() {
		Map<String, AuthenticationManager> authenticationManagers = new HashMap<>();
		authenticationManagers.put("tenantOne", jwt());
		authenticationManagers.put("tenantTwo", opaque());
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

	AuthenticationManager jwt() {
		JwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri).build();
		JwtAuthenticationProvider authenticationProvider = new JwtAuthenticationProvider(jwtDecoder);
		authenticationProvider.setJwtAuthenticationConverter(new JwtBearerTokenAuthenticationConverter());
		return authenticationProvider::authenticate;
	}

	AuthenticationManager opaque() {
		OpaqueTokenIntrospector introspectionClient = new NimbusOpaqueTokenIntrospector(this.introspectionUri,
				this.introspectionClientId, this.introspectionClientSecret);
		return new OpaqueTokenAuthenticationProvider(introspectionClient)::authenticate;
	}

}
