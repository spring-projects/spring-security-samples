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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class MagicLinkOneTimeTokenGenerationSuccessHandler implements OneTimeTokenGenerationSuccessHandler {

	private final MailSender mailSender;

	private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	public MagicLinkOneTimeTokenGenerationSuccessHandler(MailSender mailSender) {
		this.mailSender = mailSender;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, OneTimeToken oneTimeToken)
			throws IOException, ServletException {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(UrlUtils.buildFullRequestUrl(request))
			.replacePath(request.getContextPath())
			.replaceQuery(null)
			.fragment(null)
			.path("/login/ott")
			.queryParam("token", oneTimeToken.getTokenValue());
		String magicLink = builder.toUriString();
		this.mailSender.send("johndoe@example.com", "Your Spring Security One Time Token",
				"Use the following link to sign in into the application: " + magicLink);
		this.redirectStrategy.sendRedirect(request, response, "/ott/sent");
	}

}
