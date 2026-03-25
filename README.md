<h1 align="center">🍽️ Bistro - Restaurant Management System</h1>

<p align="center">
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white" alt="Java"/>
  <img src="https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL"/>
  <img src="https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white" alt="CSS"/>
</p>

> **G11_Assignment3_Project** is a robust, client-server restaurant management system built in Java. Designed to streamline restaurant operations, Bistro handles everything from table reservations and billing to backend database management.

---

## 📑 Table of Contents
- [Features](#-features)
- [Repository Structure](#-repository-structure)
- [Technologies Used](#️-technologies-used)
- [Getting Started](#️-getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation & Setup](#installation--setup)

---

## 🚀 Features

* **Reservation Management:** Efficiently book, track, and manage customer table reservations.
* **Billing System:** Process orders and generate accurate bills for restaurant patrons.
* **Database Management:** Reliable data storage and retrieval for menus, users, and daily operational records.
* **Client-Server Architecture:** Secure and synchronized communication between the restaurant staff (clients) and the central database (server) using the OCSF framework.

## 📁 Repository Structure

The workspace is divided into distinct modules to separate client-side GUI and server-side logic:

| Directory/File | Description |
|---|---|
| 💻 `BistroClient/` | Front-end Java application and UI styling (CSS) used by restaurant staff. |
| 🗄️ `BistroServer/` | Back-end Java application responsible for processing requests and interfacing with the database. |
| 🔗 `SharedFiles/` | Common classes, models, and utilities shared across both the client and server. |
| 🌐 `OCSF/` | The Object Client-Server Framework library handling the network communication layer. |
| 🗃️ `11_Assignment3_DB.sql`| The SQL script containing the database schema and initial data setup. |
| 📦 `*.jar` files | `G11_client.jar` & `G11_server.jar` are compiled, runnable Java archives for immediate deployment. |
| 📚 `JavaDoc.zip` | Comprehensive generated documentation for the project's codebase. |
| 📐 `G11_Assignment3.vpp`| Visual Paradigm project file containing system design diagrams (UML, etc.). |

## 🛠️ Technologies Used

* **Language:** Java (88.9%), CSS (11.1%)
* **Networking:** OCSF (Object Client-Server Framework)
* **Database:** SQL / MySQL

## ⚙️ Getting Started

### Prerequisites
Before you begin, ensure you have met the following requirements:
* **Java Development Kit (JDK):** Version 8 or higher installed.
* **Database:** A running MySQL/SQL database server instance.

### Installation & Setup

**1. Database Setup:** Execute the SQL script in your database environment to create the necessary tables and populate the initial data:
```sql
SOURCE path/to/11_Assignment3_DB.sql;
