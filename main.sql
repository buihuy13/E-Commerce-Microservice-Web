create database if not exists `user-service`;
use `user-service`;

create table users(
 `id` varchar(255) primary key,
 `password` varchar(255) not null,
 `username` varchar(255) not null unique,
 `email` varchar(255) not null unique,
 `phone` varchar(20) not null,
 `address` varchar(255) not null,
 `role` varchar(10) not null
 );


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
 `orderid` varchar(255) references `order`(id)
);

create database if not exists `product-service`
use `product-service`

create table products (
    `id` varchar(255) primary key,
    `name` varchar(255) not null unique,
    `description` varchar(255), 
    `price` decimal not null,
    `category` varchar(50) not null,
    `releaseDate` date
)

create table product_details (
    `id` int primary key auto_increment,
    `color` varchar(30) not null,
    `imagename` varchar(255),
    `imagetype` varchar(100),
    `imagedata` blob,
    `quantity` int not null,
    `product_id` varchar(255) references `products`(`id`)
)