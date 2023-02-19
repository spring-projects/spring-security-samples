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
package example;

import org.apereo.cas.client.session.SingleSignOutFilter;
import org.apereo.cas.client.validation.Cas30ServiceTicketValidator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.userdetails.GrantedAuthorityFromAssertionAttributesUserDetailsService;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	static String CAS_BASE_URL = "https://casserver.herokuapp.com/cas";
	ServiceProperties serviceProperties() {
		ServiceProperties serviceProperties = new ServiceProperties();
		serviceProperties.setService("https://localhost:8443/login/cas");
		return serviceProperties;
	}

	Cas30ServiceTicketValidator casServiceTicketValidator() {
		String casUrl = CAS_BASE_URL;
		return new Cas30ServiceTicketValidator(casUrl);
	}

	@Bean
	AuthenticationUserDetailsService<CasAssertionAuthenticationToken> authenticationUserDetailsService() {
		return new GrantedAuthorityFromAssertionAttributesUserDetailsService(
				new String[] { "memberOf", "role", "group" });
	}

	CasAuthenticationProvider casAuthenticationProvider(
			AuthenticationUserDetailsService<CasAssertionAuthenticationToken> authenticationUserDetailsService) {
		CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
		casAuthenticationProvider.setAuthenticationUserDetailsService(authenticationUserDetailsService);
		casAuthenticationProvider.setTicketValidator(casServiceTicketValidator());
		casAuthenticationProvider.setKey("cas_auth_provider");
		casAuthenticationProvider.setServiceProperties(serviceProperties());
		return casAuthenticationProvider;
	}

	CasAuthenticationEntryPoint casAuthenticationEntryPoint() {
		String loginUrl = CAS_BASE_URL + "/login";
		CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();
		casAuthenticationEntryPoint.setLoginUrl(loginUrl);
		casAuthenticationEntryPoint.setServiceProperties(serviceProperties());
		return casAuthenticationEntryPoint;
	}
	CasAuthenticationFilter casAuthenticationFilter(AuthenticationUserDetailsService<CasAssertionAuthenticationToken> userDetailsService) {
		SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
		successHandler.setDefaultTargetUrl("/");
		CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
		casAuthenticationFilter.setFilterProcessesUrl("/login/cas");
		casAuthenticationFilter.setSecurityContextRepository(new DelegatingSecurityContextRepository(
				new RequestAttributeSecurityContextRepository(), new HttpSessionSecurityContextRepository()));
		casAuthenticationFilter.setAuthenticationSuccessHandler(successHandler);
		casAuthenticationFilter.setAuthenticationManager(new ProviderManager(casAuthenticationProvider(userDetailsService)));
		return casAuthenticationFilter;
	}

	/**
	 * Be sure to register SingleSignOutHttpSessionListener as session listener if this single-signout filter is used.
	 * Memory leak will result from this filter storing references to sessions globally and they won't be cleaned
	 * up without the listener removing them when they expire or are destroyed.
	 * Single sign-out requires CAS to make calls to the various services that authenticated to it
	 * and it assumes the service will be able to remove a session that is global to any application cluster
	 * that will exist. This filter stores the sessions in a static map in the JVM with a key
	 * that CAS will pass to the application so this can remove
	 */
	@Bean
	SingleSignOutFilter singleSignOutFilter() {
		return new SingleSignOutFilter();
	}

	@Bean
	SecurityFilterChain app(HttpSecurity http,
		AuthenticationUserDetailsService<CasAssertionAuthenticationToken> authenticationUserDetailsService,
		SingleSignOutFilter singleSignOutFilter) throws Exception {

		http.addFilterAfter(casAuthenticationFilter(authenticationUserDetailsService), CorsFilter.class)
			.addFilterBefore(singleSignOutFilter, CasAuthenticationFilter.class)
			.authenticationProvider(casAuthenticationProvider(authenticationUserDetailsService))
			.authorizeHttpRequests(
					(authorize) -> authorize
							.requestMatchers(HttpMethod.GET, "/loggedout").permitAll()
							.anyRequest().authenticated()
			).securityContext((context) -> context.requireExplicitSave(false))
			.logout().logoutSuccessUrl("/loggedout")
			.and()
			.exceptionHandling()
			.authenticationEntryPoint(casAuthenticationEntryPoint());

		return http.build();
	}

}
