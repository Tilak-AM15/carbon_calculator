package com.example.carbon;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Tag(name = "Carbon Calculator REST APIs", description = "Endpoints for supply chain emissions tracking, RBAC, Climatiq factor selection, unit conversions, and double-counting prevention.")
public class ActivityController {

    private final EmissionActivityRepository activities;
    private final EmissionFactorRepository factors;
    private final AppUserRepository users;
    private final OrganizationRepository organizations;
    private final FacilityRepository facilities;
    private final ProductRepository products;
    private final BatchRepository batches;
    private final StageTransferRepository transfers;
    private final CategoryConfigRepository categoryConfigs;
    private final AuditEventRepository audit;
    private final CalculationEngine calculationEngine;

    public ActivityController(
            EmissionActivityRepository activities,
            EmissionFactorRepository factors,
            AppUserRepository users,
            OrganizationRepository organizations,
            FacilityRepository facilities,
            ProductRepository products,
            BatchRepository batches,
            StageTransferRepository transfers,
            CategoryConfigRepository categoryConfigs,
            AuditEventRepository audit,
            CalculationEngine calculationEngine
    ) {
        this.activities = activities;
        this.factors = factors;
        this.users = users;
        this.organizations = organizations;
        this.facilities = facilities;
        this.products = products;
        this.batches = batches;
        this.transfers = transfers;
        this.categoryConfigs = categoryConfigs;
        this.audit = audit;
        this.calculationEngine = calculationEngine;
    }

    // --- Records and DTOs ---

    public record UserDto(String username, String role, String stakeholderRole, String organizationName, String email) {}

    public record ActivityRequest(
            @NotBlank(message = "organizationName is required") String organizationName,
            String facilityCode,
            @NotBlank(message = "batchId is required") String batchId,
            Integer stageSequence,
            @NotBlank(message = "stageName is required") String stageName,
            @NotBlank(message = "activityName is required") String activityName,
            @NotBlank(message = "scope is required") String scope,
            @NotBlank(message = "categoryCode is required") String categoryCode,
            @NotNull(message = "quantity is required") @DecimalMin(value = "0.00000001", message = "quantity must be > 0") BigDecimal quantity,
            @NotBlank(message = "unit is required") String unit,
            @NotNull(message = "factorId is required") Long factorId,
            @NotNull(message = "allocationNumerator is required") @DecimalMin(value = "0.00000001", message = "allocationNumerator must be > 0") BigDecimal allocationNumerator,
            @NotNull(message = "allocationDenominator is required") @DecimalMin(value = "0.00000001", message = "allocationDenominator must be > 0") BigDecimal allocationDenominator,
            BigDecimal shippedMassTonnes,
            BigDecimal distanceKm,
            String gasSpecificName,
            Boolean isScope2MarketBased,
            Boolean isBatchProductAllocated,
            String lifecycleStage,
            String eventKey,
            String transferLegIdentifier,
            String methodNote,
            String assumptionsNote
    ) {}

    public record ActivityDto(
            Long id,
            String organizationName,
            String facilityCode,
            String batchId,
            Integer stageSequence,
            String stageName,
            String activityName,
            String scope,
            String categoryCode,
            BigDecimal inputQuantity,
            String inputUnit,
            BigDecimal standardQuantity,
            String standardUnit,
            BigDecimal unitConversionFactor,
            Long factorId,
            String factorCode,
            String factorName,
            BigDecimal factorSnapshot,
            String gasSpecificName,
            BigDecimal gasGwpValue,
            BigDecimal shippedMassTonnes,
            BigDecimal distanceKm,
            BigDecimal calculatedTonneKm,
            BigDecimal allocationNumerator,
            BigDecimal allocationDenominator,
            BigDecimal allocationPercent,
            BigDecimal rawEmissionsKg,
            BigDecimal allocatedEmissionsKg,
            Boolean isScope2MarketBased,
            Boolean isBatchProductAllocated,
            String lifecycleStage,
            String eventKey,
            String transferLegIdentifier,
            String methodNote,
            String assumptionsNote,
            String createdBy,
            String createdAt
    ) {}

