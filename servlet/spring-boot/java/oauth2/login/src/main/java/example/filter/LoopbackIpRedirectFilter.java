/*
 * Copyright 2021 the original author or authors.
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

package example.filter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * This filter ensures that the loopback IP <code>127.0.0.1</code> is used to access the
 * application so that the sample works correctly, due to the fact that redirect URIs with
 * "localhost" are rejected by the Spring Authorization Server, because the OAuth 2.1
 * draft specification states:
 *
 * <pre>
 *     While redirect URIs using localhost (i.e.,
 *     "http://localhost:{port}/{path}") function similarly to loopback IP
 *     redirects described in Section 10.3.3, the use of "localhost" is NOT
 *     RECOMMENDED.
 * </pre>
 *
 * @author Steve Riesenberg
 * @see <a href=
 * "https://tools.ietf.org/html/draft-ietf-oauth-v2-1-01#section-9.7.1">Loopback Redirect
 * Considerations in Native Apps</a>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoopbackIpRedirectFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (request.getServerName().equals("localhost") && request.getHeader("host") != null) {
			UriComponents uri = UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request))
				.host("127.0.0.1")
				.build();
			response.sendRedirect(uri.toUriString());
			return;
		}
		filterChain.doFilter(request, response);
	}

}
