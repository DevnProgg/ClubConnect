# Club Connect — University Club Management System

![Java](https://img.shields.io/badge/Java-17%2B-orange?logo=java)
![MySQL](https://img.shields.io/badge/MySQL-8.0%2B-blue?logo=mysql)
![Platform](https://img.shields.io/badge/Platform-Desktop-lightgrey)
![Status](https://img.shields.io/badge/Status-Active-success)

> A university club management system built in **Java**, designed to streamline the management of clubs, members, and activities.

---

## Table of Contents

- [About](#-about)
- [Features](#-features)
- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [Running the Application](#-running-the-application)
- [Notes](#-notes)
- [Author](#-author)

---

## About

**Club Connect** is a Java-based desktop application that enables university administrators and club leaders to efficiently manage student clubs, memberships, and events.  
It offers an intuitive structure for managing club records and supports database persistence via **MySQL**.

---

## Features

- Create and manage clubs  
- Handle membership requests and approvals  
- Maintain member profiles and records  
- Discussion forum support 
- Database-driven persistence  
-Simple and extensible Dark Themed Java Swing-based UI  

---

## Prerequisites

Before you run the application, make sure you have these installed:

- **Java JDK 17+**
- **MySQL 8.0+**

> **Recommended Setup (Docker):**
> ```bash
> docker run -d \
>   --name clubconnect-db \
>   -e MYSQL_ROOT_PASSWORD=yourpassword \
>   -e MYSQL_DATABASE=clubconnect \
>   -v mysql_data:/var/lib/mysql \
>   -p 3306:3306 \
>   mysql:8
> ```

---

## Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/clubconnect.git
   cd clubconnect
   ```

---

## Running The Application
1. **Navigate to the output folder**
```bash
cd out/artifact/clubconnect_0_1_jar/
```
2. **Run the jar**
```bash
java -jar "clubconnect 01.jar"
```

---

## Notes

-Ensure your MySQL server or Docker container is running before launching the app.

-The update and GUI enhancement features are under development.

-Compatible with Windows, macOS, and Linux (with Java installed).

---

## Author

Khalapa Qokolo
University Project — Club Management System (Java)

---