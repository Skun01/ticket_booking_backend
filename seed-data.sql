-- =====================================================================
-- Sport Slot: seed danh mục, địa điểm, sự kiện và hạng vé (MySQL 8+).
-- Dữ liệu gồm 20 sự kiện thực tế được cào trực tiếp từ Ticketbox (ticketbox.vn)
-- bao gồm: Live Concert, Âm nhạc, Sân khấu Kịch, Thể thao Esports, Nghệ thuật,
-- đầy đủ banner URL chính thức từ Ticketbox CDN, địa điểm thực tế,
-- ban tổ chức thực tế và danh sách chi tiết các hạng vé cùng mức giá.
--
-- Chạy sau khi Liquibase đã tạo schema. Chỉ dùng cho development/demo.
-- MySQL Workbench: mở file và Execute toàn bộ trên cùng một kết nối.
-- CLI: mysql -u root -p --default-character-set=utf8mb4 sport_slot_db < seed-data.sql
--
-- An toàn khi chạy lại nhiều lần (idempotent): bỏ qua nếu dữ liệu đã tồn tại.
-- Mật khẩu mặc định của tất cả Organizer: Password123!
-- =====================================================================

USE sport_slot_db;
SET NAMES utf8mb4;
SET @OLD_TIME_ZONE = @@SESSION.time_zone;
SET SESSION time_zone = '+00:00';
SET @NOW = UTC_TIMESTAMP(6);
SET @PWD = '$2a$10$A2gaCFH06FlpgAG4Th9IvuDGdlHLoUoe3O3Dh2Aw6PfnZI5RlCapq';

START TRANSACTION;

DROP TEMPORARY TABLE IF EXISTS temp_seed_categories, temp_seed_organizers, temp_seed_venues;

-- =====================================================================
-- 1) Danh mục sự kiện (Categories)
-- =====================================================================
CREATE TEMPORARY TABLE temp_seed_categories (
    name VARCHAR(100),
    slug VARCHAR(120) PRIMARY KEY
);
INSERT INTO temp_seed_categories (name, slug) VALUES
    ('Âm nhạc', 'am-nhac'),
    ('Sân khấu & Nghệ thuật', 'san-khau-nghe-thuat'),
    ('Thể thao', 'the-thao'),
    ('Tham quan & Trải nghiệm', 'tham-quan-trai-nghiem'),
    ('Lễ hội & Sự kiện', 'le-hoi-su-kien'),
    ('Hội thảo & Workshop', 'hoi-thao-workshop'),
    ('Bóng đá', 'bong-da'),
    ('Bóng rổ', 'bong-ro'),
    ('Tennis', 'tennis'),
    ('Cầu lông', 'cau-long'),
    ('Chạy bộ', 'chay-bo'),
    ('Bóng chuyền', 'bong-chuyen'),
    ('Bơi lội', 'boi-loi'),
    ('Đạp xe', 'dap-xe'),
    ('Võ thuật', 'vo-thuat'),
    ('Bóng bàn', 'bong-ban');

INSERT INTO categories (name, slug, created_at, updated_at)
SELECT s.name, s.slug, @NOW, @NOW
FROM temp_seed_categories s
WHERE NOT EXISTS (SELECT 1 FROM categories c WHERE c.slug = s.slug);

-- =====================================================================
-- 2) Ban tổ chức sự kiện thực tế từ Ticketbox (Users role ORGANIZER)
-- =====================================================================
CREATE TEMPORARY TABLE temp_seed_organizers (
    email VARCHAR(100) PRIMARY KEY,
    full_name VARCHAR(100),
    phone VARCHAR(20)
);
INSERT INTO temp_seed_organizers (email, full_name, phone) VALUES
    ('organizer1@sportslot.test', 'SS LABEL', '0910000001'),
    ('organizer2@sportslot.test', 'VieChannel - DatVietVAC Group Holdings & Nghệ Sỹ HIEUTHUHAI', '0910000002'),
    ('organizer3@sportslot.test', 'Những Thành Phố Mơ Màng (NTPMM)', '0910000003'),
    ('organizer4@sportslot.test', 'Những Thành Phố Mơ Màng (NTPMM)', '0910000004'),
    ('organizer5@sportslot.test', 'QUỐC THIÊN', '0910000005'),
    ('organizer6@sportslot.test', 'SIROCON Entertainment', '0910000006'),
    ('organizer7@sportslot.test', 'Dinh Luu Media', '0910000007'),
    ('organizer8@sportslot.test', 'CÔNG TY CỔ PHẦN TRUYỀN THÔNG SHINING MEDIA', '0910000008'),
    ('organizer9@sportslot.test', 'Cat&Mouse Live Music', '0910000009'),
    ('organizer10@sportslot.test', 'Holiday Inn & Suites Saigon Airport', '0910000010'),
    ('organizer11@sportslot.test', 'VPSTUDIO', '0910000011'),
    ('organizer12@sportslot.test', 'CÔNG TY CỔ PHẦN GIẢI TRÍ VÀ THỂ THAO ĐIỆN TỬ VIỆT NAM', '0910000012'),
    ('organizer13@sportslot.test', 'SPO (Saigon Philharmonic Orchestra)', '0910000013'),
    ('organizer14@sportslot.test', 'SPO (Saigon Philharmonic Orchestra)', '0910000014'),
    ('organizer15@sportslot.test', 'Công Ty TNHH Sân Khấu - Nghệ Thuật Thái Dương', '0910000015'),
    ('organizer16@sportslot.test', 'Công Ty TNHH Sân Khấu - Nghệ Thuật Thái Dương', '0910000016'),
    ('organizer17@sportslot.test', 'SÂN KHẤU NGHỆ THUẬT TRƯƠNG HÙNG MINH', '0910000017'),
    ('organizer18@sportslot.test', 'Sân Khấu Ban Mai', '0910000018'),
    ('organizer19@sportslot.test', 'TRUNG TÂM HOẠT ĐỘNG VĂN HÓA KHOA HỌC VĂN MIẾU – QUỐC TỬ GIÁM', '0910000019'),
    ('organizer20@sportslot.test', 'Mystic Night Show', '0910000020');

INSERT INTO users (email, password, full_name, role, phone, status, created_at, updated_at)
SELECT o.email, @PWD, o.full_name, 'ORGANIZER', o.phone, 'ACTIVE', @NOW, @NOW
FROM temp_seed_organizers o
WHERE NOT EXISTS (SELECT 1 FROM users u WHERE u.email = o.email);

-- =====================================================================
-- 3) Địa điểm tổ chức sự kiện thực tế (Venues)
-- =====================================================================
CREATE TEMPORARY TABLE temp_seed_venues (
    name VARCHAR(150) PRIMARY KEY,
    address VARCHAR(255),
    city VARCHAR(100),
    capacity INT
);
INSERT INTO temp_seed_venues (name, address, city, capacity) VALUES
    ('Trung tâm Triển lãm Việt Nam (VEC)', 'Duong Cau Tu Lien, Dong Anh Commune, Ha Noi City', 'Hà Nội', 5000),
    ('Trung tâm triển lãm Việt Nam (VEC) - SÂN BẮC', 'Duong Cau Tu Lien, Dong Anh Commune, Ha Noi City', 'Hà Nội', 5000),
    ('Trung Tâm Hội Chợ & Triển Lãm Sài Gòn (SECC)', 'So 799 Nguyen Van Linh, Tan My Ward, Ho Chi Minh City', 'TP. HCM', 5000),
    ('Trung tâm triển lãm Việt Nam (VEC) - SUNSET ZONE', 'Duong Cau Tu Lien, Dong Anh Commune, Ha Noi City', 'Hà Nội', 5000),
    ('Trung Tâm Hội Nghị Quốc Gia', 'So 57 Duong Pham Hung, Tu Liem Ward, Ha Noi City', 'Hà Nội', 3500),
    ('Trung Tâm Nghệ Thuật Âu cơ', '8 Huynh Thuc Khang, Giang Vo Ward, Ha Noi City', 'Hà Nội', 1200),
    ('Nhà hát Hoa Phượng', 'So 1 duong Tran Kien, khu do thi Bac Song Cam, Thuy Nguyen Ward, Hai Phong City', 'Hải Phòng', 1200),
    ('Cat&Mouse Live Music', '37B Pham Ngoc Thach, Xuan Hoa Ward, Ho Chi Minh City', 'TP. HCM', 1200),
    ('Khách sạn Holiday Inn & Suites Saigon Airport', '18E Cong Hoa , Tan Son Nhat Ward, Ho Chi Minh City', 'TP. HCM', 1200),
    ('VPS Arena', '476 Duong Quang Ham, An Nhon Ward, Ho Chi Minh City', 'TP. HCM', 1200),
    ('Cung Văn Hóa Hữu Nghị Việt Xô', '91 Tran Hung Dao, Cua Nam Ward, Ha Noi City', 'Hà Nội', 1200),
    ('Nhà Hát Hòa Bình', '240 Duong 3 Thang 2, Hoa Hung Ward, Ho Chi Minh City', 'TP. HCM', 1200),
    ('Sân Khấu Nhà Thiếu Nhi TP.HCM', 'So 04 Tu Xuong, Xuan Hoa Ward, Ho Chi Minh City', 'TP. HCM', 1200),
    ('Nhà Hát Kịch IDECAF', 'So 28 Le Thanh Ton., Ben Nghe Ward, 1 District, Ho Chi Minh City', 'TP. HCM', 1200),
    ('SÂN KHẤU NGHỆ THUẬT TRƯƠNG HÙNG MINH', '22 VINH VIEN, Ho Chi Minh City', 'TP. HCM', 1200),
    ('Sân khấu Ban Mai - Nhà Thiếu Nhi Thành Phố Hồ Chí Minh', '36 Le Quy Don, Xuan Hoa Ward, Ho Chi Minh City', 'TP. HCM', 1200),
    ('Văn Miếu - Quốc Tử Giám', '58 Quoc Tu Giam, Van Mieu - Quoc Tu Giam Ward, Ha Noi City', 'Hà Nội', 1200),
    ('Mystic Night Show', '54 Ky Con, Ben Thanh, Ho Chi Minh, Ben Thanh Ward, Ho Chi Minh City', 'TP. HCM', 1200);

INSERT INTO venues (name, address, city, capacity, created_at, updated_at)
SELECT v.name, v.address, v.city, v.capacity, @NOW, @NOW
FROM temp_seed_venues v
WHERE NOT EXISTS (SELECT 1 FROM venues existing WHERE existing.name = v.name);

