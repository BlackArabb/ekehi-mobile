import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import StatCard from '../components/StatCard';

describe('StatCard', () => {
  it('renders correctly with title and value', () => {
    render(
      <StatCard title="Total Users" value={1234} />
    );
    
    expect(screen.getByText('Total Users')).toBeInTheDocument();
    expect(screen.getByText('1234')).toBeInTheDocument();
  });

  it('renders description when provided', () => {
    render(
      <StatCard 
        title="Total Users" 
        value={1234} 
        description="Increase of 12% from last month" 
      />
    );
    
    expect(screen.getByText('Increase of 12% from last month')).toBeInTheDocument();
  });

  it('renders icon when provided', () => {
    const icon = <span data-testid="test-icon">Icon</span>;
    render(
      <StatCard 
        title="Total Users" 
        value={1234} 
        icon={icon}
      />
    );
    
    const iconElement = screen.getByTestId('test-icon');
    expect(iconElement).toBeInTheDocument();
  });

  it('applies correct icon color classes', () => {
    const icon = <span>Icon</span>;
    render(
      <StatCard 
        title="Total Users" 
        value={1234} 
        icon={icon}
        iconColor="green"
      />
    );
    
    const iconContainer = screen.getByTestId('stat-card-icon-container');
    expect(iconContainer).toHaveClass('bg-green-500');
  });
});