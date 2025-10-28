package example;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.core.GrantedAuthorities.FACTOR_OTT_AUTHORITY;
import static org.springframework.security.core.GrantedAuthorities.FACTOR_PASSWORD_AUTHORITY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("elevated-security")
class ElevatedSecurityPageConfigTests {
	@Autowired
	private MockMvc mvc;

	@Test
	void indexWhenUnauthenticatedThenRedirectsToLogin() throws Exception {
		this.mvc.perform(get("/"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("http://localhost/auth/password"));
	}

	@Test
	@WithMockUser
	void indexWhenAuthenticatedButNoFactorsThenAllows() throws Exception {
		this.mvc.perform(get("/"))
			.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(authorities = FACTOR_OTT_AUTHORITY)
	void indexWhenAuthenticatedWithOttThenAllows() throws Exception {
		this.mvc.perform(get("/"))
			.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(authorities = FACTOR_PASSWORD_AUTHORITY)
	void indexWhenAuthenticatedWithPasswordThenAllows() throws Exception {
		this.mvc.perform(get("/"))
			.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(authorities = FACTOR_PASSWORD_AUTHORITY)
	void profileWhenAuthenticatedWithPasswordThenRedirectsToOtt() throws Exception {
		this.mvc.perform(get("/profile"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("http://localhost/auth/ott?factor=ott"));
	}

	@Test
	@WithMockUser(authorities = FACTOR_OTT_AUTHORITY)
	void profileWhenAuthenticatedWithOttThenAllows() throws Exception {
		this.mvc.perform(get("/profile"))
			.andExpect(status().isOk());
	}
}
