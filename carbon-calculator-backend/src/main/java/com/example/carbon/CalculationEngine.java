package com.example.carbon;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Map;

@Service
public class CalculationEngine {

    public record ConversionResult(BigDecimal standardQuantity, BigDecimal conversionRatio, String standardUnit) {}

    public record CalculationResult(
            BigDecimal standardQuantity,
            String standardUnit,
            BigDecimal conversionRatio,
            BigDecimal tonneKm,
            BigDecimal gwpValue,
            BigDecimal rawEmissionsKg,
            BigDecimal allocationPercent,
            BigDecimal allocatedEmissionsKg
    ) {}

    // Documented IPCC AR5 (100-year) Global Warming Potential values
    public static final Map<String, BigDecimal> IPCC_AR5_GWP = Map.of(
            "CO2", new BigDecimal("1.0"),
            "CH4", new BigDecimal("28.0"),
            "N2O", new BigDecimal("265.0"),
            "R-134A", new BigDecimal("1300.0"),
            "R-410A", new BigDecimal("1924.0"),
            "SF6", new BigDecimal("23500.0"),
            "R-404A", new BigDecimal("3922.0")
    );

    /**
     * Converts an input quantity from inputUnit to targetUnit.
     * Throws ResponseStatusException(BAD_REQUEST) on incompatible units.
     */
    public ConversionResult convertUnit(BigDecimal quantity, String inputUnitRaw, String targetUnitRaw) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be strictly positive");
        }
        if (inputUnitRaw == null || inputUnitRaw.isBlank() || targetUnitRaw == null || targetUnitRaw.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Units cannot be blank");
        }

        String from = normalize(inputUnitRaw);
        String to = normalize(targetUnitRaw);

        if (from.equals(to)) {
            return new ConversionResult(quantity.setScale(8, RoundingMode.HALF_UP), BigDecimal.ONE, targetUnitRaw);
        }

        BigDecimal factorToStandardFrom = getFactorToStandard(from);
        BigDecimal factorToStandardTo = getFactorToStandard(to);
        String familyFrom = getUnitFamily(from);
        String familyTo = getUnitFamily(to);

        if (!familyFrom.equals(familyTo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Incompatible units: cannot convert '" + inputUnitRaw + "' (" + familyFrom + ") to '" + targetUnitRaw + "' (" + familyTo + ")");
        }

        // ratio = factorToStandardFrom / factorToStandardTo
        BigDecimal ratio = factorToStandardFrom.divide(factorToStandardTo, 12, RoundingMode.HALF_UP);
        BigDecimal converted = quantity.multiply(ratio).setScale(8, RoundingMode.HALF_UP);
        return new ConversionResult(converted, ratio.setScale(8, RoundingMode.HALF_UP), targetUnitRaw);
    }

    /**
     * Executes the backend calculation adhering to GHG Protocol guidelines:
     * - Unit conversion
     * - Freight tonne-kilometre derivation
     * - Gas mass to CO2e conversion with documented GWP
     * - Allocation ratio calculation with cap at 100%
     */
    public CalculationResult calculate(
            BigDecimal inputQuantity,
            String inputUnit,
            EmissionFactor factor,
            BigDecimal allocationNumerator,
            BigDecimal allocationDenominator,
            BigDecimal shippedMassTonnes,
            BigDecimal distanceKm,
            String gasSpecificName
    ) {
        if (inputQuantity == null || inputQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Input quantity must be strictly greater than 0");
        }
        if (allocationNumerator == null || allocationNumerator.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Allocation numerator must be greater than 0");
        }
        if (allocationDenominator == null || allocationDenominator.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Allocation denominator must be greater than 0");
        }
        if (allocationNumerator.compareTo(allocationDenominator) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Allocation numerator (" + allocationNumerator + ") cannot exceed denominator (" + allocationDenominator + ")");
        }

        BigDecimal allocationRatio = allocationNumerator.divide(allocationDenominator, 12, RoundingMode.HALF_UP);
        BigDecimal allocationPercent = allocationRatio.multiply(new BigDecimal("100")).setScale(4, RoundingMode.HALF_UP);

        BigDecimal standardQuantity = inputQuantity;
        BigDecimal conversionRatio = BigDecimal.ONE;
        String standardUnit = factor.activityUnit;
        BigDecimal calculatedTonneKm = null;
        BigDecimal gwp = null;
        BigDecimal rawEmissionsKg;

        String factorUnitNorm = normalize(factor.activityUnit);

        if (factorUnitNorm.contains("tonne.km") || factorUnitNorm.contains("tonne-km") || factorUnitNorm.contains("t.km")) {
            // Freight calculation: mass in tonnes * distance in km
            if (shippedMassTonnes != null && distanceKm != null && shippedMassTonnes.compareTo(BigDecimal.ZERO) > 0 && distanceKm.compareTo(BigDecimal.ZERO) > 0) {
                calculatedTonneKm = shippedMassTonnes.multiply(distanceKm).setScale(8, RoundingMode.HALF_UP);
                standardQuantity = calculatedTonneKm;
                conversionRatio = BigDecimal.ONE;
                rawEmissionsKg = calculatedTonneKm.multiply(factor.factorValue).setScale(8, RoundingMode.HALF_UP);
            } else {
                // User passed quantity directly in tonne-km (or ton-miles)
                ConversionResult cr = convertUnit(inputQuantity, inputUnit, factor.activityUnit);
                standardQuantity = cr.standardQuantity();
                conversionRatio = cr.conversionRatio();
                calculatedTonneKm = standardQuantity;
                rawEmissionsKg = standardQuantity.multiply(factor.factorValue).setScale(8, RoundingMode.HALF_UP);
            }
        } else if (gasSpecificName != null && !gasSpecificName.isBlank()) {
            // Fugitive refrigerant or specific GHG gas conversion
            String gasKey = gasSpecificName.trim().toUpperCase(Locale.ROOT);
            if (!IPCC_AR5_GWP.containsKey(gasKey)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown greenhouse gas: " + gasSpecificName + ". Supported: " + IPCC_AR5_GWP.keySet());
            }
            gwp = IPCC_AR5_GWP.get(gasKey);

            ConversionResult cr = convertUnit(inputQuantity, inputUnit, "kg");
            standardQuantity = cr.standardQuantity();
            conversionRatio = cr.conversionRatio();
            standardUnit = "kg";

            // If factor is already 1.0 or gas mass factor
            rawEmissionsKg = standardQuantity.multiply(gwp).multiply(factor.factorValue).setScale(8, RoundingMode.HALF_UP);
        } else {
            // Standard activity-based or spend-based factor
            ConversionResult cr = convertUnit(inputQuantity, inputUnit, factor.activityUnit);
            standardQuantity = cr.standardQuantity();
            conversionRatio = cr.conversionRatio();
            rawEmissionsKg = standardQuantity.multiply(factor.factorValue).setScale(8, RoundingMode.HALF_UP);
        }

        BigDecimal allocatedEmissionsKg = rawEmissionsKg.multiply(allocationRatio).setScale(8, RoundingMode.HALF_UP);

        return new CalculationResult(
                standardQuantity,
                standardUnit,
                conversionRatio,
                calculatedTonneKm,
                gwp,
                rawEmissionsKg,
                allocationPercent,
                allocatedEmissionsKg
        );
    }

    private String normalize(String u) {
        String s = u.trim().toLowerCase(Locale.ROOT).replace(" ", "").replace("_", "-");
        if (s.equals("t") || s.equals("tonne") || s.equals("tonnes") || s.equals("metric-ton") || s.equals("metric-tons")) return "tonne";
        if (s.equals("kg") || s.equals("kilogram") || s.equals("kilograms")) return "kg";
        if (s.equals("g") || s.equals("gram") || s.equals("grams")) return "g";
        if (s.equals("lb") || s.equals("lbs") || s.equals("pound") || s.equals("pounds")) return "lb";
        if (s.equals("km") || s.equals("kilometer") || s.equals("kilometres") || s.equals("kilometers")) return "km";
        if (s.equals("mi") || s.equals("mile") || s.equals("miles")) return "mile";
        if (s.equals("kwh") || s.equals("kilowatt-hour")) return "kwh";
        if (s.equals("mwh") || s.equals("megawatt-hour")) return "mwh";
        if (s.equals("mj") || s.equals("megajoule")) return "mj";
        if (s.equals("gj") || s.equals("gigajoule")) return "gj";
        if (s.equals("l") || s.equals("liter") || s.equals("liters") || s.equals("litre") || s.equals("litres")) return "litre";
        if (s.equals("gal") || s.equals("gallon") || s.equals("gallons")) return "gal";
        if (s.equals("m3") || s.equals("cubic-meter") || s.equals("cubic-metre")) return "m3";
        if (s.equals("tonne.km") || s.equals("tonne-km") || s.equals("t.km") || s.equals("t-km")) return "tonne.km";
        if (s.equals("ton.mile") || s.equals("ton-mile")) return "ton.mile";
        if (s.equals("usd") || s.equals("$")) return "usd";
        if (s.equals("eur") || s.equals("€")) return "eur";
        if (s.equals("gbp") || s.equals("£")) return "gbp";
        if (s.equals("passenger.km") || s.equals("passenger-km")) return "passenger.km";
        if (s.equals("vehicle.km") || s.equals("vehicle-km")) return "vehicle.km";
        if (s.equals("m2.year") || s.equals("m2/year") || s.equals("m2-year")) return "m2.year";
        if (s.equals("unit") || s.equals("item") || s.equals("pcs") || s.equals("pieces")) return "unit";
        return s;
    }

    private String getUnitFamily(String u) {
        if (u.equals("kg") || u.equals("tonne") || u.equals("g") || u.equals("lb")) return "mass";
        if (u.equals("km") || u.equals("mile")) return "distance";
        if (u.equals("kwh") || u.equals("mwh") || u.equals("mj") || u.equals("gj")) return "energy";
        if (u.equals("litre") || u.equals("gal") || u.equals("m3")) return "volume";
        if (u.equals("tonne.km") || u.equals("ton.mile")) return "freight";
        if (u.equals("usd") || u.equals("eur") || u.equals("gbp")) return "currency";
        if (u.equals("passenger.km")) return "passenger_distance";
        if (u.equals("vehicle.km")) return "vehicle_distance";
        if (u.equals("m2.year")) return "area_time";
        if (u.equals("unit")) return "discrete";
        return "custom_" + u;
    }

    private BigDecimal getFactorToStandard(String u) {
        // Base units:
        // mass: kg
        // distance: km
        // energy: kWh
        // volume: litre
        // freight: tonne.km
        // currency: USD (1:1 benchmark)
        return switch (u) {
            case "kg" -> BigDecimal.ONE;
            case "tonne" -> new BigDecimal("1000.0");
            case "g" -> new BigDecimal("0.001");
            case "lb" -> new BigDecimal("0.45359237");

            case "km" -> BigDecimal.ONE;
            case "mile" -> new BigDecimal("1.609344");

            case "kwh" -> BigDecimal.ONE;
            case "mwh" -> new BigDecimal("1000.0");
            case "mj" -> new BigDecimal("0.27777778");
            case "gj" -> new BigDecimal("277.77778");

            case "litre" -> BigDecimal.ONE;
            case "gal" -> new BigDecimal("3.785411784");
            case "m3" -> new BigDecimal("1000.0");

            case "tonne.km" -> BigDecimal.ONE;
            case "ton.mile" -> new BigDecimal("1.459972");

            case "usd" -> BigDecimal.ONE;
            case "eur" -> new BigDecimal("1.08"); // reference exchange conversion
            case "gbp" -> new BigDecimal("1.27");

            default -> BigDecimal.ONE;
        };
    }
}
