/*
SQLyog Ultimate v12.09 (64 bit)
MySQL - 5.7.24 : Database - demaestore
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`demaestore` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `demaestore`;

/*Table structure for table `address` */

DROP TABLE IF EXISTS `address`;

CREATE TABLE `address` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_userId` int(11) DEFAULT NULL,
  `postalCodeInput` varchar(255) DEFAULT NULL,
  `prefecture` varchar(255) DEFAULT NULL,
  `city` varchar(255) DEFAULT NULL,
  `addressLine` varchar(255) DEFAULT NULL,
  `phoneNumber` varchar(255) DEFAULT NULL,
  `extra1` varchar(255) DEFAULT NULL,
  `extra2` varchar(255) DEFAULT NULL,
  `extra3` varchar(255) DEFAULT NULL,
  `extra4` varchar(255) DEFAULT NULL,
  `extra5` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `user_userId` (`user_userId`),
  CONSTRAINT `address_ibfk_1` FOREIGN KEY (`user_userId`) REFERENCES `user` (`userId`)
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8;

/*Data for the table `address` */

insert  into `address`(`id`,`user_userId`,`postalCodeInput`,`prefecture`,`city`,`addressLine`,`phoneNumber`,`extra1`,`extra2`,`extra3`,`extra4`,`extra5`) values (50,36080105,'1210812','東京都','足立区 西保木間','1丁目','9988878788',NULL,NULL,NULL,NULL,NULL),(61,30009257,'1210812','東京都','足立区 西保木間','あああ','00000000000',NULL,NULL,NULL,NULL,NULL),(62,13258165,'1000000','東京都','千代田区 ','1丁目','9988878788',NULL,NULL,NULL,NULL,NULL),(63,36080117,'1200005','東京都','足立区 綾瀬','1丁目','00000000001',NULL,NULL,NULL,NULL,NULL);

/*Table structure for table `combo` */

DROP TABLE IF EXISTS `combo`;

CREATE TABLE `combo` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `reserved1` varchar(255) DEFAULT NULL,
  `reserved2` varchar(255) DEFAULT NULL,
  `reserved3` varchar(255) DEFAULT NULL,
  `reserved4` varchar(255) DEFAULT NULL,
  `reserved5` varchar(255) DEFAULT NULL,
  `reserved6` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

/*Data for the table `combo` */

insert  into `combo`(`id`,`name`,`reserved1`,`reserved2`,`reserved3`,`reserved4`,`reserved5`,`reserved6`) values (1,'お得セットA',NULL,NULL,NULL,NULL,NULL,NULL),(2,'お得セットB',NULL,NULL,NULL,NULL,NULL,NULL);

/*Table structure for table `combo_foodcategory` */

DROP TABLE IF EXISTS `combo_foodcategory`;

CREATE TABLE `combo_foodcategory` (
  `comboId` bigint(20) NOT NULL,
  `selectedCategoryId` bigint(20) NOT NULL,
  `maxSelectable` int(11) NOT NULL,
  `extra1` varchar(255) DEFAULT NULL,
  `extra2` varchar(255) DEFAULT NULL,
  `extra3` varchar(255) DEFAULT NULL,
  `extra4` varchar(255) DEFAULT NULL,
  `extra5` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`comboId`,`selectedCategoryId`),
  KEY `selectedCategoryId` (`selectedCategoryId`),
  CONSTRAINT `combo_foodcategory_ibfk_1` FOREIGN KEY (`comboId`) REFERENCES `combo` (`id`),
  CONSTRAINT `combo_foodcategory_ibfk_2` FOREIGN KEY (`selectedCategoryId`) REFERENCES `tags` (`tid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `combo_foodcategory` */

insert  into `combo_foodcategory`(`comboId`,`selectedCategoryId`,`maxSelectable`,`extra1`,`extra2`,`extra3`,`extra4`,`extra5`) values (1,1,2,NULL,NULL,NULL,NULL,NULL),(1,3,1,NULL,NULL,NULL,NULL,NULL),(1,17,1,NULL,NULL,NULL,NULL,NULL),(2,1,2,NULL,NULL,NULL,NULL,NULL),(2,3,1,NULL,NULL,NULL,NULL,NULL),(2,17,1,NULL,NULL,NULL,NULL,NULL);

/*Table structure for table `combo_foodcategory_fooditem` */

DROP TABLE IF EXISTS `combo_foodcategory_fooditem`;

CREATE TABLE `combo_foodcategory_fooditem` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `comboId` bigint(20) NOT NULL,
  `selectedCategoryId` bigint(20) NOT NULL,
  `comboFoodId` bigint(20) NOT NULL,
  `extra1` varchar(255) DEFAULT NULL,
  `extra2` varchar(255) DEFAULT NULL,
  `extra3` varchar(255) DEFAULT NULL,
  `extra4` varchar(255) DEFAULT NULL,
  `extra5` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `selectedCategoryId` (`selectedCategoryId`),
  KEY `comboFoodId` (`comboFoodId`),
  KEY `combo_foodcategory_fooditem_ibfk_1` (`comboId`),
  CONSTRAINT `combo_foodcategory_fooditem_ibfk_1` FOREIGN KEY (`comboId`) REFERENCES `combo` (`id`),
  CONSTRAINT `combo_foodcategory_fooditem_ibfk_2` FOREIGN KEY (`selectedCategoryId`) REFERENCES `tags` (`tid`),
  CONSTRAINT `combo_foodcategory_fooditem_ibfk_3` FOREIGN KEY (`comboFoodId`) REFERENCES `foods` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;

/*Data for the table `combo_foodcategory_fooditem` */

insert  into `combo_foodcategory_fooditem`(`id`,`comboId`,`selectedCategoryId`,`comboFoodId`,`extra1`,`extra2`,`extra3`,`extra4`,`extra5`) values (1,1,1,3823780596,NULL,NULL,NULL,NULL,NULL),(2,1,1,3823780596,NULL,NULL,NULL,NULL,NULL),(3,1,3,4056954171,NULL,NULL,NULL,NULL,NULL),(4,1,3,740430262,NULL,NULL,NULL,NULL,NULL),(5,1,17,100012,NULL,NULL,NULL,NULL,NULL),(6,1,17,100014,NULL,NULL,NULL,NULL,NULL),(7,2,1,2233861812,NULL,NULL,NULL,NULL,NULL),(8,2,1,3823780596,NULL,NULL,NULL,NULL,NULL),(9,2,1,2305772036,NULL,NULL,NULL,NULL,NULL),(10,2,3,740430262,NULL,NULL,NULL,NULL,NULL),(11,2,3,4056954171,NULL,NULL,NULL,NULL,NULL),(12,2,17,100012,NULL,NULL,NULL,NULL,NULL),(13,2,17,100014,NULL,NULL,NULL,NULL,NULL);