    public record StageSummaryDto(
            int stageSequence,
            String stageName,
            String organizationName,
            BigDecimal incrementalKgCo2e,
            BigDecimal cumulativeKgCo2e,
            BigDecimal percentOfTotal
    ) {}

    public record BatchSummaryDto(
            String batchId,
            String productCode,
            String productName,
            BigDecimal batchQuantity,
            String functionalUnit,
            String reportingPeriod,
            String organizationalBoundary,
            BigDecimal totalProductFootprintKgCo2e,
            BigDecimal totalProductFootprintTonnesCo2e,
            BigDecimal intensityPerUnitKgCo2e,
            BigDecimal cradleToRetailKgCo2e,
            BigDecimal cradleToRetailTonnesCo2e,
            BigDecimal useAndEndOfLifeKgCo2e,
            BigDecimal useAndEndOfLifeTonnesCo2e,
            BigDecimal scope1KgCo2e,
            BigDecimal scope2LocationBasedKgCo2e,
            BigDecimal scope2MarketBasedKgCo2e,
            String scope2AccountingGuidance,
            BigDecimal scope3KgCo2e,
            Map<String, BigDecimal> emissionsByScope,
            Map<String, BigDecimal> emissionsByCategory,
            Map<String, BigDecimal> emissionsByStakeholder,
            List<StageSummaryDto> productJourneyStages,
            List<StageTransfer> stageTransfers,
            List<ActivityDto> activities,
            boolean isCradleToRetailComplete,
            boolean hasFullLifeCycleEstimates,
            String doubleCountingPreventionNote
    ) {}

    public record CategoryStatusDto(
            String code,
            String scope,
            Integer categoryNumber,
            String name,
            String description,
            String status,
            String reason,
            String lifecycleStage,
            String standardMethod
    ) {}

    public record OrgInventoryRowDto(
            String organizationName,
            String stakeholderRole,
            BigDecimal scope1KgCo2e,
            BigDecimal scope2LocationKgCo2e,
            BigDecimal scope2MarketKgCo2e,
            BigDecimal scope3PurchasedKgCo2e,
            BigDecimal totalCorporateInventoryKgCo2e
    ) {}

    public record CorporateInventoryReportDto(
            String reportingPeriod,
            List<OrgInventoryRowDto> organizationInventories,
            BigDecimal sumOfAllCorporateInventoriesKgCo2e,
            BigDecimal reconciledBatchProductFootprintKgCo2e,
            BigDecimal avoidedDoubleCountingDeltaKgCo2e,
            String explanation
    ) {}

    // --- Endpoints ---

    @Operation(summary = "Get current authenticated user profile and permissions")
    @GetMapping("/me")
    public UserDto me(Authentication auth) {
        AppUser u = users.findByUsername(auth.getName()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found")
        );
        return new UserDto(u.username, u.role, u.stakeholderRole, u.organizationName, u.email);
    }

    @Operation(summary = "List all registered supply chain organizations")
    @GetMapping("/organizations")
    public List<Organization> listOrganizations() {
        return organizations.findAllByOrderByNameAsc();
    }

    @Operation(summary = "List facilities, optionally filtered by organization")
    @GetMapping("/facilities")
    public List<Facility> listFacilities(@RequestParam(required = false) String organizationName) {
        if (organizationName != null && !organizationName.isBlank()) {
            return facilities.findByOrganizationName(organizationName);
        }
        return facilities.findAll();
    }

    @Operation(summary = "List products in the catalog")
    @GetMapping("/products")
    public List<Product> listProducts() {
        return products.findAll();
    }

    @Operation(summary = "List all product production batches")
    @GetMapping("/batches")
    public List<ProductBatch> listBatches() {
        return batches.findAll();
    }

