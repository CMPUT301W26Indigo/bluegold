import { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router';
import { mockEvents, mockWaitingList } from '../../data/mockData';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/card';
import { Button } from '../ui/button';
import { Badge } from '../ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../ui/tabs';
import { Input } from '../ui/input';
import { Textarea } from '../ui/textarea';
import { Label } from '../ui/label';
import { toast } from 'sonner';
import {
  ArrowLeft,
  Send,
  Download,
  MapPin,
  Users,
  CheckCircle,
  XCircle,
  Clock,
  Trash2,
} from 'lucide-react';

export function ManageEvent() {
  const { id } = useParams();
  const navigate = useNavigate();
  const event = mockEvents.find((e) => e.id === id);
  const [activeTab, setActiveTab] = useState('overview');
  const [notificationMessage, setNotificationMessage] = useState('');

  if (!event) {
    return (
      <div className="p-6 max-w-2xl mx-auto text-center">
        <h2 className="text-2xl font-bold mb-4">Event Not Found</h2>
        <Button onClick={() => navigate('/organizer/events')}>
          Back to Events
        </Button>
      </div>
    );
  }

  const waitingListEntries = mockWaitingList.filter((e) => e.eventId === id);
  const selectedEntries = waitingListEntries.filter((e) => e.status === 'selected');
  const confirmedEntries = waitingListEntries.filter((e) => e.status === 'confirmed');
  const cancelledEntries = waitingListEntries.filter((e) => e.status === 'cancelled');

  const handleSendNotification = (target: string) => {
    if (!notificationMessage.trim()) {
      toast.error('Please enter a message');
      return;
    }

    toast.success(`Notification sent to ${target}`, {
      description: `${notificationMessage.substring(0, 50)}...`,
    });
    setNotificationMessage('');
  };

  const handleExportCSV = () => {
    toast.success('CSV exported successfully', {
      description: 'Entrant list has been downloaded',
    });
  };

  const handleCancelEntrant = (entrantId: string) => {
    toast.info('Entrant cancelled', {
      description: 'A replacement will be drawn from the waiting list',
    });
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 sticky top-0 z-10">
        <div className="max-w-6xl mx-auto px-4 py-3 flex items-center gap-4">
          <Button
            variant="ghost"
            size="sm"
            onClick={() => navigate('/organizer/events')}
          >
            <ArrowLeft className="w-4 h-4 mr-2" />
            Back
          </Button>
          <div className="flex-1">
            <h1 className="font-semibold text-lg">{event.name}</h1>
          </div>
          <Link to={`/organizer/draw/${event.id}`}>
            <Button>Draw Lottery</Button>
          </Link>
        </div>
      </div>

      <div className="max-w-6xl mx-auto p-4 md:p-6">
        <Tabs value={activeTab} onValueChange={setActiveTab}>
          <TabsList className="mb-6">
            <TabsTrigger value="overview">Overview</TabsTrigger>
            <TabsTrigger value="waiting">
              Waiting List ({waitingListEntries.length})
            </TabsTrigger>
            <TabsTrigger value="selected">
              Selected ({selectedEntries.length})
            </TabsTrigger>
            <TabsTrigger value="confirmed">
              Confirmed ({confirmedEntries.length})
            </TabsTrigger>
            <TabsTrigger value="notifications">Notifications</TabsTrigger>
          </TabsList>

          {/* Overview Tab */}
          <TabsContent value="overview" className="space-y-6">
            <div className="grid md:grid-cols-3 gap-6">
              <Card>
                <CardContent className="pt-6">
                  <div className="text-center">
                    <Users className="w-8 h-8 text-blue-600 mx-auto mb-2" />
                    <p className="text-3xl font-bold text-gray-900">
                      {event.waitingListCount}
                    </p>
                    <p className="text-sm text-gray-600">On Waiting List</p>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardContent className="pt-6">
                  <div className="text-center">
                    <CheckCircle className="w-8 h-8 text-green-600 mx-auto mb-2" />
                    <p className="text-3xl font-bold text-gray-900">
                      {event.confirmedCount}
                    </p>
                    <p className="text-sm text-gray-600">Confirmed</p>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardContent className="pt-6">
                  <div className="text-center">
                    <Users className="w-8 h-8 text-purple-600 mx-auto mb-2" />
                    <p className="text-3xl font-bold text-gray-900">
                      {event.capacity}
                    </p>
                    <p className="text-sm text-gray-600">Total Capacity</p>
                  </div>
                </CardContent>
              </Card>
            </div>

            {/* Geolocation Map */}
            {event.geolocationRequired && (
              <Card>
                <CardHeader>
                  <CardTitle>
                    <MapPin className="w-5 h-5 inline mr-2" />
                    Entrant Locations
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3">
                    {waitingListEntries
                      .filter((e) => e.location)
                      .map((entry) => (
                        <div
                          key={entry.entrantId}
                          className="flex items-start gap-3 p-3 bg-gray-50 rounded-lg"
                        >
                          <MapPin className="w-5 h-5 text-blue-600 mt-0.5" />
                          <div className="flex-1">
                            <p className="font-medium text-gray-900">
                              {entry.entrantName}
                            </p>
                            <p className="text-sm text-gray-600">
                              {entry.location?.address}
                            </p>
                            <p className="text-xs text-gray-500 mt-1">
                              Coordinates: {entry.location?.lat.toFixed(4)},{' '}
                              {entry.location?.lng.toFixed(4)}
                            </p>
                          </div>
                        </div>
                      ))}
                    {waitingListEntries.filter((e) => e.location).length === 0 && (
                      <p className="text-center text-gray-600 py-8">
                        No location data available yet
                      </p>
                    )}
                  </div>
                </CardContent>
              </Card>
            )}
          </TabsContent>

          {/* Waiting List Tab */}
          <TabsContent value="waiting" className="space-y-4">
            <Card>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <CardTitle>Waiting List Entrants</CardTitle>
                  <Badge variant="secondary">
                    {waitingListEntries.length} entrants
                  </Badge>
                </div>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  {waitingListEntries.map((entry) => (
                    <div
                      key={entry.entrantId}
                      className="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
                    >
                      <div className="flex-1">
                        <p className="font-medium text-gray-900">
                          {entry.entrantName}
                        </p>
                        <p className="text-sm text-gray-600">
                          Joined {new Date(entry.joinedAt).toLocaleString()}
                        </p>
                        {entry.location && (
                          <p className="text-xs text-gray-500 flex items-center gap-1 mt-1">
                            <MapPin className="w-3 h-3" />
                            {entry.location.address}
                          </p>
                        )}
                      </div>
                      <Badge variant="outline">
                        <Clock className="w-3 h-3 mr-1" />
                        {entry.status}
                      </Badge>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          {/* Selected Tab */}
          <TabsContent value="selected" className="space-y-4">
            <Card>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <CardTitle>Selected Entrants</CardTitle>
                  <Button onClick={handleExportCSV} variant="outline" size="sm">
                    <Download className="w-4 h-4 mr-2" />
                    Export CSV
                  </Button>
                </div>
              </CardHeader>
              <CardContent>
                {selectedEntries.length > 0 ? (
                  <div className="space-y-3">
                    {selectedEntries.map((entry) => (
                      <div
                        key={entry.entrantId}
                        className="flex items-center justify-between p-3 bg-green-50 rounded-lg"
                      >
                        <div className="flex-1">
                          <p className="font-medium text-gray-900">
                            {entry.entrantName}
                          </p>
                          <p className="text-sm text-gray-600">
                            Selected - Awaiting response
                          </p>
                        </div>
                        <div className="flex gap-2">
                          <Badge className="bg-green-600">Selected</Badge>
                          <Button
                            size="sm"
                            variant="ghost"
                            onClick={() => handleCancelEntrant(entry.entrantId)}
                          >
                            <Trash2 className="w-4 h-4 text-red-600" />
                          </Button>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-center text-gray-600 py-8">
                    No entrants selected yet. Run a lottery draw to select
                    participants.
                  </p>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          {/* Confirmed Tab */}
          <TabsContent value="confirmed" className="space-y-4">
            <Card>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <CardTitle>Confirmed Entrants</CardTitle>
                  <Button onClick={handleExportCSV} variant="outline" size="sm">
                    <Download className="w-4 h-4 mr-2" />
                    Export CSV
                  </Button>
                </div>
              </CardHeader>
              <CardContent>
                {confirmedEntries.length > 0 ? (
                  <div className="space-y-3">
                    {confirmedEntries.map((entry) => (
                      <div
                        key={entry.entrantId}
                        className="flex items-center justify-between p-3 bg-blue-50 rounded-lg"
                      >
                        <div className="flex-1">
                          <p className="font-medium text-gray-900">
                            {entry.entrantName}
                          </p>
                          <p className="text-sm text-gray-600">Confirmed attendance</p>
                        </div>
                        <div className="flex gap-2">
                          <Badge className="bg-blue-600">Confirmed</Badge>
                          <Button
                            size="sm"
                            variant="ghost"
                            onClick={() => handleCancelEntrant(entry.entrantId)}
                          >
                            <Trash2 className="w-4 h-4 text-red-600" />
                          </Button>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-center text-gray-600 py-8">
                    No confirmed entrants yet
                  </p>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          {/* Notifications Tab */}
          <TabsContent value="notifications" className="space-y-4">
            <Card>
              <CardHeader>
                <CardTitle>
                  <Send className="w-5 h-5 inline mr-2" />
                  Send Notifications
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="message">Notification Message</Label>
                  <Textarea
                    id="message"
                    placeholder="Enter your message..."
                    value={notificationMessage}
                    onChange={(e) => setNotificationMessage(e.target.value)}
                    rows={4}
                  />
                </div>

                <div className="grid md:grid-cols-3 gap-3">
                  <Button
                    onClick={() => handleSendNotification('all waiting list entrants')}
                    variant="outline"
                    className="w-full"
                  >
                    <Users className="w-4 h-4 mr-2" />
                    All Waiting List
                  </Button>
                  <Button
                    onClick={() => handleSendNotification('all selected entrants')}
                    variant="outline"
                    className="w-full"
                  >
                    <CheckCircle className="w-4 h-4 mr-2" />
                    All Selected
                  </Button>
                  <Button
                    onClick={() => handleSendNotification('all cancelled entrants')}
                    variant="outline"
                    className="w-full"
                  >
                    <XCircle className="w-4 h-4 mr-2" />
                    All Cancelled
                  </Button>
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}