package example;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.core.authority.FactorGrantedAuthority.OTT_AUTHORITY;
import static org.springframework.security.core.authority.FactorGrantedAuthority.PASSWORD_AUTHORITY;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("default")
class DefaultConfigTests {

	@Autowired
	private MockMvc mvc;

	@Test
	void indexWhenUnauthenticatedThenRedirectsToLogin() throws Exception {
		this.mvc.perform(get("/")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
	}

	@Test
	@WithMockUser
	void indexWhenAuthenticatedButNoFactorsThenRedirectsToLogin() throws Exception {
		this.mvc.perform(get("/"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl(
					"/login?factor.type=password&factor.type=ott&factor.reason=missing&factor.reason=missing"));
	}

	@Test
	@WithMockUser(authorities = OTT_AUTHORITY)
	void indexWhenAuthenticatedWithX509ThenRedirectsToLogin() throws Exception {
		this.mvc.perform(get("/"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/login?factor.type=password&factor.reason=missing"));
	}

	@Test
	@WithMockUser(authorities = PASSWORD_AUTHORITY)
	void indexWhenAuthenticatedWithPasswordThenRedirectsToOtt() throws Exception {
		this.mvc.perform(get("/"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/login?factor.type=ott&factor.reason=missing"));
	}

	@Test
	void profileWhenAuthenticatedWithPasswordThenRedirectsToOtt() throws Exception {
		UserDetails user = User.withDefaultPasswordEncoder()
			.username("user")
			.authorities(FactorGrantedAuthority.fromAuthority(PASSWORD_AUTHORITY))
			.build();
		this.mvc.perform(get("/profile").with(user(user)))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/login?factor.type=ott&factor.reason=missing"));
	}

	@Test
	void profileWhenAuthenticatedWithOttThenRedirectsToPassword() throws Exception {
		UserDetails user = User.withDefaultPasswordEncoder()
			.username("user")
			.authorities(FactorGrantedAuthority.fromAuthority(OTT_AUTHORITY))
			.build();
		this.mvc.perform(get("/profile").with(user(user)))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/login?factor.type=password&factor.reason=missing"));
	}

	@Test
	void profileWhenExpiredPasswordAuthorityThenRedirectsToPassword() throws Exception {
		FactorGrantedAuthority expiredPassword = FactorGrantedAuthority.withAuthority(PASSWORD_AUTHORITY)
			.issuedAt(Instant.now().minusSeconds(600))
			.build();
		FactorGrantedAuthority ott = FactorGrantedAuthority.fromAuthority(OTT_AUTHORITY);
		UserDetails user = User.withDefaultPasswordEncoder().username("user").authorities(expiredPassword, ott).build();
		this.mvc.perform(get("/profile").with(user(user)))
			.andExpect(redirectedUrl("/login?factor.type=password&factor.reason=expired"));
	}

	@Test
	void profileWhenAuthenticatedWithPasswordAndOttThenAllows() throws Exception {
		FactorGrantedAuthority password = FactorGrantedAuthority.fromAuthority(PASSWORD_AUTHORITY);
		FactorGrantedAuthority ott = FactorGrantedAuthority.fromAuthority(OTT_AUTHORITY);
		UserDetails user = User.withDefaultPasswordEncoder().username("user").authorities(password, ott).build();
		this.mvc.perform(get("/profile").with(user(user))).andExpect(status().isOk());
	}

}
