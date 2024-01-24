/*
 * Copyright 2002-2016 the original author or authors.
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

package sample.aspectj;

import java.lang.reflect.Proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(classes = AspectjSecurityConfig.class)
class AspectJInterceptorTests {

	@Autowired
	private Service service;

	@Autowired
	private SecuredService securedService;

	@Test
	void publicMethod() {
		this.service.publicMethod();
	}

	@Test
	void securedMethodNotAuthenticated() {
		assertThatExceptionOfType(AuthenticationCredentialsNotFoundException.class)
			.isThrownBy(() -> this.service.secureMethod());
	}

	@Test
	@WithMockUser
	void securedMethodEverythingOk() {
		this.service.secureMethod();
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void securedMethodWrongRole() {
		assertThatExceptionOfType(AccessDeniedException.class).isThrownBy(() -> this.service.secureMethod());
	}

	@Test
	void securedClassNotAuthenticated() {
		assertThatExceptionOfType(AuthenticationCredentialsNotFoundException.class)
			.isThrownBy(() -> this.securedService.secureMethod());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void securedClassWrongRole() {
		assertThatExceptionOfType(AccessDeniedException.class).isThrownBy(() -> this.securedService.secureMethod());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void securedClassWrongRoleOnNewedInstance() {
		assertThatExceptionOfType(AccessDeniedException.class).isThrownBy(() -> new SecuredService().secureMethod());
	}

	@Test
	@WithMockUser
	void securedClassEverythingOk() {
		this.securedService.secureMethod();
		new SecuredService().secureMethod();
	}

	// SEC-2595
	@Test
	void notProxy() {
		assertThat(Proxy.isProxyClass(this.securedService.getClass())).isFalse();
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

}
