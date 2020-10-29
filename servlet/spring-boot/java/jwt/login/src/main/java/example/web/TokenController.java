/*
 * Copyright 2020 the original author or authors.
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

package example.web;

import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A controller for the token resource.
 *
 * @author Josh Cummings
 */
@RestController
public class TokenController {

	@Value("${jwt.private.key}")
	RSAPrivateKey key;

	@PostMapping("/token")
	public String token(Authentication authentication) {
		Instant now = Instant.now();
		long expiry = 36000L;
		// @formatter:off
		String scope = authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(" "));
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.issuer("self")
				.issueTime(new Date(now.toEpochMilli()))
				.expirationTime(new Date(now.plusSeconds(expiry).toEpochMilli()))
				.subject(authentication.getName())
				.claim("scope", scope)
				.build();
		// @formatter:on
		JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).build();
		SignedJWT jwt = new SignedJWT(header, claims);
		return sign(jwt).serialize();
	}

	SignedJWT sign(SignedJWT jwt) {
		try {
			jwt.sign(new RSASSASigner(this.key));
			return jwt;
		}
		catch (Exception ex) {
			throw new IllegalArgumentException(ex);
		}
	}

}
