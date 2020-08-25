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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * OAuth2 Security Configuration.
 *
 * @author Josh Cummings
 */
@EnableWebSecurity
public class OAuth2ResourceServerSecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Value("${spring.security.oauth2.resourceserver.opaque.introspection-uri}")
	String introspectionUri;

	@Value("${spring.security.oauth2.resourceserver.opaque.introspection-client-id}")
	String clientId;

	@Value("${spring.security.oauth2.resourceserver.opaque.introspection-client-secret}")
	String clientSecret;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// @formatter:off
		http
			.authorizeRequests((requests) -> requests
					.mvcMatchers(HttpMethod.GET, "/message/**").hasAuthority("SCOPE_message:read")
					.mvcMatchers(HttpMethod.POST, "/message/**").hasAuthority("SCOPE_message:write")
					.anyRequest().authenticated()
			)
			.oauth2ResourceServer((resourceServer) -> resourceServer
					.opaqueToken((opaqueToken) -> opaqueToken
						.introspectionUri(this.introspectionUri)
						.introspectionClientCredentials(this.clientId, this.clientSecret)
					)
			);
		// @formatter:on
	}

}