-- =====================================================================
-- 4) 20 Sự kiện Ticketbox thực tế (Events - PUBLISHED)
-- Thời gian bắt đầu được lập lịch tương lai để sẵn sàng trải nghiệm đặt vé
-- =====================================================================
-- [1/20] KOSMIK LIVE CONCERT 2026 IN HANOI: HOMECOMING (Ticketbox #26701)
INSERT INTO events (organizer_id, category_id, venue_id, title, description, banner_url, sales_start_time, start_time, end_time, status, created_at, updated_at)
SELECT
    (SELECT id FROM users WHERE email = 'organizer1@sportslot.test' LIMIT 1),
    (SELECT id FROM categories WHERE slug = 'am-nhac' LIMIT 1),
    (SELECT id FROM venues WHERE name = 'Trung tâm Triển lãm Việt Nam (VEC)' LIMIT 1),
    'KOSMIK LIVE CONCERT 2026 IN HANOI: HOMECOMING',
    'KOSMIK LIVE CONCERT 2026 IN HANOI: HOMECOMING

TIẾNG VIỆT

ĐIỀU KHOẢN VÀ ĐIỀU KIỆN VÉ

• Mỗi đơn hàng được mua tối đa 04 (bốn) vé trong đợt Mở bán sớm (Presale) và tối đa 10 (mười) vé trong đợt Mở bán chính thức (General Sale). Riêng hạng vé VIP LOUNGE (NGƯỜI VIỆT) bán theo bàn tiêu chuẩn 06 (sáu) vé/bàn, áp dụng tối đa 01 (một) bàn/đơn hàng trong cả 02 đợt mở bán. 

• Mỗi mã vé chỉ dành cho 01 (một) người và chỉ được sử dụng 01 (một) lần. Khán giả có trách nhiệm tự bảo mật mã vé điện tử (e-ticket) của mình. Trường hợp có nhiều hơn 01 (một) người check-in cùng một mã vé, Ban Tổ chức (BTC) chỉ cho phép người check-in đầu tiên vào sự kiện và không giải quyết các khiếu nại phát sinh. Khán giả đã check-in và rời khỏi khu vực sự kiện sẽ không được quay lại.

• Sự kiện chỉ dành cho khán giả từ 15 tuổi trở lên. Khán giả từ 15 đến 17 tuổi phải mang giấy tờ tùy thân có hình ảnh (căn cước công dân, hộ chiếu,...) và bắt buộc phải có người giám hộ từ 21 tuổi trở lên đi cùng trong suốt thời gian diễn ra sự kiện. Người giám hộ phải có vé cùng khu vực (zone) với khán giả được giám hộ. Mỗi người giám hộ được đi cùng tối đa 02 (hai) khán giả.

• Ban Tổ Chức có quyền yêu cầu cung cấp bổ sung tài liệu hoặc thông tin cần thiết để xác minh điều kiện tham dự. Trường hợp BTC không thể xác minh hoặc phát hiện thông tin không chính xác, Ban Tổ Chức có quyền từ chối cho Người Tham Dự tham gia sự kiện và không hoàn trả lại tiền vé.

• Hạng vé VIP LOUNGE (NGƯỜI VIỆT) và SVIP (SPACESPEAKERS) chỉ dành cho khán giả từ đủ 18 tuổi trở lên. Khán giả chưa đủ 18 tuổi sẽ không được vào cửa, kể cả khi có vé hoặc có người lớn đi cùng.

• Khán giả vui lòng đứng/ngồi đúng khu vực và đúng số ghế (nếu có) được ghi trên vé.

• Vé đã mua KHÔNG được hoàn tiền, đổi hoặc chỉnh sửa trong bất kỳ trường hợp nào. Người mua vé KHÔNG được bán lại, trao đổi hoặc chuyển nhượng vé cho người khác.

• Khán giả vui lòng cảnh giác với các dịch vụ bán vé không rõ nguồn gốc và các bên trung gian. BTC không chịu trách nhiệm đối với vé mua qua các kênh không chính thức.

• Nghiêm cấm sử dụng hoặc phân phối vé cho mục đích tiếp thị và/hoặc quảng cáo khi chưa có sự cho phép của BTC. BTC có quyền thu hồi vé và không hoàn tiền.

• Người sở hữu vé phải xuất trình vé hợp lệ khi được yêu cầu kiểm tra vào bất kỳ thời điểm nào trong suốt thời gian diễn ra sự kiện.

• Vì lý do an toàn tại nơi đông người, BTC khuyến cáo phụ nữ mang thai không nên tham dự sự kiện.

• Bằng việc mua vé, Người mua vé xác nhận đã đọc, hiểu và đồng ý với toàn bộ Điều khoản và Điều kiện của BTC và Ticketbox.

• Danh mục vật dụng được phép và không được phép mang vào sự kiện, cùng các giới hạn áp dụng (nếu có), sẽ được SpaceSpeakers (SS) thông báo riêng trước ngày diễn ra sự kiện. Khán giả vui lòng theo dõi thông báo chính thức và tuân thủ nội quy tham dự do BTC công bố.

• Ghi hình và sử dụng hình ảnh:
Sự kiện có hoạt động ghi hình, chụp ảnh và thu âm. Bằng việc tham dự, khán giả đồng ý, trong phạm vi pháp luật cho phép, để BTC và các đơn vị được BTC ủy quyền hoặc hợp tác ghi lại và sử dụng hình ảnh, giọng nói của mình trên mọi phương tiện, nền tảng nhằm sản xuất, quảng bá và khai thác sự kiện cùng các nội dung liên quan, bao gồm cả mục đích thương mại. Việc tham dự hoặc mua vé không làm phát sinh quyền yêu cầu thù lao, phí bản quyền hay bất kỳ lợi ích kinh tế nào khác; khán giả và Người mua vé đồng ý không khiếu nại hoặc khởi kiện BTC và các đơn vị liên quan về việc sử dụng này.

• Trong mọi trường hợp, quyết định của BTC là quyết định cuối cùng. BTC có quyền thay đổi bất kỳ Điều khoản và Điều kiện nào của sự kiện mà không cần thông báo trước.

ĐIỀU KHOẢN & ĐIỀU KIỆN QUYỀN LỢI KÈM VÉ

• Các quyền lợi đi kèm vé được áp dụng theo hạng vé đã mua và không được tách rời, chuyển nhượng hoặc mua bán dưới bất kỳ hình thức nào.

• Đối với các quyền lợi dưới dạng vật phẩm, người tham dự sẽ nhận quà tại khu vực/cổng check-in tại sự kiện. Vui lòng kiểm tra đầy đủ vật phẩm ngay tại khu vực nhận quà trước khi rời đi. Ban Tổ Chức (BTC) kh

... (Vui lòng xem chi tiết quy định tại sự kiện)',
    'https://salt.tkbcdn.com/ts/ds/72/a1/4f/6faa7e2fd828dd9d78a4d3be80323d7e.png',
    @NOW - INTERVAL 1 DAY,
    @NOW + INTERVAL 15 DAY + INTERVAL 11 HOUR,
    @NOW + INTERVAL 15 DAY + INTERVAL 15 HOUR,
    'PUBLISHED', @NOW, @NOW
WHERE NOT EXISTS (
    SELECT 1 FROM events e WHERE e.title = 'KOSMIK LIVE CONCERT 2026 IN HANOI: HOMECOMING'
);

-- [2/20] MẮT NHẮM MẮT MỞ CONCERT - DAY 3 (HÀ NỘI) (Ticketbox #26657)
INSERT INTO events (organizer_id, category_id, venue_id, title, description, banner_url, sales_start_time, start_time, end_time, status, created_at, updated_at)
SELECT
    (SELECT id FROM users WHERE email = 'organizer2@sportslot.test' LIMIT 1),
    (SELECT id FROM categories WHERE slug = 'am-nhac' LIMIT 1),
    (SELECT id FROM venues WHERE name = 'Trung tâm triển lãm Việt Nam (VEC) - SÂN BẮC' LIMIT 1),
    'MẮT NHẮM MẮT MỞ CONCERT - DAY 3 (HÀ NỘI)',
    'MẮT NHẮM MẮT MỞ CONCERT - DAY 3 (HÀ NỘI)

Ngày 01.11.2026 tại Trung tâm triển lãm Việt Nam (VEC)

I. ĐIỀU KIỆN VÀ ĐIỀU KHOẢN MUA VÉ: 

● Khi mua hoặc nhận vé (bao gồm vé được tặng, được mua lại theo quy định của Ticketbox và/hoặc BTC), người mua/người nhận vé được xem là đã đọc, hiểu và đồng ý với Điều Kiện và Điều Khoản của BTC và Quy Định Tham Gia Chương Trình được công bố tại ticketbox.vn.

● Trường hợp người mua/người nhận vé không đồng thời là Người Tham Dự, người mua/người nhận vé phải đảm bảo Người Tham Dự đã đọc, hiểu và đồng ý với các quy định nêu trên trước khi tham dự Chương trình.

● Ticketbox là đơn vị phân phối vé độc quyền của sự kiện:  

Mắt Nhắm Mắt Mở Concert - Day 3 (Hà Nội), diễn ra vào ngày 01.11.2026 tại Sân Bắc, Trung tâm Triển lãm Việt Nam (VEC), Ticketbox chịu trách nhiệm giải quyết các vấn đề phát sinh liên quan đến vé mua và vé tặng thông qua hệ thống Ticketbox (bao gồm tính năng “Tặng vé” và “Resale” trên ứng dụng). Ticketbox không chịu trách nhiệm và có quyền từ chối giải quyết các tranh chấp liên quan đến việc trao tặng, chuyển nhượng, mua bán lại giữa các bên ngoài hệ thống Ticketbox. 

● Một (1) Mã Vé có giá trị sử dụng cho một (1) người và một (1) lần duy nhất. 

● Quy định về độ tuổi: Sự kiện dành cho khán giả từ 14 tuổi (tất cả hạng vé).  

- Người Tham Dự từ 14 tuổi đến dưới 18 tuổi phải mang giấy tờ tùy thân có hình ảnh (căn cước công dân, hộ chiếu,...). Ban Tổ Chức có quyền từ chối check-in đối với các trường hợp không đáp ứng yêu cầu về độ tuổi.    

- Ban Tổ Chức có quyền yêu cầu cung cấp bổ sung tài liệu hoặc thông tin cần thiết để xác minh điều kiện tham dự. Trường hợp BTC không thể xác minh hoặc phát hiện thông tin không chính xác, Ban Tổ Chức có quyền từ chối cho Người Tham Dự tham gia sự kiện và không hoàn trả lại tiền vé.

● Vé đã mua không được đổi, trả, nâng cấp, hủy hay hoàn tiền, kể cả khi Người Mua thay đổi kế hoạch hoặc Chương trình dời ngày. BTC chỉ hoàn tiền vé trong trường hợp Chương trình bị hủy.  

● BTC khuyến nghị phụ nữ mang thai, người khuyết tật và người có vấn đề sức khỏe chọn hạng vé khu vực ngồi. Người khuyết tật cần hỗ trợ vui lòng liên hệ BTC qua Email: fanservices@concert.viechannel.vn trước 03 ngày diễn ra chương trình để được bố trí lối đi ưu tiên và nhân viên hỗ trợ tại sự kiện. Người Tham Dự tự cân nhắc và chịu trách nhiệm về sức khỏe khi tham gia Chương Trình.   

● Ban Tổ Chức có quyền điều chỉnh, bổ sung nội dung Quy định tham gia chương trình, Điều Kiện và Điều Khoản nếu xét thấy cần thiết, đảm bảo không trái với nội dung mà Người Tham Dự đã đọc và đồng ý. Khi mua vé/nhận vé tham dự Chương trình Người Tham Dự được hiểu là đã đọc, hiểu và đồng ý với Quy định tham gia chương trình, các Điều Kiện và Điều Khoản cũng như bất kỳ thay đổi nào  sau đó. Trường hợp có bất kỳ thay đổi nào đối với Chương trình, Ban Tổ Chức sẽ cung cấp thông tin tại website ticketbox.vn và trên các phương tiện truyền thông của BTC.

● Người Tham Dự có trách nhiệm theo dõi thông báo chính thức từ Ban Tổ Chức/Ticketbox và kiểm tra lại thông tin vé (ngày, giờ, địa điểm, giá) trong trường hợp có thay đổi trước khi sự kiện diễn ra. 

● Vé chỉ được sử dụng để tham gia Chương trình. Không mua vé cho mục đích kinh doanh, thu gom số lượng lớn nhằm mục đích trục lợi trái với quy định của BTC và Ticketbox, và không sử dụng vé cho các hoạt động khuyến mãi của hàng hóa, dịch vụ của người mua. Trong trường hợp vi phạm, BTC có quyền thu hồi vé đã mua và không hoàn lại tiền, không cho phép tham dự chương trình bằng những vé này và yêu cầu bồi thường thiệt hại. Quy định này không áp dụng đối với các tổ chức, cá nhân đã đạt được thỏa thuận bằng văn bản với Công ty cổ phần Vie Channel về việc sử dụng vé để khuyến mãi. 

● Trong mọi trường hợp, quyết định của BTC là quyết định cuối cùng. 

II. CHÍNH SÁCH XUẤT HÓA ĐƠN CHO KHÁCH HÀNG MUA VÉ: 

● Khách hàng có nhu cầu lấy hóa đơn giá trị gia tăng (VAT) cho giao dịch mua vé có trách nhiệm cung cấp đầy đủ, chính xác và hợp lệ toàn bộ thông tin xuất hóa đơn

... (Vui lòng xem chi tiết quy định tại sự kiện)',
    'https://salt.tkbcdn.com/ts/ds/9a/7c/6e/60a6e4610ee4810a3227951df5a27f02.png',
    @NOW - INTERVAL 1 DAY,
    @NOW + INTERVAL 7 DAY + INTERVAL 11 HOUR,
    @NOW + INTERVAL 7 DAY + INTERVAL 15 HOUR,
    'PUBLISHED', @NOW, @NOW
WHERE NOT EXISTS (
    SELECT 1 FROM events e WHERE e.title = 'MẮT NHẮM MẮT MỞ CONCERT - DAY 3 (HÀ NỘI)'
);

-- [3/20] [TP.HCM] Những Thành Phố Mơ Màng Year End 2026 (Ticketbox #26646)
INSERT INTO events (organizer_id, category_id, venue_id, title, description, banner_url, sales_start_time, start_time, end_time, status, created_at, updated_at)
SELECT
    (SELECT id FROM users WHERE email = 'organizer3@sportslot.test' LIMIT 1),
    (SELECT id FROM categories WHERE slug = 'am-nhac' LIMIT 1),
    (SELECT id FROM venues WHERE name = 'Trung Tâm Hội Chợ & Triển Lãm Sài Gòn (SECC)' LIMIT 1),
    '[TP.HCM] Những Thành Phố Mơ Màng Year End 2026',
    'Những Thành Phố Mơ Màng Year End 2026 - Sài Gòn

THÔNG TIN SỰ KIỆN NTPMM
- Thời gian: 12/12/2026 (thời gian chi tiết sẽ được cập nhật sau)
- Địa điểm: Trung Tâm Hội Chợ & Triển Lãm Sài Gòn (SECC), Tân Mỹ, TP.HCM

GIÁ VÉ VÀ CÁC ROUND MỞ BÁN

QUY ĐỊNH CHUNG CỦA SỰ KIỆN 

Điều 1. Chương trình dành cho đối tượng khán giả trên 16 tuổi. Khán giả từ 10-15 tuổi tham gia chương trình phải có người giám hộ đi kèm (người giám hộ phải từ 18 tuổi trở lên, một người giám hộ kèm một trẻ vị thành niên) để quản lý và chịu hoàn toàn trách nhiệm nếu có bất kỳ sự cố nào xảy ra trong sự kiện.

Điều 2. Phụ nữ mang thai và người có vấn đề về sức khoẻ tự cân nhắc khi tham gia chương trình. Trong trường hợp có vấn đề xảy ra, BTC không chịu trách nhiệm.

Điều 3. Mỗi vé chỉ dành cho một khán giả tham dự, không kèm trẻ em và trẻ vị thành niên. Người giám hộ đi kèm theo khán giả vị thành niên phải mua vé để tham dự. 

Điều 4. Vé đã mua không được đổi hoặc hoàn trả dưới mọi hình thức. Khán giả có trách nhiệm tự bảo quản, bảo mật thông tin mã vé của mình. BTC từ chối giải quyết trường hợp có nhiều hơn một người check-in cùng một mã vé. Theo quy định, BTC cho phép người check-in đầu tiên mã vé bị trùng được tham dự sự kiện. 

Điều 5: Hãy kiểm tra kỹ thông tin trước khi đặt vé, BTC không hỗ trợ đổi chặng hay hoàn tiền đối với những trường hợp đặt nhầm chặng/hạng vé.

Điều 6. Vui lòng không mua vé từ bất kỳ nguồn nào khác ngoài Ticketbox tránh trường hợp vé giả hoặc lừa đảo. BTC từ chối giải quyết cho những trường hợp mua vé từ nguồn khác nếu có vấn đề hay tranh chấp xảy ra.

Điều 7. Khi tham gia chương trình, khán giả đồng ý với việc hình ảnh của mình được sử dụng để khai thác cho sản phẩm ghi hình, thu âm, quảng bá cho chương trình.

Điều 8. BTC có quyền kiểm tra giấy tờ tùy thân của khán giả nếu có nghi ngờ về độ tuổi không phù hợp và từ chối quyền tham gia của bất kì khán giả nào không tuân thủ quy định của chương trình mà không hoàn trả vé.

Điều 9. BTC có thể hoãn, hủy hoặc tạm ngưng sự kiện do vấn đề bất lợi về thời tiết, tình huống khẩn cấp hoặc nguy hiểm (trường hợp bất khả kháng hoặc nằm ngoài tầm kiểm soát của BTC). Trong trường hợp như trên, phương án đảm bảo quyền lợi cho khán giả đã mua vé sẽ được thực hiện theo quyết định của BTC. 

Điều 10. Khán giả tham dự sự kiện phải tự ý thức và có trách nhiệm bảo vệ sức khoẻ của bản thân khi tham gia. Khi tham gia show đồng nghĩa với việc khán giả đã cân nhắc về các vấn đề bao gồm điều kiện thời tiết bất lợi trước, trong hoặc sau sự kiện, các rủi ro từ tình trạng sức khỏe hiện có hoặc trong quá trình di chuyển đi và đến từ địa điểm.

Điều 11. Trong mọi trường hợp, quyết định của BTC là quyết định cuối cùng.

Điều 12. Vui lòng theo dõi thêm và tuân thủ các quy định cũng như những đồ dùng bị cấm mang sẽ được đăng tải tại trang Facebook của chương trình trước khi chương trình được diễn ra.

Mọi vấn đề liên quan đến vé sự kiện cần hỗ trợ, bạn vui lòng liên hệ tổng đài của Ticketbox theo số hotline 1900 6408.',
    'https://salt.tkbcdn.com/ts/ds/a8/f3/f1/d6a97ff188d01c3b04cb7d3db209523d.png',
    @NOW - INTERVAL 1 DAY,
    @NOW + INTERVAL 21 DAY + INTERVAL 11 HOUR,
    @NOW + INTERVAL 21 DAY + INTERVAL 15 HOUR,
    'PUBLISHED', @NOW, @NOW
WHERE NOT EXISTS (
    SELECT 1 FROM events e WHERE e.title = '[TP.HCM] Những Thành Phố Mơ Màng Year End 2026'
);

-- [4/20] [Hà Nội] Những Thành Phố Mơ Màng Year End 2026 (Ticketbox #26645)
INSERT INTO events (organizer_id, category_id, venue_id, title, description, banner_url, sales_start_time, start_time, end_time, status, created_at, updated_at)
SELECT
    (SELECT id FROM users WHERE email = 'organizer4@sportslot.test' LIMIT 1),
    (SELECT id FROM categories WHERE slug = 'am-nhac' LIMIT 1),
    (SELECT id FROM venues WHERE name = 'Trung tâm Triển lãm Việt Nam (VEC)' LIMIT 1),
    '[Hà Nội] Những Thành Phố Mơ Màng Year End 2026',
    'Những Thành Phố Mơ Màng Year End 2026 - Hà Nội

THÔNG TIN SỰ KIỆN NTPMM
- Thời gian: 17/01/2027 (thời gian chi tiết sẽ được cập nhật sau)
- Địa điểm: (Sân Bắc) Trung tâm Triển lãm Việt Nam (VEC), Đường Cầu Tứ Liên, Đông Anh, Hà Nội

GIÁ VÉ VÀ CÁC ROUND MỞ BÁN

QUY ĐỊNH CHUNG CỦA SỰ KIỆN 

Điều 1. Chương trình dành cho đối tượng khán giả trên 16 tuổi. Khán giả từ 10-15 tuổi tham gia chương trình phải có người giám hộ đi kèm (người giám hộ phải từ 18 tuổi trở lên, một người giám hộ kèm một trẻ vị thành niên) để quản lý và chịu hoàn toàn trách nhiệm nếu có bất kỳ sự cố nào xảy ra trong sự kiện.

Điều 2. Phụ nữ mang thai và người có vấn đề về sức khoẻ tự cân nhắc khi tham gia chương trình. Trong trường hợp có vấn đề xảy ra, BTC không chịu trách nhiệm.

Điều 3. Mỗi vé chỉ dành cho một khán giả tham dự, không kèm trẻ em và trẻ vị thành niên. Người giám hộ đi kèm theo khán giả vị thành niên phải mua vé để tham dự. 

Điều 4. Vé đã mua không được đổi hoặc hoàn trả dưới mọi hình thức. Khán giả có trách nhiệm tự bảo quản, bảo mật thông tin mã vé của mình. BTC từ chối giải quyết trường hợp có nhiều hơn một người check-in cùng một mã vé. Theo quy định, BTC cho phép người check-in đầu tiên mã vé bị trùng được tham dự sự kiện. 

Điều 5: Hãy kiểm tra kỹ thông tin trước khi đặt vé, BTC không hỗ trợ đổi chặng hay hoàn tiền đối với những trường hợp đặt nhầm chặng/hạng vé.

Điều 6. Vui lòng không mua vé từ bất kỳ nguồn nào khác ngoài Ticketbox tránh trường hợp vé giả hoặc lừa đảo. BTC từ chối giải quyết cho những trường hợp mua vé từ nguồn khác nếu có vấn đề hay tranh chấp xảy ra.

Điều 7. Khi tham gia chương trình, khán giả đồng ý với việc hình ảnh của mình được sử dụng để khai thác cho sản phẩm ghi hình, thu âm, quảng bá cho chương trình.

Điều 8. BTC có quyền kiểm tra giấy tờ tùy thân của khán giả nếu có nghi ngờ về độ tuổi không phù hợp và từ chối quyền tham gia của bất kì khán giả nào không tuân thủ quy định của chương trình mà không hoàn trả vé.

Điều 9. BTC có thể hoãn, hủy hoặc tạm ngưng sự kiện do vấn đề bất lợi về thời tiết, tình huống khẩn cấp hoặc nguy hiểm (trường hợp bất khả kháng hoặc nằm ngoài tầm kiểm soát của BTC). Trong trường hợp như trên, phương án đảm bảo quyền lợi cho khán giả đã mua vé sẽ được thực hiện theo quyết định của BTC. 

Điều 10. Khán giả tham dự sự kiện phải tự ý thức và có trách nhiệm bảo vệ sức khoẻ của bản thân khi tham gia. Khi tham gia show đồng nghĩa với việc khán giả đã cân nhắc về các vấn đề bao gồm điều kiện thời tiết bất lợi trước, trong hoặc sau sự kiện, các rủi ro từ tình trạng sức khỏe hiện có hoặc trong quá trình di chuyển đi và đến từ địa điểm.

Điều 11. Trong mọi trường hợp, quyết định của BTC là quyết định cuối cùng.

Điều 12. Vui lòng theo dõi thêm và tuân thủ các quy định cũng như những đồ dùng bị cấm mang sẽ được đăng tải tại trang Facebook của chương trình trước khi chương trình được diễn ra.

Mọi vấn đề liên quan đến vé sự kiện cần hỗ trợ, bạn vui lòng liên hệ tổng đài của Ticketbox theo số hotline 1900 6408.',
    'https://salt.tkbcdn.com/ts/ds/ef/28/0b/c6292332ce87884b183c77f9dd8e2c88.png',
    @NOW - INTERVAL 1 DAY,
    @NOW + INTERVAL 28 DAY + INTERVAL 11 HOUR,
    @NOW + INTERVAL 28 DAY + INTERVAL 15 HOUR,
    'PUBLISHED', @NOW, @NOW
WHERE NOT EXISTS (
    SELECT 1 FROM events e WHERE e.title = '[Hà Nội] Những Thành Phố Mơ Màng Year End 2026'
);

-- [5/20] LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN (Ticketbox #26507)
INSERT INTO events (organizer_id, category_id, venue_id, title, description, banner_url, sales_start_time, start_time, end_time, status, created_at, updated_at)
SELECT
    (SELECT id FROM users WHERE email = 'organizer5@sportslot.test' LIMIT 1),
    (SELECT id FROM categories WHERE slug = 'am-nhac' LIMIT 1),
    (SELECT id FROM venues WHERE name = 'Trung tâm triển lãm Việt Nam (VEC) - SUNSET ZONE' LIMIT 1),
    'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN',
    'I. ĐIỀU KIỆN VÀ ĐIỀU KHOẢN MUA VÉ: 

● Khi mua hoặc nhận vé (bao gồm vé được tặng, được mua lại theo quy định của Ticketbox và/hoặc BTC), người mua/người nhận vé được xem là đã đọc, hiểu và đồng ý với Điều Kiện và Điều Khoản của BTC và Quy Định Tham Gia Chương Trình được công bố tại ticketbox.vn. 

● Trường hợp người mua/người nhận vé không đồng thời là Người Tham Dự, người mua/người nhận vé phải đảm bảo Người Tham Dự đã đọc, hiểu và đồng ý với các quy định nêu trên trước khi tham dự Chương trình. 

● Ticketbox là đơn vị phân phối vé độc quyền của sự kiện:  

Live Concert SKYNOTE 2026 – The Reflection Day 2, diễn ra vào ngày 29.11.2026 tại Trung tâm triển lãm Việt Nam (VEC) Hà Nội, Ticketbox chịu trách nhiệm giải quyết các vấn đề phát sinh liên quan đến vé mua và vé tặng thông qua hệ thống Ticketbox (bao gồm tính năng “Tặng vé” và “Resale” trên ứng dụng). Ticketbox không chịu trách nhiệm và có quyền từ chối giải quyết các tranh chấp liên quan đến việc trao tặng, chuyển nhượng, mua bán lại giữa các bên ngoài hệ thống Ticketbox.

● Một (1) Mã Vé có giá trị sử dụng cho một (1) người và một (1) lần duy nhất.

● Quy định về độ tuổi: 

- Dành cho Người Tham Dự từ đủ 10 tuổi trở lên.  

- Người Tham Dự từ đủ 10 đến dưới 18 tuổi phải mang giấy tờ tùy thân có hình ảnh (căn cước công dân, hộ chiếu,...) và có người thân (từ đủ 21 tuổi) đi cùng hạng vé bảo lãnh trong suốt thời gian diễn ra chương trình. Một người thân được phép bảo lãnh tối đa 2 người. 

- Ban Tổ Chức có quyền yêu cầu cung cấp bổ sung tài liệu hoặc thông tin cần thiết để xác minh điều kiện tham dự. Trường hợp BTC không thể xác minh hoặc phát hiện thông tin không chính xác, Ban Tổ Chức có quyền từ chối cho Người Tham Dự tham gia sự kiện và không hoàn trả lại tiền vé.

- Người bảo lãnh cam kết chịu hoàn toàn trách nhiệm về việc: 

+ Đảm bảo phòng ngừa, ngăn chặn người được bảo lãnh sử dụng các chất kích thích, rượu bia, thuốc lá, chất cháy nổ và các sản phẩm bị cấm theo Quy định tham gia chương trình.

+ Chăm sóc, bảo vệ quyền, lợi ích hợp pháp của Người được bảo lãnh; chịu trách nhiệm chăm sóc y tế hoặc chi trả chi phí chăm sóc y tế cho Người được bảo lãnh hoặc người bị tổn hại sức khỏe do Người được bảo lãnh gây ra trong suốt thời gian bảo lãnh. 

● Vé đã mua KHÔNG đổi, trả, nâng cấp, hủy hay hoàn tiền trong bất kì trường hợp nào. Vé bị mất KHÔNG được xuất lại.

● Người Tham Dự cân nhắc và tự chịu trách nhiệm về sức khỏe khi tham gia Chương Trình.

● Ban Tổ Chức có quyền điều chỉnh, bổ sung nội dung Quy định tham gia chương trình, Điều Kiện và Điều Khoản nếu xét thấy cần thiết, đảm bảo không trái với nội dung mà Người Tham Dự đã đọc và đồng ý. Khi mua vé/nhận vé tham dự Chương trình, Người Tham Dự được hiểu là đã đọc, hiểu và đồng ý với Quy định tham gia chương trình, các Điều Kiện và Điều Khoản cũng như bất kỳ thay đổi nào  sau đó. Trường hợp có bất kỳ thay đổi nào đối với Chương trình, Ban Tổ Chức sẽ cung cấp thông tin tại website ticketbox.vn và trên các phương tiện truyền thông của BTC.

● Người Tham Dự có trách nhiệm theo dõi thông báo chính thức từ Ban Tổ Chức/Ticketbox và kiểm tra lại thông tin vé (ngày, giờ, địa điểm, giá) trong trường hợp có thay đổi trước khi sự kiện diễn ra.

● Vé chỉ được sử dụng để tham gia Chương trình. Không mua vé cho mục đích kinh doanh, thu gom số lượng lớn nhằm mục đích trục lợi trái với quy định của BTC và Ticketbox, và không sử dụng vé cho các hoạt động khuyến mãi của hàng hóa, dịch vụ của người mua. Trong trường hợp vi phạm, BTC có quyền thu hồi vé đã mua và không hoàn lại tiền, không cho phép tham dự chương trình bằng những vé này và yêu cầu bồi thường thiệt hại. Quy định này không áp dụng đối với các tổ chức, cá nhân đã đạt được thỏa thuận bằng văn bản với BTC về việc sử dụng vé để khuyến mãi.

● Trong mọi trường hợp, quyết định của BTC là quyết định cuối cùng. 

II. CHÍNH SÁCH XUẤT HÓA ĐƠN CHO KHÁCH HÀNG MUA VÉ: 

● Khách hàng có nhu cầu lấy hóa đơn giá trị gia tăng (VAT) cho giao dịch mua vé có trách nhiệm cung cấp đầy đủ, chính 

... (Vui lòng xem chi tiết quy định tại sự kiện)',
    'https://salt.tkbcdn.com/ts/ds/c8/6f/51/5bf244a772a9d6390b67d4e53dee0609.jpeg',
    @NOW - INTERVAL 1 DAY,
    @NOW + INTERVAL 12 DAY + INTERVAL 11 HOUR,
    @NOW + INTERVAL 12 DAY + INTERVAL 15 HOUR,
    'PUBLISHED', @NOW, @NOW
WHERE NOT EXISTS (
    SELECT 1 FROM events e WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
);

-- [6/20] Mr Siro - Encore Extended - Ai Cũng Giấu Trong Lòng Tảng Băng - Hà Nội (Ticketbox #26333)
INSERT INTO events (organizer_id, category_id, venue_id, title, description, banner_url, sales_start_time, start_time, end_time, status, created_at, updated_at)
SELECT
    (SELECT id FROM users WHERE email = 'organizer6@sportslot.test' LIMIT 1),
    (SELECT id FROM categories WHERE slug = 'am-nhac' LIMIT 1),
    (SELECT id FROM venues WHERE name = 'Trung Tâm Hội Nghị Quốc Gia' LIMIT 1),
    'Mr Siro - Encore Extended - Ai Cũng Giấu Trong Lòng Tảng Băng - Hà Nội',
    'Mr Siro - Fan Concert - Encore Extended  Ai Cũng Giấu Trong Lòng Tảng Băng - Hà Nội

MR SIRO | FAN CONCERT - ENCORE EXTENDED AI CŨNG GIẤU TRONG LÒNG TẢNG BĂNG - HÀ NỘI

CHỦ NHẬT | 01.11.26 | 19:00- 22:00 

• HÀ NỘI | TRUNG TÂM HỘI NGHỊ QUỐC GIA
57 Phạm Hùng, Phường Từ Liêm - TP Hà Nội

🎵 NHỮNG LƯU Ý QUAN TRỌNG CHO ĐÊM CONCERT 🎵
Để chúng ta có một đêm nhạc trọn vẹn cảm xúc và những kỉ niệm đáng nhớ nhất, Khán Giả tham gia concert hãy dành chút thời gian xem qua các quy định dưới đây nhé:

🎫 Cảnh Báo Mua Vé 
• Mua vé an toàn: Chỉ mua vé duy nhất tại website/app Ticketbox. Không nên mua "vé chợ đen"/ vé pass để tránh rủi ro lừa đảo, vì BTC sẽ không thể hỗ trợ các trường hợp này. 
• Số lượng: Mỗi tài khoản được mua tối đa 06 vé. Một vé dành cho một bạn và chỉ có giá trị cho một lần vào cổng thôi nhé.
• Chính sách: Vé đã mua KHÔNG thể hoàn trả, đổi ngày hoặc chỉnh sửa. Các bạn nhớ cân nhắc kỹ lịch trình trước khi quyết định mua vé nha. 
• Bảo mật: Hãy tự bảo quản mã QR vé của mình thật kỹ, không post công khai ở bất cứ đâu. BTC sẽ ưu tiên quyền vào cổng cho chính chủ (theo thông tin trên Ticketbox) nếu có tranh chấp mã vé.

⏰ Thời gian & Độ tuổi
• Độ tuổi: Sự kiện dành cho các khán giả từ 14 tuổi trở lên.
• Check-in: BTC bắt đầu đón khách từ 15:00 đến 18:50 ngày 01.011.2026.
• Lưu ý đặc biệt: Sau 18:50, BTC xin phép ngừng nhận khách check-in để ổn định chỗ ngồi hạn chế di chuyển trong khán đài để chuẩn bị cho tiết mục mở màn cũng như công tác ghi hình được thuận lợi. Các bạn hãy sắp xếp thời gian đến đúng giờ quy định để tận hưởng show diễn tốt trọn vẹn nhất nhé. BTC mong khán giả cân nhắc kỹ trước khi quyết định mua vé để BTC có thể đảm bảo được tính chất show diễn và trải nghiệm của khán giả tham gia đêm nhạc. 

🎒 Một vài quy định nhỏ khác
• Ăn uống: Không mang đồ ăn hay nước uống từ ngoài vào khán phòng để giữ không gian sạch đẹp cho hội trường theo quy định của Nhà Hát. 
• Hình ảnh: Đêm nhạc sẽ được ghi hình trực tiếp. Khi tham gia, đồng nghĩa là bạn đồng ý để những hình ảnh của mình xuất hiện trong các video truyền thông của dự án sau này. 
• Chương trình không được phép livestream, kính mong khán giả tuân thủ quy định.
• An ninh: BTC có quyền từ chối sự tham gia của các cá nhân vi phạm nội quy chung hoặc có những hành động, lời nói không phù hợp không khí đêm diễn. 
• Quyết định cuối cùng: Trong mọi tình huống, quyết định của BTC là quyết định cuối cùng.
Cảm ơn các bạn đã thấu hiểu và phối hợp cùng BTC. Hẹn gặp tất cả các bạn tại đêm diễn nhé! ✨',
    'https://salt.tkbcdn.com/ts/ds/f6/7b/fc/5657c5c2f5d83c534ae75b60e78394ee.jpg',
    @NOW - INTERVAL 1 DAY,
    @NOW + INTERVAL 9 DAY + INTERVAL 11 HOUR,
    @NOW + INTERVAL 9 DAY + INTERVAL 15 HOUR,
    'PUBLISHED', @NOW, @NOW
WHERE NOT EXISTS (
    SELECT 1 FROM events e WHERE e.title = 'Mr Siro - Encore Extended - Ai Cũng Giấu Trong Lòng Tảng Băng - Hà Nội'
);

-- [7/20] THE BROTHERS | Đỗ Hoàng Hiệp x Tăng Phúc x Hà Lê x Cheng x Bình Văn Band (Ticketbox #26364)
INSERT INTO events (organizer_id, category_id, venue_id, title, description, banner_url, sales_start_time, start_time, end_time, status, created_at, updated_at)
SELECT
    (SELECT id FROM users WHERE email = 'organizer7@sportslot.test' LIMIT 1),
    (SELECT id FROM categories WHERE slug = 'am-nhac' LIMIT 1),
    (SELECT id FROM venues WHERE name = 'Trung Tâm Nghệ Thuật Âu cơ' LIMIT 1),
    'THE BROTHERS | Đỗ Hoàng Hiệp x Tăng Phúc x Hà Lê x Cheng x Bình Văn Band',
    'Đêm nhạc THE BROTHERS quy tụ dàn nghệ sĩ tài năng: Đỗ Hoàng Hiệp x Tăng Phúc x Hà Lê x Cheng cùng Bình Văn Band tại Trung Tâm Nghệ Thuật Âu Cơ, Hà Nội.
Khán giả sẽ được đắm chìm trong những giai điệu thăng hoa và các màn kết hợp đỉnh cao, bùng nổ cảm xúc.',
    'https://salt.tkbcdn.com/ts/ds/9b/85/b2/9362cb5a33bebeb4b290577b583446dc.jpeg',
    @NOW - INTERVAL 1 DAY,
    @NOW + INTERVAL 6 DAY + INTERVAL 11 HOUR,
    @NOW + INTERVAL 6 DAY + INTERVAL 15 HOUR,
    'PUBLISHED', @NOW, @NOW
WHERE NOT EXISTS (
    SELECT 1 FROM events e WHERE e.title = 'THE BROTHERS | Đỗ Hoàng Hiệp x Tăng Phúc x Hà Lê x Cheng x Bình Văn Band'
);

-- [8/20] Whisper of Two Hearts [Hoàng Hải - Phương Linh] (Ticketbox #26746)
INSERT INTO events (organizer_id, category_id, venue_id, title, description, banner_url, sales_start_time, start_time, end_time, status, created_at, updated_at)
SELECT
    (SELECT id FROM users WHERE email = 'organizer8@sportslot.test' LIMIT 1),
    (SELECT id FROM categories WHERE slug = 'san-khau-nghe-thuat' LIMIT 1),
    (SELECT id FROM venues WHERE name = 'Nhà hát Hoa Phượng' LIMIT 1),
    'Whisper of Two Hearts [Hoàng Hải - Phương Linh]',
    'CÓ NHỮNG GIỌNG HÁT LÀ CẢ MỘT TRỜI THANH XUÂN... 🍂

Hãy nhắm mắt lại và để những thanh âm dắt bạn quay về với những rung động nguyên sơ nhất.

Tiếng hát bay bổng, kiêu sa của Phương Linh quyện cùng chất giọng rực lửa, dạt dào sức sống của Hoàng Hải chưa bao giờ ngừng xoa dịu những trái tim đang chênh vênh. Đã bao lâu rồi bạn không cho phép mình tĩnh lặng để sống lại những ký ức tuyệt đẹp ấy?

Giữa không gian tráng lệ của Nhà hát Hoa Phượng, trong tiết trời dịu dàng của đêm thu cuối tháng 10, "Whisper of Two Hearts" sẽ là nơi âm nhạc thay bạn nói lên những lời tự tình chưa từng tỏ bày. Một đêm nhạc không có chỗ cho những bộn bề, chỉ có nghệ thuật và những tâm hồn đồng điệu.

🎟 Những hàng ghế trung tâm đang tìm kiếm chủ nhân của nó. Hãy tự thưởng cho mình một đêm trọn vẹn với âm nhạc thực thụ!

Thời gian: 20h00, Thứ Bảy - 31/10/2026

Địa điểm: Nhà hát Hoa Phượng - Số 1 Trần Kiên, Thủy Nguyên, Hải Phòng',
    'https://salt.tkbcdn.com/ts/ds/ea/53/9b/5048ea57f51538928d7ca01db212f150.jpg',
    @NOW - INTERVAL 1 DAY,
    @NOW + INTERVAL 10 DAY + INTERVAL 11 HOUR,
    @NOW + INTERVAL 10 DAY + INTERVAL 15 HOUR,
    'PUBLISHED', @NOW, @NOW
WHERE NOT EXISTS (
    SELECT 1 FROM events e WHERE e.title = 'Whisper of Two Hearts [Hoàng Hải - Phương Linh]'
);

-- [9/20] [CAT&MOUSE] THÀNH VÁ (Ticketbox #26678)
INSERT INTO events (organizer_id, category_id, venue_id, title, description, banner_url, sales_start_time, start_time, end_time, status, created_at, updated_at)
SELECT
    (SELECT id FROM users WHERE email = 'organizer9@sportslot.test' LIMIT 1),
    (SELECT id FROM categories WHERE slug = 'am-nhac' LIMIT 1),
    (SELECT id FROM venues WHERE name = 'Cat&Mouse Live Music' LIMIT 1),
    '[CAT&MOUSE] THÀNH VÁ',
    'Đêm nhạc acoustic đầy cảm xúc cùng ca sĩ Thành Vá tại Cat&Mouse Live Music, TP. Hồ Chí Minh.
Thưởng thức những giai điệu acoustic mộc mạc, gần gũi trong không gian âm nhạc ấm cúng và tràn đầy năng lượng nghệ thuật.',
    'https://salt.tkbcdn.com/ts/ds/e8/c4/75/b9f4667d5ae274899ca287bada756672.png',
    @NOW - INTERVAL 1 DAY,
    @NOW + INTERVAL 4 DAY + INTERVAL 11 HOUR,
    @NOW + INTERVAL 4 DAY + INTERVAL 15 HOUR,
    'PUBLISHED', @NOW, @NOW
WHERE NOT EXISTS (
    SELECT 1 FROM events e WHERE e.title = '[CAT&MOUSE] THÀNH VÁ'
);

-- [10/20] Lễ Hội Bia Thủ Công (Ticketbox #26747)
INSERT INTO events (organizer_id, category_id, venue_id, title, description, banner_url, sales_start_time, start_time, end_time, status, created_at, updated_at)
SELECT
    (SELECT id FROM users WHERE email = 'organizer10@sportslot.test' LIMIT 1),
    (SELECT id FROM categories WHERE slug = 'am-nhac' LIMIT 1),
    (SELECT id FROM venues WHERE name = 'Khách sạn Holiday Inn & Suites Saigon Airport' LIMIT 1),
    'Lễ Hội Bia Thủ Công',
    'HARVEST BEER FEST – LỄ HỘI BIA THỦ CÔNG

Tối thứ Sáu, rủ hội bạn “chill” hết mình bên hồ bơi với bia thủ công, BBQ, âm nhạc và loạt hoạt động giải trí sôi động tại Holiday Inn & Suites Saigon Airport.

Chỉ diễn ra trong một đêm duy nhất, Harvest Beer Fest mang đến trải nghiệm Poolside Beer & Music Festival với bia C-Brew không giới hạn, buffet BBQ quốc tế, live band, trình diễn nghệ thuật lửa cùng nhiều thử thách và giải thưởng hấp dẫn.

Giá vé: 1.299.000 VND/khách

📅 Thứ Sáu, 06/11/2026 | 17:00 – 22:00
📍 Khu vực hồ bơi ngoài trời, Tầng L – Holiday Inn & Suites Saigon Airport

Chi tiết sự kiện

Vé tham dự bao gồm:

• 🍺 Bia thủ công C-Brew không giới hạn

• 🍖 Buffet BBQ quốc tế

• 🎶 Live band mang đến không khí âm nhạc sôi động bên hồ bơi

• 🔥 Show trình diễn nghệ thuật lửa

• 🎯 Các thử thách và trò chơi tương tác với cơ hội nhận 1.000.000 VND tiền mặt

• 🎁 Lucky Draw cùng nhiều giải thưởng hấp dẫn

Trải nghiệm đặc biệt

Không chỉ là một đêm thưởng thức bia, Harvest Beer Fest được thiết kế như một lễ hội bên hồ bơi với không gian mở, âm nhạc, ẩm thực và các hoạt động tương tác xuyên suốt chương trình.

Đây là dịp lý tưởng để tụ họp cùng bạn bè, nâng ly, thưởng thức BBQ và tận hưởng một tối thứ Sáu thật sôi động.

Điều khoản và điều kiện

• Vé có giá 1.299.000 VND/khách 

• Giá vé đã bao gồm 5% phí phục vụ và thuế VAT hiện hành.

• Vé áp dụng cho 01 khách tham dự sự kiện.

• Khách vui lòng xuất trình vé điện tử hoặc mã vé hợp lệ khi check-in.

• Vé đã mua không hoàn, không hủy và không quy đổi thành tiền mặt.

• Vé có thể được chuyển nhượng cho người khác trước thời điểm check-in.

• Ban tổ chức có quyền từ chối các vé không hợp lệ, đã qua sử dụng hoặc có dấu hiệu sao chép/gian lận.

• Chương trình và lịch trình có thể được điều chỉnh tùy tình hình thực tế mà không làm thay đổi quyền lợi chính của khách tham dự.

• Việc tham gia các hoạt động tại sự kiện đồng nghĩa với việc khách đồng ý tuân thủ hướng dẫn an toàn và quy định của khách sạn.

Lưu ý về trẻ em

Sự kiện có phục vụ đồ uống có cồn. Khách dưới 18 tuổi không được phục vụ hoặc sử dụng đồ uống có cồn.

Trẻ em tham dự cần có người lớn đi cùng và chịu sự giám sát của phụ huynh/người giám hộ trong suốt thời gian diễn ra sự kiện.',
    'https://salt.tkbcdn.com/ts/ds/22/be/e2/7c4ffb556c5455efe5524dfec35e5c50.jpg',
    @NOW - INTERVAL 1 DAY,
    @NOW + INTERVAL 14 DAY + INTERVAL 11 HOUR,
    @NOW + INTERVAL 14 DAY + INTERVAL 15 HOUR,
    'PUBLISHED', @NOW, @NOW
WHERE NOT EXISTS (
    SELECT 1 FROM events e WHERE e.title = 'Lễ Hội Bia Thủ Công'
);

-- [11/20] LCP 2026 Promotion Series (Ticketbox #26689)
INSERT INTO events (organizer_id, category_id, venue_id, title, description, banner_url, sales_start_time, start_time, end_time, status, created_at, updated_at)
SELECT
    (SELECT id FROM users WHERE email = 'organizer11@sportslot.test' LIMIT 1),
    (SELECT id FROM categories WHERE slug = 'the-thao' LIMIT 1),
    (SELECT id FROM venues WHERE name = 'VPS Arena' LIMIT 1),
    'LCP 2026 Promotion Series',
    'LCP 2026 PROMOTION SERIES

Cuộc Chiến Giành Vé Thăng Hạng

Quyền lợi / Benefits

Sơ đồ sự kiện / Event map

LCP 2026 Promotion Series là giải đấu thăng hạng thuộc hệ thống Liên Minh Huyền Thoại khu vực Châu Á - Thái Bình Dương (LCP) do Riot Games tổ chức.

Thời gian mở cửa:

• 10/11/2026: 10:00

• 11/11/2026: 14:00

• 12/11/2026: 14:00

• 13/11/2026: 14:00

Đến với LCP 2026 Promotion Series, người hâm mộ sẽ được:

• Theo dõi trực tiếp: Cổ vũ các đội tuyển yêu thích và tận hưởng những trận đấu gay cấn ngay tại đấu trường.

• Giao lưu cùng các đội tuyển: Tham gia các hoạt động tương tác và kết nối với cộng đồng Liên Minh Huyền Thoại.

• Nhận quà sự kiện đặc biệt: Có cơ hội nhận những phần quà hấp dẫn mang dấu ấn kết hợp giữa Liên Minh Huyền Thoại và các biểu tượng văn hóa đặc trưng của Việt Nam.

• Khám phá gian hàng tài trợ: Trải nghiệm các hoạt động thú vị, tham gia trò chơi và có cơ hội nhận những phần quà hấp dẫn từ các thương hiệu đồng hành.

Ai sẽ giành tấm vé danh giá bước vào LCP 2027?

Đừng bỏ lỡ cơ hội chứng kiến những trận chiến quyết định và hòa mình vào ngày hội Liên Minh Huyền Thoại tại TP. Hồ Chí Minh!

Đặt vé ngay và cùng cháy hết mình tại LCP 2026 Promotion Series!

---

LCP 2026 Promotion Series is a promotion tournament within the League of Legends Championship Pacific (LCP) ecosystem, organized by Riot Games.

Taking place from November 10–13, 2026 at VPS Arena, Ho Chi Minh City, the event promises high-level competition, intense matchups, and an electrifying esports atmosphere.

Doors open:

• November 10, 2026: 10:00 AM

• November 11, 2026: 2:00 PM

• November 12, 2026: 2:00 PM

• November 13, 2026: 2:00 PM

At the LCP 2026 Promotion Series, fans can look forward to:

• Watch the action live: Cheer for your favorite teams and experience the excitement of every match right from the arena.

• Connect with the teams: Take part in interactive activities and engage with the League of Legends community.

• Special event gifts: Get the chance to receive exclusive gifts that bring together League of Legends and iconic elements of Vietnamese culture.

• Explore sponsor booths: Enjoy fun activities, join games, and get the chance to receive exciting giveaways from participating brands.

Who will earn the coveted spot in LCP 2027?

Don’t miss the chance to witness the decisive battles and be part of the League of Legends celebration in Ho Chi Minh City.

Get your tickets now and bring the energy to LCP 2026 Promotion Series!

Điều kiện & Điều khoản tham gia sự kiện:

1. Lưu ý

• 
Hình thức phát hành vé: Sự kiện sẽ không giao nhận vé trực tiếp tại sự kiện. Toàn bộ vé điện tử (E-Ticket) sẽ được gửi qua Ticketbox của bạn ngay khi thanh toán thành công.

• 
Mỗi tài khoản được mua tối đa 04 vé.

• 
Chính sách hoàn/hủy và chuyển nhượng: Vé đã mua sẽ không được hoàn tiền hay đổi trả (trừ một số trường hợp đặc biệt theo chính sách của BTC). Nếu cần chuyển nhượng vé, bạn vui lòng thực hiện qua nền tảng Ticketbox để đảm bảo vé hợp lệ.

• 
Quy định check-in: Mỗi người tham gia cần xuất trình mã QR hợp lệ của riêng mình (kể cả khi bạn đi cùng nhóm hay được bạn bè mua vé giúp).

• 
Ban tổ chức không mở bán vé trực tiếp tại địa điểm thi đấu.

• 
Người mua vé chịu trách nhiệm toàn bộ cho việc đặt vé, thanh toán, nhận vé và vào cổng. Ban tổ chức không chịu trách nhiệm cho các sự cố phát sinh từ phía người tham dự.

• 
Phần bình luận trực tiếp tại sự kiện sẽ chỉ sử dụng tiếng Việt.

• 
Ban tổ chức không chịu trách nhiệm và không giải quyết bất kỳ vấn đề phát sinh từ việc mua vé qua bên thứ ba.

• 
Để bảo mật thông tin vé, khán giả không nên chia sẻ hình ảnh vé lên mạng xã hội, cũng như không gửi cho người khác.

• 
Xuất trình thẻ/ vòng tay check-in để có thể tự do ra vào sự kiện. Người tham gia không thể vào khu vực sự kiện nếu làm mất vòng tay/thẻ check-in.

• 
Vui lòng không đứng, che khuất tầm nhìn của người khác hoặc ngồi sai vị trí ghế đã được in trên vé.

• 
Không tự ý di chuyển sang vị trí ngồi khác khi chưa có sự hướng dẫn từ

... (Vui lòng xem chi tiết quy định tại sự kiện)',
    'https://salt.tkbcdn.com/ts/ds/da/b2/d6/c4ee3ae063b36d5806fc73d70081a8ad.png',
    @NOW - INTERVAL 1 DAY,
    @NOW + INTERVAL 16 DAY + INTERVAL 11 HOUR,
    @NOW + INTERVAL 16 DAY + INTERVAL 15 HOUR,
    'PUBLISHED', @NOW, @NOW
WHERE NOT EXISTS (
    SELECT 1 FROM events e WHERE e.title = 'LCP 2026 Promotion Series'
);

-- [12/20] Queens Of Glory - Giải Thể Thao Điện Tử Vinh Quang Nữ Hoàng (Ticketbox #26733)
INSERT INTO events (organizer_id, category_id, venue_id, title, description, banner_url, sales_start_time, start_time, end_time, status, created_at, updated_at)
SELECT
    (SELECT id FROM users WHERE email = 'organizer12@sportslot.test' LIMIT 1),
    (SELECT id FROM categories WHERE slug = 'the-thao' LIMIT 1),
    (SELECT id FROM venues WHERE name = 'VPS Arena' LIMIT 1),
    'Queens Of Glory - Giải Thể Thao Điện Tử Vinh Quang Nữ Hoàng',
    'Queens of Glory mùa Đông 2026

Tiếp nối dấu ấn từ mùa giải đầu tiên, Queens of Glory mùa Đông 2026 trở lại với những màn tranh tài được mong đợi, nơi các đội tuyển nữ tiếp tục khẳng định bản lĩnh và cạnh tranh cho chiếc vương miện danh giá. Đội tuyển nào sẽ giành được chiếc vương miện cao quý, trở thành nhà vô địch của Queens of Glory Mùa Đông 2026.

Từ ngày 30/10 đến 01/11/2026, sự kiện offline Queens of Glory Mùa Đông 2026 sẽ chính thức diễn ra, mang đến cơ hội để người hâm mộ trực tiếp theo dõi những trận đấu đỉnh cao, gặp gỡ các nữ tuyển thủ tài năng và bản lĩnh, đồng thời tham gia các hoạt động tương tác hấp dẫn cùng nhiều trải nghiệm thú vị sẽ được công bố trong thời gian tới.

Đừng bỏ lỡ cơ hội trở thành một phần của bữa tiệc Queens of Glory mùa Đông 2026! Hãy cùng bạn bè đến sân khấu offline, cổ vũ đội tuyển yêu thích và hòa mình vào bầu không khí sôi động của chặng đua quyết định.

SLAY PARTY: SQUAD UP!

Thời gian check-in:

• 
30/10/2026: 14:00

• 
31/10/2026: 14:00

• 
01/11/2026: 12:00

Thời gian bắt đầu thi đấu:

• 
30/10/2026: 16:00

• 
31/10/2026: 16:00

• 
01/11/2026: 14:00

SƠ ĐỒ CHỖ NGỒI

QUYỀN LỢI

QUY ĐỊNH MUA VÉ VÀ CHECK-IN:

1. Khán giả mua vé sẽ không thể thay đổi thông tin cá nhân trên vé. Vé đã mua không được đổi hoặc hoàn trả dưới mọi hình thức. Ban Tổ chức sẽ không chịu trách nhiệm giải quyết các trường hợp thông tin vé bị sai lệch, vé không hợp lệ.

2. Khán giả phải nhập đúng CMND/CCCD khi tiến hành mua vé, Ban Tổ chức có kiểm tra CMND/CCCD của người mua vé khi đến check-in tại sự kiện.

3. Mỗi lần giao dịch chỉ được mua tối đa 02 vé.

4. Quà tặng & Vòng tay:

Khán giả mua vé sẽ được nhận quà và dây đeo vào ngay khi check-in. Quà chỉ được lấy 1 lần duy nhất theo hạng vé, không hỗ trợ đổi trả.

5. Check-in:

Khán giả dùng mã vé để check-in và phải mang theo CCCD/ CMND/ VNeID để xác nhận.

6. Bảo mật:

Vui lòng không mua vé từ bất kỳ nguồn nào khác để tránh trường hợp vé giả hoặc lừa đảo, Ban Tổ chức không chịu trách nhiệm giải quyết các trường hợp này.

Khán giả có trách nhiệm tự bảo mật mã vé điện tử của mình. Ban Tổ chức từ chối giải quyết trường hợp có nhiều hơn 1 người check-in cùng 1 mã vé. Theo quy định, Ban Tổ chức sẽ chấp nhận cho người có CCCD/CMND/VNeID trùng với thông tin của mã vé. 

7. Hoạt động trong sự kiện:

Khán giả lưu ý bảo quản vòng tay cẩn thận để thuận tiện cho việc ra vào khu vực diễn ra sự kiện. Ban Tổ chức không giải quyết nếu khán giả làm mất vòng tay.

Khán giả chỉ được phép di chuyển trong khu vực quy định trên vé, mọi khu vực khác đều bị hạn chế.

8. Lưu ý khác:

Giá vé đã bao gồm 8% VAT.

Vé chỉ được sử dụng một lần.',
    'https://salt.tkbcdn.com/ts/ds/f8/cd/d1/e1dd2c3facc36c0ad261eb2b5b27f893.jpg',
    @NOW - INTERVAL 1 DAY,
    @NOW + INTERVAL 18 DAY + INTERVAL 11 HOUR,
    @NOW + INTERVAL 18 DAY + INTERVAL 15 HOUR,
    'PUBLISHED', @NOW, @NOW
WHERE NOT EXISTS (
    SELECT 1 FROM events e WHERE e.title = 'Queens Of Glory - Giải Thể Thao Điện Tử Vinh Quang Nữ Hoàng'
);

-- [13/20] Hà Nội - Eifman Ballet Of ST. Petersubrg World Tour 2026 (Ticketbox #26216)
INSERT INTO events (organizer_id, category_id, venue_id, title, description, banner_url, sales_start_time, start_time, end_time, status, created_at, updated_at)
SELECT
    (SELECT id FROM users WHERE email = 'organizer13@sportslot.test' LIMIT 1),
    (SELECT id FROM categories WHERE slug = 'san-khau-nghe-thuat' LIMIT 1),
    (SELECT id FROM venues WHERE name = 'Cung Văn Hóa Hữu Nghị Việt Xô' LIMIT 1),
    'Hà Nội - Eifman Ballet Of ST. Petersubrg World Tour 2026',
    'Eifman Ballet Of ST. Petersburg Eugene Onegin - Ho Chi Minh

Có những điều chỉ được hiểu ra khi đã quá muộn. Kiệt tác ballet tầm thế giới của Boris Eifman. Lần đầu tiên đến Việt Nam.
Chỉ 8 đêm diễn duy nhất.

Điều khoản và điều kiện:

* Sau khi đặt vé thành công, BTC SPO sẽ gửi vé điện tử trong vòng 72 giờ kể từ thời điểm thanh toán thành công

* 1 (MỘTx)) vé chỉ có hiệu lực cho 1 (MỘT) người sở hữu vé cho 1 (MỘT) lần vào bên trong khu vực diễn ra sự kiện.

* Vé KHÔNG được hoàn trả, đổi ngày hoặc chỉnh sửa trong bất kỳ trường hợp nào.

* Giá vé đã bao gồm VAT. Khách hàng có nhu cầu xuất hóa đơn VAT vui lòng liên hệ BTC SPO qua email: customerservice1.spo@gmail.com ngay trong ngày đặt vé, trường hợp qua ngày sẽ không được hỗ trợ

* Khán giả có trách nhiệm tự bảo quản vé của mình. BTC từ chối giải quyết các trường hợp có nhiều hơn 1 người check-in cùng 1 mã vé. 

* Trong mọi trường hợp, quyết định của Ban tổ chức là quyết định cuối cùng.',
    'https://salt.tkbcdn.com/ts/ds/4f/68/61/12e6530d26bb75b0910a0e2e7944337b.jpg',
    @NOW - INTERVAL 1 DAY,
    @NOW + INTERVAL 25 DAY + INTERVAL 11 HOUR,
    @NOW + INTERVAL 25 DAY + INTERVAL 15 HOUR,
    'PUBLISHED', @NOW, @NOW
WHERE NOT EXISTS (
    SELECT 1 FROM events e WHERE e.title = 'Hà Nội - Eifman Ballet Of ST. Petersubrg World Tour 2026'
);

-- [14/20] Hồ Chí Minh - Eifman Ballet Of ST. Petersubrg World Tour 2026 (Ticketbox #26215)
INSERT INTO events (organizer_id, category_id, venue_id, title, description, banner_url, sales_start_time, start_time, end_time, status, created_at, updated_at)
SELECT
    (SELECT id FROM users WHERE email = 'organizer14@sportslot.test' LIMIT 1),
    (SELECT id FROM categories WHERE slug = 'san-khau-nghe-thuat' LIMIT 1),
    (SELECT id FROM venues WHERE name = 'Nhà Hát Hòa Bình' LIMIT 1),
    'Hồ Chí Minh - Eifman Ballet Of ST. Petersubrg World Tour 2026',
    'Eifman Ballet Of ST.Petersburg Eugene Onegin

Có những điều chỉ được hiểu ra khi đã quá muộn. Kiệt tác ballet tầm thế giới của Boris Eifman. Lần đầu tiên đến Việt Nam.
Chỉ 8 đêm diễn duy nhất

Điều khoản và điều kiện:

* Sau khi đặt vé thành công, BTC SPO sẽ gửi vé điện tử trong vòng 72 giờ kể từ thời điểm thanh toán thành công

* 1 (MỘTx)) vé chỉ có hiệu lực cho 1 (MỘT) người sở hữu vé cho 1 (MỘT) lần vào bên trong khu vực diễn ra sự kiện.

* Vé KHÔNG được hoàn trả, đổi ngày hoặc chỉnh sửa trong bất kỳ trường hợp nào.

* Giá vé đã bao gồm VAT. Khách hàng có nhu cầu xuất hóa đơn VAT vui lòng liên hệ BTC SPO qua email: customerservice1.spo@gmail.com ngay trong ngày đặt vé, trường hợp qua ngày sẽ không được hỗ trợ

* Khán giả có trách nhiệm tự bảo quản vé của mình. BTC từ chối giải quyết các trường hợp có nhiều hơn 1 người check-in cùng 1 mã vé. 

* Trong mọi trường hợp, quyết định của Ban tổ chức là quyết định cuối cùng.

Vé đã mua sẽ không được hoàn hủy với bất kỳ lý do nào!',
    'https://salt.tkbcdn.com/ts/ds/6b/9b/44/d897fee76073aaabd33d0974b10b433d.jpg',
    @NOW - INTERVAL 1 DAY,
    @NOW + INTERVAL 30 DAY + INTERVAL 11 HOUR,
    @NOW + INTERVAL 30 DAY + INTERVAL 15 HOUR,
    'PUBLISHED', @NOW, @NOW
WHERE NOT EXISTS (
    SELECT 1 FROM events e WHERE e.title = 'Hồ Chí Minh - Eifman Ballet Of ST. Petersubrg World Tour 2026'
);

-- [15/20] Nhà Hát Kịch IDECAF: Dưới Bóng Giai Nhân (Ticketbox #25859)
INSERT INTO events (organizer_id, category_id, venue_id, title, description, banner_url, sales_start_time, start_time, end_time, status, created_at, updated_at)
SELECT
    (SELECT id FROM users WHERE email = 'organizer15@sportslot.test' LIMIT 1),
    (SELECT id FROM categories WHERE slug = 'san-khau-nghe-thuat' LIMIT 1),
    (SELECT id FROM venues WHERE name = 'Sân Khấu Nhà Thiếu Nhi TP.HCM' LIMIT 1),
    'Nhà Hát Kịch IDECAF: Dưới Bóng Giai Nhân',
    'Dưới Bóng Giai Nhân sẽ là một cách kể khác về Kiều và những cái tên tưởng chừng như đã rất quen thuộc. Thêm vào đó một số nhân vật và sự kiện hư cấu không có trong nguyên tác.

- Tác Giả: Quang Thảo

- Đạo Diễn: Quang Thảo

- Diễn Viên: Thanh Thủy, Hồng Ánh, Hoàng Trinh, Mỹ Duyên, Tuyền Mập, Bạch Long, Đình Toàn, Đại Nghĩa, Công Danh, Trịnh Minh Dũng, Phạm Hùng, Kan Lê, Ngọc Nguyên, Minh Sang, Hữu Đạt, Phạm Hạnh, Thanh Anh, Trúc My, Bích Trâm, Bảo Cường, Nhà Hát Thiếu Nhi NỤ CƯỜI với sự góp mặt đặc biệt của NS Diệu Đức.',
    'https://salt.tkbcdn.com/ts/ds/e9/76/b4/2e8fe2916b6237e6676f7acbb382b260.jpg',
    @NOW - INTERVAL 1 DAY,
    @NOW + INTERVAL 5 DAY + INTERVAL 11 HOUR,
    @NOW + INTERVAL 5 DAY + INTERVAL 15 HOUR,
    'PUBLISHED', @NOW, @NOW
WHERE NOT EXISTS (
    SELECT 1 FROM events e WHERE e.title = 'Nhà Hát Kịch IDECAF: Dưới Bóng Giai Nhân'
);

-- [16/20] Nhà Hát Kịch IDECAF: Hồn Ai Nấy Giữ (Ticketbox #24907)
INSERT INTO events (organizer_id, category_id, venue_id, title, description, banner_url, sales_start_time, start_time, end_time, status, created_at, updated_at)
SELECT
    (SELECT id FROM users WHERE email = 'organizer16@sportslot.test' LIMIT 1),
    (SELECT id FROM categories WHERE slug = 'san-khau-nghe-thuat' LIMIT 1),
    (SELECT id FROM venues WHERE name = 'Nhà Hát Kịch IDECAF' LIMIT 1),
    'Nhà Hát Kịch IDECAF: Hồn Ai Nấy Giữ',
    'HỒN AI NẤY GIỮ

• Tác giả: Đăng Nhân

• Đạo diễn: Hùng Lâm

• Diễn viên: Đại Nghĩa, Hồng Ánh, Đình Toàn, Quốc Thịnh, Phi Nga, Tuyền Mập, Cẩm Hò, Thái Hiển, Hoài Trang, Phạm Hạnh, Trúc My, Thanh Anh, Công Tôn Nghĩa, Phương Nguyễn...',
    'https://salt.tkbcdn.com/ts/ds/39/6e/28/325cda4a104fdc0ccb1b5c1d464927a8.jpg',
    @NOW - INTERVAL 1 DAY,
    @NOW + INTERVAL 8 DAY + INTERVAL 11 HOUR,
    @NOW + INTERVAL 8 DAY + INTERVAL 15 HOUR,
    'PUBLISHED', @NOW, @NOW
WHERE NOT EXISTS (
    SELECT 1 FROM events e WHERE e.title = 'Nhà Hát Kịch IDECAF: Hồn Ai Nấy Giữ'
);

-- [17/20] Sân khấu Trương Hùng Minh: Song Trùng (Ticketbox #25900)
INSERT INTO events (organizer_id, category_id, venue_id, title, description, banner_url, sales_start_time, start_time, end_time, status, created_at, updated_at)
SELECT
    (SELECT id FROM users WHERE email = 'organizer17@sportslot.test' LIMIT 1),
    (SELECT id FROM categories WHERE slug = 'san-khau-nghe-thuat' LIMIT 1),
    (SELECT id FROM venues WHERE name = 'SÂN KHẤU NGHỆ THUẬT TRƯƠNG HÙNG MINH' LIMIT 1),
    'Sân khấu Trương Hùng Minh: Song Trùng',
    'VỞ KỊCH: SONG TRÙNG

Tác giả: Quốc Nguyễn

Đạo diễn: NSND Hữu Quốc

Cố vấn nghệ thuật: NSND Trần Ngọc Giàu

Giám đốc sản xuất: NSƯT Minh Nhí

Biên kịch: Đình Phong',
    'https://salt.tkbcdn.com/ts/ds/df/ec/26/35bec5d69c0733a56eec669faa2a10c6.jpeg',
    @NOW - INTERVAL 1 DAY,
    @NOW + INTERVAL 11 DAY + INTERVAL 11 HOUR,
    @NOW + INTERVAL 11 DAY + INTERVAL 15 HOUR,
    'PUBLISHED', @NOW, @NOW
WHERE NOT EXISTS (
    SELECT 1 FROM events e WHERE e.title = 'Sân khấu Trương Hùng Minh: Song Trùng'
);

-- [18/20] SÂN KHẤU BAN MAI - VỞ NHẠC KỊCH THIẾU NHI: PENNY - Ô MÊ LY (Ticketbox #23446)
INSERT INTO events (organizer_id, category_id, venue_id, title, description, banner_url, sales_start_time, start_time, end_time, status, created_at, updated_at)
SELECT
    (SELECT id FROM users WHERE email = 'organizer18@sportslot.test' LIMIT 1),
    (SELECT id FROM categories WHERE slug = 'san-khau-nghe-thuat' LIMIT 1),
    (SELECT id FROM venues WHERE name = 'Sân khấu Ban Mai - Nhà Thiếu Nhi Thành Phố Hồ Chí Minh' LIMIT 1),
    'SÂN KHẤU BAN MAI - VỞ NHẠC KỊCH THIẾU NHI: PENNY - Ô MÊ LY',
    'MỘT VỞ DIỄN THIẾU NHI ĐƯỢC ĐẠO DIỄN & BIÊN KỊCH BỞI

BẢO CHU & BAN MAI

---

VỚI SỰ THAM GIA CỦA CÁC DIỄN VIÊN:

PENNY PHƯỚC LỘC – BẢO CHU · MAMY KIM ĐÀO · DADDY  – TUẤN TÚ · FUNNY ĐINH TRỌNG TRƯỜNG · EO ƠI NHƯ THÙY – ĐẶNG THANH NGÂN · MIEMIE PHƯƠNG UYÊN · SAO BÂY GIỜ PHÚC ZELO – BẢO CHU · MỰC MỘT NẮNG HẠNH THẢO – NHƯ THỦY · CÁ ĐUỐI QUÁ THẢO VY · THẦN TRÀ LÊ HIẾU · KENNY LÊ SƠN · RÙA TÂM AN · CARI C DUY KHIÊM NGÔ · CARI D CÁT CÁT · LẶN 1 KANHY · LẶN 2 HỒNG ĐÀO

GIỚI THIỆU NỘI DUNG VỀ VỞ PENNY - Ô MÊ LY

Nội dung vở kịch

Lễ hội Mùa Về đang rộn ràng khắp Nam Cực, nơi gia đình chim cánh cụt Penny cùng muôn loài quây quần chờ đón mùa cá di cư lớn nhất trong năm. Thế nhưng, một hiện tượng thiên nhiên bất ngờ ập đến khiến vùng biển náo động, cuốn trôi sự bình yên và đẩy Penny vào một chuyến phiêu lưu ngoài dự tính. Trên hành trình vượt qua những tảng băng tan và những vùng biển lạ lẫm, Penny cùng những người bạn mới phải đối mặt với muôn vàn thử thách cam go để tìm cách giải cứu ngôi nhà thân yêu. Câu chuyện là một chuyến đi đầy màu sắc, đưa các bạn nhỏ đi từ bất ngờ này đến bất ngờ khác để khám phá sức mạnh của tình bạn và ý chí kiên cường.

Bài học cho bé

• Sự sẻ chia và thấu hiểu: Bé học được giá trị của sự đoàn kết, biết giúp đỡ và cùng những người bạn mới vượt qua nghịch cảnh.

• Trân quý tài nguyên thiên nhiên: Nâng cao ý thức về biến đổi khí hậu, giúp các bạn nhỏ hiểu và yêu hơn môi trường sống của các loài động vật nơi cực lạnh.

———— 

GIỚI THIỆU VỀ SÂN KHẤU BAN MAI

Sân khấu Ban Mai - Nơi nuôi dưỡng tâm hồn trẻ thơ, khơi nguồn sáng tạo và gắn kết gia đình.

Hãy đến với Sân khấu Ban Mai để cùng các bé tận hưởng những giây phút tuyệt vời bên gia đình!

LƯU Ý:

• Quý khán giả vui lòng chờ trong giây lát để trang web tải Lịch diễn và Vé

• Vở kịch phù hợp với các bé từ 3-16 tuổi và những ai yêu thích nghệ thuật.

• Khi vào sân khấu xem kịch KHÔNG mang đồ ăn hoặc nước uống vào trong (ngoại trừ nước suối )!

Ban Mai trân trọng cám ơn!',
    'https://salt.tkbcdn.com/ts/ds/fc/26/61/6c71baf415b6cd8f2655aebdc2d6dfb2.png',
    @NOW - INTERVAL 1 DAY,
    @NOW + INTERVAL 13 DAY + INTERVAL 11 HOUR,
    @NOW + INTERVAL 13 DAY + INTERVAL 15 HOUR,
    'PUBLISHED', @NOW, @NOW
WHERE NOT EXISTS (
    SELECT 1 FROM events e WHERE e.title = 'SÂN KHẤU BAN MAI - VỞ NHẠC KỊCH THIẾU NHI: PENNY - Ô MÊ LY'
);

-- [19/20] TOUR ĐÊM VĂN MIẾU - VAN MIEU NIGHT TOUR (Ticketbox #23759)
INSERT INTO events (organizer_id, category_id, venue_id, title, description, banner_url, sales_start_time, start_time, end_time, status, created_at, updated_at)
SELECT
    (SELECT id FROM users WHERE email = 'organizer19@sportslot.test' LIMIT 1),
    (SELECT id FROM categories WHERE slug = 'tham-quan-trai-nghiem' LIMIT 1),
    (SELECT id FROM venues WHERE name = 'Văn Miếu - Quốc Tử Giám' LIMIT 1),
    'TOUR ĐÊM VĂN MIẾU - VAN MIEU NIGHT TOUR',
    'Giới thiệu sự kiện:

Tham quan Văn Miếu Đêm

Một không gian vừa mộc mạc, vừa quen thuộc nhưng lại ấn tượng về đêm với sắc thái ngàn năm văn hiến. Nhấn mạnh vào hành trình học, trở thành những bậc kỳ tài của đất nước, không gian Văn Miếu - Quốc Tử Giám về đêm được tái hiện toàn cảnh một môi trường đơn sơ về học tập thời xưa, trong trang sử 1000 năm trước, nơi hệ thống giáo dục phần lớn do các Thầy Đồ đảm nhiệm. Đến với tour đêm Văn Miếu, bạn sẽ có cái nhìn toàn cảnh về nền quốc học Việt Nam, nơi mỗi người không chỉ học cái chữ mà còn học để khám phá ra cái chân - thiện - mỹ đã có sẵn nơi mình.

Hành trình sẽ đi qua các phần: lớn lên với lời ru của mẹ, lời dạy của cha; và hành trình trải nghiệm con chữ qua công nghệ mới thực tế ảo; điểm nhấn quan trọng trước khi bước vào không gian học tập thời xưa là trò chuyện với cụ rùa qua công nghệ AI, với những câu hỏi từ đơn giản đến sâu sắc, "cụ" sẽ trả lời cho mọi người các thông tin hết sức hữu ích về Văn Miếu - Quốc Tử Giám và lịch sử Việt Nam.

Một trong những điểm nhấn chính là không gian học tập thời xưa với Thầy Đồ, với bút mực, giấy dó... nhằm cho khách tham quan cảm nhận được không khí của những nhân tài thời xưa phải trải qua hành trình học tập như thế nào. Chương trình kết lại bằng trải nghiệm xem trình chiếu 3D mapping "Tinh hoa đạo học", "Sử đá lưu danh", làm sáng lên toàn bộ cái tinh hoa của học tập thời đó.

Chi tiết trải nghiệm:

• Chương trình chính: Chương trình nghệ thuật ánh sáng trên nền di tích Văn Miếu Quốc Tử Giám. Với các hoạt động trải nghệm thực tế với thầy Đồ, với bia đá và giao lưu với Cụ Rùa Thông Thái.

• Trải nghiệm đặc biệt: Làm nho sinh, trải nghiệm 3d Mapping, Những câu chuyện thú vị thông qua công nghệ ánh sáng. Trải nghệm VR tại chương trình. 

Lịch trải nghiệm: Thứ 4, thứ 7 và chủ nhật.

Thời gian mở cửa đón khách: 18h30.

Thời gian trình chiếu 3D mapping: Tinh hoa đạo học lúc 19h45 và Sử đá lưu danh lúc 20h45

Quý khách đặt vé và lựa chọn chương trình trải nghiệm yêu thích cho hành trình trải nghiệm thú vị tại tour đêm Văn Miếu.

Lưu ý: Trong trường hợp tour đêm Văn Miếu - Quốc Tử Giám phải hoãn các chương trình vì lý do bất khả kháng (Thời tiết xấu hay vì lý do khác), quý khách đã mua vé được liên hệ đổi sang thời gian khác phù hợp. Vé sẽ không được hoàn trả vì bất kỳ lý do nào.',
    'https://salt.tkbcdn.com/ts/ds/47/19/34/ab4d5359e79aa5204c3a8376f0c96747.png',
    @NOW - INTERVAL 1 DAY,
    @NOW + INTERVAL 3 DAY + INTERVAL 11 HOUR,
    @NOW + INTERVAL 3 DAY + INTERVAL 15 HOUR,
    'PUBLISHED', @NOW, @NOW
WHERE NOT EXISTS (
    SELECT 1 FROM events e WHERE e.title = 'TOUR ĐÊM VĂN MIẾU - VAN MIEU NIGHT TOUR'
);

-- [20/20] Mystic Night Show - Magic & Comedy in Saigon (Ticketbox #26644)
INSERT INTO events (organizer_id, category_id, venue_id, title, description, banner_url, sales_start_time, start_time, end_time, status, created_at, updated_at)
SELECT
    (SELECT id FROM users WHERE email = 'organizer20@sportslot.test' LIMIT 1),
    (SELECT id FROM categories WHERE slug = 'san-khau-nghe-thuat' LIMIT 1),
    (SELECT id FROM venues WHERE name = 'Mystic Night Show' LIMIT 1),
    'Mystic Night Show - Magic & Comedy in Saigon',
    'The Mystic Night Show is one of Saigon’s most unique, interactive evening experiences, blending modern magic, stand-up comedy, and traditional Vietnamese folklore into a highly engaging live performance. Maintained as an intimate gathering rather than a large theatrical production, this show offers an up-close look at world-class magic right in the heart of Ho Chi Minh City.

The performance is led by Petey Majik Nguyen, a highly acclaimed international magician and actor. Rather than relying on standard, passive illusions, the show follows a cohesive journey inspired by a famous Vietnamese tale.

Show details:

• Happening every Friday at 8PM

• Suitable for children age 6 or older

• Show language is English

• Limited to 20 seats per show',
    'https://salt.tkbcdn.com/ts/ds/3f/af/2c/9d5c5809600713a14a9ffc9e4113cba9.png',
    @NOW - INTERVAL 1 DAY,
    @NOW + INTERVAL 2 DAY + INTERVAL 11 HOUR,
    @NOW + INTERVAL 2 DAY + INTERVAL 15 HOUR,
    'PUBLISHED', @NOW, @NOW
WHERE NOT EXISTS (
    SELECT 1 FROM events e WHERE e.title = 'Mystic Night Show - Magic & Comedy in Saigon'
);

-- =====================================================================
-- 5) Các hạng vé thực tế tương ứng cho từng sự kiện (Ticket Types)
-- =====================================================================
-- Hạng vé cho sự kiện [1/20]: KOSMIK LIVE CONCERT 2026 IN HANOI: HOMECOMING
INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'NGƯỜI VIỆT 1 (SEATED)',
    10000000.00,
    50,
    0,
    6,
    @NOW, @NOW
FROM events e
WHERE e.title = 'KOSMIK LIVE CONCERT 2026 IN HANOI: HOMECOMING'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'NGƯỜI VIỆT 1 (SEATED)'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'NGƯỜI VIỆT 2 (SEATED)',
    10000000.00,
    50,
    0,
    6,
    @NOW, @NOW
FROM events e
WHERE e.title = 'KOSMIK LIVE CONCERT 2026 IN HANOI: HOMECOMING'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'NGƯỜI VIỆT 2 (SEATED)'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'SPACESPEAKERS (SEATED)',
    8000000.00,
    50,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'KOSMIK LIVE CONCERT 2026 IN HANOI: HOMECOMING'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'SPACESPEAKERS (SEATED)'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'HANOI FLOW 1 (SEATED)',
    3500000.00,
    150,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'KOSMIK LIVE CONCERT 2026 IN HANOI: HOMECOMING'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'HANOI FLOW 1 (SEATED)'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'HANOI FLOW 2 (SEATED)',
    3500000.00,
    150,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'KOSMIK LIVE CONCERT 2026 IN HANOI: HOMECOMING'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'HANOI FLOW 2 (SEATED)'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'PHỐ CỔ 1 (SEATED)',
    2000000.00,
    150,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'KOSMIK LIVE CONCERT 2026 IN HANOI: HOMECOMING'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'PHỐ CỔ 1 (SEATED)'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'PHỐ CỔ 2 (SEATED)',
    2000000.00,
    150,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'KOSMIK LIVE CONCERT 2026 IN HANOI: HOMECOMING'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'PHỐ CỔ 2 (SEATED)'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'NEM CHUA RÁN 1 (SEATED)',
    1500000.00,
    150,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'KOSMIK LIVE CONCERT 2026 IN HANOI: HOMECOMING'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'NEM CHUA RÁN 1 (SEATED)'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'NEM CHUA RÁN 2 (SEATED)',
    1500000.00,
    150,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'KOSMIK LIVE CONCERT 2026 IN HANOI: HOMECOMING'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'NEM CHUA RÁN 2 (SEATED)'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'TRÀ ĐÁ 1 (SEATED)',
    1500000.00,
    150,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'KOSMIK LIVE CONCERT 2026 IN HANOI: HOMECOMING'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'TRÀ ĐÁ 1 (SEATED)'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'TRÀ ĐÁ 2 (SEATED)',
    1500000.00,
    150,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'KOSMIK LIVE CONCERT 2026 IN HANOI: HOMECOMING'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'TRÀ ĐÁ 2 (SEATED)'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'HOME 1 (STANDING)',
    2200000.00,
    150,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'KOSMIK LIVE CONCERT 2026 IN HANOI: HOMECOMING'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'HOME 1 (STANDING)'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'HOME 2 (STANDING)',
    2200000.00,
    150,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'KOSMIK LIVE CONCERT 2026 IN HANOI: HOMECOMING'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'HOME 2 (STANDING)'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'HOME 3 (STANDING)',
    2200000.00,
    150,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'KOSMIK LIVE CONCERT 2026 IN HANOI: HOMECOMING'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'HOME 3 (STANDING)'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'HOME 4 (STANDING)',
    2200000.00,
    150,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'KOSMIK LIVE CONCERT 2026 IN HANOI: HOMECOMING'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'HOME 4 (STANDING)'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'HOA SỮA 1 (STANDING)',
    1500000.00,
    150,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'KOSMIK LIVE CONCERT 2026 IN HANOI: HOMECOMING'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'HOA SỮA 1 (STANDING)'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'HOA SỮA 2 (STANDING)',
    1500000.00,
    150,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'KOSMIK LIVE CONCERT 2026 IN HANOI: HOMECOMING'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'HOA SỮA 2 (STANDING)'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'HƯƠNG NGỌC LAN 1 (STANDING)',
    1000000.00,
    150,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'KOSMIK LIVE CONCERT 2026 IN HANOI: HOMECOMING'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'HƯƠNG NGỌC LAN 1 (STANDING)'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'HƯƠNG NGỌC LAN 2 (STANDING)',
    1000000.00,
    150,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'KOSMIK LIVE CONCERT 2026 IN HANOI: HOMECOMING'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'HƯƠNG NGỌC LAN 2 (STANDING)'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'BÂNG KHUÂNG 1 (STANDING)',
    800000.00,
    300,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'KOSMIK LIVE CONCERT 2026 IN HANOI: HOMECOMING'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'BÂNG KHUÂNG 1 (STANDING)'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'BÂNG KHUÂNG 2 (STANDING)',
    800000.00,
    300,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'KOSMIK LIVE CONCERT 2026 IN HANOI: HOMECOMING'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'BÂNG KHUÂNG 2 (STANDING)'
  );

-- Hạng vé cho sự kiện [2/20]: MẮT NHẮM MẮT MỞ CONCERT - DAY 3 (HÀ NỘI)
INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'GÁC MÁI',
    12000000.00,
    50,
    0,
    4,
    @NOW, @NOW
FROM events e
WHERE e.title = 'MẮT NHẮM MẮT MỞ CONCERT - DAY 3 (HÀ NỘI)'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'GÁC MÁI'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'BAN CÔNG A',
    4000000.00,
    150,
    0,
    4,
    @NOW, @NOW
FROM events e
WHERE e.title = 'MẮT NHẮM MẮT MỞ CONCERT - DAY 3 (HÀ NỘI)'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'BAN CÔNG A'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'BAN CÔNG B',
    4000000.00,
    150,
    0,
    4,
    @NOW, @NOW
FROM events e
WHERE e.title = 'MẮT NHẮM MẮT MỞ CONCERT - DAY 3 (HÀ NỘI)'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'BAN CÔNG B'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'TÂM TRÍ A',
    3000000.00,
    150,
    0,
    4,
    @NOW, @NOW
FROM events e
WHERE e.title = 'MẮT NHẮM MẮT MỞ CONCERT - DAY 3 (HÀ NỘI)'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'TÂM TRÍ A'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'TÂM TRÍ B',
    3000000.00,
    150,
    0,
    4,
    @NOW, @NOW
FROM events e
WHERE e.title = 'MẮT NHẮM MẮT MỞ CONCERT - DAY 3 (HÀ NỘI)'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'TÂM TRÍ B'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'HÀNH LANG A',
    2800000.00,
    150,
    0,
    4,
    @NOW, @NOW
FROM events e
WHERE e.title = 'MẮT NHẮM MẮT MỞ CONCERT - DAY 3 (HÀ NỘI)'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'HÀNH LANG A'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'HÀNH LANG B',
    2800000.00,
    150,
    0,
    4,
    @NOW, @NOW
FROM events e
WHERE e.title = 'MẮT NHẮM MẮT MỞ CONCERT - DAY 3 (HÀ NỘI)'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'HÀNH LANG B'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'SÂN TRƯỚC A',
    2000000.00,
    150,
    0,
    4,
    @NOW, @NOW
FROM events e
WHERE e.title = 'MẮT NHẮM MẮT MỞ CONCERT - DAY 3 (HÀ NỘI)'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'SÂN TRƯỚC A'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'SÂN TRƯỚC B',
    2000000.00,
    150,
    0,
    4,
    @NOW, @NOW
FROM events e
WHERE e.title = 'MẮT NHẮM MẮT MỞ CONCERT - DAY 3 (HÀ NỘI)'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'SÂN TRƯỚC B'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'MÁI NHÀ A',
    1600000.00,
    150,
    0,
    4,
    @NOW, @NOW
FROM events e
WHERE e.title = 'MẮT NHẮM MẮT MỞ CONCERT - DAY 3 (HÀ NỘI)'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'MÁI NHÀ A'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'MÁI NHÀ B',
    1600000.00,
    150,
    0,
    4,
    @NOW, @NOW
FROM events e
WHERE e.title = 'MẮT NHẮM MẮT MỞ CONCERT - DAY 3 (HÀ NỘI)'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'MÁI NHÀ B'
  );

-- Hạng vé cho sự kiện [3/20]: [TP.HCM] Những Thành Phố Mơ Màng Year End 2026
INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'GENERAL TICKET (FINAL ROUND)',
    899000.00,
    300,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = '[TP.HCM] Những Thành Phố Mơ Màng Year End 2026'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'GENERAL TICKET (FINAL ROUND)'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'GENERAL TICKET (OPENING ROUND)',
    499000.00,
    300,
    0,
    2,
    @NOW, @NOW
FROM events e
WHERE e.title = '[TP.HCM] Những Thành Phố Mơ Màng Year End 2026'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'GENERAL TICKET (OPENING ROUND)'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'GENERAL TICKET (SPOTLIGHT ROUND)',
    755000.00,
    300,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = '[TP.HCM] Những Thành Phố Mơ Màng Year End 2026'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'GENERAL TICKET (SPOTLIGHT ROUND)'
  );

-- Hạng vé cho sự kiện [4/20]: [Hà Nội] Những Thành Phố Mơ Màng Year End 2026
INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'GENERAL TICKET (FINAL ROUND)',
    899000.00,
    300,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = '[Hà Nội] Những Thành Phố Mơ Màng Year End 2026'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'GENERAL TICKET (FINAL ROUND)'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'GENERAL TICKET (OPENING ROUND)',
    499000.00,
    300,
    0,
    2,
    @NOW, @NOW
FROM events e
WHERE e.title = '[Hà Nội] Những Thành Phố Mơ Màng Year End 2026'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'GENERAL TICKET (OPENING ROUND)'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'GERERAL TICKET (SPOTLIGHT ROUND)',
    755000.00,
    300,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = '[Hà Nội] Những Thành Phố Mơ Màng Year End 2026'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'GERERAL TICKET (SPOTLIGHT ROUND)'
  );

-- Hạng vé cho sự kiện [5/20]: LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN
INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'SKYNOTE 1',
    8800000.00,
    50,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'SKYNOTE 1'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'SKYNOTE 2',
    8800000.00,
    50,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'SKYNOTE 2'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'SKYNOTE 3',
    8800000.00,
    50,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'SKYNOTE 3'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN THANH 1',
    6800000.00,
    50,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN THANH 1'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN THANH 2',
    6800000.00,
    50,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN THANH 2'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN THANH 3',
    6800000.00,
    50,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN THANH 3'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN NGỌC 1',
    4600000.00,
    150,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN NGỌC 1'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN NGỌC 2',
    4600000.00,
    150,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN NGỌC 2'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN NGỌC 3',
    4600000.00,
    150,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN NGỌC 3'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN NGỌC 4',
    4600000.00,
    150,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN NGỌC 4'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN NGỌC 5',
    4600000.00,
    150,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN NGỌC 5'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN NGỌC 6',
    4600000.00,
    150,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN NGỌC 6'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN KIM 1',
    3300000.00,
    150,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN KIM 1'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN KIM 2',
    3300000.00,
    150,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN KIM 2'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN KIM 3',
    3300000.00,
    150,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN KIM 3'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN KIM 4',
    3300000.00,
    150,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN KIM 4'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN KIM 5',
    3300000.00,
    150,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN KIM 5'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN KIM 6',
    3300000.00,
    150,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN KIM 6'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN KIM 7',
    3300000.00,
    150,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN KIM 7'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN MỘC 1',
    1800000.00,
    150,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN MỘC 1'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN MỘC 2',
    1800000.00,
    150,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN MỘC 2'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN MỘC 3',
    1800000.00,
    150,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN MỘC 3'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN MỘC 4',
    1800000.00,
    150,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN MỘC 4'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN MỘC 5',
    1800000.00,
    150,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN MỘC 5'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN NHIÊN 1',
    880000.00,
    300,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN NHIÊN 1'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN NHIÊN 2',
    880000.00,
    300,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN NHIÊN 2'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN NHIÊN 3',
    880000.00,
    300,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN NHIÊN 3'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN NHIÊN 4',
    880000.00,
    300,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN NHIÊN 4'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN NHIÊN 5',
    880000.00,
    300,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN NHIÊN 5'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN NHIÊN 6',
    880000.00,
    300,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN NHIÊN 6'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN NHIÊN 7',
    880000.00,
    300,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN NHIÊN 7'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN NHIÊN 8',
    880000.00,
    300,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN NHIÊN 8'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN NHIÊN 9',
    880000.00,
    300,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN NHIÊN 9'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN NHIÊN 10',
    880000.00,
    300,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN NHIÊN 10'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN NHIÊN 11',
    880000.00,
    300,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN NHIÊN 11'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THIÊN NHIÊN 12',
    880000.00,
    300,
    0,
    8,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LIVE CONCERT - SKYNOTE THE REFLECTION DAY 2 - QUỐC THIÊN'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THIÊN NHIÊN 12'
  );

-- Hạng vé cho sự kiện [6/20]: Mr Siro - Encore Extended - Ai Cũng Giấu Trong Lòng Tảng Băng - Hà Nội
INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'SVIP - Em',
    5000000.00,
    50,
    0,
    6,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Mr Siro - Encore Extended - Ai Cũng Giấu Trong Lòng Tảng Băng - Hà Nội'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'SVIP - Em'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'VVIP - Một Bước Yêu Vạn Dặm Đau',
    3000000.00,
    150,
    0,
    6,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Mr Siro - Encore Extended - Ai Cũng Giấu Trong Lòng Tảng Băng - Hà Nội'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'VVIP - Một Bước Yêu Vạn Dặm Đau'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'VIP - Day Dứt Nỗi Đau',
    2800000.00,
    150,
    0,
    6,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Mr Siro - Encore Extended - Ai Cũng Giấu Trong Lòng Tảng Băng - Hà Nội'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'VIP - Day Dứt Nỗi Đau'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'Dưới Những Cơn Mưa',
    2400000.00,
    150,
    0,
    6,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Mr Siro - Encore Extended - Ai Cũng Giấu Trong Lòng Tảng Băng - Hà Nội'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'Dưới Những Cơn Mưa'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'Vô Hình Trong Tim Em',
    2000000.00,
    150,
    0,
    6,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Mr Siro - Encore Extended - Ai Cũng Giấu Trong Lòng Tảng Băng - Hà Nội'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'Vô Hình Trong Tim Em'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'Lắng Nghe Nước Mắt',
    1600000.00,
    150,
    0,
    6,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Mr Siro - Encore Extended - Ai Cũng Giấu Trong Lòng Tảng Băng - Hà Nội'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'Lắng Nghe Nước Mắt'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'Khóc Cùng Em 1',
    1500000.00,
    150,
    0,
    6,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Mr Siro - Encore Extended - Ai Cũng Giấu Trong Lòng Tảng Băng - Hà Nội'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'Khóc Cùng Em 1'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'Khóc Cùng Em 2',
    1200000.00,
    150,
    0,
    6,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Mr Siro - Encore Extended - Ai Cũng Giấu Trong Lòng Tảng Băng - Hà Nội'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'Khóc Cùng Em 2'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'Khóc Cùng Em 3',
    800000.00,
    300,
    0,
    6,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Mr Siro - Encore Extended - Ai Cũng Giấu Trong Lòng Tảng Băng - Hà Nội'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'Khóc Cùng Em 3'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'Vé Mời',
    3200000.00,
    150,
    0,
    1,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Mr Siro - Encore Extended - Ai Cũng Giấu Trong Lòng Tảng Băng - Hà Nội'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'Vé Mời'
  );

-- Hạng vé cho sự kiện [7/20]: THE BROTHERS | Đỗ Hoàng Hiệp x Tăng Phúc x Hà Lê x Cheng x Bình Văn Band
INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    '1st Row',
    3000000.00,
    150,
    0,
    4,
    @NOW, @NOW
FROM events e
WHERE e.title = 'THE BROTHERS | Đỗ Hoàng Hiệp x Tăng Phúc x Hà Lê x Cheng x Bình Văn Band'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = '1st Row'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    '1st Row (L&R)',
    2900000.00,
    150,
    0,
    4,
    @NOW, @NOW
FROM events e
WHERE e.title = 'THE BROTHERS | Đỗ Hoàng Hiệp x Tăng Phúc x Hà Lê x Cheng x Bình Văn Band'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = '1st Row (L&R)'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'RED',
    2700000.00,
    150,
    0,
    4,
    @NOW, @NOW
FROM events e
WHERE e.title = 'THE BROTHERS | Đỗ Hoàng Hiệp x Tăng Phúc x Hà Lê x Cheng x Bình Văn Band'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'RED'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'YELLOW',
    2400000.00,
    150,
    0,
    4,
    @NOW, @NOW
FROM events e
WHERE e.title = 'THE BROTHERS | Đỗ Hoàng Hiệp x Tăng Phúc x Hà Lê x Cheng x Bình Văn Band'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'YELLOW'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'PINK',
    2000000.00,
    150,
    0,
    4,
    @NOW, @NOW
FROM events e
WHERE e.title = 'THE BROTHERS | Đỗ Hoàng Hiệp x Tăng Phúc x Hà Lê x Cheng x Bình Văn Band'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'PINK'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'GREEN',
    1800000.00,
    150,
    0,
    4,
    @NOW, @NOW
FROM events e
WHERE e.title = 'THE BROTHERS | Đỗ Hoàng Hiệp x Tăng Phúc x Hà Lê x Cheng x Bình Văn Band'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'GREEN'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'BLUE',
    1300000.00,
    150,
    0,
    4,
    @NOW, @NOW
FROM events e
WHERE e.title = 'THE BROTHERS | Đỗ Hoàng Hiệp x Tăng Phúc x Hà Lê x Cheng x Bình Văn Band'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'BLUE'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'PURPLE',
    1000000.00,
    150,
    0,
    4,
    @NOW, @NOW
FROM events e
WHERE e.title = 'THE BROTHERS | Đỗ Hoàng Hiệp x Tăng Phúc x Hà Lê x Cheng x Bình Văn Band'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'PURPLE'
  );

-- Hạng vé cho sự kiện [8/20]: Whisper of Two Hearts [Hoàng Hải - Phương Linh]
INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'RUNG ĐỘNG',
    1500000.00,
    150,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Whisper of Two Hearts [Hoàng Hải - Phương Linh]'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'RUNG ĐỘNG'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'NỒNG NÀN',
    2000000.00,
    150,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Whisper of Two Hearts [Hoàng Hải - Phương Linh]'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'NỒNG NÀN'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'SAY ĐẮM',
    2500000.00,
    150,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Whisper of Two Hearts [Hoàng Hải - Phương Linh]'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'SAY ĐẮM'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'THĂNG HOA',
    3000000.00,
    150,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Whisper of Two Hearts [Hoàng Hải - Phương Linh]'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'THĂNG HOA'
  );

-- Hạng vé cho sự kiện [9/20]: [CAT&MOUSE] THÀNH VÁ
INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'STANDARD 1',
    600000.00,
    300,
    0,
    3,
    @NOW, @NOW
FROM events e
WHERE e.title = '[CAT&MOUSE] THÀNH VÁ'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'STANDARD 1'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'VIP',
    800000.00,
    300,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = '[CAT&MOUSE] THÀNH VÁ'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'VIP'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'SUPER VIP',
    1000000.00,
    150,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = '[CAT&MOUSE] THÀNH VÁ'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'SUPER VIP'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'STANDARD 2',
    500000.00,
    300,
    0,
    5,
    @NOW, @NOW
FROM events e
WHERE e.title = '[CAT&MOUSE] THÀNH VÁ'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'STANDARD 2'
  );

-- Hạng vé cho sự kiện [10/20]: Lễ Hội Bia Thủ Công
INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'Lễ Hội Bia Thủ Công - Harvest Beer Fest 2026',
    1299000.00,
    150,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Lễ Hội Bia Thủ Công'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'Lễ Hội Bia Thủ Công - Harvest Beer Fest 2026'
  );

-- Hạng vé cho sự kiện [11/20]: LCP 2026 Promotion Series
INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'Regular',
    199000.00,
    300,
    0,
    4,
    @NOW, @NOW
FROM events e
WHERE e.title = 'LCP 2026 Promotion Series'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'Regular'
  );

-- Hạng vé cho sự kiện [12/20]: Queens Of Glory - Giải Thể Thao Điện Tử Vinh Quang Nữ Hoàng
INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'Regular',
    199000.00,
    300,
    0,
    2,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Queens Of Glory - Giải Thể Thao Điện Tử Vinh Quang Nữ Hoàng'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'Regular'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'VIP',
    299000.00,
    300,
    0,
    2,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Queens Of Glory - Giải Thể Thao Điện Tử Vinh Quang Nữ Hoàng'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'VIP'
  );

-- Hạng vé cho sự kiện [13/20]: Hà Nội - Eifman Ballet Of ST. Petersubrg World Tour 2026
INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'SVIP 1',
    20000000.00,
    50,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Hà Nội - Eifman Ballet Of ST. Petersubrg World Tour 2026'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'SVIP 1'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'SVIP 2',
    7000000.00,
    50,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Hà Nội - Eifman Ballet Of ST. Petersubrg World Tour 2026'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'SVIP 2'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'VIP 1',
    5000000.00,
    50,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Hà Nội - Eifman Ballet Of ST. Petersubrg World Tour 2026'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'VIP 1'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'VIP 2',
    3000000.00,
    150,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Hà Nội - Eifman Ballet Of ST. Petersubrg World Tour 2026'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'VIP 2'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'STANDARD 1',
    1000000.00,
    150,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Hà Nội - Eifman Ballet Of ST. Petersubrg World Tour 2026'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'STANDARD 1'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'STANDARD 2',
    500000.00,
    300,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Hà Nội - Eifman Ballet Of ST. Petersubrg World Tour 2026'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'STANDARD 2'
  );

-- Hạng vé cho sự kiện [14/20]: Hồ Chí Minh - Eifman Ballet Of ST. Petersubrg World Tour 2026
INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'SVIP 1',
    20000000.00,
    50,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Hồ Chí Minh - Eifman Ballet Of ST. Petersubrg World Tour 2026'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'SVIP 1'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'SVIP 2',
    7000000.00,
    50,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Hồ Chí Minh - Eifman Ballet Of ST. Petersubrg World Tour 2026'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'SVIP 2'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'VIP 1',
    5000000.00,
    50,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Hồ Chí Minh - Eifman Ballet Of ST. Petersubrg World Tour 2026'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'VIP 1'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'VIP 2',
    3000000.00,
    150,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Hồ Chí Minh - Eifman Ballet Of ST. Petersubrg World Tour 2026'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'VIP 2'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'STANDARD 1',
    1000000.00,
    150,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Hồ Chí Minh - Eifman Ballet Of ST. Petersubrg World Tour 2026'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'STANDARD 1'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'STANDARD 2',
    500000.00,
    300,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Hồ Chí Minh - Eifman Ballet Of ST. Petersubrg World Tour 2026'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'STANDARD 2'
  );

-- Hạng vé cho sự kiện [15/20]: Nhà Hát Kịch IDECAF: Dưới Bóng Giai Nhân
INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'HẠNG VIP (không dành cho trẻ em dưới 12 tuổi)',
    350000.00,
    300,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Nhà Hát Kịch IDECAF: Dưới Bóng Giai Nhân'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'HẠNG VIP (không dành cho trẻ em dưới 12 tuổi)'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'HẠNG REGULAR (không dành cho trẻ dưới 12 tuổi)',
    300000.00,
    300,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Nhà Hát Kịch IDECAF: Dưới Bóng Giai Nhân'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'HẠNG REGULAR (không dành cho trẻ dưới 12 tuổi)'
  );

-- Hạng vé cho sự kiện [16/20]: Nhà Hát Kịch IDECAF: Hồn Ai Nấy Giữ
INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'Hạng VIP (Không dành cho trẻ em dưới 12 tuổi)',
    350000.00,
    300,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Nhà Hát Kịch IDECAF: Hồn Ai Nấy Giữ'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'Hạng VIP (Không dành cho trẻ em dưới 12 tuổi)'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'Hạng Regular (Không dành cho trẻ em dưới 12 tuổi)',
    300000.00,
    300,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Nhà Hát Kịch IDECAF: Hồn Ai Nấy Giữ'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'Hạng Regular (Không dành cho trẻ em dưới 12 tuổi)'
  );

-- Hạng vé cho sự kiện [17/20]: Sân khấu Trương Hùng Minh: Song Trùng
INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'VIP',
    350000.00,
    300,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Sân khấu Trương Hùng Minh: Song Trùng'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'VIP'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'Thường',
    300000.00,
    300,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Sân khấu Trương Hùng Minh: Song Trùng'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'Thường'
  );

