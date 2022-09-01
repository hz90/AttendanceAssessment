/*
SQLyog Community v13.1.6 (64 bit)
MySQL - 5.7.32 : Database - test001
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`test001` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;

USE `test001`;

/*Table structure for table `attendancerecords` */

DROP TABLE IF EXISTS `attendancerecords`;

CREATE TABLE `attendancerecords` (
  `id` INT NOT NULL COMMENT 'id',
  `userid` VARCHAR(10) NOT NULL COMMENT '用户ID',
  `record_date` DATE NOT NULL COMMENT '考勤日期',
  `status_id` int(10) NOT NULL COMMENT '考勤状态',
  `createtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `createuser` varchar(128) DEFAULT NULL,
  `updatetime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updateuser` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`userid`,`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='考勤记录';


/*Table structure for table `record_status` */

DROP TABLE IF EXISTS `record_status`;

CREATE TABLE `record_status` (
  `status_id` int(10) NOT NULL,
  `record_status` varchar(10) NOT NULL COMMENT '考勤状态',
  PRIMARY KEY (`status_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='考勤状态';


/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;