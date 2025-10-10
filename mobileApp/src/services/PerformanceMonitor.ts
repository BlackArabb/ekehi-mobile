// Performance monitoring service for tracking app performance metrics
class PerformanceMonitor {
  private static instance: PerformanceMonitor;
  private metrics: Map<string, number[]> = new Map();
  private startTime: number = 0;

  private constructor() {
    this.startTime = Date.now();
  }

  static getInstance(): PerformanceMonitor {
    if (!PerformanceMonitor.instance) {
      PerformanceMonitor.instance = new PerformanceMonitor();
    }
    return PerformanceMonitor.instance;
  }

  // Start timing an operation
  startTiming(operation: string): void {
    if (!this.metrics.has(operation)) {
      this.metrics.set(operation, []);
    }
    // Store the start time as a negative value to distinguish from actual metrics
    this.metrics.get(operation)?.push(-Date.now());
  }

  // End timing an operation and record the duration
  endTiming(operation: string): number {
    const timings = this.metrics.get(operation);
    if (!timings || timings.length === 0) {
      console.warn(`PerformanceMonitor: No start time found for operation ${operation}`);
      return 0;
    }

    // Get the last start time (negative value)
    const startTime = -timings[timings.length - 1];
    const duration = Date.now() - startTime;
    
    // Replace the start time with the actual duration
    timings[timings.length - 1] = duration;
    
    console.log(`⏱️ Performance - ${operation}: ${duration}ms`);
    return duration;
  }

  // Record a custom metric
  recordMetric(operation: string, value: number): void {
    if (!this.metrics.has(operation)) {
      this.metrics.set(operation, []);
    }
    this.metrics.get(operation)?.push(value);
    console.log(`📊 Performance - ${operation}: ${value}`);
  }

  // Get average duration for an operation
  getAverageTime(operation: string): number {
    const timings = this.metrics.get(operation);
    if (!timings || timings.length === 0) {
      return 0;
    }
    
    const sum = timings.reduce((acc, val) => acc + val, 0);
    return sum / timings.length;
  }

  // Get all metrics for an operation
  getMetrics(operation: string): number[] {
    return this.metrics.get(operation) || [];
  }

  // Get all operations
  getAllOperations(): string[] {
    return Array.from(this.metrics.keys());
  }

  // Clear all metrics
  clearMetrics(): void {
    this.metrics.clear();
  }

  // Get a summary report
  getSummary(): Record<string, { count: number; average: number; min: number; max: number }> {
    const summary: Record<string, { count: number; average: number; min: number; max: number }> = {};
    
    for (const [operation, timings] of this.metrics.entries()) {
      if (timings.length > 0) {
        const count = timings.length;
        const sum = timings.reduce((acc, val) => acc + val, 0);
        const average = sum / count;
        const min = Math.min(...timings);
        const max = Math.max(...timings);
        
        summary[operation] = {
          count,
          average,
          min,
          max
        };
      }
    }
    
    return summary;
  }

  // Log a summary report
  logSummary(): void {
    console.log('📈 Performance Monitor Summary:');
    const summary = this.getSummary();
    
    for (const [operation, stats] of Object.entries(summary)) {
      console.log(`  ${operation}:`);
      console.log(`    Count: ${stats.count}`);
      console.log(`    Average: ${stats.average.toFixed(2)}ms`);
      console.log(`    Min: ${stats.min}ms`);
      console.log(`    Max: ${stats.max}ms`);
    }
  }
}

export default PerformanceMonitor.getInstance();