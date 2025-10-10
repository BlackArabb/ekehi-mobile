import { useEffect } from 'react';
import { useLocation } from 'react-router';

export default function OAuthRedirect() {
  const location = useLocation();

  useEffect(() => {
    // Construct the deep link URL for the mobile app
    // Include all query parameters from the OAuth callback
    let deepLink = 'ekehi://oauth/return';
    if (location.search) {
      deepLink += location.search;
    }
    
    // Redirect to the mobile app
    window.location.href = deepLink;
  }, [location]);

  return (
    <div className="min-h-screen bg-black text-white flex items-center justify-center">
      <div className="text-center">
        <div className="mb-4">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-ekehi-gold"></div>
        </div>
        <h1 className="text-2xl font-bold mb-2">Redirecting...</h1>
        <p className="text-medium-gray">
          Redirecting to Ekehi mobile app
        </p>
      </div>
    </div>
  );
}