// Logging service for debugging and monitoring
export type LogLevel = 'debug' | 'info' | 'warn' | 'error';

export interface LogEntry {
  timestamp: Date;
  level: LogLevel;
  message: string;
  context?: string;
  data?: any;
  stack?: string;
}

class LoggingService {
  private logs: LogEntry[] = [];
  private maxLogs = 1000;
  private enabled = true;

  // Enable or disable logging
  setEnabled(enabled: boolean): void {
    this.enabled = enabled;
  }

  // Set maximum number of logs to keep in memory
  setMaxLogs(maxLogs: number): void {
    this.maxLogs = maxLogs;
  }

  // Log a debug message
  debug(message: string, context?: string, data?: any): void {
    this.log('debug', message, context, data);
  }

  // Log an info message
  info(message: string, context?: string, data?: any): void {
    this.log('info', message, context, data);
  }

  // Log a warning message
  warn(message: string, context?: string, data?: any): void {
    this.log('warn', message, context, data);
  }

  // Log an error message
  error(message: string, context?: string, data?: any, error?: Error): void {
    const stack = error ? error.stack : undefined;
    this.log('error', message, context, data, stack);
  }

  // Generic log method
  private log(level: LogLevel, message: string, context?: string, data?: any, stack?: string): void {
    if (!this.enabled) return;

    const logEntry: LogEntry = {
      timestamp: new Date(),
      level,
      message,
      context,
      data,
      stack
    };

    // Add to logs array
    this.logs.push(logEntry);

    // Trim logs if we exceed maxLogs
    if (this.logs.length > this.maxLogs) {
      this.logs = this.logs.slice(-this.maxLogs);
    }

    // Also log to console for development
    this.logToConsole(logEntry);
  }

  // Log to console based on level
  private logToConsole(logEntry: LogEntry): void {
    const { timestamp, level, message, context, data, stack } = logEntry;
    const timestampStr = timestamp.toISOString();
    const contextStr = context ? `[${context}]` : '';
    const dataStr = data ? JSON.stringify(data) : '';
    const stackStr = stack ? `\n${stack}` : '';

    const logMessage = `${timestampStr} ${level.toUpperCase()}${contextStr}: ${message}${dataStr ? ` ${dataStr}` : ''}${stackStr}`;

    switch (level) {
      case 'debug':
        console.debug(logMessage);
        break;
      case 'info':
        console.info(logMessage);
        break;
      case 'warn':
        console.warn(logMessage);
        break;
      case 'error':
        console.error(logMessage);
        break;
    }
  }

  // Get all logs
  getLogs(): LogEntry[] {
    return [...this.logs];
  }

  // Get logs filtered by level
  getLogsByLevel(level: LogLevel): LogEntry[] {
    return this.logs.filter(log => log.level === level);
  }

  // Get logs filtered by context
  getLogsByContext(context: string): LogEntry[] {
    return this.logs.filter(log => log.context === context);
  }

  // Clear all logs
  clearLogs(): void {
    this.logs = [];
  }

  // Export logs as JSON string
  exportLogs(): string {
    return JSON.stringify(this.logs, null, 2);
  }

  // Save logs to file (in a real app, this would use FileSystem API)
  async saveLogsToFile(): Promise<void> {
    try {
      // In a real implementation, we would use Expo's FileSystem API
      // For now, we'll just log that this would happen
      console.log('Logs would be saved to file in a real implementation');
    } catch (error) {
      console.error('Failed to save logs to file:', error);
    }
  }

  // Get log statistics
  getLogStats(): Record<LogLevel, number> {
    const stats: Record<LogLevel, number> = {
      debug: 0,
      info: 0,
      warn: 0,
      error: 0
    };

    this.logs.forEach(log => {
      stats[log.level]++;
    });

    return stats;
  }
}

// Export singleton instance
export default new LoggingService();