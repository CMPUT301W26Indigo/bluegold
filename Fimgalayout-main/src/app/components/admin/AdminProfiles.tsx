import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/card';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Badge } from '../ui/badge';
import { Avatar, AvatarFallback } from '../ui/avatar';
import { Search, Trash2, Eye, Mail, Users as UsersIcon, Shield } from 'lucide-react';
import { toast } from 'sonner';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from '../ui/alert-dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../ui/select';

interface User {
  id: string;
  name: string;
  email: string;
  role: 'entrant' | 'organizer';
  deviceId: string;
  joinedEvents: number;
  organizedEvents?: number;
  status: 'active' | 'suspended';
}

const mockUsers: User[] = [
  {
    id: '1',
    name: 'Alex Johnson',
    email: 'alex.johnson@email.com',
    role: 'entrant',
    deviceId: 'device-12345',
    joinedEvents: 2,
    status: 'active',
  },
  {
    id: '2',
    name: 'Sarah Smith',
    email: 'sarah.smith@email.com',
    role: 'entrant',
    deviceId: 'device-67890',
    joinedEvents: 5,
    status: 'active',
  },
  {
    id: '3',
    name: 'Community Recreation Centre',
    email: 'admin@comrec.org',
    role: 'organizer',
    deviceId: 'device-org001',
    joinedEvents: 0,
    organizedEvents: 3,
    status: 'active',
  },
  {
    id: '4',
    name: 'Music Academy',
    email: 'info@musicacademy.com',
    role: 'organizer',
    deviceId: 'device-org002',
    joinedEvents: 0,
    organizedEvents: 1,
    status: 'active',
  },
];

