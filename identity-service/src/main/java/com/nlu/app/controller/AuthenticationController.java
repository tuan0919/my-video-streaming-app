package com.nlu.app.controller;

import java.text.ParseException;

import com.nlu.app.common.share.dto.identity_service.request.IntrospectRequest;
import com.nlu.app.dto.request.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.JOSEException;
import com.nlu.app.service.AuthenticationService;
import com.nlu.app.dto.AppResponse;
import com.nlu.app.common.share.dto.identity_service.response.AuthenticationResponse;
import com.nlu.app.common.share.dto.identity_service.response.IntrospectResponse;
import com.nlu.app.common.share.dto.identity_service.response.TokenUserResponse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;
    @PostMapping("/token")
    AppResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return AppResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/introspect")
    AppResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request) {
        var result = authenticationService.introspect(request);
        return AppResponse.<IntrospectResponse>builder().result(result).build();
    }

    @PostMapping("/refresh")
    AppResponse<AuthenticationResponse> authenticate(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        return AppResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/userinfo")
    AppResponse<TokenUserResponse> authenticate(@RequestBody TokenUserRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.getUserInToken(request);
        return AppResponse.<TokenUserResponse>builder().result(result).build();
    }

    @PostMapping("/logout")
    AppResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return AppResponse.<Void>builder().build();
    }
}
