import { useState } from 'react';
import { useParams, useNavigate } from 'react-router';
import { mockEvents, mockWaitingList } from '../../data/mockData';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/card';
import { Button } from '../ui/button';
import { Badge } from '../ui/badge';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Progress } from '../ui/progress';
import { toast } from 'sonner';
import { ArrowLeft, TrendingUp, Users, Shuffle, CheckCircle } from 'lucide-react';
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

export function DrawLottery() {
  const { id } = useParams();
  const navigate = useNavigate();
  const event = mockEvents.find((e) => e.id === id);
  const [numToDraw, setNumToDraw] = useState('');
  const [isDrawing, setIsDrawing] = useState(false);
  const [drawnEntrants, setDrawnEntrants] = useState<string[]>([]);

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

  const waitingListEntries = mockWaitingList.filter(
    (e) => e.eventId === id && e.status === 'waiting'
  );
  const availableSpots = event.capacity - event.confirmedCount - event.selectedCount;

  const handleDraw = () => {
    const num = parseInt(numToDraw);
    
    if (!num || num <= 0) {
      toast.error('Please enter a valid number');
      return;
    }

    if (num > availableSpots) {
      toast.error(`Only ${availableSpots} spots available`);
      return;
    }

    if (num > waitingListEntries.length) {
      toast.error(`Only ${waitingListEntries.length} entrants in waiting list`);
      return;
    }

    // Simulate lottery draw animation
    setIsDrawing(true);
    
    setTimeout(() => {
      // Randomly select entrants
      const shuffled = [...waitingListEntries].sort(() => 0.5 - Math.random());
      const selected = shuffled.slice(0, num).map((e) => e.entrantName);
      setDrawnEntrants(selected);
      setIsDrawing(false);
      
      toast.success(`${num} entrants selected!`, {
        description: 'Notifications have been sent to selected participants',
      });
    }, 2000);
  };

  const handleConfirmDraw = () => {
    toast.success('Lottery draw completed successfully!', {
      description: 'Selected entrants have been notified',
    });
    navigate(`/organizer/manage/${id}`);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 sticky top-0 z-10">
        <div className="max-w-4xl mx-auto px-4 py-3 flex items-center gap-4">
          <Button
            variant="ghost"
            size="sm"
            onClick={() => navigate(`/organizer/manage/${id}`)}
          >
            <ArrowLeft className="w-4 h-4 mr-2" />
            Back
          </Button>
          <div className="flex-1">
            <h1 className="font-semibold text-lg">Draw Lottery - {event.name}</h1>
          </div>
        </div>
      </div>

      <div className="max-w-4xl mx-auto p-4 md:p-6 space-y-6">
        {/* Info Cards */}
        <div className="grid md:grid-cols-3 gap-4">
          <Card>
            <CardContent className="pt-6">
              <div className="text-center">
                <Users className="w-8 h-8 text-blue-600 mx-auto mb-2" />
                <p className="text-3xl font-bold text-gray-900">
                  {waitingListEntries.length}
                </p>
                <p className="text-sm text-gray-600">In Waiting List</p>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="pt-6">
              <div className="text-center">
                <TrendingUp className="w-8 h-8 text-green-600 mx-auto mb-2" />
                <p className="text-3xl font-bold text-gray-900">{availableSpots}</p>
                <p className="text-sm text-gray-600">Spots Available</p>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="pt-6">
              <div className="text-center">
                <CheckCircle className="w-8 h-8 text-purple-600 mx-auto mb-2" />
                <p className="text-3xl font-bold text-gray-900">{event.capacity}</p>
                <p className="text-sm text-gray-600">Total Capacity</p>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Lottery Draw Card */}
        <Card>
          <CardHeader>
            <CardTitle>
              <Shuffle className="w-5 h-5 inline mr-2" />
              Conduct Lottery Draw
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="space-y-2">
              <Label htmlFor="numToDraw">Number of Entrants to Select</Label>
              <Input
                id="numToDraw"
                type="number"
                placeholder={`Maximum: ${Math.min(availableSpots, waitingListEntries.length)}`}
                value={numToDraw}
                onChange={(e) => setNumToDraw(e.target.value)}
                disabled={isDrawing || drawnEntrants.length > 0}
                max={Math.min(availableSpots, waitingListEntries.length)}
              />
              <p className="text-sm text-gray-600">
                You can select up to {Math.min(availableSpots, waitingListEntries.length)}{' '}
                entrants
              </p>
            </div>

            {isDrawing && (
              <div className="space-y-3">
                <div className="flex items-center justify-center gap-3 py-6">
                  <Shuffle className="w-8 h-8 text-blue-600 animate-spin" />
                  <p className="text-lg font-medium text-gray-900">
                    Drawing lottery...
                  </p>
                </div>
                <Progress value={50} className="animate-pulse" />
              </div>
            )}

            {drawnEntrants.length === 0 && !isDrawing && (
              <AlertDialog>
                <AlertDialogTrigger asChild>
                  <Button className="w-full" size="lg" disabled={!numToDraw}>
                    <Shuffle className="w-5 h-5 mr-2" />
                    Draw {numToDraw || '0'} Entrants
                  </Button>
                </AlertDialogTrigger>
                <AlertDialogContent>
                  <AlertDialogHeader>
                    <AlertDialogTitle>Confirm Lottery Draw</AlertDialogTitle>
                    <AlertDialogDescription>
                      Are you sure you want to draw {numToDraw} entrants from the
                      waiting list? Selected entrants will be notified immediately.
                    </AlertDialogDescription>
                  </AlertDialogHeader>
                  <AlertDialogFooter>
                    <AlertDialogCancel>Cancel</AlertDialogCancel>
                    <AlertDialogAction onClick={handleDraw}>
                      Confirm Draw
                    </AlertDialogAction>
                  </AlertDialogFooter>
                </AlertDialogContent>
              </AlertDialog>
            )}
          </CardContent>
        </Card>

        {/* Results Card */}
        {drawnEntrants.length > 0 && (
          <Card className="border-green-200 bg-green-50">
            <CardHeader>
              <CardTitle className="text-green-800">
                <CheckCircle className="w-5 h-5 inline mr-2" />
                Lottery Draw Complete
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div>
                <p className="font-medium text-gray-900 mb-3">
                  Selected Entrants ({drawnEntrants.length}):
                </p>
                <div className="space-y-2">
                  {drawnEntrants.map((name, index) => (
                    <div
                      key={index}
                      className="flex items-center gap-3 p-3 bg-white rounded-lg"
                    >
                      <Badge variant="default">{index + 1}</Badge>
                      <p className="font-medium text-gray-900">{name}</p>
                      <CheckCircle className="w-4 h-4 text-green-600 ml-auto" />
                    </div>
                  ))}
                </div>
              </div>

              <div className="bg-white p-4 rounded-lg">
                <p className="text-sm text-gray-700">
                  ✓ Notifications sent to selected entrants
                  <br />
                  ✓ Entrants have been moved to "Selected" status
                  <br />✓ They will receive an invitation to confirm their attendance
                </p>
              </div>

              <Button onClick={handleConfirmDraw} className="w-full" size="lg">
                Complete & View Selected Entrants
              </Button>
            </CardContent>
          </Card>
        )}

        {/* Lottery Information */}
        <Card>
          <CardHeader>
            <CardTitle>Lottery Process Information</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="flex gap-3">
              <span className="bg-blue-100 text-blue-600 rounded-full w-6 h-6 flex items-center justify-center flex-shrink-0 font-semibold text-sm">
                1
              </span>
              <p className="text-gray-700">
                The system will randomly select the specified number of entrants from
                the waiting list
              </p>
            </div>
            <div className="flex gap-3">
              <span className="bg-blue-100 text-blue-600 rounded-full w-6 h-6 flex items-center justify-center flex-shrink-0 font-semibold text-sm">
                2
              </span>
              <p className="text-gray-700">
                Selected entrants will receive a notification and must confirm their
                attendance
              </p>
            </div>
            <div className="flex gap-3">
              <span className="bg-blue-100 text-blue-600 rounded-full w-6 h-6 flex items-center justify-center flex-shrink-0 font-semibold text-sm">
                3
              </span>
              <p className="text-gray-700">
                If someone declines, you can draw a replacement from the remaining
                waiting list
              </p>
            </div>
            <div className="flex gap-3">
              <span className="bg-blue-100 text-blue-600 rounded-full w-6 h-6 flex items-center justify-center flex-shrink-0 font-semibold text-sm">
                4
              </span>
              <p className="text-gray-700">
                All draws are logged and can be reviewed for transparency
              </p>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
