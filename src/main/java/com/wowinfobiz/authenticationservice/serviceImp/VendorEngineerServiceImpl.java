package com.wowinfobiz.authenticationservice.serviceImp;

import com.wowinfobiz.authenticationservice.enums.AccessPrincipalType;
import com.wowinfobiz.authenticationservice.model.User;
import com.wowinfobiz.authenticationservice.repo.UserRepository;
import com.wowinfobiz.authenticationservice.services.VendorEngineerService;
import java.util.List;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class VendorEngineerServiceImpl implements VendorEngineerService {

    private final UserRepository userRepository;

    public VendorEngineerServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createVendorEngineer(UUID vendorId, User vendorEngineer) {
        User vendor = userRepository.findByIdAndRole(vendorId, AccessPrincipalType.VENDOR)
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found: " + vendorId));
        vendorEngineer.setVendorId(vendorId);
        vendorEngineer.setRole(AccessPrincipalType.VENDOR_ENGINEER);
        if (vendorEngineer.getOrganizationId() == null) {
            vendorEngineer.setOrganizationId(vendor.getOrganizationId());
        }
        return userRepository.save(vendorEngineer);
    }

    @Override
    public User getVendorEngineer(UUID vendorId, UUID engineerId) {
        return userRepository.findByIdAndVendorIdAndRole(engineerId, vendorId, AccessPrincipalType.VENDOR_ENGINEER)
                .orElseThrow(() -> new IllegalArgumentException("Vendor engineer not found: " + engineerId));
    }

    @Override
    public List<User> getAllVendorEngineers(UUID vendorId) {
        userRepository.findByIdAndRole(vendorId, AccessPrincipalType.VENDOR)
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found: " + vendorId));
        return userRepository.findByVendorIdAndRole(vendorId, AccessPrincipalType.VENDOR_ENGINEER);
    }

    @Override
    public User updateVendorEngineer(UUID vendorId, UUID engineerId, User vendorEngineer, Boolean active) {
        User existing = getVendorEngineer(vendorId, engineerId);
        if (vendorEngineer.getName() != null && !vendorEngineer.getName().isBlank()) {
            existing.setName(vendorEngineer.getName());
        }
        if (vendorEngineer.getEmail() != null && !vendorEngineer.getEmail().isBlank()) {
            existing.setEmail(vendorEngineer.getEmail());
        }
        if (vendorEngineer.getPasswordHash() != null && !vendorEngineer.getPasswordHash().isBlank()) {
            existing.setPasswordHash(vendorEngineer.getPasswordHash());
        }
        if (vendorEngineer.getOrganizationId() != null) {
            existing.setOrganizationId(vendorEngineer.getOrganizationId());
        }
        if (active != null) {
            existing.setActive(active);
        }
        return userRepository.save(existing);
    }

    @Override
    public void deleteVendorEngineer(UUID vendorId, UUID engineerId) {
        User existing = getVendorEngineer(vendorId, engineerId);
        userRepository.delete(existing);
    }
}
