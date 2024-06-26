/*
 * Copyright 2024 the original author or authors.
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

package org.example.compromisedpasswordchecker;

import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordDecision;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
class ResetPasswordController {

	private final InMemoryUserDetailsManager userDetailsManager;

	private final PasswordEncoder passwordEncoder;

	private final CompromisedPasswordChecker passwordChecker;

	ResetPasswordController(InMemoryUserDetailsManager userDetailsManager, PasswordEncoder passwordEncoder,
			CompromisedPasswordChecker passwordChecker) {
		this.userDetailsManager = userDetailsManager;
		this.passwordEncoder = passwordEncoder;
		this.passwordChecker = passwordChecker;
	}

	@GetMapping("/reset-password")
	String resetPasswordPage() {
		return "reset-password";
	}

	@PostMapping("/reset-password")
	String resetPassword(ResetPasswordRequest resetPasswordRequest) {
		UserDetails user = this.userDetailsManager.loadUserByUsername(resetPasswordRequest.username());
		if (user == null) {
			throw new UsernameNotFoundException("User not found");
		}
		CompromisedPasswordDecision compromisedPassword = this.passwordChecker
			.check(resetPasswordRequest.newPassword());
		if (compromisedPassword.isCompromised()) {
			return "redirect:/reset-password?error=compromised_password";
		}
		boolean oldPasswordMatches = this.passwordEncoder.matches(resetPasswordRequest.currentPassword(),
				user.getPassword());
		if (!oldPasswordMatches) {
			return "redirect:/reset-password?error=invalid_password";
		}
		this.userDetailsManager.updatePassword(user, this.passwordEncoder.encode(resetPasswordRequest.newPassword()));
		return "redirect:/login";
	}

	record ResetPasswordRequest(String username, String currentPassword, String newPassword) {
	}

}
