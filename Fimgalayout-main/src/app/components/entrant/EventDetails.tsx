import { useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router';
import { mockEvents, mockEntrant } from '../../data/mockData';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/card';
import { Button } from '../ui/button';
import { Badge } from '../ui/badge';
import { QRCodeSVG } from 'qrcode.react';
import { toast } from 'sonner';
import {
  Calendar,
  MapPin,
  Users,
  DollarSign,
  Clock,
  ArrowLeft,
  CheckCircle,
  XCircle,
  MapPinned,
  Info,
} from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '../ui/dialog';
import { Alert, AlertDescription } from '../ui/alert';

export function EventDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const event = mockEvents.find((e) => e.id === id);
  const [isJoined, setIsJoined] = useState(
    mockEntrant.joinedEvents.includes(id || '')
  );
  const [showQR, setShowQR] = useState(false);

  if (!event) {
    return (
      <div className="p-6 max-w-2xl mx-auto text-center">
        <h2 className="text-2xl font-bold mb-4">Event Not Found</h2>
        <Button onClick={() => navigate('/entrant/events')}>
          Back to Events
        </Button>
      </div>
    );
  }

  const today = new Date('2026-02-17');
  const regOpen = new Date(event.registrationOpen);
  const regClose = new Date(event.registrationClose);
  const isOpen = today >= regOpen && today <= regClose;
  const isUpcoming = today < regOpen;
  const isClosed = today > regClose;

  const handleJoinWaitingList = () => {
    setIsJoined(true);
    toast.success('Successfully joined waiting list!', {
      description: `You'll be notified when the lottery is drawn on ${regClose.toLocaleDateString()}`,
    });
  };

  const handleLeaveWaitingList = () => {
    setIsJoined(false);
    toast.info('Left waiting list', {
      description: 'You can rejoin anytime before registration closes',
    });
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 sticky top-0 z-10">
        <div className="max-w-4xl mx-auto px-4 py-3 flex items-center gap-4">
          <Button variant="ghost" size="sm" onClick={() => navigate('/entrant/events')}>
            <ArrowLeft className="w-4 h-4 mr-2" />
            Back
          </Button>
          <div className="flex-1" />
          <Dialog open={showQR} onOpenChange={setShowQR}>
            <DialogTrigger asChild>
              <Button variant="outline" size="sm">
                Show QR Code
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Event QR Code</DialogTitle>
                <DialogDescription>
                  Scan this code to share or quickly access this event
                </DialogDescription>
              </DialogHeader>
              <div className="flex justify-center p-6 bg-white">
                <QRCodeSVG
                  value={`https://eventlottery.app/event/${event.id}`}
                  size={250}
                  level="H"
                  includeMargin
                />
              </div>
            </DialogContent>
          </Dialog>
        </div>
      </div>

      <div className="max-w-4xl mx-auto p-4 md:p-6 space-y-6">
        {/* Event Poster */}
        {event.posterUrl && (
          <div className="rounded-lg overflow-hidden shadow-lg">
            <img
              src={event.posterUrl}
              alt={event.name}
              className="w-full h-64 md:h-96 object-cover"
            />
          </div>
        )}

        {/* Event Header */}
        <div>
          <div className="flex items-start justify-between gap-4 mb-3">
            <h1 className="text-2xl md:text-3xl font-bold text-gray-900">{event.name}</h1>
            <Badge
              variant={isOpen ? 'default' : isUpcoming ? 'secondary' : 'outline'}
              className="text-sm shrink-0"
            >
              {isOpen ? 'Open' : isUpcoming ? 'Opens Soon' : 'Closed'}
            </Badge>
          </div>
          <p className="text-gray-600 text-base md:text-lg mb-3">{event.description}</p>
          
          {/* Tags */}
          <div className="flex flex-wrap gap-2">
            {event.tags.map((tag) => (
              <Badge key={tag} variant="outline" className="text-xs md:text-sm">
                {tag}
              </Badge>
            ))}
          </div>
        </div>

        {/* Registration Status Alert */}
        {isJoined && isOpen && (
          <Alert className="border-green-200 bg-green-50">
            <CheckCircle className="h-4 w-4 text-green-600" />
            <AlertDescription className="text-green-800">
              You're on the waiting list! The lottery will be drawn on{' '}
              {regClose.toLocaleDateString()}.
            </AlertDescription>
          </Alert>
        )}

        {isClosed && !isJoined && (
          <Alert className="border-red-200 bg-red-50">
            <XCircle className="h-4 w-4 text-red-600" />
            <AlertDescription className="text-red-800">
              Registration for this event has closed.
            </AlertDescription>
          </Alert>
        )}

        {/* Event Details Card */}
        <Card>
          <CardHeader>
            <CardTitle>Event Details</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid md:grid-cols-2 gap-4">
              <div className="flex items-start gap-3">
                <Calendar className="w-5 h-5 text-gray-500 mt-0.5" />
                <div>
                  <p className="font-medium text-gray-900">Event Dates</p>
                  <p className="text-sm text-gray-600">
                    {new Date(event.startDate).toLocaleDateString()} -{' '}
                    {new Date(event.endDate).toLocaleDateString()}
                  </p>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <MapPin className="w-5 h-5 text-gray-500 mt-0.5" />
                <div>
                  <p className="font-medium text-gray-900">Location</p>
                  <p className="text-sm text-gray-600">{event.location}</p>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <DollarSign className="w-5 h-5 text-gray-500 mt-0.5" />
                <div>
                  <p className="font-medium text-gray-900">Price</p>
                  <p className="text-sm text-gray-600">${event.price}</p>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <Users className="w-5 h-5 text-gray-500 mt-0.5" />
                <div>
                  <p className="font-medium text-gray-900">Capacity</p>
                  <p className="text-sm text-gray-600">
                    {event.capacity} participants
                  </p>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <Clock className="w-5 h-5 text-gray-500 mt-0.5" />
                <div>
                  <p className="font-medium text-gray-900">Registration Period</p>
                  <p className="text-sm text-gray-600">
                    {regOpen.toLocaleDateString()} - {regClose.toLocaleDateString()}
                  </p>
                </div>
              </div>

              {event.geolocationRequired && (
                <div className="flex items-start gap-3">
                  <MapPinned className="w-5 h-5 text-gray-500 mt-0.5" />
                  <div>
                    <p className="font-medium text-gray-900">Geolocation</p>
                    <p className="text-sm text-gray-600">Location verification required</p>
                  </div>
                </div>
              )}

              {event.radiusRestrictionKm && event.locationCoords && (
                <div className="flex items-start gap-3">
                  <MapPinned className="w-5 h-5 text-blue-600 mt-0.5" />
                  <div>
                    <p className="font-medium text-gray-900">Location Restriction</p>
                    <p className="text-sm text-gray-600">
                      Only available within {event.radiusRestrictionKm}km of event location
                    </p>
                  </div>
                </div>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Waiting List Info */}
        <Card>
          <CardHeader>
            <CardTitle>Waiting List Information</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="font-medium text-gray-900">Total Entrants</p>
                <p className="text-sm text-gray-600">
                  {event.waitingListCount} people on waiting list
                </p>
              </div>
              <Badge variant="secondary" className="text-lg px-4 py-2">
                {event.waitingListCount}
              </Badge>
            </div>

            {event.waitingListLimit && (
              <div className="flex items-center justify-between">
                <div>
                  <p className="font-medium text-gray-900">Waiting List Limit</p>
                  <p className="text-sm text-gray-600">
                    Maximum {event.waitingListLimit} entrants allowed
                  </p>
                </div>
                <Badge variant="outline" className="text-lg px-4 py-2">
                  {event.waitingListLimit}
                </Badge>
              </div>
            )}

            <Alert>
              <Info className="h-4 w-4" />
              <AlertDescription>
                <strong>Lottery Selection Process:</strong> After registration closes,
                the system will randomly select {event.capacity} participants from the
                waiting list. Selected participants will be notified and must confirm
                their attendance.
              </AlertDescription>
            </Alert>
          </CardContent>
        </Card>

        {/* Organizer Info */}
        <Card>
          <CardHeader>
            <CardTitle>Organizer</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-gray-900 font-medium">{event.organizerName}</p>
          </CardContent>
        </Card>

        {/* Action Buttons */}
        <div className="sticky bottom-0 bg-white border-t border-gray-200 p-4 -mx-4 md:-mx-6">
          <div className="max-w-4xl mx-auto">
            {isJoined ? (
              <Button
                variant="outline"
                className="w-full"
                size="lg"
                onClick={handleLeaveWaitingList}
                disabled={isClosed}
              >
                <XCircle className="w-5 h-5 mr-2" />
                Leave Waiting List
              </Button>
            ) : (
              <Button
                className="w-full"
                size="lg"
                onClick={handleJoinWaitingList}
                disabled={!isOpen || (event.waitingListLimit !== undefined && event.waitingListCount >= event.waitingListLimit)}
              >
                <CheckCircle className="w-5 h-5 mr-2" />
                {isUpcoming
                  ? 'Registration Opens ' + regOpen.toLocaleDateString()
                  : isClosed
                  ? 'Registration Closed'
                  : 'Join Waiting List'}
              </Button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}