package com.example.carbon;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
}

interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByCode(String code);
    Optional<Organization> findByName(String name);
    List<Organization> findAllByOrderByNameAsc();
}

interface FacilityRepository extends JpaRepository<Facility, Long> {
    Optional<Facility> findByFacilityCode(String facilityCode);
    List<Facility> findByOrganizationName(String organizationName);
}

interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByProductCode(String productCode);
}

interface BatchRepository extends JpaRepository<ProductBatch, Long> {
    Optional<ProductBatch> findByBatchId(String batchId);
}

interface StageTransferRepository extends JpaRepository<StageTransfer, Long> {
    Optional<StageTransfer> findByLegIdentifier(String legIdentifier);
    List<StageTransfer> findByBatchIdOrderByStageSequenceAsc(String batchId);
}

interface EmissionFactorRepository extends JpaRepository<EmissionFactor, Long> {
    Optional<EmissionFactor> findByFactorCode(String factorCode);
    List<EmissionFactor> findByScope(String scope);
    List<EmissionFactor> findByCategoryCode(String categoryCode);
    List<EmissionFactor> findAllByOrderByScopeAscCategoryCodeAscNameAsc();
}

interface CategoryConfigRepository extends JpaRepository<CategoryConfig, String> {
    List<CategoryConfig> findByScope(String scope);
    List<CategoryConfig> findAllByOrderByScopeAscCategoryNumberAsc();
}

interface EmissionActivityRepository extends JpaRepository<EmissionActivity, Long> {
    Optional<EmissionActivity> findByEventKey(String eventKey);
    List<EmissionActivity> findAllByOrderByCreatedAtDesc();
    List<EmissionActivity> findByOrganizationNameOrderByCreatedAtDesc(String organizationName);
    List<EmissionActivity> findByBatchIdOrderByStageSequenceAscCreatedAtAsc(String batchId);
    List<EmissionActivity> findByBatchId(String batchId);
}

interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    List<AuditEvent> findAllByOrderByCreatedAtDesc();
    List<AuditEvent> findByOrganizationNameOrderByCreatedAtDesc(String organizationName);
}
