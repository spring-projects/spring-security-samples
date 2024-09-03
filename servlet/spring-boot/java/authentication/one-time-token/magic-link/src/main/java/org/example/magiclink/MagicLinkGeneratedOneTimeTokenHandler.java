package org.example.magiclink;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.ott.GeneratedOneTimeTokenHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class MagicLinkGeneratedOneTimeTokenHandler implements GeneratedOneTimeTokenHandler {

	private final MailSender mailSender;

	private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	public MagicLinkGeneratedOneTimeTokenHandler(MailSender mailSender) {
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
