drop database if exists `order-service`;
drop database if exists `product-service`;
drop database if exists `user-service`;

create database if not exists `user-service`;
use `user-service`;

create table users(
 `id` varchar(255) primary key,
 `password` varchar(255) not null,
 `username` varchar(255) not null unique,
 `email` varchar(255) not null unique,
 `phone` varchar(20) not null,
 `address` varchar(255) not null,
 `role` varchar(10) not null,
 `active` varchar(20) not null,
 `verificationcode` varchar(255) not null
 );

insert into users(id, `password`, username, email, phone, address, `role`, `active`, `verificationcode`) values 
("testadminid", "$2a$12$xv4.GmxuJeUUs54wJNwPdODdcvnHs7ikvpCuLeVVMy4tki5hZLq/m", "testadmin", "testadmin@gmail.com", "0762612698", "TPHCM", "ADMIN", "ACTIVE", "abcxyz123"),
("testuserid", "$2a$12$CydeMvJj1Hvu/824Lh2NuOEIrZnlhRMIUM736cYXa7bSD3LUmGW7K", "testuser", "testuser@gmail.com", "0762612699", "TP Vinh, Nghe An", "USER", "ACTIVE", "abcxyz456");


create database if not exists `order-service`;
use `order-service`;

create table `order`( 
 `id` varchar(255) primary key,
 `status` varchar(20) not null,
 `user_id` varchar(255) not null,
 `created_at` date not null
);

create table cart(
 `id` bigint primary key auto_increment,
 `quantity` int not null,
 `productdetailsid` int not null,
 `order_id` varchar(255) references `order`(id)
);

DELIMITER $$
CREATE EVENT auto_remove_pending_orders
ON SCHEDULE EVERY 5 MINUTE
DO
BEGIN
    DELETE FROM `order` 
    WHERE status = 'PENDING' 
    AND created_at <= DATE_SUB(NOW(), INTERVAL 15 MINUTE);
END$$
DELIMITER ;

SET GLOBAL event_scheduler = ON;

create database if not exists `product-service`;
use `product-service`;

create table products (
    `id` varchar(255) primary key,
    `name` varchar(255) not null unique,
    `description` varchar(255), 
    `price` decimal not null,
    `category` varchar(50) not null,
    `release_date` date
);

create table product_details (
    `id` int primary key auto_increment,
    `color` varchar(30) not null,
    `quantity` int not null,
    `size` varchar(30) not null,
    `product_id` varchar(255) not null references `products`(`id`)
);

create table images (
    `id` bigint primary key auto_increment,
    `imagename` varchar(255) not null,
    `imagetype` varchar(100) not null,
    `imagedata` longblob not null,
    `productdetails_id` int not null references `product_details`(`id`)
);