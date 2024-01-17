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

import io.rsocket.metadata.WellKnownMimeType;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.rsocket.server.LocalRSocketServerPort;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.security.rsocket.metadata.SimpleAuthenticationEncoder;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.MimeTypeUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for the rsocket application.
 *
 * @author Rob Winch
 * @author Eddú Meléndez
 * @since 5.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.rsocket.server.port=0")
public class HelloRSocketApplicationITests {

	@Autowired
	RSocketRequester.Builder requester;

	@LocalRSocketServerPort
	int port;

	@Test
	void messageWhenAuthenticatedThenSuccess() {
		UsernamePasswordMetadata credentials = new UsernamePasswordMetadata("user", "password");
		// @formatter:off
		RSocketRequester requester = this.requester
				.rsocketStrategies((builder) -> builder.encoder(new SimpleAuthenticationEncoder()))
				.setupMetadata(credentials, MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString()))
				.connectTcp("localhost", this.port)
				.block();
		// @formatter:on

		String message = requester.route("message").data(Mono.empty()).retrieveMono(String.class).block();

		assertThat(message).isEqualTo("Hello");
	}

	@Test
	void messageWhenNotAuthenticatedThenError() {
		RSocketRequester requester = this.requester.connectTcp("localhost", this.port).block();

		assertThatThrownBy(() -> requester.route("message").data(Mono.empty()).retrieveMono(String.class).block())
			.isNotNull();
	}

}
