import { useState } from 'react'

interface SearchFilterProps {
  onSearch: (query: string) => void
  onFilter: (filters: Record<string, any>) => void
  onSort: (sortBy: string, sortOrder: 'asc' | 'desc') => void
  filterOptions?: {
    name: string
    options: { value: string; label: string }[]
  }[]
  sortOptions?: { value: string; label: string }[]
}

export default function SearchFilter({
  onSearch,
  onFilter,
  onSort,
  filterOptions = [],
  sortOptions = []
}: SearchFilterProps) {
  const [searchQuery, setSearchQuery] = useState('')
  const [filters, setFilters] = useState<Record<string, any>>({})
  const [sortBy, setSortBy] = useState('')
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc')

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const query = e.target.value
    setSearchQuery(query)
    onSearch(query)
  }

  const handleFilterChange = (filterName: string, value: string) => {
    const newFilters = { ...filters, [filterName]: value }
    setFilters(newFilters)
    onFilter(newFilters)
  }

  const handleSortChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const [field, order] = e.target.value.split(':')
    setSortBy(field)
    setSortOrder(order as 'asc' | 'desc')
    onSort(field, order as 'asc' | 'desc')
  }

  const clearFilters = () => {
    setSearchQuery('')
    setFilters({})
    setSortBy('')
    setSortOrder('asc')
    onSearch('')
    onFilter({})
    onSort('', 'asc')
  }

  return (
    <div className="mb-6 bg-white rounded-lg shadow p-4">
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        {/* Search Input */}
        <div className="md:col-span-2">
          <label htmlFor="search" className="block text-sm font-medium text-gray-700 mb-1">
            Search
          </label>
          <div className="relative rounded-md shadow-sm">
            <input
              type="text"
              id="search"
              className="focus:ring-indigo-500 focus:border-indigo-500 block w-full pr-10 py-2 sm:text-sm border-gray-300 rounded-md"
              placeholder="Search..."
              value={searchQuery}
              onChange={handleSearchChange}
            />
            <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
              <svg className="h-5 w-5 text-gray-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clipRule="evenodd" />
              </svg>
            </div>
          </div>
        </div>

        {/* Filter Options */}
        {filterOptions.map((filter) => (
          <div key={filter.name}>
            <label htmlFor={filter.name} className="block text-sm font-medium text-gray-700 mb-1">
              {filter.name}
            </label>
            <select
              id={filter.name}
              className="focus:ring-indigo-500 focus:border-indigo-500 block w-full py-2 sm:text-sm border-gray-300 rounded-md"
              value={filters[filter.name] || ''}
              onChange={(e) => handleFilterChange(filter.name, e.target.value)}
            >
              <option value="">All</option>
              {filter.options.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
        ))}

        {/* Sort Options */}
        {sortOptions.length > 0 && (
          <div>
            <label htmlFor="sort" className="block text-sm font-medium text-gray-700 mb-1">
              Sort By
            </label>
            <select
              id="sort"
              className="focus:ring-indigo-500 focus:border-indigo-500 block w-full py-2 sm:text-sm border-gray-300 rounded-md"
              value={`${sortBy}:${sortOrder}`}
              onChange={handleSortChange}
            >
              <option value="">Default</option>
              {sortOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
        )}
      </div>

      {/* Clear Filters Button */}
      <div className="mt-4 flex justify-end">
        <button
          type="button"
          className="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
          onClick={clearFilters}
        >
          Clear Filters
        </button>
      </div>
    </div>
  )
}