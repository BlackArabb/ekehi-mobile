'use client';

import React from 'react';
import Card from '@/components/Card';
import Chart from '@/components/Chart';

interface DashboardChartProps {
  title: string;
  type: 'line' | 'bar' | 'pie' | 'area';
  data: any[];
  dataKey: string;
  nameKey?: string;
  height?: number;
}

export default function DashboardChart({ 
  title, 
  type, 
  data, 
  dataKey, 
  nameKey = 'name', 
  height = 300 
}: DashboardChartProps) {
  return (
    <Card title={title} className="h-full">
      <Chart 
        type={type} 
        data={data} 
        dataKey={dataKey} 
        nameKey={nameKey} 
        height={height} 
      />
    </Card>
  );
}