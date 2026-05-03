# ShopEase 🛍️

A modern, premium e-commerce web application built with Spring Boot, Thymeleaf, and Bootstrap. ShopEase provides a seamless shopping experience with a beautifully designed, responsive interface that supports both light and dark themes.

## ✨ Features

- **Comprehensive Shopping Experience**: Browse products, search, filter, and view detailed product pages.
- **Cart & Wishlist**: Manage shopping cart and save favorite items to your wishlist.
- **Secure Checkout**: Seamless checkout process with multiple payment methods including online payments (Razorpay) and Cash on Delivery.
- **Dynamic Theme**: Full light and dark mode support for a premium user experience.
- **Order Tracking**: Complete order history and real-time tracking dashboard for users.
- **Elite Membership**: Exclusive discounts and perks for premium members.
- **Admin Dashboard**: Powerful, responsive dashboard to manage products, orders, promotional offers, and users.
- **Secure Authentication**: User registration, login, profile management, and password recovery.
- **Address Management**: Save and manage multiple shipping addresses for quicker checkouts.

## 📸 Screenshots / Application Images

> *Note: Add your application screenshots in the `images` folder and update the links below.*

| Home Page | Product Details |
| :---: | :---: |
| ![Home Page](images/home-placeholder.jpg) | ![Product Details](images/product-placeholder.jpg) |

| Shopping Cart | Checkout Flow |
| :---: | :---: |
| ![Shopping Cart](images/cart-placeholder.jpg) | ![Checkout](images/checkout-placeholder.jpg) |

| Admin Dashboard | Dark Mode UI |
| :---: | :---: |
| ![Admin Dashboard](images/admin-placeholder.jpg) | ![Dark Mode](images/darkmode-placeholder.jpg) |

## 🛠️ Technology Stack

- **Backend**: Java 17, Spring Boot, Spring Security, Spring Data JPA
- **Frontend**: HTML5, CSS3, Vanilla JavaScript, Bootstrap 5, Thymeleaf
- **Database**: H2 Database (Development) / MySQL (Production ready)
- **Payment Gateway**: Razorpay Integration
- **Build Tool**: Maven

## 🚀 Getting Started

### Prerequisites
- Java Development Kit (JDK) 17 or higher
- Maven 3.6+

### Installation & Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/Deepansh1822/ShopEase.git
   cd ShopEase
   ```

2. **Configure Application Properties**
   Navigate to `src/main/resources/application.properties` and update the following settings if necessary (e.g., database credentials, email SMTP settings, and Razorpay API keys):
   ```properties
   # Example Razorpay Config
   razorpay.key.id=your_key_id
   razorpay.key.secret=your_key_secret
   ```

3. **Build the Application**
   ```bash
   mvn clean install
   ```

4. **Run the Application**
   ```bash
   mvn spring-boot:run
   ```
   The application will start and be accessible at `http://localhost:8080`.

## 🤝 Contributing

Contributions, issues, and feature requests are welcome! Feel free to check the [issues page](https://github.com/Deepansh1822/ShopEase/issues).

## 📄 License

This project is licensed under the MIT License.
