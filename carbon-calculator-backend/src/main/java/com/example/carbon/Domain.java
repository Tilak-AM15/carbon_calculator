package com.example.carbon;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name="app_users")
class AppUser {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;

    @Column(unique=true, nullable=false, length=64)
    public String username;

    @Column(nullable=false, length=255)
    public String passwordHash;

    @Column(nullable=false, length=32)
    public String role; // ADMIN, STAKEHOLDER, REVIEWER

    @Column(length=64)
    public String stakeholderRole; // SUPPLIER, MANUFACTURER, LOGISTICS, DISTRIBUTOR, WHOLESALER, RETAILER, WASTE_HANDLER, AUDITOR, ADMIN

    @Column(nullable=false, length=128)
    public String organizationName;

    @Column(length=128)
    public String email;

    @Column(nullable=false)
    public Instant createdAt = Instant.now();

    public AppUser() {}
    public AppUser(String username, String passwordHash, String role, String stakeholderRole, String organizationName, String email) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.stakeholderRole = stakeholderRole;
        this.organizationName = organizationName;
        this.email = email;
    }
}

@Entity
@Table(name="organizations")
class Organization {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;

    @Column(unique=true, nullable=false, length=64)
    public String code;

    @Column(unique=true, nullable=false, length=128)
    public String name;

    @Column(nullable=false, length=64)
    public String stakeholderRole;

    @Column(length=64)
    public String country;

    @Column(length=128)
    public String contactEmail;

    @Column(length=512)
    public String description;

    public Organization() {}
    public Organization(String code, String name, String stakeholderRole, String country, String contactEmail, String description) {
        this.code = code;
        this.name = name;
        this.stakeholderRole = stakeholderRole;
        this.country = country;
        this.contactEmail = contactEmail;
        this.description = description;
    }
}

@Entity
@Table(name="facilities")
class Facility {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;

    @Column(unique=true, nullable=false, length=64)
    public String facilityCode;

    @Column(nullable=false, length=128)
    public String name;

    @Column(nullable=false, length=128)
    public String organizationName;

    @Column(length=64)
    public String city;

    @Column(length=64)
    public String country;

    @Column(length=64)
    public String gridRegion;

    public Facility() {}
    public Facility(String facilityCode, String name, String organizationName, String city, String country, String gridRegion) {
        this.facilityCode = facilityCode;
        this.name = name;
        this.organizationName = organizationName;
        this.city = city;
        this.country = country;
        this.gridRegion = gridRegion;
    }
}

@Entity
@Table(name="products")
class Product {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;

    @Column(unique=true, nullable=false, length=64)
    public String productCode;

    @Column(nullable=false, length=128)
    public String name;

    @Column(length=512)
    public String description;

    @Column(length=64)
    public String category;

    @Column(nullable=false, length=32)
    public String functionalUnit; // unit, item, kg

    @Column(precision=12, scale=4)
    public BigDecimal massPerUnitKg;

    public Product() {}
    public Product(String productCode, String name, String description, String category, String functionalUnit, BigDecimal massPerUnitKg) {
        this.productCode = productCode;
        this.name = name;
        this.description = description;
        this.category = category;
        this.functionalUnit = functionalUnit;
        this.massPerUnitKg = massPerUnitKg;
    }
}

@Entity
@Table(name="product_batches")
class ProductBatch {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;

    @Column(unique=true, nullable=false, length=64)
    public String batchId;

    @Column(nullable=false, length=64)
    public String productCode;

    @Column(nullable=false, length=128)
    public String productName;

    @Column(nullable=false, precision=16, scale=4)
    public BigDecimal quantity; // e.g. 1000 units

    @Column(nullable=false, length=32)
    public String unit;

    @Column(nullable=false, length=32)
    public String reportingPeriod; // e.g. 2026-Q1

    @Column(length=128)
    public String organizationalBoundary; // Operational Control

    @Column(length=32)
    public String status = "ACTIVE";

    @Column(length=512)
    public String notes;

    @Column(nullable=false)
    public Instant createdAt = Instant.now();

    public ProductBatch() {}
    public ProductBatch(String batchId, String productCode, String productName, BigDecimal quantity, String unit, String reportingPeriod, String organizationalBoundary, String notes) {
        this.batchId = batchId;
        this.productCode = productCode;
        this.productName = productName;
        this.quantity = quantity;
        this.unit = unit;
        this.reportingPeriod = reportingPeriod;
        this.organizationalBoundary = organizationalBoundary;
        this.notes = notes;
    }
}

@Entity
@Table(name="stage_transfers")
class StageTransfer {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;

    @Column(unique=true, nullable=false, length=64)
    public String legIdentifier;

    @Column(nullable=false, length=64)
    public String batchId;

    @Column(nullable=false)
    public Integer stageSequence;

    @Column(nullable=false, length=128)
    public String stageName;

    @Column(nullable=false, length=128)
    public String fromOrganization;

