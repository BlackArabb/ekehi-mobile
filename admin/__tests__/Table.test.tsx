import React from 'react';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom';
import Table from '../components/Table';

describe('Table', () => {
  const columns = [
    { key: 'name', title: 'Name' },
    { key: 'email', title: 'Email' },
    { key: 'role', title: 'Role' }
  ];

  const data = [
    { name: 'John Doe', email: 'john@example.com', role: 'Admin' },
    { name: 'Jane Smith', email: 'jane@example.com', role: 'User' }
  ];

  it('renders correctly with data', () => {
    render(<Table columns={columns} data={data} />);
    
    expect(screen.getByText('Name')).toBeInTheDocument();
    expect(screen.getByText('Email')).toBeInTheDocument();
    expect(screen.getByText('Role')).toBeInTheDocument();
    
    expect(screen.getByText('John Doe')).toBeInTheDocument();
    expect(screen.getByText('john@example.com')).toBeInTheDocument();
    expect(screen.getByText('Admin')).toBeInTheDocument();
    
    expect(screen.getByText('Jane Smith')).toBeInTheDocument();
    expect(screen.getByText('jane@example.com')).toBeInTheDocument();
    expect(screen.getByText('User')).toBeInTheDocument();
  });

  it('renders loading state when loading is true', () => {
    render(<Table columns={columns} data={data} loading={true} />);
    
    const spinner = screen.getByText('', { selector: 'div.animate-spin' });
    expect(spinner).toBeInTheDocument();
  });

  it('calls onRowClick when a row is clicked', async () => {
    const user = userEvent.setup();
    const handleRowClick = jest.fn();
    render(<Table columns={columns} data={data} onRowClick={handleRowClick} />);
    
    const firstRow = screen.getByText('John Doe').closest('tr');
    if (firstRow) {
      await user.click(firstRow);
      expect(handleRowClick).toHaveBeenCalledWith(data[0]);
    }
  });

  it('renders custom cell content when render function is provided', () => {
    const columnsWithRender = [
      { key: 'name', title: 'Name' },
      { key: 'email', title: 'Email' },
      { 
        key: 'role', 
        title: 'Role',
        render: (value: string) => (
          <span className="font-bold">{value.toUpperCase()}</span>
        )
      }
    ];
    
    render(<Table columns={columnsWithRender} data={data} />);
    
    const roleCell = screen.getByText('ADMIN');
    expect(roleCell).toHaveClass('font-bold');
  });

  it('applies custom className when provided', () => {
    render(<Table columns={columns} data={data} className="custom-table" />);
    
    const tableContainer = screen.getByText('Name').closest('div');
    expect(tableContainer).toHaveClass('custom-table');
  });
});