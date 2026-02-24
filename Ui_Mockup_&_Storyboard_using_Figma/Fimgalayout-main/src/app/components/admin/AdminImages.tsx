import { useState } from 'react';
import { mockEvents } from '../../data/mockData';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/card';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Badge } from '../ui/badge';
import { Search, Trash2, Eye, Image as ImageIcon, AlertTriangle, Filter, Flag } from 'lucide-react';
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
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '../ui/dialog';
import { Alert, AlertDescription } from '../ui/alert';

interface EventImage {
  id: string;
  url: string;
  eventName: string;
  organizerName: string;
  uploadedAt: string;
  flagged: boolean;
  flagReason?: string;
}

export function AdminImages() {
  const [searchTerm, setSearchTerm] = useState('');
  const [flagFilter, setFlagFilter] = useState<'all' | 'flagged' | 'normal'>('all');

  // Create mock images from events with posters
  const initialImages: EventImage[] = mockEvents
    .filter((e) => e.posterUrl)
    .map((e, index) => ({
      id: e.id,
      url: e.posterUrl!,
      eventName: e.name,
      organizerName: e.organizerName,
      uploadedAt: new Date(e.registrationOpen).toISOString(),
      flagged: index < 2, // Flag first 2 images for demo
      flagReason: index === 0 ? 'Inappropriate content detected' : index === 1 ? 'Copyright violation suspected' : undefined,
    }));

  const [images, setImages] = useState<EventImage[]>(initialImages);

  const filteredImages = images
    .filter(
      (img) =>
        (img.eventName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        img.organizerName.toLowerCase().includes(searchTerm.toLowerCase())) &&
        (flagFilter === 'all' || 
         (flagFilter === 'flagged' && img.flagged) ||
         (flagFilter === 'normal' && !img.flagged))
    )
    // Sort flagged images first
    .sort((a, b) => {
      if (a.flagged && !b.flagged) return -1;
      if (!a.flagged && b.flagged) return 1;
      return 0;
    });

  const handleRemoveImage = (imageId: string, eventName: string) => {
    setImages(images.filter((img) => img.id !== imageId));
    toast.success('Image removed', {
      description: `Poster for "${eventName}" has been removed`,
    });
  };

  const handleUnflag = (imageId: string) => {
    setImages(
      images.map((img) => (img.id === imageId ? { ...img, flagged: false, flagReason: undefined } : img))
    );
    toast.success('Image approved');
  };

  const flaggedCount = images.filter((img) => img.flagged).length;

  return (
    <div className="p-4 md:p-6 max-w-6xl mx-auto">
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">Manage Images</h1>
        <p className="text-gray-600">
          Review and remove event poster images that violate policies
        </p>
      </div>

      {/* Stats */}
      <div className="grid md:grid-cols-3 gap-4 mb-6">
        <Card>
          <CardContent className="pt-6">
            <div className="text-center">
              <ImageIcon className="w-8 h-8 text-blue-600 mx-auto mb-2" />
              <p className="text-3xl font-bold text-gray-900">{images.length}</p>
              <p className="text-sm text-gray-600">Total Images</p>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="pt-6">
            <div className="text-center">
              <AlertTriangle className="w-8 h-8 text-orange-600 mx-auto mb-2" />
              <p className="text-3xl font-bold text-gray-900">{flaggedCount}</p>
              <p className="text-sm text-gray-600">Flagged for Review</p>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="pt-6">
            <div className="text-center">
              <Eye className="w-8 h-8 text-green-600 mx-auto mb-2" />
              <p className="text-3xl font-bold text-gray-900">
                {images.length - flaggedCount}
              </p>
              <p className="text-sm text-gray-600">Approved</p>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Search and Filter */}
      <div className="mb-6 flex gap-4">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
          <Input
            type="text"
            placeholder="Search by event or organizer..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pl-10"
          />
        </div>
        <Select onValueChange={setFlagFilter} value={flagFilter}>
          <SelectTrigger className="w-40">
            <SelectValue placeholder="Filter by flag status">
              {flagFilter === 'all' ? 'All' : flagFilter === 'flagged' ? 'Flagged' : 'Normal'}
            </SelectValue>
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All</SelectItem>
            <SelectItem value="flagged">Flagged</SelectItem>
            <SelectItem value="normal">Normal</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {/* Images Grid */}
      <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
        {filteredImages.map((image) => (
          <Card key={image.id} className={image.flagged ? 'border-orange-200' : ''}>
            <CardHeader className="p-0">
              <div className="relative">
                <img
                  src={image.url}
                  alt={image.eventName}
                  className="w-full h-48 object-cover rounded-t-lg"
                />
                {image.flagged && (
                  <Badge
                    variant="destructive"
                    className="absolute top-2 right-2 bg-orange-600"
                  >
                    <AlertTriangle className="w-3 h-3 mr-1" />
                    Flagged
                  </Badge>
                )}
              </div>
            </CardHeader>
            <CardContent className="pt-4">
              <h3 className="font-semibold text-gray-900 mb-1">{image.eventName}</h3>
              <p className="text-sm text-gray-600 mb-2">{image.organizerName}</p>
              <p className="text-xs text-gray-500 mb-3">
                Uploaded {new Date(image.uploadedAt).toLocaleDateString()}
              </p>

              <div className="flex gap-2">
                <Dialog>
                  <DialogTrigger asChild>
                    <Button variant="outline" size="sm" className="flex-1">
                      <Eye className="w-4 h-4 mr-2" />
                      View
                    </Button>
                  </DialogTrigger>
                  <DialogContent className="max-w-3xl">
                    <DialogHeader>
                      <DialogTitle>{image.eventName}</DialogTitle>
                    </DialogHeader>
                    <div className="mt-4">
                      <img
                        src={image.url}
                        alt={image.eventName}
                        className="w-full rounded-lg"
                      />
                      <div className="mt-4 space-y-2">
                        <p className="text-sm">
                          <strong>Organizer:</strong> {image.organizerName}
                        </p>
                        <p className="text-sm">
                          <strong>Uploaded:</strong>{' '}
                          {new Date(image.uploadedAt).toLocaleString()}
                        </p>
                        {image.flagged && (
                          <Badge variant="destructive" className="bg-orange-600">
                            <AlertTriangle className="w-3 h-3 mr-1" />
                            Flagged for Review
                          </Badge>
                        )}
                      </div>
                    </div>
                  </DialogContent>
                </Dialog>

                {image.flagged ? (
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handleUnflag(image.id)}
                  >
                    Approve
                  </Button>
                ) : null}

                <AlertDialog>
                  <AlertDialogTrigger asChild>
                    <Button variant="destructive" size="sm">
                      <Trash2 className="w-4 h-4" />
                    </Button>
                  </AlertDialogTrigger>
                  <AlertDialogContent>
                    <AlertDialogHeader>
                      <AlertDialogTitle>Remove Image</AlertDialogTitle>
                      <AlertDialogDescription>
                        Are you sure you want to remove this image for "
                        {image.eventName}"? The event will remain active but without a
                        poster.
                      </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                      <AlertDialogCancel>Cancel</AlertDialogCancel>
                      <AlertDialogAction
                        onClick={() => handleRemoveImage(image.id, image.eventName)}
                        className="bg-red-600 hover:bg-red-700"
                      >
                        Remove Image
                      </AlertDialogAction>
                    </AlertDialogFooter>
                  </AlertDialogContent>
                </AlertDialog>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {filteredImages.length === 0 && (
        <Card>
          <CardContent className="py-12 text-center">
            <ImageIcon className="w-12 h-12 text-gray-300 mx-auto mb-4" />
            <h3 className="text-lg font-semibold text-gray-900 mb-2">No images found</h3>
            <p className="text-gray-600">Try adjusting your search</p>
          </CardContent>
        </Card>
      )}
    </div>
  );
}