package com.wowinfobiz.authenticationservice.repo;

import com.wowinfobiz.authenticationservice.enums.AccessPrincipalType;
import com.wowinfobiz.authenticationservice.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
    long countByAdminIdAndRole(UUID adminId, AccessPrincipalType role);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndRole(String email, AccessPrincipalType role);

    Optional<User> findByIdAndRole(UUID id, AccessPrincipalType role);

    List<User> findByRole(AccessPrincipalType role);

    List<User> findByAdminIdAndRole(UUID adminId, AccessPrincipalType role);

    List<User> findByVendorIdAndRole(UUID vendorId, AccessPrincipalType role);

    Optional<User> findByIdAndAdminIdAndRole(UUID id, UUID adminId, AccessPrincipalType role);

    Optional<User> findByIdAndVendorIdAndRole(UUID id, UUID vendorId, AccessPrincipalType role);
}
