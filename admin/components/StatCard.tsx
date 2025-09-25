import React from 'react';

interface StatCardProps {
  title: string;
  value: string | number;
  description?: string;
  icon?: React.ReactNode;
  iconColor?: 'indigo' | 'green' | 'red' | 'blue' | 'purple';
}

export default function StatCard({ title, value, description, icon, iconColor = 'indigo' }: StatCardProps) {
  const iconColorClasses = {
    indigo: 'bg-indigo-500',
    green: 'bg-green-500',
    red: 'bg-red-500',
    blue: 'bg-blue-500',
    purple: 'bg-purple-500'
  };

  return (
    <div className="bg-white overflow-hidden shadow rounded-lg dark:bg-gray-800">
      <div className="px-4 py-5 sm:p-6">
        <div className="flex items-center">
          {icon && (
            <div 
              data-testid="stat-card-icon-container"
              className={`flex-shrink-0 ${iconColorClasses[iconColor]} rounded-md p-3`}
            >
              {icon}
            </div>
          )}
          <div className="ml-5 w-0 flex-1">
            <dl>
              <dt className="text-sm font-medium text-gray-500 truncate dark:text-gray-400">
                {title}
              </dt>
              <dd className="flex items-baseline">
                <div className="text-2xl font-semibold text-gray-900 dark:text-white">
                  {value}
                </div>
              </dd>
              {description && (
                <dd className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                  {description}
                </dd>
              )}
            </dl>
          </div>
        </div>
      </div>
    </div>
  );
}