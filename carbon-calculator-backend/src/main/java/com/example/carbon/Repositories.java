package com.example.carbon;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

interface AppUserRepository extends JpaRepository<AppUser,Long> {
    Optional<AppUser> findByUsername(String username);
}
interface FactorRepository extends JpaRepository<EmissionFactor,Long> {}
interface ActivityRepository extends JpaRepository<EmissionActivity,Long> {
    Optional<EmissionActivity> findByEventKey(String eventKey);
    List<EmissionActivity> findAllByOrderByCreatedAtDesc();
    List<EmissionActivity> findByOrganizationNameOrderByCreatedAtDesc(String organizationName);
    List<EmissionActivity> findByBatchIdOrderByCreatedAtAsc(String batchId);
}

interface AuditEventRepository extends JpaRepository<AuditEvent,Long> {
    List<AuditEvent> findAllByOrderByCreatedAtDesc();
    List<AuditEvent> findByOrganizationNameOrderByCreatedAtDesc(String organizationName);
}
