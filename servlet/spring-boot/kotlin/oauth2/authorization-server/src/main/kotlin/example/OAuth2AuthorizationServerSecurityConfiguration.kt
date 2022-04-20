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

package example

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Role
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.core.oidc.OidcScopes
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.config.ClientSettings
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.UUID


/**
 * OAuth Authorization Server Configuration.
 *
 * @author Steve Riesenberg
 */
@Configuration
class OAuth2AuthorizationServerSecurityConfiguration {
    @Bean
    @Order(1)
    fun authorizationServerSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http)
        return http.formLogin {}.build()
    }

    @Bean
    @Order(2)
    fun standardSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        // @formatter:off
        return http
            .authorizeHttpRequests { authorize ->
                authorize.anyRequest().authenticated()
            }
            .formLogin {}
            .build()
        // @formatter:on
    }

    @Bean
    fun registeredClientRepository(): RegisteredClientRepository {
        // @formatter:off
        val loginClient = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("login-client")
            .clientSecret("{noop}openid-connect")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantTypes {
                it.add(AuthorizationGrantType.AUTHORIZATION_CODE)
                it.add(AuthorizationGrantType.REFRESH_TOKEN)
            }
            .redirectUris {
                it.add("http://127.0.0.1:8080/login/oauth2/code/login-client")
                it.add("http://127.0.0.1:8080/authorized")
            }
            .scopes {
                it.add(OidcScopes.OPENID)
                it.add(OidcScopes.PROFILE)
            }
            .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
            .build()
        val registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("messaging-client")
            .clientSecret("{noop}secret")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantTypes {
                it.add(AuthorizationGrantType.CLIENT_CREDENTIALS)
            }
            .scopes {
                it.add("message:read")
                it.add("message:write")
            }
            .build()
        // @formatter:on
        return InMemoryRegisteredClientRepository(loginClient, registeredClient)
    }

    @Bean
    fun jwkSource(keyPair: KeyPair): JWKSource<SecurityContext> {
        val publicKey = keyPair.public as RSAPublicKey
        val privateKey = keyPair.private as RSAPrivateKey
        // @formatter:off
        val rsaKey = RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(UUID.randomUUID().toString())
            .build()
        // @formatter:on
        val jwkSet = JWKSet(rsaKey)
        return ImmutableJWKSet(jwkSet)
    }

    @Bean
    fun jwtDecoder(keyPair: KeyPair): JwtDecoder {
        return NimbusJwtDecoder.withPublicKey(keyPair.public as RSAPublicKey).build()
    }

    @Bean
    fun providerSettings(): ProviderSettings {
        return ProviderSettings.builder().issuer("http://localhost:9000").build()
    }

    @Bean
    fun userDetailsService(): UserDetailsService {
        // @formatter:off
        val userDetails = User.withDefaultPasswordEncoder()
            .username("user")
            .password("password")
            .roles("USER")
            .build()
        // @formatter:on
        return InMemoryUserDetailsManager(userDetails)
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    fun generateRsaKey(): KeyPair {
        try {
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(2048)
            return keyPairGenerator.generateKeyPair()
        } catch (ex: Exception) {
            throw IllegalStateException(ex)
        }
    }
}
