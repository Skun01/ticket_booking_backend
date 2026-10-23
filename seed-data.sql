-- =====================================================================
-- Sport Slot: seed danh mục, sân, sự kiện và hạng vé (MySQL 8+).
-- Chạy sau khi Liquibase đã tạo schema. Chỉ dùng cho development/demo.
-- MySQL Workbench: mở file và Execute toàn bộ trên cùng một kết nối.
-- CLI: mysql -u root -p --default-character-set=utf8mb4 sport_slot_db < seed-data.sql
--
-- KHÔNG tạo đơn hàng, order_items, vé đã mua, thanh toán, mail hay log.
-- KHÔNG tạo admin/customer; bạn tự đăng ký và đặt hàng qua ứng dụng.
-- Organizer là dữ liệu bắt buộc để liên kết events.organizer_id.
-- Organizer mới: organizer1@sportslot.test ... organizer30@sportslot.test
-- Mật khẩu: Password123! (BCrypt đã kiểm chứng).
--
-- Không xóa/ghi đè dữ liệu đã có. Bỏ qua bản ghi trùng theo các khóa seed
-- bên dưới. Không chạy đồng thời nhiều bản script hoặc khi app đang ghi dữ liệu.
-- Nếu lỗi: dừng thực thi, ROLLBACK trên cùng kết nối rồi kiểm tra nguyên nhân.
-- Script không dọn dữ liệu đã COMMIT từ phiên bản seed cũ.
-- =====================================================================

USE sport_slot_db;
SET NAMES utf8mb4;
SET @OLD_TIME_ZONE = @@SESSION.time_zone;
SET SESSION time_zone = '+00:00';
SET @NOW = UTC_TIMESTAMP(6);
SET @PWD = '$2a$10$A2gaCFH06FlpgAG4Th9IvuDGdlHLoUoe3O3Dh2Aw6PfnZI5RlCapq';

-- Mỗi số lượng phải nằm trong khoảng 1..5000.
SET @N_ORGANIZERS = 30;
SET @N_VENUES = 25;
SET @N_EVENTS = 400;

START TRANSACTION;

DROP TEMPORARY TABLE IF EXISTS seed_nums, seed_categories, seed_org_map, seed_cat_map, seed_venue_map;
CREATE TEMPORARY TABLE seed_nums (n INT PRIMARY KEY);
-- Sinh 1..5000, không cần thay đổi giới hạn đệ quy của MySQL.
INSERT INTO seed_nums (n)
SELECT a.d + b.d * 10 + c.d * 100 + d.d * 1000 + 1
FROM JSON_TABLE('[0,1,2,3,4,5,6,7,8,9]', '$[*]' COLUMNS (d INT PATH '$')) a
CROSS JOIN JSON_TABLE('[0,1,2,3,4,5,6,7,8,9]', '$[*]' COLUMNS (d INT PATH '$')) b
CROSS JOIN JSON_TABLE('[0,1,2,3,4,5,6,7,8,9]', '$[*]' COLUMNS (d INT PATH '$')) c
CROSS JOIN JSON_TABLE('[0,1,2,3,4]', '$[*]' COLUMNS (d INT PATH '$')) d;

-- 1) Organizer: chỉ tạo nếu email chưa tồn tại, không đổi role/password user cũ.
INSERT INTO users (email, password, full_name, role, phone, status, created_at, updated_at)
SELECT CONCAT('organizer', n, '@sportslot.test'), @PWD, CONCAT('Ban tổ chức thể thao ', n),
	'ORGANIZER', CONCAT('091', LPAD(n, 7, '0')), 'ACTIVE', @NOW, @NOW
FROM seed_nums s
WHERE n <= @N_ORGANIZERS
	AND NOT EXISTS (SELECT 1 FROM users u WHERE u.email = CONCAT('organizer', s.n, '@sportslot.test'));

-- 2) Danh mục thể thao.
CREATE TEMPORARY TABLE seed_categories (
	n INT PRIMARY KEY,
	name VARCHAR(100),
	slug VARCHAR(120)
);
INSERT INTO seed_categories VALUES
	(1, 'Bóng đá', 'bong-da'),
	(2, 'Bóng rổ', 'bong-ro'),
	(3, 'Tennis', 'tennis'),
	(4, 'Cầu lông', 'cau-long'),
	(5, 'Chạy bộ', 'chay-bo'),
	(6, 'Bóng chuyền', 'bong-chuyen'),
	(7, 'Bơi lội', 'boi-loi'),
	(8, 'Đạp xe', 'dap-xe'),
	(9, 'Võ thuật', 'vo-thuat'),
	(10, 'Bóng bàn', 'bong-ban');

INSERT INTO categories (name, slug, created_at, updated_at)
SELECT s.name, s.slug, @NOW, @NOW
FROM seed_categories s
WHERE NOT EXISTS (SELECT 1 FROM categories c WHERE c.slug = s.slug);

-- 3) Sân/địa điểm: sức chứa tối thiểu 1000, đủ 750 vé mỗi sự kiện.
INSERT INTO venues (name, address, city, capacity, created_at, updated_at)
SELECT CONCAT('Sân vận động ', n), CONCAT(n, ' Đường Thể Thao, Quận ', (n % 12) + 1),
	ELT(((n - 1) % 5) + 1, 'Hà Nội', 'TP. HCM', 'Đà Nẵng', 'Hải Phòng', 'Cần Thơ'),
	1000 + (n % 10) * 500, @NOW, @NOW
