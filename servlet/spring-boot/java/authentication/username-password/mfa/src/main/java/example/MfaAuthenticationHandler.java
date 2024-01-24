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

package example;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * An authentication handler that saves an authentication either way.
 *
 * The reason for this is so that the rest of the factors are collected, even if earlier
 * factors failed.
 *
 * @author Josh Cummings
 */
public class MfaAuthenticationHandler implements AuthenticationSuccessHandler, AuthenticationFailureHandler {

	private final AuthenticationSuccessHandler successHandler;

	private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

	public MfaAuthenticationHandler(String url) {
		SimpleUrlAuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler(url);
		successHandler.setAlwaysUseDefaultTargetUrl(true);
		this.successHandler = successHandler;
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		Authentication anonymous = new AnonymousAuthenticationToken("key", "anonymousUser",
				AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
		saveMfaAuthentication(request, response, new MfaAuthentication(anonymous));
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		saveMfaAuthentication(request, response, authentication);
	}

	private void saveMfaAuthentication(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(new MfaAuthentication(authentication));
		this.securityContextRepository.saveContext(context, request, response);
		this.successHandler.onAuthenticationSuccess(request, response, authentication);
	}

}
