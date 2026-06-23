package com.wowinfobiz.authenticationservice.repo;

import com.wowinfobiz.authenticationservice.enums.AccessPrincipalType;
import com.wowinfobiz.authenticationservice.model.SiteZoneAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SiteZoneAccessRepository extends JpaRepository<SiteZoneAccess, UUID> {
    List<SiteZoneAccess> findByAssignedByTypeAndAssignedById(
            AccessPrincipalType assignedByType,
            UUID assignedById
    );

    Optional<SiteZoneAccess> findByIdAndAssignedByTypeAndAssignedById(
            UUID id,
            AccessPrincipalType assignedByType,
            UUID assignedById
    );

    void deleteByPrincipalTypeAndPrincipalIdAndSiteIdAndZoneId(
            AccessPrincipalType principalType,
            UUID principalId,
            UUID siteId,
            UUID zoneId
    );

    @Query(
            value = """
                    SELECT
                        sza.id AS accessId,
                        sza.principal_type AS principalType,
                        sza.principal_id AS principalId,
                        u.name AS principalName,
                        u.organization_id AS principalOrganizationId,
                        sza.assigned_by_type AS assignedByType,
                        sza.assigned_by_id AS assignedById,
                        sza.assigned_at AS assignedAt,
                        o.organization_id AS organizationId,
                        o.name AS organizationName,
                        access_site.sites_id AS siteId,
                        access_site.name AS siteName,
                        access_site.location AS siteLocation,
                        access_zone.zone_id AS zoneId,
                        access_zone.name AS zoneName
                    FROM site_zone_access sza
                    LEFT JOIN users u ON u.id = sza.principal_id
                    LEFT JOIN zones access_zone ON access_zone.zone_id = sza.zone_id
                    LEFT JOIN sites access_site ON access_site.sites_id = COALESCE(sza.site_id, access_zone.site_id)
                    LEFT JOIN organization o ON o.organization_id = access_site.organization_id
                    ORDER BY sza.assigned_at DESC
                    """,
            nativeQuery = true
    )
    List<AccessJoinedProjection> findAllAccessWithDetails();

    @Query(
            value = """
                    SELECT
                        sza.id AS accessId,
                        sza.principal_type AS principalType,
                        sza.principal_id AS principalId,
                        u.name AS principalName,
                        u.organization_id AS principalOrganizationId,
                        sza.assigned_by_type AS assignedByType,
                        sza.assigned_by_id AS assignedById,
                        sza.assigned_at AS assignedAt,
                        o.organization_id AS organizationId,
                        o.name AS organizationName,
                        access_site.sites_id AS siteId,
                        access_site.name AS siteName,
                        access_site.location AS siteLocation,
                        access_zone.zone_id AS zoneId,
                        access_zone.name AS zoneName
                    FROM site_zone_access sza
                    LEFT JOIN users u ON u.id = sza.principal_id
                    LEFT JOIN zones access_zone ON access_zone.zone_id = sza.zone_id
                    LEFT JOIN sites access_site ON access_site.sites_id = COALESCE(sza.site_id, access_zone.site_id)
                    LEFT JOIN organization o ON o.organization_id = access_site.organization_id
                    WHERE sza.assigned_by_type = :assignedByType
                      AND sza.assigned_by_id = :assignedById
                    ORDER BY sza.assigned_at DESC
                    """,
            nativeQuery = true
    )
    List<AccessJoinedProjection> findAllAccessWithDetailsByAssigner(
            @Param("assignedByType") String assignedByType,
            @Param("assignedById") UUID assignedById
    );

    @Query(
            value = """
                    SELECT
                        sza.id AS accessId,
                        sza.principal_type AS principalType,
                        sza.principal_id AS principalId,
                        sza.assigned_by_type AS assignedByType,
                        sza.assigned_by_id AS assignedById,
                        sza.assigned_at AS assignedAt,
                        o.organization_id AS organizationId,
                        o.name AS organizationName,
                        s.sites_id AS siteId,
                        s.name AS siteName,
                        s.location AS siteLocation,
                        z.zone_id AS zoneId,
                        z.name AS zoneName
                    FROM site_zone_access sza
                    LEFT JOIN zones access_zone ON access_zone.zone_id = sza.zone_id
                    LEFT JOIN sites access_site ON access_site.sites_id = COALESCE(sza.site_id, access_zone.site_id)
                    LEFT JOIN organization o ON o.organization_id = access_site.organization_id
                    LEFT JOIN sites s ON s.organization_id = o.organization_id
                    LEFT JOIN zones z ON z.site_id = s.sites_id
                    ORDER BY sza.assigned_at DESC, s.sites_id, z.zone_id
                    """,
            nativeQuery = true
    )
    List<AccessOrganizationHierarchyProjection> findAllAccessWithOrganizationHierarchy();

    @Query(
            value = """
                    SELECT
                        sza.id AS accessId,
                        sza.principal_type AS principalType,
                        sza.principal_id AS principalId,
                        sza.assigned_by_type AS assignedByType,
                        sza.assigned_by_id AS assignedById,
                        sza.assigned_at AS assignedAt,
                        o.organization_id AS organizationId,
                        o.name AS organizationName,
                        s.sites_id AS siteId,
                        s.name AS siteName,
                        s.location AS siteLocation,
                        z.zone_id AS zoneId,
                        z.name AS zoneName
                    FROM site_zone_access sza
                    LEFT JOIN zones access_zone ON access_zone.zone_id = sza.zone_id
                    LEFT JOIN sites access_site ON access_site.sites_id = COALESCE(sza.site_id, access_zone.site_id)
                    LEFT JOIN organization o ON o.organization_id = access_site.organization_id
                    LEFT JOIN sites s ON s.organization_id = o.organization_id
                    LEFT JOIN zones z ON z.site_id = s.sites_id
                    WHERE sza.assigned_by_type = :assignedByType
                      AND sza.assigned_by_id = :assignedById
                    ORDER BY sza.assigned_at DESC, s.sites_id, z.zone_id
                    """,
            nativeQuery = true
    )
    List<AccessOrganizationHierarchyProjection> findAllAccessWithOrganizationHierarchyByAssigner(
            @Param("assignedByType") String assignedByType,
            @Param("assignedById") UUID assignedById
    );

    @Query(
            value = """
                    SELECT
                        CAST(NULL AS UUID) AS accessId,
                        CAST(NULL AS VARCHAR) AS principalType,
                        CAST(NULL AS UUID) AS principalId,
                        CAST(NULL AS VARCHAR) AS assignedByType,
                        CAST(NULL AS UUID) AS assignedById,
                        CAST(NULL AS TIMESTAMP) AS assignedAt,
                        o.organization_id AS organizationId,
                        o.name AS organizationName,
                        s.sites_id AS siteId,
                        s.name AS siteName,
                        s.location AS siteLocation,
                        z.zone_id AS zoneId,
                        z.name AS zoneName
                    FROM organization o
                    LEFT JOIN sites s ON s.organization_id = o.organization_id
                    LEFT JOIN zones z ON z.site_id = s.sites_id
                    ORDER BY o.name, s.name, z.name
                    """,
            nativeQuery = true
    )
    List<AccessOrganizationHierarchyProjection> findFullOrganizationHierarchy();

    @Query(
            value = """
                    SELECT
                        CAST(NULL AS UUID) AS accessId,
                        CAST(NULL AS VARCHAR) AS principalType,
                        CAST(NULL AS UUID) AS principalId,
                        CAST(NULL AS VARCHAR) AS assignedByType,
                        CAST(NULL AS UUID) AS assignedById,
                        CAST(NULL AS TIMESTAMP) AS assignedAt,
                        o.organization_id AS organizationId,
                        o.name AS organizationName,
                        s.sites_id AS siteId,
                        s.name AS siteName,
                        s.location AS siteLocation,
                        z.zone_id AS zoneId,
                        z.name AS zoneName
                    FROM organization o
                    LEFT JOIN sites s ON s.organization_id = o.organization_id
                    LEFT JOIN zones z ON z.site_id = s.sites_id
                    WHERE o.organization_id = :organizationId
                    ORDER BY o.name, s.name, z.name
                    """,
            nativeQuery = true
    )
    List<AccessOrganizationHierarchyProjection> findOrganizationHierarchyByOrganizationId(
            @Param("organizationId") UUID organizationId
    );

    @Query(
            value = """
                    SELECT
                        sza.id AS accessId,
                        sza.principal_type AS principalType,
                        sza.principal_id AS principalId,
                        sza.assigned_by_type AS assignedByType,
                        sza.assigned_by_id AS assignedById,
                        sza.assigned_at AS assignedAt,
                        o.organization_id AS organizationId,
                        o.name AS organizationName,
                        s.sites_id AS siteId,
                        s.name AS siteName,
                        s.location AS siteLocation,
                        z.zone_id AS zoneId,
                        z.name AS zoneName
                    FROM site_zone_access sza
                    LEFT JOIN zones access_zone ON access_zone.zone_id = sza.zone_id
                    LEFT JOIN sites access_site ON access_site.sites_id = COALESCE(sza.site_id, access_zone.site_id)
                    LEFT JOIN organization o ON o.organization_id = access_site.organization_id
                    LEFT JOIN sites s ON s.organization_id = o.organization_id
                    LEFT JOIN zones z ON z.site_id = s.sites_id
                    WHERE sza.principal_type = :principalType
                      AND sza.principal_id = :principalId
                    ORDER BY sza.assigned_at DESC, s.sites_id, z.zone_id
                    """,
            nativeQuery = true
    )
    List<AccessOrganizationHierarchyProjection> findAllAccessWithOrganizationHierarchyByPrincipal(
            @Param("principalType") String principalType,
            @Param("principalId") UUID principalId
    );
}
