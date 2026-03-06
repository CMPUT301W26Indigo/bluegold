import { useState } from 'react';
import { useNavigate } from 'react-router';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/card';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Textarea } from '../ui/textarea';
import { Switch } from '../ui/switch';
import { Badge } from '../ui/badge';
import { Checkbox } from '../ui/checkbox';
import { Calendar, MapPin, DollarSign, Users, Upload, QrCode, Tag } from 'lucide-react';
import { toast } from 'sonner';
import { QRCodeSVG } from 'qrcode.react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '../ui/dialog';

const AVAILABLE_TAGS = [
  'Arts & Crafts',
  'Music',
  'Sports',
  'Fitness',
  'Dance',
  'Wellness',
  'Education',
  'Community',
  'Kids',
  'Adults',
  'Seniors',
  'Beginners',
  'Intermediate',
  'Advanced',
  'All Levels',
  'Outdoor',
  'Indoor',
  'Workshop',
  'Technology',
];

export function CreateEvent() {
  const navigate = useNavigate();
  const [showQR, setShowQR] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    location: '',
    startDate: '',
    endDate: '',
    registrationOpen: '',
    registrationClose: '',
    price: '',
    capacity: '',
    waitingListLimit: '',
    geolocationRequired: false,
    radiusRestrictionKm: '',
    poster: null as File | null,
    tags: [] as string[],
  });

  const toggleTag = (tag: string) => {
    setFormData(prev => ({
      ...prev,
      tags: prev.tags.includes(tag)
        ? prev.tags.filter(t => t !== tag)
        : [...prev.tags, tag]
    }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    // Validation
    if (!formData.name || !formData.description || !formData.location) {
      toast.error('Please fill in all required fields');
      return;
    }

    if (formData.tags.length === 0) {
      toast.error('Please select at least one tag');
      return;
    }

    toast.success('Event created successfully!', {
      description: 'Your event is now live and accepting registrations',
    });

    setShowQR(true);

    // Navigate after showing QR code
    setTimeout(() => {
      navigate('/organizer/events');
    }, 3000);
  };

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setFormData({ ...formData, poster: file });
      toast.success('Poster uploaded successfully');
    }
  };

  return (
    <div className="p-4 md:p-6 max-w-4xl mx-auto">
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">Create New Event</h1>
        <p className="text-gray-600">
          Set up your event and generate a QR code for promotions
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Basic Information */}
        <Card>
          <CardHeader>
            <CardTitle>Basic Information</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="name">Event Name *</Label>
              <Input
                id="name"
                placeholder="e.g., Swimming Lessons for Beginners"
                value={formData.name}
                onChange={(e) =>
                  setFormData({ ...formData, name: e.target.value })
                }
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">Description *</Label>
              <Textarea
                id="description"
                placeholder="Describe your event..."
                value={formData.description}
                onChange={(e) =>
                  setFormData({ ...formData, description: e.target.value })
                }
                rows={4}
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="location">
                <MapPin className="w-4 h-4 inline mr-2" />
                Location *
              </Label>
              <Input
                id="location"
                placeholder="e.g., Community Recreation Centre"
                value={formData.location}
                onChange={(e) =>
                  setFormData({ ...formData, location: e.target.value })
                }
                required
              />
            </div>
          </CardContent>
        </Card>

        {/* Dates */}
        <Card>
          <CardHeader>
            <CardTitle>
              <Calendar className="w-5 h-5 inline mr-2" />
              Event & Registration Dates
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="startDate">Event Start Date *</Label>
                <Input
                  id="startDate"
                  type="date"
                  value={formData.startDate}
                  onChange={(e) =>
                    setFormData({ ...formData, startDate: e.target.value })
                  }
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="endDate">Event End Date *</Label>
                <Input
                  id="endDate"
                  type="date"
                  value={formData.endDate}
                  onChange={(e) =>
                    setFormData({ ...formData, endDate: e.target.value })
                  }
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="registrationOpen">Registration Opens *</Label>
                <Input
                  id="registrationOpen"
                  type="date"
                  value={formData.registrationOpen}
                  onChange={(e) =>
                    setFormData({ ...formData, registrationOpen: e.target.value })
                  }
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="registrationClose">Registration Closes *</Label>
                <Input
                  id="registrationClose"
                  type="date"
                  value={formData.registrationClose}
                  onChange={(e) =>
                    setFormData({ ...formData, registrationClose: e.target.value })
                  }
                  required
                />
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Capacity & Pricing */}
        <Card>
          <CardHeader>
            <CardTitle>Capacity & Pricing</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="capacity">
                  <Users className="w-4 h-4 inline mr-2" />
                  Event Capacity *
                </Label>
                <Input
                  id="capacity"
                  type="number"
                  placeholder="e.g., 20"
                  value={formData.capacity}
                  onChange={(e) =>
                    setFormData({ ...formData, capacity: e.target.value })
                  }
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="waitingListLimit">
                  Waiting List Limit (Optional)
                </Label>
                <Input
                  id="waitingListLimit"
                  type="number"
                  placeholder="e.g., 50"
                  value={formData.waitingListLimit}
                  onChange={(e) =>
                    setFormData({ ...formData, waitingListLimit: e.target.value })
                  }
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="price">
                  <DollarSign className="w-4 h-4 inline mr-2" />
                  Price *
                </Label>
                <Input
                  id="price"
                  type="number"
                  placeholder="e.g., 120"
                  value={formData.price}
                  onChange={(e) =>
                    setFormData({ ...formData, price: e.target.value })
                  }
                  required
                />
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Event Poster */}
        <Card>
          <CardHeader>
            <CardTitle>
              <Upload className="w-5 h-5 inline mr-2" />
              Event Poster (Optional)
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center">
              <Upload className="w-12 h-12 text-gray-400 mx-auto mb-4" />
              <p className="text-gray-600 mb-4">
                Upload an event poster image (JPG, PNG)
              </p>
              <Input
                type="file"
                accept="image/*"
                onChange={handleFileUpload}
                className="max-w-xs mx-auto"
              />
              {formData.poster && (
                <p className="text-green-600 mt-3 text-sm">
                  ✓ {formData.poster.name} uploaded
                </p>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Settings */}
        <Card>
          <CardHeader>
            <CardTitle>Additional Settings</CardTitle>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="flex items-center justify-between">
              <div className="space-y-1">
                <Label htmlFor="geolocation">Geolocation Verification</Label>
                <p className="text-sm text-gray-600">
                  Require entrants to share their location when joining the waiting
                  list
                </p>
              </div>
              <Switch
                id="geolocation"
                checked={formData.geolocationRequired}
                onCheckedChange={(checked) =>
                  setFormData({ ...formData, geolocationRequired: checked })
                }
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="radiusRestriction">
                Location Radius Restriction (Optional)
              </Label>
              <p className="text-sm text-gray-600 mb-2">
                Only allow entrants within a certain distance from the event location to view and register (1-500 km)
              </p>
              <Input
                id="radiusRestriction"
                type="number"
                min="1"
                max="500"
                placeholder="e.g., 50"
                value={formData.radiusRestrictionKm}
                onChange={(e) =>
                  setFormData({ ...formData, radiusRestrictionKm: e.target.value })
                }
              />
              {formData.radiusRestrictionKm && (
                <p className="text-xs text-blue-600 mt-1">
                  Events will only be visible to users within {formData.radiusRestrictionKm} km of the event location
                </p>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Tags */}
        <Card>
          <CardHeader>
            <CardTitle>
              <Tag className="w-5 h-5 inline mr-2" />
              Event Tags *
            </CardTitle>
            <p className="text-sm text-gray-600">Select categories that describe your event</p>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-2 md:grid-cols-3 gap-2">
              {AVAILABLE_TAGS.map(tag => (
                <Badge
                  key={tag}
                  className={`cursor-pointer justify-center py-2 text-xs md:text-sm transition-colors ${
                    formData.tags.includes(tag) 
                      ? 'bg-blue-600 hover:bg-blue-700 text-white' 
                      : 'bg-gray-200 hover:bg-gray-300 text-gray-800'
                  }`}
                  onClick={() => toggleTag(tag)}
                >
                  {tag}
                </Badge>
              ))}
            </div>
            {formData.tags.length > 0 && (
              <p className="text-sm text-green-600 mt-3">
                {formData.tags.length} tag{formData.tags.length !== 1 ? 's' : ''} selected
              </p>
            )}
          </CardContent>
        </Card>

        {/* Actions */}
        <div className="flex gap-4">
          <Button
            type="button"
            variant="outline"
            onClick={() => navigate('/organizer/events')}
            className="flex-1"
          >
            Cancel
          </Button>
          <Button type="submit" className="flex-1">
            <QrCode className="w-4 h-4 mr-2" />
            Create Event & Generate QR Code
          </Button>
        </div>
      </form>

      {/* QR Code Dialog */}
      <Dialog open={showQR} onOpenChange={setShowQR}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Event Created Successfully!</DialogTitle>
            <DialogDescription>
              Your promotional QR code is ready. Share this code to promote your
              event.
            </DialogDescription>
          </DialogHeader>
          <div className="flex justify-center p-6 bg-white">
            <QRCodeSVG
              value={`https://eventlottery.app/event/${Math.random().toString(36).substr(2, 9)}`}
              size={250}
              level="H"
              includeMargin
            />
          </div>
          <p className="text-center text-sm text-gray-600">
            Redirecting to your events...
          </p>
        </DialogContent>
      </Dialog>
    </div>
  );
}