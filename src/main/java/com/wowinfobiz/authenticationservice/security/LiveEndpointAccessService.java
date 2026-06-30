package com.wowinfobiz.authenticationservice.security;

import com.wowinfobiz.authenticationservice.enums.AccessPrincipalType;
import com.wowinfobiz.authenticationservice.model.User;
import com.wowinfobiz.authenticationservice.repo.SiteZoneAccessRepository;
import com.wowinfobiz.authenticationservice.repo.UserRepository;
import com.wowinfobiz.authenticationservice.repo.UserSensorAccessRepository;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LiveEndpointAccessService {

    private final UserRepository userRepository;
    private final UserSensorAccessRepository userSensorAccessRepository;
    private final SiteZoneAccessRepository siteZoneAccessRepository;

    public LiveEndpointAccessService(UserRepository userRepository,
                                     UserSensorAccessRepository userSensorAccessRepository,
                                     SiteZoneAccessRepository siteZoneAccessRepository) {
        this.userRepository = userRepository;
        this.userSensorAccessRepository = userSensorAccessRepository;
        this.siteZoneAccessRepository = siteZoneAccessRepository;
    }

    public User requireUser(String userId) {
        User user = resolveUser(userId);
        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is inactive");
        }
        return user;
    }

    public User requireUserForSensor(String userId,
                                     String sensorId,
                                     UUID siteId,
                                     UUID zoneId) {
        User user = requireUser(userId);
        if (user.getRole() == AccessPrincipalType.SUPER_ADMIN) {
            return user;
        }

        UUID parsedSensorId = parseUuid(sensorId, "sensorId");
        switch (user.getRole()) {
            case USER -> {
                boolean allowed = userSensorAccessRepository.existsByUserIdAndSensorId(user.getId(), parsedSensorId);
                if (!allowed) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User does not have access to this sensor");
                }
            }
            case ADMIN, VENDOR_ENGINEER, VENDOR -> {
                boolean allowed = hasScopedAccess(user.getRole(), user.getId(), siteId, zoneId);
                if (!allowed) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User does not have site or zone access for this sensor");
                }
            }
            default -> throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not allowed to access this sensor");
        }
        return user;
    }

    private boolean hasScopedAccess(AccessPrincipalType principalType,
                                    UUID principalId,
                                    UUID siteId,
                                    UUID zoneId) {
        if (zoneId != null && siteZoneAccessRepository.existsByPrincipalTypeAndPrincipalIdAndZoneId(
                principalType,
                principalId,
                zoneId
        )) {
            return true;
        }
        return siteId != null && siteZoneAccessRepository.existsByPrincipalTypeAndPrincipalIdAndSiteId(
                principalType,
                principalId,
                siteId
        );
    }

    private User resolveUser(String userId) {
        final String normalized = normalizeRequired(userId, "userId");

        if (looksLikeUuid(normalized)) {
            UUID parsedUserId = UUID.fromString(normalized);
            return userRepository.findById(parsedUserId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        }

        if (normalized.matches("\\d{6}")) {
            List<User> matches = userRepository.findAll()
                    .stream()
                    .filter(user -> user.getId() != null)
                    .filter(user -> normalized.equals(toSixDigitUserId(user.getId())))
                    .toList();
            if (matches.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }
            if (matches.size() > 1) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Multiple users matched this 6-digit userId. Use the full UUID instead."
                );
            }
            return matches.get(0);
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "userId must be a valid UUID or 6-digit user id"
        );
    }

    private UUID parseUuid(String value, String fieldName) {
        String normalized = normalizeRequired(value, fieldName);
        try {
            return UUID.fromString(normalized);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " must be a valid UUID");
        }
    }

    private String normalizeRequired(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
        return value.trim();
    }

    private boolean looksLikeUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private String toSixDigitUserId(UUID userId) {
        String compact = userId.toString().replace("-", "");
        try {
            BigInteger numeric = new BigInteger(compact, 16);
            BigInteger sixDigit = numeric.mod(BigInteger.valueOf(900000L)).add(BigInteger.valueOf(100000L));
            return sixDigit.toString();
        } catch (RuntimeException ex) {
            int hash = 0;
            String normalized = userId.toString();
            for (int i = 0; i < normalized.length(); i++) {
                hash = ((hash * 31) + normalized.charAt(i)) & 0x7fffffff;
            }
            int sixDigit = (hash % 900000) + 100000;
            return Integer.toString(sixDigit);
        }
    }
}
