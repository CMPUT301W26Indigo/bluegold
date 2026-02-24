import { useState } from 'react';
import { Link } from 'react-router';
import { mockEvents, mockEntrant } from '../../data/mockData';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/card';
import { Button } from '../ui/button';
import { Badge } from '../ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../ui/tabs';
import { Calendar, MapPin, Clock, CheckCircle, XCircle, Loader2 } from 'lucide-react';

export function MyEvents() {
  const [activeTab, setActiveTab] = useState('waiting');

  // Mock status for demo - in real app this would come from backend
  const eventsWithStatus = mockEvents
    .filter((e) => mockEntrant.joinedEvents.includes(e.id))
    .map((event) => ({
      ...event,
      status: event.id === '1' ? 'waiting' : 'waiting',
      lotteryDate: new Date(event.registrationClose),
    }));

  const waitingEvents = eventsWithStatus.filter((e) => e.status === 'waiting');
  const selectedEvents = eventsWithStatus.filter((e) => e.status === 'selected');
  const confirmedEvents = eventsWithStatus.filter((e) => e.status === 'confirmed');
  const rejectedEvents = eventsWithStatus.filter((e) => e.status === 'rejected');

  const EventCard = ({ event, showActions = false }: any) => (
    <Card>
      <CardHeader>
        <div className="flex items-start justify-between gap-2">
          <CardTitle className="text-xl">{event.name}</CardTitle>
          {event.status === 'waiting' && (
            <Badge variant="secondary">
              <Loader2 className="w-3 h-3 mr-1 animate-spin" />
              Waiting
            </Badge>
          )}
          {event.status === 'selected' && (
            <Badge className="bg-green-500">
              <CheckCircle className="w-3 h-3 mr-1" />
              Selected
            </Badge>
          )}
          {event.status === 'confirmed' && (
            <Badge className="bg-blue-500">
              <CheckCircle className="w-3 h-3 mr-1" />
              Confirmed
            </Badge>
          )}
          {event.status === 'rejected' && (
            <Badge variant="outline">
              <XCircle className="w-3 h-3 mr-1" />
              Not Selected
            </Badge>
          )}
        </div>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="space-y-2 text-sm">
          <div className="flex items-center gap-2 text-gray-600">
            <Calendar className="w-4 h-4" />
            <span>{new Date(event.startDate).toLocaleDateString()}</span>
          </div>
          <div className="flex items-center gap-2 text-gray-600">
            <MapPin className="w-4 h-4" />
            <span>{event.location}</span>
          </div>
          {event.status === 'waiting' && (
            <div className="flex items-center gap-2 text-orange-600 bg-orange-50 px-3 py-2 rounded-md mt-2">
              <Clock className="w-4 h-4" />
              <span>Lottery draw on {event.lotteryDate.toLocaleDateString()}</span>
            </div>
          )}
        </div>

        {showActions && event.status === 'selected' && (
          <div className="flex gap-2 pt-2">
            <Button className="flex-1 bg-green-600 hover:bg-green-700">
              Accept Invitation
            </Button>
            <Button variant="outline" className="flex-1">
              Decline
            </Button>
          </div>
        )}

        <Link to={`/entrant/events/${event.id}`}>
          <Button variant="outline" className="w-full">
            View Details
          </Button>
        </Link>
      </CardContent>
    </Card>
  );

  return (
    <div className="p-4 md:p-6 max-w-4xl mx-auto">
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">My Events</h1>
        <p className="text-gray-600">Track your event registrations and lottery status</p>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="grid w-full grid-cols-4 mb-6">
          <TabsTrigger value="waiting">
            Waiting ({waitingEvents.length})
          </TabsTrigger>
          <TabsTrigger value="selected">
            Selected ({selectedEvents.length})
          </TabsTrigger>
          <TabsTrigger value="confirmed">
            Confirmed ({confirmedEvents.length})
          </TabsTrigger>
          <TabsTrigger value="history">
            History ({rejectedEvents.length})
          </TabsTrigger>
        </TabsList>

        <TabsContent value="waiting" className="space-y-4">
          {waitingEvents.length > 0 ? (
            waitingEvents.map((event) => (
              <EventCard key={event.id} event={event} />
            ))
          ) : (
            <Card>
              <CardContent className="py-12 text-center">
                <Loader2 className="w-12 h-12 text-gray-300 mx-auto mb-4" />
                <h3 className="text-lg font-semibold text-gray-900 mb-2">
                  No events in waiting
                </h3>
                <p className="text-gray-600 mb-4">
                  Join event waiting lists to see them here
                </p>
                <Link to="/entrant/events">
                  <Button>Browse Events</Button>
                </Link>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        <TabsContent value="selected" className="space-y-4">
          {selectedEvents.length > 0 ? (
            selectedEvents.map((event) => (
              <EventCard key={event.id} event={event} showActions />
            ))
          ) : (
            <Card>
              <CardContent className="py-12 text-center">
                <CheckCircle className="w-12 h-12 text-gray-300 mx-auto mb-4" />
                <h3 className="text-lg font-semibold text-gray-900 mb-2">
                  No selected events yet
                </h3>
                <p className="text-gray-600">
                  You'll see events here when you're selected in a lottery draw
                </p>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        <TabsContent value="confirmed" className="space-y-4">
          {confirmedEvents.length > 0 ? (
            confirmedEvents.map((event) => (
              <EventCard key={event.id} event={event} />
            ))
          ) : (
            <Card>
              <CardContent className="py-12 text-center">
                <Calendar className="w-12 h-12 text-gray-300 mx-auto mb-4" />
                <h3 className="text-lg font-semibold text-gray-900 mb-2">
                  No confirmed events
                </h3>
                <p className="text-gray-600">
                  Events you've confirmed will appear here
                </p>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        <TabsContent value="history" className="space-y-4">
          {rejectedEvents.length > 0 ? (
            rejectedEvents.map((event) => (
              <EventCard key={event.id} event={event} />
            ))
          ) : (
            <Card>
              <CardContent className="py-12 text-center">
                <XCircle className="w-12 h-12 text-gray-300 mx-auto mb-4" />
                <h3 className="text-lg font-semibold text-gray-900 mb-2">
                  No history yet
                </h3>
                <p className="text-gray-600">
                  Your past lottery results will appear here
                </p>
              </CardContent>
            </Card>
          )}
        </TabsContent>
      </Tabs>
    </div>
  );
}
