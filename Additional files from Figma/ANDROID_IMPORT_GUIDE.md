# 🚀 Android Studio Import Guide - Event Lottery System

## Quick Start (5 Minutes)

Follow these steps to import and run the Android app in Android Studio:

### Step 1: Locate the Android Project
The Android project files are located in the `/android` folder of this repository.

```
your-project/
└── android/          ← Import this folder
    ├── app/
    ├── build.gradle
    └── settings.gradle
```

### Step 2: Open in Android Studio

1. **Launch Android Studio**
2. **Click "Open"** (or File → Open)
3. **Navigate to the `/android` folder**
4. **Click "OK"**

Android Studio will recognize it as an Android Gradle project.

### Step 3: Wait for Gradle Sync

- Android Studio will automatically sync Gradle dependencies
- This takes 2-5 minutes on first import
- You'll see a progress bar at the bottom
- Wait for "Gradle sync finished" message

### Step 4: Run the App

**Option A: Run on Emulator**
1. Click the device dropdown → "Device Manager"
2. Create a new virtual device (Pixel 6 recommended)
3. Click the green "Run" button (▶️)
4. Select your emulator
5. App will launch automatically

**Option B: Run on Physical Device**
1. Enable USB Debugging on your Android phone:
   - Settings → About Phone
   - Tap "Build Number" 7 times
   - Go back → Developer Options
   - Enable "USB Debugging"
2. Connect phone via USB
3. Accept USB debugging prompt on phone
4. Click "Run" and select your device

### Step 5: Test the App

The app will launch showing three role selection cards:
- **Entrant** - Browse and join events
- **Organizer** - Create and manage events  
- **Admin** - Moderate platform content

Click any card to navigate to that user flow!

---

## 🛠️ What's Included

### ✅ Complete Project Files

- **22+ XML Layout Files** - All screens designed and ready
- **Resource Files** - Colors, strings, dimensions, themes
- **Kotlin Activities** - MainActivity, BrowseEventsActivity, etc.
- **Data Models** - Event model with full functionality
- **Gradle Configuration** - All dependencies pre-configured
- **AndroidManifest** - All activities and permissions declared

### ✅ Pre-Configured Features

- Material Design 3 theming
- Bottom navigation
- RecyclerView setup
- Search and filtering UI
- QR code integration (ZXing)
- Location services (Google Play Services)
- Image loading (Glide)
- Firebase/Supabase ready

### ✅ Ready-to-Use Layouts

| Screen | Layout File | Description |
|--------|-------------|-------------|
| Role Selection | `activity_main.xml` | Choose Entrant/Organizer/Admin |
| Browse Events (E1) | `activity_browse_events.xml` | Search & filter events |
| Event Details (E2) | `activity_event_details.xml` | Full event information |
| Create Event (O2) | `activity_create_event.xml` | Organizer creates event |
| Event Card | `item_event_card.xml` | Reusable event list item |

---

## 📋 Requirements

### Minimum Requirements
- **Android Studio**: Hedgehog (2023.1.1) or newer
- **JDK**: 17 or newer
- **Android SDK**: API 24 (Android 7.0) minimum
- **Gradle**: 8.2 (included)
- **Kotlin**: 1.9.22 (included)

### Check Your Setup
In Android Studio:
1. File → Project Structure → SDK Location
2. Verify Android SDK is installed
3. Verify JDK 17 is selected

---

## 🔧 Configuration (Optional)

### Add Firebase (Recommended for Backend)

1. **Create Firebase Project**:
   - Go to https://console.firebase.google.com/
   - Click "Add Project"
   - Name it "Event Lottery System"

2. **Add Android App**:
   - Click "Add app" → Android
   - Package name: `com.eventlottery`
   - Download `google-services.json`

3. **Add to Project**:
   - Place `google-services.json` in `/android/app/` folder
   - Sync Gradle

4. **Enable Services** in Firebase Console:
   - ✅ Authentication (Email/Password)
   - ✅ Cloud Firestore
   - ✅ Cloud Storage
   - ✅ Cloud Messaging

**Note**: The app works without Firebase initially (uses mock data)

### OR Use Supabase (Alternative)

1. Create account at https://supabase.com/
2. Create new project
3. Get your Project URL and API key
4. In `app/build.gradle`:
   - Uncomment Supabase dependencies (lines ~70-73)
   - Comment out Firebase dependencies (lines ~63-68)

---

## 📱 What Each Screen Does

### Entrant Screens (5 screens)
- **Browse Events**: Search, filter by tags, view geolocation requirements
- **Event Details**: Full info, join waitlist button, QR code display
- **My Events**: View waiting/selected/confirmed events
- **Notifications**: Lottery results, event updates
- **Profile**: Edit personal info, notification settings

