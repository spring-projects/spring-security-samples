package example;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.core.authority.FactorGrantedAuthority.FACTOR_OTT_AUTHORITY;
import static org.springframework.security.core.authority.FactorGrantedAuthority.FACTOR_PASSWORD_AUTHORITY;
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
		this.mvc.perform(get("/"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("http://localhost/login"));
	}

	@Test
	@WithMockUser
	void indexWhenAuthenticatedButNoFactorsThenRedirectsToLogin() throws Exception {
		this.mvc.perform(get("/"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("http://localhost/login?factor=password"));
	}

	@Test
	@WithMockUser(authorities = FACTOR_OTT_AUTHORITY)
	void indexWhenAuthenticatedWithX509ThenRedirectsToLogin() throws Exception {
		this.mvc.perform(get("/"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("http://localhost/login?factor=password"));
	}

	@Test
	@WithMockUser(authorities = FACTOR_PASSWORD_AUTHORITY)
	void indexWhenAuthenticatedWithPasswordThenRedirectsToOtt() throws Exception {
		this.mvc.perform(get("/"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("http://localhost/login?factor=ott"));
	}
}