-- Hạng vé cho sự kiện [18/20]: SÂN KHẤU BAN MAI - VỞ NHẠC KỊCH THIẾU NHI: PENNY - Ô MÊ LY
INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'Ban Mai',
    350000.00,
    300,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'SÂN KHẤU BAN MAI - VỞ NHẠC KỊCH THIẾU NHI: PENNY - Ô MÊ LY'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'Ban Mai'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'Sương Sớm',
    260000.00,
    300,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'SÂN KHẤU BAN MAI - VỞ NHẠC KỊCH THIẾU NHI: PENNY - Ô MÊ LY'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'Sương Sớm'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'Mây Trắng',
    190000.00,
    300,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'SÂN KHẤU BAN MAI - VỞ NHẠC KỊCH THIẾU NHI: PENNY - Ô MÊ LY'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'Mây Trắng'
  );

-- Hạng vé cho sự kiện [19/20]: TOUR ĐÊM VĂN MIẾU - VAN MIEU NIGHT TOUR
INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'Tinh hoa đạo học - Vé người lớn',
    299000.00,
    300,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'TOUR ĐÊM VĂN MIẾU - VAN MIEU NIGHT TOUR'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'Tinh hoa đạo học - Vé người lớn'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'Tinh hoa đạo học - Vé trẻ em cao 1m - 1.3m',
    149000.00,
    300,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'TOUR ĐÊM VĂN MIẾU - VAN MIEU NIGHT TOUR'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'Tinh hoa đạo học - Vé trẻ em cao 1m - 1.3m'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'Sử đá lưu danh',
    299000.00,
    300,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'TOUR ĐÊM VĂN MIẾU - VAN MIEU NIGHT TOUR'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'Sử đá lưu danh'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'Sử đá lưu danh - Vé Trẻ em 1m - 1,3m',
    149000.00,
    300,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'TOUR ĐÊM VĂN MIẾU - VAN MIEU NIGHT TOUR'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'Sử đá lưu danh - Vé Trẻ em 1m - 1,3m'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'COMBO 2 show',
    399000.00,
    300,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'TOUR ĐÊM VĂN MIẾU - VAN MIEU NIGHT TOUR'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'COMBO 2 show'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'COMBO 2 show - Vé trẻ em cao 1m - 1.3m',
    199000.00,
    300,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'TOUR ĐÊM VĂN MIẾU - VAN MIEU NIGHT TOUR'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'COMBO 2 show - Vé trẻ em cao 1m - 1.3m'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'Vé trẻ em cao dưới 1m',
    0.00,
    300,
    0,
    1,
    @NOW, @NOW
