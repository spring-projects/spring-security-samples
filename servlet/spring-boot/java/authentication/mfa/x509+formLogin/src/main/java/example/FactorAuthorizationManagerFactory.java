/*
 * Copyright 2025 the original author or authors.
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

import org.jspecify.annotations.NullMarked;

import org.springframework.security.authorization.AllAuthoritiesAuthorizationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagerFactory;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.authorization.DefaultAuthorizationManagerFactory;

@NullMarked
public final class FactorAuthorizationManagerFactory implements AuthorizationManagerFactory<Object> {

	private static final AuthorizationDecision allAbstain = new AuthorizationDecision(false);

	private final AuthorizationManager<Object> factors;

	private final AuthorizationManagerFactory<Object> defaults = new DefaultAuthorizationManagerFactory<>();

	public FactorAuthorizationManagerFactory(String... authorities) {
		this.factors = AllAuthoritiesAuthorizationManager.hasAllAuthorities(authorities);
	}

	@Override
	public AuthorizationManager<Object> permitAll() {
		return this.defaults.permitAll();
	}

	@Override
	public AuthorizationManager<Object> denyAll() {
		return this.defaults.denyAll();
	}

	@Override
	public AuthorizationManager<Object> hasRole(String role) {
		return AuthorizationManagers.allOf(allAbstain, this.factors, this.defaults.hasRole(role));
	}

	@Override
	public AuthorizationManager<Object> hasAnyRole(String... roles) {
		return AuthorizationManagers.allOf(allAbstain, this.factors, this.defaults.hasAnyRole(roles));
	}

	@Override
	public AuthorizationManager<Object> hasAuthority(String authority) {
		return AuthorizationManagers.allOf(allAbstain, this.factors, this.defaults.hasAuthority(authority));
	}

	@Override
	public AuthorizationManager<Object> hasAnyAuthority(String... authorities) {
		return AuthorizationManagers.allOf(allAbstain, this.factors, this.defaults.hasAnyAuthority(authorities));
	}

	@Override
	public AuthorizationManager<Object> authenticated() {
		return AuthorizationManagers.allOf(allAbstain, this.factors, this.defaults.authenticated());
	}

	@Override
	public AuthorizationManager<Object> fullyAuthenticated() {
		return AuthorizationManagers.allOf(allAbstain, this.factors, this.defaults.fullyAuthenticated());
	}

	@Override
	public AuthorizationManager<Object> rememberMe() {
		return this.defaults.rememberMe();
	}

	@Override
	public AuthorizationManager<Object> anonymous() {
		return this.defaults.anonymous();
	}

}
