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
