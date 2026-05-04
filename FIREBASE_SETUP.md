# Firebase Setup Guide for Barangay Borrowing System

## Your Firebase Configuration

✅ **Database URL**: `https://juanezaian-58a6b03c-default-rtdb.asia-southeast1.firebasedatabase.app/`  
✅ **Project ID**: `juanezaian-58a6b03c`  
✅ **Storage Bucket**: `juanezaian-58a6b03c.firebasestorage.app`  

## Quick Setup Steps

### 1. Firebase Console Configuration

1. **Authentication Setup**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Select your project: `juanezaian-58a6b03c`
   - Navigate to **Authentication** → **Sign-in method**
   - Enable **Email/Password** provider
   - Set **Email enumeration protection** to "Off" (for testing)

2. **Database Rules**
   - Go to **Realtime Database** → **Rules**
   - Replace with these rules for development:

```json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null",
    "users": {
      "$uid": {
        ".read": "$uid === auth.uid || root.child('users').child(auth.uid).child('role').val() === 'admin'",
        ".write": "$uid === auth.uid || root.child('users').child(auth.uid).child('role').val() === 'admin'"
      }
    },
    "items": {
      ".read": "auth != null",
      ".write": "auth != null && root.child('users').child(auth.uid).child('role').val() === 'admin'"
    },
    "borrowings": {
      "$userId": {
        ".read": "$userId === auth.uid || root.child('users').child(auth.uid).child('role').val() === 'admin'",
        ".write": "$userId === auth.uid || root.child('users').child(auth.uid).child('role').val() === 'admin'"
      }
    }
  }
}
```

### 2. App Configuration

Your app is already configured with:
- ✅ `google-services.json` in place
- ✅ Firebase dependencies added
- ✅ Database URL configured in `firebase_config.xml`
- ✅ FirebaseHelper class for easy database operations

### 3. Test the Connection

1. **Build and run** the app
2. **Check connection status** - You should see "Connected to Firebase Database" toast
3. **Test registration** - Create a new user account
4. **Test login** - Sign in with the created account

### 4. Create Admin User

#### Method 1: Through Firebase Console
1. Go to **Authentication** → **Users**
2. Click **Add user**
3. Enter email: `admin@barangay.com`
4. Password: `admin123`
5. Go to **Realtime Database** → **Data**
6. Add this under `users` → `{user_id}`:

```json
{
  "uid": "your_user_id",
  "fullName": "System Administrator",
  "email": "admin@barangay.com",
  "role": "admin",
  "phoneNumber": "+639123456789",
  "address": "Barangay Office",
  "createdAt": 1640995200000,
  "lastLogin": 1640995200000,
  "isActive": true,
  "totalBorrowings": 0,
  "activeBorrowings": 0
}
```

#### Method 2: Through App Registration
1. Register a new user through the app
2. Go to Firebase Console → **Realtime Database** → **Data**
3. Find the user under `users` → `{user_id}`
4. Change the `role` field from `"user"` to `"admin"`

### 5. Database Structure

Your database will automatically create this structure:

```
juanezaian-58a6b03c-default-rtdb/
├── users/
│   ├── {user_id}/
│   │   ├── uid: "firebase_uid"
│   │   ├── fullName: "John Doe"
│   │   ├── email: "john@example.com"
│   │   ├── role: "user" | "admin"
│   │   ├── phoneNumber: "+639123456789"
│   │   ├── address: "123 Main St"
│   │   ├── createdAt: 1640995200000
│   │   ├── lastLogin: 1640995200000
│   │   ├── isActive: true
│   │   ├── totalBorrowings: 0
│   │   └── activeBorrowings: 0
├── items/
│   ├── {item_id}/
│   │   ├── name: "Projector"
│   │   ├── description: "Portable projector"
│   │   ├── category: "Electronics"
│   │   ├── status: "available"
│   │   └── createdAt: 1640995200000
└── borrowings/
    ├── {user_id}/
    │   ├── {borrowing_id}/
    │   │   ├── itemId: "item_id"
    │   │   ├── borrowedAt: 1640995200000
    │   │   ├── returnBy: 1641081600000
    │   │   └── status: "active"
```

### 6. Testing Checklist

#### Basic Functionality
- [ ] App launches successfully
- [ ] "Connected to Firebase Database" toast appears
- [ ] User registration works
- [ ] User login works
- [ ] Password reset email is sent
- [ ] Admin login works

#### Role-based Access
- [ ] Regular users see User Dashboard
- [ ] Admin users see Admin Dashboard
- [ ] Statistics display correctly
- [ ] Logout functionality works

#### Database Operations
- [ ] Users are saved to database
- [ ] User data is retrieved correctly
- [ ] Session management works
- [ ] Auto-login works for existing sessions

### 7. Troubleshooting

#### Common Issues and Solutions

**"Firebase connection failed"**
- Check internet connection
- Verify database URL in `firebase_config.xml`
- Ensure Firebase project is active

**"Authentication failed"**
- Check if Email/Password is enabled in Firebase Console
- Verify user exists in Authentication section
- Check database rules for read/write permissions

**"User not found in database"**
- User exists in Authentication but not in Realtime Database
- Check if user data was created during registration
- Manually create user record in database

**"Permission denied"**
- Update database rules (see step 2)
- Ensure user is authenticated
- Check user role for admin operations

#### Debug Tips

1. **Check Android Studio Logcat** for Firebase-related logs
2. **Use Firebase Console** → **Realtime Database** → **Data** to verify data
3. **Test with emulator** first before physical device
4. **Clear app data** and test fresh installation

### 8. Production Considerations

When moving to production:

1. **Security Rules**: Make database rules more restrictive
2. **Email Verification**: Enable email verification in Authentication
3. **Password Policy**: Set stronger password requirements
4. **Backup**: Set up database backups
5. **Monitoring**: Enable Firebase Analytics and Crashlytics

### 9. Next Steps

After basic setup is working:

1. **Test all features** thoroughly
2. **Add sample data** to database
3. **Implement item management** features
4. **Add borrowing functionality**
5. **Test with multiple users**

---

## Quick Commands

### Test Database Connection
```bash
# In Android Studio Logcat, look for:
# FirebaseHelper: Firebase initialized with URL: https://juanezaian-58a6b03c-default-rtdb.asia-southeast1.firebasedatabase.app/
# FirebaseHelper: Firebase connection status: true
```

### Verify Database URL
```bash
# Check app/src/main/res/values/firebase_config.xml
<string name="firebase_database_url">https://juanezaian-58a6b03c-default-rtdb.asia-southeast1.firebasedatabase.app/</string>
```

### Default Admin Credentials
- **Email**: `admin@barangay.com`
- **Password**: `admin123`

---

**Your Firebase project is ready!** 🎉

The app is now configured to use your specific Firebase database. Follow the testing checklist to ensure everything works correctly.
