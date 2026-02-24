# Event Lottery System - UI/UX Prototype

This is a comprehensive mobile app UI/UX prototype for the Event Lottery System, designed for eventual implementation in Android Studio.

## 🎯 Purpose

This prototype demonstrates the complete user interface and flow for a fair, accessible event registration system using a lottery mechanism. The design supports three user roles: **Entrants**, **Organizers**, and **Administrators**.

## 📱 Features Demonstrated

### For Entrants
- **Browse Events**: Search and filter available events
- **Join Waiting Lists**: Register interest in events during the registration period
- **Event Details**: View comprehensive event information including QR codes
- **My Events**: Track lottery status (waiting, selected, confirmed)
- **Notifications**: Receive updates about lottery draws and event status
- **Profile Management**: Manage personal information and view event history
- **Device-Based Authentication**: No username/password required

### For Organizers
- **Create Events**: Full event creation with QR code generation
- **Event Management**: View and manage all event details
- **Waiting List View**: See all entrants with geolocation data (if enabled)
- **Lottery Draw**: Conduct random selection of participants
- **Send Notifications**: Communicate with entrants at different stages
- **Export Data**: Download entrant lists in CSV format
- **Geolocation Tracking**: Optional location verification for entrants

### For Administrators
- **Dashboard**: Overview of system activity and statistics
- **Manage Events**: Review and remove policy-violating events
- **Manage Profiles**: Oversee entrants and organizers
- **Remove Organizers**: Handle policy violations
- **Review Images**: Approve or remove event posters
- **System Monitoring**: Track notifications and user activity

## 🎨 Key UI Components

### Navigation
- **Mobile-First Design**: Bottom navigation for entrants, side drawer for organizers/admins
- **Role-Based Navigation**: Different navigation structures for each user type
- **Responsive Layout**: Adapts to mobile and desktop screens

### Visual Elements
- **Event Cards**: Rich event display with posters, stats, and quick actions
- **Status Badges**: Clear visual indicators for event and registration status
- **Progress Indicators**: Fill rates, lottery progress, and loading states
- **Interactive Maps**: Geolocation visualization using Leaflet
- **QR Codes**: Generated for each event for easy sharing

### User Flows
1. **Entrant Journey**: Browse → Join Waiting List → Lottery Draw → Accept/Decline → Confirmed
2. **Organizer Journey**: Create Event → Monitor Waiting List → Draw Lottery → Manage Confirmations
3. **Admin Journey**: Monitor System → Review Content → Handle Violations

## 🛠 Technologies Used

- **React**: Component-based UI
- **React Router**: Multi-page navigation
- **Tailwind CSS**: Responsive styling
- **Radix UI**: Accessible component primitives
- **Lucide Icons**: Comprehensive icon set
- **QR Code React**: QR code generation
- **Leaflet**: Interactive maps for geolocation
- **Sonner**: Toast notifications

## 📋 User Stories Addressed

This prototype demonstrates UI/UX for all 50+ user stories including:

- ✅ US 01.01.01-04: Entrant event browsing and waiting list management
- ✅ US 01.02.01-04: Profile management
- ✅ US 01.04.01-03: Notification system
- ✅ US 01.05.01-05: Lottery acceptance/decline flow
- ✅ US 01.06.01-02: QR code scanning and event signup
- ✅ US 01.07.01: Device-based identification
- ✅ US 02.01.01-04: Event creation and QR generation
- ✅ US 02.02.01-03: Waiting list management and geolocation
- ✅ US 02.03.01: Waiting list limits
- ✅ US 02.04.01-02: Event poster upload
- ✅ US 02.05.01-03: Lottery drawing and replacement selection
- ✅ US 02.06.01-05: Entrant list management and CSV export
- ✅ US 02.07.01-03: Organizer notifications
- ✅ US 03.01.01-08.01: Admin management functions

## 🎭 Demo Features

### Mock Data
The prototype includes realistic mock data:
- 5 sample events across different categories
- Multiple entrants with various statuses
- Geolocation data for mapping demonstration
- Notification history
- Event statistics

### Interactive Elements
- Fully functional forms and inputs
- Simulated lottery draw with animation
- Toast notifications for user feedback
- Confirmation dialogs for destructive actions
- Expandable/collapsible sections

## 🚀 Next Steps for Android Implementation

### Backend Requirements (Firebase/Supabase)
1. User authentication (device-based)
2. Real-time database for events and waiting lists
3. Cloud Functions for lottery draws
4. Push notifications service
5. File storage for event posters
6. Geolocation services

### Android-Specific Considerations
1. **Camera Integration**: QR code scanning using device camera
2. **Location Services**: GPS integration for geolocation verification
3. **Push Notifications**: FCM integration
4. **Offline Support**: Local data caching
5. **Material Design**: Adapt UI to Material Design 3 guidelines
6. **Permissions**: Camera, location, notifications

### Database Schema
Key collections/tables needed:
- Users (entrants, organizers, admins)
- Events
- WaitingLists
- Notifications
- LotteryDraws (audit log)
- DeviceRegistrations

## 📐 Design Decisions

### Accessibility
- Clear status indicators (badges, colors)
- High contrast text
- Large touch targets for mobile
- Descriptive labels and alt text
- Confirmation dialogs for critical actions

### Fairness & Transparency
- Lottery criteria clearly explained
- Waiting list position visible
- Draw process documented
- Notification of all status changes
- Equal opportunity regardless of timing

### Mobile-First Approach
- Bottom navigation for thumb-friendly access
- Sticky headers for context
- Large tap targets (minimum 44x44px)
- Responsive images
- Progressive disclosure of information

## 🎯 Key Screens

1. **Home** - Role selection
2. **Browse Events** - Event discovery
3. **Event Details** - Complete event information
4. **My Events** - Personal event tracking
5. **Notifications** - Communication center
6. **Profile** - User settings
7. **Create Event** - Organizer event setup
8. **Manage Event** - Organizer dashboard
9. **Draw Lottery** - Random selection interface
10. **Admin Dashboard** - System overview

## 📱 Responsive Breakpoints

- Mobile: < 768px (primary target)
- Tablet: 768px - 1024px
- Desktop: > 1024px (for testing/admin use)

## 🎨 Color Scheme

- **Primary (Blue)**: Actions, selected states
- **Green**: Success, confirmed
- **Orange**: Warnings, pending
- **Red**: Errors, destructive actions
- **Purple**: Admin/organizer features
- **Gray**: Neutral content

This prototype serves as a complete visual and functional reference for Android development in Android Studio.
