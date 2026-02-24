import { useState } from 'react';
import { mockEvents } from '../../data/mockData';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/card';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Badge } from '../ui/badge';
import { Search, Trash2, Eye, Calendar, MapPin, Users, AlertTriangle, Flag, Filter } from 'lucide-react';
import { toast } from 'sonner';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../ui/select';
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
import { Alert, AlertDescription } from '../ui/alert';

export function AdminEvents() {
  const [events, setEvents] = useState(mockEvents);
  const [searchTerm, setSearchTerm] = useState('');
  const [flagFilter, setFlagFilter] = useState<'all' | 'flagged' | 'normal'>('all');

  const filteredEvents = events
    .filter(
      (event) =>
        (event.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        event.organizerName.toLowerCase().includes(searchTerm.toLowerCase())) &&
        (flagFilter === 'all' || 
         (flagFilter === 'flagged' && event.flagged) ||
         (flagFilter === 'normal' && !event.flagged))
    )
    // Sort flagged events first
    .sort((a, b) => {
      if (a.flagged && !b.flagged) return -1;
      if (!a.flagged && b.flagged) return 1;
      return 0;
    });

  const flaggedCount = events.filter(e => e.flagged).length;

  const handleDeleteEvent = (eventId: string, eventName: string) => {
    setEvents(events.filter((e) => e.id !== eventId));
    toast.success('Event removed', {
      description: `"${eventName}" has been permanently deleted`,
    });
  };

  const handleUnflagEvent = (eventId: string) => {
    setEvents(events.map(e => 
      e.id === eventId ? { ...e, flagged: false, flagReason: undefined } : e
    ));
    toast.success('Event unflagged');
  };

  return (
    <div className="p-4 md:p-6 max-w-6xl mx-auto">
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">Manage Events</h1>
        <p className="text-gray-600">Review and remove events that violate policies</p>
      </div>

      {/* Flagged Events Alert */}
      {flaggedCount > 0 && (
        <Alert className="mb-6 border-red-200 bg-red-50">
          <AlertTriangle className="h-4 w-4 text-red-600" />
          <AlertDescription className="text-red-800">
            <strong>{flaggedCount}</strong> event{flaggedCount !== 1 ? 's' : ''} flagged for review
          </AlertDescription>
        </Alert>
      )}

      {/* Search and Filter */}
      <div className="mb-6 flex flex-col sm:flex-row gap-4">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
          <Input
            type="text"
            placeholder="Search events by name or organizer..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pl-10"
          />
        </div>
        <Select value={flagFilter} onValueChange={(val: 'all' | 'flagged' | 'normal') => setFlagFilter(val)}>
          <SelectTrigger className="w-full sm:w-48">
            <Filter className="w-4 h-4 mr-2" />
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All Events</SelectItem>
            <SelectItem value="flagged">Flagged Only ({flaggedCount})</SelectItem>
            <SelectItem value="normal">Normal Only</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {/* Events List */}
      <div className="space-y-4">
        {filteredEvents.map((event) => (
          <Card key={event.id} className={event.flagged ? 'border-red-300 bg-red-50/50' : ''}>
            <CardHeader>
              <div className="flex items-start justify-between gap-4">
                <div className="flex-1">
                  <div className="flex items-start gap-2 mb-2">
                    {event.flagged && (
                      <Flag className="w-5 h-5 text-red-600 mt-0.5 shrink-0" />
                    )}
                    <CardTitle className="text-xl">{event.name}</CardTitle>
                  </div>
                  
                  {event.flagged && event.flagReason && (
                    <Alert className="mb-3 border-red-300 bg-red-100">
                      <AlertTriangle className="h-4 w-4 text-red-600" />
                      <AlertDescription className="text-red-800 text-sm">
                        <strong>Flagged:</strong> {event.flagReason}
                      </AlertDescription>
                    </Alert>
                  )}
                  
                  <p className="text-sm text-gray-600 mb-3">{event.description}</p>
                  <div className="flex flex-wrap gap-2">
                    <Badge variant="secondary">{event.organizerName}</Badge>
                    <Badge variant="outline">
                      {event.waitingListCount} entrants
                    </Badge>
                    {event.geolocationRequired && (
                      <Badge variant="outline">Geolocation Required</Badge>
                    )}
                    {event.radiusRestrictionKm && (
                      <Badge variant="outline">Radius: {event.radiusRestrictionKm}km</Badge>
                    )}
                    {event.tags.slice(0, 3).map((tag) => (
                      <Badge key={tag} variant="outline" className="text-xs">
                        {tag}
                      </Badge>
                    ))}
                  </div>
                </div>
                {event.posterUrl && (
                  <img
                    src={event.posterUrl}
                    alt={event.name}
                    className="w-24 h-24 object-cover rounded-lg"
                  />
                )}
              </div>
            </CardHeader>
            <CardContent>
              <div className="grid md:grid-cols-3 gap-4 mb-4">
                <div className="flex items-center gap-2 text-sm text-gray-600">
                  <Calendar className="w-4 h-4" />
                  <span>{new Date(event.startDate).toLocaleDateString()}</span>
                </div>
                <div className="flex items-center gap-2 text-sm text-gray-600">
                  <MapPin className="w-4 h-4" />
                  <span>{event.location}</span>
                </div>
                <div className="flex items-center gap-2 text-sm text-gray-600">
                  <Users className="w-4 h-4" />
                  <span>{event.capacity} capacity</span>
                </div>
              </div>

              <div className="flex gap-2 flex-wrap">
                <Button variant="outline" size="sm">
                  <Eye className="w-4 h-4 mr-2" />
                  View Details
                </Button>
                {event.flagged && (
                  <Button 
                    variant="outline" 
                    size="sm"
                    onClick={() => handleUnflagEvent(event.id)}
                  >
                    <Flag className="w-4 h-4 mr-2" />
                    Unflag
                  </Button>
                )}
                <AlertDialog>
                  <AlertDialogTrigger asChild>
                    <Button variant="destructive" size="sm">
                      <Trash2 className="w-4 h-4 mr-2" />
                      Remove Event
                    </Button>
                  </AlertDialogTrigger>
                  <AlertDialogContent>
                    <AlertDialogHeader>
                      <AlertDialogTitle>Remove Event</AlertDialogTitle>
                      <AlertDialogDescription>
                        Are you sure you want to permanently remove "{event.name}"?
                        This action cannot be undone. All entrants will be notified.
                      </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                      <AlertDialogCancel>Cancel</AlertDialogCancel>
                      <AlertDialogAction
                        onClick={() => handleDeleteEvent(event.id, event.name)}
                        className="bg-red-600 hover:bg-red-700"
                      >
                        Remove Event
                      </AlertDialogAction>
                    </AlertDialogFooter>
                  </AlertDialogContent>
                </AlertDialog>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {filteredEvents.length === 0 && (
        <Card>
          <CardContent className="py-12 text-center">
            <Calendar className="w-12 h-12 text-gray-300 mx-auto mb-4" />
            <h3 className="text-lg font-semibold text-gray-900 mb-2">
              No events found
            </h3>
            <p className="text-gray-600">Try adjusting your search or filters</p>
          </CardContent>
        </Card>
      )}
    </div>
  );
}