import React, { useEffect, useState } from 'react';
import { Platform } from 'react-native';
import AdModal from './AdModal';

let AdMobService: any = null;
let isAdMobAvailable = false;

// Try to import AdMobService safely
try {
  if (Platform.OS !== 'web') {
    AdMobService = require('@/services/AdMobService').default;
    isAdMobAvailable = AdMobService && AdMobService.isAdMobAvailable && AdMobService.isAdMobAvailable();
  }
} catch (error) {
  console.warn('[AdMobWrapper] AdMobService not available:', error);
  isAdMobAvailable = false;
}

interface AdMobWrapperProps {
  isVisible: boolean;
  onClose: () => void;
  onComplete: (result: { success: boolean; reward?: number; error?: string }) => Promise<void>;
  title?: string;
  description?: string;
  reward?: number;
  onTestEvent?: (event: string, data?: any) => void;
}

const AdMobWrapper: React.FC<AdMobWrapperProps> = (props) => {
  const [isAvailable, setIsAvailable] = useState(isAdMobAvailable);

  useEffect(() => {
    // Check availability when component mounts
    if (Platform.OS !== 'web') {
      setIsAvailable(isAdMobAvailable);
    } else {
      setIsAvailable(false);
    }
  }, []);

  // If AdMob is not available, show a message and close the modal
  if (!isAvailable || Platform.OS === 'web') {
    useEffect(() => {
      if (props.isVisible) {
        console.log('[AdMobWrapper] AdMob not available, closing modal');
        // Show a notification to the user
        // Note: We can't directly show notifications here, so we'll handle it in the parent component
        props.onClose();
      }
    }, [props.isVisible, props.onClose]);

    return null;
  }

  // If AdMob is available, render the AdModal
  return <AdModal {...props} />;
};

export default AdMobWrapper;