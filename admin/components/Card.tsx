import React from 'react';

interface CardProps {
  title?: string;
  subtitle?: string;
  children: React.ReactNode;
  actions?: React.ReactNode;
  className?: string;
}

export default function Card({ title, subtitle, children, actions, className = '' }: CardProps) {
  return (
    <div className={`bg-white shadow rounded-lg overflow-hidden dark:bg-gray-800 ${className}`}>
      {(title || subtitle || actions) && (
        <div className="px-3 py-4 sm:px-6 border-b border-gray-200 dark:border-gray-700">
          <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-2 sm:gap-0">
            <div className="text-center sm:text-left">
              {title && (
                <h3 className="text-base md:text-lg font-medium leading-6 text-gray-900 dark:text-white">
                  {title}
                </h3>
              )}
              {subtitle && (
                <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                  {subtitle}
                </p>
              )}
            </div>
            {actions && <div className="flex justify-center sm:justify-end">{actions}</div>}
          </div>
        </div>
      )}
      <div className="px-3 py-4 sm:p-6">
        {children}
      </div>
    </div>
  );
}