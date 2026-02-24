import { useState } from 'react';
import { Link } from 'react-router';
import { mockEvents, mockEntrant } from '../../data/mockData';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/card';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Badge } from '../ui/badge';
import { 
  Calendar, 
  MapPin, 
  Users, 
  DollarSign, 
  Search,
  Filter,
  Clock,
  X,
  Tag,
  Navigation,
  MapPinOff
} from 'lucide-react';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../ui/select';
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
} from '../ui/sheet';
import { Checkbox } from '../ui/checkbox';
import { ScrollArea } from '../ui/scroll-area';
import { Alert, AlertDescription } from '../ui/alert';

// Extract all unique tags from events
const getAllTags = () => {
  const tags = new Set<string>();
  mockEvents.forEach(event => {
    event.tags.forEach(tag => tags.add(tag));
  });
  return Array.from(tags).sort();
};

// Calculate distance between two coordinates using Haversine formula
const calculateDistance = (
  lat1: number,
  lon1: number,
  lat2: number,
  lon2: number
): number => {
  const R = 6371; // Radius of the Earth in kilometers
  const dLat = ((lat2 - lat1) * Math.PI) / 180;
  const dLon = ((lon2 - lon1) * Math.PI) / 180;
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos((lat1 * Math.PI) / 180) *
      Math.cos((lat2 * Math.PI) / 180) *
      Math.sin(dLon / 2) *
      Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
};