    @Operation(summary = "Scope & category catalogue")
    @GetMapping("/categories")
    public List<CategoryConfig> categories() {
        return categoryConfigs.findAllByOrderByScopeAscCategoryNumberAsc();
    }

    @Operation(summary = "All 15 Scope 3 categories, Scope 1 and Scope 2 with explicit status and reason")
    @GetMapping("/category-status")
    public List<CategoryStatusDto> categoryStatus() {
        return categoryConfigs.findAllByOrderByScopeAscCategoryNumberAsc().stream()
                .map(c -> new CategoryStatusDto(
                        c.code, c.scope, c.categoryNumber, c.name, c.description,
                        c.status, c.statusReason, c.lifecycleStage, c.standardMethod
                )).toList();
    }

    @Operation(summary = "Curated Climatiq-cited emission factor catalogue with full provenance metadata")
    @GetMapping("/factors")
    public List<EmissionFactor> factors() {
        return factors.findAllByOrderByScopeAscCategoryCodeAscNameAsc();
    }

    @Operation(summary = "Create an activity with backend unit conversion, Climatiq factor snapshot, and RBAC enforcement")
    @PostMapping("/activities")
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityDto createActivity(@Valid @RequestBody ActivityRequest req, Authentication auth) {
        boolean isAdmin = hasRole(auth, "ADMIN");
        boolean isReviewer = hasRole(auth, "REVIEWER");

        if (isReviewer) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Reviewers have read-only access and cannot create activity records.");
        }

        AppUser current = users.findByUsername(auth.getName()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated")
        );

        // RBAC cross-organization write denial
        if (!isAdmin && !current.organizationName.equalsIgnoreCase(req.organizationName().trim())) {
            audit.save(new AuditEvent(auth.getName(), current.organizationName, "SECURITY_DENIAL", "EMISSION_ACTIVITY", null,
                    "Attempted unauthorized cross-organization write for: " + req.organizationName()));
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied: Stakeholders can only record activities for their own organization ('" + current.organizationName + "').");
        }

        // Duplicate event key check (guard against double counting)
        if (req.eventKey() != null && !req.eventKey().isBlank()) {
            String trimmedKey = req.eventKey().trim();
            if (activities.findByEventKey(trimmedKey).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Physical emission event key '" + trimmedKey + "' already exists. Reuse existing record to prevent duplicate counting.");
            }
        }

        // Look up factor
        EmissionFactor factor = factors.findById(req.factorId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Emission factor with ID " + req.factorId() + " not found")
        );

        // Execute calculation engine
        CalculationEngine.CalculationResult calc = calculationEngine.calculate(
                req.quantity(),
                req.unit(),
                factor,
                req.allocationNumerator(),
                req.allocationDenominator(),
                req.shippedMassTonnes(),
                req.distanceKm(),
                req.gasSpecificName()
        );

        boolean isMarketBased = (req.isScope2MarketBased() != null && req.isScope2MarketBased()) || factor.isMarketBasedAlternative;
        boolean isBatchAllocated = req.isBatchProductAllocated() == null || req.isBatchProductAllocated();
        String lifecycle = (req.lifecycleStage() != null && !req.lifecycleStage().isBlank()) ? req.lifecycleStage() : "CRADLE_TO_RETAIL";

        EmissionActivity a = new EmissionActivity();
        a.organizationName = req.organizationName().trim();
        a.facilityCode = req.facilityCode();
        a.batchId = req.batchId().trim();
        a.stageSequence = req.stageSequence() != null ? req.stageSequence() : 1;
        a.stageName = req.stageName().trim();
        a.activityName = req.activityName().trim();
        a.scope = req.scope().trim();
        a.categoryCode = req.categoryCode().trim();

