import { Link } from 'react-router';
import { Calendar, Users, Shield, QrCode } from 'lucide-react';
import { Card, CardContent } from './ui/card';
import { Button } from './ui/button';

export function Home() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
      <div className="max-w-4xl w-full">
        <div className="text-center mb-6 md:mb-8">
          <div className="flex justify-center mb-4">
            <div className="bg-white rounded-full p-4 shadow-lg">
              <Calendar className="w-10 h-10 md:w-12 md:h-12 text-blue-600" />
            </div>
          </div>
          <h1 className="text-3xl md:text-4xl font-bold text-gray-900 mb-2">
            Event Lottery System
          </h1>
          <p className="text-base md:text-lg text-gray-600">
            Fair and accessible event registration for everyone
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 md:gap-6 mb-6 md:mb-8">
          <Card className="hover:shadow-lg transition-shadow">
            <CardContent className="p-4 md:p-6">
              <Link to="/entrant/events" className="block">
                <div className="bg-blue-100 rounded-full w-12 h-12 flex items-center justify-center mb-4">
                  <Users className="w-6 h-6 text-blue-600" />
                </div>
                <h2 className="text-lg md:text-xl font-semibold mb-2">Entrant</h2>
                <p className="text-sm md:text-base text-gray-600 mb-4">
                  Browse events, join waiting lists, and manage your registrations
                </p>
                <Button className="w-full">Enter as Entrant</Button>
              </Link>
            </CardContent>
          </Card>

          <Card className="hover:shadow-lg transition-shadow">
            <CardContent className="p-4 md:p-6">
              <Link to="/organizer/events" className="block">
                <div className="bg-green-100 rounded-full w-12 h-12 flex items-center justify-center mb-4">
                  <QrCode className="w-6 h-6 text-green-600" />
                </div>
                <h2 className="text-lg md:text-xl font-semibold mb-2">Organizer</h2>
                <p className="text-sm md:text-base text-gray-600 mb-4">
                  Create events, manage waiting lists, and conduct lottery draws
                </p>
                <Button className="w-full bg-green-600 hover:bg-green-700">
                  Enter as Organizer
                </Button>
              </Link>
            </CardContent>
          </Card>

          <Card className="hover:shadow-lg transition-shadow">
            <CardContent className="p-4 md:p-6">
              <Link to="/admin" className="block">
                <div className="bg-purple-100 rounded-full w-12 h-12 flex items-center justify-center mb-4">
                  <Shield className="w-6 h-6 text-purple-600" />
                </div>
                <h2 className="text-lg md:text-xl font-semibold mb-2">Administrator</h2>
                <p className="text-sm md:text-base text-gray-600 mb-4">
                  Manage events, profiles, and monitor system activity
                </p>
                <Button className="w-full bg-purple-600 hover:bg-purple-700">
                  Enter as Admin
                </Button>
              </Link>
            </CardContent>
          </Card>
        </div>

        <Card>
          <CardContent className="p-4 md:p-6">
            <h3 className="text-base md:text-lg font-semibold mb-3">How It Works</h3>
            <div className="space-y-3 text-sm md:text-base text-gray-600">
              <div className="flex gap-3">
                <span className="bg-blue-100 text-blue-600 rounded-full w-6 h-6 flex items-center justify-center flex-shrink-0 font-semibold text-sm">
                  1
                </span>
                <p>
                  <strong>Join the Waiting List:</strong> Browse events and join waiting lists during the registration period
                </p>
              </div>
              <div className="flex gap-3">
                <span className="bg-blue-100 text-blue-600 rounded-full w-6 h-6 flex items-center justify-center flex-shrink-0 font-semibold text-sm">
                  2
                </span>
                <p>
                  <strong>Lottery Draw:</strong> After registration closes, the system randomly selects participants
                </p>
              </div>
              <div className="flex gap-3">
                <span className="bg-blue-100 text-blue-600 rounded-full w-6 h-6 flex items-center justify-center flex-shrink-0 font-semibold text-sm">
                  3
                </span>
                <p>
                  <strong>Get Notified:</strong> Selected participants receive notifications and can accept or decline
                </p>
              </div>
              <div className="flex gap-3">
                <span className="bg-blue-100 text-blue-600 rounded-full w-6 h-6 flex items-center justify-center flex-shrink-0 font-semibold text-sm">
                  4
                </span>
                <p>
                  <strong>Second Chances:</strong> If someone declines, another entrant is drawn from the waiting list
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}