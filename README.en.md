# Tonarinet Backend Server

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Description

This is the backend server for the Tonarinet project, built with Spring Boot. It provides the core functionalities for a community-based service platform, including user management, content sharing, real-time communication, and more.

## Features

-   **User Authentication:** Secure sign-up and sign-in using JWT (JSON Web Tokens) and OAuth2.
-   **RESTful API:** A comprehensive set of APIs for managing articles, comments, users, and other resources.
-   **Real-time Chat:** WebSocket-based real-time chat functionality.
-   **Content Management:** Features for creating, reading, updating, and deleting articles and posts.
-   **Task & Party Management:** Functionality for organizing and participating in tasks and events.
-   **AI Integration:** Utilizes AI for features like content recommendation.
-   **API Documentation:** API documentation generated with Swagger.

## Installation

### Prerequisites

-   Java 17
-   Gradle 8.x
-   MySQL

### Setup

1.  **Clone the repository**
    ```bash
    git clone https://github.com/scit4bits/tonarinet-be-springboot.git
    cd tonarinet-be-springboot
    ```

2.  **Database Setup**
    -   Create a MySQL database.
    -   Execute the `sql/tonarinet_db.sql` script to create the necessary tables.
    ```bash
    mysql -u [your_username] -p [your_database_name] < sql/tonarinet_db.sql
    ```

3.  **Configure Application**
    -   Open `src/main/resources/application.properties`.
    -   Update the database connection details (`spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`).
    -   Configure your JWT secret key, OAuth2 credentials, and any other external service settings.

## Usage

### Running the Application

You can run the application using the Gradle wrapper:

```bash
./gradlew bootRun
```

The server will start on `http://localhost:8080`.

### API Documentation

Once the application is running, you can access the Swagger UI for API documentation at:
`http://localhost:8080/swagger-ui.html`

## Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m '''Add some AmazingFeature'''`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

## License

This project is licensed under the MIT License. See the `LICENSE` file for more information.

## Contact

Project Link: [https://github.com/scit4bits/tonarinet-be-springboot](https://github.com/scit4bits/tonarinet-be-springboot)
