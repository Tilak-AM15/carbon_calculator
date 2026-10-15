package com.example.carbon;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeDatabase(
            AppUserRepository users,
            OrganizationRepository orgs,
            FacilityRepository facilities,
            ProductRepository products,
            BatchRepository batches,
            StageTransferRepository transfers,
            EmissionFactorRepository factors,
            CategoryConfigRepository categories,
            EmissionActivityRepository activities,
            CalculationEngine engine,
            PasswordEncoder encoder
    ) {
        return args -> {
            seedOrganizations(orgs);
            seedFacilities(facilities);
            seedUsers(users, encoder);
            seedProductsAndBatches(products, batches);
            seedCategoryConfigs(categories);
            seedClimatiqFactors(factors);
            seedStageTransfers(transfers);
            seedActivities(activities, factors, engine);
        };
    }

    private void seedOrganizations(OrganizationRepository orgs) {
        if (orgs.count() == 0) {
            orgs.saveAll(List.of(
                    new Organization("ORG-ADMIN", "Evergreen Platform Administration", "ADMIN", "Global", "admin@evergreen-carbon.org", "Central carbon calculation and oversight authority"),
                    new Organization("ORG-APEX", "Apex Sustainable Agriculture", "SUPPLIER", "India", "sustainability@apex-bio.com", "Certified organic bio-polymer and agricultural feedstocks"),
                    new Organization("ORG-ECOTECH", "EcoTech Precision Manufacturing", "MANUFACTURER", "United States", "ops@ecotech-mfg.com", "Zero-waste precision assembly and thermoforming plant"),
                    new Organization("ORG-TRANSGLOBAL", "TransGlobal Green Freight", "LOGISTICS", "United States", "freight@transglobal-eco.com", "Intermodal green freight logistics and fleet management"),
                    new Organization("ORG-CENTRALDIST", "Central Green Distribution", "DISTRIBUTOR", "United States", "hubs@centralgreen-dist.com", "Automated low-emission distribution warehousing"),
                    new Organization("ORG-NORDIC", "Nordic Wholesalers Network", "WHOLESALER", "Netherlands", "terminal@nordic-wholesalers.eu", "Regional wholesale hub and supply aggregator"),
                    new Organization("ORG-BIOMARKET", "BioMarket Retail Stores", "RETAILER", "United States", "compliance@biomarket-stores.com", "Nationwide eco-certified sustainable retail chain"),
                    new Organization("ORG-CIRCULAR", "Circular Cycle Reclamation", "WASTE_HANDLER", "United States", "recovery@circularcycle-waste.com", "Closed-loop material recovery and industrial recycling facility"),
                    new Organization("ORG-ECOAUDIT", "EcoAudit International", "AUDITOR", "United Kingdom", "audits@ecoaudit-global.com", "Independent third-party greenhouse gas verifier and auditor")
            ));
        }
    }

    private void seedFacilities(FacilityRepository facilities) {
        if (facilities.count() == 0) {
            facilities.saveAll(List.of(
                    new Facility("FAC-APEX-FARM", "Apex Bio-Extraction Mill", "Apex Sustainable Agriculture", "Gujarat", "India", "IN-WEST-GRID"),
                    new Facility("FAC-ECOTECH-OHIO", "EcoTech Precision Plant 01", "EcoTech Precision Manufacturing", "Columbus", "United States", "US-RFCW-GRID"),
                    new Facility("FAC-TG-CHICAGO", "TransGlobal Midwest Intermodal Hub", "TransGlobal Green Freight", "Chicago", "United States", "US-MROW-GRID"),
                    new Facility("FAC-CENTRAL-INDY", "Central Regional Fulfillment Hub", "Central Green Distribution", "Indianapolis", "United States", "US-RFCW-GRID"),
                    new Facility("FAC-NORDIC-ROTT", "Nordic Wholesalers Terminal", "Nordic Wholesalers Network", "Rotterdam", "Netherlands", "EU-NL-GRID"),
                    new Facility("FAC-BIOMARKET-SF", "BioMarket Storefront Bay Area", "BioMarket Retail Stores", "San Francisco", "United States", "US-CAMX-GRID"),
                    new Facility("FAC-CIRCULAR-NV", "Circular Reclamation Plant", "Circular Cycle Reclamation", "Reno", "United States", "US-NWPP-GRID")
            ));
        }
    }

    private void seedUsers(AppUserRepository users, PasswordEncoder encoder) {
        seedUser(users, encoder, "admin", env("DEMO_ADMIN_PASSWORD", "AdminDemo123!"), "ADMIN", "ADMIN", "Evergreen Platform Administration", "admin@evergreen-carbon.org");
        seedUser(users, encoder, "supplier", env("DEMO_STAKEHOLDER_PASSWORD", "SupplierDemo123!"), "STAKEHOLDER", "SUPPLIER", "Apex Sustainable Agriculture", "supplier@apex-bio.com");
        seedUser(users, encoder, "manufacturer", env("DEMO_STAKEHOLDER_PASSWORD", "SupplierDemo123!"), "STAKEHOLDER", "MANUFACTURER", "EcoTech Precision Manufacturing", "mfg@ecotech-mfg.com");
        seedUser(users, encoder, "logistics", env("DEMO_STAKEHOLDER_PASSWORD", "SupplierDemo123!"), "STAKEHOLDER", "LOGISTICS", "TransGlobal Green Freight", "logistics@transglobal-eco.com");
        seedUser(users, encoder, "distributor", env("DEMO_STAKEHOLDER_PASSWORD", "SupplierDemo123!"), "STAKEHOLDER", "DISTRIBUTOR", "Central Green Distribution", "distributor@centralgreen-dist.com");
        seedUser(users, encoder, "wholesaler", env("DEMO_STAKEHOLDER_PASSWORD", "SupplierDemo123!"), "STAKEHOLDER", "WHOLESALER", "Nordic Wholesalers Network", "wholesaler@nordic-wholesalers.eu");
        seedUser(users, encoder, "retailer", env("DEMO_STAKEHOLDER_PASSWORD", "SupplierDemo123!"), "STAKEHOLDER", "RETAILER", "BioMarket Retail Stores", "retailer@biomarket-stores.com");
        seedUser(users, encoder, "wastehandler", env("DEMO_STAKEHOLDER_PASSWORD", "SupplierDemo123!"), "STAKEHOLDER", "WASTE_HANDLER", "Circular Cycle Reclamation", "waste@circularcycle-waste.com");
        seedUser(users, encoder, "reviewer", env("DEMO_REVIEWER_PASSWORD", "ReviewerDemo123!"), "REVIEWER", "AUDITOR", "EcoAudit International", "auditor@ecoaudit-global.com");
    }

    private void seedUser(AppUserRepository users, PasswordEncoder encoder, String username, String password, String role, String stakeholderRole, String organization, String email) {
        if (users.findByUsername(username).isEmpty()) {
            users.save(new AppUser(username, encoder.encode(password), role, stakeholderRole, organization, email));
        }
    }

    private void seedProductsAndBatches(ProductRepository products, BatchRepository batches) {
        if (products.findByProductCode("PROD-ECO-01").isEmpty()) {
            products.save(new Product(
                    "PROD-ECO-01",
                    "EcoSmart Circular Thermal Bottle (750ml)",
                    "Double-walled vacuum insulated bottle manufactured with recycled stainless steel and plant-based biopolymers.",
                    "Consumer Goods / Reusable Drinkware",
                    "unit",
                    new BigDecimal("0.4500")
            ));
        }

        if (batches.findByBatchId("BATCH-2026-ECO-01").isEmpty()) {
            batches.save(new ProductBatch(
                    "BATCH-2026-ECO-01",
                    "PROD-ECO-01",
                    "EcoSmart Circular Thermal Bottle (750ml)",
                    new BigDecimal("1000.0000"),
                    "unit",
                    "2026-Q1",
                    "Operational Control - Full Value Chain (Cradle to Grave)",
                    "Flagship production batch passing through all supply chain stakeholders with verified Climatiq factors."
            ));
        }
    }

    private void seedCategoryConfigs(CategoryConfigRepository categories) {
        if (categories.count() == 0) {
            categories.saveAll(List.of(
                    // Scope 1
                    new CategoryConfig("S1_STATIONARY", "SCOPE_1", 0, "Stationary Combustion", "Boilers, industrial generators and heating equipment", "APPLICABLE", "Used in manufacturing boiler steam heating.", "CRADLE_TO_RETAIL", "ACTIVITY_BASED"),
                    new CategoryConfig("S1_MOBILE", "SCOPE_1", 0, "Mobile Combustion", "Owned/controlled commercial vehicles and agricultural tractors", "APPLICABLE", "Used in supplier harvest and tractor transport.", "CRADLE_TO_RETAIL", "ACTIVITY_BASED"),
                    new CategoryConfig("S1_PROCESS", "SCOPE_1", 0, "Process Emissions", "Direct physical or chemical emissions from production", "NOT_APPLICABLE", "No high-temperature chemical transformation emissions in this assembly process.", "CRADLE_TO_RETAIL", "ACTIVITY_BASED"),
                    new CategoryConfig("S1_FUGITIVE", "SCOPE_1", 0, "Fugitive Emissions", "Refrigerant gas leaks from chillers and HVAC systems", "APPLICABLE", "Refrigerant R-134a chiller leakage during plant operations.", "CRADLE_TO_RETAIL", "ACTIVITY_BASED"),

                    // Scope 2
                    new CategoryConfig("S2_LOCATION", "SCOPE_2", 0, "Purchased Electricity (Location-Based)", "Grid average emission factor based on local regional grid mix", "APPLICABLE", "Standard accounting for plant, warehouse and retail power consumption.", "CRADLE_TO_RETAIL", "ACTIVITY_BASED"),
                    new CategoryConfig("S2_MARKET", "SCOPE_2", 0, "Purchased Electricity (Market-Based)", "Contractual supplier rates and EAC/REC renewable power PPAs", "APPLICABLE", "Demonstrated via EcoTech 100% Green Power contractual PPA (0.00 kg CO2e). Reported separately from location-based.", "CRADLE_TO_RETAIL", "SUPPLIER_SPECIFIC"),

                    // Scope 3 Categories 1 to 15
                    new CategoryConfig("CAT_1", "SCOPE_3", 1, "Purchased Goods & Services", "Raw materials, components and outsourced packaging", "APPLICABLE", "Bio-polymer feedstock and stainless steel components purchased for batch.", "CRADLE_TO_RETAIL", "ACTIVITY_BASED"),
                    new CategoryConfig("CAT_2", "SCOPE_3", 2, "Capital Goods", "Manufacturing equipment and machinery depreciation", "APPLICABLE", "Tooling and injection molding machines allocated by capital spend.", "CRADLE_TO_RETAIL", "SPEND_BASED"),
                    new CategoryConfig("CAT_3", "SCOPE_3", 3, "Fuel & Energy Related Activities", "Upstream fuel extraction, refining and transmission & distribution losses", "APPLICABLE", "Upstream grid line transmission losses for electricity consumed in production.", "CRADLE_TO_RETAIL", "ACTIVITY_BASED"),
                    new CategoryConfig("CAT_4", "SCOPE_3", 4, "Upstream Transportation & Distribution", "Inbound raw material freight and inter-facility logistics", "APPLICABLE", "Road freight (Leg 1) and Rail freight (Leg 2) moving materials and components.", "CRADLE_TO_RETAIL", "FREIGHT_TONNE_KM"),
                    new CategoryConfig("CAT_5", "SCOPE_3", 5, "Waste Generated in Operations", "Operational scrap and packaging waste generated during manufacturing", "APPLICABLE", "Scrap trimmings sent to local closed-loop recycling.", "CRADLE_TO_RETAIL", "ACTIVITY_BASED"),
                    new CategoryConfig("CAT_6", "SCOPE_3", 6, "Business Travel", "Commercial flights, passenger trains and employee business lodging", "APPLICABLE", "Engineering quality auditor site visits during production run.", "CRADLE_TO_RETAIL", "ACTIVITY_BASED"),
                    new CategoryConfig("CAT_7", "SCOPE_3", 7, "Employee Commuting", "Transportation of employees between homes and production facilities", "APPLICABLE", "Commuting emissions for manufacturing line technicians allocated to batch.", "CRADLE_TO_RETAIL", "ACTIVITY_BASED"),
                    new CategoryConfig("CAT_8", "SCOPE_3", 8, "Upstream Leased Assets", "Operations of assets leased by the reporting company", "APPLICABLE", "Leased warehouse buffer space allocated by floor area.", "CRADLE_TO_RETAIL", "ACTIVITY_BASED"),
                    new CategoryConfig("CAT_9", "SCOPE_3", 9, "Downstream Transportation & Distribution", "Outbound freight from distribution hubs to retail shelves", "APPLICABLE", "Final mile van delivery to retail store branches.", "CRADLE_TO_RETAIL", "FREIGHT_TONNE_KM"),
                    new CategoryConfig("CAT_10", "SCOPE_3", 10, "Processing of Sold Products", "Third-party customer processing of intermediate goods", "NOT_APPLICABLE", "EcoSmart bottle is a finished retail consumer good requiring no further conversion.", "USE_AND_END_OF_LIFE", "ACTIVITY_BASED"),
                    new CategoryConfig("CAT_11", "SCOPE_3", 11, "Use of Sold Products", "Lifetime direct energy and fuel consumption during product use", "APPLICABLE", "Customer cleaning, hot water sanitization and dishwasher power over product lifetime.", "USE_AND_END_OF_LIFE", "ACTIVITY_BASED"),
                    new CategoryConfig("CAT_12", "SCOPE_3", 12, "End-of-Life Treatment of Sold Products", "Waste disposal, municipal landfill or material recycling at end of life", "APPLICABLE", "End-of-life closed loop metal recovery and recycling.", "USE_AND_END_OF_LIFE", "ACTIVITY_BASED"),
                    new CategoryConfig("CAT_13", "SCOPE_3", 13, "Downstream Leased Assets", "Operations of assets owned by reporting company and leased to third parties", "NOT_APPLICABLE", "Reporting organization does not lease operational assets to downstream third parties.", "USE_AND_END_OF_LIFE", "ACTIVITY_BASED"),
                    new CategoryConfig("CAT_14", "SCOPE_3", 14, "Franchises", "Operations of franchise entities", "NOT_APPLICABLE", "Product is distributed via wholesale and direct retail; no franchise agreements exist.", "CRADLE_TO_RETAIL", "SPEND_BASED"),
                    new CategoryConfig("CAT_15", "SCOPE_3", 15, "Investments", "Financed emissions from investments and lending activities", "DATA_UNAVAILABLE", "Downstream financial asset allocation data for batch investment financing is pending portfolio accounting.", "USE_AND_END_OF_LIFE", "SPEND_BASED")
            ));
        }
    }

    private void seedClimatiqFactors(EmissionFactorRepository factors) {
        if (factors.count() == 0) {
            factors.saveAll(List.of(
                    // Scope 1
                    new EmissionFactor(
                            "CLIM-S1-NATGAS-DEFRA", "Natural Gas Stationary Combustion (100% Gross CV)",
                            "SCOPE_1", "S1_STATIONARY", "m3",
                            new BigDecimal("2.02130000"),
                            "UK DEFRA / DESNZ", "https://www.climatiq.io/data/explorer/fuel-combustion-natural_gas",
                            "Climatiq Data Version 37", 2024, "Global", "CO2e", "IPCC AR5 (100-year)",
                            "direct-combustion", "activity-based", "2026-10-13",
                            "Open Government Licence v3.0", false,
                            "Verified DEFRA 2024 natural gas combustion factor. Suitable for industrial boilers and heaters."
                    ),
                    new EmissionFactor(
                            "CLIM-S1-DIESEL-EPA", "Diesel Fuel Mobile Fleet Combustion",
                            "SCOPE_1", "S1_MOBILE", "litre",
                            new BigDecimal("2.70550000"),
                            "US EPA GHG Emission Factors Hub", "https://www.climatiq.io/data/explorer/fuel-diesel_us_epa",
                            "Climatiq Data Version 37", 2024, "US", "CO2e", "IPCC AR5 (100-year)",
                            "direct-combustion", "activity-based", "2026-10-13",
                            "US EPA Public Domain", false,
                            "US EPA 2024 mobile diesel factor for commercial fleet vehicles and agricultural tractors."
                    ),
                    new EmissionFactor(
                            "CLIM-S1-R134A-IPCC", "Refrigerant R-134a Fugitive Leakage (1,1,1,2-Tetrafluoroethane)",
                            "SCOPE_1", "S1_FUGITIVE", "kg",
                            new BigDecimal("1300.00000000"),
                            "IPCC AR5 / UK DEFRA", "https://www.climatiq.io/data/explorer/fugitive-refrigerant_r134a",
                            "Climatiq Data Version 37", 2024, "Global", "R-134a", "IPCC AR5 (100-year)",
                            "fugitive-emissions", "activity-based", "2026-10-13",
                            "IPCC Open Access", false,
                            "Documented GWP of 1,300 kg CO2e / kg gas mass according to IPCC Fifth Assessment Report (AR5)."
                    ),

                    // Scope 2
                    new EmissionFactor(
                            "CLIM-S2-ELEC-US-AVG", "Purchased Electricity (US National Average Grid Location-Based)",
                            "SCOPE_2", "S2_LOCATION", "kWh",
                            new BigDecimal("0.37120000"),
                            "US EPA eGRID / Climatiq v37", "https://www.climatiq.io/data/explorer/electricity-us_national_grid",
                            "Climatiq Data Version 37", 2024, "US", "CO2e", "IPCC AR5 (100-year)",
                            "generation-only", "activity-based", "2026-10-13",
                            "US EPA Public Domain", false,
                            "US national average location-based emission factor for purchased grid electricity."
                    ),
                    new EmissionFactor(
                            "CLIM-S2-ELEC-UK-GRID", "Purchased Electricity (UK National Grid Location-Based)",
                            "SCOPE_2", "S2_LOCATION", "kWh",
                            new BigDecimal("0.20707400"),
                            "UK DEFRA / DESNZ", "https://www.climatiq.io/data/explorer/electricity-uk_grid_location",
                            "Climatiq Data Version 37", 2024, "GB", "CO2e", "IPCC AR5 (100-year)",
                            "generation-only", "activity-based", "2026-10-13",
                            "Open Government Licence v3.0", false,
                            "Official UK DEFRA 2024 location-based grid emission factor (0.207074 kg CO2e/kWh)."
                    ),
                    new EmissionFactor(
                            "CLIM-S2-ELEC-CONTRACTUAL-GREEN", "Purchased Electricity (100% Renewable PPA with Guarantees of Origin)",
                            "SCOPE_2", "S2_MARKET", "kWh",
                            new BigDecimal("0.00000000"),
                            "GHG Protocol Scope 2 / EcoTech Contractual PPA", "https://www.climatiq.io/docs/scope-2-guidance",
                            "Climatiq Data Version 37", 2024, "US", "CO2e", "IPCC AR5 (100-year)",
                            "cradle-to-gate", "supplier-specific", "2026-10-13",
                            "Contractual Evidence / EAC Retained", true,
                            "Verified zero-carbon contractual market-based electricity backed by retired EAC certificates. Reported as market-based alternative; NEVER added to location-based."
                    ),
                    new EmissionFactor(
                            "CLIM-S2-ELEC-RESIDUAL-MIX", "Purchased Electricity (AIB Residual Mix Market-Based)",
                            "SCOPE_2", "S2_MARKET", "kWh",
                            new BigDecimal("0.41200000"),
                            "AIB Residual Mix 2024", "https://www.climatiq.io/data/explorer/electricity-residual_mix",
                            "Climatiq Data Version 37", 2024, "EU", "CO2e", "IPCC AR5 (100-year)",
                            "generation-only", "supplier-specific", "2026-10-13",
                            "AIB Open Data", true,
                            "Market-based residual mix emission factor used when no specific renewable contractual certificates exist."
                    ),

                    // Scope 3
                    new EmissionFactor(
                            "CLIM-S3-RAW-BIOPOLYMER", "Organic Plant-Based Bio-Polymer Feedstock",
                            "SCOPE_3", "CAT_1", "kg",
                            new BigDecimal("2.35000000"),
                            "UK DEFRA / Ecoinvent / Climatiq v37", "https://www.climatiq.io/data/explorer/materials-biopolymer",
                            "Climatiq Data Version 37", 2024, "Global", "CO2e", "IPCC AR5 (100-year)",
                            "cradle-to-gate", "activity-based", "2026-10-13",
                            "Licensed Dataset / Climatiq Terms", false,
                            "Cradle-to-gate carbon footprint of agricultural bio-polymer raw material."
                    ),
                    new EmissionFactor(
                            "CLIM-S3-RAW-STEEL", "Recycled Stainless Steel Coils (304 Grade)",
                            "SCOPE_3", "CAT_1", "kg",
                            new BigDecimal("1.85000000"),
                            "World Steel Association / Climatiq v37", "https://www.climatiq.io/data/explorer/materials-stainless_steel",
                            "Climatiq Data Version 37", 2024, "Global", "CO2e", "IPCC AR5 (100-year)",
                            "cradle-to-gate", "activity-based", "2026-10-13",
                            "WorldSteel Open Data", false,
                            "Cradle-to-gate emission factor for stainless steel containing 75% recycled scrap content."
                    ),
                    new EmissionFactor(
                            "CLIM-S3-SPEND-PROCUREMENT", "General Supply Chain Electronic Components (Spend-Based)",
                            "SCOPE_3", "CAT_1", "USD",
                            new BigDecimal("0.42000000"),
                            "US EEIO v2.0 / Climatiq v37", "https://www.climatiq.io/data/explorer/spend-electronic_components",
                            "Climatiq Data Version 37", 2024, "US", "CO2e", "IPCC AR5 (100-year)",
                            "cradle-to-gate", "spend-based", "2026-10-13",
                            "US EPA Public Domain", false,
                            "Environmentally Extended Input-Output factor for intermediate electronic and sensor components."
                    ),
                    new EmissionFactor(
                            "CLIM-S3-CAPITAL-EQUIPMENT", "Industrial Tooling & Machinery (Spend-Based)",
                            "SCOPE_3", "CAT_2", "USD",
                            new BigDecimal("0.31000000"),
                            "US EEIO v2.0 / Climatiq v37", "https://www.climatiq.io/data/explorer/spend-machinery",
                            "Climatiq Data Version 37", 2024, "US", "CO2e", "IPCC AR5 (100-year)",
                            "cradle-to-gate", "spend-based", "2026-10-13",
                            "US EPA Public Domain", false,
                            "Spend-based factor for capital manufacturing machinery allocated over asset useful life."
                    ),
                    new EmissionFactor(
                            "CLIM-S3-ENERGY-TD-LOSSES", "Grid Electricity Transmission & Distribution Losses",
                            "SCOPE_3", "CAT_3", "kWh",
                            new BigDecimal("0.04120000"),
                            "UK DEFRA / DESNZ", "https://www.climatiq.io/data/explorer/energy-td_losses",
                            "Climatiq Data Version 37", 2024, "Global", "CO2e", "IPCC AR5 (100-year)",
                            "transmission-only", "activity-based", "2026-10-13",
                            "Open Government Licence v3.0", false,
                            "Upstream emissions from power losses in electricity transmission networks."
                    ),
                    new EmissionFactor(
                            "CLIM-S3-FREIGHT-ROAD-HGV", "Heavy Goods Vehicle Road Freight (Articulated >33t, Average Laden)",
                            "SCOPE_3", "CAT_4", "tonne.km",
                            new BigDecimal("0.10820000"),
                            "UK DEFRA / DESNZ 2024", "https://www.climatiq.io/data/explorer/freight-hgv_road",
                            "Climatiq Data Version 37", 2024, "Global", "CO2e", "IPCC AR5 (100-year)",
                            "well-to-wheel", "freight-tonne-km", "2026-10-13",
                            "Open Government Licence v3.0", false,
                            "DEFRA 2024 well-to-wheel factor for articulated HGV road freight. Units: kg CO2e per tonne-kilometre."
                    ),
                    new EmissionFactor(
                            "CLIM-S3-FREIGHT-RAIL", "Freight Train Electric Cargo (Intermodal Container)",
                            "SCOPE_3", "CAT_4", "tonne.km",
                            new BigDecimal("0.02750000"),
                            "UK DEFRA / DESNZ 2024", "https://www.climatiq.io/data/explorer/freight-rail_cargo",
                            "Climatiq Data Version 37", 2024, "Global", "CO2e", "IPCC AR5 (100-year)",
                            "well-to-wheel", "freight-tonne-km", "2026-10-13",
                            "Open Government Licence v3.0", false,
                            "Low-carbon intermodal rail freight factor per tonne-kilometre."
                    ),
                    new EmissionFactor(
                            "CLIM-S3-WASTE-RECYCLING", "Operational Scrap Polymer Sent to Closed-Loop Recycling",
                            "SCOPE_3", "CAT_5", "kg",
                            new BigDecimal("0.02100000"),
                            "UK DEFRA / WRAP", "https://www.climatiq.io/data/explorer/waste-plastics_recycling",
                            "Climatiq Data Version 37", 2024, "Global", "CO2e", "IPCC AR5 (100-year)",
                            "waste-treatment", "activity-based", "2026-10-13",
                            "Open Government Licence v3.0", false,
                            "Net emissions from collection, sorting and closed-loop re-granulation of manufacturing polymer scrap."
                    ),
                    new EmissionFactor(
                            "CLIM-S3-TRAVEL-FLIGHT", "Commercial Passenger Flight Domestic Economy",
                            "SCOPE_3", "CAT_6", "passenger.km",
                            new BigDecimal("0.24580000"),
                            "UK DEFRA 2024 (with RF)", "https://www.climatiq.io/data/explorer/travel-domestic_flight",
                            "Climatiq Data Version 37", 2024, "Global", "CO2e", "IPCC AR5 (100-year)",
                            "well-to-wheel", "activity-based", "2026-10-13",
                            "Open Government Licence v3.0", false,
                            "Business travel flight emissions with radiative forcing index included."
                    ),
                    new EmissionFactor(
                            "CLIM-S3-COMMUTING-CAR", "Employee Commuting Passenger Car (Average Gasoline)",
                            "SCOPE_3", "CAT_7", "passenger.km",
                            new BigDecimal("0.17100000"),
                            "US EPA / DEFRA 2024", "https://www.climatiq.io/data/explorer/travel-passenger_car",
                            "Climatiq Data Version 37", 2024, "US", "CO2e", "IPCC AR5 (100-year)",
                            "well-to-wheel", "activity-based", "2026-10-13",
                            "US EPA Public Domain", false,
                            "Average single-occupant passenger car commuting emissions per kilometre."
                    ),
                    new EmissionFactor(
                            "CLIM-S3-LEASED-WAREHOUSE", "Upstream Leased Warehouse Storage Space",
                            "SCOPE_3", "CAT_8", "m2.year",
                            new BigDecimal("18.50000000"),
                            "CIBSE Guide F / UK DEFRA", "https://www.climatiq.io/data/explorer/buildings-warehouse_energy",
                            "Climatiq Data Version 37", 2024, "Global", "CO2e", "IPCC AR5 (100-year)",
                            "operational", "activity-based", "2026-10-13",
                            "CIBSE Open Data", false,
                            "Benchmarked operational emissions per square metre year for climate-controlled warehousing."
                    ),
                    new EmissionFactor(
                            "CLIM-S3-DOWNSTREAM-VAN", "Downstream Delivery Van (<3.5t Diesel Delivery)",
                            "SCOPE_3", "CAT_9", "vehicle.km",
                            new BigDecimal("0.21400000"),
                            "UK DEFRA 2024", "https://www.climatiq.io/data/explorer/freight-delivery_van",
                            "Climatiq Data Version 37", 2024, "Global", "CO2e", "IPCC AR5 (100-year)",
                            "well-to-wheel", "freight-vehicle-km", "2026-10-13",
                            "Open Government Licence v3.0", false,
                            "Outbound urban retail delivery van transport factor per vehicle kilometre."
                    ),
                    new EmissionFactor(
                            "CLIM-S3-USE-ENERGY", "Consumer In-Use Energy (Lifetime Hot Water Sanitization & Dishwasher)",
                            "SCOPE_3", "CAT_11", "kWh",
                            new BigDecimal("0.37120000"),
                            "US EPA eGRID 2024 / Climatiq v37", "https://www.climatiq.io/data/explorer/electricity-us_use_phase",
                            "Climatiq Data Version 37", 2024, "US", "CO2e", "IPCC AR5 (100-year)",
                            "use-phase", "activity-based", "2026-10-13",
                            "US EPA Public Domain", false,
                            "Electricity consumed over 3-year expected lifecycle for sanitization of 1,000 thermal bottles."
                    ),
                    new EmissionFactor(
                            "CLIM-S3-EOL-RECYCLING", "End-of-Life Product Recycling Reclamation Loop",
                            "SCOPE_3", "CAT_12", "kg",
                            new BigDecimal("0.03500000"),
                            "UK DEFRA / WRAP", "https://www.climatiq.io/data/explorer/waste-reclamation_metals",
                            "Climatiq Data Version 37", 2024, "Global", "CO2e", "IPCC AR5 (100-year)",
                            "end-of-life", "activity-based", "2026-10-13",
                            "Open Government Licence v3.0", false,
                            "End-of-life disassembly, shredding and circular metal recovery credit accounting."
                    )
            ));
        }
    }

    private void seedStageTransfers(StageTransferRepository transfers) {
        if (transfers.count() == 0) {
            StageTransfer leg1 = new StageTransfer();
            leg1.legIdentifier = "LEG-001-SUPPLIER-TO-MFG";
            leg1.batchId = "BATCH-2026-ECO-01";
            leg1.stageSequence = 2;
            leg1.stageName = "Upstream Inbound Freight (Supplier to Manufacturer)";
            leg1.fromOrganization = "Apex Sustainable Agriculture";
            leg1.fromFacility = "FAC-APEX-FARM";
            leg1.toOrganization = "EcoTech Precision Manufacturing";
            leg1.toFacility = "FAC-ECOTECH-OHIO";
            leg1.carrierName = "TransGlobal Green Freight";
            leg1.transportMode = "ROAD_FREIGHT";
            leg1.distanceKm = new BigDecimal("450.0000");
            leg1.shippedMassTonnes = new BigDecimal("2.5000");
            leg1.tonneKm = new BigDecimal("1125.0000");
            leg1.isSharedLeg = true;
            leg1.allocationNumerator = new BigDecimal("2.5000"); // 2.5t allocated of 10t total truck
            leg1.allocationDenominator = new BigDecimal("10.0000");
            leg1.allocatedTonneKm = new BigDecimal("281.2500");
            leg1.eventKey = "EVENT-LEG-001-SHARED-FREIGHT-2026";
            leg1.notes = "Shared articulated truck freight. 25% allocated to BATCH-2026-ECO-01 based on certified cargo weight.";
            transfers.save(leg1);

            StageTransfer leg2 = new StageTransfer();
            leg2.legIdentifier = "LEG-002-MFG-TO-DIST";
            leg2.batchId = "BATCH-2026-ECO-01";
            leg2.stageSequence = 4;
            leg2.stageName = "Inter-Facility Logistics (Manufacturer to Distribution)";
            leg2.fromOrganization = "EcoTech Precision Manufacturing";
            leg2.fromFacility = "FAC-ECOTECH-OHIO";
            leg2.toOrganization = "Central Green Distribution";
            leg2.toFacility = "FAC-CENTRAL-INDY";
            leg2.carrierName = "TransGlobal Green Freight";
            leg2.transportMode = "RAIL_FREIGHT";
            leg2.distanceKm = new BigDecimal("800.0000");
            leg2.shippedMassTonnes = new BigDecimal("2.2000");
            leg2.tonneKm = new BigDecimal("1760.0000");
            leg2.isSharedLeg = false;
            leg2.allocationNumerator = new BigDecimal("100.0000");
            leg2.allocationDenominator = new BigDecimal("100.0000");
            leg2.allocatedTonneKm = new BigDecimal("1760.0000");
            leg2.eventKey = "EVENT-LEG-002-RAIL-FREIGHT-2026";
            leg2.notes = "Dedicated intermodal electric rail freight for finished boxed bottles.";
            transfers.save(leg2);
        }
    }

    private void seedActivities(EmissionActivityRepository activities, EmissionFactorRepository factors, CalculationEngine engine) {
        if (activities.count() == 0) {
            String batchId = "BATCH-2026-ECO-01";

            // Stage 1: Raw Material Supplier (Apex Sustainable Agriculture)
            createSeedActivity(activities, factors, engine,
                    "Apex Sustainable Agriculture", "FAC-APEX-FARM", batchId, 1,
                    "1. Raw Material Sourcing", "Bio-Polymer Feedstock Cultivation & Extraction",
                    "SCOPE_3", "CAT_1", "CLIM-S3-RAW-BIOPOLYMER",
                    new BigDecimal("2500.0"), "kg",
                    new BigDecimal("100.0"), new BigDecimal("100.0"),
                    null, null, null,
                    false, true, "CRADLE_TO_RETAIL",
                    "EVENT-APEX-BIOPOLYMER-HARVEST", null,
                    "Raw agricultural feedstock for 1000 insulated bottle shells.", "supplier"
            );

            createSeedActivity(activities, factors, engine,
                    "Apex Sustainable Agriculture", "FAC-APEX-FARM", batchId, 1,
                    "1. Raw Material Sourcing", "Tractor Field Operations & Harvesting (Diesel)",
                    "SCOPE_1", "S1_MOBILE", "CLIM-S1-DIESEL-EPA",
                    new BigDecimal("150.0"), "litre",
                    new BigDecimal("100.0"), new BigDecimal("100.0"),
                    null, null, null,
                    false, true, "CRADLE_TO_RETAIL",
                    "EVENT-APEX-TRACTOR-DIESEL", null,
                    "Owned farm machinery mobile combustion for harvest.", "supplier"
            );

            // Stage 2: Upstream Logistics (TransGlobal Green Freight) - Shared Truck Freight Leg
            createSeedActivity(activities, factors, engine,
                    "TransGlobal Green Freight", "FAC-TG-CHICAGO", batchId, 2,
                    "2. Upstream Logistics", "Shared Truck Freight (Supplier to Factory)",
                    "SCOPE_3", "CAT_4", "CLIM-S3-FREIGHT-ROAD-HGV",
                    new BigDecimal("1125.0"), "tonne.km",
                    new BigDecimal("2.5"), new BigDecimal("10.0"), // 25% mass allocation
                    new BigDecimal("2.5"), new BigDecimal("450.0"), null,
                    false, true, "CRADLE_TO_RETAIL",
                    "EVENT-LEG-001-SHARED-FREIGHT-2026", "LEG-001-SUPPLIER-TO-MFG",
                    "Shared heavy articulated truck leg. 2.5 tonnes of 10.0 tonnes total truck payload allocated (25%).", "logistics"
            );

            // Stage 3: Manufacturer (EcoTech Precision Manufacturing)
            createSeedActivity(activities, factors, engine,
                    "EcoTech Precision Manufacturing", "FAC-ECOTECH-OHIO", batchId, 3,
                    "3. Manufacturing & Assembly", "Assembly Plant Boiler Natural Gas Heating",
                    "SCOPE_1", "S1_STATIONARY", "CLIM-S1-NATGAS-DEFRA",
                    new BigDecimal("300.0"), "m3",
                    new BigDecimal("50.0"), new BigDecimal("100.0"), // 50% allocation to this batch
                    null, null, null,
                    false, true, "CRADLE_TO_RETAIL",
                    "EVENT-ECOTECH-BOILER-HEAT", null,
                    "Industrial natural gas boiler heating allocated 50% to batch BATCH-2026-ECO-01.", "manufacturer"
            );

            createSeedActivity(activities, factors, engine,
                    "EcoTech Precision Manufacturing", "FAC-ECOTECH-OHIO", batchId, 3,
                    "3. Manufacturing & Assembly", "Assembly Cleanroom Chiller Refrigerant Leakage (R-134a)",
                    "SCOPE_1", "S1_FUGITIVE", "CLIM-S1-R134A-IPCC",
                    new BigDecimal("0.25"), "kg",
                    new BigDecimal("100.0"), new BigDecimal("100.0"),
                    null, null, "R-134a",
                    false, true, "CRADLE_TO_RETAIL",
                    "EVENT-ECOTECH-CHILLER-R134A", null,
                    "0.25 kg fugitive leakage of R-134a with documented IPCC AR5 GWP of 1,300.", "manufacturer"
            );

            createSeedActivity(activities, factors, engine,
                    "EcoTech Precision Manufacturing", "FAC-ECOTECH-OHIO", batchId, 3,
                    "3. Manufacturing & Assembly", "Molding & Assembly Grid Electricity (Location-Based)",
                    "SCOPE_2", "S2_LOCATION", "CLIM-S2-ELEC-US-AVG",
                    new BigDecimal("1680.0"), "kWh",
                    new BigDecimal("40.0"), new BigDecimal("100.0"), // 40% line allocation
                    null, null, null,
                    false, true, "CRADLE_TO_RETAIL",
                    "EVENT-ECOTECH-GRID-ELECTRICITY", null,
                    "Purchased grid electricity measured at meter and allocated 40% to batch.", "manufacturer"
            );

            // Scope 2 Market-Based Alternative for EcoTech (separate, not double counted in location-based)
            createSeedActivity(activities, factors, engine,
                    "EcoTech Precision Manufacturing", "FAC-ECOTECH-OHIO", batchId, 3,
                    "3. Manufacturing & Assembly", "100% Green Power Contractual PPA (Market-Based)",
                    "SCOPE_2", "S2_MARKET", "CLIM-S2-ELEC-CONTRACTUAL-GREEN",
                    new BigDecimal("1680.0"), "kWh",
                    new BigDecimal("40.0"), new BigDecimal("100.0"),
                    null, null, null,
                    true, true, "CRADLE_TO_RETAIL",
                    "EVENT-ECOTECH-PPA-MARKET-BASED", null,
                    "Contractual market-based reporting with certified EAC/REC retirements (0.00 kg CO2e). Never added to location-based total.", "manufacturer"
            );

            createSeedActivity(activities, factors, engine,
                    "EcoTech Precision Manufacturing", "FAC-ECOTECH-OHIO", batchId, 3,
                    "3. Manufacturing & Assembly", "Factory Polymer Scrap Sent to Recycling",
                    "SCOPE_3", "CAT_5", "CLIM-S3-WASTE-RECYCLING",
                    new BigDecimal("80.0"), "kg",
                    new BigDecimal("100.0"), new BigDecimal("100.0"),
                    null, null, null,
                    false, true, "CRADLE_TO_RETAIL",
                    "EVENT-ECOTECH-SCRAP-RECYCLING", null,
                    "Industrial polymer trim scrap collected and routed to local closed-loop reprocessing.", "manufacturer"
            );

            // Stage 4: Intermodal Logistics Leg 2 (TransGlobal Green Freight)
            createSeedActivity(activities, factors, engine,
                    "TransGlobal Green Freight", "FAC-TG-CHICAGO", batchId, 4,
                    "4. Finished Goods Rail Freight", "Intermodal Container Rail Freight (Factory to Hub)",
                    "SCOPE_3", "CAT_4", "CLIM-S3-FREIGHT-RAIL",
                    new BigDecimal("1760.0"), "tonne.km",
                    new BigDecimal("100.0"), new BigDecimal("100.0"),
                    new BigDecimal("2.2"), new BigDecimal("800.0"), null,
                    false, true, "CRADLE_TO_RETAIL",
                    "EVENT-LEG-002-RAIL-FREIGHT-2026", "LEG-002-MFG-TO-DIST",
                    "Intermodal electric rail container moving 2.2 tonnes over 800 km.", "logistics"
            );

            // Stage 5: Distributor (Central Green Distribution)
            createSeedActivity(activities, factors, engine,
                    "Central Green Distribution", "FAC-CENTRAL-INDY", batchId, 5,
                    "5. Distribution Hub", "Warehouse Climate Control & Operations (Location-Based)",
                    "SCOPE_2", "S2_LOCATION", "CLIM-S2-ELEC-US-AVG",
                    new BigDecimal("142.5"), "kWh",
                    new BigDecimal("15.0"), new BigDecimal("100.0"), // 15% bay allocation
                    null, null, null,
                    false, true, "CRADLE_TO_RETAIL",
                    "EVENT-CENTRALDIST-WAREHOUSE-POWER", null,
                    "Regional distribution center conveyor and climate power allocated by pallet area.", "distributor"
            );

            // Stage 6: Wholesaler (Nordic Wholesalers Network)
            createSeedActivity(activities, factors, engine,
                    "Nordic Wholesalers Network", "FAC-NORDIC-ROTT", batchId, 6,
                    "6. Wholesale Staging", "Wholesale Logistics Terminal Power (Location-Based)",
                    "SCOPE_2", "S2_LOCATION", "CLIM-S2-ELEC-UK-GRID",
                    new BigDecimal("80.0"), "kWh",
                    new BigDecimal("20.0"), new BigDecimal("100.0"),
                    null, null, null,
                    false, true, "CRADLE_TO_RETAIL",
                    "EVENT-NORDIC-WHOLESALE-POWER", null,
                    "Wholesale handling and terminal sorting electricity.", "wholesaler"
            );

            // Stage 7: Retailer (BioMarket Retail Stores)
            createSeedActivity(activities, factors, engine,
                    "BioMarket Retail Stores", "FAC-BIOMARKET-SF", batchId, 7,
                    "7. Retail Shelving", "Retail Display Lighting & HVAC Power (Location-Based)",
                    "SCOPE_2", "S2_LOCATION", "CLIM-S2-ELEC-US-AVG",
                    new BigDecimal("70.0"), "kWh",
                    new BigDecimal("20.0"), new BigDecimal("100.0"),
                    null, null, null,
                    false, true, "CRADLE_TO_RETAIL",
                    "EVENT-BIOMARKET-STORE-POWER", null,
                    "In-store display lighting and retail climate control allocated 20% to product footprint.", "retailer"
            );

            createSeedActivity(activities, factors, engine,
                    "BioMarket Retail Stores", "FAC-BIOMARKET-SF", batchId, 7,
                    "7. Retail Shelving", "Final Mile Store Distribution Van Delivery",
                    "SCOPE_3", "CAT_9", "CLIM-S3-DOWNSTREAM-VAN",
                    new BigDecimal("250.0"), "vehicle.km",
                    new BigDecimal("100.0"), new BigDecimal("100.0"),
                    null, null, null,
                    false, true, "CRADLE_TO_RETAIL",
                    "EVENT-BIOMARKET-VAN-DELIVERY", null,
                    "Final delivery van routes distributing inventory across local stores.", "retailer"
            );

            // Stage 8: Customer Product Use (Lifetime use phase)
            createSeedActivity(activities, factors, engine,
                    "BioMarket Retail Stores", "FAC-BIOMARKET-SF", batchId, 8,
                    "8. Consumer Product Use", "Expected 3-Year Lifetime Sanitization & Dishwasher Cleaning",
                    "SCOPE_3", "CAT_11", "CLIM-S3-USE-ENERGY",
                    new BigDecimal("2500.0"), "kWh",
                    new BigDecimal("100.0"), new BigDecimal("100.0"),
                    null, null, null,
                    false, true, "USE_AND_END_OF_LIFE",
                    "EVENT-CONSUMER-USE-PHASE-ESTIMATE", null,
                    "GHG Protocol Category 11 use-phase model: 2.5 kWh per bottle over 3-year expected functional lifetime.", "retailer"
            );

            // Stage 9: Recycler / Waste Handler (Circular Cycle Reclamation)
            createSeedActivity(activities, factors, engine,
                    "Circular Cycle Reclamation", "FAC-CIRCULAR-NV", batchId, 9,
                    "9. End-of-Life Reclamation", "Disassembly, Shredding & Circular Material Recovery",
                    "SCOPE_3", "CAT_12", "CLIM-S3-EOL-RECYCLING",
                    new BigDecimal("450.0"), "kg",
                    new BigDecimal("100.0"), new BigDecimal("100.0"),
                    null, null, null,
                    false, true, "USE_AND_END_OF_LIFE",
                    "EVENT-CIRCULAR-EOL-RECOVERY", null,
                    "End-of-life closed-loop metal and bio-polymer recovery accounting.", "wastehandler"
            );
        }
    }

    private void createSeedActivity(
            EmissionActivityRepository activities,
            EmissionFactorRepository factors,
            CalculationEngine engine,
            String orgName, String facilityCode, String batchId, int stageSeq,
            String stageName, String activityName, String scope, String catCode,
            String factorCode, BigDecimal inputQty, String inputUnit,
            BigDecimal allocNum, BigDecimal allocDenom,
            BigDecimal shippedMassTonnes, BigDecimal distanceKm, String gasSpecificName,
            boolean isMarketBased, boolean isBatchAllocated, String lifecycleStage,
            String eventKey, String legId, String note, String createdBy
    ) {
        EmissionFactor factor = factors.findByFactorCode(factorCode).orElseThrow();
        CalculationEngine.CalculationResult calc = engine.calculate(
                inputQty, inputUnit, factor, allocNum, allocDenom,
                shippedMassTonnes, distanceKm, gasSpecificName
        );

        EmissionActivity a = new EmissionActivity();
        a.organizationName = orgName;
        a.facilityCode = facilityCode;
        a.batchId = batchId;
        a.stageSequence = stageSeq;
        a.stageName = stageName;
        a.activityName = activityName;
        a.scope = scope;
        a.categoryCode = catCode;
        a.inputQuantity = inputQty;
        a.inputUnit = inputUnit;
        a.standardQuantity = calc.standardQuantity();
        a.standardUnit = calc.standardUnit();
        a.unitConversionFactor = calc.conversionRatio();
        a.factor = factor;
        a.factorSnapshot = factor.factorValue;
        a.gasSpecificName = gasSpecificName;
        a.gasGwpValue = calc.gwpValue();
        a.shippedMassTonnes = shippedMassTonnes;
        a.distanceKm = distanceKm;
        a.calculatedTonneKm = calc.tonneKm();
        a.allocationNumerator = allocNum;
        a.allocationDenominator = allocDenom;
        a.allocationPercent = calc.allocationPercent();
        a.rawEmissionsKg = calc.rawEmissionsKg();
        a.allocatedEmissionsKg = calc.allocatedEmissionsKg();
        a.isScope2MarketBased = isMarketBased;
        a.isBatchProductAllocated = isBatchAllocated;
        a.lifecycleStage = lifecycleStage;
        a.eventKey = eventKey;
        a.transferLegIdentifier = legId;
        a.methodNote = note;
        a.assumptionsNote = "Calculated via Evergreen Climatiq engine snapshot.";
        a.createdBy = createdBy;

        activities.save(a);
    }

    private static String env(String key, String fallback) {
        return System.getenv().getOrDefault(key, fallback);
    }
}
