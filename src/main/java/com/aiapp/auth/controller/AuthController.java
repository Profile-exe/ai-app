package com.aiapp.auth.controller;

import com.aiapp.auth.dto.LoginRequest;
import com.aiapp.auth.dto.RegisterRequest;
import com.aiapp.auth.dto.RegisterResposne;
import com.aiapp.auth.jwt.AuthToken;
import com.aiapp.auth.resolver.RefreshToken;
import com.aiapp.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<RegisterResposne> signUp(@RequestBody RegisterRequest registerRequest) {
        RegisterResposne response = authService.signUp(registerRequest.email(), registerRequest.password(), registerRequest.name());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthToken> login(@RequestBody LoginRequest loginRequest) {
        AuthToken response = authService.login(loginRequest.email(), loginRequest.password());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reissue")
    public ResponseEntity<AuthToken> reissue(@RefreshToken String refreshToken) {
        AuthToken response = authService.reissue(refreshToken);
        return ResponseEntity.ok(response);
    }
}
