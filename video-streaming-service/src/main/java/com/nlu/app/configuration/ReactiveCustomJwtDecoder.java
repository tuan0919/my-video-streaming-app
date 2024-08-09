package com.nlu.app.configuration;

import com.nimbusds.jwt.SignedJWT;
import com.nlu.app.exception.ApplicationException;
import com.nlu.app.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.text.ParseException;

@Component
@Slf4j
public class CustomJwtDecoder implements ReactiveJwtDecoder {
    @Override
    public Mono<Jwt> decode(String token) throws JwtException {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            var issueTime = signedJWT.getJWTClaimsSet().getIssueTime().toInstant();
            var expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime().toInstant();
            var header = signedJWT.getHeader().toJSONObject();
            var claims = signedJWT.getJWTClaimsSet().getClaims();
            return Mono.just(new Jwt(token, issueTime, expirationTime, header, claims));
        } catch (ParseException e) {
            log.error("ParseException: ", e);
            throw new ApplicationException(ErrorCode.UNAUTHENTICATED);
        }
    }
}
