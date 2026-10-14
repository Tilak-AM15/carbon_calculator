package com.example.carbon;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.util.List;

@SpringBootApplication
public class CarbonCalculatorApplication {
    public static void main(String[] args) { SpringApplication.run(CarbonCalculatorApplication.class, args); }

    @Bean
    CommandLineRunner seed(FactorRepository factors, AppUserRepository users, PasswordEncoder encoder) {
        return args -> {
            // Idempotent: seed missing demo users individually, even when the database is not empty.
            seedUser(users, encoder, "admin", env("DEMO_ADMIN_PASSWORD", "AdminDemo123!"), "ADMIN", "Platform Admin");
            seedUser(users, encoder, "supplier", env("DEMO_STAKEHOLDER_PASSWORD", "SupplierDemo123!"), "STAKEHOLDER", "Demo Supplier");
            seedUser(users, encoder, "manufacturer", env("DEMO_STAKEHOLDER_PASSWORD", "SupplierDemo123!"), "STAKEHOLDER", "Demo Manufacturer");
            seedUser(users, encoder, "logistics", env("DEMO_STAKEHOLDER_PASSWORD", "SupplierDemo123!"), "STAKEHOLDER", "Demo Logistics");
            seedUser(users, encoder, "distributor", env("DEMO_STAKEHOLDER_PASSWORD", "SupplierDemo123!"), "STAKEHOLDER", "Demo Distributor");
            seedUser(users, encoder, "wholesaler", env("DEMO_STAKEHOLDER_PASSWORD", "SupplierDemo123!"), "STAKEHOLDER", "Demo Wholesaler");
            seedUser(users, encoder, "retailer", env("DEMO_STAKEHOLDER_PASSWORD", "SupplierDemo123!"), "STAKEHOLDER", "Demo Retailer");
            seedUser(users, encoder, "wastehandler", env("DEMO_STAKEHOLDER_PASSWORD", "SupplierDemo123!"), "STAKEHOLDER", "Demo Waste Handler");
            seedUser(users, encoder, "reviewer", env("DEMO_REVIEWER_PASSWORD", "ReviewerDemo123!"), "REVIEWER", "Independent Reviewer");
            if (factors.count() == 0) {
                // Explicit demo-only factors; these are not Climatiq-verified and must never be presented as verified.
                factors.saveAll(List.of(
                    new EmissionFactor("DEMO-ELECTRICITY", "Illustrative electricity factor (demo only)", "kWh", new BigDecimal("0.45"), "DEMO ONLY — illustrative value, not a verified Climatiq factor; replace with cited licensed factor."),
                    new EmissionFactor("DEMO-MATERIAL", "Illustrative material factor (demo only)", "kg", new BigDecimal("1.20"), "DEMO ONLY — illustrative value, not a verified Climatiq factor; replace with cited licensed factor."),
                    new EmissionFactor("DEMO-FREIGHT", "Illustrative freight factor (demo only)", "tonne-km", new BigDecimal("0.10"), "DEMO ONLY — illustrative value, not a verified Climatiq factor; replace with cited licensed factor."),
                    new EmissionFactor("DEMO-WASTE", "Illustrative waste factor (demo only)", "kg", new BigDecimal("0.25"), "DEMO ONLY — illustrative value, not a verified Climatiq factor; replace with cited licensed factor.")
                ));
            }
        };
    }
    private static String env(String key, String fallback) { return System.getenv().getOrDefault(key, fallback); }
    private static void seedUser(AppUserRepository users, PasswordEncoder encoder, String username, String password, String role, String organization) {
        if (users.findByUsername(username).isEmpty()) users.save(new AppUser(username, encoder.encode(password), role, organization));
    }
}