export function AdminProfiles() {
  const [users, setUsers] = useState<User[]>(mockUsers);
  const [searchTerm, setSearchTerm] = useState('');
  const [roleFilter, setRoleFilter] = useState<'all' | 'entrant' | 'organizer'>('all');

  const filteredUsers = users.filter((user) => {
    const matchesSearch =
      user.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.email.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesRole = roleFilter === 'all' || user.role === roleFilter;
    return matchesSearch && matchesRole;
  });

  const handleDeleteProfile = (userId: string, userName: string) => {
    setUsers(users.filter((u) => u.id !== userId));
    toast.success('Profile removed', {
      description: `"${userName}" has been permanently deleted`,
    });
  };

  const handleRemoveOrganizer = (userId: string, userName: string) => {
    setUsers(users.filter((u) => u.id !== userId));
    toast.success('Organizer removed', {
      description: `"${userName}" has been removed for policy violations`,
    });
  };

  const getInitials = (name: string) => {
    return name
      .split(' ')
      .map((n) => n[0])
      .join('')
      .toUpperCase()
      .substring(0, 2);
  };

  return (
    <div className="p-4 md:p-6 max-w-6xl mx-auto">
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">Manage Profiles</h1>
        <p className="text-gray-600">
          Review and remove profiles and organizers that violate policies
        </p>
      </div>

      {/* Search and Filter */}
      <div className="mb-6 flex flex-col sm:flex-row gap-4">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
          <Input
            type="text"
            placeholder="Search by name or email..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pl-10"
          />
        </div>
        <Select value={roleFilter} onValueChange={(value: any) => setRoleFilter(value)}>
          <SelectTrigger className="w-full sm:w-48">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All Users</SelectItem>
            <SelectItem value="entrant">Entrants</SelectItem>
            <SelectItem value="organizer">Organizers</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {/* Stats */}
      <div className="grid md:grid-cols-3 gap-4 mb-6">
        <Card>
          <CardContent className="pt-6">
            <div className="text-center">
              <UsersIcon className="w-8 h-8 text-blue-600 mx-auto mb-2" />
              <p className="text-3xl font-bold text-gray-900">{users.length}</p>
              <p className="text-sm text-gray-600">Total Users</p>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="pt-6">
            <div className="text-center">
              <UsersIcon className="w-8 h-8 text-green-600 mx-auto mb-2" />
              <p className="text-3xl font-bold text-gray-900">
                {users.filter((u) => u.role === 'entrant').length}
              </p>
              <p className="text-sm text-gray-600">Entrants</p>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="pt-6">
            <div className="text-center">
              <Shield className="w-8 h-8 text-purple-600 mx-auto mb-2" />
              <p className="text-3xl font-bold text-gray-900">
                {users.filter((u) => u.role === 'organizer').length}
              </p>
              <p className="text-sm text-gray-600">Organizers</p>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Users List */}
      <div className="space-y-4">
        {filteredUsers.map((user) => (
          <Card key={user.id}>
            <CardContent className="p-4">
              <div className="flex items-start gap-4">
                <Avatar className="w-12 h-12">
                  <AvatarFallback
                    className={
                      user.role === 'organizer'
                        ? 'bg-purple-100 text-purple-600'
                        : 'bg-blue-100 text-blue-600'
                    }
                  >
                    {getInitials(user.name)}
                  </AvatarFallback>
                </Avatar>

                <div className="flex-1 min-w-0">
                  <div className="flex items-start justify-between gap-4 mb-2">
                    <div className="flex-1">
                      <h3 className="font-semibold text-gray-900">{user.name}</h3>
                      <div className="flex items-center gap-2 text-sm text-gray-600 mt-1">
                        <Mail className="w-4 h-4" />
                        <span>{user.email}</span>
                      </div>
                      <p className="text-xs text-gray-500 mt-1">
                        Device: {user.deviceId}
                      </p>
                    </div>
                    <div className="flex flex-col gap-2">
                      <Badge
                        variant={user.role === 'organizer' ? 'default' : 'secondary'}
                      >
                        {user.role === 'organizer' ? (
                          <Shield className="w-3 h-3 mr-1" />
                        ) : (
                          <UsersIcon className="w-3 h-3 mr-1" />
                        )}
                        {user.role}
                      </Badge>
                      <Badge
                        variant={user.status === 'active' ? 'outline' : 'destructive'}
                      >
                        {user.status}
                      </Badge>
                    </div>
                  </div>

                  <div className="flex flex-wrap gap-4 text-sm text-gray-600 mb-4">
                    {user.role === 'entrant' && (
                      <span>Joined {user.joinedEvents} events</span>
                    )}
                    {user.role === 'organizer' && (
                      <span>Organized {user.organizedEvents} events</span>
                    )}
                  </div>

                  <div className="flex gap-2">
                    <Button variant="outline" size="sm">
                      <Eye className="w-4 h-4 mr-2" />
                      View Details
                    </Button>

                    {user.role === 'organizer' ? (
                      <AlertDialog>
                        <AlertDialogTrigger asChild>
                          <Button variant="destructive" size="sm">
                            <Trash2 className="w-4 h-4 mr-2" />
                            Remove Organizer
                          </Button>
                        </AlertDialogTrigger>
                        <AlertDialogContent>
                          <AlertDialogHeader>
                            <AlertDialogTitle>Remove Organizer</AlertDialogTitle>
                            <AlertDialogDescription>
                              Are you sure you want to remove "{user.name}" for policy
                              violations? All their events will be cancelled and
                              participants will be notified.
                            </AlertDialogDescription>
                          </AlertDialogHeader>
                          <AlertDialogFooter>
                            <AlertDialogCancel>Cancel</AlertDialogCancel>
                            <AlertDialogAction
                              onClick={() => handleRemoveOrganizer(user.id, user.name)}
                              className="bg-red-600 hover:bg-red-700"
                            >
                              Remove Organizer
                            </AlertDialogAction>
                          </AlertDialogFooter>
                        </AlertDialogContent>
                      </AlertDialog>
                    ) : (
                      <AlertDialog>
                        <AlertDialogTrigger asChild>
                          <Button variant="destructive" size="sm">
                            <Trash2 className="w-4 h-4 mr-2" />
                            Remove Profile
                          </Button>
                        </AlertDialogTrigger>
                        <AlertDialogContent>
                          <AlertDialogHeader>
                            <AlertDialogTitle>Remove Profile</AlertDialogTitle>
                            <AlertDialogDescription>
                              Are you sure you want to permanently remove "{user.name}"?
                              This action cannot be undone. They will be removed from all
                              event waiting lists.
                            </AlertDialogDescription>
                          </AlertDialogHeader>
                          <AlertDialogFooter>
                            <AlertDialogCancel>Cancel</AlertDialogCancel>
                            <AlertDialogAction
                              onClick={() => handleDeleteProfile(user.id, user.name)}
                              className="bg-red-600 hover:bg-red-700"
                            >
                              Remove Profile
                            </AlertDialogAction>
                          </AlertDialogFooter>
                        </AlertDialogContent>
                      </AlertDialog>
                    )}
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {filteredUsers.length === 0 && (
        <Card>
          <CardContent className="py-12 text-center">
            <UsersIcon className="w-12 h-12 text-gray-300 mx-auto mb-4" />
            <h3 className="text-lg font-semibold text-gray-900 mb-2">No users found</h3>
            <p className="text-gray-600">Try adjusting your search or filters</p>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