        a.inputQuantity = req.quantity();
        a.inputUnit = req.unit().trim();
        a.standardQuantity = calc.standardQuantity();
        a.standardUnit = calc.standardUnit();
        a.unitConversionFactor = calc.conversionRatio();

        a.factor = factor;
        a.factorSnapshot = factor.factorValue;
        a.gasSpecificName = req.gasSpecificName();
        a.gasGwpValue = calc.gwpValue();

        a.shippedMassTonnes = req.shippedMassTonnes();
        a.distanceKm = req.distanceKm();
        a.calculatedTonneKm = calc.tonneKm();

        a.allocationNumerator = req.allocationNumerator();
        a.allocationDenominator = req.allocationDenominator();
        a.allocationPercent = calc.allocationPercent();

        a.rawEmissionsKg = calc.rawEmissionsKg();
        a.allocatedEmissionsKg = calc.allocatedEmissionsKg();

        a.isScope2MarketBased = isMarketBased;
        a.isBatchProductAllocated = isBatchAllocated;
        a.lifecycleStage = lifecycle;

        a.eventKey = (req.eventKey() == null || req.eventKey().isBlank()) ? null : req.eventKey().trim();
        a.transferLegIdentifier = req.transferLegIdentifier();
        a.methodNote = req.methodNote();
        a.assumptionsNote = req.assumptionsNote();
        a.createdBy = auth.getName();

        EmissionActivity saved = activities.save(a);

        // Write audit entry
        audit.save(new AuditEvent(auth.getName(), current.organizationName, "CREATE", "EMISSION_ACTIVITY",
                String.valueOf(saved.id),
                "Batch=" + saved.batchId + ", Stage=" + saved.stageName + ", Factor=" + factor.factorCode +
                        ", AllocatedKg=" + saved.allocatedEmissionsKg));

