package com.wowinfobiz.authenticationservice.services;

import com.wowinfobiz.authenticationservice.model.User;

import java.util.List;
import java.util.UUID;

public interface VendorEngineerService {
    User createVendorEngineer(UUID vendorId, User vendorEngineer);

    User getVendorEngineer(UUID vendorId, UUID engineerId);

    List<User> getAllVendorEngineers(UUID vendorId);

    User updateVendorEngineer(UUID vendorId, UUID engineerId, User vendorEngineer, Boolean active);

    void deleteVendorEngineer(UUID vendorId, UUID engineerId);
}
