import React from 'react';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom';
import Alert from '../components/Alert';

describe('Alert', () => {
  it('renders correctly with title and message', () => {
    render(
      <Alert
        title="Success"
        message="Operation completed successfully"
        type="success"
      />
    );
    
    expect(screen.getByText('Success')).toBeInTheDocument();
    expect(screen.getByText('Operation completed successfully')).toBeInTheDocument();
  });

  it('renders correct styling for success type', () => {
    render(
      <Alert
        title="Success"
        message="Operation completed successfully"
        type="success"
      />
    );
    
    const alert = screen.getByRole('alert');
    expect(alert).toHaveClass('bg-green-50');
  });

  it('renders correct styling for error type', () => {
    render(
      <Alert
        title="Error"
        message="An error occurred"
        type="error"
      />
    );
    
    const alert = screen.getByRole('alert');
    expect(alert).toHaveClass('bg-red-50');
  });

  it('renders correct styling for warning type', () => {
    render(
      <Alert
        title="Warning"
        message="Please check your input"
        type="warning"
      />
    );
    
    const alert = screen.getByRole('alert');
    expect(alert).toHaveClass('bg-yellow-50');
  });

  it('renders correct styling for info type', () => {
    render(
      <Alert
        title="Information"
        message="This is an informational message"
        type="info"
      />
    );
    
    const alert = screen.getByRole('alert');
    expect(alert).toHaveClass('bg-blue-50');
  });

  it('renders close button when onClose is provided', () => {
    const handleClose = jest.fn();
    render(
      <Alert
        title="Success"
        message="Operation completed successfully"
        type="success"
        onClose={handleClose}
      />
    );
    
    const closeButton = screen.getByRole('button');
    expect(closeButton).toBeInTheDocument();
  });

  it('calls onClose when close button is clicked', async () => {
    const user = userEvent.setup();
    const handleClose = jest.fn();
    render(
      <Alert
        title="Success"
        message="Operation completed successfully"
        type="success"
        onClose={handleClose}
      />
    );
    
    const closeButton = screen.getByRole('button');
    await user.click(closeButton);
    
    expect(handleClose).toHaveBeenCalledTimes(1);
  });

  it('does not render close button when onClose is not provided', () => {
    render(
      <Alert
        title="Success"
        message="Operation completed successfully"
        type="success"
      />
    );
    
    const closeButton = screen.queryByRole('button');
    expect(closeButton).not.toBeInTheDocument();
  });
});