package com.example.carbon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

public class CalculationEngineTest {

    private CalculationEngine engine;
    private EmissionFactor sampleFactor;
    private EmissionFactor freightFactor;
    private EmissionFactor refrigerantFactor;

    @BeforeEach
    void setUp() {
        engine = new CalculationEngine();

        sampleFactor = new EmissionFactor(
                "TEST-ELEC", "Test Electricity Factor", "SCOPE_2", "S2_LOCATION",
                "kWh", new BigDecimal("0.37120000"),
                "US EPA eGRID", "https://climatiq.io", "v37", 2024, "US", "CO2e", "IPCC AR5",
                "generation", "activity-based", "2026-10-13", "Open", false, "Test factor"
        );

        freightFactor = new EmissionFactor(
                "TEST-FREIGHT", "Test HGV Road Freight", "SCOPE_3", "CAT_4",
                "tonne.km", new BigDecimal("0.10820000"),
                "UK DEFRA", "https://climatiq.io", "v37", 2024, "Global", "CO2e", "IPCC AR5",
                "well-to-wheel", "freight-tonne-km", "2026-10-13", "Open", false, "Freight factor"
        );

        refrigerantFactor = new EmissionFactor(
                "TEST-R134A", "Test Fugitive R-134a", "SCOPE_1", "S1_FUGITIVE",
                "kg", BigDecimal.ONE,
                "IPCC AR5", "https://climatiq.io", "v37", 2024, "Global", "R-134a", "IPCC AR5",
                "fugitive", "activity-based", "2026-10-13", "Open", false, "Refrigerant factor"
        );
    }

    @Test
    @DisplayName("Unit conversion: mass conversion (tonne to kg, lb to kg, g to kg)")
    void testMassUnitConversions() {
        // 2.5 tonnes -> 2500 kg
        CalculationEngine.ConversionResult r1 = engine.convertUnit(new BigDecimal("2.5"), "tonne", "kg");
        assertEquals(new BigDecimal("2500.00000000"), r1.standardQuantity());
        assertEquals(new BigDecimal("1000.00000000"), r1.conversionRatio());

        // 100 lb -> 45.359237 kg
        CalculationEngine.ConversionResult r2 = engine.convertUnit(new BigDecimal("100.0"), "lb", "kg");
        assertEquals(new BigDecimal("45.35923700"), r2.standardQuantity());

        // 5000 g -> 5.0 kg
        CalculationEngine.ConversionResult r3 = engine.convertUnit(new BigDecimal("5000.0"), "g", "kg");
        assertEquals(new BigDecimal("5.00000000"), r3.standardQuantity());
    }

    @Test
    @DisplayName("Unit conversion: energy conversion (MWh to kWh, GJ to kWh)")
    void testEnergyUnitConversions() {
        // 3.5 MWh -> 3500 kWh
        CalculationEngine.ConversionResult r1 = engine.convertUnit(new BigDecimal("3.5"), "MWh", "kWh");
        assertEquals(new BigDecimal("3500.00000000"), r1.standardQuantity());

        // 1 GJ -> 277.77778 kWh
        CalculationEngine.ConversionResult r2 = engine.convertUnit(BigDecimal.ONE, "GJ", "kWh");
        assertEquals(new BigDecimal("277.77778000"), r2.standardQuantity());
    }

    @Test
    @DisplayName("Unit conversion: distance and volume conversions")
    void testDistanceAndVolumeConversions() {
        // 50 miles -> 80.4672 km
        CalculationEngine.ConversionResult r1 = engine.convertUnit(new BigDecimal("50.0"), "mile", "km");
        assertEquals(new BigDecimal("80.46720000"), r1.standardQuantity());

        // 500 gallons -> 1892.705892 litres
        CalculationEngine.ConversionResult r2 = engine.convertUnit(new BigDecimal("500.0"), "gal", "litre");
        assertEquals(new BigDecimal("1892.70589200"), r2.standardQuantity());

        // 1.5 m3 -> 1500 litres
        CalculationEngine.ConversionResult r3 = engine.convertUnit(new BigDecimal("1.5"), "m3", "litre");
        assertEquals(new BigDecimal("1500.00000000"), r3.standardQuantity());
    }

