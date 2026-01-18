CREATE DATABASE  IF NOT EXISTS `Bistro` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `Bistro`;-- MySQL dump 10.13  Distrib 8.0.44, for macos15 (arm64)
--
-- Host: localhost    Database: Bistro
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `bills`
--

DROP TABLE IF EXISTS `bills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bills` (
  `bill_number` int NOT NULL AUTO_INCREMENT,
  `total_amount` decimal(10,2) NOT NULL,
  `discount_amount` decimal(10,2) DEFAULT '0.00',
  `bill_date` date NOT NULL,
  `reservation_id` int NOT NULL,
  PRIMARY KEY (`bill_number`),
  KEY `fk_bill_reservation` (`reservation_id`),
  CONSTRAINT `fk_bill_reservation` FOREIGN KEY (`reservation_id`) REFERENCES `reservations` (`reservation_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5011 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bills`
--

LOCK TABLES `bills` WRITE;
/*!40000 ALTER TABLE `bills` DISABLE KEYS */;
INSERT INTO `bills` VALUES (5001,220.00,20.00,'2025-12-25',1),(5002,200.00,0.00,'2026-01-07',9),(5003,500.00,50.00,'2026-01-07',10),(5004,500.00,50.00,'2026-01-07',10),(5005,200.00,20.00,'2026-01-10',15),(5007,200.00,0.00,'2026-01-14',19),(5008,200.00,0.00,'2026-01-17',32),(5009,200.00,20.00,'2026-01-18',60),(5010,200.00,20.00,'2026-01-19',58);
/*!40000 ALTER TABLE `bills` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `monthly_subscriber_report`
--

DROP TABLE IF EXISTS `monthly_subscriber_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `monthly_subscriber_report` (
  `report_year` int NOT NULL,
  `report_month` int NOT NULL,
  `subscriber_orders` int DEFAULT '0',
  `subscriber_waiting_list` int DEFAULT '0',
  `generated_date` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`report_year`,`report_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `monthly_subscriber_report`
--

LOCK TABLES `monthly_subscriber_report` WRITE;
/*!40000 ALTER TABLE `monthly_subscriber_report` DISABLE KEYS */;
INSERT INTO `monthly_subscriber_report` VALUES (2026,1,10,1,'2026-01-19 19:21:44');
/*!40000 ALTER TABLE `monthly_subscriber_report` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `monthly_time_report`
--

DROP TABLE IF EXISTS `monthly_time_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `monthly_time_report` (
  `report_year` int NOT NULL,
  `report_month` int NOT NULL,
  `total_normal` int DEFAULT '0',
  `total_delayed` int DEFAULT '0',
  `total_extended` int DEFAULT '0',
  `generated_date` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`report_year`,`report_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `monthly_time_report`
--

LOCK TABLES `monthly_time_report` WRITE;
/*!40000 ALTER TABLE `monthly_time_report` DISABLE KEYS */;
INSERT INTO `monthly_time_report` VALUES (2025,1,0,0,0,'2026-01-16 19:40:38'),(2026,1,7,2,2,'2026-01-19 19:23:01'),(2026,7,0,0,0,'2026-01-17 18:51:42'),(2026,10,0,0,0,'2026-01-16 19:40:25');
/*!40000 ALTER TABLE `monthly_time_report` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `opening_hours`
--

DROP TABLE IF EXISTS `opening_hours`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `opening_hours` (
  `id` int NOT NULL AUTO_INCREMENT,
  `day` enum('SUNDAY','MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY') NOT NULL,
  `open_time` time NOT NULL,
  `close_time` time NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `opening_hours`
--

LOCK TABLES `opening_hours` WRITE;
/*!40000 ALTER TABLE `opening_hours` DISABLE KEYS */;
INSERT INTO `opening_hours` VALUES (1,'SUNDAY','11:00:00','20:00:00'),(2,'MONDAY','10:00:00','22:00:00'),(3,'TUESDAY','10:00:00','22:00:00'),(4,'WEDNESDAY','10:00:00','22:00:00'),(5,'THURSDAY','10:00:00','23:00:00'),(6,'FRIDAY','09:00:00','15:00:00'),(7,'SATURDAY','10:00:00','00:00:00');
/*!40000 ALTER TABLE `opening_hours` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reports`
--

DROP TABLE IF EXISTS `reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reports` (
  `id` int NOT NULL AUTO_INCREMENT,
  `report_start_date` date NOT NULL,
  `report_end_date` date NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reports`
--

LOCK TABLES `reports` WRITE;
/*!40000 ALTER TABLE `reports` DISABLE KEYS */;
INSERT INTO `reports` VALUES (1,'2025-12-01','2025-12-31');
/*!40000 ALTER TABLE `reports` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `representatives`
--

DROP TABLE IF EXISTS `representatives`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `representatives` (
  `user_id` int NOT NULL,
  `representative_number` varchar(50) NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `representative_number` (`representative_number`),
  CONSTRAINT `representatives_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `representatives`
--

LOCK TABLES `representatives` WRITE;
/*!40000 ALTER TABLE `representatives` DISABLE KEYS */;
INSERT INTO `representatives` VALUES (3,'REP-ADMIN-001'),(2,'REP002');
/*!40000 ALTER TABLE `representatives` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservations`
--

DROP TABLE IF EXISTS `reservations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservations` (
  `reservation_id` int NOT NULL AUTO_INCREMENT,
  `dinners_number` int NOT NULL,
  `confirmation_code` varchar(20) NOT NULL,
  `status` enum('ACTIVE','CANCELED','COMPLETED','CHECKED_IN') DEFAULT 'ACTIVE',
  `table_number` int DEFAULT NULL,
  `subscriber_number` varchar(20) DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `check_in_time` datetime DEFAULT NULL,
  `reminder_sent_at` datetime DEFAULT NULL,
  PRIMARY KEY (`reservation_id`),
  UNIQUE KEY `uq_reservation_code` (`confirmation_code`)
) ENGINE=InnoDB AUTO_INCREMENT=63 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservations`
--

LOCK TABLES `reservations` WRITE;
/*!40000 ALTER TABLE `reservations` DISABLE KEYS */;
INSERT INTO `reservations` VALUES (1,2,'277771','CANCELED',1,NULL,'2026-01-07 12:00:00','2026-01-07 14:00:00','1','','2026-01-06 01:07:07',NULL,NULL),(2,3,'660400','CANCELED',2,NULL,'2026-01-09 14:00:00','2026-01-09 16:00:00','1','','2026-01-06 01:07:17',NULL,NULL),(3,3,'534785','CANCELED',3,NULL,'2026-01-07 12:00:00','2026-01-07 14:00:00','1','','2026-01-06 01:07:21',NULL,NULL),(4,3,'639486','CANCELED',4,NULL,'2026-01-07 12:00:00','2026-01-07 14:00:00','1','','2026-01-06 01:07:22',NULL,NULL),(5,2,'362209','CANCELED',1,'1','2026-01-07 12:00:00','2026-01-07 14:00:00','0500000000','ashrf@test.com','2026-01-06 01:43:55',NULL,NULL),(6,2,'755033','CANCELED',1,NULL,'2026-01-07 18:00:00','2026-01-07 20:00:00','00000','','2026-01-07 14:40:03',NULL,NULL),(7,2,'183706','CANCELED',2,'1','2026-01-07 18:00:00','2026-01-07 20:00:00','0500000000','ashrf@test.com','2026-01-07 14:41:47',NULL,NULL),(8,2,'820635','ACTIVE',1,NULL,'2026-01-21 18:00:00','2026-01-21 20:00:00','12','','2026-01-07 15:24:16',NULL,NULL),(9,2,'901986','COMPLETED',2,NULL,'2026-01-07 15:00:00','2026-01-07 18:30:00','12','','2026-01-07 15:24:52',NULL,NULL),(10,5,'278961','COMPLETED',3,'1','2026-01-07 15:00:00','2026-01-07 20:00:00','0500000000','ashrf@test.com','2026-01-07 15:31:03',NULL,NULL),(11,2,'211293','CANCELED',1,NULL,'2026-01-10 18:00:00','2026-01-10 20:00:00','1','','2026-01-09 13:58:04',NULL,NULL),(12,2,'263509','CANCELED',2,'1','2026-01-10 18:00:00','2026-01-10 20:00:00','0527393342','ashrf@test.com','2026-01-09 23:42:43',NULL,NULL),(13,2,'657707','CANCELED',1,'SUB123','2026-01-15 10:00:00','2026-01-15 12:00:00','0527393342','ashrf@test.com','2026-01-09 23:58:26',NULL,NULL),(14,2,'989304','CANCELED',1,'SUB123','2026-01-16 11:00:00','2026-01-16 13:00:00','0527393342','ashrf@test.com','2026-01-10 00:02:03',NULL,NULL),(15,2,'355210','COMPLETED',1,'SUB123','2026-01-10 00:00:00','2026-01-10 12:00:00','0527393342','ashrf@test.com','2026-01-10 00:16:30',NULL,NULL),(16,2,'305779','CANCELED',2,'SUB123','2026-01-21 17:00:00','2026-01-21 19:00:00','0527393342','ashrf@test.com','2026-01-10 00:30:25',NULL,NULL),(17,2,'749390','CANCELED',1,NULL,'2026-01-15 11:30:00','2026-01-15 13:30:00','١٢٣','','2026-01-10 00:38:51',NULL,NULL),(18,2,'271598','CANCELED',1,'SUB6','2026-01-11 14:00:00','2026-01-11 16:00:00','1234567890','mhmd@test.com','2026-01-10 01:10:21',NULL,NULL),(19,2,'393897','COMPLETED',2,NULL,'2026-01-14 23:00:00','2026-01-15 01:00:00','1234512345','thisistest@gmail.com','2026-01-14 22:52:40','2026-01-14 22:53:58',NULL),(20,6,'205795','CANCELED',3,'SUB123','2026-01-16 19:30:00','2026-01-16 21:30:00','0527393342','ashrfcode@gmail.com','2026-01-16 17:28:16',NULL,NULL),(21,2,'NORM-TEST','COMPLETED',5,NULL,'2026-01-10 18:00:00','2026-01-10 19:00:00','0500000001','test1@gmail.com','2026-01-16 19:48:25','2026-01-10 18:00:00',NULL),(22,2,'DELAY-TEST','COMPLETED',6,NULL,'2026-01-12 18:00:00','2026-01-12 19:30:00','0500000002','test2@gmail.com','2026-01-16 19:48:36','2026-01-12 18:14:00',NULL),(23,2,'REP-001','COMPLETED',1,'SUB-100','2026-01-02 18:00:00','2026-01-02 19:00:00','0501111111','sub1@test.com','2026-01-16 20:18:37','2026-01-02 18:00:00',NULL),(24,4,'REP-002','COMPLETED',2,'SUB-100','2026-01-05 19:00:00','2026-01-05 20:30:00','0501111111','sub1@test.com','2026-01-16 20:18:37','2026-01-05 19:00:00',NULL),(25,2,'REP-003','COMPLETED',3,'SUB-200','2026-01-10 13:00:00','2026-01-10 14:00:00','0502222222','sub2@test.com','2026-01-16 20:18:37','2026-01-10 13:00:00',NULL),(26,3,'REP-004','COMPLETED',4,'SUB-200','2026-01-15 20:00:00','2026-01-15 21:00:00','0502222222','sub2@test.com','2026-01-16 20:18:37','2026-01-15 20:00:00',NULL),(27,2,'REP-005','COMPLETED',1,'SUB-300','2026-01-20 18:00:00','2026-01-20 19:30:00','0503333333','sub3@test.com','2026-01-16 20:18:37','2026-01-20 18:00:00',NULL),(28,2,'184982','CANCELED',1,'SUB123','2026-01-17 15:30:00','2026-01-17 17:30:00','0527393342','ashrfcode@gmail.com','2026-01-17 14:28:41',NULL,NULL),(29,2,'997324','CANCELED',2,'SUB123','2026-01-17 16:00:00','2026-01-17 18:00:00','0527393342','ashrfcode@gmail.com','2026-01-17 14:31:26',NULL,NULL),(30,2,'375501','CANCELED',3,'SUB123','2026-01-17 16:00:00','2026-01-17 18:00:00','0527393342','ashrfcode@gmail.com','2026-01-17 14:31:43',NULL,NULL),(31,2,'761497','CANCELED',4,'SUB123','2026-01-17 16:00:00','2026-01-17 18:00:00','0527393342','ashrfcode@gmail.com','2026-01-17 14:31:45',NULL,NULL),(32,2,'313492','COMPLETED',1,NULL,'2026-01-17 16:00:00','2026-01-17 19:00:00','','ashrfasadi435@gmail.com','2026-01-17 15:54:35','2026-01-17 16:13:29',NULL),(33,2,'706466','ACTIVE',1,'SUB123','2026-01-20 10:00:00','2026-01-20 12:00:00','0527393342','ashrfcode@gmail.com','2026-01-17 17:07:36',NULL,NULL),(34,2,'791072','CANCELED',1,'SUB123','2026-01-18 10:00:00','2026-01-18 12:00:00','0527393342','ashrfcode@gmail.com','2026-01-17 19:43:27',NULL,NULL),(35,2,'105172','CANCELED',2,'SUB123','2026-01-18 10:00:00','2026-01-18 12:00:00','0527393342','ashrfcode@gmail.com','2026-01-17 19:52:05',NULL,NULL),(36,2,'180172','CANCELED',3,'SUB123','2026-01-18 10:00:00','2026-01-18 12:00:00','0527393342','ashrfcode@gmail.com','2026-01-17 19:56:20',NULL,NULL),(37,2,'174412','CANCELED',4,'SUB123','2026-01-18 10:00:00','2026-01-18 12:00:00','0527393342','ashrfcode@gmail.com','2026-01-17 20:05:24',NULL,NULL),(39,2,'538762','CANCELED',1,NULL,'2026-01-17 23:00:00','2026-01-18 01:00:00','1','','2026-01-17 23:11:29',NULL,NULL),(40,6,'151026','CANCELED',3,'SUB8','2026-01-18 12:00:00','2026-01-18 14:00:00','123456789','aya@test.com','2026-01-18 00:00:54',NULL,NULL),(41,6,'765757','CANCELED',4,'SUB8','2026-01-18 12:00:00','2026-01-18 14:00:00','123456789','aya@test.com','2026-01-18 00:01:05',NULL,NULL),(42,6,'334020','CANCELED',3,'SUB8','2026-01-18 14:00:00','2026-01-18 16:00:00','123456789','aya@test.com','2026-01-18 00:01:13',NULL,NULL),(43,6,'224453','CANCELED',4,'SUB8','2026-01-18 14:00:00','2026-01-18 16:00:00','123456789','aya@test.com','2026-01-18 00:01:15',NULL,NULL),(44,6,'978006','CANCELED',3,'SUB8','2026-01-18 16:00:00','2026-01-18 18:00:00','123456789','aya@test.com','2026-01-18 00:01:19',NULL,NULL),(45,2,'802944','CANCELED',1,'SUB8','2026-01-18 16:00:00','2026-01-18 18:00:00','123456789','aya@test.com','2026-01-18 00:01:33',NULL,NULL),(46,2,'461814','CANCELED',2,'SUB8','2026-01-18 16:00:00','2026-01-18 18:00:00','123456789','aya@test.com','2026-01-18 00:01:34',NULL,NULL),(47,2,'542225','CANCELED',4,'SUB8','2026-01-18 16:00:00','2026-01-18 18:00:00','123456789','aya@test.com','2026-01-18 00:01:34',NULL,NULL),(48,2,'730379','CANCELED',1,'SUB8','2026-01-18 12:00:00','2026-01-18 14:00:00','123456789','aya@test.com','2026-01-18 00:02:03',NULL,NULL),(49,2,'521762','CANCELED',2,'SUB8','2026-01-18 12:00:00','2026-01-18 14:00:00','123456789','aya@test.com','2026-01-18 00:02:10',NULL,NULL),(50,2,'881147','CANCELED',1,'SUB8','2026-01-18 14:00:00','2026-01-18 16:00:00','123456789','aya@test.com','2026-01-18 00:02:14',NULL,NULL),(51,2,'793615','CANCELED',2,'SUB8','2026-01-18 14:00:00','2026-01-18 16:00:00','123456789','aya@test.com','2026-01-18 00:02:15',NULL,NULL),(52,6,'370774','CANCELED',3,'SUB8','2026-01-18 18:00:00','2026-01-18 20:00:00','123456789','aya@test.com','2026-01-18 00:02:29',NULL,NULL),(53,6,'356304','CANCELED',4,'SUB8','2026-01-18 18:00:00','2026-01-18 20:00:00','123456789','aya@test.com','2026-01-18 00:02:39',NULL,NULL),(54,2,'405203','CANCELED',1,NULL,'2026-01-19 10:00:00','2026-01-19 12:00:00','٩','','2026-01-18 00:23:00',NULL,NULL),(55,2,'WL891019','CANCELED',1,NULL,'2026-01-19 12:45:32','2026-01-19 14:45:32','0527393342','tssest@gmail.com','2026-01-19 12:45:32',NULL,NULL),(56,2,'WL216775','CANCELED',2,NULL,'2026-01-19 12:45:35','2026-01-19 14:45:35','050','','2026-01-19 12:45:34',NULL,NULL),(57,3,'WL788897','CANCELED',4,NULL,'2026-01-19 12:45:35','2026-01-19 14:45:35','1','1','2026-01-19 12:45:34',NULL,NULL),(58,2,'WL593524','COMPLETED',1,'SUB123','2026-01-19 15:00:17','2026-01-19 17:00:17','0527393342','ashrfcode@gmail.com','2026-01-19 15:00:16','2026-01-19 15:01:02',NULL),(59,2,'WL957302','CANCELED',1,'SUB123','2026-01-19 16:32:51','2026-01-19 18:32:51','0527393342','ashrfcode@gmail.com','2026-01-19 16:32:50',NULL,NULL),(60,2,'WL879951','COMPLETED',2,'SUB123','2026-01-18 01:30:00','2026-01-19 19:59:21','0527393342','ashrfcode@gmail.com','2026-01-19 17:59:20',NULL,NULL),(61,2,'291389','COMPLETED',1,'SUB123','2026-01-19 16:30:00','2026-01-19 18:30:00','0527393342','ashrfcode@gmail.com','2026-01-19 15:00:12','2026-01-19 16:33:19',NULL),(62,2,'858312','ACTIVE',1,'SUB123','2026-01-19 20:00:00','2026-01-19 22:00:00','0527393342','ashrfcode@gmail.com','2026-01-19 18:33:20',NULL,NULL);
/*!40000 ALTER TABLE `reservations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `restaurant_tables`
--

DROP TABLE IF EXISTS `restaurant_tables`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `restaurant_tables` (
  `table_number` int NOT NULL,
  `capacity` int NOT NULL,
  `location` varchar(50) DEFAULT NULL,
  `status` enum('EMPTY','OCCUPIED') DEFAULT 'EMPTY',
  PRIMARY KEY (`table_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `restaurant_tables`
--

LOCK TABLES `restaurant_tables` WRITE;
/*!40000 ALTER TABLE `restaurant_tables` DISABLE KEYS */;
INSERT INTO `restaurant_tables` VALUES (1,2,'VIP Room','EMPTY'),(2,4,'Inside','EMPTY'),(4,8,'VIP Room','EMPTY');
/*!40000 ALTER TABLE `restaurant_tables` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `special_opening_hours`
--

DROP TABLE IF EXISTS `special_opening_hours`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `special_opening_hours` (
  `id` int NOT NULL AUTO_INCREMENT,
  `special_date` date NOT NULL,
  `open_time` time DEFAULT NULL,
  `close_time` time DEFAULT NULL,
  `is_closed` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `special_date` (`special_date`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `special_opening_hours`
--

LOCK TABLES `special_opening_hours` WRITE;
/*!40000 ALTER TABLE `special_opening_hours` DISABLE KEYS */;
INSERT INTO `special_opening_hours` VALUES (1,'2025-12-31',NULL,NULL,1),(2,'2025-12-24','10:00:00','01:00:00',0),(3,'2026-01-17','10:00:00','00:00:00',0),(5,'2026-01-18',NULL,NULL,1);
/*!40000 ALTER TABLE `special_opening_hours` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subscribers`
--

DROP TABLE IF EXISTS `subscribers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subscribers` (
  `user_id` int NOT NULL,
  `subscriber_number` varchar(50) NOT NULL,
  `digital_card` varchar(100) NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `subscriber_number` (`subscriber_number`),
  CONSTRAINT `subscribers_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subscribers`
--

LOCK TABLES `subscribers` WRITE;
/*!40000 ALTER TABLE `subscribers` DISABLE KEYS */;
INSERT INTO `subscribers` VALUES (1,'SUB123','CARD123'),(6,'SUB6','CARD6'),(7,'SUB7','CARD7'),(8,'SUB8','CARD8');
/*!40000 ALTER TABLE `subscribers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tags`
--

DROP TABLE IF EXISTS `tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tags` (
  `id` int NOT NULL AUTO_INCREMENT,
  `activity_history` text NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tags`
--

LOCK TABLES `tags` WRITE;
/*!40000 ALTER TABLE `tags` DISABLE KEYS */;
/*!40000 ALTER TABLE `tags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `email` varchar(120) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `password` varchar(100) NOT NULL,
  `role` enum('SUBSCRIBER','REPRESENTATIVE','MANAGER') NOT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES
(1,'Ashrf','ashrfcode@gmail.com','0527393342','1234','SUBSCRIBER',1,'2025-12-17 17:37:36'),
(2,'Mona','mona@test.com','0501111111','1234','REPRESENTATIVE',1,'2025-12-17 18:05:58'),
(3,'test','test@test.com','123456789','1234','MANAGER',1,'2025-12-17 18:05:50'),
(6,'mhmd','mhmd@gmail.com','1234567890','1234','SUBSCRIBER',1,'2026-01-09 22:54:29'),
(7,'asad','asad@test.com','1234123412','1234','SUBSCRIBER',1,'2026-01-14 23:52:57'),
(8,'aya','aya@test.com','123456789','1234','SUBSCRIBER',1,'2026-01-17 21:59:05');

/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `waiting_list`
--

DROP TABLE IF EXISTS `waiting_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `waiting_list` (
  `id` int NOT NULL AUTO_INCREMENT,
  `request_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `subscriber_number` varchar(20) DEFAULT NULL,
  `diners_number` int NOT NULL DEFAULT '1',
  `guest_phone` varchar(20) DEFAULT NULL,
  `guest_email` varchar(255) DEFAULT NULL,
  `confirmation_code` varchar(20) NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'WAITING',
  `invited_at` timestamp NULL DEFAULT NULL,
  `expires_at` timestamp NULL DEFAULT NULL,
  `assigned_table` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_waiting_confirmation_code` (`confirmation_code`),
  KEY `fk_waitinglist_subscriber` (`subscriber_number`),
  CONSTRAINT `fk_waitinglist_subscriber` FOREIGN KEY (`subscriber_number`) REFERENCES `subscribers` (`subscriber_number`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `waiting_list`
--

LOCK TABLES `waiting_list` WRITE;
/*!40000 ALTER TABLE `waiting_list` DISABLE KEYS */;
INSERT INTO `waiting_list` VALUES (3,'2026-01-14 20:02:23',NULL,2,'0527393342','tssest@gmail.com','WL891019','EXPIRED','2026-01-19 10:45:32','2026-01-19 11:00:32',1),(4,'2026-01-14 21:46:51',NULL,2,'050','','WL216775','EXPIRED','2026-01-19 10:45:34','2026-01-19 11:00:34',2),(17,'2026-01-17 19:04:59',NULL,3,'1','1','WL788897','EXPIRED','2026-01-19 10:45:34','2026-01-19 11:00:34',4),(24,'2026-01-19 15:33:52','SUB123',2,'','','WL879951','INVITED','2026-01-19 15:59:20','2026-01-19 16:14:20',2);
/*!40000 ALTER TABLE `waiting_list` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-19 19:53:29
