// Retry utility for network operations
export interface RetryOptions {
  maxRetries?: number;
  delay?: number;
  exponentialBackoff?: boolean;
  maxDelay?: number;
  shouldRetry?: (error: any) => boolean;
}

export interface RetryResult<T> {
  success: boolean;
  data?: T;
  error?: any;
  attempts: number;
}

/**
 * Retry a function with exponential backoff
 * @param fn Function to retry
 * @param options Retry options
 * @returns Promise with retry result
 */
export async function retryWithBackoff<T>(
  fn: () => Promise<T>,
  options: RetryOptions = {}
): Promise<RetryResult<T>> {
  const {
    maxRetries = 3,
    delay = 1000,
    exponentialBackoff = true,
    maxDelay = 10000,
    shouldRetry = () => true
  } = options;

  let lastError: any;
  let attempts = 0;

  for (let i = 0; i <= maxRetries; i++) {
    attempts = i + 1;
    
    try {
      const result = await fn();
      return {
        success: true,
        data: result,
        attempts
      };
    } catch (error) {
      lastError = error;
      
      // If this is the last attempt or shouldRetry returns false, don't retry
      if (i === maxRetries || !shouldRetry(error)) {
        break;
      }
      
      // Calculate delay
      let currentDelay = delay;
      if (exponentialBackoff) {
        currentDelay = Math.min(delay * Math.pow(2, i), maxDelay);
      }
      
      // Add jitter to prevent thundering herd
      const jitter = Math.random() * 0.5 * currentDelay;
      currentDelay += jitter;
      
      // Wait before retrying
      await new Promise(resolve => setTimeout(resolve, currentDelay));
    }
  }

  return {
    success: false,
    error: lastError,
    attempts
  };
}

/**
 * Default retry condition for network errors
 * @param error Error object
 * @returns boolean indicating if operation should be retried
 */
export function isNetworkError(error: any): boolean {
  if (!error) return false;
  
  // Check for network error indicators
  if (error.message && (
    error.message.includes('Network') ||
    error.message.includes('timeout') ||
    error.message.includes('Failed to fetch') ||
    error.message.includes('ECONNREFUSED') ||
    error.message.includes('ENOTFOUND')
  )) {
    return true;
  }
  
  // Check for HTTP status codes that should be retried
  if (error.code && [502, 503, 504].includes(error.code)) {
    return true;
  }
  
  // For Appwrite errors
  if (error.type === 'server' || error.type === 'network') {
    return true;
  }
  
  return false;
}

/**
 * Enhanced retry with progress callback
 * @param fn Function to retry
 * @param options Retry options
 * @param onProgress Progress callback
 * @returns Promise with retry result
 */
export async function retryWithProgress<T>(
  fn: () => Promise<T>,
  options: RetryOptions = {},
  onProgress?: (attempt: number, maxRetries: number, error?: any) => void
): Promise<RetryResult<T>> {
  const {
    maxRetries = 3,
    delay = 1000,
    exponentialBackoff = true,
    maxDelay = 10000,
    shouldRetry = () => true
  } = options;

  let lastError: any;
  let attempts = 0;

  for (let i = 0; i <= maxRetries; i++) {
    attempts = i + 1;
    
    // Report progress
    if (onProgress) {
      onProgress(i, maxRetries, lastError);
    }
    
    try {
      const result = await fn();
      return {
        success: true,
        data: result,
        attempts
      };
    } catch (error) {
      lastError = error;
      
      // If this is the last attempt or shouldRetry returns false, don't retry
      if (i === maxRetries || !shouldRetry(error)) {
        break;
      }
      
      // Calculate delay
      let currentDelay = delay;
      if (exponentialBackoff) {
        currentDelay = Math.min(delay * Math.pow(2, i), maxDelay);
      }
      
      // Add jitter to prevent thundering herd
      const jitter = Math.random() * 0.5 * currentDelay;
      currentDelay += jitter;
      
      // Wait before retrying
      await new Promise(resolve => setTimeout(resolve, currentDelay));
    }
  }

  return {
    success: false,
    error: lastError,
    attempts
  };
}