package com.example.carbon;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AggregationAndDoubleCountingTest {

    @Autowired
    private ActivityController activityController;

    @Autowired
    private EmissionActivityRepository activityRepository;

    @Autowired
    private StageTransferRepository transferRepository;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Aggregation test: Scope 2 location and market based alternatives are never added together")
    void testScope2LocationAndMarketBasedNeverAddedTogether() {
        ActivityController.BatchSummaryDto summary = activityController.batchSummary("BATCH-2026-ECO-01", null);

        assertNotNull(summary);
        BigDecimal s2Location = summary.scope2LocationBasedKgCo2e();
        BigDecimal s2Market = summary.scope2MarketBasedKgCo2e();
        BigDecimal totalFootprint = summary.totalProductFootprintKgCo2e();

        // Location based is positive (plant, warehouse, retail grid power)
        assertTrue(s2Location.compareTo(BigDecimal.ZERO) > 0, "Location-based Scope 2 must be positive");

        // Market based contractual PPA is tracked separately (0.00 kg CO2e)
        assertNotNull(s2Market, "Market-based Scope 2 must be tracked");

        // Prove location and market are not added together:
        // Sum of scopes in product footprint includes scope1 + scope2Location + scope3
        BigDecimal expectedSum = summary.scope1KgCo2e()
                .add(summary.scope2LocationBasedKgCo2e())
                .add(summary.scope3KgCo2e())
                .setScale(4, RoundingMode.HALF_UP);

        assertEquals(expectedSum, totalFootprint,
                "Total footprint must equal Scope 1 + Scope 2 Location + Scope 3, without adding Market-based Scope 2");

        // Verify explicit guidance note is included
        assertTrue(summary.scope2AccountingGuidance().contains("NEVER be added together"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Aggregation test: Shared transport event is counted once in batch footprint")
    void testSharedTransportEventCountedOnce() {
        ActivityController.BatchSummaryDto summary = activityController.batchSummary("BATCH-2026-ECO-01", null);

        // Find Leg 1 shared road freight activity
        long count = summary.activities().stream()
                .filter(a -> "EVENT-LEG-001-SHARED-FREIGHT-2026".equals(a.eventKey()))
                .count();

        assertEquals(1, count, "Shared freight leg event must appear exactly once in batch ledger");

        ActivityController.ActivityDto sharedLeg = summary.activities().stream()
                .filter(a -> "EVENT-LEG-001-SHARED-FREIGHT-2026".equals(a.eventKey()))
                .findFirst()
                .orElseThrow();

        // 25% allocation on 1125 tonne.km * 0.1082 factor = 30.43125 kg CO2e
        assertEquals(new BigDecimal("25.0000"), sharedLeg.allocationPercent());
        assertEquals(new BigDecimal("30.43125000"), sharedLeg.allocatedEmissionsKg());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Aggregation test: Corporate inventory separation prevents double counting in product footprint")
    void testCorporateInventorySeparationPreventsDoubleCounting() {
        ActivityController.CorporateInventoryReportDto report = activityController.corporateInventoryReport();

        assertNotNull(report);
        assertTrue(report.sumOfAllCorporateInventoriesKgCo2e().compareTo(report.reconciledBatchProductFootprintKgCo2e()) > 0,
                "Sum of unadjusted corporate inventories must exceed reconciled unique batch footprint");

        assertTrue(report.avoidedDoubleCountingDeltaKgCo2e().compareTo(BigDecimal.ZERO) > 0,
                "Avoided double counting delta must be strictly positive");

        assertTrue(report.explanation().contains("eliminating"), "Report must include transparent double-counting explanation");
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Aggregation test: Cradle-to-retail and full life-cycle totals reported separately")
    void testCradleToRetailAndFullLifeCycleSeparation() {
        ActivityController.BatchSummaryDto summary = activityController.batchSummary("BATCH-2026-ECO-01", null);

        BigDecimal cradleToRetail = summary.cradleToRetailKgCo2e();
        BigDecimal useAndEol = summary.useAndEndOfLifeKgCo2e();
        BigDecimal total = summary.totalProductFootprintKgCo2e();

        assertTrue(cradleToRetail.compareTo(BigDecimal.ZERO) > 0, "Cradle-to-retail emissions must be positive");
        assertTrue(useAndEol.compareTo(BigDecimal.ZERO) > 0, "Use and end-of-life emissions must be positive");

        assertEquals(cradleToRetail.add(useAndEol).setScale(4, RoundingMode.HALF_UP), total,
                "Total footprint must equal Cradle-to-Retail + Use and End-of-Life");

        // Intensity per unit: total / 1000 units
        assertEquals(total.divide(new BigDecimal("1000.0000"), 4, RoundingMode.HALF_UP), summary.intensityPerUnitKgCo2e());
    }
}
