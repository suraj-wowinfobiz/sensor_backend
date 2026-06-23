package com.wowinfobiz.authenticationservice.serviceImp;

import com.wowinfobiz.authenticationservice.enums.AccessPrincipalType;
import com.wowinfobiz.authenticationservice.model.SiteZoneAccess;
import com.wowinfobiz.authenticationservice.model.User;
import com.wowinfobiz.authenticationservice.repo.SiteZoneAccessRepository;
import com.wowinfobiz.authenticationservice.repo.UserRepository;
import com.wowinfobiz.authenticationservice.services.SuperAdminService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SuperAdminServiceImpl implements SuperAdminService {

    private final UserRepository userRepository;
    private final SiteZoneAccessRepository siteZoneAccessRepository;

    public SuperAdminServiceImpl(
            UserRepository userRepository,
            SiteZoneAccessRepository siteZoneAccessRepository
    ) {
        this.userRepository = userRepository;
        this.siteZoneAccessRepository = siteZoneAccessRepository;
    }

    @Override
    public User createSuperAdmin(User superAdmin) {
        if (userRepository.findByEmail(superAdmin.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already registered: " + superAdmin.getEmail());
        }
        superAdmin.setRole(AccessPrincipalType.SUPER_ADMIN);
        return userRepository.save(superAdmin);
    }

    @Override
    public User createAdmin(UUID superAdminId, User admin) {
        userRepository.findByIdAndRole(superAdminId, AccessPrincipalType.SUPER_ADMIN)
                .orElseThrow(() -> new IllegalArgumentException("Super admin not found: " + superAdminId));
        if (admin.getMaxUsersAllowed() == null || admin.getMaxUsersAllowed() < 0) {
            throw new IllegalArgumentException("Max users allowed must be 0 or greater.");
        }
        admin.setRole(AccessPrincipalType.ADMIN);
        admin.setCreatedBySuperAdminId(superAdminId);
        return userRepository.save(admin);
    }

    @Override
    public User createUserForAdmin(UUID superAdminId, UUID adminId, User user) {
        userRepository.findByIdAndRole(superAdminId, AccessPrincipalType.SUPER_ADMIN)
                .orElseThrow(() -> new IllegalArgumentException("Super admin not found: " + superAdminId));
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
            UUID superAdminId,
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
        access.setAssignedByType(AccessPrincipalType.SUPER_ADMIN);
        access.setAssignedById(superAdminId);
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