    @Column(length=64)
    public String fromFacility;

    @Column(nullable=false, length=128)
    public String toOrganization;

    @Column(length=64)
    public String toFacility;

    @Column(nullable=false, length=128)
    public String carrierName;

    @Column(nullable=false, length=64)
    public String transportMode; // ROAD_FREIGHT, RAIL_FREIGHT, AIR_CARGO, SEA_FREIGHT

    @Column(nullable=false, precision=12, scale=4)
    public BigDecimal distanceKm;

    @Column(nullable=false, precision=12, scale=4)
    public BigDecimal shippedMassTonnes;

    @Column(nullable=false, precision=16, scale=4)
    public BigDecimal tonneKm;

    @Column(nullable=false)
    public Boolean isSharedLeg = false;

    @Column(nullable=false, precision=12, scale=4)
    public BigDecimal allocationNumerator = BigDecimal.ONE;

    @Column(nullable=false, precision=12, scale=4)
    public BigDecimal allocationDenominator = BigDecimal.ONE;

    @Column(nullable=false, precision=16, scale=4)
    public BigDecimal allocatedTonneKm;

    @Column(unique=true, length=120)
    public String eventKey;

    @Column(length=512)
    public String notes;

    @Column(nullable=false)
    public Instant createdAt = Instant.now();

    public StageTransfer() {}
}

@Entity
@Table(name="emission_factors")
class EmissionFactor {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;

    @Column(unique=true, nullable=false, length=64)
    public String factorCode;

    @Column(nullable=false, length=255)
    public String name;

    @Column(nullable=false, length=32)
    public String scope; // SCOPE_1, SCOPE_2, SCOPE_3

    @Column(nullable=false, length=32)
    public String categoryCode;

    @Column(nullable=false, length=32)
    public String activityUnit; // kg, tonne, kWh, MWh, litre, m3, tonne.km, passenger.km, USD, m2.year

    @Column(nullable=false, precision=20, scale=8)
    public BigDecimal factorValue;

    @Column(length=128)
    public String sourcePublisher; // UK DEFRA/DESNZ, US EPA GHG Hub, Climatiq v37 / IEA, IPCC AR5

    @Column(length=512)
    public String sourceUrl;

    @Column(length=64)
    public String datasetVersion = "Climatiq Data Version 37";

    public Integer factorYear = 2024;

    @Column(length=64)
    public String geography = "Global";

    @Column(length=64)
    public String gasBasis = "CO2e";

    @Column(length=64)
    public String gwpBasis = "IPCC AR5 (100-year)";

    @Column(length=64)
    public String lifecycleBoundary = "cradle-to-gate";

    @Column(length=64)
    public String calculationMethod = "activity-based";

    @Column(length=32)
    public String accessDate = "2026-10-13";

    @Column(length=128)
    public String license = "Open Government Licence v3.0 / Climatiq Community Terms";

    @Column(nullable=false)
    public Boolean isMarketBasedAlternative = false;

    @Column(length=1200)
    public String sourceNote;

    public EmissionFactor() {}
    public EmissionFactor(String factorCode, String name, String scope, String categoryCode, String activityUnit,
                          BigDecimal factorValue, String sourcePublisher, String sourceUrl, String datasetVersion,
                          Integer factorYear, String geography, String gasBasis, String gwpBasis,
                          String lifecycleBoundary, String calculationMethod, String accessDate, String license,
                          Boolean isMarketBasedAlternative, String sourceNote) {
        this.factorCode = factorCode;
        this.name = name;
        this.scope = scope;
        this.categoryCode = categoryCode;
        this.activityUnit = activityUnit;
        this.factorValue = factorValue;
        this.sourcePublisher = sourcePublisher;
        this.sourceUrl = sourceUrl;
        this.datasetVersion = datasetVersion;
        this.factorYear = factorYear;
        this.geography = geography;
        this.gasBasis = gasBasis;
        this.gwpBasis = gwpBasis;
        this.lifecycleBoundary = lifecycleBoundary;
        this.calculationMethod = calculationMethod;
        this.accessDate = accessDate;
        this.license = license;
        this.isMarketBasedAlternative = isMarketBasedAlternative != null && isMarketBasedAlternative;
        this.sourceNote = sourceNote;
    }
}

@Entity
@Table(name="category_configs")
class CategoryConfig {
    @Id
    @Column(length=32)
    public String code; // S1_STATIONARY, S1_MOBILE, S1_PROCESS, S1_FUGITIVE, S2_LOCATION, S2_MARKET, CAT_1..CAT_15

    @Column(nullable=false, length=32)
    public String scope; // SCOPE_1, SCOPE_2, SCOPE_3

    public Integer categoryNumber; // 1 to 15 for Scope 3, null or 0 for Scope 1/2

    @Column(nullable=false, length=128)
    public String name;

    @Column(length=512)
    public String description;

