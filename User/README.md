# MySQL Spring Boot Setup Guide

This guide will help you set up MySQL database in a Spring Boot project on macOS.

## Prerequisites

- macOS
- Java 17 or higher
- Spring Boot project
- Homebrew (package manager)

## 1. Install MySQL on Mac

### Install using Homebrew

```bash
# Install Homebrew (if not already installed)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install MySQL
brew install mysql

# Start MySQL service
brew services start mysql

# Secure MySQL installation (recommended)
mysql_secure_installation
```

### Verify Installation

```bash
mysql --version
```

## 2. Create Database and User

```bash
# Login to MySQL (default root password is empty)
mysql -u root -p
```

Execute the following SQL commands:

```sql
-- Create database
CREATE DATABASE your_database_name;

-- Create user (optional but recommended)
CREATE USER 'your_username'@'localhost' IDENTIFIED BY 'your_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON your_database_name.* TO 'your_username'@'localhost';
FLUSH PRIVILEGES;

-- Exit MySQL
exit;
```

## 3. Add Dependencies

### For Maven (pom.xml)

```xml
<dependencies>
    <!-- MySQL Driver -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Spring Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
</dependencies>
```

### For Gradle (build.gradle)

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    runtimeOnly 'com.mysql:mysql-connector-j'
}
```

## 4. Configure Database Connection

### application.properties

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/your_database_name?serverTimezone=UTC
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.properties.hibernate.format_sql=true
```

### application.yml (Alternative)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database_name?serverTimezone=UTC
    username: your_username
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true
```

### Configuration Options Explained

- `ddl-auto=update`: Automatically updates database schema
    - `create`: Creates schema, destroys previous data
    - `create-drop`: Creates schema, drops when session ends
    - `validate`: Validates schema, makes no changes
    - `none`: No schema changes
- `show-sql=true`: Logs SQL statements to console
- `format_sql=true`: Formats SQL for better readability

## 5. Create Entity Class

```java
package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    // Constructors
    public User() {}
    
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}
```

## 6. Create Repository Interface

```java
package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```

## 7. Create Service Layer (Optional)

```java
package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
```

## 8. Test Connection

Run your Spring Boot application:

```bash
./mvnw spring-boot:run
# or
./gradlew bootRun
```

Look for successful connection logs in the console.

## Troubleshooting

### MySQL Not Running

```bash
# Check MySQL status
brew services list

# Start MySQL
brew services start mysql

# Restart MySQL
brew services restart mysql
```

### Connection Refused Error

- Verify MySQL is running
- Check port 3306 is not blocked
- Verify database name, username, and password

### Time Zone Error

Add timezone parameter to your URL:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/your_database_name?serverTimezone=UTC
```

### Authentication Plugin Error

Run in MySQL:
```sql
ALTER USER 'your_username'@'localhost' IDENTIFIED WITH mysql_native_password BY 'your_password';
FLUSH PRIVILEGES;
```

### Port Already in Use

Change MySQL port:
```bash
# Edit MySQL configuration
nano /opt/homebrew/etc/my.cnf

# Add under [mysqld]
port = 3307

# Restart MySQL
brew services restart mysql
```

Update your Spring Boot configuration:
```properties
spring.datasource.url=jdbc:mysql://localhost:3307/your_database_name
```

## Useful MySQL Commands

```bash
# Stop MySQL
brew services stop mysql

# Uninstall MySQL
brew uninstall mysql

# Access MySQL CLI
mysql -u root -p

# Show databases
SHOW DATABASES;

# Use database
USE your_database_name;

# Show tables
SHOW TABLES;

# Describe table
DESCRIBE users;
```

## Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [MySQL Documentation](https://dev.mysql.com/doc/)

## Notes

- Keep your database credentials secure
- Never commit `application.properties` with real credentials to version control
- Use environment variables or configuration servers for production
- Regular database backups are recommended

---

**Happy Coding!** 🚀