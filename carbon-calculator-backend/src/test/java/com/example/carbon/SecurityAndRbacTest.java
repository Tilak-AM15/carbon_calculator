package com.example.carbon;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityAndRbacTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmissionFactorRepository factors;

    @Test
    @DisplayName("Security: Unauthenticated request to /api/activities is rejected with 401 Unauthorized")
    void testUnauthenticatedRequestRejected() throws Exception {
        mockMvc.perform(get("/api/activities"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Security: Public health and Swagger endpoints are accessible without authentication")
    void testPublicEndpointsAccessible() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("RBAC: Reviewer has read access to /api/me and /api/activities")
    void testReviewerReadAccessPermitted() throws Exception {
        mockMvc.perform(get("/api/me").with(httpBasic("reviewer", "ReviewerDemo123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("reviewer"))
                .andExpect(jsonPath("$.role").value("REVIEWER"));

        mockMvc.perform(get("/api/activities").with(httpBasic("reviewer", "ReviewerDemo123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    @DisplayName("RBAC: Reviewer attempting to POST activity is blocked with 403 Forbidden")
    void testReviewerCreateBlocked() throws Exception {
        Long factorId = factors.findAll().get(0).id;
        String jsonPayload = """
                {
                    "organizationName": "EcoAudit International",
                    "batchId": "BATCH-2026-ECO-01",
                    "stageName": "Audit Stage",
                    "activityName": "Unauthorized Reviewer Activity",
                    "scope": "SCOPE_1",
                    "categoryCode": "S1_STATIONARY",
                    "quantity": 100,
                    "unit": "m3",
                    "factorId": %d,
                    "allocationNumerator": 100,
                    "allocationDenominator": 100
                }
                """.formatted(factorId);

        mockMvc.perform(post("/api/activities")
                        .with(httpBasic("reviewer", "ReviewerDemo123!"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RBAC & Data Isolation: Stakeholder attempting cross-organization write is rejected with 403 Forbidden")
    void testCrossOrganizationWriteDenied() throws Exception {
        Long factorId = factors.findAll().get(0).id;
        // User 'supplier' belongs to 'Apex Sustainable Agriculture', attempting to write for 'EcoTech Precision Manufacturing'
        String crossOrgPayload = """
                {
                    "organizationName": "EcoTech Precision Manufacturing",
                    "batchId": "BATCH-2026-ECO-01",
                    "stageName": "Manufacturing Stage",
                    "activityName": "Cross Org Spoofed Entry",
                    "scope": "SCOPE_1",
                    "categoryCode": "S1_STATIONARY",
                    "quantity": 50,
                    "unit": "m3",
                    "factorId": %d,
                    "allocationNumerator": 100,
                    "allocationDenominator": 100
                }
                """.formatted(factorId);

        mockMvc.perform(post("/api/activities")
                        .with(httpBasic("supplier", "SupplierDemo123!"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crossOrgPayload))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RBAC: Stakeholder can successfully create activity for own organization")
    void testStakeholderCreateOwnOrganizationSuccess() throws Exception {
        EmissionFactor factor = factors.findByFactorCode("CLIM-S1-DIESEL-EPA").orElseThrow();
        String validPayload = """
                {
                    "organizationName": "Apex Sustainable Agriculture",
                    "batchId": "BATCH-2026-ECO-01",
                    "stageName": "1. Raw Material Sourcing",
                    "activityName": "Additional Farm Irrigation Pump Diesel",
                    "scope": "SCOPE_1",
                    "categoryCode": "S1_MOBILE",
                    "quantity": 25.0,
                    "unit": "litre",
                    "factorId": %d,
                    "allocationNumerator": 100,
                    "allocationDenominator": 100,
                    "eventKey": "EVENT-APEX-IRRIGATION-TEST-01"
                }
                """.formatted(factor.id);

        mockMvc.perform(post("/api/activities")
                        .with(httpBasic("supplier", "SupplierDemo123!"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.organizationName").value("Apex Sustainable Agriculture"))
                .andExpect(jsonPath("$.allocatedEmissionsKg").isNumber());
    }

    @Test
    @DisplayName("RBAC & Data Isolation: Stakeholder listing /api/activities sees only own organization rows")
    void testStakeholderDataIsolationOnListing() throws Exception {
        mockMvc.perform(get("/api/activities").with(httpBasic("supplier", "SupplierDemo123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].organizationName", everyItem(is("Apex Sustainable Agriculture"))));
    }

    @Test
    @DisplayName("Duplicate Prevention: Duplicate physical event key is rejected with 409 Conflict")
    void testDuplicatePhysicalEventKeyRejected() throws Exception {
        EmissionFactor factor = factors.findByFactorCode("CLIM-S1-DIESEL-EPA").orElseThrow();
        String payload = """
                {
                    "organizationName": "Apex Sustainable Agriculture",
                    "batchId": "BATCH-2026-ECO-01",
                    "stageName": "1. Raw Material Sourcing",
                    "activityName": "Duplicate Event Attempt",
                    "scope": "SCOPE_1",
                    "categoryCode": "S1_MOBILE",
                    "quantity": 10.0,
                    "unit": "litre",
                    "factorId": %d,
                    "allocationNumerator": 100,
                    "allocationDenominator": 100,
                    "eventKey": "EVENT-APEX-TRACTOR-DIESEL"
                }
                """.formatted(factor.id);

        mockMvc.perform(post("/api/activities")
                        .with(httpBasic("supplier", "SupplierDemo123!"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Validation: Non-positive quantity is rejected with 400 Bad Request")
    void testNegativeQuantityRejected() throws Exception {
        EmissionFactor factor = factors.findAll().get(0);
        String invalidPayload = """
                {
                    "organizationName": "Apex Sustainable Agriculture",
                    "batchId": "BATCH-2026-ECO-01",
                    "stageName": "1. Raw Material Sourcing",
                    "activityName": "Negative Quantity Attempt",
                    "scope": "SCOPE_1",
                    "categoryCode": "S1_STATIONARY",
                    "quantity": -10.0,
                    "unit": "%s",
                    "factorId": %d,
                    "allocationNumerator": 100,
                    "allocationDenominator": 100
                }
                """.formatted(factor.activityUnit, factor.id);

        mockMvc.perform(post("/api/activities")
                        .with(httpBasic("supplier", "SupplierDemo123!"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }
}
