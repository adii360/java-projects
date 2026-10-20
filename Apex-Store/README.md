# ApexStore - Pure Jakarta EE 11 / Java 25 E-Commerce Web Application

A full-stack, database-driven e-commerce platform built strictly using **traditional Java web development technologies**: Java Servlets (Jakarta EE 6.1 / Tomcat 11), JDBC, raw SQL queries, and MySQL 8.x.

Designed and architected without heavyweight frameworks (No Spring Boot, No Spring MVC, No Hibernate, No JPA, No ORMs) to provide an authentic, deep understanding of request lifecycles, HTTP sessions, filter chains, connection transactions, and database connectivity.

---

## 🛠️ Technology Stack

- **Java**: 25 (OpenJDK / Oracle JDK)
- **Web / Servlet Container**: Apache Tomcat 11.0.x
- **Servlet Specification**: Jakarta Servlet API 6.1.0 (`jakarta.servlet.*`)
- **Server-Side Templating**: Jakarta Server Pages 3.1 & JSTL 3.0 (Glassfish implementation)
- **Persistence Layer**: JDBC (Java Database Connectivity) with PreparedStatements
- **Database**: MySQL 8.x (InnoDB engine with Foreign Keys and Transactions)
- **Driver**: MySQL Connector/J 8.3.0
- **Security & Cryptography**: BCrypt (jBCrypt 0.4) for salted password hashing, Cryptographic CSRF Tokens
- **Build & Dependency Management**: Apache Maven 3.9+
- **Frontend**: HTML5, Modern Vanilla CSS3 (Custom Design System with CSS variables), Vanilla JavaScript (ES6+)

---

## 📁 Project Architecture

```
ecommerce/
├── pom.xml                               # Maven project configuration (Java 25, Tomcat 11, Jakarta EE)
├── README.md                             # Comprehensive technical documentation & deployment guide
├── database/
│   ├── schema.sql                        # 11 InnoDB relational tables with constraints and indexes
│   └── sample-data.sql                   # Seed data (5 categories, 12 products, coupons, demo users)
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/ecommerce/
│   │   │       ├── model/                # Domain models (User, Product, Category, CartItem, Order, etc.)
│   │   │       ├── dao/                  # JDBC Data Access Objects with PreparedStatements
│   │   │       │   ├── UserDAO.java
│   │   │       │   ├── ProductDAO.java
│   │   │       │   ├── CategoryDAO.java
│   │   │       │   ├── CartDAO.java
│   │   │       │   ├── WishlistDAO.java
│   │   │       │   ├── AddressDAO.java
│   │   │       │   ├── OrderDAO.java
│   │   │       │   ├── ReviewDAO.java
│   │   │       │   ├── CouponDAO.java
│   │   │       │   └── ContactMessageDAO.java
│   │   │       ├── service/              # Business logic & transaction boundaries
│   │   │       │   ├── UserService.java
│   │   │       │   ├── ProductService.java
│   │   │       │   ├── CartService.java
│   │   │       │   ├── OrderService.java   # Multi-step atomic JDBC transactions
│   │   │       │   └── AdminService.java
│   │   │       ├── servlet/              # HTTP Request Controllers (Jakarta Servlets)
│   │   │       │   ├── HomeServlet.java
│   │   │       │   ├── RegisterServlet.java
│   │   │       │   ├── LoginServlet.java
│   │   │       │   ├── LogoutServlet.java
│   │   │       │   ├── ProfileServlet.java
│   │   │       │   ├── ProductListServlet.java
│   │   │       │   ├── ProductDetailServlet.java
│   │   │       │   ├── SearchServlet.java
│   │   │       │   ├── CartServlet.java
│   │   │       │   ├── AddToCartServlet.java
│   │   │       │   ├── UpdateCartServlet.java
│   │   │       │   ├── RemoveCartItemServlet.java
│   │   │       │   ├── WishlistServlet.java
│   │   │       │   ├── AddToWishlistServlet.java
│   │   │       │   ├── RemoveFromWishlistServlet.java
│   │   │       │   ├── AddressServlet.java
│   │   │       │   ├── CheckoutServlet.java
│   │   │       │   ├── PlaceOrderServlet.java
│   │   │       │   ├── OrderHistoryServlet.java
│   │   │       │   ├── OrderDetailServlet.java
│   │   │       │   ├── ReviewServlet.java
│   │   │       │   ├── ContactServlet.java
│   │   │       │   ├── AdminDashboardServlet.java
│   │   │       │   ├── AdminProductServlet.java
│   │   │       │   ├── AdminCategoryServlet.java
│   │   │       │   ├── AdminOrderServlet.java
│   │   │       │   ├── AdminInventoryServlet.java
│   │   │       │   └── AdminCustomerServlet.java
│   │   │       ├── filter/               # Security & Encoding Filters
│   │   │       │   ├── CharacterEncodingFilter.java
│   │   │       │   ├── AuthFilter.java
│   │   │       │   └── AdminFilter.java
│   │   │       ├── listener/             # Lifecycle Listener
│   │   │       │   └── AppContextListener.java
│   │   │       └── util/                 # Utility classes
│   │   │           ├── DBConnection.java
│   │   │           ├── PasswordUtil.java
│   │   │           ├── CsrfUtil.java
│   │   │           ├── ValidationUtil.java
│   │   │           └── AdminSetupCli.java
│   │   ├── resources/
│   │   │   └── db.properties             # Externalizable database connection credentials
│   │   └── webapp/
│   │       ├── index.jsp                 # Context root redirect
│   │       ├── assets/
│   │       │   ├── css/style.css         # Modern, responsive design system
│   │       │   └── js/main.js            # UI interactions and quantity steppers
│   │       └── WEB-INF/
│   │           ├── web.xml               # Deployment descriptor, session security, error pages
│   │           └── views/
│   │               ├── common/           # header.jsp, footer.jsp
│   │               ├── auth/             # login.jsp, register.jsp
│   │               ├── catalog/          # list.jsp, detail.jsp
│   │               ├── cart/             # cart.jsp
│   │               ├── order/            # checkout.jsp, history.jsp, detail.jsp
│   │               ├── customer/         # profile.jsp, addresses.jsp, wishlist.jsp
│   │               ├── admin/            # dashboard.jsp, products.jsp, categories.jsp, orders.jsp, inventory.jsp, customers.jsp
│   │               ├── public/           # contact.jsp
│   │               └── error/            # 404.jsp, 500.jsp
│   └── test/                             # JUnit 5 test suite
```

