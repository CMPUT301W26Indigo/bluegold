# Event Lottery System - Android Application

## Project Overview

This is the Android implementation of the Event Lottery System, built with **Java** in **Android Studio**. The app allows users to participate in a lottery system to sign up for community events, giving everyone a fair chance to participate regardless of constraints affecting sign-up speed, such work, disability, or internet connection.

## User Roles

### 1. **Entrant**
- Browse and search for events
- Join event waiting lists via QR code scanning or by clicking on an event for more details.
- Receive lottery results notifications
- Confirm or decline event invitations
- Filter by availability, location, and preferences.
- Review past events and lottery results.

### 2. **Organizer**
- Create and manage events
- Generate QR codes for event registration
- Set geolocation requirements (1-500km radius)
- Run lottery draws
- Export entrant lists to CSV
- Send notifications to participants
- View entrant locations on map

### 3. **Administrator**
- Review flagged content (events, images, users)
- Moderate inappropriate content
- Review notification logs
- Manage users and events

## How to Run

### Prerequisites

- **Android Studio Hedgehog (2023.1.1)** or newer
- **JDK 17** or newer
- **Android SDK 24** (minimum) to **36** (target)
- **Java** version 17

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
   - If your build fails due to the google.services.json file not being found, make sure you have included the file at the app level. Ask for access from one of the repo owners if necessary. 

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

1. **Enable Developer Options (Android Only)**:
   - IMPORTANT: These steps only work on an Android Phone.
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   - Go to Settings → Developer Options
   - Enable "USB Debugging"
   - Alternatively, enable “WiFi Debugging”

2. **Connect Device**:
   - Connect via USB
   - Accept USB debugging prompt on device
   - If you chose to connect over WiFi, follow the prompts on the screen.

3. **Run App**:
   - Click **Run** and select your device

4. **Permissions*
- The app requires the following permissions:
  - **INTERNET** - For database connectivity
  - **ACCESS_FINE_LOCATION** - For geolocation features
  - **ACCESS_COARSE_LOCATION** - For geolocation features
  - **CAMERA** - For QR code scanning
  - **READ_MEDIA_IMAGES** - For event poster uploads
  - **POST_NOTIFICATIONS** - For lottery results and updates
- All permissions are handled with runtime permission requests.

## UI/UX Reference
- Please refer to the Storyboard linked in the repository.
-https://github.com/CMPUT301W26Indigo/bluegold/blob/main/Ui_Mockup_%26_Storyboard_using_Figma/CMPUT301StoryboardForPart3.png 

## Class/Code Documentation
- Please refer to the UML Diagrams linked in the repository for an overview of the classes used and their properties
- Please refer to the JavaDocs linked in the repository for detailed documentation on each class.

## Acknowledgements and LLM Use Disclosure
The following sources were used to help develop our application:
- Android Studio Docs
- Firebase Docs
- https://www.youtube.com/watch?v=n8HdrLYL9DA (Tutorial on QR Code Generators)
- https://www.youtube.com/watch?v=jtT60yFPelI (Tutorial on QR Code Scanners)

We acknowledge the use of LLM’s to help generate portions of the code. Below were our use cases
- Porting the UI created in Figma into Android Studio. Gemini was used to create equivalent Classes, Activities, Fragments, and XML files. The code inside these files was the minimum required for proper UI flow.
- Debugging portions of the application.
- Generating unit tests.

