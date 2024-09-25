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

import jakarta.servlet.http.HttpSession;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
class ResetPasswordController {

	private final InMemoryUserDetailsManager userDetailsManager;

	private final PasswordEncoder passwordEncoder;

	ResetPasswordController(InMemoryUserDetailsManager userDetailsManager, PasswordEncoder passwordEncoder) {
		this.userDetailsManager = userDetailsManager;
		this.passwordEncoder = passwordEncoder;
	}

	@PostMapping("/reset-password")
	String resetPassword(ResetPasswordRequest resetPasswordRequest, HttpSession session) {
		String newPassword = this.passwordEncoder.encode(resetPasswordRequest.newPassword());
		this.userDetailsManager.changePassword(resetPasswordRequest.currentPassword(), newPassword);
		session.removeAttribute("compromised_password");
		return "redirect:/";
	}

	record ResetPasswordRequest(String currentPassword, String newPassword) {
	}

}
