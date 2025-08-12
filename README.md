# Tonarinet BE Spring Boot

Tonarinet BE Spring Boot is a backend service built with Spring Boot for the Tonarinet project.

## Features

- RESTful API endpoints
- Database integration (JPA/Hibernate)
- User authentication and authorization
- Modular architecture

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL (or your preferred database)

### Installation

```bash
git clone https://github.com/scit4bits/tonarinet-be-springboot.git
cd tonarinet-be-springboot
./gradlew bootRun
```

### Configuration

Update `src/main/resources/application.properties` with your database credentials.

### Running the Application

```bash
mvn spring-boot:run
```

## API Documentation

API endpoints are documented using Swagger. Access it at `http://localhost:8080/swagger-ui.html` after starting the application.

## Contributing

Contributions are welcome! Please open issues or submit pull requests.

## License

This project is licensed under the MIT License.
