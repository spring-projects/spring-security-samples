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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor.PrincipalResolver;
import org.springframework.security.oauth2.client.web.client.RequestAttributePrincipalResolver;
import org.springframework.security.oauth2.client.web.client.SecurityContextHolderPrincipalResolver;

/**
 * Configuration for demonstrating additional strategies for resolving a {@code principal}
 * via the {@link PrincipalResolver}. This sample uses the following profiles to
 * demonstrate multiple configurations:
 *
 * <ol>
 * <li>{@code default} - Demonstrates the default setup with
 * {@link SecurityContextHolderPrincipalResolver}.</li>
 * <li>{@code per-request} - Demonstrates an alternate setup with
 * {@link RequestAttributePrincipalResolver}. Requires specifying the {@code principal}
 * via {@link RequestAttributePrincipalResolver#principal(Authentication)}.</li>
 * <li>{@code anonymous-user} - Demonstrates a custom {@link PrincipalResolver} that
 * statically resolves the {@code principal}.</li>
 * </ol>
 *
 * @author Steve Riesenberg
 */
public class PrincipalResolverConfiguration {

	@Configuration
	@Profile("per-request")
	public static class PerRequestPrincipalResolverConfiguration {

		@Bean
		public PrincipalResolver principalResolver() {
			return new RequestAttributePrincipalResolver();
		}

	}

	@Configuration
	@Profile("anonymous-user")
	public static class AnonymousUserPrincipalResolverConfiguration {

		@Bean
		public PrincipalResolver principalResolver() {
			AnonymousAuthenticationToken anonymousUser = new AnonymousAuthenticationToken("anonymous", "anonymousUser",
					AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
			return (request) -> anonymousUser;
		}

	}

}
