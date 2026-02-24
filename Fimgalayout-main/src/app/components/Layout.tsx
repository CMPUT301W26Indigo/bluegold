import { Outlet, useLocation } from 'react-router';
import { Link } from 'react-router';
import { 
  Home, 
  Calendar, 
  User, 
  Bell, 
  LayoutDashboard, 
  QrCode,
  Shield,
  Menu,
  X
} from 'lucide-react';
import { Button } from './ui/button';
import { useState } from 'react';

export function Layout() {
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  
  const isEntrantRoute = location.pathname.startsWith('/entrant');
  const isOrganizerRoute = location.pathname.startsWith('/organizer');
  const isAdminRoute = location.pathname.startsWith('/admin');

  const entrantLinks = [
    { to: '/entrant/events', icon: Calendar, label: 'Events' },
    { to: '/entrant/my-events', icon: LayoutDashboard, label: 'My Events' },
    { to: '/entrant/notifications', icon: Bell, label: 'Notifications' },
    { to: '/entrant/profile', icon: User, label: 'Profile' },
  ];

  const organizerLinks = [
    { to: '/organizer/events', icon: LayoutDashboard, label: 'My Events' },
    { to: '/organizer/create', icon: QrCode, label: 'Create Event' },
  ];

  const adminLinks = [
    { to: '/admin', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/admin/events', icon: Calendar, label: 'Events' },
    { to: '/admin/profiles', icon: User, label: 'Profiles' },
    { to: '/admin/images', icon: Shield, label: 'Images' },
  ];

  let currentLinks = [];
  if (isEntrantRoute) currentLinks = entrantLinks;
  if (isOrganizerRoute) currentLinks = organizerLinks;
  if (isAdminRoute) currentLinks = adminLinks;

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      {/* Mobile Header */}
      <header className="bg-white border-b border-gray-200 px-4 py-3 flex items-center justify-between md:hidden">
        <Link to="/" className="flex items-center gap-2">
          <Calendar className="w-6 h-6 text-blue-600" />
          <span className="font-semibold text-lg">Event Lottery</span>
        </Link>
        {currentLinks.length > 0 && (
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          >
            {mobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
          </Button>
        )}
      </header>

      {/* Mobile Menu */}
      {mobileMenuOpen && currentLinks.length > 0 && (
        <div className="md:hidden bg-white border-b border-gray-200">
          <nav className="flex flex-col">
            {currentLinks.map((link) => {
              const Icon = link.icon;
              const isActive = location.pathname === link.to;
              return (
                <Link
                  key={link.to}
                  to={link.to}
                  onClick={() => setMobileMenuOpen(false)}
                  className={`flex items-center gap-3 px-4 py-3 border-b border-gray-100 ${
                    isActive
                      ? 'bg-blue-50 text-blue-600'
                      : 'text-gray-700 hover:bg-gray-50'
                  }`}
                >
                  <Icon className="w-5 h-5" />
                  <span>{link.label}</span>
                </Link>
              );
            })}
          </nav>
        </div>
      )}

      <div className="flex-1 flex flex-col md:flex-row">
        {/* Desktop Sidebar */}
        {currentLinks.length > 0 && (
          <aside className="hidden md:block w-64 bg-white border-r border-gray-200">
            <div className="p-4 border-b border-gray-200">
              <Link to="/" className="flex items-center gap-2">
                <Calendar className="w-6 h-6 text-blue-600" />
                <span className="font-semibold text-lg">Event Lottery</span>
              </Link>
            </div>
            <nav className="p-4 flex flex-col gap-1">
              {currentLinks.map((link) => {
                const Icon = link.icon;
                const isActive = location.pathname === link.to;
                return (
                  <Link
                    key={link.to}
                    to={link.to}
                    className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                      isActive
                        ? 'bg-blue-50 text-blue-600'
                        : 'text-gray-700 hover:bg-gray-100'
                    }`}
                  >
                    <Icon className="w-5 h-5" />
                    <span>{link.label}</span>
                  </Link>
                );
              })}
            </nav>
          </aside>
        )}

        {/* Main Content */}
        <main className="flex-1 overflow-auto">
          <Outlet />
        </main>
      </div>

      {/* Bottom Navigation for Entrant (Mobile) */}
      {isEntrantRoute && (
        <nav className="md:hidden bg-white border-t border-gray-200 flex justify-around py-2">
          {entrantLinks.map((link) => {
            const Icon = link.icon;
            const isActive = location.pathname === link.to;
            return (
              <Link
                key={link.to}
                to={link.to}
                className={`flex flex-col items-center gap-1 px-4 py-2 ${
                  isActive ? 'text-blue-600' : 'text-gray-600'
                }`}
              >
                <Icon className="w-5 h-5" />
                <span className="text-xs">{link.label}</span>
              </Link>
            );
          })}
        </nav>
      )}
    </div>
  );
}
