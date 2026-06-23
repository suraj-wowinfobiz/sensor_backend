package com.wowinfobiz.authenticationservice.serviceImp;

import com.wowinfobiz.authenticationservice.enums.AccessPrincipalType;
import com.wowinfobiz.authenticationservice.model.SiteZoneAccess;
import com.wowinfobiz.authenticationservice.model.User;
import com.wowinfobiz.authenticationservice.repo.SiteZoneAccessRepository;
import com.wowinfobiz.authenticationservice.repo.UserRepository;
import com.wowinfobiz.authenticationservice.services.AdminService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final SiteZoneAccessRepository siteZoneAccessRepository;

    public AdminServiceImpl(
            UserRepository userRepository,
            SiteZoneAccessRepository siteZoneAccessRepository
    ) {
        this.userRepository = userRepository;
        this.siteZoneAccessRepository = siteZoneAccessRepository;
    }

    @Override
    public User createUser(UUID adminId, User user) {
        User admin = userRepository.findByIdAndRole(adminId, AccessPrincipalType.ADMIN)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found: " + adminId));
        long currentCount = userRepository.countByAdminIdAndRole(adminId, AccessPrincipalType.USER);
        int maxUsersAllowed = admin.getMaxUsersAllowed() == null ? 0 : admin.getMaxUsersAllowed();
        if (currentCount >= maxUsersAllowed) {
            throw new IllegalStateException("Admin user limit reached for admin: " + adminId);
        }
        user.setRole(AccessPrincipalType.USER);
        user.setAdminId(adminId);
        if (user.getOrganizationId() == null) {
            user.setOrganizationId(admin.getOrganizationId());
        }
        return userRepository.save(user);
    }

    @Override
    public SiteZoneAccess assignSiteZoneAccess(
            UUID adminId,
            AccessPrincipalType principalType,
            UUID principalId,
            UUID siteId,
            UUID zoneId
    ) {
        SiteZoneAccess access = new SiteZoneAccess();
        access.setPrincipalType(principalType);
        access.setPrincipalId(principalId);
        access.setSiteId(siteId);
        access.setZoneId(zoneId);
        access.setAssignedByType(AccessPrincipalType.ADMIN);
        access.setAssignedById(adminId);
        return siteZoneAccessRepository.save(access);
    }

    @Override
    public void revokeSiteZoneAccess(
            AccessPrincipalType principalType,
            UUID principalId,
            UUID siteId,
            UUID zoneId
    ) {
        siteZoneAccessRepository.deleteByPrincipalTypeAndPrincipalIdAndSiteIdAndZoneId(
                principalType,
                principalId,
                siteId,
                zoneId
        );
    }
}