export function EntrantEvents() {
  const [searchTerm, setSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState('all');
  const [selectedTags, setSelectedTags] = useState<string[]>([]);
  const [isFilterSheetOpen, setIsFilterSheetOpen] = useState(false);

  const today = new Date('2026-02-17');
  const allTags = getAllTags();
  const entrant = mockEntrant;
  
  const getEventStatus = (event: typeof mockEvents[0]) => {
    const regOpen = new Date(event.registrationOpen);
    const regClose = new Date(event.registrationClose);
    
    if (today < regOpen) return 'upcoming';
    if (today >= regOpen && today <= regClose) return 'open';
    return 'closed';
  };

  const toggleTag = (tag: string) => {
    setSelectedTags(prev => 
      prev.includes(tag) 
        ? prev.filter(t => t !== tag)
        : [...prev, tag]
    );
  };

  const clearAllTags = () => {
    setSelectedTags([]);
  };

  // Filter events based on all criteria including radius restriction
  const filteredEvents = mockEvents.filter((event) => {
    // Check radius restriction
    if (event.radiusRestrictionKm && event.locationCoords) {
      // If event has radius restriction, user must have geolocation enabled and be within range
      if (!entrant.geolocationEnabled || !entrant.currentLocation) {
        return false; // Don't show events with radius restriction if geolocation is disabled
      }
      
      const distance = calculateDistance(
        entrant.currentLocation.lat,
        entrant.currentLocation.lng,
        event.locationCoords.lat,
        event.locationCoords.lng
      );
      
      if (distance > event.radiusRestrictionKm) {
        return false; // User is too far from event
      }
    }

    const matchesSearch = event.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         event.description.toLowerCase().includes(searchTerm.toLowerCase());
    const status = getEventStatus(event);
    const matchesFilter = filterStatus === 'all' || status === filterStatus;
    const matchesTags = selectedTags.length === 0 || 
                       selectedTags.some(tag => event.tags.includes(tag));
    return matchesSearch && matchesFilter && matchesTags;
  });

  // Count events hidden by radius restriction
  const hiddenByRadius = mockEvents.filter(event => {
    if (event.radiusRestrictionKm && event.locationCoords) {
      if (!entrant.geolocationEnabled || !entrant.currentLocation) {
        return true;
      }
      const distance = calculateDistance(
        entrant.currentLocation.lat,
        entrant.currentLocation.lng,
        event.locationCoords.lat,
        event.locationCoords.lng
      );
      return distance > event.radiusRestrictionKm;
    }
    return false;
  }).length;

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="p-4 md:p-6 max-w-7xl mx-auto pb-20 md:pb-6">
        {/* Header */}
        <div className="mb-4 md:mb-6">
          <h1 className="text-2xl md:text-3xl font-bold text-gray-900 mb-1 md:mb-2">Browse Events</h1>
          <p className="text-sm md:text-base text-gray-600">
            Join waiting lists for events you're interested in
          </p>
        </div>

        {/* Search and Filter Bar */}
        <div className="mb-4 flex flex-col gap-3">
          <div className="flex gap-2">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4 md:w-5 md:h-5" />
              <Input
                type="text"
                placeholder="Search events..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-9 md:pl-10 h-11 md:h-10"
              />
            </div>
            <Sheet open={isFilterSheetOpen} onOpenChange={setIsFilterSheetOpen}>
              <SheetTrigger asChild>
                <Button variant="outline" size="default" className="shrink-0 h-11 md:h-10 px-3 md:px-4">
                  <Filter className="w-4 h-4 mr-2" />
                  <span className="hidden sm:inline">Filters</span>
                  {selectedTags.length > 0 && (
                    <Badge variant="secondary" className="ml-2">
                      {selectedTags.length}
                    </Badge>
                  )}
                </Button>
              </SheetTrigger>
              <SheetContent side="right" className="w-full sm:w-[400px]">
                <SheetHeader>
                  <SheetTitle>Filter Events</SheetTitle>
                  <SheetDescription>
                    Filter events by tags and availability
                  </SheetDescription>
                </SheetHeader>
                <div className="mt-6 space-y-6">
                  {/* Status Filter */}
                  <div>
                    <label className="text-sm font-medium mb-2 block">Registration Status</label>
                    <Select value={filterStatus} onValueChange={setFilterStatus}>
                      <SelectTrigger className="w-full">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="all">All Events</SelectItem>
                        <SelectItem value="open">Open for Registration</SelectItem>
                        <SelectItem value="upcoming">Opening Soon</SelectItem>
                        <SelectItem value="closed">Registration Closed</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  {/* Tag Filter */}
                  <div>
                    <div className="flex items-center justify-between mb-3">
                      <label className="text-sm font-medium">Tags</label>
                      {selectedTags.length > 0 && (
                        <Button 
                          variant="ghost" 
                          size="sm" 
                          onClick={clearAllTags}
                          className="h-auto p-0 text-xs"
                        >
                          Clear all
                        </Button>
                      )}
                    </div>
                    <ScrollArea className="h-[400px] pr-4">
                      <div className="space-y-3">
                        {allTags.map((tag) => (
                          <div key={tag} className="flex items-center space-x-2">
                            <Checkbox
                              id={tag}
                              checked={selectedTags.includes(tag)}
                              onCheckedChange={() => toggleTag(tag)}
                            />
                            <label
                              htmlFor={tag}
                              className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70 cursor-pointer flex-1"
                            >
                              {tag}
                            </label>
                          </div>
                        ))}
                      </div>
                    </ScrollArea>
                  </div>
                </div>
              </SheetContent>
            </Sheet>
          </div>

          {/* Selected Tags Display */}
          {selectedTags.length > 0 && (
            <div className="flex flex-wrap gap-2">
              {selectedTags.map((tag) => (
                <Badge 
                  key={tag} 
                  variant="secondary" 
                  className="pl-2 pr-1 py-1 text-xs md:text-sm"
                >
                  {tag}
                  <button
                    onClick={() => toggleTag(tag)}
                    className="ml-1 hover:bg-gray-300 rounded-full p-0.5"
                  >
                    <X className="w-3 h-3" />
                  </button>
                </Badge>
              ))}
            </div>
          )}
        </div>

        {/* Results Count */}
        <div className="mb-4 text-sm text-gray-600">
          Showing {filteredEvents.length} of {mockEvents.length} events
        </div>

        {/* Events Grid */}
        <div className="grid gap-4 md:gap-6 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3">
          {filteredEvents.map((event) => {
            const status = getEventStatus(event);
            const regCloseDate = new Date(event.registrationClose);
            const daysUntilClose = Math.ceil((regCloseDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));

            return (
              <Card key={event.id} className="hover:shadow-lg transition-shadow flex flex-col">
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
                    <Badge
                      variant={status === 'open' ? 'default' : status === 'upcoming' ? 'secondary' : 'outline'}
                      className="shrink-0"
                    >
                      {status === 'open' ? 'Open' : status === 'upcoming' ? 'Soon' : 'Closed'}
                    </Badge>
                  </div>
                  <p className="text-xs md:text-sm text-gray-600 line-clamp-2">{event.description}</p>
                </CardHeader>
                <CardContent className="space-y-3 flex-1 flex flex-col">
                  {/* Tags */}
                  <div className="flex flex-wrap gap-1.5">
                    {event.tags.slice(0, 3).map((tag) => (
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

                  <div className="grid grid-cols-2 gap-2 md:gap-3 text-xs md:text-sm">
                    <div className="flex items-center gap-1.5 md:gap-2 text-gray-600">
                      <Calendar className="w-3.5 h-3.5 md:w-4 md:h-4 shrink-0" />
                      <span className="truncate">{new Date(event.startDate).toLocaleDateString()}</span>
                    </div>
                    <div className="flex items-center gap-1.5 md:gap-2 text-gray-600">
                      <MapPin className="w-3.5 h-3.5 md:w-4 md:h-4 shrink-0" />
                      <span className="truncate">{event.location.split(' ')[0]}</span>
                    </div>
                    <div className="flex items-center gap-1.5 md:gap-2 text-gray-600">
                      <DollarSign className="w-3.5 h-3.5 md:w-4 md:h-4 shrink-0" />
                      <span>${event.price}</span>
                    </div>
                    <div className="flex items-center gap-1.5 md:gap-2 text-gray-600">
                      <Users className="w-3.5 h-3.5 md:w-4 md:h-4 shrink-0" />
                      <span>{event.waitingListCount} waiting</span>
                    </div>
                  </div>

                  {status === 'open' && daysUntilClose <= 7 && (
                    <div className="flex items-center gap-2 text-orange-600 bg-orange-50 px-2.5 md:px-3 py-2 rounded-md text-xs md:text-sm">
                      <Clock className="w-3.5 h-3.5 md:w-4 md:h-4 shrink-0" />
                      <span>Closes in {daysUntilClose} days</span>
                    </div>
                  )}

                  <Link to={`/entrant/events/${event.id}`} className="mt-auto">
                    <Button className="w-full h-10 md:h-9 text-sm md:text-base">View Details</Button>
                  </Link>
                </CardContent>
              </Card>
            );
          })}
        </div>

        {filteredEvents.length === 0 && (
          <div className="text-center py-12 md:py-16">
            <Tag className="w-12 h-12 md:w-16 md:h-16 text-gray-300 mx-auto mb-4" />
            <h3 className="text-base md:text-lg font-semibold text-gray-900 mb-2">No events found</h3>
            <p className="text-sm md:text-base text-gray-600 mb-4">Try adjusting your search or filters</p>
            {(selectedTags.length > 0 || searchTerm) && (
              <Button 
                variant="outline" 
                onClick={() => {
                  setSearchTerm('');
                  setSelectedTags([]);
                  setFilterStatus('all');
                }}
              >
                Clear all filters
              </Button>
            )}
          </div>
        )}

        {hiddenByRadius > 0 && (
          <Alert className="mt-4">
            <Navigation className="h-4 w-4" />
            <AlertDescription>
              {hiddenByRadius} events are hidden because they are outside your radius restriction.
            </AlertDescription>
          </Alert>
        )}
      </div>
    </div>
  );
}