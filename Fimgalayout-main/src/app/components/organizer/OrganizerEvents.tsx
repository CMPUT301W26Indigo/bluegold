import { useState } from 'react';
import { Link } from 'react-router';
import { mockEvents } from '../../data/mockData';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/card';
import { Button } from '../ui/button';
import { Badge } from '../ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../ui/tabs';
import {
  Calendar,
  MapPin,
  Users,
  Plus,
  Settings,
  QrCode,
  TrendingUp,
} from 'lucide-react';

export function OrganizerEvents() {
  const [activeTab, setActiveTab] = useState('active');

  // Filter events by organizer
  const organizerEvents = mockEvents.filter((e) => e.organizerId === 'org1');
  
  const activeEvents = organizerEvents.filter((e) => {
    const today = new Date('2026-02-17');
    const endDate = new Date(e.endDate);
    return endDate >= today;
  });

  const pastEvents = organizerEvents.filter((e) => {
    const today = new Date('2026-02-17');
    const endDate = new Date(e.endDate);
    return endDate < today;
  });

  const EventCard = ({ event }: any) => {
    const registrationClosed = new Date('2026-02-17') > new Date(event.registrationClose);
    const fillRate = (event.confirmedCount / event.capacity) * 100;

    return (
      <Card className="hover:shadow-lg transition-shadow">
        {event.posterUrl && (
          <div className="h-40 md:h-48 overflow-hidden rounded-t-lg">
            <img
              src={event.posterUrl}
              alt={event.name}
              className="w-full h-full object-cover"
            />
          </div>
        )}
        <CardHeader className="pb-3">
          <div className="flex items-start justify-between gap-2 mb-2">
            <CardTitle className="text-lg md:text-xl line-clamp-2">{event.name}</CardTitle>
            <Badge variant={registrationClosed ? 'outline' : 'default'} className="shrink-0">
              {registrationClosed ? 'In Progress' : 'Open'}
            </Badge>
          </div>
          {/* Tags */}
          <div className="flex flex-wrap gap-1.5">
            {event.tags.slice(0, 3).map((tag: string) => (
              <Badge key={tag} variant="outline" className="text-xs">
                {tag}
              </Badge>
            ))}
            {event.tags.length > 3 && (
              <Badge variant="outline" className="text-xs">
                +{event.tags.length - 3}
              </Badge>
            )}
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-2 gap-2 md:gap-3 text-xs md:text-sm">
            <div className="flex items-center gap-1.5 md:gap-2 text-gray-600">
              <Calendar className="w-3.5 h-3.5 md:w-4 md:h-4 shrink-0" />
              <span className="truncate">{new Date(event.startDate).toLocaleDateString()}</span>
            </div>
            <div className="flex items-center gap-1.5 md:gap-2 text-gray-600">
              <MapPin className="w-3.5 h-3.5 md:w-4 md:h-4 shrink-0" />
              <span className="truncate">{event.location.split(' ')[0]}</span>
            </div>
          </div>

          {/* Stats */}
          <div className="grid grid-cols-3 gap-2 p-3 bg-gray-50 rounded-lg">
            <div className="text-center">
              <p className="text-xl md:text-2xl font-bold text-blue-600">
                {event.waitingListCount}
              </p>
              <p className="text-xs text-gray-600">Waiting</p>
            </div>
            <div className="text-center">
              <p className="text-xl md:text-2xl font-bold text-green-600">
                {event.selectedCount}
              </p>
              <p className="text-xs text-gray-600">Selected</p>
            </div>
            <div className="text-center">
              <p className="text-xl md:text-2xl font-bold text-purple-600">
                {event.confirmedCount}
              </p>
              <p className="text-xs text-gray-600">Confirmed</p>
            </div>
          </div>

          {/* Fill Rate */}
          <div>
            <div className="flex justify-between text-xs md:text-sm mb-1">
              <span className="text-gray-600">Fill Rate</span>
              <span className="font-semibold">{Math.round(fillRate)}%</span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div
                className="bg-blue-600 h-2 rounded-full transition-all"
                style={{ width: `${fillRate}%` }}
              />
            </div>
          </div>

          <div className="flex gap-2">
            <Link to={`/organizer/manage/${event.id}`} className="flex-1">
              <Button variant="outline" className="w-full h-9 text-sm md:text-base">
                <Settings className="w-3.5 h-3.5 md:w-4 md:h-4 mr-2" />
                Manage
              </Button>
            </Link>
            {registrationClosed && event.selectedCount < event.capacity && (
              <Link to={`/organizer/draw/${event.id}`} className="flex-1">
                <Button className="w-full h-9 text-sm md:text-base">
                  <TrendingUp className="w-3.5 h-3.5 md:w-4 md:h-4 mr-2" />
                  Draw
                </Button>
              </Link>
            )}
          </div>
        </CardContent>
      </Card>
    );
  };

  return (
    <div className="p-4 md:p-6 max-w-6xl mx-auto">
      <div className="mb-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 mb-2">My Events</h1>
          <p className="text-gray-600">Manage your events and lottery draws</p>
        </div>
        <Link to="/organizer/create">
          <Button size="lg" className="w-full sm:w-auto">
            <Plus className="w-5 h-5 mr-2" />
            Create Event
          </Button>
        </Link>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="mb-6">
          <TabsTrigger value="active">
            Active Events ({activeEvents.length})
          </TabsTrigger>
          <TabsTrigger value="past">Past Events ({pastEvents.length})</TabsTrigger>
        </TabsList>

        <TabsContent value="active">
          {activeEvents.length > 0 ? (
            <div className="grid gap-6 md:grid-cols-2">
              {activeEvents.map((event) => (
                <EventCard key={event.id} event={event} />
              ))}
            </div>
          ) : (
            <Card>
              <CardContent className="py-12 text-center">
                <Calendar className="w-12 h-12 text-gray-300 mx-auto mb-4" />
                <h3 className="text-lg font-semibold text-gray-900 mb-2">
                  No active events
                </h3>
                <p className="text-gray-600 mb-4">
                  Create your first event to get started
                </p>
                <Link to="/organizer/create">
                  <Button>
                    <Plus className="w-4 h-4 mr-2" />
                    Create Event
                  </Button>
                </Link>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        <TabsContent value="past">
          {pastEvents.length > 0 ? (
            <div className="grid gap-6 md:grid-cols-2">
              {pastEvents.map((event) => (
                <EventCard key={event.id} event={event} />
              ))}
            </div>
          ) : (
            <Card>
              <CardContent className="py-12 text-center">
                <Calendar className="w-12 h-12 text-gray-300 mx-auto mb-4" />
                <h3 className="text-lg font-semibold text-gray-900 mb-2">
                  No past events
                </h3>
                <p className="text-gray-600">
                  Completed events will appear here
                </p>
              </CardContent>
            </Card>
          )}
        </TabsContent>
      </Tabs>
    </div>
  );
}