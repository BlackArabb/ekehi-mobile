'use client'

import { Fragment } from 'react'
import { Bars3Icon, BellIcon, XMarkIcon } from '@heroicons/react/24/outline'
import { Menu, Transition } from '@headlessui/react'
import { classNames } from '@/lib/utils'

export default function Header({
  setSidebarOpen,
}: {
  setSidebarOpen: (open: boolean) => void
}) {
  return (
    <header className="z-10 py-4 glass-effect border-b border-gray-700/50 shadow-lg">
      <div className="container flex items-center justify-between h-full px-6 mx-auto">
        {/* Mobile hamburger */}
        <button
          className="p-2 mr-5 -ml-1 rounded-lg bg-gradient-to-br from-purple-500 to-cyan-500 text-white shadow-lg focus:outline-none focus:ring-2 focus:ring-white md:hidden"
          onClick={() => setSidebarOpen(true)}
          aria-label="Menu"
        >
          <Bars3Icon className="w-6 h-6" aria-hidden="true" />
        </button>
        
        {/* Search input */}
        <div className="flex justify-center flex-1 lg:mr-32">
          <div className="relative w-full max-w-xl mr-6">
            <div className="absolute inset-y-0 flex items-center pl-3">
              <svg className="w-5 h-5 text-gray-400" aria-hidden="true" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clipRule="evenodd"></path>
              </svg>
            </div>
            <input
              className="w-full pl-10 pr-4 py-3 text-sm text-white placeholder-gray-400 bg-gray-800/50 border border-gray-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent backdrop-blur-sm"
              type="text"
              placeholder="Search..."
              aria-label="Search"
            />
          </div>
        </div>
        
        <ul className="flex items-center flex-shrink-0 space-x-4">
          {/* Theme toggler */}
          <li className="flex">
            <button
              className="p-2 rounded-lg bg-gray-800/50 text-gray-300 hover:text-white hover:bg-gray-700/50 focus:outline-none focus:ring-2 focus:ring-purple-500 transition-colors duration-200"
              aria-label="Toggle color mode"
            >
              <svg className="w-5 h-5" aria-hidden="true" fill="currentColor" viewBox="0 0 20 20">
                <path d="M17.293 13.293A8 8 0 016.707 2.707a8.001 8.001 0 1010.586 10.586z"></path>
              </svg>
            </button>
          </li>
          
          {/* Notifications menu */}
          <li className="relative">
            <button
              className="p-2 rounded-lg bg-gray-800/50 text-gray-300 hover:text-white hover:bg-gray-700/50 focus:outline-none focus:ring-2 focus:ring-purple-500 transition-colors duration-200"
              aria-label="Notifications"
              aria-haspopup="true"
            >
              <BellIcon className="w-5 h-5" aria-hidden="true" />
              {/* Notification badge */}
              <span className="absolute top-0 right-0 inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-white transform translate-x-1/2 -translate-y-1/2 bg-pink-500 rounded-full">3</span>
            </button>
          </li>
          
          {/* Profile menu */}
          <li className="relative">
            <Menu as="div" className="relative ml-3">
              <div>
                <Menu.Button className="flex text-sm bg-gradient-to-br from-purple-500 to-cyan-500 rounded-full focus:outline-none focus:ring-2 focus:ring-white focus:ring-offset-2 focus:ring-offset-gray-800">
                  <span className="sr-only">Open user menu</span>
                  <div className="h-10 w-10 rounded-full flex items-center justify-center bg-gradient-to-br from-purple-500 to-cyan-500 shadow-lg">
                    <span className="text-white font-bold text-lg">A</span>
                  </div>
                </Menu.Button>
              </div>
              <Transition
                as={Fragment}
                enter="transition ease-out duration-100"
                enterFrom="transform opacity-0 scale-95"
                enterTo="transform opacity-100 scale-100"
                leave="transition ease-in duration-75"
                leaveFrom="transform opacity-100 scale-100"
                leaveTo="transform opacity-0 scale-95"
              >
                <Menu.Items className="absolute right-0 z-10 mt-2 w-48 origin-top-right rounded-xl glass-effect border border-gray-700/50 shadow-2xl">
                  <Menu.Item>
                    {({ active }: { active: boolean }) => (
                      <a
                        href="#"
                        className={classNames(active ? 'bg-gray-700/50' : '', 'block px-4 py-3 text-sm text-white')}
                      >
                        <div className="font-medium">Admin User</div>
                        <div className="text-gray-300 text-xs">admin@ekehi.com</div>
                      </a>
                    )}
                  </Menu.Item>
                  <div className="border-t border-gray-700/50 my-1"></div>
                  <Menu.Item>
                    {({ active }: { active: boolean }) => (
                      <a
                        href="#"
                        className={classNames(active ? 'bg-gray-700/50' : '', 'block px-4 py-2 text-sm text-white')}
                      >
                        Your Profile
                      </a>
                    )}
                  </Menu.Item>
                  <Menu.Item>
                    {({ active }: { active: boolean }) => (
                      <a
                        href="#"
                        className={classNames(active ? 'bg-gray-700/50' : '', 'block px-4 py-2 text-sm text-white')}
                      >
                        Settings
                      </a>
                    )}
                  </Menu.Item>
                  <div className="border-t border-gray-700/50 my-1"></div>
                  <Menu.Item>
                    {({ active }: { active: boolean }) => (
                      <a
                        href="#"
                        className={classNames(active ? 'bg-red-500/20' : '', 'block px-4 py-2 text-sm text-red-400')}
                      >
                        Sign out
                      </a>
                    )}
                  </Menu.Item>
                </Menu.Items>
              </Transition>
            </Menu>
          </li>
        </ul>
      </div>
    </header>
  )
}