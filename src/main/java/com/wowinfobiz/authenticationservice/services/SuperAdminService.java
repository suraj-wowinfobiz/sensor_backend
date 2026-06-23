package com.wowinfobiz.authenticationservice.services;

import com.wowinfobiz.authenticationservice.enums.AccessPrincipalType;
import com.wowinfobiz.authenticationservice.model.SiteZoneAccess;
import com.wowinfobiz.authenticationservice.model.User;

import java.util.UUID;

public interface SuperAdminService {
    User createSuperAdmin(User superAdmin);

    User createAdmin(UUID superAdminId, User admin);

    User createUserForAdmin(UUID superAdminId, UUID adminId, User user);

    SiteZoneAccess assignSiteZoneAccess(
            UUID superAdminId,
            AccessPrincipalType principalType,
            UUID principalId,
            UUID siteId,
            UUID zoneId
    );

    void revokeSiteZoneAccess(
            AccessPrincipalType principalType,
            UUID principalId,
            UUID siteId,
            UUID zoneId
    );
}
