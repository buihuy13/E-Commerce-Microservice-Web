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
 `verificationcode` varchar(255) not null,
 );

insert into users(id, `password`, username, email, phone, address, `role`) values 
("testadminid", "$2a$12$xv4.GmxuJeUUs54wJNwPdODdcvnHs7ikvpCuLeVVMy4tki5hZLq/m", "testadmin", "testadmin@gmail.com", "0762612698", "TPHCM", "ADMIN"),
("testuserid", "$2a$12$CydeMvJj1Hvu/824Lh2NuOEIrZnlhRMIUM736cYXa7bSD3LUmGW7K", "testuser", "testuser@gmail.com", "0762612699", "TP Vinh, Nghe An", "USER");


create database if not exists `order-service`;
use `order-service`;

create table `order`( 
 `id` varchar(255) primary key,
 `status` varchar(20) not null
);

create table cart(
 `id` bigint primary key auto_increment,
 `quantity` int not null,
 `productdetailsid` int not null,
 `order_id` varchar(255) references `order`(id)
);

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