    @Column(nullable=false, length=32)
    public String status; // APPLICABLE, NOT_APPLICABLE, DATA_UNAVAILABLE

    @Column(length=1000)
    public String statusReason;

    @Column(nullable=false, length=32)
    public String lifecycleStage; // CRADLE_TO_RETAIL, USE_AND_END_OF_LIFE, OPERATIONAL

    @Column(length=64)
    public String standardMethod; // ACTIVITY_BASED, FREIGHT_TONNE_KM, SPEND_BASED, SUPPLIER_SPECIFIC

    public CategoryConfig() {}
    public CategoryConfig(String code, String scope, Integer categoryNumber, String name, String description,
                          String status, String statusReason, String lifecycleStage, String standardMethod) {
        this.code = code;
        this.scope = scope;
        this.categoryNumber = categoryNumber;
        this.name = name;
        this.description = description;
        this.status = status;
        this.statusReason = statusReason;
        this.lifecycleStage = lifecycleStage;
        this.standardMethod = standardMethod;
    }
}

@Entity
@Table(name="emission_activities")
class EmissionActivity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;

    @Column(nullable=false, length=128)
    public String organizationName;

    @Column(length=64)
    public String facilityCode;

    @Column(nullable=false, length=64)
    public String batchId;

    public Integer stageSequence;

    @Column(nullable=false, length=128)
    public String stageName;

    @Column(nullable=false, length=128)
    public String activityName;

    @Column(nullable=false, length=32)
    public String scope; // SCOPE_1, SCOPE_2, SCOPE_3

    @Column(nullable=false, length=32)
    public String categoryCode;

    @Column(nullable=false, precision=20, scale=8)
    public BigDecimal inputQuantity;

    @Column(nullable=false, length=32)
    public String inputUnit;

    @Column(nullable=false, precision=20, scale=8)
    public BigDecimal standardQuantity; // converted to match factor activityUnit

    @Column(nullable=false, length=32)
    public String standardUnit;

    @Column(nullable=false, precision=20, scale=8)
    public BigDecimal unitConversionFactor = BigDecimal.ONE;

    @ManyToOne(optional=false)
    public EmissionFactor factor;

    @Column(nullable=false, precision=20, scale=8)
    public BigDecimal factorSnapshot;

    @Column(length=64)
    public String gasSpecificName; // e.g. R-134a

    @Column(precision=12, scale=4)
    public BigDecimal gasGwpValue; // e.g. 1300.0

    @Column(precision=12, scale=4)
    public BigDecimal shippedMassTonnes;

    @Column(precision=12, scale=4)
    public BigDecimal distanceKm;

    @Column(precision=16, scale=4)
    public BigDecimal calculatedTonneKm;

    @Column(nullable=false, precision=12, scale=8)
    public BigDecimal allocationNumerator = BigDecimal.ONE;

    @Column(nullable=false, precision=12, scale=8)
    public BigDecimal allocationDenominator = BigDecimal.ONE;

    @Column(nullable=false, precision=10, scale=4)
    public BigDecimal allocationPercent = new BigDecimal("100.0000");

    @Column(nullable=false, precision=20, scale=8)
    public BigDecimal rawEmissionsKg;

    @Column(nullable=false, precision=20, scale=8)
    public BigDecimal allocatedEmissionsKg;

    @Column(nullable=false)
    public Boolean isScope2MarketBased = false;

    @Column(nullable=false)
    public Boolean isBatchProductAllocated = true; // false if purely corporate org inventory event not in batch ledger

    @Column(nullable=false, length=32)
    public String lifecycleStage = "CRADLE_TO_RETAIL"; // CRADLE_TO_RETAIL vs USE_AND_END_OF_LIFE

    @Column(unique=true, length=120)
    public String eventKey;

    @Column(length=64)
    public String transferLegIdentifier;

    @Column(length=1200)
    public String methodNote;

    @Column(length=1200)
    public String assumptionsNote;

    @Column(nullable=false, length=64)
    public String createdBy;

    @Column(nullable=false)
    public Instant createdAt = Instant.now();

    public EmissionActivity() {}
}

@Entity
@Table(name="audit_events")
class AuditEvent {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;

    @Column(nullable=false, length=64)
    public String username;

    @Column(nullable=false, length=128)
    public String organizationName;

    @Column(nullable=false, length=32)
    public String action; // CREATE, UPDATE, DELETE, RECALCULATE, SECURITY_DENIAL, ACCESS

    @Column(nullable=false, length=64)
    public String recordType;

    @Column(length=64)
    public String recordId;

    @Column(length=1500)
    public String details;

    @Column(nullable=false)
    public Instant createdAt = Instant.now();

    public AuditEvent() {}
    public AuditEvent(String username, String organizationName, String action, String recordType, String recordId, String details) {
        this.username = username;
        this.organizationName = organizationName;
        this.action = action;
        this.recordType = recordType;
        this.recordId = recordId;
        this.details = details;
    }
}
