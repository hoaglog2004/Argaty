# 🎮 Argaty - Gaming Gear E-Commerce Platform

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A modern, feature-rich e-commerce platform specialized in gaming gear and accessories. Built with Java Spring Boot and Thymeleaf for a seamless shopping experience.

## 🌟 Features

### Customer Features
- **🏠 Public Access**
  - Browse products without login
  - View product details, categories, and brands
  - Search and filter products
  - View promotions and featured items
  
- **🛒 Shopping**
  - Add items to cart (no login required)
  - Wishlist management (login required)
  - Secure checkout process
  - Apply vouchers and discounts
  
- **👤 User Account**
  - User registration and login
  - Profile management
  - Order history tracking
  - Multiple delivery addresses
  - Password reset via email
  - Review and rate products

### Admin Features
- **📊 Dashboard**
  - Sales statistics and analytics
  - Order management
  - Inventory tracking
  
- **🛍️ Product Management**
  - CRUD operations for products
  - Product variants and images
  - Category and brand management
  
- **👥 User Management**
  - View and manage users
  - Role-based access control
  
- **🎁 Marketing**
  - Voucher/coupon management
  - Banner management
  - Featured products
  
- **📝 Content Management**
  - Review moderation
  - Order status updates
  - Notification system

## 🏗️ Technology Stack

### Backend
- **Java 17** - Core programming language
- **Spring Boot 4.0.1** - Application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database access
- **Hibernate** - ORM framework
- **SQL Server** - Database
- **Lombok** - Reduce boilerplate code
- **ModelMapper** - Object mapping

### Frontend
- **Thymeleaf** - Server-side template engine
- **Thymeleaf Layout Dialect** - Template layouts
- **HTML5 & CSS3** - Markup and styling
- **JavaScript** - Client-side interactivity
- **Boxicons** - Icon library
- **Google Fonts** - Typography

### Build & Deployment
- **Maven** - Dependency management and build tool
- **Spring Boot DevTools** - Development tools
- **BCrypt** - Password encryption

## 📋 Prerequisites

- Java JDK 17 or higher
- Maven 3.6+
- SQL Server 2019 or higher
- Git

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/hoaglog2004/Argaty.git
cd Argaty
```

### 2. Configure Database
Create a SQL Server database and update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=ArgatyDB;encrypt=true;trustServerCertificate=true
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Configure Email (Optional)
For password reset functionality, configure SMTP settings:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME:}
spring.mail.password=${MAIL_PASSWORD:}
app.mail.from=${MAIL_FROM:${spring.mail.username:noreply@argaty.com}}
app.base-url=${APP_BASE_URL:http://localhost:8080}
```

Example (PowerShell):

```powershell
$env:MAIL_USERNAME="your-email@gmail.com"
$env:MAIL_PASSWORD="your-app-password"
$env:MAIL_FROM="Argaty <your-email@gmail.com>"
$env:APP_BASE_URL="http://localhost:8080"
```

### 4. Build the Application
```bash
mvn clean install
```

### 5. Run the Application
```bash
mvn spring-boot:run
```

The application will be available at: `http://localhost:8080`

## 📁 Project Structure

```
Argaty/
├── src/
│   ├── main/
│   │   ├── java/com/argaty/
│   │   │   ├── config/           # Configuration classes
│   │   │   ├── controller/       # MVC Controllers
│   │   │   │   ├── admin/        # Admin controllers
│   │   │   │   ├── api/          # REST API controllers
│   │   │   │   ├── auth/         # Authentication controllers
│   │   │   │   └── user/         # User controllers
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   │   ├── request/      # Request DTOs
│   │   │   │   └── response/     # Response DTOs
│   │   │   ├── entity/           # JPA Entities
│   │   │   ├── enums/            # Enumerations
│   │   │   ├── exception/        # Custom exceptions
│   │   │   ├── repository/       # Spring Data repositories
│   │   │   ├── service/          # Business logic services
│   │   │   │   └── impl/         # Service implementations
│   │   │   └── util/             # Utility classes
│   │   └── resources/
│   │       ├── static/           # Static resources
│   │       │   ├── css/          # Stylesheets
│   │       │   ├── js/           # JavaScript files
│   │       │   └── images/       # Images
│   │       ├── templates/        # Thymeleaf templates
│   │       │   ├── admin/        # Admin pages
│   │       │   ├── auth/         # Authentication pages
│   │       │   ├── error/        # Error pages
│   │       │   ├── fragments/    # Reusable fragments
│   │       │   ├── layouts/      # Layout templates
│   │       │   └── user/         # User pages
│   │       └── application.properties
│   └── test/                     # Test files
├── pom.xml                       # Maven configuration
└── README.md                     # This file
```

