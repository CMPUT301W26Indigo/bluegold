import { createBrowserRouter } from "react-router";
import { Layout } from "./components/Layout";
import { Home } from "./components/Home";
import { EntrantEvents } from "./components/entrant/EntrantEvents";
import { EventDetails } from "./components/entrant/EventDetails";
import { MyEvents } from "./components/entrant/MyEvents";
import { EntrantProfile } from "./components/entrant/EntrantProfile";
import { EntrantNotifications } from "./components/entrant/EntrantNotifications";
import { OrganizerEvents } from "./components/organizer/OrganizerEvents";
import { CreateEvent } from "./components/organizer/CreateEvent";
import { ManageEvent } from "./components/organizer/ManageEvent";
import { DrawLottery } from "./components/organizer/DrawLottery";
import { AdminDashboard } from "./components/admin/AdminDashboard";
import { AdminEvents } from "./components/admin/AdminEvents";
import { AdminProfiles } from "./components/admin/AdminProfiles";
import { AdminImages } from "./components/admin/AdminImages";

export const router = createBrowserRouter([
  {
    path: "/",
    Component: Layout,
    children: [
      { index: true, Component: Home },
      // Entrant routes
      { path: "entrant/events", Component: EntrantEvents },
      { path: "entrant/events/:id", Component: EventDetails },
      { path: "entrant/my-events", Component: MyEvents },
      { path: "entrant/profile", Component: EntrantProfile },
      { path: "entrant/notifications", Component: EntrantNotifications },
      // Organizer routes
      { path: "organizer/events", Component: OrganizerEvents },
      { path: "organizer/create", Component: CreateEvent },
      { path: "organizer/manage/:id", Component: ManageEvent },
      { path: "organizer/draw/:id", Component: DrawLottery },
      // Admin routes
      { path: "admin", Component: AdminDashboard },
      { path: "admin/events", Component: AdminEvents },
      { path: "admin/profiles", Component: AdminProfiles },
      { path: "admin/images", Component: AdminImages },
    ],
  },
]);
