DROP TABLE IF EXISTS tb_trace_record;
DROP TABLE IF EXISTS tb_order;
DROP TABLE IF EXISTS tb_wholesale_inquiry;
DROP TABLE IF EXISTS tb_live_room;
DROP TABLE IF EXISTS tb_origin_archive;
DROP TABLE IF EXISTS tb_product;
DROP TABLE IF EXISTS tb_cooperative;
DROP TABLE IF EXISTS tb_user;

CREATE TABLE tb_user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(120) NOT NULL,
  real_name VARCHAR(50) NOT NULL,
  phone VARCHAR(20) NOT NULL UNIQUE,
  role_type VARCHAR(20) NOT NULL DEFAULT 'CONSUMER',
  avatar VARCHAR(255) DEFAULT NULL,
  audit_status INT NOT NULL DEFAULT 1,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tb_cooperative (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  coop_name VARCHAR(100) NOT NULL,
  province VARCHAR(50) NOT NULL,
  city VARCHAR(50) NOT NULL,
  district VARCHAR(50) NOT NULL,
  detail_address VARCHAR(200) NOT NULL,
  longitude DOUBLE DEFAULT 101.5098,
  latitude DOUBLE DEFAULT 27.4211,
  base_area DOUBLE DEFAULT 1200.0,
  license_url VARCHAR(255) NOT NULL,
  credit_code VARCHAR(50) DEFAULT '91513400MA6XXXXX',
  description TEXT,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tb_product (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  coop_id BIGINT NOT NULL,
  title VARCHAR(150) NOT NULL,
  category VARCHAR(50) NOT NULL,
  origin_place VARCHAR(100) NOT NULL,
  retail_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  wholesale_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  min_wholesale_amount INT NOT NULL DEFAULT 500,
  stock INT NOT NULL DEFAULT 1000,
  unit VARCHAR(10) NOT NULL DEFAULT '斤',
  yield_cycle VARCHAR(50) DEFAULT '每年10月至次年2月',
  total_yield VARCHAR(50) DEFAULT '年产500吨',
  cover_image VARCHAR(255) NOT NULL,
  is_presale INT NOT NULL DEFAULT 0,
  presale_delivery_date DATE DEFAULT NULL,
  status INT NOT NULL DEFAULT 1,
  version INT NOT NULL DEFAULT 0,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tb_origin_archive (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  product_id BIGINT NOT NULL UNIQUE,
  soil_quality VARCHAR(100),
  water_environment VARCHAR(100),
  field_photos TEXT,
  pesticide_test_report VARCHAR(255),
  test_result VARCHAR(50),
  origin_story TEXT
);

CREATE TABLE tb_live_room (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  anchor_id BIGINT NOT NULL,
  title VARCHAR(100) NOT NULL,
  cover_url VARCHAR(255) NOT NULL,
  stream_url VARCHAR(255),
  current_product_id BIGINT DEFAULT NULL,
  status INT NOT NULL DEFAULT 1,
  online_count INT NOT NULL DEFAULT 2860,
  total_gmv DECIMAL(12,2) NOT NULL DEFAULT 58420.00,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tb_wholesale_inquiry (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  inquiry_no VARCHAR(32) NOT NULL UNIQUE,
  buyer_id BIGINT NOT NULL,
  coop_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  demand_quantity DECIMAL(12,2) NOT NULL,
  unit VARCHAR(10) NOT NULL DEFAULT '吨',
  expected_unit_price DECIMAL(10,2) NOT NULL,
  coop_quote_price DECIMAL(10,2) DEFAULT NULL,
  delivery_target_date DATE NOT NULL,
  packaging_demand VARCHAR(255),
  status INT NOT NULL DEFAULT 0,
  contract_no VARCHAR(50) DEFAULT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tb_order (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_no VARCHAR(32) NOT NULL UNIQUE,
  buyer_id BIGINT NOT NULL,
  order_type INT NOT NULL DEFAULT 1,
  total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  pay_status INT NOT NULL DEFAULT 1,
  order_status INT NOT NULL DEFAULT 1,
  receiver_name VARCHAR(50) NOT NULL,
  receiver_phone VARCHAR(20) NOT NULL,
  receiver_address VARCHAR(255) NOT NULL,
  tracking_no VARCHAR(50) DEFAULT 'SF10928392183',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tb_trace_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  trace_code VARCHAR(64) NOT NULL,
  product_id BIGINT NOT NULL,
  node_name VARCHAR(50) NOT NULL,
  node_time VARCHAR(30) NOT NULL,
  operator VARCHAR(50) NOT NULL,
  description TEXT NOT NULL
);