---

## 🗄️ Database Setup (MySQL 8.x)

### 1. Initialize the Database
Open MySQL Command-Line Client, MySQL Workbench, or PowerShell:

```sql
mysql -u root -p < database/schema.sql
mysql -u root -p < database/sample-data.sql
```

Alternatively, run the queries in `database/schema.sql` and `database/sample-data.sql` inside your MySQL GUI (e.g., MySQL Workbench, DBeaver, or HeidiSQL).

### 2. Configure Credentials

By default, the application connects using `src/main/resources/db.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/ecommerce_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
db.username=root
db.password=
```

#### Windows PowerShell Environment Variable Overrides (Recommended for Production):
You can override any database parameter without editing code:
```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/ecommerce_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8"
$env:DB_USERNAME="your_mysql_username"
$env:DB_PASSWORD="your_secure_password"
```

---

## 👤 Default Demo Accounts & Administrator Setup

The seed dataset creates two ready-to-test accounts (passwords hashed using BCrypt):

| Role | Email | Password | Privileges |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin@ecommerce.com` | `Password@123` | Full access to `/admin/*`, product CRUD, category CRUD, inventory replenishment, and customer management. |
| **Customer** | `jane@example.com` | `Password@123` | Shopping cart, checkout, profile updates, address management, orders, wishlist, verified buyer reviews. |

### Provisioning a New Administrator via CLI
To create a custom administrator account without hardcoding credentials:
```powershell
java -cp "target/classes;target/dependency/*" com.ecommerce.util.AdminSetupCli
```

---

## ⚙️ Building with Apache Maven

Run the clean build command:
```powershell
mvn clean package
```

This compiles all 67 Java source classes under Java 25, runs the 21 automated JUnit 5 tests, and outputs the WAR file:
```
target/ecommerce.war
```

To run tests individually:
```powershell
mvn test
```

---

## 🚀 Deploying to Apache Tomcat 11 on Windows

1. **Prerequisites**:
   - Ensure JDK 25 is installed and `JAVA_HOME` points to your JDK directory:
     ```powershell
     $env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.2"
     ```
   - Download Apache Tomcat 11 (e.g., from [tomcat.apache.org](https://tomcat.apache.org/download-110.cgi)).
   
2. **Deploy the WAR File**:
   - Copy `target/ecommerce.war` into Tomcat's `webapps/` directory:
     ```powershell
     Copy-Item "target\ecommerce.war" -Destination "C:\apache-tomcat-11.0.x\webapps\"
     ```

3. **Start Apache Tomcat**:
   - Navigate to the Tomcat `bin` folder and launch:
     ```powershell
     cd C:\apache-tomcat-11.0.x\bin
     .\startup.bat
     ```

4. **Access the Application**:
   - Open your web browser and navigate to:
     **`http://localhost:8080/ecommerce/`**

---

## 🌐 Application URL Directory

| Route | HTTP Methods | Description | Access |
| :--- | :--- | :--- | :--- |
| `/` or `/home` | GET | Storefront homepage with hero banners & featured picks | Public |
| `/products` | GET | Product catalog browsing with category, price filter, sorting, and pagination | Public |
| `/product?id=X` | GET | Product detail page with reviews and stock indicators | Public |
| `/search?q=...` | GET | Keyword search over titles and product descriptions | Public |
| `/contact` | GET, POST | Customer service contact and FAQ | Public |
| `/register` | GET, POST | Customer self-registration (BCrypt hashed) | Anonymous |
| `/login` | GET, POST | Authentication with session fixation rotation | Anonymous |
| `/logout` | GET, POST | Session invalidation | Authenticated |
| `/cart` | GET | Cart view with coupon validation | Customer / Admin |
| `/cart/add` | POST | Add item to cart with live stock check | Customer / Admin |
| `/cart/update` | POST | Adjust quantities or apply promotional coupons | Customer / Admin |
| `/cart/remove` | POST | Remove item from cart | Customer / Admin |
| `/wishlist` | GET | Saved wishlist items | Customer / Admin |
| `/addresses` | GET, POST | Address book management & default selector | Customer / Admin |
| `/checkout` | GET | Order review and address selection | Customer / Admin |
| `/orders/place` | POST | Multi-table JDBC transaction order placement | Customer / Admin |
| `/orders` | GET | Customer order history | Customer / Admin |
| `/orders/detail?id=X` | GET | Detailed order invoice & status (IDOR protected) | Owner / Admin |
| `/reviews` | POST | Verified purchaser product review submission | Customer / Admin |
| `/admin/dashboard` | GET | Executive store analytics & low stock warnings | ADMIN Only |
| `/admin/products` | GET, POST | Product catalog CRUD management | ADMIN Only |
| `/admin/categories`| GET, POST | Category taxonomy CRUD management | ADMIN Only |
| `/admin/orders` | GET, POST | Order status and fulfillment updating | ADMIN Only |
| `/admin/inventory` | GET, POST | Warehouse stock level adjustment | ADMIN Only |
| `/admin/customers` | GET | Customer audit directory | ADMIN Only |

---

## 🔒 Security Architectures Implemented

1. **PreparedStatement for All SQL Operations**: Complete elimination of SQL Injection risks across search, filters, inserts, and updates.
2. **Strict Whitelisting for Sorting**: SQL `ORDER BY` fields are checked against a hardcoded immutable Set (`ALLOWED_SORT_COLUMNS`) preventing SQL injection via sorting parameters.
3. **Cryptographic CSRF Protection**: Random 256-bit URL-safe tokens injected into each session and validated on state-altering POST requests.
4. **Session Fixation Defense**: `request.changeSessionId()` called upon successful login.
5. **Role-Based Access Control Filters**: `AuthFilter` and `AdminFilter` inspect server session attributes on every protected URL pattern.
6. **Insecure Direct Object Reference (IDOR) Protection**: Order details verify that the session user matches the order's `user_id` or possesses the `ADMIN` role.
7. **Atomic Database Transactions**: Checkout coordinates stock deduction (`stock_quantity >= ?`), order creation, line item insertion, and cart clearance using `Connection.setAutoCommit(false)`, `commit()`, and `rollback()`.
8. **Output Sanitization & Error Masking**: `ValidationUtil.sanitize()` strips dangerous HTML entities, and Tomcat error pages (404, 500) prevent technical stack traces from leaking to end users.
