import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import Card from '../components/Card';

describe('Card', () => {
  it('renders correctly with children', () => {
    render(
      <Card>
        <p>Card content</p>
      </Card>
    );
    
    expect(screen.getByText('Card content')).toBeInTheDocument();
  });

  it('renders title when provided', () => {
    render(
      <Card title="Card Title">
        <p>Card content</p>
      </Card>
    );
    
    expect(screen.getByText('Card Title')).toBeInTheDocument();
  });

  it('renders subtitle when provided', () => {
    render(
      <Card title="Card Title" subtitle="Card subtitle">
        <p>Card content</p>
      </Card>
    );
    
    expect(screen.getByText('Card subtitle')).toBeInTheDocument();
  });

  it('renders actions when provided', () => {
    const actions = <button>Action Button</button>;
    render(
      <Card title="Card Title" actions={actions}>
        <p>Card content</p>
      </Card>
    );
    
    expect(screen.getByText('Action Button')).toBeInTheDocument();
  });

  it('applies custom className when provided', () => {
    render(
      <Card className="custom-class">
        <p>Card content</p>
      </Card>
    );
    
    // The Card component has multiple divs, we need to find the one with the custom class
    // The outermost div should have the custom class
    const cardElements = screen.getAllByText('Card content');
    const cardContainer = cardElements[0].closest('.custom-class');
    expect(cardContainer).toBeInTheDocument();
  });
});