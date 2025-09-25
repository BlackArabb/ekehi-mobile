import React from 'react';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom';
import SearchFilter from '../components/SearchFilter';

describe('SearchFilter', () => {
  const mockOnSearch = jest.fn();
  const mockOnFilter = jest.fn();
  const mockOnSort = jest.fn();
  
  const filterOptions = [
    {
      name: 'status',
      options: [
        { value: 'active', label: 'Active' },
        { value: 'inactive', label: 'Inactive' }
      ]
    }
  ];
  
  const sortOptions = [
    { value: 'name:asc', label: 'Name (A-Z)' },
    { value: 'name:desc', label: 'Name (Z-A)' }
  ];

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders search input correctly', () => {
    render(
      <SearchFilter
        onSearch={mockOnSearch}
        onFilter={mockOnFilter}
        onSort={mockOnSort}
      />
    );
    
    const searchInput = screen.getByPlaceholderText('Search...');
    expect(searchInput).toBeInTheDocument();
  });

  it('renders filter options correctly', () => {
    render(
      <SearchFilter
        onSearch={mockOnSearch}
        onFilter={mockOnFilter}
        onSort={mockOnSort}
        filterOptions={filterOptions}
      />
    );
    
    const statusFilter = screen.getByLabelText('status', { selector: 'select' });
    expect(statusFilter).toBeInTheDocument();
    expect(screen.getByText('Active')).toBeInTheDocument();
    expect(screen.getByText('Inactive')).toBeInTheDocument();
  });

  it('renders sort options correctly', () => {
    render(
      <SearchFilter
        onSearch={mockOnSearch}
        onFilter={mockOnFilter}
        onSort={mockOnSort}
        sortOptions={sortOptions}
      />
    );
    
    const sortSelect = screen.getByLabelText('Sort By', { selector: 'select' });
    expect(sortSelect).toBeInTheDocument();
    expect(screen.getByText('Name (A-Z)')).toBeInTheDocument();
    expect(screen.getByText('Name (Z-A)')).toBeInTheDocument();
  });

  it('calls onSearch when typing in search input', async () => {
    const user = userEvent.setup();
    render(
      <SearchFilter
        onSearch={mockOnSearch}
        onFilter={mockOnFilter}
        onSort={mockOnSort}
      />
    );
    
    const searchInput = screen.getByPlaceholderText('Search...');
    await user.type(searchInput, 'test');
    
    expect(mockOnSearch).toHaveBeenCalledWith('test');
  });

  it('calls onFilter when selecting a filter option', async () => {
    const user = userEvent.setup();
    render(
      <SearchFilter
        onSearch={mockOnSearch}
        onFilter={mockOnFilter}
        onSort={mockOnSort}
        filterOptions={filterOptions}
      />
    );
    
    const statusFilter = screen.getByLabelText('status', { selector: 'select' });
    await user.selectOptions(statusFilter, 'active');
    
    expect(mockOnFilter).toHaveBeenCalledWith({ status: 'active' });
  });

  it('calls onSort when selecting a sort option', async () => {
    const user = userEvent.setup();
    render(
      <SearchFilter
        onSearch={mockOnSearch}
        onFilter={mockOnFilter}
        onSort={mockOnSort}
        sortOptions={sortOptions}
      />
    );
    
    const sortSelect = screen.getByLabelText('Sort By', { selector: 'select' });
    await user.selectOptions(sortSelect, 'name:asc');
    
    expect(mockOnSort).toHaveBeenCalledWith('name', 'asc');
  });

  it('calls all handlers when clear filters is clicked', async () => {
    const user = userEvent.setup();
    render(
      <SearchFilter
        onSearch={mockOnSearch}
        onFilter={mockOnFilter}
        onSort={mockOnSort}
        filterOptions={filterOptions}
        sortOptions={sortOptions}
      />
    );
    
    const clearButton = screen.getByText('Clear Filters');
    await user.click(clearButton);
    
    expect(mockOnSearch).toHaveBeenCalledWith('');
    expect(mockOnFilter).toHaveBeenCalledWith({});
    expect(mockOnSort).toHaveBeenCalledWith('', 'asc');
  });
});