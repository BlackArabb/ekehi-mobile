'use client'

import { Dialog, Transition } from '@headlessui/react'
import { Fragment, useState } from 'react'
import { 
  HomeIcon, 
  UsersIcon, 
  CurrencyDollarIcon, 
  ChartBarIcon, 
  CogIcon,
  XMarkIcon,
  WalletIcon,
  MegaphoneIcon,
  GiftIcon,
  ChevronRightIcon
} from '@heroicons/react/24/outline'
import { classNames } from '@/lib/utils'

const navigation = [
  { name: 'Dashboard', href: '/dashboard', icon: HomeIcon, current: true },
  { name: 'Users', href: '/dashboard/users', icon: UsersIcon, current: false },
  { name: 'Presale', href: '/dashboard/presale', icon: CurrencyDollarIcon, current: false },
  { name: 'Wallet', href: '/dashboard/wallet', icon: WalletIcon, current: false },
  { name: 'Social Tasks', href: '/dashboard/social', icon: MegaphoneIcon, current: false },
  { name: 'Ads', href: '/dashboard/ads', icon: GiftIcon, current: false },
  { name: 'Settings', href: '/dashboard/settings', icon: CogIcon, current: false },
]

export default function Sidebar({
  sidebarOpen,
  setSidebarOpen,
}: {
  sidebarOpen: boolean
  setSidebarOpen: (open: boolean) => void
}) {
  const [expanded, setExpanded] = useState(false)

  // Function to toggle sidebar expansion
  const toggleExpansion = () => {
    setExpanded(!expanded)
  }

  return (
    <>
      <Transition.Root show={sidebarOpen} as={Fragment}>
        <Dialog as="div" className="relative z-50 md:hidden" onClose={setSidebarOpen}>
          <Transition.Child
            as={Fragment}
            enter="transition-opacity ease-linear duration-300"
            enterFrom="opacity-0"
            enterTo="opacity-100"
            leave="transition-opacity ease-linear duration-300"
            leaveFrom="opacity-100"
            leaveTo="opacity-0"
          >
            <div className="fixed inset-0 bg-gray-900/80 backdrop-blur-sm" />
          </Transition.Child>

          <div className="fixed inset-0 flex">
            <Transition.Child
              as={Fragment}
              enter="transition ease-in-out duration-300 transform"
              enterFrom="-translate-x-full"
              enterTo="translate-x-0"
              leave="transition ease-in-out duration-300 transform"
              leaveFrom="translate-x-0"
              leaveTo="-translate-x-full"
            >
              <Dialog.Panel className="relative flex w-full max-w-xs flex-1 flex-col glass-effect border-r border-purple-500/20" style={{ zIndex: 50 }}>
                <Transition.Child
                  as={Fragment}
                  enter="ease-in-out duration-300"
                  enterFrom="opacity-0"
                  enterTo="opacity-100"
                  leave="ease-in-out duration-300"
                  leaveFrom="opacity-100"
                  leaveTo="opacity-0"
                >
                  <div className="absolute top-0 right-0 -mr-12 pt-2">
                    <button
                      type="button"
                      className="ml-1 flex h-10 w-10 items-center justify-center rounded-full bg-gradient-to-br from-purple-500 to-cyan-500 text-white shadow-lg focus:outline-none focus:ring-2 focus:ring-white"
                      onClick={() => setSidebarOpen(false)}
                    >
                      <span className="sr-only">Close sidebar</span>
                      <XMarkIcon className="h-6 w-6" aria-hidden="true" />
                    </button>
                  </div>
                </Transition.Child>
                <div className="h-0 flex-1 overflow-y-auto pt-5 pb-4">
                  <div className="flex flex-shrink-0 items-center px-4">
                    <div className="h-10 w-10 rounded-full bg-gradient-to-br from-purple-500 to-cyan-500 flex items-center justify-center shadow-lg">
                      <span className="text-white font-bold text-lg">E</span>
                    </div>
                    <div className="ml-3">
                      <h1 className="text-xl font-bold bg-gradient-to-r from-purple-400 to-cyan-400 bg-clip-text text-transparent">Ekehi Admin</h1>
                    </div>
                  </div>
                  <nav className="mt-5 space-y-1 px-2">
                    {navigation.map((item) => (
                      <a
                        key={item.name}
                        href={item.href}
                        className={classNames(
                          item.current
                            ? 'bg-gradient-to-r from-purple-500/20 to-cyan-500/20 text-white border-l-4 border-purple-500'
                            : 'text-gray-300 hover:bg-gray-700/50 hover:text-white',
                          'group flex items-center px-2 py-3 text-base font-medium rounded-lg transition-all duration-200'
                        )}
                      >
                        <item.icon
                          className={classNames(
                            item.current
                              ? 'text-purple-400'
                              : 'text-gray-400 group-hover:text-cyan-400',
                            'mr-4 h-6 w-6 flex-shrink-0'
                          )}
                          aria-hidden="true"
                        />
                        {item.name}
                      </a>
                    ))}
                  </nav>
                </div>
              </Dialog.Panel>
            </Transition.Child>
          </div>
        </Dialog>
      </Transition.Root>

      {/* Static sidebar for desktop - collapsed by default */}
      <div className={`hidden md:fixed md:inset-y-0 md:left-0 md:flex md:flex-col transition-all duration-300 ease-in-out ${expanded ? 'md:w-64' : 'md:w-20'} z-30`} style={{ zIndex: 30 }}>
        {/* Sidebar component */}
        <div className="flex min-h-0 flex-1 flex-col glass-effect border-r border-purple-500/20" style={{ zIndex: 30 }}>
          <div className="flex flex-shrink-0 items-center p-4 justify-center">
            <div className="h-10 w-10 rounded-full bg-gradient-to-br from-purple-500 to-cyan-500 flex items-center justify-center shadow-lg">
              <span className="text-white font-bold text-lg">E</span>
            </div>
          </div>
          
          {/* Expand/Collapse button */}
          <div className="flex justify-center py-2">
            <button
              onClick={toggleExpansion}
              className="p-1 rounded-lg bg-gray-800/50 text-gray-300 hover:text-white hover:bg-gray-700/50 focus:outline-none focus:ring-2 focus:ring-purple-500 transition-colors duration-200"
              aria-label={expanded ? "Collapse sidebar" : "Expand sidebar"}
            >
              <ChevronRightIcon 
                className={`h-5 w-5 transition-transform duration-300 ${expanded ? 'rotate-180' : ''}`} 
                aria-hidden="true" 
              />
            </button>
          </div>
          
          <nav className="mt-5 flex-1 px-2 space-y-1">
            {navigation.map((item) => (
              <a
                key={item.name}
                href={item.href}
                className={classNames(
                  item.current
                    ? 'bg-gradient-to-r from-purple-500/20 to-cyan-500/20 text-white'
                    : 'text-gray-300 hover:bg-gray-700/50 hover:text-white',
                  'group flex items-center rounded-lg transition-all duration-200 py-3',
                  expanded ? 'px-3' : 'justify-center px-3'
                )}
              >
                <item.icon
                  className={classNames(
                    item.current
                      ? 'text-purple-400'
                      : 'text-gray-400 group-hover:text-cyan-400',
                    'h-6 w-6 flex-shrink-0'
                  )}
                  aria-hidden="true"
                />
                {expanded && (
                  <span className="ml-3 text-sm font-medium">{item.name}</span>
                )}
              </a>
            ))}
          </nav>
        </div>
      </div>
    </>
  )
}