package com.example.carbon;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name="app_users")
class AppUser {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @Column(unique=true, nullable=false) String username;
    @Column(nullable=false) String passwordHash;
    @Column(nullable=false) String role;
    @Column(nullable=false) String organizationName;
    protected AppUser() {}
    AppUser(String username, String passwordHash, String role, String organizationName) {
        this.username=username; this.passwordHash=passwordHash; this.role=role; this.organizationName=organizationName;
    }
}

@Entity
@Table(name="emission_factors")
class EmissionFactor {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @Column(unique=true, nullable=false) String factorCode;
    @Column(nullable=false) String name;
    @Column(nullable=false) String activityUnit;
    @Column(nullable=false, precision=20, scale=8) BigDecimal factorValue;
    @Column(length=1200) String sourceNote;
    protected EmissionFactor() {}
    EmissionFactor(String factorCode, String name, String activityUnit, BigDecimal factorValue, String sourceNote) {
        this.factorCode=factorCode; this.name=name; this.activityUnit=activityUnit; this.factorValue=factorValue; this.sourceNote=sourceNote;
    }
}

@Entity
@Table(name="emission_activities")
class EmissionActivity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @Column(nullable=false) String organizationName;
    @Column(nullable=false) String batchId;
    @Column(nullable=false) String stageName;
    @Column(nullable=false) String activityName;
    @Column(nullable=false) String scope;
    @Column(nullable=false) String categoryCode;
    @Column(nullable=false, precision=20, scale=8) BigDecimal quantity;
    @Column(nullable=false) String unit;
    @ManyToOne(optional=false) EmissionFactor factor;
    @Column(nullable=false, precision=20, scale=8) BigDecimal factorSnapshot;
    @Column(nullable=false, precision=20, scale=8) BigDecimal emissionsKg;
    @Column(nullable=false, precision=12, scale=8) BigDecimal allocationNumerator;
    @Column(nullable=false, precision=12, scale=8) BigDecimal allocationDenominator;
    @Column(nullable=false) String createdBy;
    // A physical event key identifies one real-world emission event (for example one shared freight leg).
    // Unique when supplied so the same event cannot be entered twice into the product event ledger.
    @Column(unique=true, length=120) String eventKey;
    @Column(length=1200) String methodNote;
    @Column(nullable=false) Instant createdAt = Instant.now();
    protected EmissionActivity() {}
}

enum ScopeCategory {
    SCOPE_1("SCOPE_1","S1","Scope 1 — Direct emissions"),
    SCOPE_2("SCOPE_2","S2","Scope 2 — Purchased energy"),
    CAT_1("SCOPE_3","CAT_1","1. Purchased goods and services"),
    CAT_2("SCOPE_3","CAT_2","2. Capital goods"),
    CAT_3("SCOPE_3","CAT_3","3. Fuel- and energy-related activities"),
    CAT_4("SCOPE_3","CAT_4","4. Upstream transportation and distribution"),
    CAT_5("SCOPE_3","CAT_5","5. Waste generated in operations"),
    CAT_6("SCOPE_3","CAT_6","6. Business travel"),
    CAT_7("SCOPE_3","CAT_7","7. Employee commuting"),
    CAT_8("SCOPE_3","CAT_8","8. Upstream leased assets"),
    CAT_9("SCOPE_3","CAT_9","9. Downstream transportation and distribution"),
    CAT_10("SCOPE_3","CAT_10","10. Processing of sold products"),
    CAT_11("SCOPE_3","CAT_11","11. Use of sold products"),
    CAT_12("SCOPE_3","CAT_12","12. End-of-life treatment of sold products"),
    CAT_13("SCOPE_3","CAT_13","13. Downstream leased assets"),
    CAT_14("SCOPE_3","CAT_14","14. Franchises"),
    CAT_15("SCOPE_3","CAT_15","15. Investments");
    final String scope, code, label;
    ScopeCategory(String scope, String code, String label) { this.scope=scope; this.code=code; this.label=label; }
}


@Entity
@Table(name="audit_events")
class AuditEvent {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @Column(nullable=false) String username;
    @Column(nullable=false) String organizationName;
    @Column(nullable=false) String action;
    @Column(nullable=false) String recordType;
    String recordId;
    @Column(length=1200) String details;
    @Column(nullable=false) Instant createdAt = Instant.now();
    protected AuditEvent() {}
    AuditEvent(String username, String organizationName, String action, String recordType, String recordId, String details) {
        this.username=username; this.organizationName=organizationName; this.action=action;
        this.recordType=recordType; this.recordId=recordId; this.details=details;
    }
}
