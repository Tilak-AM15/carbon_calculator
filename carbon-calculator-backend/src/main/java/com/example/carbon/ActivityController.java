package com.example.carbon;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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
class ActivityController {
    private final ActivityRepository activities;
    private final FactorRepository factors;
    private final AppUserRepository users;
    private final AuditEventRepository audit;

    ActivityController(ActivityRepository activities, FactorRepository factors, AppUserRepository users, AuditEventRepository audit) {
        this.activities=activities; this.factors=factors; this.users=users; this.audit=audit;
    }

    record ActivityRequest(@NotBlank String organizationName, @NotBlank String batchId,
        @NotBlank String stageName, @NotBlank String activityName,
        @NotBlank String scope, @NotBlank String categoryCode,
        @NotNull @DecimalMin("0.00000001") BigDecimal quantity, @NotBlank String unit,
        @NotNull Long factorId, @NotNull @DecimalMin("0.00000001") BigDecimal allocationNumerator,
        @NotNull @DecimalMin("0.00000001") BigDecimal allocationDenominator, String eventKey, String methodNote) {}
    record CategoryDto(String scope, String code, String label) {}
    record ActivityDto(Long id, String organizationName, String batchId, String stageName,
        String activityName, String scope, String categoryCode, BigDecimal quantity, String unit,
        Long factorId, String factorName, BigDecimal factorSnapshot, BigDecimal emissionsKg,
        BigDecimal allocationPercent, String createdBy, String createdAt, String eventKey, String methodNote) {}
    record SummaryDto(String batchId, BigDecimal totalKgCo2e, BigDecimal totalTonnesCo2e,
        Map<String,BigDecimal> byScope, Map<String,BigDecimal> byCategory,
        Map<String,BigDecimal> byStage, List<ActivityDto> activities) {}

    record UserDto(String username, String role, String organizationName) {}
    record CategoryStatusDto(String scope, String code, String label, String status, String reason) {}

    @GetMapping("/me")
    UserDto me(Authentication auth) {
        AppUser u = users.findByUsername(auth.getName()).orElseThrow();
        return new UserDto(u.username, u.role, u.organizationName);
    }

    @GetMapping("/category-status")
    List<CategoryStatusDto> categoryStatus() {
        return Arrays.stream(ScopeCategory.values()).map(c -> new CategoryStatusDto(c.scope, c.code, c.label,
            "DATA_UNAVAILABLE", "Category is modeled, but a suitable verified factor/activity dataset has not been configured in this prototype." )).toList();
    }

    @GetMapping("/audit")
    List<Map<String,Object>> auditEvents(Authentication auth) {
        boolean admin = hasRole(auth,"ADMIN");
        boolean reviewer = hasRole(auth,"REVIEWER");
        AppUser current = users.findByUsername(auth.getName()).orElseThrow();
        List<AuditEvent> rows = (admin || reviewer) ? audit.findAllByOrderByCreatedAtDesc() : audit.findByOrganizationNameOrderByCreatedAtDesc(current.organizationName);
        return rows.stream().map(e -> {
            Map<String,Object> m = new LinkedHashMap<>(); m.put("id",e.id); m.put("username",e.username);
            m.put("organizationName",e.organizationName); m.put("action",e.action); m.put("recordType",e.recordType);
            m.put("recordId",e.recordId); m.put("details",e.details); m.put("createdAt",e.createdAt.toString()); return m;
        }).toList();
    }

    @GetMapping("/categories")
    List<CategoryDto> categories() {
        return Arrays.stream(ScopeCategory.values())
            .map(c -> new CategoryDto(c.scope,c.code,c.label)).toList();
    }

    @GetMapping("/factors")
    List<Map<String,Object>> factors() {
        return this.factors.findAll().stream().map(f -> {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("id", f.id); m.put("factorCode", f.factorCode); m.put("name", f.name);
            m.put("activityUnit", f.activityUnit); m.put("factorValue", f.factorValue); m.put("sourceNote", f.sourceNote);
            return m;
        }).toList();
    }

