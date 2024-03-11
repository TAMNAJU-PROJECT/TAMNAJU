-- tamnaju_db 스키마 생성
CREATE SCHEMA IF NOT EXISTS `tamnaju_db`;

CREATE TABLE IF NOT EXISTS `tamnaju_db`.`users` (
    `id` VARCHAR(32) NOT NULL,
    `email` VARCHAR(50) NOT NULL,
    `name` VARCHAR(20) NULL,
    `password` VARCHAR(60) NOT NULL,
    `birth` DATE NOT NULL,
    `admin_flag` BOOLEAN NOT NULL DEFAULT FALSE,
    `registered_at` DATETIME NOT NULL DEFAULT NOW(),
    `deleted_at` DATETIME NULL DEFAULT NULL,
    `suspended_at` DATETIME NULL DEFAULT NULL,
    `provider` VARCHAR(10) NULL,
    `provider_id` VARCHAR(20) NULL,
    CONSTRAINT PRIMARY KEY (`id`),
    CONSTRAINT UNIQUE (`email`)
);

CREATE TABLE IF NOT EXISTS `tamnaju_db`.`notices` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` VARCHAR(32) NOT NULL,
    `title` VARCHAR(100) NOT NULL,
    `content` LONGTEXT NOT NULL,
    `view` BIGINT NOT NULL DEFAULT 0,
    `posted_at` DATETIME NOT NULL DEFAULT NOW(),
    `modified_at` DATETIME NULL DEFAULT NULL,
    `deleted_at` DATETIME NULL DEFAULT NULL,
    CONSTRAINT PRIMARY KEY (`id`),
    CONSTRAINT FOREIGN KEY (`user_id`) REFERENCES `tamnaju_db`.`users` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS `tamnaju_db`.`qnas` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` VARCHAR(32) NOT NULL,
    `title` VARCHAR(100) NOT NULL,
    `content` LONGTEXT NOT NULL,
    `view` BIGINT NOT NULL DEFAULT 0,
    `posted_at` DATETIME NOT NULL DEFAULT NOW(),
    `modified_at` DATETIME NULL DEFAULT NULL,
    `deleted_at` DATETIME NULL DEFAULT NULL,
    CONSTRAINT PRIMARY KEY (`id`),
    CONSTRAINT FOREIGN KEY (`user_id`) REFERENCES `tamnaju_db`.`users` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

INSERT INTO `tamnaju_db`.`notices` (`user_id`, `title`, `content`)
VALUES ('shuffler54', 'title', 'content'),
       ('shuffler54', 'title', 'content'),
       ('shuffler54', 'title', 'content'),
       ('shuffler54', 'title', 'content'),
       ('shuffler54', 'title', 'content'),
       ('shuffler54', 'title', 'content'),
       ('shuffler54', 'title', 'content');
