import { useState } from 'react';
import { Wallet, ArrowLeftRight, Coins, Image, Building, Users, ChevronDown } from 'lucide-react';

const ecosystemProducts = [
  {
    id: 1,
    name: 'Ekehi Wallet',
    status: 'coming-soon',
    icon: Wallet,
    description: 'Secure, non-custodial wallet supporting multiple chains. Store, send, and receive EKH and other cryptocurrencies with ease.',
    features: [
      'Multi-chain support',
      'Built-in DEX integration', 
      'Hardware wallet compatible',
      'Mobile & desktop apps'
    ],
    cta: 'Download Wallet',
    position: { x: 20, y: 20 }
  },
  {
    id: 2,
    name: 'Ekehi Swap',
    status: 'coming-soon',
    icon: ArrowLeftRight,
    description: 'Instantly swap tokens with minimal fees and maximum security. Our AMM-based decentralized exchange ensures deep liquidity and seamless trading experiences.',
    features: [
      'Automated market making',
      'Yield farming pools',
      'Zero-knowledge privacy',
      'Cross-chain bridges'
    ],
    cta: 'Join Waitlist',
    position: { x: 70, y: 15 }
  },
  {
    id: 3,
    name: 'Ekehi Staking',
    status: 'coming-soon',
    icon: Coins,
    description: 'Earn passive income by staking your EKH tokens. Flexible and fixed-term options with competitive APY rates.',
    features: [
      'Up to 15% APY',
      'Flexible unstaking',
      'Compound rewards',
      'No minimum stake'
    ],
    cta: 'Coming Soon',
    position: { x: 15, y: 65 }
  },
  {
    id: 4,
    name: 'Ekehi NFT Marketplace',
    status: 'development',
    icon: Image,
    description: 'Create, buy, and sell NFTs on a sustainable blockchain. Support artists and creators in the Web3 economy.',
    features: [
      'Lazy minting',
      'Royalty automation',
      'Multi-format support',
      'Creator launchpad'
    ],
    cta: 'Learn More',
    position: { x: 75, y: 70 }
  },
  {
    id: 5,
    name: 'Ekehi RWA Tokenization',
    status: 'planned',
    icon: Building,
    description: 'EKEHI RWA tokenization digitizes real-world assets on the EKEHI blockchain, making them easier to trade, increasing liquidity, and broadening investor access. It streamlines asset management and enables fractional ownership, enhancing efficiency and transparency in transactions.',
    features: [
      'Real estate tokenization',
      'Commodity-backed tokens',
      'Fractional ownership',
      'Asset-backed lending'
    ],
    cta: 'Coming Q3 2025',
    position: { x: 25, y: 85 }
  },
  {
    id: 6,
    name: 'Ekehi Foundation',
    status: 'planned',
    icon: Users,
    description: 'The EKEHI Foundation is a charity leveraging blockchain for efficient and transparent giving, aiming to maximize the impact of donations to address social and humanitarian issues globally.',
    features: [
      'Blockchain education programs',
      'Sustainability grants',
      'Community development',
      'Open-source research'
    ],
    cta: 'Coming Q4 2025',
    position: { x: 65, y: 85 }
  }
];

const getStatusBadge = (status: string) => {
  switch (status) {
    case 'live':
      return 'badge-live';
    case 'coming-soon':
      return 'badge-coming-soon';
    case 'development':
      return 'badge-development';
    case 'planned':
      return 'badge-planned';
    default:
      return 'badge-planned';
  }
};

const getStatusText = (status: string) => {
  switch (status) {
    case 'live':
      return 'Live';
    case 'coming-soon':
      return 'Coming Soon';
    case 'development':
      return 'In Development';
    case 'planned':
      return 'Planned';
    default:
      return 'Planned';
  }
};

const getStatusColor = (status: string) => {
  switch (status) {
    case 'live':
      return 'border-green-500';
    case 'coming-soon':
      return 'border-yellow-500';
    case 'development':
      return 'border-blue-500';
    case 'planned':
      return 'border-gray-500';
    default:
      return 'border-gray-500';
  }
};

