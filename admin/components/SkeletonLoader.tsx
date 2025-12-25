import React from 'react';

interface SkeletonLoaderProps {
  type?: 'text' | 'avatar' | 'button' | 'card' | 'table' | 'list';
  width?: string | number;
  height?: string | number;
  count?: number;
  className?: string;
}

const SkeletonLoader: React.FC<SkeletonLoaderProps> = ({
  type = 'text',
  width,
  height,
  count = 1,
  className = ''
}) => {
  const baseClasses = 'animate-pulse bg-gray-700/50 rounded-md';
  
  const getTypeClasses = () => {
    switch (type) {
      case 'avatar':
        return 'rounded-full w-10 h-10';
      case 'button':
        return 'rounded-lg w-24 h-10';
      case 'card':
        return 'rounded-2xl w-full h-32';
      case 'table':
        return 'rounded-lg w-full h-4';
      case 'list':
        return 'rounded-lg w-full h-16';
      default:
        return `rounded-md ${width ? `w-${width}` : 'w-32'} ${height ? `h-${height}` : 'h-4'}`;
    }
  };

  const skeletons = Array.from({ length: count }, (_, index) => (
    <div
      key={index}
      className={`${baseClasses} ${getTypeClasses()} ${className}`}
      style={{
        width: width && typeof width === 'string' && !width.includes('w-') ? width : undefined,
        height: height && typeof height === 'string' && !height.includes('h-') ? height : undefined,
      }}
    />
  ));

  return <>{skeletons}</>;
};

export default SkeletonLoader;