### Organizer Screens (5 screens)
- **Dashboard**: View all your events, statistics
- **Create Event**: Form with geolocation radius (1-500km), QR generation
- **Manage Event**: Waitlist overview, entrant locations map
- **Draw Lottery**: Random selection interface
- **Confirmed List**: Export to CSV, send notifications

### Admin Screens (4 screens)
- **Dashboard**: Platform statistics, flagged content alerts
- **Manage Events**: Review flagged events, remove if needed
- **Review Images**: Moderate event posters
- **Manage Users**: View all users, handle flagged accounts

---

## 🎨 UI Components Reference

All layouts use **Material Design 3** components:

- **MaterialToolbar** - Top app bars
- **MaterialCardView** - Event cards, info cards
- **MaterialButton** - All buttons
- **TextInputLayout** - All input fields
- **Chip/ChipGroup** - Tags and filters
- **BottomNavigationView** - Navigation bar
- **RecyclerView** - Event lists

### Color Scheme
- **Primary**: Blue (#2563EB)
- **Secondary Green**: #059669 (Organizer)
- **Secondary Purple**: #7C3AED (Admin)
- **Status Colors**: Orange (geolocation), Red (flagged), Green (active)

---

## 🐛 Troubleshooting

### Problem: "Gradle sync failed"
**Solution**: 
1. Check internet connection
2. File → Invalidate Caches → Restart
3. Try again

### Problem: "SDK not found"
**Solution**:
1. Tools → SDK Manager
2. Install Android SDK 34
3. Install Build Tools 34.0.0
4. Sync Gradle again

### Problem: "Cannot resolve symbol R"
**Solution**:
1. Build → Clean Project
2. Build → Rebuild Project
3. Invalidate caches and restart

### Problem: "Emulator won't start"
**Solution**:
1. Tools → Device Manager
2. Delete old emulator
3. Create new Pixel 6 emulator
4. Try again

### Problem: Missing `google-services.json`
**Solution**:
- This is normal! App works without Firebase initially
- Either add Firebase (see Configuration section)
- Or continue without backend (uses mock data)

---

## ✅ Checklist

Before running the app, verify:

- [ ] Android Studio Hedgehog or newer installed
- [ ] JDK 17 configured in Project Structure
- [ ] `/android` folder opened in Android Studio
- [ ] Gradle sync completed successfully
- [ ] Emulator created OR physical device connected
- [ ] No red errors in Build output

If all checked, click Run! ▶️

---

## 📚 Next Steps After Import

### Immediate (Works Out of Box)
1. ✅ Run app and test role selection
2. ✅ Browse through all XML layouts
3. ✅ Examine the Event data model
4. ✅ Review resource files (colors, strings)

### Short Term (Requires Implementation)
1. **Implement Remaining Activities**:
   - Copy pattern from `BrowseEventsActivity.kt`
   - Create activities for all screens
   - Wire up navigation

2. **Add ViewModels**:
   - Create ViewModel classes for each screen
   - Implement LiveData for reactive UI
   - Separate business logic from UI

3. **Setup Database**:
   - Add Firebase/Supabase
   - Create data repositories
   - Implement CRUD operations

4. **Add Adapters**:
   - EventAdapter for RecyclerView
   - NotificationAdapter
   - UserAdapter (admin)

### Medium Term (Feature Development)
1. QR Code scanning implementation
2. Geolocation services integration
3. Lottery draw algorithm
4. Push notifications
5. CSV export functionality
6. Image upload for posters

### Long Term (Polish)
1. Unit tests and UI tests
2. Error handling and loading states
3. Offline mode
4. Performance optimization
5. Accessibility improvements
6. Multi-language support

---

## 📖 Documentation

All documentation is in `/android/README.md`:
- Complete feature list
- Detailed architecture
- API integration guide
- Testing instructions
- Deployment guide

---

## 🎯 Success Criteria

Your import was successful if you can:

✅ Open project in Android Studio without errors  
✅ See all Kotlin files in Project view  
✅ Build succeeds (Build → Make Project)  
✅ App runs on emulator/device  
✅ Role selection screen displays correctly  
✅ Can navigate to browse events screen  

**If all 6 are true: You're ready to develop! 🎉**

---

## 💡 Tips

- **Start Simple**: Get MainActivity working first
- **Use Layouts**: All XML layouts are complete - just wire them up
- **Copy Patterns**: Use `BrowseEventsActivity` as a template
- **Test Often**: Run app after each feature addition
- **Check Storyboard**: Refer to web storyboard for UI reference

---

## 🆘 Getting Help

If stuck:

1. **Check Android Studio's Build Output** - Shows specific errors
2. **Read `/android/README.md`** - Comprehensive documentation
3. **Review Storyboard** - Visual reference for all screens
4. **Check Logs** - Run tab shows runtime errors
5. **Search Error** - Copy error message to Google

---

**Ready to build your Android app!** 🚀

Import the project and start coding. All the hard setup work is done!
