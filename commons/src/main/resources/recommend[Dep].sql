/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50638
Source Host           : localhost:3306
Source Database       : recommend

Target Server Type    : MYSQL
Target Server Version : 50638
File Encoding         : 65001

Date: 2017-11-23 09:08:49
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for averagemovies
-- ----------------------------
DROP TABLE IF EXISTS `averagemovies`;
CREATE TABLE `averagemovies` (
  `mid` int(11) DEFAULT NULL,
  `avg` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for genrestopmovies
-- ----------------------------
DROP TABLE IF EXISTS `genrestopmovies`;
CREATE TABLE `genrestopmovies` (
  `genres` text,
  `recs` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for movie
-- ----------------------------
DROP TABLE IF EXISTS `movie`;
CREATE TABLE `movie` (
  `mid` int(11) DEFAULT NULL,
  `name` text,
  `descri` text,
  `timelong` text,
  `issue` text,
  `shoot` text,
  `language` text,
  `genres` text,
  `actors` text,
  `directors` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for movierating
-- ----------------------------
DROP TABLE IF EXISTS `movierating`;
CREATE TABLE `movierating` (
  `uid` int(11) DEFAULT NULL,
  `mid` int(11) DEFAULT NULL,
  `score` double DEFAULT NULL,
  `timestamp` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for movierecs
-- ----------------------------
DROP TABLE IF EXISTS `movierecs`;
CREATE TABLE `movierecs` (
  `mid` int(11) DEFAULT NULL,
  `recs` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for ratemoremovies
-- ----------------------------
DROP TABLE IF EXISTS `ratemoremovies`;
CREATE TABLE `ratemoremovies` (
  `mid` int(11) DEFAULT NULL,
  `count` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for ratemorerecentlymovies
-- ----------------------------
DROP TABLE IF EXISTS `ratemorerecentlymovies`;
CREATE TABLE `ratemorerecentlymovies` (
  `mid` int(11) DEFAULT NULL,
  `count` bigint(20) NOT NULL,
  `yeahmonth` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for streamrecs
-- ----------------------------
DROP TABLE IF EXISTS `streamrecs`;
CREATE TABLE `streamrecs` (
  `uid` int(11) DEFAULT NULL,
  `recs` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
  `uid` int(11) DEFAULT NULL,
  `mid` int(11) DEFAULT NULL,
  `tag` text,
  `timestamp` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `uid` int(11) NOT NULL,
  `username` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `first` bit(1) DEFAULT NULL,
  `timestamp` int(11) DEFAULT NULL,
  `prefgenres` text,
  PRIMARY KEY (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for userrecs
-- ----------------------------
DROP TABLE IF EXISTS `userrecs`;
CREATE TABLE `userrecs` (
  `uid` int(11) DEFAULT NULL,
  `recs` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
