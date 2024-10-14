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

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@SpringBootTest
@AutoConfigureWebTestClient(timeout = "30s")
class MagicLinkApplicationTests {

	@RegisterExtension
	static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP);

	@Autowired
	WebTestClient web;

	@Test
	void ottLoginWhenUserExistsThenSendEmailAndAuthenticate() throws Exception {
		this.web.mutateWith(csrf())
			.post()
			.uri("/ott/generate")
			.body(BodyInserters.fromFormData("username", "user"))
			.exchange()
			.expectStatus()
			.isFound()
			.expectHeader()
			.location("/ott/sent");

		greenMail.waitForIncomingEmail(1);
		MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
		String content = GreenMailUtil.getBody(receivedMessage);
		String url = content.split(": ")[1];
		UriComponents uriComponents = UriComponentsBuilder.fromUriString(url).build();
		String token = uriComponents.getQueryParams().get("token").get(0);

		assertThat(token).isNotEmpty();

		this.web.mutateWith(csrf())
			.post()
			.uri("/login/ott")
			.body(BodyInserters.fromFormData("token", token))
			.exchange()
			.expectStatus()
			.isFound()
			.expectHeader()
			.location("/");
	}

	@Test
	void ottLoginWhenInvalidTokenThenFails() throws Exception {
		this.web.mutateWith(csrf())
			.post()
			.uri("/ott/generate")
			.body(BodyInserters.fromFormData("username", "user"))
			.exchange()
			.expectStatus()
			.isFound()
			.expectHeader()
			.location("/ott/sent");

		String token = "1234;";

		this.web.mutateWith(csrf())
			.post()
			.uri("/login/ott")
			.body(BodyInserters.fromFormData("token", token))
			.exchange()
			.expectStatus()
			.isFound()
			.expectHeader()
			.location("/login?error");
	}

}