        return toDto(saved);
    }

    @Operation(summary = "List activities (Admin/Reviewer see all; Stakeholders see own organization records)")
    @GetMapping("/activities")
    public List<ActivityDto> listActivities(Authentication auth) {
        if (hasRole(auth, "ADMIN") || hasRole(auth, "REVIEWER")) {
            return activities.findAllByOrderByCreatedAtDesc().stream().map(this::toDto).toList();
        }
        AppUser user = users.findByUsername(auth.getName()).orElseThrow();
        return activities.findByOrganizationNameOrderByCreatedAtDesc(user.organizationName).stream().map(this::toDto).toList();
    }

    @Operation(summary = "List supply chain transport legs")
    @GetMapping("/transfers")
    public List<StageTransfer> listTransfers(@RequestParam(required = false) String batchId) {
        if (batchId != null && !batchId.isBlank()) {
            return transfers.findByBatchIdOrderByStageSequenceAsc(batchId);
        }
        return transfers.findAll();
    }

    @Operation(summary = "Comprehensive product batch footprint and journey summary with double-counting prevention")
    @GetMapping("/batches/{batchId}/summary")
    public BatchSummaryDto batchSummary(@PathVariable String batchId, Authentication auth) {
        ProductBatch batch = batches.findByBatchId(batchId).orElseGet(() ->
                new ProductBatch(batchId, "PROD-GENERIC", "Batch " + batchId,
                        new BigDecimal("1000.0"), "unit", "2026-Q1", "Operational Control", "Summary view")
        );

        List<EmissionActivity> allBatchActivities = activities.findByBatchIdOrderByStageSequenceAscCreatedAtAsc(batchId);
        List<StageTransfer> batchTransfers = transfers.findByBatchIdOrderByStageSequenceAsc(batchId);

        // Filter activities contributing to the Reconciled Product Total:
        // 1. Must be allocated to the batch (isBatchProductAllocated == true)
        // 2. Scope 2 Market-Based entries are NOT added to location-based to prevent duplicate counting
        List<EmissionActivity> productLedgerActivities = allBatchActivities.stream()
                .filter(a -> Boolean.TRUE.equals(a.isBatchProductAllocated))
                .filter(a -> !Boolean.TRUE.equals(a.isScope2MarketBased))
                .toList();

        // Scopes breakdown
        BigDecimal scope1Kg = productLedgerActivities.stream()
                .filter(a -> "SCOPE_1".equalsIgnoreCase(a.scope))
                .map(a -> a.allocatedEmissionsKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);

        BigDecimal scope2LocationKg = productLedgerActivities.stream()
                .filter(a -> "SCOPE_2".equalsIgnoreCase(a.scope))
                .map(a -> a.allocatedEmissionsKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);

        BigDecimal scope3Kg = productLedgerActivities.stream()
                .filter(a -> "SCOPE_3".equalsIgnoreCase(a.scope))
                .map(a -> a.allocatedEmissionsKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);

        BigDecimal totalProductFootprintKg = scope1Kg.add(scope2LocationKg).add(scope3Kg)
                .setScale(4, RoundingMode.HALF_UP);

        BigDecimal totalProductFootprintTonnes = totalProductFootprintKg
                .divide(new BigDecimal("1000"), 6, RoundingMode.HALF_UP);

        BigDecimal intensityPerUnit = (batch.quantity != null && batch.quantity.compareTo(BigDecimal.ZERO) > 0)
                ? totalProductFootprintKg.divide(batch.quantity, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Cradle to retail vs Use and End-of-Life
        BigDecimal cradleToRetailKg = productLedgerActivities.stream()
                .filter(a -> "CRADLE_TO_RETAIL".equalsIgnoreCase(a.lifecycleStage))
                .map(a -> a.allocatedEmissionsKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);

        BigDecimal useAndEndOfLifeKg = totalProductFootprintKg.subtract(cradleToRetailKg)
                .setScale(4, RoundingMode.HALF_UP);

        // Scope 2 Market-Based alternative (calculated separately, NEVER added to location total!)
        BigDecimal scope2MarketKg = allBatchActivities.stream()
                .filter(a -> "SCOPE_2".equalsIgnoreCase(a.scope) && Boolean.TRUE.equals(a.isScope2MarketBased))
                .map(a -> a.allocatedEmissionsKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);

        Map<String, BigDecimal> emissionsByScope = new LinkedHashMap<>();
        emissionsByScope.put("SCOPE_1", scope1Kg);
        emissionsByScope.put("SCOPE_2_LOCATION", scope2LocationKg);
        emissionsByScope.put("SCOPE_3", scope3Kg);

        Map<String, BigDecimal> emissionsByCategory = productLedgerActivities.stream()
                .collect(Collectors.groupingBy(a -> a.categoryCode, TreeMap::new,
                        Collectors.reducing(BigDecimal.ZERO, a -> a.allocatedEmissionsKg, BigDecimal::add)));

        Map<String, BigDecimal> emissionsByStakeholder = productLedgerActivities.stream()
                .collect(Collectors.groupingBy(a -> a.organizationName, TreeMap::new,
                        Collectors.reducing(BigDecimal.ZERO, a -> a.allocatedEmissionsKg, BigDecimal::add)));

        // Stage Journey in sequence
        Map<String, List<EmissionActivity>> stageGroups = productLedgerActivities.stream()
                .collect(Collectors.groupingBy(a -> (a.stageSequence != null ? a.stageSequence : 1) + "::" + a.stageName + "::" + a.organizationName,
                        LinkedHashMap::new, Collectors.toList()));

        List<StageSummaryDto> journeyStages = new ArrayList<>();
        BigDecimal runningTotal = BigDecimal.ZERO;

        for (Map.Entry<String, List<EmissionActivity>> entry : stageGroups.entrySet()) {
            String[] parts = entry.getKey().split("::");
            int seq = Integer.parseInt(parts[0]);
            String name = parts[1];
            String org = parts[2];

            BigDecimal stageIncremental = entry.getValue().stream()
                    .map(a -> a.allocatedEmissionsKg)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(4, RoundingMode.HALF_UP);

            runningTotal = runningTotal.add(stageIncremental);

            BigDecimal pct = (totalProductFootprintKg.compareTo(BigDecimal.ZERO) > 0)
                    ? stageIncremental.multiply(new BigDecimal("100")).divide(totalProductFootprintKg, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            journeyStages.add(new StageSummaryDto(seq, name, org, stageIncremental, runningTotal, pct));
        }

        journeyStages.sort(Comparator.comparingInt(StageSummaryDto::stageSequence));

        // Permitted activities view for caller: Admin & Reviewer see all, Stakeholder sees permitted records
        List<EmissionActivity> userVisibleActivities = allBatchActivities;
        if (auth != null && !hasRole(auth, "ADMIN") && !hasRole(auth, "REVIEWER")) {
            AppUser u = users.findByUsername(auth.getName()).orElseThrow();
            userVisibleActivities = allBatchActivities.stream()
                    .filter(a -> a.organizationName.equalsIgnoreCase(u.organizationName))
                    .toList();
        }

        return new BatchSummaryDto(
                batchId,
                batch.productCode,
                batch.productName,
                batch.quantity,
                batch.unit,
                batch.reportingPeriod,
                batch.organizationalBoundary,
                totalProductFootprintKg,
                totalProductFootprintTonnes,
                intensityPerUnit,
                cradleToRetailKg,
                cradleToRetailKg.divide(new BigDecimal("1000"), 6, RoundingMode.HALF_UP),
                useAndEndOfLifeKg,
                useAndEndOfLifeKg.divide(new BigDecimal("1000"), 6, RoundingMode.HALF_UP),
                scope1Kg,
                scope2LocationKg,
                scope2MarketKg,
                "Scope 2 location-based (" + scope2LocationKg + " kg) and market-based (" + scope2MarketKg +
                        " kg) represent dual accounting alternatives under the GHG Protocol. They must NEVER be added together.",
                scope3Kg,
                emissionsByScope,
                emissionsByCategory,
                emissionsByStakeholder,
                journeyStages,
                batchTransfers,
                userVisibleActivities.stream().map(this::toDto).toList(),
                cradleToRetailKg.compareTo(BigDecimal.ZERO) > 0,
                useAndEndOfLifeKg.compareTo(BigDecimal.ZERO) > 0,
                "Double Counting Prevention: Product footprint totals unique physical emission events allocated to the batch once. Upstream supplier Scope 1 and Scope 2 emissions are not duplicate-counted alongside buyer Scope 3 Category 1 events."
        );
    }

    @Operation(summary = "Corporate Greenhouse Gas Inventory breakdown comparing organization boundaries with reconciled product batch footprint")
    @GetMapping("/reports/organization-inventory")
    public CorporateInventoryReportDto corporateInventoryReport() {
        List<EmissionActivity> all = activities.findAll();
        Map<String, List<EmissionActivity>> orgMap = all.stream()
                .collect(Collectors.groupingBy(a -> a.organizationName, TreeMap::new, Collectors.toList()));

        List<OrgInventoryRowDto> rows = new ArrayList<>();
        BigDecimal totalCorporateSum = BigDecimal.ZERO;

        for (Map.Entry<String, List<EmissionActivity>> entry : orgMap.entrySet()) {
            String orgName = entry.getKey();
            List<EmissionActivity> list = entry.getValue();

            BigDecimal s1 = list.stream().filter(a -> "SCOPE_1".equalsIgnoreCase(a.scope))
                    .map(a -> a.rawEmissionsKg).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal s2Loc = list.stream().filter(a -> "SCOPE_2".equalsIgnoreCase(a.scope) && !Boolean.TRUE.equals(a.isScope2MarketBased))
                    .map(a -> a.rawEmissionsKg).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal s2Mkt = list.stream().filter(a -> "SCOPE_2".equalsIgnoreCase(a.scope) && Boolean.TRUE.equals(a.isScope2MarketBased))
                    .map(a -> a.rawEmissionsKg).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal s3 = list.stream().filter(a -> "SCOPE_3".equalsIgnoreCase(a.scope))
                    .map(a -> a.rawEmissionsKg).reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal orgTotal = s1.add(s2Loc).add(s3);
            totalCorporateSum = totalCorporateSum.add(orgTotal);

            String role = organizations.findByName(orgName).map(o -> o.stakeholderRole).orElse("STAKEHOLDER");
            rows.add(new OrgInventoryRowDto(orgName, role, s1, s2Loc, s2Mkt, s3, orgTotal));
        }

        // Product Footprint Reconciled Total (sum of unique physical events once)
        BigDecimal reconciledBatchTotal = all.stream()
                .filter(a -> Boolean.TRUE.equals(a.isBatchProductAllocated))
                .filter(a -> !Boolean.TRUE.equals(a.isScope2MarketBased))
                .map(a -> a.allocatedEmissionsKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);

        BigDecimal delta = totalCorporateSum.subtract(reconciledBatchTotal);

        return new CorporateInventoryReportDto(
                "2026-Q1",
                rows,
                totalCorporateSum.setScale(4, RoundingMode.HALF_UP),
                reconciledBatchTotal,
                delta.setScale(4, RoundingMode.HALF_UP),
                "If every stakeholder's corporate inventory is summed without event reconciliation, " +
                        totalCorporateSum.setScale(2, RoundingMode.HALF_UP) + " kg CO2e is computed. " +
                        "The reconciled unique physical event ledger allocates each event once, yielding " +
                        reconciledBatchTotal.setScale(2, RoundingMode.HALF_UP) + " kg CO2e, eliminating " +
                        delta.setScale(2, RoundingMode.HALF_UP) + " kg CO2e in double-counted boundaries."
        );
    }

    @Operation(summary = "Audit event log (Admin/Reviewer see all; Stakeholder sees own organization)")
    @GetMapping("/audit")
    public List<AuditEvent> auditEvents(Authentication auth) {
        boolean isAdmin = hasRole(auth, "ADMIN");
        boolean isReviewer = hasRole(auth, "REVIEWER");
        AppUser current = users.findByUsername(auth.getName()).orElseThrow();

        if (isAdmin || isReviewer) {
            return audit.findAllByOrderByCreatedAtDesc();
        }
        return audit.findByOrganizationNameOrderByCreatedAtDesc(current.organizationName);
    }

    // --- Helper Methods ---

    private boolean hasRole(Authentication a, String role) {
        if (a == null) return false;
        return a.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(x -> x.equals("ROLE_" + role));
    }

    private ActivityDto toDto(EmissionActivity a) {
        return new ActivityDto(
                a.id,
                a.organizationName,
                a.facilityCode,
                a.batchId,
                a.stageSequence,
                a.stageName,
                a.activityName,
                a.scope,
                a.categoryCode,
                a.inputQuantity,
                a.inputUnit,
                a.standardQuantity,
                a.standardUnit,
                a.unitConversionFactor,
                a.factor.id,
                a.factor.factorCode,
                a.factor.name,
                a.factorSnapshot,
                a.gasSpecificName,
                a.gasGwpValue,
                a.shippedMassTonnes,
                a.distanceKm,
                a.calculatedTonneKm,
                a.allocationNumerator,
                a.allocationDenominator,
                a.allocationPercent,
                a.rawEmissionsKg,
                a.allocatedEmissionsKg,
                a.isScope2MarketBased,
                a.isBatchProductAllocated,
                a.lifecycleStage,
                a.eventKey,
                a.transferLegIdentifier,
                a.methodNote,
                a.assumptionsNote,
                a.createdBy,
                a.createdAt.toString()
        );
    }
}
