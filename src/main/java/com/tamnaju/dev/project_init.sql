-- tamnaju_db 스키마 생성
CREATE SCHEMA IF NOT EXISTS `tamnaju_db`;

CREATE TABLE IF NOT EXISTS `tamnaju_db`.`users` (
    `id` VARCHAR(32) NOT NULL,
    `email` VARCHAR(50) NOT NULL,
    `name` VARCHAR(20) NULL,
    `password` VARCHAR(60) NOT NULL,
    `birth` DATE NOT NULL,
    `admin_flag` BOOLEAN NOT NULL DEFAULT FALSE,
    `registered_at` DATETIME NOT NULL DEFAULT NOW (),
    `deleted_at` DATETIME NULL DEFAULT NULL,
    `suspended_at` DATETIME NULL DEFAULT NULL,
    `provider` VARCHAR(10) NULL,
    `provider_id` VARCHAR(20) NULL,
    CONSTRAINT PRIMARY KEY (`email`)
);

DROP TABLE `tamnaju_db`.`users`;

CREATE TABLE IF NOT EXISTS `tamnaju_db`.`signature` (
    `key_byte` VARBINARY(32) NOT NULL,
    `created_at` DATETIME NOT NULL,
    CONSTRAINT PRIMARY KEY (`key_byte`)
);
