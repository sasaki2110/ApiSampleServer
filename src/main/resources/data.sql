DELETE FROM order_line;
DELETE FROM order_header;
DELETE FROM product;
DELETE FROM party;

INSERT INTO party (code, name) VALUES ('1001', '〇〇商事');
INSERT INTO party (code, name) VALUES ('2001', '△△電池工業');
INSERT INTO party (code, name) VALUES ('3001', '関東物流センター');
INSERT INTO party (code, name) VALUES ('1002', '自動車部品ホールディングス');
INSERT INTO party (code, name) VALUES ('2002', '極板製造所横浜');
INSERT INTO party (code, name) VALUES ('3002', '西日本バッテリー販売');
INSERT INTO party (code, name) VALUES ('1003', 'グリーンモビリティ部品');
INSERT INTO party (code, name) VALUES ('2003', '解体リサイクル化工');
INSERT INTO party (code, name) VALUES ('3003', '中部セル流通基地');
INSERT INTO party (code, name) VALUES ('1004', '東洋エネルギー商事');

INSERT INTO product (code, name) VALUES ('B001', 'リチウムセルL型');
INSERT INTO product (code, name) VALUES ('B002', '鉛蓄電池パック');
INSERT INTO product (code, name) VALUES ('B003', '電解液ユニット');
INSERT INTO product (code, name) VALUES ('B004', 'AGMバッテリー12V');
INSERT INTO product (code, name) VALUES ('B005', 'セパレータロール');
INSERT INTO product (code, name) VALUES ('B006', '角形リチウムモジュール');
INSERT INTO product (code, name) VALUES ('B007', 'ジクロマット正極板');
INSERT INTO product (code, name) VALUES ('B008', 'コールドチェーン梱包材');
INSERT INTO product (code, name) VALUES ('B009', '48Vマイルド用補機バッテリー');
INSERT INTO product (code, name) VALUES ('B010', 'リサイクル鉛インゴット');

-- 受注サンプル（一覧・検索確認用）
-- contractPartyCode=1001 かつ dueDate 2026年内: id 1, 2
-- orderNumber 部分一致「60330」: id 1
-- 納期 null（日付レンジではヒットしない）: id 4
INSERT INTO order_header (id, order_number, contract_party_code, delivery_party_code, delivery_location, due_date, forecast_number, created_at) VALUES
(1, 'ORD-20260330-001', '1001', '3001', '第2倉庫', DATE '2026-04-06', 'FC-2026-001', TIMESTAMP '2026-03-30 09:00:00'),
(2, 'ORD-20260331-002', '1001', '2001', NULL, DATE '2026-06-01', NULL, TIMESTAMP '2026-03-31 14:30:00'),
(3, 'ORD-20260215-003', '2001', '3001', '東京港', DATE '2026-02-15', 'FC-2026-010', TIMESTAMP '2026-02-15 11:00:00'),
(4, 'ORD-20260331-099', '1003', '3002', '仮置き場', NULL, NULL, TIMESTAMP '2026-03-31 16:00:00');

INSERT INTO order_line (id, line_no, product_code, product_name, quantity, unit_price, amount, order_header_id) VALUES
(1, 1, 'B001', 'リチウムセルL型', 100, 1500, 150000, 1),
(2, 2, 'B004', 'AGMバッテリー12V', 50, 3200, 160000, 1),
(3, 1, 'B002', '鉛蓄電池パック', 20, 8000, 160000, 2),
(4, 1, 'B003', '電解液ユニット', 200, 500, 100000, 3),
(5, 1, 'B005', 'セパレータロール', 10, 1200, 12000, 4);

ALTER TABLE order_header ALTER COLUMN id RESTART WITH 100;
ALTER TABLE order_line ALTER COLUMN id RESTART WITH 100;
