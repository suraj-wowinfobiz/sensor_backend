package com.wowinfobiz.authenticationservice.services;

import com.wowinfobiz.authenticationservice.model.User;
import java.util.List;
import java.util.UUID;

public interface VendorService {
    User createVendor(User vendor);

    User getVendor(UUID vendorId);

    List<User> getAllVendors();

    User updateVendor(UUID vendorId, User vendor, Boolean active);

    void deleteVendor(UUID vendorId);
}
