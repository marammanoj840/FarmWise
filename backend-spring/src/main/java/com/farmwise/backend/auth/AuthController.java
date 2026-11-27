package com.farmwise.backend.auth;

import com.farmwise.backend.auth.AuthDtos.AuthResponse;
import com.farmwise.backend.auth.AuthDtos.ForgotPasswordRequest;
import com.farmwise.backend.auth.AuthDtos.ForgotPasswordResponse;
import com.farmwise.backend.auth.AuthDtos.LoginRequest;
import com.farmwise.backend.auth.AuthDtos.LogoutRequest;
import com.farmwise.backend.auth.AuthDtos.RefreshRequest;
import com.farmwise.backend.auth.AuthDtos.RegisterRequest;
import com.farmwise.backend.auth.AuthDtos.ResetPasswordRequest;
import com.farmwise.backend.auth.AuthDtos.UserSummary;
import com.farmwise.backend.auth.AuthDtos.VerifyEmailRequest;
import com.farmwise.backend.common.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Registration, JWT authentication, refresh rotation, password reset, and verification")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new Farmer, Expert, Supplier, or Buyer account")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest servletRequest) {
        AuthResponse response = authService.register(
                request, servletRequest.getHeader("User-Agent"), servletRequest.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate with email and password")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(authService.login(
                request, servletRequest.getHeader("User-Agent"), servletRequest.getRemoteAddr()));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate refresh token and obtain fresh JWT access token")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(authService.refresh(
                request, servletRequest.getHeader("User-Agent"), servletRequest.getRemoteAddr()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke refresh token / end active session")
    public ResponseEntity<MessageResponse> logout(
            @RequestBody(required = false) LogoutRequest request,
            HttpServletRequest servletRequest) {
        String token = request == null ? null : request.refreshToken();
        return ResponseEntity.ok(authService.logout(token, servletRequest.getRemoteAddr()));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset instructions")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using issued token")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(authService.resetPassword(request, servletRequest.getRemoteAddr()));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify user email address with token")
    public ResponseEntity<MessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return ResponseEntity.ok(authService.verifyEmail(request));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification token for signed-in user")
    public ResponseEntity<ForgotPasswordResponse> resendVerification() {
        return ResponseEntity.ok(authService.resendVerification());
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user summary")
    public ResponseEntity<UserSummary> me() {
        return ResponseEntity.ok(authService.currentUser());
    }
}
