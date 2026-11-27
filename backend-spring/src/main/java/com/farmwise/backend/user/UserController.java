package com.farmwise.backend.user;

import com.farmwise.backend.common.MessageResponse;
import com.farmwise.backend.user.UserDtos.ChangePasswordRequest;
import com.farmwise.backend.user.UserDtos.FullUserProfileResponse;
import com.farmwise.backend.user.UserDtos.SessionItem;
import com.farmwise.backend.user.UserDtos.UpdateProfileRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users & Profiles", description = "User profile management, password updates, and session control")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me/profile")
    @Operation(summary = "Get full user profile")
    public ResponseEntity<FullUserProfileResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @PutMapping("/me/profile")
    @Operation(summary = "Update full user profile and localization settings")
    public ResponseEntity<FullUserProfileResponse> updateMyProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateMyProfile(request));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Change account password")
    public ResponseEntity<MessageResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(userService.changePassword(request, servletRequest.getRemoteAddr()));
    }

    @GetMapping("/me/sessions")
    @Operation(summary = "List active refresh-token sessions")
    public ResponseEntity<List<SessionItem>> getActiveSessions() {
        return ResponseEntity.ok(userService.getActiveSessions());
    }

    @DeleteMapping("/me/sessions/{id}")
    @Operation(summary = "Revoke a specific session")
    public ResponseEntity<MessageResponse> revokeSession(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.revokeSession(id));
    }

    @DeleteMapping("/me/sessions")
    @Operation(summary = "Revoke all active sessions")
    public ResponseEntity<MessageResponse> revokeAllSessions() {
        return ResponseEntity.ok(userService.revokeAllSessions());
    }
}
