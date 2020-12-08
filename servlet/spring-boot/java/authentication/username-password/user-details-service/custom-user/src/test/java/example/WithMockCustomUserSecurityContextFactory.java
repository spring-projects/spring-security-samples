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

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

	@Override
	public SecurityContext createSecurityContext(WithMockCustomUser mockCustomUser) {
		String username = mockCustomUser.email();
		// a stub CustomUserRepository that returns the user defined in the annotation
		CustomUserRepository userRepository = (email) -> new CustomUser(mockCustomUser.id(), username, "");
		// CustomUserRepositoryUserDetailsService ensures our UserDetails is consistent
		// with our production application
		CustomUserRepositoryUserDetailsService userDetailsService = new CustomUserRepositoryUserDetailsService(
				userRepository);
		UserDetails userDetails = userDetailsService.loadUserByUsername(username);
		SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
		securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(userDetails,
				userDetails.getPassword(), userDetails.getAuthorities()));
		return securityContext;
	}

}
