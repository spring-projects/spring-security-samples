/*
 * Copyright 2021 the original author or authors.
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

import java.security.SecureRandom;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MfaController {

	private final MfaService mfaService;

	private final BytesEncryptor encryptor;

	private final PasswordEncoder encoder;

	private final AuthenticationSuccessHandler successHandler;

	private final AuthenticationFailureHandler failureHandler;

	private final String failedAuthenticationSecret;

	private final String failedAuthenticationSecurityAnswer;

	public MfaController(MfaService mfaService, BytesEncryptor encryptor, PasswordEncoder encoder,
			AuthenticationSuccessHandler successHandler, AuthenticationFailureHandler failureHandler) {

		this.mfaService = mfaService;
		this.encryptor = encryptor;
		this.encoder = encoder;
		this.successHandler = successHandler;
		this.failureHandler = failureHandler;

		this.failedAuthenticationSecret = randomValue();
		this.failedAuthenticationSecurityAnswer = this.encoder.encode(randomValue());
	}

	@GetMapping("/second-factor")
	public String requestSecondFactor() {
		return "second-factor";
	}

	@PostMapping("/second-factor")
	public void processSecondFactor(@RequestParam("code") String code, MfaAuthentication authentication,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		MfaAuthenticationHandler handler = new MfaAuthenticationHandler("/third-factor");
		String secret = getSecret(authentication);
		if (this.mfaService.check(secret, code)) {
			handler.onAuthenticationSuccess(request, response, authentication.getFirst());
		}
		else {
			handler.onAuthenticationFailure(request, response, new BadCredentialsException("bad credentials"));
		}
	}

	@GetMapping("/third-factor")
	public String requestThirdFactor() {
		return "third-factor";
	}

	@PostMapping("/third-factor")
	public void processThirdFactor(@RequestParam("answer") String answer, MfaAuthentication authentication,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		String encodedAnswer = getAnswer(authentication);
		if (this.encoder.matches(answer, encodedAnswer)) {
			SecurityContextHolder.getContext().setAuthentication(authentication.getFirst());
			this.successHandler.onAuthenticationSuccess(request, response, authentication.getFirst());
		}
		else {
			this.failureHandler.onAuthenticationFailure(request, response,
					new BadCredentialsException("bad credentials"));
		}
	}

	private String getSecret(MfaAuthentication authentication) throws Exception {
		if (authentication.getPrincipal() instanceof CustomUser) {
			CustomUser user = (CustomUser) authentication.getPrincipal();
			byte[] bytes = Hex.decode(user.getSecret());
			return new String(this.encryptor.decrypt(bytes));
		}
		// earlier factor failed
		return this.failedAuthenticationSecret;
	}

	private String getAnswer(MfaAuthentication authentication) {
		if (authentication.getPrincipal() instanceof CustomUser) {
			CustomUser user = (CustomUser) authentication.getPrincipal();
			return user.getAnswer();
		}
		// earlier factor failed
		return this.failedAuthenticationSecurityAnswer;
	}

	private static String randomValue() {
		SecureRandom random = new SecureRandom();
		byte[] bytes = new byte[20];
		random.nextBytes(bytes);
		return new String(Hex.encode(bytes));
	}

}
