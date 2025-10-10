'use client'

import { useState, useEffect } from 'react'
import { CheckCircleIcon, XCircleIcon } from '@heroicons/react/24/outline'

export default function Settings() {
  const [isAppReady, setIsAppReady] = useState(false)
  const [saving, setSaving] = useState(false)
  const [saved, setSaved] = useState(false)

  // Load the current app status from localStorage when component mounts
  useEffect(() => {
    const savedStatus = localStorage.getItem('ekehiAppReady')
    if (savedStatus !== null) {
      setIsAppReady(savedStatus === 'true')
    }
  }, [])

  // Save the app status to localStorage whenever it changes
  const handleSave = () => {
    setSaving(true)
    // Simulate API call delay
    setTimeout(() => {
      localStorage.setItem('ekehiAppReady', isAppReady.toString())
      setSaving(false)
      setSaved(true)
      // Reset saved status after 3 seconds
      setTimeout(() => setSaved(false), 3000)
    }, 500)
  }

  return (
    <div className="py-6">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 md:px-8">
        <h1 className="text-2xl font-bold text-white">App Settings</h1>
        <p className="mt-1 text-sm text-gray-400">
          Manage the status of the Ekehi mobile app
        </p>
      </div>

      <div className="mx-auto max-w-7xl px-4 sm:px-6 md:px-8">
        {/* App Status Card */}
        <div className="mt-6 glass-effect rounded-2xl shadow-2xl border border-purple-500/20 p-6">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-lg font-medium text-white">Mobile App Status</h2>
              <p className="mt-1 text-sm text-gray-400">
                Control whether users see "Download" or "COMING SOON" on the landing page
              </p>
            </div>
            
            <div className="flex items-center">
              {isAppReady ? (
                <div className="flex items-center">
                  <CheckCircleIcon className="h-5 w-5 text-green-500" />
                  <span className="ml-2 text-green-500 font-medium">Ready for Download</span>
                </div>
              ) : (
                <div className="flex items-center">
                  <XCircleIcon className="h-5 w-5 text-yellow-500" />
                  <span className="ml-2 text-yellow-500 font-medium">Coming Soon</span>
                </div>
              )}
            </div>
          </div>

          <div className="mt-6 space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-base font-medium text-white">App Download Status</h3>
                <p className="mt-1 text-sm text-gray-400">
                  {isAppReady 
                    ? 'Users will see the "Download" button on the landing page' 
                    : 'Users will see the "COMING SOON" button on the landing page'}
                </p>
              </div>
              
              <div className="flex items-center">
                <button
                  type="button"
                  className={`${
                    isAppReady 
                      ? 'bg-gradient-to-r from-green-500 to-emerald-600' 
                      : 'bg-gradient-to-r from-yellow-500 to-amber-600'
                  } relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2`}
                  onClick={() => setIsAppReady(!isAppReady)}
                >
                  <span
                    className={`${
                      isAppReady ? 'translate-x-5' : 'translate-x-0'
                    } pointer-events-none relative inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out`}
                  >
                    <span
                      className={`${
                        isAppReady
                          ? 'opacity-0 duration-100 ease-out'
                          : 'opacity-100 duration-200 ease-in'
                      } absolute inset-0 flex h-full w-full items-center justify-center transition-opacity`}
                      aria-hidden="true"
                    >
                      <svg className="h-3 w-3 text-gray-400" fill="none" viewBox="0 0 12 12">
                        <path
                          d="M4 8l2-2m0 0l2-2M6 6L4 4m2 2l2 2"
                          stroke="currentColor"
                          strokeWidth={2}
                          strokeLinecap="round"
                          strokeLinejoin="round"
                        />
                      </svg>
                    </span>
                    <span
                      className={`${
                        isAppReady
                          ? 'opacity-100 duration-200 ease-in'
                          : 'opacity-0 duration-100 ease-out'
                      } absolute inset-0 flex h-full w-full items-center justify-center transition-opacity`}
                      aria-hidden="true"
                    >
                      <svg className="h-3 w-3 text-green-600" fill="currentColor" viewBox="0 0 12 12">
                        <path d="M3.707 5.293a1 1 0 00-1.414 1.414l1.414-1.414zM5 8l-.707.707a1 1 0 001.414 0L5 8zm4.707-5.707a1 1 0 00-1.414-1.414l1.414 1.414zm-7.414 2l2 2 1.414-1.414-2-2-1.414 1.414zm3.414 2l4-4-1.414-1.414-4 4 1.414 1.414z" />
                      </svg>
                    </span>
                  </span>
                </button>
              </div>
            </div>

            <div className="flex justify-end pt-4">
              <button
                type="button"
                className="inline-flex items-center rounded-md bg-gradient-to-r from-purple-600 to-indigo-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:from-purple-700 hover:to-indigo-700 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2 disabled:opacity-50"
                onClick={handleSave}
                disabled={saving}
              >
                {saving ? (
                  <>
                    <svg className="mr-2 h-4 w-4 animate-spin" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Saving...
                  </>
                ) : (
                  'Save Changes'
                )}
              </button>
              
              {saved && (
                <div className="ml-4 flex items-center text-sm text-green-500">
                  <CheckCircleIcon className="h-5 w-5" />
                  <span className="ml-1">Settings saved!</span>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Preview Card */}
        <div className="mt-6 glass-effect rounded-2xl shadow-2xl border border-purple-500/20 p-6">
          <h2 className="text-lg font-medium text-white">Preview</h2>
          <p className="mt-1 text-sm text-gray-400">
            This is how the button will appear on the landing page
          </p>
          
          <div className="mt-6 flex flex-col items-center">
            <div className="text-center mb-4">
              <h3 className="text-base font-medium text-white">Landing Page Button Preview</h3>
            </div>
            
            {isAppReady ? (
              <button 
                className="btn-primary inline-flex items-center gap-2 group py-3 px-6 text-sm"
                disabled
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M3 17a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm3.293-7.707a1 1 0 011.414 0L9 10.586V3a1 1 0 112 0v7.586l1.293-1.293a1 1 0 111.414 1.414l-3 3a1 1 0 01-1.414 0l-3-3a1 1 0 010-1.414z" clipRule="evenodd" />
                </svg>
                Download Mining App
              </button>
            ) : (
              <button 
                className="btn-secondary inline-flex items-center gap-2 group py-3 px-6 text-sm cursor-not-allowed opacity-75"
                disabled
              >
                <span className="animate-pulse">COMING SOON</span>
              </button>
            )}
            
            <div className="mt-4 text-sm text-gray-400">
              <p>
                {isAppReady 
                  ? 'Users will be able to download the app when they visit the landing page' 
                  : 'Users will see the coming soon message until the app is ready'}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}