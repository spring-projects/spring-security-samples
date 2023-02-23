/*
 * Copyright 2002-2022 the original author or authors.
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.Order;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(-102) // To run before FilterChainProxy
public class SamlExtensionUrlForwardingFilter extends OncePerRequestFilter {

	// @formatter:off
	private static final Map<String, String> urlMapping = Map.of("/saml/SSO", "/login/saml2/sso/one",
			"/saml/login", "/saml2/authenticate/one",
			"/saml/logout", "/logout/saml2/slo",
			"/saml/SingleLogout", "/logout/saml2/slo",
			"/saml/metadata", "/saml2/service-provider-metadata/one");
	// @formatter:on

	private final RequestMatcher matcher = createRequestMatcher();

	private RequestMatcher createRequestMatcher() {
		Set<String> urls = urlMapping.keySet();
		List<RequestMatcher> matchers = new LinkedList<>();
		urls.forEach((url) -> matchers.add(new AntPathRequestMatcher(url)));
		return new OrRequestMatcher(matchers);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		boolean match = this.matcher.matches(request);
		if (!match) {
			filterChain.doFilter(request, response);
			return;
		}
		String forwardUrl = urlMapping.get(request.getRequestURI());
		RequestDispatcher dispatcher = request.getRequestDispatcher(forwardUrl);
		dispatcher.forward(request, response);
	}

}