    @PostMapping("/activities")
    @ResponseStatus(HttpStatus.CREATED)
    ActivityDto create(@Valid @RequestBody ActivityRequest req, Authentication auth) {
        boolean admin = hasRole(auth,"ADMIN");
        AppUser current = users.findByUsername(auth.getName()).orElseThrow();
        if (!admin && !current.organizationName.equals(req.organizationName()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot create records for another organization");
        if (req.allocationNumerator().compareTo(req.allocationDenominator()) > 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Allocation numerator cannot exceed denominator");
        if (req.eventKey() != null && !req.eventKey().isBlank() && activities.findByEventKey(req.eventKey().trim()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This physical emission event already exists. Reuse its existing record to avoid double counting.");
        ScopeCategory cat = Arrays.stream(ScopeCategory.values())
            .filter(c -> c.code.equals(req.categoryCode()) && c.scope.equals(req.scope())).findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown scope/category combination"));
        EmissionFactor factor = factors.findById(req.factorId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Emission factor not found"));
        if (!factor.activityUnit.equalsIgnoreCase(req.unit()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unit does not match factor unit: expected " + factor.activityUnit);
        BigDecimal allocation = req.allocationNumerator().divide(req.allocationDenominator(), 8, RoundingMode.HALF_UP);
        BigDecimal kg = req.quantity().multiply(factor.factorValue).multiply(allocation).setScale(8, RoundingMode.HALF_UP);

        EmissionActivity a = new EmissionActivity();
        a.organizationName=req.organizationName(); a.batchId=req.batchId(); a.stageName=req.stageName();
        a.activityName=req.activityName(); a.scope=req.scope(); a.categoryCode=req.categoryCode();
        a.quantity=req.quantity(); a.unit=req.unit(); a.factor=factor; a.factorSnapshot=factor.factorValue;
        a.emissionsKg=kg; a.allocationNumerator=req.allocationNumerator(); a.allocationDenominator=req.allocationDenominator();
        a.createdBy=auth.getName();
        a.eventKey = req.eventKey() == null || req.eventKey().isBlank() ? null : req.eventKey().trim();
        a.methodNote = req.methodNote();
        EmissionActivity saved = activities.save(a);
        audit.save(new AuditEvent(auth.getName(), current.organizationName, "CREATE", "EMISSION_ACTIVITY", String.valueOf(saved.id),
            "Batch=" + saved.batchId + "; scope=" + saved.scope + "; category=" + saved.categoryCode + "; kgCO2e=" + saved.emissionsKg));
        return dto(saved);
    }

    @GetMapping("/activities")
    List<ActivityDto> list(Authentication auth) {
        if (hasRole(auth,"ADMIN") || hasRole(auth,"REVIEWER"))
            return activities.findAllByOrderByCreatedAtDesc().stream().map(this::dto).toList();
        AppUser user = users.findByUsername(auth.getName()).orElseThrow();
        return activities.findByOrganizationNameOrderByCreatedAtDesc(user.organizationName).stream().map(this::dto).toList();
    }

    @GetMapping("/batches/{batchId}/summary")
    SummaryDto summary(@PathVariable String batchId, Authentication auth) {
        List<EmissionActivity> rows = activities.findByBatchIdOrderByCreatedAtAsc(batchId);
        if (!hasRole(auth,"ADMIN") && !hasRole(auth,"REVIEWER")) {
            String org = users.findByUsername(auth.getName()).orElseThrow().organizationName;
            // Stakeholders may view only their own records in this starter. Add explicit linked-traceability permissions for the final assignment.
            rows = rows.stream().filter(a -> a.organizationName.equals(org)).toList();
        }
        BigDecimal total = rows.stream().map(a -> a.emissionsKg).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String,BigDecimal> byScope = group(rows, a -> a.scope);
        Map<String,BigDecimal> byCategory = group(rows, a -> a.categoryCode);
        Map<String,BigDecimal> byStage = group(rows, a -> a.stageName);
        return new SummaryDto(batchId,total.setScale(4,RoundingMode.HALF_UP),
            total.divide(new BigDecimal("1000"),8,RoundingMode.HALF_UP),byScope,byCategory,byStage,rows.stream().map(this::dto).toList());
    }

    private Map<String,BigDecimal> group(List<EmissionActivity> rows, java.util.function.Function<EmissionActivity,String> key) {
        return rows.stream().collect(Collectors.groupingBy(key, TreeMap::new,
            Collectors.reducing(BigDecimal.ZERO, a -> a.emissionsKg, BigDecimal::add)));
    }
    private boolean hasRole(Authentication a, String role) {
        return a.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(x -> x.equals("ROLE_"+role));
    }
    private ActivityDto dto(EmissionActivity a) {
        BigDecimal pct=a.allocationNumerator.multiply(new BigDecimal("100")).divide(a.allocationDenominator,4,RoundingMode.HALF_UP);
        return new ActivityDto(a.id,a.organizationName,a.batchId,a.stageName,a.activityName,a.scope,a.categoryCode,
            a.quantity,a.unit,a.factor.id,a.factor.name,a.factorSnapshot,a.emissionsKg,pct,a.createdBy,a.createdAt.toString(),a.eventKey,a.methodNote);
    }
}