## 🔒 Security Features

- **Authentication**: Form-based login with email and password
- **Authorization**: Role-based access control (USER, ADMIN)
- **Password Security**: BCrypt encryption
- **CSRF Protection**: Enabled for all POST requests
- **Session Management**: Secure session handling
- **Password Reset**: Token-based password reset via email
- **SQL Injection Prevention**: Parameterized queries
- **XSS Protection**: Thymeleaf auto-escaping

## 🌐 Access Control

### Public Routes (No authentication required)
- `/` - Home page
- `/products/**` - Product listing and details
- `/categories/**` - Category pages
- `/brands/**` - Brand pages
- `/about`, `/contact`, `/faq` - Information pages
- `/auth/**` - Login, register, forgot password

### Protected Routes (Authentication required)
- `/cart/**` - Shopping cart
- `/checkout/**` - Checkout process
- `/profile/**` - User profile and settings
- `/wishlist/**` - Wishlist management
- `/api/cart/**`, `/api/wishlist/**` - User APIs

### Admin Routes (ADMIN role required)
- `/admin/**` - Admin dashboard and management

## 🗄️ Database Schema

### Main Tables
- **users** - User accounts and authentication
- **products** - Product catalog
- **product_variants** - Product variations (size, color, etc.)
- **product_images** - Product images
- **categories** - Product categories
- **brands** - Product brands
- **orders** - Customer orders
- **order_items** - Order line items
- **carts** - Shopping carts
- **cart_items** - Cart items
- **wishlists** - User wishlists
- **reviews** - Product reviews
- **vouchers** - Discount vouchers
- **banners** - Marketing banners
- **user_addresses** - Customer addresses
- **notifications** - User notifications
- **password_reset_tokens** - Password reset tokens

## 🎨 UI/UX Features

- **Responsive Design**: Mobile-first approach
- **Cosmic Gaming Theme**: Modern, futuristic design
- **Dark Mode**: Gaming-optimized color scheme
- **Smooth Animations**: Enhanced user experience
- **Loading States**: User feedback for async operations
- **Form Validation**: Client and server-side validation
- **Error Handling**: Meaningful error messages
- **Pagination**: Efficient data browsing

## 🧪 Testing

Run tests with:
```bash
mvn test
```

## 📦 Deployment

### Production Build
```bash
mvn clean package -DskipTests
```

The built JAR file will be in `target/Argaty-0.0.1-SNAPSHOT.jar`

### Run Production Build
```bash
java -jar target/Argaty-0.0.1-SNAPSHOT.jar
```

## 🔧 Configuration

Key configuration options in `application.properties`:

```properties
# Server
server.port=8080

# Database
spring.jpa.hibernate.ddl-auto=update

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=50MB

# Session
server.servlet.session.timeout=30m

# Pagination
app.pagination.products-per-page=12
app.pagination.orders-per-page=10

# Shipping
app.shipping.default-fee=30000
app.shipping.free-threshold=500000
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Authors

- **Argaty Team** - Initial work

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Thymeleaf team for the template engine
- All contributors and supporters

## 📞 Support

For support, email support@argaty.com or open an issue in the repository.

---

**Made with ❤️ by Argaty Team**
