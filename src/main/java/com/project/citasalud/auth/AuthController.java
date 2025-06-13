package com.project.citasalud.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<EmailResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }


    @PostMapping("/register")
    public ResponseEntity<EmailResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader(HttpHeaders.AUTHORIZATION) final String authHeader) {
        return ResponseEntity.ok(authService.refreshToken(authHeader));
    }

    @PostMapping("/verifyCode")
    public ResponseEntity<AuthResponse> codeMailMFA(@RequestBody CodeEmailRequest codeEmailRequest) {
        return ResponseEntity.ok(authService.verifyCodeAndGenerateTokens(codeEmailRequest));
    }

}
