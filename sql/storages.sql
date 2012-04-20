/*
Navicat MySQL Data Transfer

Source Server         : gms
Source Server Version : 50144
Source Host           : localhost:3306
Source Database       : moopledev

Target Server Type    : MYSQL
Target Server Version : 50144
File Encoding         : 65001

Date: 2011-05-23 23:35:45
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `storages`
-- ----------------------------
DROP TABLE IF EXISTS `storages`;
CREATE TABLE `storages` (
  `storageid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `accountid` int(11) NOT NULL DEFAULT '0',
  `world` int(2) NOT NULL,
  `slots` int(11) NOT NULL DEFAULT '0',
  `meso` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`storageid`),
  KEY `accountid` (`accountid`),
  CONSTRAINT `storages_ibfk_1` FOREIGN KEY (`accountid`) REFERENCES `accounts` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of storages
-- ----------------------------
