package com.farmwise.backend.security;

import com.farmwise.backend.exception.ForbiddenOperationException;
import com.farmwise.backend.exception.UnauthorizedException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<FarmWiseUserPrincipal> currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof FarmWiseUserPrincipal fw) {
            return Optional.of(fw);
        }
        return Optional.empty();
    }

    public static UUID requireCurrentUserId() {
        return currentPrincipal()
                .map(FarmWiseUserPrincipal::getUserId)
                .orElseThrow(() -> new UnauthorizedException("Authentication required."));
    }

    public static FarmWiseUserPrincipal requireCurrentPrincipal() {
        return currentPrincipal()
                .orElseThrow(() -> new UnauthorizedException("Authentication required."));
    }

    public static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    public static void assertOwnerOrAdmin(UUID ownerId, String resourceLabel) {
        UUID current = requireCurrentUserId();
        if (!current.equals(ownerId) && !isAdmin()) {
            throw new ForbiddenOperationException("You do not have permission to access this " + resourceLabel + ".");
        }
    }
}
