-- ========================================================
-- 农产品直播电商产销对接平台 - 数据库脚本 (schema.sql)
-- 包含核心表结构定义、索引及五大角色高保真演示数据
-- ========================================================

DROP DATABASE IF EXISTS agri_platform;
CREATE DATABASE agri_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE agri_platform;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 用户表 (tb_user)
-- ----------------------------
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '登录账号',
  `password` VARCHAR(120) NOT NULL COMMENT '密码密文',
  `real_name` VARCHAR(50) NOT NULL COMMENT '实名姓名/企业名称',
  `phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
  `role_type` VARCHAR(20) NOT NULL DEFAULT 'CONSUMER' COMMENT '角色: COOP(合作社), ANCHOR(主播), WHOLESALE(批发商), CONSUMER(消费者), ADMIN(管理员)',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `audit_status` INT NOT NULL DEFAULT 1 COMMENT '审核状态: 0-待审核, 1-已认证, 2-已驳回',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户核心表';

-- ----------------------------
-- 2. 种植合作社资质档案表 (tb_cooperative)
-- ----------------------------
DROP TABLE IF EXISTS `tb_cooperative`;
CREATE TABLE `tb_cooperative` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '合作社ID',
  `user_id` BIGINT NOT NULL COMMENT '关联用户ID',
  `coop_name` VARCHAR(100) NOT NULL COMMENT '合作社法定全称',
  `province` VARCHAR(50) NOT NULL COMMENT '省份',
  `city` VARCHAR(50) NOT NULL COMMENT '城市',
  `district` VARCHAR(50) NOT NULL COMMENT '区县',
  `detail_address` VARCHAR(200) NOT NULL COMMENT '种植基地详细地址',
  `longitude` DECIMAL(10,6) DEFAULT 102.261200 COMMENT '基地经度',
  `latitude` DECIMAL(10,6) DEFAULT 27.881200 COMMENT '基地纬度',
  `base_area` DOUBLE DEFAULT 1200.0 COMMENT '基地种植面积(亩)',
  `license_url` VARCHAR(255) NOT NULL COMMENT '营业执照图片地址',
  `credit_code` VARCHAR(50) DEFAULT '91513400MA6XXXXX' COMMENT '统一社会信用代码',
  `description` TEXT COMMENT '合作社简介与优势',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='种植合作社资质档案表';

-- ----------------------------
-- 3. 农产品货架与大宗货源表 (tb_product)
-- ----------------------------
DROP TABLE IF EXISTS `tb_product`;
CREATE TABLE `tb_product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `coop_id` BIGINT NOT NULL COMMENT '供货种植合作社ID',
  `title` VARCHAR(150) NOT NULL COMMENT '商品展示标题',
  `category` VARCHAR(50) NOT NULL COMMENT '品类: 水果/蔬菜/粮油/禽蛋',
  `origin_place` VARCHAR(100) NOT NULL COMMENT '地理标志产地简称',
  `retail_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'C端零售价(元/件)',
  `wholesale_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'B端大宗参考单价(元/斤)',
  `min_wholesale_amount` INT NOT NULL DEFAULT 500 COMMENT '大宗最低起订量(斤)',
  `stock` INT NOT NULL DEFAULT 1000 COMMENT '当前库存(件/斤)',
  `unit` VARCHAR(10) NOT NULL DEFAULT '斤' COMMENT '计量单位',
  `yield_cycle` VARCHAR(50) DEFAULT '每年10月至次年2月' COMMENT '上市周期',
  `total_yield` VARCHAR(50) DEFAULT '年产500吨' COMMENT '预估年产量',
  `cover_image` VARCHAR(255) NOT NULL COMMENT '主图URL',
  `is_presale` INT NOT NULL DEFAULT 0 COMMENT '是否预售: 0-现货直供, 1-田间预售',
  `presale_delivery_date` DATE DEFAULT NULL COMMENT '预售预计发货日期',
  `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 0-下架, 1-在售, 2-封禁',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_coop_id` (`coop_id`),
  KEY `idx_category` (`category`),
  KEY `idx_origin` (`origin_place`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='农产品货架与货源表';

-- ----------------------------
-- 4. 产地实拍与溯源检测档案表 (tb_origin_archive)
-- ----------------------------
DROP TABLE IF EXISTS `tb_origin_archive`;
CREATE TABLE `tb_origin_archive` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` BIGINT NOT NULL COMMENT '关联商品ID',
  `soil_quality` VARCHAR(100) DEFAULT '富硒弱酸性红壤，有机质含量大于3.2%' COMMENT '土壤环境说明',
  `water_environment` VARCHAR(100) DEFAULT '海拔2600米纯净高山融雪水源直灌' COMMENT '水质灌溉说明',
  `field_photos` TEXT COMMENT '田间采摘实拍照片列表JSON',
  `pesticide_test_report` VARCHAR(255) DEFAULT 'https://images.unsplash.com/photo-1584727638096-042c45049ebe?w=800' COMMENT '农残检测合格报告',
  `test_result` VARCHAR(50) DEFAULT '未检出有机磷等农药残留，符合绿色食品标准' COMMENT '检测结论',
  `origin_story` TEXT COMMENT '种植故事与基地风貌',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产地实拍与溯源检测档案表';

-- ----------------------------
-- 5. 直播间表 (tb_live_room)
-- ----------------------------
DROP TABLE IF EXISTS `tb_live_room`;
CREATE TABLE `tb_live_room` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '直播间ID',
  `anchor_id` BIGINT NOT NULL COMMENT '主播用户ID',
  `title` VARCHAR(100) NOT NULL COMMENT '直播间标题',
  `cover_url` VARCHAR(255) NOT NULL COMMENT '直播封面图',
  `stream_url` VARCHAR(255) DEFAULT 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4' COMMENT '视频流地址',
  `current_product_id` BIGINT DEFAULT NULL COMMENT '当前正在讲解的商品ID',
  `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 0-未开播, 1-直播中, 2-已下播',
  `online_count` INT NOT NULL DEFAULT 2860 COMMENT '在线观看人数',
  `total_gmv` DECIMAL(12,2) NOT NULL DEFAULT 58420.00 COMMENT '累计带货GMV(元)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='直播间表';

-- ----------------------------
-- 6. 大宗询价意向单表 (tb_wholesale_inquiry)
-- ----------------------------
DROP TABLE IF EXISTS `tb_wholesale_inquiry`;
CREATE TABLE `tb_wholesale_inquiry` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `inquiry_no` VARCHAR(32) NOT NULL COMMENT '询价单流水号',
  `buyer_id` BIGINT NOT NULL COMMENT '采购批发商用户ID',
  `coop_id` BIGINT NOT NULL COMMENT '供货合作社ID',
  `product_id` BIGINT NOT NULL COMMENT '意向农产品ID',
  `demand_quantity` DECIMAL(12,2) NOT NULL COMMENT '意向采购数量(如20.0吨)',
  `unit` VARCHAR(10) NOT NULL DEFAULT '吨' COMMENT '单位',
  `expected_unit_price` DECIMAL(10,2) NOT NULL COMMENT '批发商期望单价(元/斤)',
  `coop_quote_price` DECIMAL(10,2) DEFAULT NULL COMMENT '合作社核定/还价单价',
  `delivery_target_date` DATE NOT NULL COMMENT '期望交货日期',
  `packaging_demand` VARCHAR(255) DEFAULT '需冷链恒温专车直达，5层瓦楞果筐加厚包装' COMMENT '包装冷链要求',
  `status` INT NOT NULL DEFAULT 0 COMMENT '状态: 0-待合作社确认, 1-已达成意向, 2-已驳回, 3-已生成电子合同',
  `contract_no` VARCHAR(50) DEFAULT NULL COMMENT '生成的电子合同编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inquiry_no` (`inquiry_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='大宗询价意向单表';

-- ----------------------------
-- 7. 交易订单表 (tb_order)
-- ----------------------------
DROP TABLE IF EXISTS `tb_order`;
CREATE TABLE `tb_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单主键ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '电商订单流水号',
  `buyer_id` BIGINT NOT NULL COMMENT '下单买家ID',
  `order_type` INT NOT NULL DEFAULT 1 COMMENT '类型: 1-C端零售直采, 2-直播间抢购, 3-B端大宗订单',
  `total_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '总金额',
  `pay_status` INT NOT NULL DEFAULT 1 COMMENT '支付状态: 0-未支付, 1-已支付',
  `order_status` INT NOT NULL DEFAULT 1 COMMENT '履约状态: 0-待付款, 1-待发货, 2-已发货, 3-已完成, 4-售后退款中',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收件人',
  `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收件电话',
  `receiver_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `tracking_no` VARCHAR(50) DEFAULT 'SF10928392183' COMMENT '快递单号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易订单主表';

-- ----------------------------
-- 8. 农产品溯源码批次追踪表 (tb_trace_record)
-- ----------------------------
DROP TABLE IF EXISTS `tb_trace_record`;
CREATE TABLE `tb_trace_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `trace_code` VARCHAR(64) NOT NULL COMMENT '一物一码追溯码',
  `product_id` BIGINT NOT NULL COMMENT '关联商品ID',
  `node_name` VARCHAR(50) NOT NULL COMMENT '追溯节点名',
  `node_time` VARCHAR(30) NOT NULL COMMENT '节点发生时间',
  `operator` VARCHAR(50) NOT NULL COMMENT '操作人/记录人',
  `description` TEXT NOT NULL COMMENT '环境监测数据与操作细节',
  PRIMARY KEY (`id`),
  KEY `idx_trace_code` (`trace_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='农产品溯源码批次追踪表';

-- ========================================================
-- 初始高保真演示数据注入
-- ========================================================

-- 1. 用户数据 (5类用户，密码均为 123456 的BCrypt加密串)
INSERT INTO `tb_user` (`id`, `username`, `password`, `real_name`, `phone`, `role_type`, `avatar`, `audit_status`) VALUES
(1, 'admin', '$2a$10$wT3WfUu9m0pLzD4rK9YhveG9B9D8R0zWq3yK9qM.8W6U0.Z4fXW0u', '系统超级管理员', '18800000000', 'ADMIN', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120', 1),
(2, 'coop_liangshan', '$2a$10$wT3WfUu9m0pLzD4rK9YhveG9B9D8R0zWq3yK9qM.8W6U0.Z4fXW0u', '凉山盐源高山生态果蔬合作社', '13988880001', 'COOP', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=120', 1),
(3, 'coop_shouguang', '$2a$10$wT3WfUu9m0pLzD4rK9YhveG9B9D8R0zWq3yK9qM.8W6U0.Z4fXW0u', '寿光绿源无公害蔬菜种植合作社', '13988880002', 'COOP', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=120', 1),
(4, 'anchor_xiaoli', '$2a$10$wT3WfUu9m0pLzD4rK9YhveG9B9D8R0zWq3yK9qM.8W6U0.Z4fXW0u', '川农小李·产地实景带货', '13766660001', 'ANCHOR', 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=120', 1),
(5, 'buyer_jingdong', '$2a$10$wT3WfUu9m0pLzD4rK9YhveG9B9D8R0zWq3yK9qM.8W6U0.Z4fXW0u', '鼎鲜优选全国连锁生鲜集采中心', '13599990001', 'WHOLESALE', 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=120', 1),
(6, 'consumer_user', '$2a$10$wT3WfUu9m0pLzD4rK9YhveG9B9D8R0zWq3yK9qM.8W6U0.Z4fXW0u', '淘宝资深吃货买家', '18612345678', 'CONSUMER', 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=120', 1);

-- 2. 合作社资质档案
INSERT INTO `tb_cooperative` (`id`, `user_id`, `coop_name`, `province`, `city`, `district`, `detail_address`, `longitude`, `latitude`, `base_area`, `license_url`, `description`) VALUES
(1, 2, '凉山盐源高山生态果蔬合作社', '四川省', '凉山彝族自治州', '盐源县', '卫城镇高山生态种植基地8号', 101.5098, 27.4211, 2500.0, 'https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=600', '拥有高海拔充足光照与纯净高山融雪灌溉，专注于高原丑苹果、有机核桃的绿色种植。'),
(2, 3, '寿光绿源无公害蔬菜种植合作社', '山东省', '潍坊市', '寿光市', '孙家集街道现代农业示范园区B区', 118.7401, 36.8812, 1800.0, 'https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=600', '全国著名的蔬菜之乡核心示范基地，配备数字化日光大棚与全自动温控滴灌水肥一体化设施。');

-- 3. 核心农产品数据
INSERT INTO `tb_product` (`id`, `coop_id`, `title`, `category`, `origin_place`, `retail_price`, `wholesale_price`, `min_wholesale_amount`, `stock`, `unit`, `yield_cycle`, `total_yield`, `cover_image`, `is_presale`, `status`) VALUES
(1, 1, '【产地直采】四川凉山盐源丑苹果 高原红富士 脆甜多汁带糖心 净重5斤装', '水果', '四川凉山', 39.90, 2.40, 1000, 8500, '斤', '每年10月至次年3月', '年产800吨', 'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=600', 0, 1),
(2, 2, '【有机认证】山东寿光水果自然熟铁皮西红柿 沙瓤爆汁生吃小番茄 4斤整箱', '蔬菜', '山东寿光', 29.80, 1.80, 500, 12000, '斤', '全年分批上市', '年产1200吨', 'https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=600', 0, 1),
(3, 1, '【田间预售】正宗赣南寻乌高山脐橙 树上现摘新鲜纯甜 12月集中直发 10斤装', '水果', '江西赣州', 58.00, 3.20, 2000, 15000, '斤', '每年11月至次年1月', '年产600吨', 'https://images.unsplash.com/photo-1611080626919-7cf5a9dbab5b?w=600', 1, 1),
(4, 2, '【地理标志】黑龙江五常稻花香2号核心产区新米 现磨直供 真空包装 10kg', '粮油', '黑龙江五常', 89.00, 5.50, 1000, 6000, '斤', '每年10月新米上市', '年产1500吨', 'https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600', 0, 1);

-- 4. 产地实拍与溯源档案
INSERT INTO `tb_origin_archive` (`id`, `product_id`, `soil_quality`, `water_environment`, `field_photos`, `pesticide_test_report`, `test_result`, `origin_story`) VALUES
(1, 1, '海拔2600米高山红壤，富含微量元素硒和钾，透气排水良好', '源自青藏高原东南边缘雪山纯净融水，无重金属工业污染', '[\"https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=600\"]', 'https://images.unsplash.com/photo-1584727638096-042c45049ebe?w=800', '经过SGS与农业农村部质检中心320项农残专项筛查，农残0检出', '盐源县拥有得天独厚的紫外线照射与近15度的昼夜温差，使苹果自然凝结半透明蜜心。'),
(2, 2, '寿光特级草炭土改良土质，施用充分发酵黄豆有机发酵肥料', '全封闭地下50米深井水源，经多介质石英砂与反渗透软化过滤', '[\"https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=600\"]', 'https://images.unsplash.com/photo-1584727638096-042c45049ebe?w=800', '不使用任何化学膨大剂与催熟剂，自然成熟，达到无公害绿色标准', '每一棵番茄苗均采用熊蜂天然授粉，找回小时候自然熟浓郁番茄风味。');

-- 5. 直播间数据
INSERT INTO `tb_live_room` (`id`, `anchor_id`, `title`, `cover_url`, `current_product_id`, `status`, `online_count`, `total_gmv`) VALUES
(1, 4, '【实地采摘】川农小李带你走进四川盐源 高原丑苹果现摘现尝 现场质检！', 'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=600', 1, 1, 3520, 68900.00);

-- 6. 大宗询价意向单演示
INSERT INTO `tb_wholesale_inquiry` (`id`, `inquiry_no`, `buyer_id`, `coop_id`, `product_id`, `demand_quantity`, `unit`, `expected_unit_price`, `coop_quote_price`, `delivery_target_date`, `status`, `contract_no`) VALUES
(1, 'INQ202611130001', 5, 1, 1, 30.00, '吨', 2.20, 2.35, '2026-12-15', 3, 'AGRI-CONTRACT-202611-001'),
(2, 'INQ202611130002', 5, 2, 2, 15.00, '吨', 1.60, NULL, '2026-11-28', 0, NULL);

-- 7. 演示订单数据
INSERT INTO `tb_order` (`id`, `order_no`, `buyer_id`, `order_type`, `total_amount`, `pay_status`, `order_status`, `receiver_name`, `receiver_phone`, `receiver_address`) VALUES
(1, 'TB202611138891001', 6, 2, 39.90, 1, 2, '张小敏', '18612345678', '北京市朝阳区望京科技大厦B座1201室'),
(2, 'TB202611138891002', 6, 1, 58.00, 1, 1, '张小敏', '18612345678', '北京市朝阳区望京科技大厦B座1201室');

-- 8. 溯源追踪档案
INSERT INTO `tb_trace_record` (`id`, `trace_code`, `product_id`, `node_name`, `node_time`, `operator`, `description`) VALUES
(1, 'AGRI20261101001', 1, '田间修剪与疏果', '2026-05-10 09:30:00', '果农李大伯', '严格执行人工一梢留单果疏果工艺，确保光照通透与单果养分供给。'),
(2, 'AGRI20261101001', 1, '套袋与有机施肥', '2026-07-15 14:20:00', '技术员张工', '采用双层透气果袋物理防虫害，施用腐熟油枯有机肥，拒绝化学膨大农药。'),
(3, 'AGRI20261101001', 1, '人工成熟采摘', '2026-11-05 08:00:00', '盐源采摘一组', '糖度测定均值达15.6度，色泽覆盖率85%以上方准入箱采摘。'),
(4, 'AGRI20261101001', 1, '第三方农残抽检', '2026-11-07 16:00:00', '中检质检专员', '随机抽样100箱检测，重金属及农残指标完全符合绿色食品标准。'),
(5, 'AGRI20261101001', 1, '产地冷链顺丰直发', '2026-11-13 10:15:00', '顺丰产地仓', '专线恒温发货，直达消费者手中，运单号：SF10928392183。');

SET FOREIGN_KEY_CHECKS = 1;
