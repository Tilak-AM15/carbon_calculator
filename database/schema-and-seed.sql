-- ============================================================================
-- Evergreen Carbon Intelligence — PostgreSQL / Supabase Schema & Seed Script
-- Compatible with PostgreSQL 14+, Supabase, Render, AWS RDS
-- ============================================================================

-- 1. App Users & RBAC
CREATE TABLE IF NOT EXISTS public.app_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL, -- ADMIN, STAKEHOLDER, REVIEWER
    stakeholder_role VARCHAR(64), -- SUPPLIER, MANUFACTURER, LOGISTICS, DISTRIBUTOR, WHOLESALER, RETAILER, WASTE_HANDLER, AUDITOR, ADMIN
    organization_name VARCHAR(128) NOT NULL,
    email VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. Organizations
CREATE TABLE IF NOT EXISTS public.organizations (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL UNIQUE,
    stakeholder_role VARCHAR(64) NOT NULL,
    country VARCHAR(64),
    contact_email VARCHAR(128),
    description VARCHAR(512)
);

-- 3. Facilities
CREATE TABLE IF NOT EXISTS public.facilities (
    id BIGSERIAL PRIMARY KEY,
    facility_code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    organization_name VARCHAR(128) NOT NULL,
    city VARCHAR(64),
    country VARCHAR(64),
    grid_region VARCHAR(64)
);

-- 4. Products
CREATE TABLE IF NOT EXISTS public.products (
    id BIGSERIAL PRIMARY KEY,
    product_code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(512),
    category VARCHAR(64),
    functional_unit VARCHAR(32) NOT NULL DEFAULT 'unit',
    mass_per_unit_kg NUMERIC(12, 4)
);

