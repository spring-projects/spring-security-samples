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

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.encrypt.BytesEncryptor;

/**
 * Hello Security application.
 *
 * @author Josh Cummings
 */
@SpringBootApplication
public class MfaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MfaApplication.class, args);
	}

	@Bean
	MapCustomUserRepository userRepository(BytesEncryptor encryptor) {
		// the hashed password was calculated using the following code
		// the hash should be done up front, so malicious users cannot discover the
		// password
		// PasswordEncoder encoder =
		// PasswordEncoderFactories.createDelegatingPasswordEncoder();
		// String encodedPassword = encoder.encode("password");

		// the raw password is "password"
		String encodedPassword = "{bcrypt}$2a$10$h/AJueu7Xt9yh3qYuAXtk.WZJ544Uc2kdOKlHu2qQzCh/A3rq46qm";

		// to sync your phone with the Google Authenticator secret, hand enter the value
		// in base32Key
		// String base32Key = "QDWSM3OYBPGTEVSPB5FKVDM3CSNCWHVK";
		// Base32 base32 = new Base32();
		// byte[] b = base32.decode(base32Key);
		// String secret = Hex.encodeHexString(b);

		String hexSecret = "80ed266dd80bcd32564f0f4aaa8d9b149a2b1eaa";
		String encrypted = new String(Hex.encode(encryptor.encrypt(hexSecret.getBytes())));

		// the raw security answer is "smith"
		String encodedSecurityAnswer = "{bcrypt}$2a$10$JIXMjAszy3RUu8y5T0zH0enGJCGumI8YE.K7w3wsM5xXDfeVIsJhq";

		CustomUser customUser = new CustomUser(1L, "user@example.com", encodedPassword, encrypted,
				encodedSecurityAnswer);
		Map<String, CustomUser> emailToCustomUser = new HashMap<>();
		emailToCustomUser.put(customUser.getEmail(), customUser);
		return new MapCustomUserRepository(emailToCustomUser);
	}

}
