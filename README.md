# Event Lottery System - Android Application

## 📱 Project Overview

This is the Android implementation of the Event Lottery System, built with **Kotlin** and following **Material Design 3** guidelines. The app allows users to participate in a fair lottery system for popular community events.

## 🎯 User Roles

### 1. **Entrant**
- Browse and search for events
- Join event waiting lists via QR code scanning
- Receive lottery results notifications
- Confirm or decline event invitations
- View geolocation-restricted events (1-500km radius)

### 2. **Organizer**
- Create and manage events
- Generate QR codes for event registration
- Set geolocation requirements (1-500km radius)
- Run lottery draws
- Export entrant lists to CSV
- Send notifications to participants
- View entrant locations on map

### 3. **Administrator**
- Monitor platform statistics
- Review flagged content (events, images, users)
- Moderate inappropriate content
- Manage users and events
- Prioritize flagged items for review

## 📂 Project Structure

```
android/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── AndroidManifest.xml
│   │       ├── java/com/eventlottery/
│   │       │   ├── MainActivity.kt
│   │       │   ├── ui/
│   │       │   │   ├── entrant/
│   │       │   │   │   ├── BrowseEventsActivity.kt
│   │       │   │   │   ├── EventDetailsActivity.kt
│   │       │   │   │   ├── MyEventsActivity.kt
│   │       │   │   │   ├── NotificationsActivity.kt
│   │       │   │   │   └── ProfileActivity.kt
│   │       │   │   ├── organizer/
│   │       │   │   │   ├── OrganizerDashboardActivity.kt
│   │       │   │   │   ├── CreateEventActivity.kt
│   │       │   │   │   ├── ManageEventActivity.kt
│   │       │   │   │   └── DrawLotteryActivity.kt
│   │       │   │   ├── admin/
│   │       │   │   │   ├── AdminDashboardActivity.kt
│   │       │   │   │   ├── ManageEventsActivity.kt
│   │       │   │   │   ├── ReviewImagesActivity.kt
│   │       │   │   │   └── ManageUsersActivity.kt
│   │       │   │   ├── adapters/
│   │       │   │   └── qr/
│   │       │   │       └── QRScannerActivity.kt
│   │       │   ├── data/
│   │       │   │   ├── models/
│   │       │   │   │   └── Event.kt
│   │       │   │   └── repositories/
│   │       │   ├── services/
│   │       │   │   ├── FCMService.kt
│   │       │   │   └── LocationService.kt
│   │       │   └── utils/
│   │       └── res/
│   │           ├── layout/
│   │           │   ├── activity_browse_events.xml
│   │           │   ├── activity_event_details.xml
│   │           │   ├── activity_create_event.xml
│   │           │   └── item_event_card.xml
│   │           ├── values/
│   │           │   ├── colors.xml
│   │           │   ├── strings.xml
│   │           │   ├── dimens.xml
│   │           │   └── themes.xml
│   │           ├── menu/
│   │           │   └── bottom_nav_entrant.xml
│   │           └── drawable/
│   └── build.gradle
├── build.gradle
└── settings.gradle
```

## 🚀 Getting Started

### Prerequisites

- **Android Studio Hedgehog (2023.1.1)** or newer
- **JDK 17** or newer
- **Android SDK 24** (minimum) to **34** (target)
- **Kotlin 1.9.22** or newer

### Import into Android Studio

1. **Open Android Studio**

2. **Import the Project**:
   - Click **File → Open**
   - Navigate to the `/android` folder
   - Click **OK** to open the project

3. **Sync Gradle**:
   - Android Studio will automatically start syncing Gradle
   - If not, click **File → Sync Project with Gradle Files**

4. **Wait for Build**:
   - Wait for Gradle sync and indexing to complete
   - This may take several minutes on first import

### Configure Firebase (Recommended)

1. **Create Firebase Project**:
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create a new project
   - Add an Android app with package name: `com.eventlottery`

2. **Download google-services.json**:
   - Download the `google-services.json` file
   - Place it in `/android/app/` directory

3. **Enable Firebase Services**:
   - Enable **Authentication** (Email/Password, Google Sign-In)
   - Enable **Cloud Firestore** (Database)
   - Enable **Cloud Storage** (Image uploads)
   - Enable **Cloud Messaging** (Notifications)

### Alternative: Configure Supabase

If using Supabase instead of Firebase:

