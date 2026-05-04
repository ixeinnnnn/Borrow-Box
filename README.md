# Barangay Resources Borrowing System

A complete Android application for managing community resource borrowing in barangays (local communities in the Philippines). This system provides role-based access for administrators and users, with Firebase backend integration.

## Features

### 🔐 Authentication System
- **Email/Password Login** with Firebase Authentication
- **User Registration** with validation
- **Forgot Password** functionality
- **Admin Login** with separate credentials
- **Session Management** with SharedPreferences
- **Role-based Access Control** (Admin/User)

### 👥 User Management
- **User Dashboard** for regular users
- **Admin Dashboard** for system administrators
- **User Profile** management
- **Borrowing History** tracking

### 🏦 Resource Management
- **Browse Items** available for borrowing
- **My Borrowings** section
- **Item Categories** and search
- **Availability Status** tracking

### 📊 Admin Features
- **User Management** (view, edit, delete users)
- **Item Management** (add, edit, delete items)
- **Reports** and analytics
- **System Statistics**

## Technical Stack

### Frontend
- **Java** programming language
- **XML** layouts with Material Design
- **AndroidX** libraries
- **CardView** and RecyclerView components
- **Custom fonts** (Poppins)

### Backend
- **Firebase Authentication** for user management
- **Firebase Realtime Database** for data storage
- **Firebase Analytics** for usage tracking

### Architecture
- **MVC Pattern** (Model-View-Controller)
- **Clean Architecture** principles
- **Separate Activities** for each screen
- **Helper Classes** for utilities

## Project Structure

```
app/
├── src/main/
│   ├── java/com/example/barrowing_system/
│   │   ├── LoginActivity.java           # Main login screen
│   │   ├── AdminLoginActivity.java     # Admin login
│   │   ├── RegisterActivity.java       # User registration
│   │   ├── ForgotPasswordActivity.java # Password reset
│   │   ├── AdminDashboardActivity.java # Admin dashboard
│   │   ├── UserDashboardActivity.java  # User dashboard
│   │   ├── MainActivity.java           # Entry point & routing
│   │   ├── User.java                   # User model class
│   │   └── SessionManager.java         # Session handling
│   ├── res/
│   │   ├── layout/                     # XML layouts
│   │   ├── drawable/                   # Icons and graphics
│   │   ├── values/                     # Strings, colors, styles
│   │   └── font/                       # Custom fonts
│   └── AndroidManifest.xml             # App configuration
└── build.gradle.kts                    # Build configuration
```

## Installation Guide

### Prerequisites
- **Android Studio** (latest version)
- **Java 11** or higher
- **Android SDK** (API level 36)
- **Firebase Project** (see Firebase setup below)

### Step 1: Clone/Import Project
1. Open Android Studio
2. Click "Open an existing Android Studio project"
3. Navigate to the project directory
4. Wait for Gradle sync to complete

### Step 2: Firebase Setup
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or use existing one
3. Add an Android app with package name: `com.example.barrowing_system`
4. Download `google-services.json`
5. Place it in `app/src/main/` directory
6. Enable Firebase Authentication:
   - Go to Authentication → Sign-in method
   - Enable "Email/Password" provider
7. Enable Firebase Realtime Database:
   - Go to Realtime Database → Create Database
   - Choose "Start in test mode" for development
   - Copy database URL

### Step 3: Update Configuration
1. Open `app/build.gradle.kts`
2. Verify Firebase dependencies are present
3. Sync Gradle if needed

### Step 4: Run the Application
1. Connect an Android device or start emulator
2. Click "Run" button in Android Studio
3. The app will install and launch

## Default Credentials

### Admin Account
- **Email**: `admin@barangay.com`
- **Password**: `admin123`

### Test User Account
- **Email**: Register through the app
- **Password**: Minimum 6 characters

## Usage Guide

### For Users
1. **Register**: Create account with email and password
2. **Login**: Use credentials to sign in
3. **Browse Items**: View available resources
4. **Borrow Items**: Request items for borrowing
5. **Track Borrowings**: View current and past borrowings

### For Administrators
1. **Admin Login**: Use admin credentials
2. **Dashboard**: View system statistics
3. **Manage Users**: Add, edit, or remove users
4. **Manage Items**: Add, edit, or remove borrowing items
5. **View Reports**: Generate and view system reports

## Database Structure

