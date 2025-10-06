import { useState } from 'react';
import { Wallet, ArrowLeftRight, Coins, Image, TrendingUp, Vote, ChevronDown, ExternalLink } from 'lucide-react';

const ecosystemProducts = [
  {
    id: 1,
    name: 'Ekehi Wallet',
    status: 'live',
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
    name: 'Ekehi Exchange',
    status: 'coming-soon',
    icon: ArrowLeftRight,
    description: 'Decentralized exchange with low fees, deep liquidity, and lightning-fast trades. Swap tokens without intermediaries.',
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
    status: 'live',
    icon: Coins,
    description: 'Earn passive income by staking your EKH tokens. Flexible and fixed-term options with competitive APY rates.',
    features: [
      'Up to 15% APY',
      'Flexible unstaking',
      'Compound rewards',
      'No minimum stake'
    ],
    cta: 'Start Staking',
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
    name: 'Ekehi DeFi Hub',
    status: 'planned',
    icon: TrendingUp,
    description: 'Complete DeFi suite including lending, borrowing, and yield optimization strategies. Maximize your crypto returns.',
    features: [
      'Lending protocols',
      'Flash loans',
      'Liquidity mining',
      'Portfolio tracker'
    ],
    cta: 'Coming Q3 2025',
    position: { x: 25, y: 85 }
  },
  {
    id: 6,
    name: 'Ekehi Governance',
    status: 'planned',
    icon: Vote,
    description: 'Decentralized governance platform where token holders shape the future. Propose, vote, and execute community decisions.',
    features: [
      'On-chain voting',
      'Proposal creation',
      'Treasury management',
      'Delegation system'
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
      return '✅ LIVE';
    case 'coming-soon':
      return '🔄 COMING SOON';
    case 'development':
      return '🛠️ IN DEVELOPMENT';
    case 'planned':
      return '📋 PLANNED';
    default:
      return 'PLANNED';
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

        {/* Desktop Ecosystem Visualization */}
        <div className="hidden lg:block relative mb-12 md:mb-16">
          <div className="relative h-80 md:h-96 max-w-4xl mx-auto">
            {/* Central Hub */}
            <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-24 h-24 md:w-32 md:h-32 bg-gradient-to-br from-yellow-500 to-amber-glow rounded-full flex items-center justify-center shadow-gold-lg animate-pulse-gold">
              <span className="text-lg md:text-2xl font-bold text-black">EKEHI</span>
            </div>

            {/* Product Nodes */}
            {ecosystemProducts.map((product) => {
              const IconComponent = product.icon;
              return (
                <div
                  key={product.id}
                  className="absolute w-40 md:w-48 transform -translate-x-1/2 -translate-y-1/2 group cursor-pointer"
                  style={{ 
                    left: `${product.position.x}%`, 
                    top: `${product.position.y}%` 
                  }}
                  onClick={() => toggleProduct(product.id)}
                >
                  {/* Connection Line */}
                  <svg className="absolute inset-0 w-full h-full pointer-events-none">
                    <line
                      x1="50%"
                      y1="50%"
                      x2="200%"
                      y2="150%"
                      stroke="rgba(255,160,0,0.3)"
                      strokeWidth="2"
                      strokeDasharray="8,4"
                      className="group-hover:stroke-yellow-500 transition-colors"
                    />
                  </svg>

                  <div className={`card group-hover:border-yellow-500 group-hover:shadow-gold transition-all duration-300 p-4 ${expandedProduct === product.id ? 'border-yellow-500 shadow-gold' : ''}`}>
                    <div className="flex items-center gap-3 mb-3">
                      <div className="w-10 h-10 bg-yellow-500/10 rounded-full flex items-center justify-center">
                        <IconComponent size={20} className="text-yellow-500" />
                      </div>
                      <div>
                        <h4 className="font-semibold text-white text-sm">{product.name}</h4>
                        <span className={`${getStatusBadge(product.status)} text-xs`}>
                          {getStatusText(product.status)}
                        </span>
                      </div>
                    </div>
                    <p className="text-xs text-soft-white line-clamp-3">
                      {product.description}
                    </p>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Mobile Product Cards */}
        <div className="lg:hidden grid grid-cols-1 md:grid-cols-2 gap-4 md:gap-6 mb-12 md:mb-16">
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
                    <div className="w-10 h-10 bg-yellow-500/10 rounded-full flex items-center justify-center group-hover:bg-yellow-500/20 transition-colors">
                      <IconComponent size={20} className="text-yellow-500" />
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
                
                <button className={`w-full ${product.status === 'live' ? 'btn-primary' : 'btn-secondary'} text-sm py-2.5`}>
                  {product.cta}
                </button>
              </div>
            );
          })}
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