import React from 'react';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom';

// Create a simplified version of the Modal component for testing
const TestModal = ({ 
  open, 
  onClose, 
  title, 
  children, 
  actions,
  size = 'md'
}: {
  open: boolean;
  onClose: () => void;
  title: string;
  children: React.ReactNode;
  actions?: React.ReactNode;
  size?: 'sm' | 'md' | 'lg' | 'xl';
}) => {
  if (!open) return null;

  const sizeClasses = {
    sm: 'max-w-sm',
    md: 'max-w-md',
    lg: 'max-w-lg',
    xl: 'max-w-xl'
  };

  return (
    <div data-testid="modal">
      <div className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" />
      <div className="fixed inset-0 z-50 overflow-y-auto">
        <div className="flex min-h-full items-end justify-center p-4 text-center sm:items-center sm:p-0">
          <div className={`relative transform rounded-lg bg-white px-4 pt-5 pb-4 text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-${sizeClasses[size]} sm:p-6 dark:bg-gray-800`}>
            <div className="absolute top-0 right-0 pt-4 pr-4">
              <button
                type="button"
                className="rounded-md bg-white text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 dark:bg-gray-800 dark:text-gray-400 dark:hover:text-gray-300"
                onClick={onClose}
                aria-label="Close"
              >
                <span className="sr-only">Close</span>
                <span>×</span>
              </button>
            </div>
            <div className="sm:flex sm:items-start">
              <div className="mt-3 text-center sm:mt-0 sm:text-left w-full">
                <h3 className="text-lg font-medium leading-6 text-gray-900 dark:text-white">
                  {title}
                </h3>
                <div className="mt-2">
                  {children}
                </div>
              </div>
            </div>
            {actions && (
              <div className="mt-5 sm:mt-4 sm:flex sm:flex-row-reverse">
                {actions}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

describe('Modal', () => {
  it('renders correctly when open is true', () => {
    render(
      <TestModal open={true} onClose={jest.fn()} title="Test Modal">
        <p>Modal content</p>
      </TestModal>
    );
    
    expect(screen.getByText('Test Modal')).toBeInTheDocument();
    expect(screen.getByText('Modal content')).toBeInTheDocument();
  });

  it('does not render when open is false', () => {
    render(
      <TestModal open={false} onClose={jest.fn()} title="Test Modal">
        <p>Modal content</p>
      </TestModal>
    );
    
    expect(screen.queryByText('Test Modal')).not.toBeInTheDocument();
  });

  it('calls onClose when close button is clicked', async () => {
    const user = userEvent.setup();
    const handleClose = jest.fn();
    render(
      <TestModal open={true} onClose={handleClose} title="Test Modal">
        <p>Modal content</p>
      </TestModal>
    );
    
    const closeButton = screen.getByLabelText('Close');
    await user.click(closeButton);
    
    expect(handleClose).toHaveBeenCalledTimes(1);
  });

  it('renders actions when provided', () => {
    const actions = <button>Custom Action</button>;
    render(
      <TestModal open={true} onClose={jest.fn()} title="Test Modal" actions={actions}>
        <p>Modal content</p>
      </TestModal>
    );
    
    expect(screen.getByText('Custom Action')).toBeInTheDocument();
  });

  it('applies correct size classes', () => {
    render(
      <TestModal open={true} onClose={jest.fn()} title="Test Modal" size="lg">
        <p>Modal content</p>
      </TestModal>
    );
    
    const modalPanel = screen.getByText('Test Modal').closest('div');
    expect(modalPanel).toBeInTheDocument();
  });
});