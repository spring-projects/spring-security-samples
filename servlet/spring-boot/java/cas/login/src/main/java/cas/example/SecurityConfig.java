/*
 * Copyright 2023 the original author or authors.
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

package cas.example;

import org.apereo.cas.client.session.SingleSignOutFilter;
import org.apereo.cas.client.validation.Cas30ServiceTicketValidator;
import org.apereo.cas.client.validation.TicketValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.cas.web.CasGatewayAuthenticationRedirectFilter;
import org.springframework.security.cas.web.CasGatewayResolverRequestMatcher;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
public class SecurityConfig {

	@Value("${cas.base.url}")
	private String casBaseUrl;

	@Value("${cas.login.url}")
	private String casLoginUrl;

	@Autowired
	private ServletWebServerApplicationContext context;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, UserDetailsService userDetailsService,
			MvcRequestMatcher.Builder builder) throws Exception {
		// @formatter:off
		CasGatewayAuthenticationRedirectFilter casGatewayAuthenticationRedirectFilter = new CasGatewayAuthenticationRedirectFilter(this.casLoginUrl, serviceProperties());
		casGatewayAuthenticationRedirectFilter.setRequestMatcher(new AndRequestMatcher(
				builder.pattern("/public"), new CasGatewayResolverRequestMatcher(serviceProperties())));
		http
				.authorizeHttpRequests((authorize) -> authorize
						.requestMatchers(HttpMethod.GET, "/loggedout").permitAll()
						.requestMatchers("/public").permitAll()
						.anyRequest().authenticated()
				)
				.exceptionHandling((exceptions) -> exceptions.authenticationEntryPoint(casAuthenticationEntryPoint()))
				.logout((logout) -> logout.logoutSuccessUrl("/loggedout"))
				.addFilter(casAuthenticationFilter(userDetailsService))
				.addFilterBefore(new SingleSignOutFilter(), CasAuthenticationFilter.class)
				.addFilterAfter(casGatewayAuthenticationRedirectFilter, CasAuthenticationFilter.class);
		return http.build();
		// @formatter:on
	}

	@Bean
	MvcRequestMatcher.Builder mvcRequestMatcherBuilder(HandlerMappingIntrospector introspector) {
		return new MvcRequestMatcher.Builder(introspector);
	}

	public CasAuthenticationProvider casAuthenticationProvider(UserDetailsService userDetailsService) {
		CasAuthenticationProvider provider = new CasAuthenticationProvider();
		provider.setAuthenticationUserDetailsService(new UserDetailsByNameServiceWrapper<>(userDetailsService));
		provider.setServiceProperties(serviceProperties());
		provider.setTicketValidator(cas30ServiceTicketValidator());
		provider.setKey("key");
		return provider;
	}

	private TicketValidator cas30ServiceTicketValidator() {
		return new Cas30ServiceTicketValidator(this.casBaseUrl);
	}

	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails user = User.withDefaultPasswordEncoder().username("casuser").password("Mellon").roles("USER")
				.build();
		return new InMemoryUserDetailsManager(user);
	}

	public CasAuthenticationEntryPoint casAuthenticationEntryPoint() {
		CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();
		casAuthenticationEntryPoint.setLoginUrl(this.casLoginUrl);
		casAuthenticationEntryPoint.setServiceProperties(serviceProperties());
		return casAuthenticationEntryPoint;
	}

	public CasAuthenticationFilter casAuthenticationFilter(UserDetailsService userDetailsService) {
		CasAuthenticationFilter filter = new CasAuthenticationFilter();
		CasAuthenticationProvider casAuthenticationProvider = casAuthenticationProvider(userDetailsService);
		filter.setAuthenticationManager(new ProviderManager(casAuthenticationProvider));
		return filter;
	}

	public ServiceProperties serviceProperties() {
		ServiceProperties serviceProperties = new ServiceProperties();
		serviceProperties.setService("http://localhost:" + this.context.getWebServer().getPort() + "/login/cas");
		return serviceProperties;
	}

}
