package com.spring.sample.b2c.security;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.DelegatingJwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.springframework.security.oauth2.jwt.JwtClaimNames.AUD;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${validateAudience}")
    String validateAudience;

    @Value("${tenantOne.jwkSetUri}")
    String tenantOneJwkSetUri;

    @Value("${tenantOne.issuerUri}")
    String tenantOneIssuerUri;

    @Value("${tenantTwo.jwkSetUri}")
    String tenantTwoJwkSetUri;

    @Value("${tenantTwo.issuerUri}")
    String tenantTwoIssuerUri;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        Map<String, AuthenticationManager> authenticationManagers = new HashMap<>();
        authenticationManagers.put(tenantOneIssuerUri, authenticationManager(tenantOneJwkSetUri,
                tenantOneIssuerUri));
        authenticationManagers.put(tenantTwoIssuerUri, authenticationManager(tenantTwoJwkSetUri,
                tenantTwoIssuerUri));

        AuthenticationManagerResolver<HttpServletRequest> resolver = (request) -> {
            String token = new DefaultBearerTokenResolver().resolve(request);
            String issuer;
            try {
                // Choose AuthenticationManager by accessToken issuer, you can add your own logic here
                JWT result = JWTParser.parse(token);
                JWTClaimsSet name = result.getJWTClaimsSet();
                issuer = name.getIssuer();
            } catch (ParseException e) {
                throw new IllegalArgumentException("unknown tenant");
            }
            // @formatter:off
            return Optional.ofNullable(issuer)
                    .map(authenticationManagers::get)
                    .orElseThrow(() -> new IllegalArgumentException("unknown tenant"));
            // @formatter:on
        };

        http.authorizeRequests((requests) -> requests.anyRequest().authenticated())
                .oauth2ResourceServer()
                .authenticationManagerResolver(resolver);
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        DelegatingJwtGrantedAuthoritiesConverter composite =
                new DelegatingJwtGrantedAuthoritiesConverter(roles(), groups());
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(composite);
        return jwtAuthenticationConverter;
    }

    /**
     * This converter will add authority by claim "roles"
     *
     * @return JwtGrantedAuthoritiesConverter
     */
    JwtGrantedAuthoritiesConverter roles() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthorityPrefix("ROLE_");
        authorities.setAuthoritiesClaimName("roles");
        return authorities;
    }

    /**
     * This converter will add authority by claim "scp"
     *
     * @return JwtGrantedAuthoritiesConverter
     */
    JwtGrantedAuthoritiesConverter groups() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthorityPrefix("SCOPE_");
        authorities.setAuthoritiesClaimName("scp");
        return authorities;
    }

    /**
     * Create AuthenticationManager with jwkSetUri and issuerUri, you can add other resources here.
     *
     * @param jwkSetUri jwk set uri
     * @param issuerUri issuer uri
     * @return AuthenticationManager
     */
    AuthenticationManager authenticationManager(String jwkSetUri, String issuerUri) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        // you can custom your own validator with your logic here.
        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                // Select your instance under `Applications` from portal, and then Fill in `${validate-audience}` from `Application ID`.
                new JwtClaimValidator(AUD, aud -> aud != null && ((ArrayList) aud).contains(validateAudience)),
                JwtValidators.createDefaultWithIssuer(issuerUri)));
        JwtAuthenticationProvider authenticationProvider = new JwtAuthenticationProvider(jwtDecoder);
        authenticationProvider.setJwtAuthenticationConverter(jwtAuthenticationConverter());
        return authenticationProvider::authenticate;
    }
}