export default function EcosystemSection() {
  const [expandedProduct, setExpandedProduct] = useState<number | null>(null);

  const toggleProduct = (id: number) => {
    setExpandedProduct(expandedProduct === id ? null : id);
  };

  return (
    <section id="ecosystem" className="section-padding bg-black">
      <div className="container">
        {/* Section Header */}
        <div className="text-center mb-12 md:mb-16">
          <h2 className="text-h2 font-display text-gradient-gold mb-4 md:mb-6">
            The Ekehi Ecosystem
          </h2>
          <p className="text-body text-soft-white max-w-3xl mx-auto px-4 md:px-0 mb-6 md:mb-8">
            A connected universe of products and services built to empower users, creators, 
            and developers in the decentralized economy.
          </p>
          
          {/* Value Proposition */}
          <div className="bg-dark-slate rounded-xl p-5 md:p-6 border border-charcoal-gray max-w-4xl mx-auto">
            <p className="text-body-small md:text-body text-soft-white">
              All ecosystem products interconnected, creating a seamless experience. 
              Your EKH tokens work across all platforms, earning rewards and unlocking features as the ecosystem grows.
            </p>
          </div>
        </div>

        {/* Desktop Ecosystem Visualization - COMPLETELY RESTRUCTURED to fix overlapping issue */}
        <div className="hidden lg:block mb-12 md:mb-16">
          {/* Central Hub with Logo - ENHANCED DESIGN */}
          <div className="text-center mb-10">
            <div className="inline-flex items-center justify-center w-24 h-24 md:w-32 md:h-32 rounded-full border-4 border-yellow-500 bg-transparent shadow-gold-lg animate-pulse-glow mx-auto p-2 transition-transform duration-300 hover:scale-105">
              <img 
                src="/logo.png" 
                alt="EKEHI Logo" 
                className="w-20 h-20 md:w-28 md:h-28 object-contain logo-glow"
              />
            </div>
            <h3 className="text-xl md:text-2xl font-bold text-gradient-gold mt-4">EKEHI ECOSYSTEM</h3>
            <p className="text-medium-gray text-sm md:text-base mt-2 max-w-md mx-auto">
              The heart of our interconnected universe of products and services
            </p>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 max-w-6xl mx-auto">
            {ecosystemProducts.map((product) => {
              const IconComponent = product.icon;
              const isExpanded = expandedProduct === product.id;
              
              return (
                <div 
                  key={product.id} 
                  className={`card group cursor-pointer transition-all duration-300 ${isExpanded ? 'border-yellow-500 shadow-gold' : ''}`}
                  onClick={() => toggleProduct(product.id)}
                >
                  <div className="flex items-center justify-between mb-4">
                    <div className="flex items-center gap-3">
                      <div className="w-12 h-12 bg-yellow-500/10 rounded-full flex items-center justify-center group-hover:bg-yellow-500/20 transition-colors">
                        <IconComponent size={24} className="text-yellow-500" />
                      </div>
                      <div>
                        <h3 className="text-lg font-semibold text-white">{product.name}</h3>
                        <span className={getStatusBadge(product.status)}>
                          {getStatusText(product.status)}
                        </span>
                      </div>
                    </div>
                    <ChevronDown 
                      size={20} 
                      className={`text-yellow-500 transition-transform duration-300 ${isExpanded ? 'rotate-180' : ''}`} 
                    />
                  </div>
                  
                  <p className={`text-soft-white mb-4 transition-all duration-300 ${isExpanded ? 'line-clamp-none' : 'line-clamp-2'}`}>
                    {product.description}
                  </p>
                  
                  {isExpanded && (
                    <div className="mb-6">
                      <h4 className="text-sm font-semibold text-white mb-2">Features:</h4>
                      <ul className="grid grid-cols-1 gap-2 text-sm text-medium-gray">
                        {product.features.map((feature, idx) => (
                          <li key={idx} className="flex items-center gap-2">
                            <span className="w-1.5 h-1.5 bg-yellow-500 rounded-full"></span>
                            {feature}
                          </li>
                        ))}
                      </ul>
                    </div>
                  )}
                  
                  <button className={`w-full sm:w-auto ${product.status === 'live' ? 'btn-primary' : 'btn-secondary'} text-sm py-2.5 centered-button`}>
                    {product.cta}
                  </button>

                </div>
              );
            })}
          </div>
        </div>

        {/* Mobile Product Cards - ENHANCED layout */}
        <div className="lg:hidden grid grid-cols-1 gap-4 md:gap-6 mb-12 md:mb-16">
          {/* Central Hub with Logo for mobile - ENHANCED DESIGN */}
          <div className="text-center mb-8">
            <div className="inline-flex items-center justify-center w-20 h-20 rounded-full border-4 border-yellow-500 bg-transparent shadow-gold-lg animate-pulse-glow mx-auto p-2 transition-transform duration-300 hover:scale-105">
              <img 
                src="/logo.png" 
                alt="EKEHI Logo" 
                className="w-16 h-16 object-contain logo-glow"
              />
            </div>
            <h3 className="text-lg font-bold text-gradient-gold mt-3">EKEHI ECOSYSTEM</h3>
            <p className="text-medium-gray text-xs md:text-sm mt-1 max-w-xs mx-auto">
              The heart of our interconnected universe
            </p>
          </div>
          
          <div className="grid grid-cols-1 gap-4">
            {ecosystemProducts.map((product) => {
              const IconComponent = product.icon;
              const isExpanded = expandedProduct === product.id;
              
              return (
                <div 
                  key={product.id} 
                  className={`card group cursor-pointer transition-all duration-300 ${isExpanded ? 'border-yellow-500 shadow-gold' : ''}`}
                  onClick={() => toggleProduct(product.id)}
                >
                  <div className="flex items-center justify-between mb-4">
                    <div className="flex items-center gap-3">
                      <div className="w-12 h-12 bg-yellow-500/10 rounded-full flex items-center justify-center group-hover:bg-yellow-500/20 transition-colors">
                        <IconComponent size={24} className="text-yellow-500" />
                      </div>
                      <div>
                        <h3 className="text-lg font-semibold text-white">{product.name}</h3>
                        <span className={getStatusBadge(product.status)}>
                          {getStatusText(product.status)}
                        </span>
                      </div>
                    </div>
                    <ChevronDown 
                      size={20} 
                      className={`text-yellow-500 transition-transform duration-300 ${isExpanded ? 'rotate-180' : ''}`} 
                    />
                  </div>
                  
                  <p className={`text-soft-white mb-4 transition-all duration-300 ${isExpanded ? 'line-clamp-none' : 'line-clamp-2'}`}>
                    {product.description}
                  </p>
                  
                  {isExpanded && (
                    <div className="mb-6">
                      <h4 className="text-sm font-semibold text-white mb-2">Features:</h4>
                      <ul className="grid grid-cols-1 gap-2 text-sm text-medium-gray">
                        {product.features.map((feature, idx) => (
                          <li key={idx} className="flex items-center gap-2">
                            <span className="w-1.5 h-1.5 bg-yellow-500 rounded-full"></span>
                            {feature}
                          </li>
                        ))}
                      </ul>
                    </div>
                  )}
                  
                  <button className={`w-full sm:w-auto ${product.status === 'live' ? 'btn-primary' : 'btn-secondary'} text-sm py-2.5 centered-button`}>
                    {product.cta}
                  </button>

                </div>
              );
            })}
          </div>
        </div>

        {/* Statistics */}
        <div className="bg-dark-slate rounded-2xl p-6 md:p-8 border border-charcoal-gray">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-6 md:gap-8 text-center">
            <div className="group">
              <div className="text-2xl md:text-3xl font-bold text-gradient-gold mb-2 group-hover:scale-110 transition-transform">6</div>
              <div className="text-medium-gray text-xs md:text-sm">Products Planned</div>
            </div>
            <div className="group">
              <div className="text-2xl md:text-3xl font-bold text-gradient-gold mb-2 group-hover:scale-110 transition-transform">2</div>
              <div className="text-medium-gray text-xs md:text-sm">Currently Live</div>
            </div>
            <div className="group">
              <div className="text-2xl md:text-3xl font-bold text-gradient-gold mb-2 group-hover:scale-110 transition-transform">100%</div>
              <div className="text-medium-gray text-xs md:text-sm">Interconnected</div>
            </div>
            <div className="group">
              <div className="text-2xl md:text-3xl font-bold text-gradient-gold mb-2 group-hover:scale-110 transition-transform">1</div>
              <div className="text-medium-gray text-xs md:text-sm">Unified Token</div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}