1. **Create Supabase Project**:
   - Go to [Supabase Dashboard](https://supabase.com/)
   - Create a new project

2. **Get API Keys**:
   - Copy your **Project URL** and **anon key**

3. **Update Dependencies**:
   - In `app/build.gradle`, uncomment Supabase dependencies
   - Comment out Firebase dependencies

4. **Add API Keys**:
   - Create `local.properties` file in project root
   - Add: 
     ```
     supabase.url=YOUR_PROJECT_URL
     supabase.anon.key=YOUR_ANON_KEY
     ```

## 🔧 Configuration

### Update Application ID (Optional)

If you want to change the package name:

1. Edit `app/build.gradle`:
   ```gradle
   android {
       namespace 'com.yourcompany.eventlottery'
       defaultConfig {
           applicationId "com.yourcompany.eventlottery"
       }
   }
   ```

2. Refactor package name in Android Studio:
   - Right-click on `com.eventlottery` package
   - Select **Refactor → Rename**
   - Enter new package name

### Configure Location Services

For geolocation features (1-500km radius restriction):

1. **Get Google Maps API Key**:
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Enable **Maps SDK for Android**
   - Create API key

2. **Add to AndroidManifest.xml**:
   ```xml
   <application>
       <meta-data
           android:name="com.google.android.geo.API_KEY"
           android:value="YOUR_API_KEY_HERE" />
   </application>
   ```

## 🏗️ Build and Run

### Run on Emulator

1. **Create AVD (Android Virtual Device)**:
   - Tools → Device Manager
   - Create Device
   - Select Pixel 6 or similar
   - System Image: Android 13 (API 33) or higher

2. **Run App**:
   - Click **Run** (green triangle) or press `Shift + F10`
   - Select your emulator
   - App will install and launch

### Run on Physical Device

1. **Enable Developer Options**:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   - Go to Settings → Developer Options
   - Enable "USB Debugging"

2. **Connect Device**:
   - Connect via USB
   - Accept USB debugging prompt on device

3. **Run App**:
   - Click **Run** and select your device

## 📋 Required Permissions

The app requires the following permissions:

- **INTERNET** - For database connectivity
- **ACCESS_FINE_LOCATION** - For geolocation features
- **ACCESS_COARSE_LOCATION** - For geolocation features
- **CAMERA** - For QR code scanning
- **READ_MEDIA_IMAGES** - For event poster uploads
- **POST_NOTIFICATIONS** - For lottery results and updates

All permissions are handled with runtime permission requests.

## 🎨 UI/UX Reference

All screens are based on the comprehensive storyboard documentation. Each activity corresponds to a screen in the storyboard:

### Entrant Screens
- **E1**: BrowseEventsActivity
- **E2**: EventDetailsActivity
- **E3**: MyEventsActivity
- **E4**: NotificationsActivity
- **E5**: ProfileActivity

### Organizer Screens
- **O1**: OrganizerDashboardActivity
- **O2**: CreateEventActivity
- **O3**: ManageEventActivity
- **O4**: DrawLotteryActivity
- **O5**: (Confirmed Entrants - part of ManageEventActivity)

### Admin Screens
- **A1**: AdminDashboardActivity
- **A2**: ManageEventsActivity
- **A3**: ReviewImagesActivity
- **A4**: ManageUsersActivity

## 🔑 Key Features Implementation

### QR Code Generation & Scanning
- Using **ZXing library** (`com.google.zxing:core`)
- QR codes generated automatically when organizer creates event
- Entrants scan QR codes to register for events

### Geolocation Restriction (1-500km)
- Organizers can set radius requirement when creating event
- Uses **Google Play Services Location API**
- Haversine formula calculates distance between coordinates
- Events only visible to users within specified radius

### Lottery System
- Cryptographically secure random selection algorithm
- Fair probability for all entrants
- Automatic notification on draw
- Replacement draws when users decline

### CSV Export
- Organizers can export waitlist/confirmed entrants
- Uses **OpenCSV library**
- Includes all entrant details and contact information

### Admin Flagging System
- Users can flag inappropriate events/images
- Flagged content appears at top of admin lists
- Shows flag count and reasons
- Admin can dismiss or remove content

### Notifications
- **Firebase Cloud Messaging** for push notifications
- Lottery results (selected/not selected)
- Event updates from organizers
- Response deadline reminders

## 📱 Minimum Requirements

- **Android Version**: 7.0 (Nougat) - API Level 24
- **Target Version**: Android 14 - API Level 34
- **Screen Size**: 5.0" minimum (optimized for phones)
- **Internet**: Required for all features
- **Location**: Required for geolocation-restricted events
- **Camera**: Required for QR code scanning

## 🧪 Testing

### Run Unit Tests
```bash
./gradlew test
```

### Run Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Test Accounts (for development)

Create test accounts for each role:
- **Entrant**: entrant@test.com
- **Organizer**: organizer@test.com  
- **Admin**: admin@test.com

## 📝 Next Steps

1. **Implement Remaining Activities**: Complete all Activity/Fragment classes
2. **Setup Database**: Configure Firebase/Supabase with proper schema
3. **Implement Adapters**: Create RecyclerView adapters for lists
4. **Add ViewModels**: Implement MVVM architecture with ViewModels
5. **Repository Pattern**: Create repositories for data access
6. **Navigation**: Implement Navigation Component for better flow
7. **Testing**: Write unit tests and UI tests
8. **Accessibility**: Add content descriptions and screen reader support
9. **Localization**: Add support for multiple languages
10. **Performance**: Optimize image loading and database queries

## 🐛 Troubleshooting

### Gradle Sync Issues
- **Solution**: File → Invalidate Caches → Invalidate and Restart

### Missing Dependencies
- **Solution**: Check internet connection, sync Gradle again

### Build Errors
- **Solution**: Clean project (Build → Clean Project), then rebuild

### Emulator Issues
- **Solution**: Wipe emulator data, or create new AVD

## 📚 Documentation

- [Android Developer Guide](https://developer.android.com/guide)
- [Material Design 3](https://m3.material.io/)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Firebase Documentation](https://firebase.google.com/docs)

## 📄 License

This project is part of the Event Lottery System application suite.

## 🤝 Support

For questions or issues:
1. Check the storyboard documentation
2. Review this README
3. Consult Android Studio's built-in help
4. Check Firebase/Supabase documentation for backend issues

---

**Ready to build!** 🎉 Import the project and start developing your Event Lottery System Android app!
