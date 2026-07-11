package com.chatop.api.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtConfig {

    @Bean
    JwtEncoder jwtEncoder(@Value("${app.jwt.secret}") String jwtSecret) {
        SecretKey secretKey = jwtSecretKey(jwtSecret);

        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("${app.jwt.secret}") String jwtSecret) {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey(jwtSecret))
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
    }

    private SecretKey jwtSecretKey(String jwtSecret) {
        return new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
