import { Alert } from 'react-native';

// Define error types
export type AppError = {
  code?: number;
  message: string;
  type: 'network' | 'auth' | 'validation' | 'server' | 'unknown';
  userMessage?: string;
  retryable?: boolean;
};

// Error handling service
class ErrorService {
  // Handle Appwrite errors
  handleAppwriteError(error: any): AppError {
    const appError: AppError = {
      code: error?.code,
      message: error?.message || 'Unknown error occurred',
      type: 'unknown',
      retryable: false,
    };

    // Determine error type based on code or message
    if (error?.code) {
      switch (error.code) {
        case 400:
          appError.type = 'validation';
          appError.userMessage = 'Invalid input. Please check your data and try again.';
          appError.retryable = false;
          break;
        case 401:
          appError.type = 'auth';
          appError.userMessage = 'Authentication failed. Please sign in again.';
          appError.retryable = false;
          break;
        case 403:
          appError.type = 'auth';
          appError.userMessage = 'Access denied. You do not have permission to perform this action.';
          appError.retryable = false;
          break;
        case 404:
          appError.type = 'server';
          appError.userMessage = 'Resource not found. Please try again later.';
          appError.retryable = true;
          break;
        case 409:
          appError.type = 'validation';
          appError.userMessage = 'Data conflict. This item may already exist.';
          appError.retryable = false;
          break;
        case 500:
        case 502:
        case 503:
          appError.type = 'server';
          appError.userMessage = 'Server error. Please try again later.';
          appError.retryable = true;
          break;
        default:
          appError.type = 'unknown';
          appError.userMessage = 'An unexpected error occurred. Please try again.';
          appError.retryable = true;
      }
    } else if (error?.message) {
      // Handle errors based on message content
      if (error.message.includes('Network')) {
        appError.type = 'network';
        appError.userMessage = 'Network error. Please check your internet connection and try again.';
        appError.retryable = true;
      } else if (error.message.includes('timeout')) {
        appError.type = 'network';
        appError.userMessage = 'Request timed out. Please check your internet connection and try again.';
        appError.retryable = true;
      } else if (error.message.includes('Unauthorized')) {
        appError.type = 'auth';
        appError.userMessage = 'Authentication failed. Please sign in again.';
        appError.retryable = false;
      } else {
        appError.type = 'unknown';
        appError.userMessage = 'An unexpected error occurred. Please try again.';
        appError.retryable = true;
      }
    }

    return appError;
  }

  // Show error notification
  showErrorNotification(error: AppError, title?: string) {
    // In a real implementation, this would use the notification context
    // For now, we'll use Alert as a fallback
    Alert.alert(
      title || 'Error',
      error.userMessage || error.message || 'An unexpected error occurred'
    );
  }

  // Log error for debugging
  logError(error: AppError, context: string) {
    console.error(`[ErrorService] ${context}:`, {
      code: error.code,
      message: error.message,
      type: error.type,
      userMessage: error.userMessage,
      retryable: error.retryable,
    });
  }

  // Check if error is retryable
  isRetryable(error: AppError): boolean {
    return error.retryable === true;
  }

  // Get user-friendly error message
  getUserMessage(error: AppError): string {
    return error.userMessage || error.message || 'An unexpected error occurred';
  }
}

// Export singleton instance
export default new ErrorService();