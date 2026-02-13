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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Josh Cummings
 */
@SpringBootTest
class MessageServiceTests {

	@Autowired
	MessageService messages;

	// -- findMessage ---

	@Test
	@WithUnauthenticatedUser
	void findMessageWhenNotAuthenticatedThenDenied() {
		assertThatExceptionOfType(AccessDeniedException.class).isThrownBy(this.messages::findMessage);
	}

	@Test
	@WithMockUser
	void findMessageWhenUserThenSuccess() {
		assertThat(this.messages.findMessage()).isEqualTo("Hello User!");
	}

	// -- findSecretMessage ---

	@Test
	@WithUnauthenticatedUser
	void findSecretMessageWhenNotAuthenticatedThenDenied() {
		assertThatExceptionOfType(AccessDeniedException.class).isThrownBy(this.messages::findSecretMessage);
	}

	@Test
	@WithMockUser
	void findSecretMessageWhenNotAuthorizedThenDenied() {
		assertThatExceptionOfType(AccessDeniedException.class).isThrownBy(this.messages::findSecretMessage);
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void findSecretMessageWhenAuthorizedThenSuccess() {
		assertThat(this.messages.findSecretMessage()).isEqualTo("Hello Admin!");
	}

	@Target({ ElementType.METHOD, ElementType.TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@WithSecurityContext(factory = WithUnauthenticatedUserSecurityContextFactory.class)
	private @interface WithUnauthenticatedUser {

	}

	private static final class WithUnauthenticatedUserSecurityContextFactory
			implements WithSecurityContextFactory<WithUnauthenticatedUser> {

		@Override
		public SecurityContext createSecurityContext(WithUnauthenticatedUser annotation) {
			TestingAuthenticationToken token = new TestingAuthenticationToken("user", "password");
			token.setAuthenticated(false);
			return new SecurityContextImpl(token);
		}

	}

}
