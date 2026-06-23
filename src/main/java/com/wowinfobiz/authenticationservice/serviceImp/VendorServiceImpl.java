package com.wowinfobiz.authenticationservice.serviceImp;

import com.wowinfobiz.authenticationservice.enums.AccessPrincipalType;
import com.wowinfobiz.authenticationservice.model.User;
import com.wowinfobiz.authenticationservice.repo.UserRepository;
import com.wowinfobiz.authenticationservice.services.VendorService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class VendorServiceImpl implements VendorService {

    private final UserRepository userRepository;

    public VendorServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createVendor(User vendor) {
        vendor.setRole(AccessPrincipalType.VENDOR);
        return userRepository.save(vendor);
    }

    @Override
    public User getVendor(UUID vendorId) {
        return userRepository.findByIdAndRole(vendorId, AccessPrincipalType.VENDOR)
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found: " + vendorId));
    }

    @Override
    public List<User> getAllVendors() {
        return userRepository.findByRole(AccessPrincipalType.VENDOR);
    }

    @Override
    public User updateVendor(UUID vendorId, User vendor, Boolean active) {
        User existing = getVendor(vendorId);
        if (vendor.getName() != null && !vendor.getName().isBlank()) {
            existing.setName(vendor.getName());
        }
        if (vendor.getEmail() != null && !vendor.getEmail().isBlank()) {
            existing.setEmail(vendor.getEmail());
        }
        if (vendor.getPasswordHash() != null && !vendor.getPasswordHash().isBlank()) {
            existing.setPasswordHash(vendor.getPasswordHash());
        }
        if (vendor.getOrganizationId() != null) {
            existing.setOrganizationId(vendor.getOrganizationId());
        }
        if (active != null) {
            existing.setActive(active);
        }
        return userRepository.save(existing);
    }

    @Override
    public void deleteVendor(UUID vendorId) {
        User existing = getVendor(vendorId);
        userRepository.delete(existing);
    }
}
