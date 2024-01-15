-- tamnaju_db 스키마 생성
CREATE SCHEMA IF NOT EXISTS `tamnaju_db`;
CREATE TABLE IF NOT EXISTS `tamnaju_db`.`users` (
    `email` VARCHAR(50) NOT NULL,
    `name` VARCHAR(20) NOT NULL,
    `nickname` VARCHAR(20) NOT NULL,
    `password` VARCHAR(60) NULL,
    `birth` DATE NOT NULL,
    `admin_flag` BOOLEAN NOT NULL DEFAULT FALSE,
    `registered_at` DATETIME NOT NULL DEFAULT NOW (),
    `deleted_at` DATETIME NULL DEFAULT NULL,
    `suspended_at` DATETIME NULL DEFAULT NULL,
    `provider` VARCHAR(10) NULL,
    `providerId` VARCHAR(20) NULL,
    CONSTRAINT PRIMARY KEY (`email`)
);