/*Table structure for table `comment` */

DROP TABLE IF EXISTS `comment`;

CREATE TABLE `comment` (
  `rpid` int(11) NOT NULL AUTO_INCREMENT,
  `userId` int(11) NOT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `content` text NOT NULL,
  `ctime` datetime DEFAULT CURRENT_TIMESTAMP,
  `like` int(11) DEFAULT '0',
  `extra1` varchar(255) DEFAULT NULL,
  `extra2` varchar(255) DEFAULT NULL,
  `extra3` varchar(255) DEFAULT NULL,
  `extra4` varchar(255) DEFAULT NULL,
  `extra5` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`rpid`),
  KEY `userId` (`userId`),
  CONSTRAINT `comment_ibfk_1` FOREIGN KEY (`userId`) REFERENCES `user` (`userId`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8;

/*Data for the table `comment` */

insert  into `comment`(`rpid`,`userId`,`avatar`,`content`,`ctime`,`like`,`extra1`,`extra2`,`extra3`,`extra4`,`extra5`) values (2,36080105,'http://toutiao.itheima.net/resources/images/98.jpg','お肉はお値段相当でしょう、一度は是非のお店と思います','2024-09-04 09:57:45',182,NULL,NULL,NULL,NULL,NULL),(3,13258165,'http://toutiao.itheima.net/resources/images/98.jpg','久しぶりに訪問。また値上げされていました','2024-09-17 09:57:45',128,NULL,NULL,NULL,NULL,NULL),(32,30009257,NULL,'美味しい','2024-09-17 21:34:41',13,NULL,NULL,NULL,NULL,NULL),(45,30009257,NULL,'こんにちは','2024-10-08 11:03:52',204,NULL,NULL,NULL,NULL,NULL);

/*Table structure for table `files` */

DROP TABLE IF EXISTS `files`;

CREATE TABLE `files` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `userId` int(11) DEFAULT NULL,
  `file_name` varchar(255) NOT NULL,
  `file_path` varchar(255) NOT NULL,
  `file_type` varchar(50) NOT NULL,
  `file_size` int(11) NOT NULL,
  `upload_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `extra1` varchar(255) DEFAULT NULL,
  `extra2` varchar(255) DEFAULT NULL,
  `extra3` varchar(255) DEFAULT NULL,
  `extra4` varchar(255) DEFAULT NULL,
  `extra5` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_user` (`userId`),
  CONSTRAINT `fk_user` FOREIGN KEY (`userId`) REFERENCES `user` (`userId`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;

/*Data for the table `files` */

insert  into `files`(`id`,`userId`,`file_name`,`file_path`,`file_type`,`file_size`,`upload_time`,`extra1`,`extra2`,`extra3`,`extra4`,`extra5`) values (5,30009257,'1730364166108-OIP (2).jpg','/uploads/1730364166108-OIP (2).jpg','image/jpeg',4131,'2024-09-14 13:53:39',NULL,NULL,NULL,NULL,NULL),(6,13258165,'1726539854738-menu1.jpg','/uploads/1726539854738-menu1.jpg','image/jpeg',596157,'2024-09-17 11:24:14',NULL,NULL,NULL,NULL,NULL),(8,36080105,'1726717850880-family.jpg','/uploads/1726717850880-family.jpg','image/jpeg',1853,'2024-09-19 12:31:57',NULL,NULL,NULL,NULL,NULL);

/*Table structure for table `flavors` */

DROP TABLE IF EXISTS `flavors`;

CREATE TABLE `flavors` (
  `fid` bigint(20) NOT NULL AUTO_INCREMENT,
  `flavor` varchar(255) DEFAULT NULL,
  `column1` varchar(255) DEFAULT NULL,
  `column2` varchar(255) DEFAULT NULL,
  `column3` varchar(255) DEFAULT NULL,
  `column4` varchar(255) DEFAULT NULL,
  `column5` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`fid`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8;

/*Data for the table `flavors` */

insert  into `flavors`(`fid`,`flavor`,`column1`,`column2`,`column3`,`column4`,`column5`) values (1,'塩味',NULL,NULL,NULL,NULL,NULL),(2,'タレ',NULL,NULL,NULL,NULL,NULL),(3,'味噌',NULL,NULL,NULL,NULL,NULL),(4,'七味唐辛子',NULL,NULL,NULL,NULL,NULL),(5,'你好',NULL,NULL,NULL,NULL,NULL),(6,'おすすめ',NULL,NULL,NULL,NULL,NULL),(7,'推薦',NULL,NULL,NULL,NULL,NULL),(8,'テスト',NULL,NULL,NULL,NULL,NULL),(9,'123',NULL,NULL,NULL,NULL,NULL),(22,'456',NULL,NULL,NULL,NULL,NULL),(23,'塩',NULL,NULL,NULL,NULL,NULL),(24,'１２３',NULL,NULL,NULL,NULL,NULL),(25,'234',NULL,NULL,NULL,NULL,NULL),(26,'2344',NULL,NULL,NULL,NULL,NULL),(27,'23445',NULL,NULL,NULL,NULL,NULL),(28,'23445333',NULL,NULL,NULL,NULL,NULL),(29,'213',NULL,NULL,NULL,NULL,NULL),(30,'1232',NULL,NULL,NULL,NULL,NULL),(31,'444444',NULL,NULL,NULL,NULL,NULL),(32,'test',NULL,NULL,NULL,NULL,NULL),(33,'vcvc',NULL,NULL,NULL,NULL,NULL),(34,'tr',NULL,NULL,NULL,NULL,NULL),(35,'3245242',NULL,NULL,NULL,NULL,NULL),(36,'231',NULL,NULL,NULL,NULL,NULL),(37,'12312',NULL,NULL,NULL,NULL,NULL);

/*Table structure for table `food_flavors` */

DROP TABLE IF EXISTS `food_flavors`;

CREATE TABLE `food_flavors` (
  `food_id` bigint(20) NOT NULL,
  `flavor_id` bigint(20) NOT NULL,
  PRIMARY KEY (`food_id`,`flavor_id`),
  KEY `food_flavors_ibfk_2` (`flavor_id`),
  CONSTRAINT `food_flavors_ibfk_1` FOREIGN KEY (`food_id`) REFERENCES `foods` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `food_flavors_ibfk_2` FOREIGN KEY (`flavor_id`) REFERENCES `flavors` (`fid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `food_flavors` */

insert  into `food_flavors`(`food_id`,`flavor_id`) values (3823780596,1),(8078956697,1),(3823780596,2),(8078956697,2),(3823780596,3),(8078956697,3),(3823780596,4),(8078956697,4),(100006,9),(100006,25),(100006,30),(100006,31),(100006,32);

/*Table structure for table `foodpicture` */

DROP TABLE IF EXISTS `foodpicture`;

CREATE TABLE `foodpicture` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fid` bigint(20) NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `file_path` varchar(255) NOT NULL,
  `file_type` varchar(50) NOT NULL,
  `file_size` int(11) NOT NULL,
  `upload_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `extra1` varchar(255) DEFAULT NULL,
  `extra2` varchar(255) DEFAULT NULL,
  `extra3` varchar(255) DEFAULT NULL,
  `extra4` varchar(255) DEFAULT NULL,
  `extra5` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_foodpicture_food` (`fid`),
  CONSTRAINT `fk_foodpicture_food` FOREIGN KEY (`fid`) REFERENCES `foods` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8;

/*Data for the table `foodpicture` */

insert  into `foodpicture`(`id`,`fid`,`file_name`,`file_path`,`file_type`,`file_size`,`upload_time`,`extra1`,`extra2`,`extra3`,`extra4`,`extra5`) values (1,8078956697,'th','/th.jpg','image/jpeg',100000,'2024-09-19 13:49:48',NULL,NULL,NULL,NULL,NULL),(2,7384994864,'/menu1.jpg','/menu1.jpg','image/jpeg',30946,'2024-09-19 13:49:48',NULL,NULL,NULL,NULL,NULL),(3,2305772036,'menu2','/menu2.jpg','image/jpeg',100000,'2024-09-19 13:49:48',NULL,NULL,NULL,NULL,NULL),(4,2233861812,'menu3','/menu3.jpg','image/jpeg',100000,'2024-09-19 13:49:48',NULL,NULL,NULL,NULL,NULL),(5,3823780596,'menu4','/menu4.jpg','image/jpeg',100000,'2024-09-19 13:49:48',NULL,NULL,NULL,NULL,NULL),(6,6592009498,'menu5','/menu5.jpg','image/jpeg',100000,'2024-09-19 13:49:48',NULL,NULL,NULL,NULL,NULL),(7,4056954171,'menu6','/menu6.jpg','image/jpeg',100000,'2024-09-19 13:49:48',NULL,NULL,NULL,NULL,NULL),(8,740430262,'menu7','/menu7.jpg','image/jpeg',100000,'2024-09-19 13:49:48',NULL,NULL,NULL,NULL,NULL),(9,7466390504,'menu8','/menu8.jpg','image/jpeg',100000,'2024-09-19 13:49:48',NULL,NULL,NULL,NULL,NULL),(19,100006,'1730276439711-OIP (2).jpg','/uploads/foods/1730276439711-OIP (2).jpg','image/jpeg',4131,'2024-10-17 19:20:58',NULL,NULL,NULL,NULL,NULL),(20,10009,'1729647569157-OIP (2).jpg','/uploads/foods/1729647569157-OIP (2).jpg','image/jpeg',4131,'2024-10-23 10:39:29',NULL,NULL,NULL,NULL,NULL),(21,100012,'1735609835555-1000_F_228934142_5cmtzHO73Sle6Y8uhRg2yeBjmi8DvgQr.jpg','/uploads/foods/1735609835555-1000_F_228934142_5cmtzHO73Sle6Y8uhRg2yeBjmi8DvgQr.jpg','image/jpeg',142722,'2024-12-31 10:50:35',NULL,NULL,NULL,NULL,NULL),(22,100013,'1735610114860-menu4.jpg','/uploads/foods/1735610114860-menu4.jpg','image/jpeg',96486,'2024-12-31 10:55:15',NULL,NULL,NULL,NULL,NULL),(23,100014,'1735712350106-strawberry.jpg','/uploads/foods/1735712350106-strawberry.jpg','image/jpeg',4513,'2025-01-01 15:19:10',NULL,NULL,NULL,NULL,NULL),(24,100015,'1735809044652-menu5.jpg','/uploads/foods/1735809044652-menu5.jpg','image/jpeg',165075,'2025-01-02 18:10:44',NULL,NULL,NULL,NULL,NULL);

/*Table structure for table `foods` */

DROP TABLE IF EXISTS `foods`;

CREATE TABLE `foods` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `like_ratio_desc` varchar(255) DEFAULT NULL,
  `month_saled` int(11) DEFAULT NULL,
  `unit` varchar(50) DEFAULT NULL,
  `food_tag_list` varchar(255) DEFAULT NULL,
  `price` int(11) DEFAULT '99999999',
  `picture` varchar(255) DEFAULT NULL,
  `description` text,
  `tag` varchar(50) DEFAULT NULL,
  `count` int(11) DEFAULT '1',
  `shop_id` int(11) DEFAULT NULL,
  `status` enum('online','stage') NOT NULL DEFAULT 'stage',
  `comboId` bigint(20) DEFAULT NULL,
  `extra3` varchar(255) DEFAULT NULL,
  `extra4` varchar(255) DEFAULT NULL,
  `extra5` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_shop_id` (`shop_id`),
  KEY `fk_tag` (`tag`),
  KEY `fk_combo` (`comboId`),
  CONSTRAINT `fk_combo` FOREIGN KEY (`comboId`) REFERENCES `combo` (`id`),
  CONSTRAINT `fk_shop_id` FOREIGN KEY (`shop_id`) REFERENCES `root` (`rootId`),
  CONSTRAINT `fk_tag` FOREIGN KEY (`tag`) REFERENCES `tags` (`tag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `foods` */

insert  into `foods`(`id`,`name`,`like_ratio_desc`,`month_saled`,`unit`,`food_tag_list`,`price`,`picture`,`description`,`tag`,`count`,`shop_id`,`status`,`comboId`,`extra3`,`extra4`,`extra5`) values (10009,'トンカツ',NULL,NULL,'10本',NULL,8000,NULL,NULL,'43800681885285920000',1,30009257,'online',NULL,NULL,NULL,NULL),(100006,'トンカツ',NULL,NULL,'10本','',1233,NULL,NULL,'43800681885285920000',1,30009257,'online',NULL,NULL,NULL,NULL),(100012,'ビール',NULL,NULL,'一杯',NULL,300,NULL,NULL,'52151467139914770000',1,30009257,'online',NULL,NULL,NULL,NULL),(100013,'季節限定お得セットA',NULL,NULL,'一つ',NULL,4000,NULL,NULL,'56890217062791730000',1,30009257,'online',1,NULL,NULL,NULL),(100014,'ストロベリーシェイク',NULL,NULL,'一杯',NULL,400,NULL,NULL,'52151467139914770000',1,30009257,'online',NULL,NULL,NULL,NULL),(100015,'季節限定お得セットB',NULL,NULL,'一つ',NULL,8000,NULL,NULL,'56890217062791730000',1,30009257,'online',2,NULL,NULL,NULL),(740430262,'焼き餃子','好評度100%',100,'6個',NULL,500,NULL,'黄金色の表皮とカリカリの食感','98147100',1,30009257,'online',NULL,NULL,NULL,NULL),(2233861812,'秋刀魚の丸焼き','好評度73%',600,'1人前','おすすめ,油の香りが濃くて味が良い,美味しい',1500,NULL,'原材料：秋刀魚、レモン','318569657',1,30009257,'online',NULL,NULL,NULL,NULL),(2305772036,'アナゴ天婦羅','好評度91%',300,'1人前',NULL,2000,NULL,'原材料：卵、天ぷら粉、油、アナゴ','318569657',1,30009257,'online',NULL,NULL,NULL,NULL),(3823780596,'鶏肉串焼き','',200,'10本','おすすめ',800,NULL,'原材料：鶏肉、ネギ','82022594',1,30009257,'online',NULL,NULL,NULL,NULL),(4056954171,'ご飯','',1000,'約三百グラム',NULL,200,NULL,'ご飯にゴマの香り','98147100',1,30009257,'online',NULL,NULL,NULL,NULL),(6592009498,'羊のステーキ','',50,'1人前','',2500,NULL,'原材料：6-8ヶ月仔羊のリブステーキ','82022594',1,30009257,'online',NULL,NULL,NULL,NULL),(7384994864,'海老天ぷら','好評度81%',100,'1人前',NULL,1300,NULL,'','318569657',1,30009257,'online',NULL,NULL,NULL,NULL),(7466390504,'豚骨ラーメン','好評度100%',100,'一丁',NULL,1000,NULL,'','98147100',1,30009257,'online',NULL,NULL,NULL,NULL),(8078956697,'くし焼きラム肉(10本)','好評度100%',40,'10本','おすすめ',900,NULL,'','318569657',1,30009257,'online',NULL,NULL,NULL,NULL);

/*Table structure for table `order_combo_foods` */

DROP TABLE IF EXISTS `order_combo_foods`;

CREATE TABLE `order_combo_foods` (
  `orderId` int(11) NOT NULL,
  `comboFoodId` bigint(20) NOT NULL,
  `comboCategoryFoodId` bigint(20) NOT NULL,
  `quantity` int(11) NOT NULL,
  `flavorId` bigint(20) DEFAULT NULL,
  `extra1` varchar(255) DEFAULT NULL,
  `extra2` varchar(255) DEFAULT NULL,
  `extra3` varchar(255) DEFAULT NULL,
  `extra4` varchar(255) DEFAULT NULL,
  `extra5` varchar(255) DEFAULT NULL,
  UNIQUE KEY `unique_order_food_combo` (`orderId`,`comboFoodId`,`comboCategoryFoodId`),
  KEY `order_combo_foods_ibfk_2` (`comboFoodId`),
  KEY `order_combo_foods_ibfk_3` (`flavorId`),
  KEY `order_combo_foods_ibfk_4` (`comboCategoryFoodId`),
  CONSTRAINT `order_combo_foods_ibfk_1` FOREIGN KEY (`orderId`) REFERENCES `orders` (`oid`) ON UPDATE CASCADE,
  CONSTRAINT `order_combo_foods_ibfk_2` FOREIGN KEY (`comboFoodId`) REFERENCES `foods` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `order_combo_foods_ibfk_3` FOREIGN KEY (`flavorId`) REFERENCES `flavors` (`fid`) ON UPDATE CASCADE,
  CONSTRAINT `order_combo_foods_ibfk_4` FOREIGN KEY (`comboCategoryFoodId`) REFERENCES `combo_foodcategory_fooditem` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `order_combo_foods` */

insert  into `order_combo_foods`(`orderId`,`comboFoodId`,`comboCategoryFoodId`,`quantity`,`flavorId`,`extra1`,`extra2`,`extra3`,`extra4`,`extra5`) values (205,100013,1,1,1,NULL,NULL,NULL,NULL,NULL),(205,100013,2,1,1,NULL,NULL,NULL,NULL,NULL),(205,100013,4,1,NULL,NULL,NULL,NULL,NULL,NULL),(205,100013,6,1,NULL,NULL,NULL,NULL,NULL,NULL),(206,100013,1,1,1,NULL,NULL,NULL,NULL,NULL),(206,100013,2,1,1,NULL,NULL,NULL,NULL,NULL),(206,100013,4,1,NULL,NULL,NULL,NULL,NULL,NULL),(206,100013,6,1,NULL,NULL,NULL,NULL,NULL,NULL);

/*Table structure for table `order_foods` */

DROP TABLE IF EXISTS `order_foods`;

CREATE TABLE `order_foods` (
  `order_id` int(11) NOT NULL,
  `food_id` bigint(20) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
  `flavor_id` bigint(20) DEFAULT NULL,
  UNIQUE KEY `unique_order_food_flavor` (`order_id`,`food_id`,`flavor_id`),
  KEY `food_id` (`food_id`),
  KEY `flavor_id` (`flavor_id`),
  CONSTRAINT `order_foods_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`oid`) ON UPDATE CASCADE,
  CONSTRAINT `order_foods_ibfk_2` FOREIGN KEY (`food_id`) REFERENCES `foods` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `order_foods_ibfk_3` FOREIGN KEY (`flavor_id`) REFERENCES `flavors` (`fid`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `order_foods` */

insert  into `order_foods`(`order_id`,`food_id`,`quantity`,`flavor_id`) values (203,3823780596,1,1),(203,6592009498,1,NULL),(204,3823780596,1,1),(204,6592009498,1,NULL),(205,7384994864,4,NULL),(205,8078956697,2,2),(205,100013,1,NULL),(206,100013,1,NULL);

/*Table structure for table `orders` */

DROP TABLE IF EXISTS `orders`;

CREATE TABLE `orders` (
  `oid` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL,
  `ctime` datetime DEFAULT CURRENT_TIMESTAMP,
  `amount` decimal(10,2) NOT NULL,
  `status` enum('Pending','Processing','Shipped','Delivered','Cancelled','Returned') NOT NULL DEFAULT 'Pending',
  `extra2` varchar(255) DEFAULT NULL,
  `extra3` varchar(255) DEFAULT NULL,
  `extra4` varchar(255) DEFAULT NULL,
  `extra5` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `uid` (`uid`),
  KEY `fk_comboId` (`extra2`),
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`uid`) REFERENCES `user` (`userId`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=207 DEFAULT CHARSET=utf8;

/*Data for the table `orders` */

insert  into `orders`(`oid`,`uid`,`ctime`,`amount`,`status`,`extra2`,`extra3`,`extra4`,`extra5`) values (123,13258165,'2024-10-22 16:24:26','2.00','Delivered',NULL,NULL,NULL,NULL),(124,13258165,'2024-10-23 10:50:12','10000.00','Pending',NULL,NULL,NULL,NULL),(125,13258165,'2024-10-23 11:30:21','2000.00','Pending',NULL,NULL,NULL,NULL),(126,13258165,'2024-10-23 11:57:14','2000.00','Pending',NULL,NULL,NULL,NULL),(127,13258165,'2024-10-23 11:58:23','2000.00','Pending',NULL,NULL,NULL,NULL),(128,30009257,'2024-10-24 17:33:31','4100.00','Pending',NULL,NULL,NULL,NULL),(129,30009257,'2024-10-24 17:37:39','7400.00','Pending',NULL,NULL,NULL,NULL),(130,13258165,'2024-10-25 09:58:30','2000.00','Pending',NULL,NULL,NULL,NULL),(131,13258165,'2024-10-25 10:38:43','2000.00','Pending',NULL,NULL,NULL,NULL),(132,30009257,'2024-10-25 13:48:45','6600.00','Pending',NULL,NULL,NULL,NULL),(133,30009257,'2024-10-25 13:50:30','6600.00','Pending',NULL,NULL,NULL,NULL),(134,30009257,'2024-10-25 13:52:36','6600.00','Pending',NULL,NULL,NULL,NULL),(135,30009257,'2024-10-25 13:53:27','6600.00','Pending',NULL,NULL,NULL,NULL),(136,30009257,'2024-10-25 13:53:42','4100.00','Pending',NULL,NULL,NULL,NULL),(137,30009257,'2024-10-25 13:54:40','4100.00','Pending',NULL,NULL,NULL,NULL),(138,30009257,'2024-10-25 14:04:37','6600.00','Pending',NULL,NULL,NULL,NULL),(139,30009257,'2024-10-25 14:07:37','6600.00','Pending',NULL,NULL,NULL,NULL),(140,30009257,'2024-10-25 14:09:41','4100.00','Pending',NULL,NULL,NULL,NULL),(141,30009257,'2024-10-25 14:10:58','4100.00','Pending',NULL,NULL,NULL,NULL),(142,30009257,'2024-10-25 14:18:07','6600.00','Pending',NULL,NULL,NULL,NULL),(143,30009257,'2024-10-25 14:19:09','3300.00','Pending',NULL,NULL,NULL,NULL),(144,30009257,'2024-10-25 14:19:55','4100.00','Pending',NULL,NULL,NULL,NULL),(145,30009257,'2024-10-25 14:46:17','5000.00','Pending',NULL,NULL,NULL,NULL),(146,30009257,'2024-10-25 15:14:24','4100.00','Pending',NULL,NULL,NULL,NULL),(147,30009257,'2024-10-25 19:09:11','3300.00','Pending',NULL,NULL,NULL,NULL),(148,30009257,'2024-10-28 09:38:42','6600.00','Pending',NULL,NULL,NULL,NULL),(149,30009257,'2024-10-28 09:41:13','3300.00','Pending',NULL,NULL,NULL,NULL),(150,30009257,'2024-10-28 09:45:50','4100.00','Pending',NULL,NULL,NULL,NULL),(151,30009257,'2024-10-28 09:48:38','3300.00','Pending',NULL,NULL,NULL,NULL),(152,30009257,'2024-10-28 12:00:19','5000.00','Pending',NULL,NULL,NULL,NULL),(153,30009257,'2024-10-28 17:41:10','5000.00','Pending',NULL,NULL,NULL,NULL),(154,30009257,'2024-10-28 19:19:23','1300.00','Pending',NULL,NULL,NULL,NULL),(155,30009257,'2024-10-28 19:37:49','3300.00','Pending',NULL,NULL,NULL,NULL),(156,30009257,'2024-10-28 19:49:53','3500.00','Pending',NULL,NULL,NULL,NULL),(157,30009257,'2024-10-28 20:44:29','5000.00','Pending',NULL,NULL,NULL,NULL),(158,30009257,'2024-10-30 13:44:07','1600.00','Pending',NULL,NULL,NULL,NULL),(159,30009257,'2024-10-30 15:13:29','3300.00','Pending',NULL,NULL,NULL,NULL),(160,30009257,'2024-10-30 16:41:23','7300.00','Pending',NULL,NULL,NULL,NULL),(161,30009257,'2024-10-30 17:14:14','4100.00','Pending',NULL,NULL,NULL,NULL),(162,30009257,'2024-10-30 19:57:18','8300.00','Pending',NULL,NULL,NULL,NULL),(163,30009257,'2024-10-31 17:41:58','16500.00','Pending',NULL,NULL,NULL,NULL),(164,30009257,'2024-11-06 10:59:08','9100.00','Pending',NULL,NULL,NULL,NULL),(165,30009257,'2024-11-07 10:13:57','5500.00','Pending',NULL,NULL,NULL,NULL),(167,36080105,'2024-11-07 18:28:58','4800.00','Pending',NULL,NULL,NULL,NULL),(168,36080105,'2024-11-07 20:22:43','3500.00','Pending',NULL,NULL,NULL,NULL),(169,36080105,'2024-11-07 22:03:16','12100.00','Pending',NULL,NULL,NULL,NULL),(170,13258165,'2024-11-08 16:27:33','4900.00','Pending',NULL,NULL,NULL,NULL),(171,30009257,'2024-11-09 20:44:53','3300.00','Pending',NULL,NULL,NULL,NULL),(172,30009257,'2024-11-11 12:40:40','800.00','Pending',NULL,NULL,NULL,NULL),(173,36080105,'2024-11-11 17:36:31','4100.00','Pending',NULL,NULL,NULL,NULL),(174,36080105,'2024-11-13 12:35:13','4800.00','Pending',NULL,NULL,NULL,NULL),(175,36080105,'2024-11-14 12:07:57','2100.00','Delivered',NULL,NULL,NULL,NULL),(176,30009257,'2024-11-14 18:14:36','12200.00','Delivered',NULL,NULL,NULL,NULL),(177,36080105,'2024-11-15 12:31:48','8300.00','Processing',NULL,NULL,NULL,NULL),(178,36080105,'2024-11-18 12:36:12','5000.00','Pending',NULL,NULL,NULL,NULL),(179,36080105,'2024-11-18 19:18:30','8800.00','Delivered',NULL,NULL,NULL,NULL),(180,36080105,'2024-11-19 12:15:17','3300.00','Pending',NULL,NULL,NULL,NULL),(181,36080105,'2024-11-19 17:18:37','9100.00','Delivered',NULL,NULL,NULL,NULL),(182,36080105,'2024-11-19 18:27:42','3300.00','Delivered',NULL,NULL,NULL,NULL),(183,36080105,'2024-11-20 19:33:56','8800.00','Delivered',NULL,NULL,NULL,NULL),(184,36080105,'2024-11-21 12:19:53','6300.00','Pending',NULL,NULL,NULL,NULL),(185,36080117,'2024-12-26 12:37:04','6600.00','Pending',NULL,NULL,NULL,NULL),(186,30009257,'2024-12-27 18:27:16','5000.00','Pending',NULL,NULL,NULL,NULL),(187,36080105,'2024-12-31 10:44:27','8300.00','Pending',NULL,NULL,NULL,NULL),(188,36080105,'2024-12-31 17:18:29','3300.00','Pending',NULL,NULL,NULL,NULL),(189,36080105,'2024-12-31 17:18:52','5000.00','Pending',NULL,NULL,NULL,NULL),(190,36080105,'2024-12-31 17:19:47','5000.00','Pending',NULL,NULL,NULL,NULL),(191,36080105,'2024-12-31 17:19:49','5000.00','Pending',NULL,NULL,NULL,NULL),(192,36080105,'2024-12-31 17:20:00','5000.00','Pending',NULL,NULL,NULL,NULL),(193,36080105,'2024-12-31 17:20:01','5000.00','Pending',NULL,NULL,NULL,NULL),(194,36080105,'2024-12-31 17:20:56','5000.00','Pending',NULL,NULL,NULL,NULL),(195,36080105,'2024-12-31 17:26:51','5000.00','Pending',NULL,NULL,NULL,NULL),(196,36080105,'2024-12-31 17:29:32','8300.00','Pending',NULL,NULL,NULL,NULL),(197,36080105,'2024-12-31 17:34:22','7000.00','Pending',NULL,NULL,NULL,NULL),(198,36080105,'2024-12-31 17:34:50','4500.00','Pending',NULL,NULL,NULL,NULL),(201,30009257,'2025-01-07 12:37:56','2400.00','Pending',NULL,NULL,NULL,NULL),(202,36080105,'2025-01-09 12:40:07','1800.00','Pending',NULL,NULL,NULL,NULL),(203,36080105,'2025-01-09 12:52:11','3300.00','Pending',NULL,NULL,NULL,NULL),(204,36080105,'2025-01-09 12:52:28','3300.00','Pending',NULL,NULL,NULL,NULL),(205,36080105,'2025-01-16 12:31:17','11000.00','Pending',NULL,NULL,NULL,NULL),(206,36080105,'2025-01-17 12:15:31','4000.00','Delivered',NULL,NULL,NULL,NULL);

/*Table structure for table `root` */

DROP TABLE IF EXISTS `root`;

CREATE TABLE `root` (
  `rootId` int(11) NOT NULL,
  `password` varchar(255) NOT NULL,
  `phoneNumber` varchar(20) DEFAULT NULL,
  `mailAddress` varchar(255) DEFAULT NULL,
  `uname` varchar(255) DEFAULT NULL,
  `gender` enum('male','female') DEFAULT NULL,
  `lastUpdated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `reserved1` varchar(255) DEFAULT NULL,
  `reserved2` varchar(255) DEFAULT NULL,
  `reserved3` varchar(255) DEFAULT NULL,
  `reserved4` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`rootId`),
  UNIQUE KEY `phoneNumber` (`phoneNumber`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `root` */

insert  into `root`(`rootId`,`password`,`phoneNumber`,`mailAddress`,`uname`,`gender`,`lastUpdated`,`reserved1`,`reserved2`,`reserved3`,`reserved4`) values (30009257,'246811','13211111112',NULL,'紺伝助',NULL,'2024-09-18 19:39:35',NULL,NULL,NULL,NULL);

/*Table structure for table `shop` */

DROP TABLE IF EXISTS `shop`;

CREATE TABLE `shop` (
  `shopId` int(11) NOT NULL AUTO_INCREMENT,
  `shopName` varchar(255) NOT NULL,
  `shopUrl` varchar(255) DEFAULT NULL,
  `reserved2` varchar(255) DEFAULT NULL,
  `reserved3` varchar(255) DEFAULT NULL,
  `reserved4` varchar(255) DEFAULT NULL,
  `reserved5` varchar(255) DEFAULT NULL,
  `reserved6` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`shopId`)
) ENGINE=InnoDB AUTO_INCREMENT=30009259 DEFAULT CHARSET=utf8;

/*Data for the table `shop` */

insert  into `shop`(`shopId`,`shopName`,`shopUrl`,`reserved2`,`reserved3`,`reserved4`,`reserved5`,`reserved6`) values (30009257,'焼肉屋',NULL,NULL,NULL,NULL,NULL,NULL),(30009258,'パン屋',NULL,NULL,NULL,NULL,NULL,NULL);

/*Table structure for table `shoptables` */

DROP TABLE IF EXISTS `shoptables`;

CREATE TABLE `shoptables` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `shopId` int(11) NOT NULL,
  `tableId` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `shopId` (`shopId`),
  KEY `tableId` (`tableId`),
  CONSTRAINT `shoptables_ibfk_1` FOREIGN KEY (`shopId`) REFERENCES `shop` (`shopId`) ON UPDATE CASCADE,
  CONSTRAINT `shoptables_ibfk_2` FOREIGN KEY (`tableId`) REFERENCES `tables` (`tableId`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

/*Data for the table `shoptables` */

insert  into `shoptables`(`id`,`shopId`,`tableId`) values (1,30009257,1),(2,30009257,2),(3,30009257,3);

/*Table structure for table `tables` */

DROP TABLE IF EXISTS `tables`;

CREATE TABLE `tables` (
  `tableId` int(11) NOT NULL AUTO_INCREMENT,
  `tableNumber` varchar(255) NOT NULL,
  `reserved1` varchar(255) DEFAULT NULL,
  `reserved2` varchar(255) DEFAULT NULL,
  `reserved3` varchar(255) DEFAULT NULL,
  `reserved4` varchar(255) DEFAULT NULL,
  `reserved5` varchar(255) DEFAULT NULL,
  `reserved6` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`tableId`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8;

/*Data for the table `tables` */

insert  into `tables`(`tableId`,`tableNumber`,`reserved1`,`reserved2`,`reserved3`,`reserved4`,`reserved5`,`reserved6`) values (1,'1',NULL,NULL,NULL,NULL,NULL,NULL),(2,'2',NULL,NULL,NULL,NULL,NULL,NULL),(3,'3',NULL,NULL,NULL,NULL,NULL,NULL),(4,'4',NULL,NULL,NULL,NULL,NULL,NULL),(5,'5',NULL,NULL,NULL,NULL,NULL,NULL),(6,'6',NULL,NULL,NULL,NULL,NULL,NULL),(7,'7',NULL,NULL,NULL,NULL,NULL,NULL),(8,'8',NULL,NULL,NULL,NULL,NULL,NULL),(9,'9',NULL,NULL,NULL,NULL,NULL,NULL),(10,'10',NULL,NULL,NULL,NULL,NULL,NULL),(11,'11',NULL,NULL,NULL,NULL,NULL,NULL),(12,'12',NULL,NULL,NULL,NULL,NULL,NULL),(13,'13',NULL,NULL,NULL,NULL,NULL,NULL),(14,'14',NULL,NULL,NULL,NULL,NULL,NULL),(15,'15',NULL,NULL,NULL,NULL,NULL,NULL),(16,'16',NULL,NULL,NULL,NULL,NULL,NULL),(17,'17',NULL,NULL,NULL,NULL,NULL,NULL),(18,'18',NULL,NULL,NULL,NULL,NULL,NULL),(19,'19',NULL,NULL,NULL,NULL,NULL,NULL),(20,'20',NULL,NULL,NULL,NULL,NULL,NULL),(21,'21',NULL,NULL,NULL,NULL,NULL,NULL),(22,'22',NULL,NULL,NULL,NULL,NULL,NULL),(23,'23',NULL,NULL,NULL,NULL,NULL,NULL),(24,'24',NULL,NULL,NULL,NULL,NULL,NULL),(25,'25',NULL,NULL,NULL,NULL,NULL,NULL),(26,'26',NULL,NULL,NULL,NULL,NULL,NULL),(27,'27',NULL,NULL,NULL,NULL,NULL,NULL),(28,'28',NULL,NULL,NULL,NULL,NULL,NULL),(29,'29',NULL,NULL,NULL,NULL,NULL,NULL),(30,'30',NULL,NULL,NULL,NULL,NULL,NULL),(31,'31',NULL,NULL,NULL,NULL,NULL,NULL),(32,'32',NULL,NULL,NULL,NULL,NULL,NULL),(33,'33',NULL,NULL,NULL,NULL,NULL,NULL),(34,'34',NULL,NULL,NULL,NULL,NULL,NULL),(35,'35',NULL,NULL,NULL,NULL,NULL,NULL),(36,'36',NULL,NULL,NULL,NULL,NULL,NULL),(37,'37',NULL,NULL,NULL,NULL,NULL,NULL),(38,'38',NULL,NULL,NULL,NULL,NULL,NULL),(39,'39',NULL,NULL,NULL,NULL,NULL,NULL),(40,'40',NULL,NULL,NULL,NULL,NULL,NULL),(41,'41',NULL,NULL,NULL,NULL,NULL,NULL),(42,'42',NULL,NULL,NULL,NULL,NULL,NULL),(43,'43',NULL,NULL,NULL,NULL,NULL,NULL),(44,'44',NULL,NULL,NULL,NULL,NULL,NULL),(45,'45',NULL,NULL,NULL,NULL,NULL,NULL),(46,'46',NULL,NULL,NULL,NULL,NULL,NULL),(47,'47',NULL,NULL,NULL,NULL,NULL,NULL),(48,'48',NULL,NULL,NULL,NULL,NULL,NULL),(49,'49',NULL,NULL,NULL,NULL,NULL,NULL),(50,'50',NULL,NULL,NULL,NULL,NULL,NULL);

/*Table structure for table `tags` */

DROP TABLE IF EXISTS `tags`;

CREATE TABLE `tags` (
  `tid` bigint(20) NOT NULL AUTO_INCREMENT,
  `tag` varchar(50) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `extra1` varchar(255) DEFAULT NULL,
  `extra2` varchar(255) DEFAULT NULL,
  `extra3` varchar(255) DEFAULT NULL,
  `extra4` varchar(255) DEFAULT NULL,
  `extra5` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`tid`),
  UNIQUE KEY `name` (`name`),
  UNIQUE KEY `tag` (`tag`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8;

/*Data for the table `tags` */

insert  into `tags`(`tid`,`tag`,`name`,`extra1`,`extra2`,`extra3`,`extra4`,`extra5`) values (1,'318569657','定食',NULL,NULL,NULL,NULL,NULL),(2,'82022594','焼肉',NULL,NULL,NULL,NULL,NULL),(3,'98147100','主食',NULL,NULL,NULL,NULL,NULL),(16,'43800681885285920000','test',NULL,NULL,NULL,NULL,NULL),(17,'52151467139914770000','ドリンク',NULL,NULL,NULL,NULL,NULL),(18,'56890217062791730000','セット',NULL,NULL,NULL,NULL,NULL);

/*Table structure for table `user` */

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `userId` int(11) NOT NULL AUTO_INCREMENT,
  `PASSWORD` varchar(255) NOT NULL,
  `phoneNumber` varchar(20) DEFAULT NULL,
  `mailAddress` varchar(255) DEFAULT NULL,
  `uname` varchar(255) DEFAULT NULL,
  `gender` enum('male','female') DEFAULT NULL,
  `lastUpdated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `verificationCodeUpdatedAt` datetime DEFAULT NULL,
  `managerId` int(11) DEFAULT NULL,
  `reserved5` varchar(255) DEFAULT NULL,
  `reserved6` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`userId`),
  UNIQUE KEY `phoneNumber` (`phoneNumber`),
  UNIQUE KEY `mailAddress` (`mailAddress`),
  KEY `fk_manager` (`managerId`),
  CONSTRAINT `fk_manager` FOREIGN KEY (`managerId`) REFERENCES `shop` (`shopId`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=36080118 DEFAULT CHARSET=utf8;

/*Data for the table `user` */

insert  into `user`(`userId`,`PASSWORD`,`phoneNumber`,`mailAddress`,`uname`,`gender`,`lastUpdated`,`verificationCodeUpdatedAt`,`managerId`,`reserved5`,`reserved6`) values (13258165,'242222','13211118888',NULL,'パパ',NULL,'2024-09-17 11:24:14','2024-11-04 16:22:05',NULL,NULL,NULL),(30009257,'246810','13211111111','','紺伝助','male','2024-11-06 14:14:14','2024-11-04 16:22:05',30009257,NULL,NULL),(36080105,'322588','13211111112','konndenn61@gmail.com','NOZAKI','male','2025-01-17 12:14:41','2025-01-17 12:14:41',30009257,NULL,NULL),(36080110,'243581',NULL,'232@sa.com',NULL,NULL,'2024-11-04 15:03:30','2024-11-04 16:22:05',NULL,NULL,NULL),(36080111,'341667',NULL,'2314213@gmail.com',NULL,NULL,'2024-11-04 15:17:32','2024-11-04 16:22:05',NULL,NULL,NULL),(36080112,'962222',NULL,'2342@gmail.com',NULL,NULL,'2024-11-04 16:28:13','2024-11-04 16:23:00',NULL,NULL,NULL),(36080113,'472949',NULL,'1231@gmail.com','232','female','2024-11-04 16:38:16','2024-11-04 16:37:36',NULL,NULL,NULL),(36080114,'889799',NULL,'konndenn@gmail.com',NULL,NULL,'2024-11-04 17:28:50','2024-11-04 17:28:50',NULL,NULL,NULL),(36080115,'977099',NULL,'konnndenn61@gmail.com',NULL,NULL,'2024-11-07 19:50:22','2024-11-07 19:50:22',NULL,NULL,NULL),(36080116,'546669',NULL,'841364841@qq.com',NULL,NULL,'2024-11-22 14:39:14','2024-11-22 14:39:14',NULL,NULL,NULL),(36080117,'122490',NULL,'ganjiabubuzhang@gmail.com',NULL,NULL,'2024-12-27 19:48:05','2024-12-27 19:48:05',NULL,NULL,NULL);

/*Table structure for table `usertablebinding` */

DROP TABLE IF EXISTS `usertablebinding`;

CREATE TABLE `usertablebinding` (
  `bindingId` int(11) NOT NULL AUTO_INCREMENT,
  `userId` int(11) DEFAULT NULL,
  `tableId` int(11) DEFAULT NULL,
  `bindingTime` datetime NOT NULL,
  `reserved1` varchar(255) DEFAULT NULL,
  `reserved2` varchar(255) DEFAULT NULL,
  `reserved3` varchar(255) DEFAULT NULL,
  `reserved4` varchar(255) DEFAULT NULL,
  `reserved5` varchar(255) DEFAULT NULL,
  `reserved6` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`bindingId`),
  KEY `userId` (`userId`),
  KEY `tableId` (`tableId`),
  CONSTRAINT `usertablebinding_ibfk_1` FOREIGN KEY (`userId`) REFERENCES `user` (`userId`) ON UPDATE CASCADE,
  CONSTRAINT `usertablebinding_ibfk_2` FOREIGN KEY (`tableId`) REFERENCES `tables` (`tableId`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

/*Data for the table `usertablebinding` */

insert  into `usertablebinding`(`bindingId`,`userId`,`tableId`,`bindingTime`,`reserved1`,`reserved2`,`reserved3`,`reserved4`,`reserved5`,`reserved6`) values (1,30009257,1,'2024-11-06 15:37:20',NULL,NULL,NULL,NULL,NULL,NULL),(3,36080105,2,'2024-11-08 15:38:39',NULL,NULL,NULL,NULL,NULL,NULL),(4,13258165,3,'2024-11-08 16:25:18',NULL,NULL,NULL,NULL,NULL,NULL);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