FROM events e
WHERE e.title = 'TOUR ĐÊM VĂN MIẾU - VAN MIEU NIGHT TOUR'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'Vé trẻ em cao dưới 1m'
  );

-- Hạng vé cho sự kiện [20/20]: Mystic Night Show - Magic & Comedy in Saigon
INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'Adult Ticket',
    1000000.00,
    150,
    0,
    10,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Mystic Night Show - Magic & Comedy in Saigon'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'Adult Ticket'
  );

INSERT INTO ticket_types (event_id, name, price, total_quantity, sold_quantity, max_per_order, created_at, updated_at)
SELECT
    e.id,
    'Child Ticket (6-15 years)',
    850000.00,
    300,
    0,
    5,
    @NOW, @NOW
FROM events e
WHERE e.title = 'Mystic Night Show - Magic & Comedy in Saigon'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_types tt WHERE tt.event_id = e.id AND tt.name = 'Child Ticket (6-15 years)'
  );

DROP TEMPORARY TABLE IF EXISTS temp_seed_categories, temp_seed_organizers, temp_seed_venues;
COMMIT;
SET SESSION time_zone = @OLD_TIME_ZONE;

-- =====================================================================
-- Thống kê tổng số bản ghi sau khi seed:
-- =====================================================================
SELECT 'organizers' AS table_name, COUNT(*) AS total FROM users WHERE role = 'ORGANIZER'
UNION ALL SELECT 'categories', COUNT(*) FROM categories
UNION ALL SELECT 'venues', COUNT(*) FROM venues
UNION ALL SELECT 'events', COUNT(*) FROM events
UNION ALL SELECT 'ticket_types', COUNT(*) FROM ticket_types;
