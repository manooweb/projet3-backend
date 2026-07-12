package com.chatop.api.auth.service;

import java.time.Instant;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.chatop.api.config.properties.ChatopProperties;
import com.chatop.api.user.model.User;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final long expirationSeconds;

    public JwtService(JwtEncoder jwtEncoder, ChatopProperties chatopProperties) {
        this.jwtEncoder = jwtEncoder;
        this.expirationSeconds = chatopProperties.getJwt().getExpirationSeconds();
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .subject(user.getEmail())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(expirationSeconds))
            .claim("userId", user.getId())
            .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
