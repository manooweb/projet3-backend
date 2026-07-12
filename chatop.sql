DROP TABLE IF EXISTS `MESSAGES`;
DROP TABLE IF EXISTS `RENTALS`;
DROP TABLE IF EXISTS `USERS`;

CREATE TABLE `USERS` (
  `id` integer PRIMARY KEY AUTO_INCREMENT,
  `email` varchar(255),
  `name` varchar(255),
  `password` varchar(255),
  `created_at` timestamp,
  `updated_at` timestamp
);

CREATE TABLE `RENTALS` (
  `id` integer PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(255),
  `surface` numeric,
  `price` numeric,
  `picture` varchar(255),
  `description` varchar(2000),
  `owner_id` integer NOT NULL,
  `created_at` timestamp,
  `updated_at` timestamp
);

CREATE TABLE `MESSAGES` (
  `id` integer PRIMARY KEY AUTO_INCREMENT,
  `rental_id` integer,
  `user_id` integer,
  `message` varchar(2000),
  `created_at` timestamp,
  `updated_at` timestamp
);

CREATE UNIQUE INDEX `USERS_index` ON `USERS` (`email`);

ALTER TABLE `RENTALS` ADD FOREIGN KEY (`owner_id`) REFERENCES `USERS` (`id`);

ALTER TABLE `MESSAGES` ADD FOREIGN KEY (`user_id`) REFERENCES `USERS` (`id`);

ALTER TABLE `MESSAGES` ADD FOREIGN KEY (`rental_id`) REFERENCES `RENTALS` (`id`);

INSERT INTO `USERS` (`id`, `email`, `name`, `password`, `created_at`, `updated_at`)
VALUES (
  1,
  'demo@chatop.com',
  'Demo User',
  '$2b$10$hikZVgHZZJYsMWOCfBRR4ejuz8izyuKzmitTCc/OIa2DUe2CugGra',
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
);

INSERT INTO `RENTALS` (
  `id`,
  `name`,
  `surface`,
  `price`,
  `picture`,
  `description`,
  `owner_id`,
  `created_at`,
  `updated_at`
)
VALUES (
  1,
  'Demo rental',
  75,
  1200,
  '/api/uploads/rentals/online-house-rental-sites.jpg',
  'A demo rental available after importing the database script.',
  1,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
);
