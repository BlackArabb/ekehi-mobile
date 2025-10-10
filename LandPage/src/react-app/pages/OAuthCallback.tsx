import { useEffect, useState } from 'react';
import { useLocation } from 'react-router';
import { Loader2, CheckCircle, AlertCircle } from 'lucide-react';

export default function OAuthCallback() {
  const location = useLocation();
  const [status, setStatus] = useState<'processing' | 'success' | 'error'>('processing');
  const [message, setMessage] = useState('Processing authentication...');

  useEffect(() => {
    // Parse URL parameters from OAuth callback
    const searchParams = new URLSearchParams(location.search);
    const error = searchParams.get('error');
    
    // Check if there was an error in the OAuth flow
    if (error) {
      setStatus('error');
      setMessage(`Authentication failed: ${error}`);
      return;
    }
    
    // Attempt to redirect to the mobile app with all OAuth parameters
    attemptAppRedirect();
  }, [location]);

  const attemptAppRedirect = () => {
    try {
      setStatus('processing');
      setMessage('Redirecting to Ekehi mobile app...');
      
      // Construct the deep link URL for the mobile app
      // Include all query parameters from the OAuth callback
      let deepLink = 'ekehi://oauth/return';
      if (location.search) {
        deepLink += location.search;
      }
      
      // Try to open the mobile app
      window.location.href = deepLink;
      
      // If we're still here after a short delay, show success message
      setTimeout(() => {
        if (status === 'processing') {
          setStatus('success');
          setMessage(
            'Authentication successful!\n\n' +
            'If the Ekehi app did not open automatically:\n' +
            '1. Open the Ekehi mobile app manually\n' +
            '2. The authentication should complete automatically\n\n' +
            'If you continue to experience issues, please contact support.'
          );
        }
      }, 2000);
    } catch (err) {
      setStatus('error');
      setMessage('Failed to redirect to mobile app. Please open the Ekehi app manually.');
      console.error('Redirect error:', err);
    }
  };

  return (
    <div className="min-h-screen bg-black text-white flex items-center justify-center p-4">
      <div className="max-w-md w-full bg-charcoal-gray rounded-2xl p-8 shadow-xl border border-ekehi-gold/20">
        <div className="text-center">
          {/* Icon based on status */}
          {status === 'processing' && (
            <div className="flex justify-center mb-6">
              <Loader2 className="h-16 w-16 text-ekehi-gold animate-spin" />
            </div>
          )}
          
          {status === 'success' && (
            <div className="flex justify-center mb-6">
              <CheckCircle className="h-16 w-16 text-green-500" />
            </div>
          )}
          
          {status === 'error' && (
            <div className="flex justify-center mb-6">
              <AlertCircle className="h-16 w-16 text-red-500" />
            </div>
          )}
          
          {/* Title */}
          <h1 className="text-2xl font-bold mb-4">
            {status === 'processing' && 'Processing Authentication'}
            {status === 'success' && 'Authentication Successful'}
            {status === 'error' && 'Authentication Failed'}
          </h1>
          
          {/* Message */}
          <p className="text-medium-gray mb-8 whitespace-pre-line">
            {message}
          </p>
          
          {/* Additional Info */}
          <div className="text-center text-sm text-medium-gray">
            <p>
              This page is part of the Ekehi mobile app authentication flow.
              Please use the Ekehi mobile app to complete the sign-in process.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}