-- Optional Supabase SQL Editor reference. The Spring Boot application creates its JPA tables
-- automatically with spring.jpa.hibernate.ddl-auto=update. This table stores the 15 category labels
-- as a durable reference catalogue. All factors shipped in the starter are DEMO ONLY.
create table if not exists public.reference_scope3_categories (
  code text primary key,
  category_number integer not null unique check (category_number between 1 and 15),
  label text not null,
  created_at timestamptz not null default now()
);
insert into public.reference_scope3_categories(code, category_number, label) values
('CAT_1',1,'Purchased goods and services'),
('CAT_2',2,'Capital goods'),
('CAT_3',3,'Fuel- and energy-related activities'),
('CAT_4',4,'Upstream transportation and distribution'),
('CAT_5',5,'Waste generated in operations'),
('CAT_6',6,'Business travel'),
('CAT_7',7,'Employee commuting'),
('CAT_8',8,'Upstream leased assets'),
('CAT_9',9,'Downstream transportation and distribution'),
('CAT_10',10,'Processing of sold products'),
('CAT_11',11,'Use of sold products'),
('CAT_12',12,'End-of-life treatment of sold products'),
('CAT_13',13,'Downstream leased assets'),
('CAT_14',14,'Franchises'),
('CAT_15',15,'Investments')
on conflict (code) do nothing;
