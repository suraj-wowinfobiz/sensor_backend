package com.wowinfobiz.authenticationservice.services;

import com.wowinfobiz.authenticationservice.enums.AccessPrincipalType;
import com.wowinfobiz.authenticationservice.model.SiteZoneAccess;
import com.wowinfobiz.authenticationservice.model.User;

import java.util.UUID;

public interface AdminService {
    User createUser(UUID adminId, User user);

    SiteZoneAccess assignSiteZoneAccess(
            UUID adminId,
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