### Users Collection
```json
{
  "users": {
    "userId": {
      "uid": "firebase_uid",
      "fullName": "John Doe",
      "email": "john@example.com",
      "role": "user", // or "admin"
      "phoneNumber": "+1234567890",
      "address": "123 Main St",
      "createdAt": 1640995200000,
      "lastLogin": 1640995200000,
      "isActive": true,
      "totalBorrowings": 5,
      "activeBorrowings": 2
    }
  }
}
```

### Items Collection
```json
{
  "items": {
    "itemId": {
      "name": "Projector",
      "description": "Portable projector for presentations",
      "category": "Electronics",
      "status": "available", // or "borrowed", "maintenance"
      "borrowedBy": "userId",
      "borrowedAt": 1640995200000,
      "returnBy": 1641081600000,
      "createdAt": 1640995200000,
      "createdBy": "userId"
    }
  }
}
```

### Borrowings Collection
```json
{
  "borrowings": {
    "userId": {
      "borrowingId": {
        "itemId": "itemId",
        "borrowedAt": 1640995200000,
        "returnBy": 1641081600000,
        "returnedAt": null,
        "status": "active" // or "returned", "overdue"
      }
    }
  }
}
```

## Security Features

### Authentication
- **Firebase Auth** for secure user authentication
- **Password Hashing** handled by Firebase
- **Session Tokens** for secure API access
- **Email Verification** (optional)

### Data Protection
- **Input Validation** on all user inputs
- **SQL Injection Protection** (Firebase handles this)
- **XSS Prevention** (TextView sanitization)
- **Role-based Access** control

### Session Management
- **SharedPreferences** for local session storage
- **Session Timeout** (configurable)
- **Auto-logout** on session expiry
- **Secure Session** validation

## Development Guidelines

### Code Style
- Follow **Java Naming Conventions**
- Use **Meaningful Variable Names**
- Add **Javadoc Comments** for all public methods
- Keep **Methods Short** (max 20-30 lines)
- Use **Constants** for magic numbers/strings

### Git Workflow
- Create **Feature Branches** for new features
- Use **Descriptive Commit Messages**
- **Code Review** before merging
- **Semantic Versioning** for releases

### Testing
- **Unit Tests** for business logic
- **Integration Tests** for Firebase operations
- **UI Tests** for critical user flows
- **Manual Testing** on multiple devices

## Troubleshooting

### Common Issues

#### Build Errors
- **Gradle Sync Failed**: Check internet connection and Firebase configuration
- **Missing Dependencies**: Verify Firebase SDK versions in build.gradle
- **Resource Not Found**: Check drawable and string resources

#### Authentication Issues
- **Login Failed**: Check Firebase Auth configuration
- **Registration Error**: Verify email/password validation rules
- **Session Lost**: Check SharedPreferences permissions

#### Database Issues
- **Data Not Loading**: Check Firebase Database rules
- **Write Permission Denied**: Verify database security rules
- **Network Error**: Check internet connection

### Debug Tips
1. **Enable Debug Logging**: Use `Log.d()` for debugging
2. **Check Firebase Console**: Monitor authentication and database events
3. **Use Android Studio Debugger**: Set breakpoints in critical methods
4. **Test with Emulator**: Easier debugging than physical device

## Future Enhancements

### Planned Features
- **Google Sign-In** integration
- **Push Notifications** for borrowing reminders
- **Offline Mode** with local SQLite database
- **Barcode/QR Code** scanning for items
- **Multi-language Support** (Filipino, English)
- **Dark Mode** theme support
- **Export Reports** to PDF/Excel
- **Mobile App** for iOS (React Native/Flutter)

### Improvements
- **Performance Optimization** for large datasets
- **UI/UX Enhancements** with Material Design 3
- **Advanced Search** and filtering options
- **Real-time Updates** with WebSocket integration
- **Backup and Restore** functionality

## Contributing

1. **Fork** the repository
2. **Create** a feature branch
3. **Make** your changes
4. **Test** thoroughly
5. **Submit** a pull request
6. **Wait** for code review

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions:
- **Email**: support@barangay-system.com
- **GitHub Issues**: Create an issue in the repository
- **Documentation**: Check the inline code comments

## Acknowledgments

- **Firebase** for backend services
- **Material Design** for UI components
- **Android Community** for libraries and resources
- **Barangay Officials** for requirements and feedback

---

**Version**: 1.0.0  
**Last Updated**: April 2026  
**Developer**: Barangay System Development Team
#   B o r r o w - B o x  
 #   B o r r o w - B o x  
 