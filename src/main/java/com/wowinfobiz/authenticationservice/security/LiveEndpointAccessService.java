package com.wowinfobiz.authenticationservice.security;

import com.wowinfobiz.authenticationservice.enums.AccessPrincipalType;
import com.wowinfobiz.authenticationservice.model.User;
import com.wowinfobiz.authenticationservice.repo.SiteZoneAccessRepository;
import com.wowinfobiz.authenticationservice.repo.UserRepository;
import com.wowinfobiz.authenticationservice.repo.UserSensorAccessRepository;
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
        UUID parsedUserId = parseUuid(userId, "userId");
        User user = userRepository.findById(parsedUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
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

    private UUID parseUuid(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " must be a valid UUID");
        }
    }
}