    @Test
    @DisplayName("Unit conversion: reject incompatible unit families")
    void testIncompatibleUnitsRejected() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                engine.convertUnit(BigDecimal.TEN, "kg", "kWh")
        );
        assertTrue(ex.getReason().contains("Incompatible units"));
    }

    @Test
    @DisplayName("Calculation: standard activity with unit conversion and decimal precision")
    void testStandardActivityCalculation() {
        // 2 MWh = 2000 kWh * 0.3712 factor = 742.4 kg CO2e, 100% allocation
        CalculationEngine.CalculationResult res = engine.calculate(
                new BigDecimal("2.0"), "MWh", sampleFactor,
                BigDecimal.ONE, BigDecimal.ONE,
                null, null, null
        );

        assertEquals(new BigDecimal("2000.00000000"), res.standardQuantity());
        assertEquals("kWh", res.standardUnit());
        assertEquals(new BigDecimal("742.40000000"), res.rawEmissionsKg());
        assertEquals(new BigDecimal("742.40000000"), res.allocatedEmissionsKg());
        assertEquals(new BigDecimal("100.0000"), res.allocationPercent());
    }

    @Test
    @DisplayName("Calculation: freight tonne-kilometres and shared leg allocation")
    void testFreightTonneKmAndSharedAllocation() {
        // 2.5 tonnes shipped over 450 km = 1,125.0 tonne.km
        // Raw emissions = 1,125.0 * 0.1082 = 121.725 kg CO2e
        // Allocation: 2.5 tonnes of 10.0 tonnes total truck capacity (25% allocation)
        // Allocated emissions = 121.725 * 0.25 = 30.43125 kg CO2e
        CalculationEngine.CalculationResult res = engine.calculate(
                new BigDecimal("1125.0"), "tonne.km", freightFactor,
                new BigDecimal("2.5"), new BigDecimal("10.0"),
                new BigDecimal("2.5"), new BigDecimal("450.0"), null
        );

        assertEquals(new BigDecimal("1125.00000000"), res.tonneKm());
        assertEquals(new BigDecimal("121.72500000"), res.rawEmissionsKg());
        assertEquals(new BigDecimal("25.0000"), res.allocationPercent());
        assertEquals(new BigDecimal("30.43125000"), res.allocatedEmissionsKg());
    }

    @Test
    @DisplayName("Calculation: fugitive refrigerant gas mass to CO2e using IPCC AR5 GWP")
    void testFugitiveRefrigerantGwpConversion() {
        // 0.25 kg of R-134a with documented IPCC AR5 GWP of 1,300
        // Expected emissions = 0.25 * 1300 = 325.0 kg CO2e
        CalculationEngine.CalculationResult res = engine.calculate(
                new BigDecimal("0.25"), "kg", refrigerantFactor,
                BigDecimal.ONE, BigDecimal.ONE,
                null, null, "R-134a"
        );

        assertEquals(new BigDecimal("1300.0"), res.gwpValue());
        assertEquals(new BigDecimal("325.00000000"), res.rawEmissionsKg());
        assertEquals(new BigDecimal("325.00000000"), res.allocatedEmissionsKg());
    }

    @Test
    @DisplayName("Validation: reject negative or zero quantities")
    void testNegativeQuantityRejected() {
        assertThrows(ResponseStatusException.class, () ->
                engine.calculate(new BigDecimal("-5.0"), "kWh", sampleFactor, BigDecimal.ONE, BigDecimal.ONE, null, null, null)
        );
        assertThrows(ResponseStatusException.class, () ->
                engine.calculate(BigDecimal.ZERO, "kWh", sampleFactor, BigDecimal.ONE, BigDecimal.ONE, null, null, null)
        );
    }

    @Test
    @DisplayName("Validation: reject allocation numerator exceeding denominator (>100%)")
    void testAllocationExceeding100PercentRejected() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                engine.calculate(new BigDecimal("100.0"), "kWh", sampleFactor, new BigDecimal("120.0"), new BigDecimal("100.0"), null, null, null)
        );
        assertTrue(ex.getReason().contains("cannot exceed denominator"));
    }

    @Test
    @DisplayName("Validation: reject non-positive allocation inputs")
    void testNonPositiveAllocationRejected() {
        assertThrows(ResponseStatusException.class, () ->
                engine.calculate(new BigDecimal("10.0"), "kWh", sampleFactor, BigDecimal.ZERO, new BigDecimal("100.0"), null, null, null)
        );
        assertThrows(ResponseStatusException.class, () ->
                engine.calculate(new BigDecimal("10.0"), "kWh", sampleFactor, new BigDecimal("10.0"), new BigDecimal("-100.0"), null, null, null)
        );
    }
}