FROM seed_nums s
WHERE n <= @N_VENUES
	AND NOT EXISTS (SELECT 1 FROM venues v WHERE v.name = CONCAT('Sân vận động ', s.n));

-- Map ID thực tế, không giả định AUTO_INCREMENT liền mạch.
CREATE TEMPORARY TABLE seed_org_map (rn INT PRIMARY KEY, id BIGINT);
CREATE TEMPORARY TABLE seed_cat_map (rn INT PRIMARY KEY, id BIGINT, name VARCHAR(100));
CREATE TEMPORARY TABLE seed_venue_map (rn INT PRIMARY KEY, id BIGINT);

INSERT INTO seed_org_map
SELECT ROW_NUMBER() OVER (ORDER BY u.id), u.id
FROM users u
JOIN seed_nums s ON u.email = CONCAT('organizer', s.n, '@sportslot.test')
WHERE s.n <= @N_ORGANIZERS AND u.role = 'ORGANIZER' AND u.status = 'ACTIVE';

INSERT INTO seed_cat_map
SELECT ROW_NUMBER() OVER (ORDER BY s.n), c.id, s.name
FROM categories c JOIN seed_categories s ON s.slug = c.slug;

INSERT INTO seed_venue_map
SELECT ROW_NUMBER() OVER (ORDER BY v.id), v.id
FROM venues v JOIN seed_nums s ON v.name = CONCAT('Sân vận động ', s.n)
WHERE s.n <= @N_VENUES AND v.capacity >= 750;

SET @ORG_CNT = (SELECT COUNT(*) FROM seed_org_map);
SET @CAT_CNT = (SELECT COUNT(*) FROM seed_cat_map);
SET @VENUE_CNT = (SELECT COUNT(*) FROM seed_venue_map);

-- Các số lượng map phải > 0. Nếu bằng 0, không tạo được sự kiện.
SELECT @ORG_CNT AS organizers_available, @CAT_CNT AS categories_available,
	@VENUE_CNT AS venues_available;

-- 4) Sự kiện: tương lai 7..126 ngày, PUBLISHED để xem và đặt ngay.
-- Marker trong description nhận diện dữ liệu seed, tránh tạo trùng khi chạy lại.
INSERT INTO events (organizer_id, category_id, venue_id, title, description, banner_url,
	start_time, end_time, status, created_at, updated_at)
SELECT om.id, cm.id, vm.id,
	CONCAT('Giải ', cm.name, ' mở rộng #', s.n),
	CONCAT('[SPORT_SLOT_CATALOG_SEED:', s.n, '] ',
		'Giải ', cm.name, ' dành cho người yêu thể thao. ',
		'Cùng cổ vũ các vận động viên và trải nghiệm không khí thi đấu sôi động.'),
	NULL,
	@NOW + INTERVAL (7 + (s.n % 120)) DAY,
	@NOW + INTERVAL (7 + (s.n % 120)) DAY + INTERVAL 3 HOUR,
	'PUBLISHED', @NOW, @NOW
FROM seed_nums s
JOIN seed_org_map om ON om.rn = ((s.n - 1) % @ORG_CNT) + 1
JOIN seed_cat_map cm ON cm.rn = ((s.n - 1) % @CAT_CNT) + 1
JOIN seed_venue_map vm ON vm.rn = ((s.n - 1) % @VENUE_CNT) + 1
WHERE s.n <= @N_EVENTS
	AND NOT EXISTS (
		SELECT 1 FROM events e
		WHERE e.description LIKE CONCAT('[SPORT_SLOT_CATALOG_SEED:', s.n, '] %')
	);

-- 5) Hạng vé là cấu hình bán vé, KHÔNG phải vé khách đã mua.
-- Bán từ lúc tạo sự kiện tới trước giờ bắt đầu 1 giờ; số đã bán ban đầu bằng 0.
-- Không sửa sold_quantity hay hạng vé đã có khi chạy lại.
INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order,
	sale_start, sale_end, created_at, updated_at)
SELECT e.id, t.name, t.price, t.quantity, 0, t.max_per_order,
	e.created_at, e.start_time - INTERVAL 1 HOUR, @NOW, @NOW
FROM events e
CROSS JOIN (
	SELECT 'Standard' AS name, 150000.00 AS price, 500 AS quantity, 6 AS max_per_order
	UNION ALL SELECT 'VIP', 350000.00, 200, 4
	UNION ALL SELECT 'VVIP', 700000.00, 50, 2
) t
WHERE e.description LIKE '[SPORT_SLOT_CATALOG_SEED:%'
	AND NOT EXISTS (
		SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = t.name
	);

DROP TEMPORARY TABLE IF EXISTS seed_nums, seed_categories, seed_org_map, seed_cat_map, seed_venue_map;
COMMIT;
SET SESSION time_zone = @OLD_TIME_ZONE;

-- Tổng số bản ghi trong DB (bao gồm dữ liệu có sẵn).
SELECT 'organizers' AS table_name, COUNT(*) AS total FROM users WHERE role = 'ORGANIZER'
UNION ALL SELECT 'categories', COUNT(*) FROM categories
UNION ALL SELECT 'venues', COUNT(*) FROM venues
UNION ALL SELECT 'events', COUNT(*) FROM events
UNION ALL SELECT 'ticket_types', COUNT(*) FROM ticket_types;
