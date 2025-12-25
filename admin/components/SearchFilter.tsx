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
    <div className="mb-6 glass-effect rounded-2xl border border-purple-500/20 p-6 shadow-xl">
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        {/* Search Input */}
        <div className="md:col-span-2">
          <label htmlFor="search" className="block text-sm font-medium text-gray-300 mb-1">
            Search
          </label>
          <div className="relative rounded-lg">
            <input
              type="text"
              id="search"
              className="block w-full py-3 px-4 bg-gray-800/50 border border-gray-700/50 text-white placeholder-gray-400 focus:border-purple-500 focus:ring-purple-500 rounded-lg"
              placeholder="Search users..."
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
            <label htmlFor={filter.name} className="block text-sm font-medium text-gray-300 mb-1">
              {filter.name}
            </label>
            <select
              id={filter.name}
              className="block w-full py-3 px-3 bg-gray-800/50 border border-gray-700/50 text-white focus:border-purple-500 focus:ring-purple-500 rounded-lg"
              value={filters[filter.name] || ''}
              onChange={(e) => handleFilterChange(filter.name, e.target.value)}
            >
              <option value="" className="bg-gray-800">All</option>
              {filter.options.map((option) => (
                <option key={option.value} value={option.value} className="bg-gray-800">
                  {option.label}
                </option>
              ))}
            </select>
          </div>
        ))}

        {/* Sort Options */}
        {sortOptions.length > 0 && (
          <div>
            <label htmlFor="sort" className="block text-sm font-medium text-gray-300 mb-1">
              Sort By
            </label>
            <select
              id="sort"
              className="block w-full py-3 px-3 bg-gray-800/50 border border-gray-700/50 text-white focus:border-purple-500 focus:ring-purple-500 rounded-lg"
              value={`${sortBy}:${sortOrder}`}
              onChange={handleSortChange}
            >
              <option value="" className="bg-gray-800">Default</option>
              {sortOptions.map((option) => (
                <option key={option.value} value={option.value} className="bg-gray-800">
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
          className="inline-flex items-center px-4 py-2 text-sm font-medium text-gray-300 hover:text-white border border-gray-600 hover:border-gray-500 rounded-lg transition-colors duration-200"
          onClick={clearFilters}
        >
          Clear Filters
        </button>
      </div>
    </div>
  )
}