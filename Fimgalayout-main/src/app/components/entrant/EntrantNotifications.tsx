import { useState } from 'react';
import { mockNotifications } from '../../data/mockData';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/card';
import { Button } from '../ui/button';
import { Badge } from '../ui/badge';
import { Switch } from '../ui/switch';
import { Label } from '../ui/label';
import { Bell, BellOff, CheckCircle, XCircle, Info, Trash2 } from 'lucide-react';
import { toast } from 'sonner';

export function EntrantNotifications() {
  const [notifications, setNotifications] = useState(mockNotifications);
  const [notificationsEnabled, setNotificationsEnabled] = useState(true);

  const unreadCount = notifications.filter((n) => !n.read).length;

  const markAsRead = (id: string) => {
    setNotifications(
      notifications.map((n) => (n.id === id ? { ...n, read: true } : n))
    );
  };

  const markAllAsRead = () => {
    setNotifications(notifications.map((n) => ({ ...n, read: true })));
    toast.success('All notifications marked as read');
  };

  const deleteNotification = (id: string) => {
    setNotifications(notifications.filter((n) => n.id !== id));
    toast.info('Notification deleted');
  };

  const getNotificationIcon = (type: string) => {
    switch (type) {
      case 'selected':
        return <CheckCircle className="w-5 h-5 text-green-500" />;
      case 'not-selected':
        return <XCircle className="w-5 h-5 text-orange-500" />;
      case 'confirmed':
        return <CheckCircle className="w-5 h-5 text-blue-500" />;
      case 'cancelled':
        return <XCircle className="w-5 h-5 text-red-500" />;
      default:
        return <Info className="w-5 h-5 text-gray-500" />;
    }
  };

  return (
    <div className="p-4 md:p-6 max-w-4xl mx-auto">
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">Notifications</h1>
        <p className="text-gray-600">
          Stay updated on your event lottery status
          {unreadCount > 0 && ` (${unreadCount} unread)`}
        </p>
      </div>

      {/* Notification Settings */}
      <Card className="mb-6">
        <CardHeader>
          <CardTitle className="text-lg">Notification Settings</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              {notificationsEnabled ? (
                <Bell className="w-5 h-5 text-blue-600" />
              ) : (
                <BellOff className="w-5 h-5 text-gray-400" />
              )}
              <div>
                <Label htmlFor="notifications-toggle" className="text-base">
                  Push Notifications
                </Label>
                <p className="text-sm text-gray-600">
                  Receive notifications from organizers and admins
                </p>
              </div>
            </div>
            <Switch
              id="notifications-toggle"
              checked={notificationsEnabled}
              onCheckedChange={(checked) => {
                setNotificationsEnabled(checked);
                toast.success(
                  checked ? 'Notifications enabled' : 'Notifications disabled'
                );
              }}
            />
          </div>

          {unreadCount > 0 && (
            <Button variant="outline" onClick={markAllAsRead} className="w-full">
              Mark All as Read
            </Button>
          )}
        </CardContent>
      </Card>

      {/* Notifications List */}
      <div className="space-y-4">
        {notifications.length > 0 ? (
          notifications.map((notification) => (
            <Card
              key={notification.id}
              className={notification.read ? 'opacity-75' : 'border-blue-200'}
            >
              <CardContent className="p-4">
                <div className="flex gap-4">
                  <div className="flex-shrink-0 mt-1">
                    {getNotificationIcon(notification.type)}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-start justify-between gap-2 mb-2">
                      <div className="flex-1">
                        <h3 className="font-semibold text-gray-900 mb-1">
                          {notification.eventName}
                        </h3>
                        {!notification.read && (
                          <Badge variant="default" className="mb-2">
                            New
                          </Badge>
                        )}
                      </div>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => deleteNotification(notification.id)}
                      >
                        <Trash2 className="w-4 h-4 text-gray-400 hover:text-red-600" />
                      </Button>
                    </div>
                    <p className="text-gray-700 mb-2">{notification.message}</p>
                    <div className="flex items-center justify-between">
                      <p className="text-sm text-gray-500">
                        {new Date(notification.timestamp).toLocaleString()}
                      </p>
                      {!notification.read && (
                        <Button
                          variant="link"
                          size="sm"
                          onClick={() => markAsRead(notification.id)}
                          className="text-blue-600"
                        >
                          Mark as read
                        </Button>
                      )}
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))
        ) : (
          <Card>
            <CardContent className="py-12 text-center">
              <Bell className="w-12 h-12 text-gray-300 mx-auto mb-4" />
              <h3 className="text-lg font-semibold text-gray-900 mb-2">
                No notifications yet
              </h3>
              <p className="text-gray-600">
                You'll receive notifications about lottery draws and event updates
              </p>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  );
}
