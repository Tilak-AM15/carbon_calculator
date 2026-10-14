-- PostgreSQL/Supabase reference SQL. Core application tables are managed by Spring Data JPA.
-- This file is safe to run repeatedly. It does not contain credentials or invented emission factors.
CREATE TABLE IF NOT EXISTS public.reference_scope3_categories (
  code varchar(16) PRIMARY KEY,
  category_number integer NOT NULL UNIQUE CHECK (category_number BETWEEN 1 AND 15),
  label varchar(160) NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now()
);
INSERT INTO public.reference_scope3_categories(code,category_number,label) VALUES
('CAT_1',1,'Purchased goods and services'),('CAT_2',2,'Capital goods'),
('CAT_3',3,'Fuel- and energy-related activities'),('CAT_4',4,'Upstream transportation and distribution'),
('CAT_5',5,'Waste generated in operations'),('CAT_6',6,'Business travel'),
('CAT_7',7,'Employee commuting'),('CAT_8',8,'Upstream leased assets'),
('CAT_9',9,'Downstream transportation and distribution'),('CAT_10',10,'Processing of sold products'),
('CAT_11',11,'Use of sold products'),('CAT_12',12,'End-of-life treatment of sold products'),
('CAT_13',13,'Downstream leased assets'),('CAT_14',14,'Franchises'),('CAT_15',15,'Investments')
ON CONFLICT (code) DO NOTHING;
