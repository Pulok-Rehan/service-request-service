package com.beacepl.service_request_service.service.support;

import com.beacepl.service_request_service.entity.ServiceRequestConfigEntity;
import com.beacepl.service_request_service.enums.AudienceType;

import java.util.List;
import java.util.Locale;

/**
 * Helpers for role-based access to service request configurations.
 */
public final class ServiceRoleUtil {

    public static final String ROLE_CLIENT = "ROLE_CLIENT";
    public static final String ROLE_RM = "ROLE_RM";
    public static final String VALUE_SOURCE_REQUEST = "REQUEST";

    private ServiceRoleUtil() {
    }

    /**
     * Normalizes a role string: "rm", "RM", "ROLE_RM" -> "ROLE_RM". Blank/null -> null.
     */
    public static String normalize(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }
        String upper = role.trim().toUpperCase(Locale.ROOT);
        return upper.startsWith("ROLE_") ? upper : "ROLE_" + upper;
    }

    /**
     * Resolves the submitter role. Missing role defaults to ROLE_CLIENT (client portal is the default actor).
     */
    public static String resolveRequesterRole(String role) {
        String normalized = normalize(role);
        return normalized != null ? normalized : ROLE_CLIENT;
    }

    /**
     * Roles allowed to submit the service. Uses requesterRoles when configured, otherwise derives from targetAudience.
     */
    public static List<String> effectiveRequesterRoles(ServiceRequestConfigEntity config) {
        if (config.getRequesterRoles() != null && !config.getRequesterRoles().isEmpty()) {
            return config.getRequesterRoles().stream().map(ServiceRoleUtil::normalize).toList();
        }
        AudienceType audience = config.getTargetAudience() != null ? config.getTargetAudience() : AudienceType.BOTH;
        return switch (audience) {
            case CLIENT -> List.of(ROLE_CLIENT);
            case RM -> List.of(ROLE_RM);
            case BOTH -> List.of(ROLE_CLIENT, ROLE_RM);
        };
    }

    public static boolean isRoleAllowed(ServiceRequestConfigEntity config, String role) {
        String normalized = normalize(role);
        return normalized != null && effectiveRequesterRoles(config).contains(normalized);
    }

    public static boolean isRequestValueSource(ServiceRequestConfigEntity config) {
        return config != null && VALUE_SOURCE_REQUEST.equalsIgnoreCase(config.getValueSource());
    }

    public static boolean isOtpRequired(ServiceRequestConfigEntity config) {
        return config == null || config.getOtpRequired() == null || config.getOtpRequired();
    }
}