-- 5. Product Batches
CREATE TABLE IF NOT EXISTS public.product_batches (
    id BIGSERIAL PRIMARY KEY,
    batch_id VARCHAR(64) NOT NULL UNIQUE,
    product_code VARCHAR(64) NOT NULL,
    product_name VARCHAR(128) NOT NULL,
    quantity NUMERIC(16, 4) NOT NULL DEFAULT 1000.0,
    unit VARCHAR(32) NOT NULL DEFAULT 'unit',
    reporting_period VARCHAR(32) NOT NULL DEFAULT '2026-Q1',
    organizational_boundary VARCHAR(128) DEFAULT 'Operational Control - Full Value Chain',
    status VARCHAR(32) DEFAULT 'ACTIVE',
    notes VARCHAR(512),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 6. Stage Transfers & Logistics Legs
CREATE TABLE IF NOT EXISTS public.stage_transfers (
    id BIGSERIAL PRIMARY KEY,
    leg_identifier VARCHAR(64) NOT NULL UNIQUE,
    batch_id VARCHAR(64) NOT NULL,
    stage_sequence INT NOT NULL,
    stage_name VARCHAR(128) NOT NULL,
    from_organization VARCHAR(128) NOT NULL,
    from_facility VARCHAR(64),
    to_organization VARCHAR(128) NOT NULL,
    to_facility VARCHAR(64),
    carrier_name VARCHAR(128) NOT NULL,
    transport_mode VARCHAR(64) NOT NULL,
    distance_km NUMERIC(12, 4) NOT NULL,
    shipped_mass_tonnes NUMERIC(12, 4) NOT NULL,
    tonne_km NUMERIC(16, 4) NOT NULL,
    is_shared_leg BOOLEAN NOT NULL DEFAULT FALSE,
    allocation_numerator NUMERIC(12, 4) NOT NULL DEFAULT 1.0,
    allocation_denominator NUMERIC(12, 4) NOT NULL DEFAULT 1.0,
    allocated_tonne_km NUMERIC(16, 4) NOT NULL,
    event_key VARCHAR(120) UNIQUE,
    notes VARCHAR(512),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 7. Climatiq Emission Factors
CREATE TABLE IF NOT EXISTS public.emission_factors (
    id BIGSERIAL PRIMARY KEY,
    factor_code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    scope VARCHAR(32) NOT NULL,
    category_code VARCHAR(32) NOT NULL,
    activity_unit VARCHAR(32) NOT NULL,
    factor_value NUMERIC(20, 8) NOT NULL,
    source_publisher VARCHAR(128),
    source_url VARCHAR(512),
    dataset_version VARCHAR(64) DEFAULT 'Climatiq Data Version 37',
    factor_year INT DEFAULT 2024,
    geography VARCHAR(64) DEFAULT 'Global',
    gas_basis VARCHAR(64) DEFAULT 'CO2e',
    gwp_basis VARCHAR(64) DEFAULT 'IPCC AR5 (100-year)',
    lifecycle_boundary VARCHAR(64) DEFAULT 'cradle-to-gate',
    calculation_method VARCHAR(64) DEFAULT 'activity-based',
    access_date VARCHAR(32) DEFAULT '2026-10-13',
    license VARCHAR(128),
    is_market_based_alternative BOOLEAN NOT NULL DEFAULT FALSE,
    source_note VARCHAR(1200)
);

-- 8. Category Configurations (Scope 1, 2, and all 15 Scope 3 Categories)
CREATE TABLE IF NOT EXISTS public.category_configs (
    code VARCHAR(32) PRIMARY KEY,
    scope VARCHAR(32) NOT NULL,
    category_number INT,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(512),
    status VARCHAR(32) NOT NULL, -- APPLICABLE, NOT_APPLICABLE, DATA_UNAVAILABLE
    status_reason VARCHAR(1000),
    lifecycle_stage VARCHAR(32) NOT NULL,
    standard_method VARCHAR(64)
);

-- 9. Emission Activities (Calculation Snapshots & Event Ledger)
CREATE TABLE IF NOT EXISTS public.emission_activities (
    id BIGSERIAL PRIMARY KEY,
    organization_name VARCHAR(128) NOT NULL,
    facility_code VARCHAR(64),
    batch_id VARCHAR(64) NOT NULL,
    stage_sequence INT DEFAULT 1,
    stage_name VARCHAR(128) NOT NULL,
    activity_name VARCHAR(128) NOT NULL,
    scope VARCHAR(32) NOT NULL,
    category_code VARCHAR(32) NOT NULL,
    input_quantity NUMERIC(20, 8) NOT NULL,
    input_unit VARCHAR(32) NOT NULL,
    standard_quantity NUMERIC(20, 8) NOT NULL,
    standard_unit VARCHAR(32) NOT NULL,
    unit_conversion_factor NUMERIC(20, 8) NOT NULL DEFAULT 1.0,
    factor_id BIGINT NOT NULL REFERENCES public.emission_factors(id),
    factor_snapshot NUMERIC(20, 8) NOT NULL,
    gas_specific_name VARCHAR(64),
    gas_gwp_value NUMERIC(12, 4),
    shipped_mass_tonnes NUMERIC(12, 4),
    distance_km NUMERIC(12, 4),
    calculated_tonne_km NUMERIC(16, 4),
    allocation_numerator NUMERIC(12, 8) NOT NULL DEFAULT 1.0,
    allocation_denominator NUMERIC(12, 8) NOT NULL DEFAULT 1.0,
    allocation_percent NUMERIC(10, 4) NOT NULL DEFAULT 100.0,
    raw_emissions_kg NUMERIC(20, 8) NOT NULL,
    allocated_emissions_kg NUMERIC(20, 8) NOT NULL,
    is_scope2_market_based BOOLEAN NOT NULL DEFAULT FALSE,
    is_batch_product_allocated BOOLEAN NOT NULL DEFAULT TRUE,
    lifecycle_stage VARCHAR(32) NOT NULL DEFAULT 'CRADLE_TO_RETAIL',
    event_key VARCHAR(120) UNIQUE,
    transfer_leg_identifier VARCHAR(64),
    method_note VARCHAR(1200),
    assumptions_note VARCHAR(1200),
    created_by VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 10. Audit Events
CREATE TABLE IF NOT EXISTS public.audit_events (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    organization_name VARCHAR(128) NOT NULL,
    action VARCHAR(32) NOT NULL,
    record_type VARCHAR(64) NOT NULL,
    record_id VARCHAR(64),
    details VARCHAR(1500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes for performance & integrity
CREATE INDEX IF NOT EXISTS idx_activities_batch ON public.emission_activities(batch_id);
CREATE INDEX IF NOT EXISTS idx_activities_org ON public.emission_activities(organization_name);
CREATE INDEX IF NOT EXISTS idx_activities_event_key ON public.emission_activities(event_key);
CREATE INDEX IF NOT EXISTS idx_transfers_batch ON public.stage_transfers(batch_id);
CREATE INDEX IF NOT EXISTS idx_audit_created ON public.audit_events(created_at DESC);

-- ============================================================================
-- Seed Reference Scope 3 Categories & Scopes
-- ============================================================================
INSERT INTO public.category_configs(code, scope, category_number, name, description, status, status_reason, lifecycle_stage, standard_method) VALUES
('S1_STATIONARY', 'SCOPE_1', 0, 'Stationary Combustion', 'Boilers, industrial generators and heating equipment', 'APPLICABLE', 'Used in manufacturing boiler steam heating.', 'CRADLE_TO_RETAIL', 'ACTIVITY_BASED'),
('S1_MOBILE', 'SCOPE_1', 0, 'Mobile Combustion', 'Owned/controlled commercial vehicles and agricultural tractors', 'APPLICABLE', 'Used in supplier harvest and tractor transport.', 'CRADLE_TO_RETAIL', 'ACTIVITY_BASED'),
('S1_PROCESS', 'SCOPE_1', 0, 'Process Emissions', 'Direct physical or chemical emissions from production', 'NOT_APPLICABLE', 'No high-temperature chemical transformation emissions in this assembly process.', 'CRADLE_TO_RETAIL', 'ACTIVITY_BASED'),
('S1_FUGITIVE', 'SCOPE_1', 0, 'Fugitive Emissions', 'Refrigerant gas leaks from chillers and HVAC systems', 'APPLICABLE', 'Refrigerant R-134a chiller leakage during plant operations.', 'CRADLE_TO_RETAIL', 'ACTIVITY_BASED'),
('S2_LOCATION', 'SCOPE_2', 0, 'Purchased Electricity (Location-Based)', 'Grid average emission factor based on local regional grid mix', 'APPLICABLE', 'Standard accounting for plant, warehouse and retail power consumption.', 'CRADLE_TO_RETAIL', 'ACTIVITY_BASED'),
('S2_MARKET', 'SCOPE_2', 0, 'Purchased Electricity (Market-Based)', 'Contractual supplier rates and EAC/REC renewable power PPAs', 'APPLICABLE', 'Demonstrated via EcoTech 100% Green Power contractual PPA (0.00 kg CO2e). Reported separately from location-based.', 'CRADLE_TO_RETAIL', 'SUPPLIER_SPECIFIC'),
('CAT_1', 'SCOPE_3', 1, 'Purchased Goods & Services', 'Raw materials, components and outsourced packaging', 'APPLICABLE', 'Bio-polymer feedstock and stainless steel components purchased for batch.', 'CRADLE_TO_RETAIL', 'ACTIVITY_BASED'),
('CAT_2', 'SCOPE_3', 2, 'Capital Goods', 'Manufacturing equipment and machinery depreciation', 'APPLICABLE', 'Tooling and injection molding machines allocated by capital spend.', 'CRADLE_TO_RETAIL', 'SPEND_BASED'),
('CAT_3', 'SCOPE_3', 3, 'Fuel & Energy Related Activities', 'Upstream fuel extraction, refining and transmission & distribution losses', 'APPLICABLE', 'Upstream grid line transmission losses for electricity consumed in production.', 'CRADLE_TO_RETAIL', 'ACTIVITY_BASED'),
('CAT_4', 'SCOPE_3', 4, 'Upstream Transportation & Distribution', 'Inbound raw material freight and inter-facility logistics', 'APPLICABLE', 'Road freight (Leg 1) and Rail freight (Leg 2) moving materials and components.', 'CRADLE_TO_RETAIL', 'FREIGHT_TONNE_KM'),
('CAT_5', 'SCOPE_3', 5, 'Waste Generated in Operations', 'Operational scrap and packaging waste generated during manufacturing', 'APPLICABLE', 'Scrap trimmings sent to local closed-loop recycling.', 'CRADLE_TO_RETAIL', 'ACTIVITY_BASED'),
('CAT_6', 'SCOPE_3', 6, 'Business Travel', 'Commercial flights, passenger trains and employee business lodging', 'APPLICABLE', 'Engineering quality auditor site visits during production run.', 'CRADLE_TO_RETAIL', 'ACTIVITY_BASED'),
('CAT_7', 'SCOPE_3', 7, 'Employee Commuting', 'Transportation of employees between homes and production facilities', 'APPLICABLE', 'Commuting emissions for manufacturing line technicians allocated to batch.', 'CRADLE_TO_RETAIL', 'ACTIVITY_BASED'),
('CAT_8', 'SCOPE_3', 8, 'Upstream Leased Assets', 'Operations of assets leased by the reporting company', 'APPLICABLE', 'Leased warehouse buffer space allocated by floor area.', 'CRADLE_TO_RETAIL', 'ACTIVITY_BASED'),
('CAT_9', 'SCOPE_3', 9, 'Downstream Transportation & Distribution', 'Outbound freight from distribution hubs to retail shelves', 'APPLICABLE', 'Final mile van delivery to retail store branches.', 'CRADLE_TO_RETAIL', 'FREIGHT_TONNE_KM'),
('CAT_10', 'SCOPE_3', 10, 'Processing of Sold Products', 'Third-party customer processing of intermediate goods', 'NOT_APPLICABLE', 'EcoSmart bottle is a finished retail consumer good requiring no further conversion.', 'USE_AND_END_OF_LIFE', 'ACTIVITY_BASED'),
('CAT_11', 'SCOPE_3', 11, 'Use of Sold Products', 'Lifetime direct energy and fuel consumption during product use', 'APPLICABLE', 'Customer cleaning, hot water sanitization and dishwasher power over product lifetime.', 'USE_AND_END_OF_LIFE', 'ACTIVITY_BASED'),
('CAT_12', 'SCOPE_3', 12, 'End-of-Life Treatment of Sold Products', 'Waste disposal, municipal landfill or material recycling at end of life', 'APPLICABLE', 'End-of-life closed loop metal recovery and recycling.', 'USE_AND_END_OF_LIFE', 'ACTIVITY_BASED'),
('CAT_13', 'SCOPE_3', 13, 'Downstream Leased Assets', 'Operations of assets owned by reporting company and leased to third parties', 'NOT_APPLICABLE', 'Reporting organization does not lease operational assets to downstream third parties.', 'USE_AND_END_OF_LIFE', 'ACTIVITY_BASED'),
('CAT_14', 'SCOPE_3', 14, 'Franchises', 'Operations of franchise entities', 'NOT_APPLICABLE', 'Product is distributed via wholesale and direct retail; no franchise agreements exist.', 'CRADLE_TO_RETAIL', 'SPEND_BASED'),
('CAT_15', 'SCOPE_3', 15, 'Investments', 'Financed emissions from investments and lending activities', 'DATA_UNAVAILABLE', 'Downstream financial asset allocation data for batch investment financing is pending portfolio accounting.', 'USE_AND_END_OF_LIFE', 'SPEND_BASED')
ON CONFLICT (code) DO UPDATE SET
    status = EXCLUDED.status,
    status_reason = EXCLUDED.status_reason,
    name = EXCLUDED.name;
