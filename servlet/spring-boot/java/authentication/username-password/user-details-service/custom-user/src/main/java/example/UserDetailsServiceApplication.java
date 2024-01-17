/*
 * Copyright 2002-2024 the original author or authors.
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

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Hello Security application.
 *
 * @author Joe Grandja
 */
@SpringBootApplication
public class UserDetailsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserDetailsServiceApplication.class, args);
	}

	@Bean
	MapCustomUserRepository userRepository() {
		// the hashed password was calculated using the following code
		// the hash should be done up front, so malicious users cannot discover the
		// password
		// PasswordEncoder encoder =
		// PasswordEncoderFactories.createDelegatingPasswordEncoder();
		// String encodedPassword = encoder.encode("password");

		// the raw password is "password"
		String encodedPassword = "{bcrypt}$2a$10$h/AJueu7Xt9yh3qYuAXtk.WZJ544Uc2kdOKlHu2qQzCh/A3rq46qm";

		CustomUser customUser = new CustomUser(1L, "user@example.com", encodedPassword);
		Map<String, CustomUser> emailToCustomUser = new HashMap<>();
		emailToCustomUser.put(customUser.getEmail(), customUser);
		return new MapCustomUserRepository(emailToCustomUser);
	}

}
