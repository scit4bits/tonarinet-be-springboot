# Tonarinet となりネット - Backend

[![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=springboot&logoColor=fff)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-00000F?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0)

**Demo Site: https://tn.thxx.xyz**

## Introduction

**Tonarinet** is an integrated management and support platform for international students and foreign workers. This project is the backend server for a team project (4bits) conducted in the SMART Cloud IT Master 47th course.

University and company administrators can efficiently manage international students and foreign workers through this platform, and users are provided with various information and community functions necessary for adapting to local life.

## Tech Stack

- **Language:** Java 17
- **Framework:** Spring Boot 3.5.4
- **Database:** MySQL
- **Authentication:** Spring Security, JWT, OAuth 2.0 (Google, Kakao, LINE)
- **API:** REST API with Swagger (springdoc-openapi)
- **Real-time Communication:** WebSocket (with STOMP)
- **AI:** Spring AI (OpenAI GPT-5-mini)
- **ORM:** Spring Data JPA (Hibernate)
- **Mail:** Spring Boot Starter Mail (OCI Email Delivery)
- **Build:** Gradle

## Getting Started

### Environment Variables

To run this project, you need to create a `.env` file in the project root and set the following environment variables.

```
MYSQL_URL=jdbc:mysql://<host>:<port>/<database>
MYSQL_USER=<username>
MYSQL_PASSWORD=<password>

UPLOAD_PATH=<upload_directory_path>

LINE_API_CLIENT_ID=...
LINE_API_CLIENT_SECRET=...
LINE_API_REDIRECT_URI=...

GOOGLE_API_CLIENT_ID=...
GOOGLE_API_CLIENT_SECRET=...
GOOGLE_API_REDIRECT_URI=...

KAKAO_CLIENT_ID=...
KAKAO_REDIRECT_URI=...
KAKAO_CLIENT_SECRET=...

JWT_SECRET_KEY=...

SPRING_MAIL_HOST=...
SPRING_MAIL_PORT=...
SPRING_MAIL_USERNAME=...
SPRING_MAIL_PASSWORD=...

OPENAI_API_KEY=...
GOOGLE_TRANS_API_KEY=...

SWAGGER_AUTH_USERNAME=...
SWAGGER_AUTH_PASSWORD=...
```

### Running the application

The command to build and run the project is as follows:

```bash
./gradlew bootRun
```

API documentation can be found at `http://localhost:8999/swagger-ui/index.html` after the server starts.

## Technical Features

- **REST API & Swagger Documentation:** Provides Swagger UI for API specification and testing using springdoc-openapi.
- **Authentication & Authorization:** Built a stateless authentication/authorization system by combining Spring Security and JWT. It also supports social logins for Google, Kakao, and LINE through OAuth 2.0.
- **Real-time Chat:** Implemented real-time chat functionality between users and administrators using WebSocket and STOMP protocol.
- **AI Integration:** Provides various intelligent services such as AI chatbot and task recommendations by integrating OpenAI's language model through Spring AI.
- **Database Management:** Efficiently manages and manipulates data using Spring Data JPA and JPQL.
- **Email Service:** Sends emails for membership authentication, password reset, etc. by linking with OCI (Oracle Cloud Infrastructure)'s Email Delivery service.



## License

This project is licensed under the [GNU LGPLv3](LICENSE.md) License.