import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import PerformanceMonitor from '@/services/PerformanceMonitor';

interface PerformanceReportProps {
  onClose: () => void;
}

const PerformanceReport: React.FC<PerformanceReportProps> = ({ onClose }) => {
  const [metrics, setMetrics] = useState<Record<string, { count: number; average: number; min: number; max: number }>>({});

  useEffect(() => {
    // Get initial metrics
    setMetrics(PerformanceMonitor.getSummary());
    
    // Update metrics every 5 seconds
    const interval = setInterval(() => {
      setMetrics(PerformanceMonitor.getSummary());
    }, 5000);
    
    return () => clearInterval(interval);
  }, []);

  const handleClearMetrics = () => {
    PerformanceMonitor.clearMetrics();
    setMetrics({});
  };

  const handleLogSummary = () => {
    PerformanceMonitor.logSummary();
  };

  return (
    <LinearGradient colors={['#1a1a2e', '#16213e', '#0f3460']} style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Performance Report</Text>
        <TouchableOpacity style={styles.closeButton} onPress={onClose}>
          <Text style={styles.closeButtonText}>×</Text>
        </TouchableOpacity>
      </View>
      
      <ScrollView style={styles.content}>
        {Object.keys(metrics).length === 0 ? (
          <View style={styles.emptyState}>
            <Text style={styles.emptyText}>No performance data available yet</Text>
          </View>
        ) : (
          Object.entries(metrics).map(([operation, stats]) => (
            <View key={operation} style={styles.metricCard}>
              <Text style={styles.operationName}>{operation}</Text>
              <View style={styles.metricRow}>
                <Text style={styles.metricLabel}>Count:</Text>
                <Text style={styles.metricValue}>{stats.count}</Text>
              </View>
              <View style={styles.metricRow}>
                <Text style={styles.metricLabel}>Average:</Text>
                <Text style={styles.metricValue}>{stats.average.toFixed(2)}ms</Text>
              </View>
              <View style={styles.metricRow}>
                <Text style={styles.metricLabel}>Min:</Text>
                <Text style={styles.metricValue}>{stats.min}ms</Text>
              </View>
              <View style={styles.metricRow}>
                <Text style={styles.metricLabel}>Max:</Text>
                <Text style={styles.metricValue}>{stats.max}ms</Text>
              </View>
            </View>
          ))
        )}
      </ScrollView>
      
      <View style={styles.actions}>
        <TouchableOpacity style={styles.actionButton} onPress={handleClearMetrics}>
          <Text style={styles.actionButtonText}>Clear Metrics</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.actionButton} onPress={handleLogSummary}>
          <Text style={styles.actionButtonText}>Log Summary</Text>
        </TouchableOpacity>
      </View>
    </LinearGradient>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 20,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  closeButton: {
    padding: 10,
  },
  closeButtonText: {
    fontSize: 24,
    color: '#ffffff',
    fontWeight: 'bold',
  },
  content: {
    flex: 1,
  },
  emptyState: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  emptyText: {
    fontSize: 16,
    color: 'rgba(255, 255, 255, 0.7)',
  },
  metricCard: {
    backgroundColor: 'rgba(255, 255, 255, 0.05)',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.1)',
  },
  operationName: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#ffa000',
    marginBottom: 10,
  },
  metricRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 5,
  },
  metricLabel: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.7)',
  },
  metricValue: {
    fontSize: 14,
    color: '#ffffff',
    fontWeight: '500',
  },
  actions: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 20,
  },
  actionButton: {
    backgroundColor: 'rgba(255, 160, 0, 0.2)',
    borderRadius: 8,
    paddingVertical: 12,
    paddingHorizontal: 20,
  },
  actionButtonText: {
    color: '#ffa000',
    fontWeight: '600',
  },
});

export default PerformanceReport;