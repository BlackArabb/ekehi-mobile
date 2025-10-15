import { useState, useRef } from 'react';
import { Chart as ChartJS, ArcElement, Tooltip, Legend, Title } from 'chart.js';
import { Doughnut } from 'react-chartjs-2';
import { TrendingUp, Users, Shield, Zap, DollarSign, Globe, ChevronDown } from 'lucide-react';

ChartJS.register(ArcElement, Tooltip, Legend, Title);

const tokenDistribution = [
  { label: 'Community', percentage: 50, amount: 500000000, color: '#ffa000', description: 'Available to community members and early supporters', vesting: 'Unlocked at TGE' },
  { label: 'Public Sale', percentage: 20, amount: 200000000, color: '#ffb333', description: 'Available to public investors during token sale', vesting: 'Unlocked at TGE' },
  { label: 'Liquidity', percentage: 10, amount: 100000000, color: '#ff9800', description: 'DEX liquidity pools and market making', vesting: 'Locked for 2 years' },
  { label: 'Reserve', percentage: 10, amount: 100000000, color: '#9e9e9e', description: 'Strategic partnerships and future opportunities', vesting: 'Multi-sig controlled, community governed' },
  { label: 'Team', percentage: 5, amount: 50000000, color: '#cc8000', description: 'Core team and early contributors', vesting: '1-year cliff, 3-year linear vesting' },
  { label: 'Staking', percentage: 5, amount: 50000000, color: '#4caf50', description: 'Staking rewards for network security', vesting: 'Released over 5 years' }
];

const tokenStats = [
  { icon: DollarSign, label: 'Total Supply', value: '1B EKH', subtext: '1,000,000,000 tokens' },
  { icon: TrendingUp, label: 'Initial Market Cap', value: '$60M', subtext: 'At $0.1 initial price' },
  { icon: Shield, label: 'Circulating Supply', value: '750M', subtext: '20% at launch' },
  { icon: Globe, label: 'Fully Diluted Value', value: '$50M', subtext: 'At $0.1 price' }
];

const tokenUtilities = [
  { icon: Users, title: 'Governance', description: 'Vote on proposals and protocol changes' },
  { icon: Zap, title: 'Staking', description: 'Coming soon - Earn rewards by locking tokens' },
  { icon: DollarSign, title: 'Fee Discounts', description: 'Reduced fees across Ekehi ecosystem' },
  { icon: TrendingUp, title: 'Liquidity Mining', description: 'Earn additional tokens by providing liquidity' }
];

export default function TokenomicsSection() {
  const [activeDistribution, setActiveDistribution] = useState(0);
  const [expandedUtility, setExpandedUtility] = useState<number | null>(null);
  const chartRef = useRef<ChartJS<'doughnut'> | null>(null);

  const chartData = {
    labels: tokenDistribution.map(item => item.label),
    datasets: [
      {
        data: tokenDistribution.map(item => item.percentage),
        backgroundColor: tokenDistribution.map(item => item.color),
        borderColor: '#000000',
        borderWidth: 2,
        hoverOffset: 10
      }
    ]
  };

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false
      },
      tooltip: {
        backgroundColor: '#1a1a1a',
        titleColor: '#ffffff',
        bodyColor: '#f5f5f5',
        borderColor: '#ffa000',
        borderWidth: 1,
        cornerRadius: 8,
        displayColors: true,
        callbacks: {
          label: function(context: any) {
            const item = tokenDistribution[context.dataIndex];
            return [
              `${context.label}: ${context.parsed}%`,
              `${item.amount.toLocaleString()} EKH`,
              `Vesting: ${item.vesting}`
            ];
          }
        }
      },
      title: {
        display: true,
        text: 'EKEHI Token Distribution',
        color: '#ffffff',
        font: {
          size: 16,
          family: 'Space Grotesk, sans-serif'
        },
        padding: {
          top: 10,
          bottom: 30
        }
      }
    },
    animation: {
      animateRotate: true,
      animateScale: true,
      duration: 1500
    },
    hover: {
      mode: 'nearest' as const,
      intersect: true,
      onHover: (event: any, chartElement: any) => {
        event.native.target.style.cursor = chartElement[0] ? 'pointer' : 'default';
      }
    },
    onClick: (_event: any, elements: any) => {
      if (elements.length > 0) {
        const index = elements[0].index;
        setActiveDistribution(index);
      }
    }
  };

  const toggleUtility = (index: number) => {
    setExpandedUtility(expandedUtility === index ? null : index);
  };

  return (
    <section id="tokenomics" className="section-padding bg-rich-charcoal">
      <div className="container">
        {/* Section Header */}
        <div className="text-center mb-12 md:mb-16">
          <h2 className="text-h2 font-display text-gradient-gold mb-4 md:mb-6">
            Tokenomics
          </h2>
          <p className="text-body text-soft-white max-w-3xl mx-auto px-4 md:px-0">
            Transparent and sustainable token economics designed for long-term growth and community benefit.
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 md:gap-12 mb-12 md:mb-16">
          {/* Chart Section */}
          <div className="flex flex-col items-center">
            <div className="relative w-64 h-64 md:w-80 md:h-80 mb-6 md:mb-8">
              <Doughnut ref={chartRef} data={chartData} options={chartOptions} />
              <div className="absolute inset-0 flex items-center justify-center">
                <div className="text-center align-middle">
                  <div className="text-2xl md:text-3xl font-bold text-gradient-gold">{tokenDistribution[activeDistribution].percentage}%</div>
                  <div className="text-medium-gray text-xs md:text-sm">{tokenDistribution[activeDistribution].label}</div>
                </div>
              </div>
            </div>

            {/* Burn Mechanism */}
            <div className="bg-black/40 rounded-xl p-5 md:p-6 border border-charcoal-gray max-w-md w-full">
              <h4 className="text-lg font-semibold text-white mb-3 flex items-center gap-2">
                🔥 Burn Mechanism
              </h4>
              <p className="text-body-small md:text-body text-soft-white mb-4">
                Deflationary tokenomics with transparent fee distribution:
              </p>
              <div className="space-y-2">
                <div className="flex justify-between items-center">
                  <span className="text-soft-white">50% Burnt</span>
                  <span className="text-yellow-500 font-medium">Supply Reduction</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-soft-white">25% Liquidity Pool</span>
                  <span className="text-blue-500 font-medium">Market Stability</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-soft-white">25% Ekehi Foundation</span>
                  <span className="text-green-500 font-medium">Community Growth</span>
                </div>
              </div>
              <p className="text-xs text-medium-gray mt-3">
                0.5% of all transaction fees automatically distributed according to this mechanism
              </p>
            </div>
          </div>

          {/* Distribution Details */}
          <div className="space-y-4">
            <h3 className="text-h3 text-white mb-4">Token Distribution</h3>
            {tokenDistribution.map((item, index) => (
              <div 
                key={index} 
                className={`bg-dark-slate rounded-lg p-4 border-l-4 transition-all duration-300 cursor-pointer hover:bg-dark-slate/80 ${
                  activeDistribution === index ? 'border-yellow-500 bg-dark-slate/80 shadow-gold' : 'border-charcoal-gray'
                }`}
                style={{ borderLeftColor: item.color }}
                onClick={() => setActiveDistribution(index)}
              >
                <div className="flex justify-between items-start mb-2">
                  <div className="flex items-center gap-3">
                    <div className="w-4 h-4 rounded-full" style={{ backgroundColor: item.color }}></div>
                    <h4 className="font-semibold text-white">{item.label}</h4>
                  </div>
                  <div className="text-right">
                    <div className="text-lg font-bold text-gradient-gold">{item.percentage}%</div>
                    <div className="text-xs text-medium-gray">{item.amount.toLocaleString()} EKH</div>
                  </div>
                </div>
                <p className="text-soft-white text-sm mb-2">{item.description}</p>
                <div className="text-xs text-medium-gray">
                  <strong>Vesting:</strong> {item.vesting}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Token Statistics */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 md:gap-6 mb-12 md:mb-16">
          {tokenStats.map((stat, index) => {
            const IconComponent = stat.icon;
            return (
              <div key={index} className="bg-black border border-yellow-500 rounded-xl p-5 md:p-6 text-center hover:shadow-gold transition-all duration-300 group">
                <IconComponent size={28} className="md:size-32 text-yellow-500 mx-auto mb-3 group-hover:scale-110 transition-transform" />
                <div className="text-xl md:text-2xl font-bold text-gradient-gold mb-1">{stat.value}</div>
                <div className="text-white font-medium text-sm mb-1">{stat.label}</div>
                <div className="text-medium-gray text-xs">{stat.subtext}</div>
              </div>
            );
          })}
        </div>

        {/* Token Utility */}
        <div className="bg-dark-slate rounded-2xl p-6 md:p-8 border border-charcoal-gray">
          <h3 className="text-h3 text-center text-white mb-6 md:mb-8">Token Utility</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 md:gap-6">
            {tokenUtilities.map((utility, index) => {
              const IconComponent = utility.icon;
              const isExpanded = expandedUtility === index;
              
              return (
                <div 
                  key={index} 
                  className="bg-black/20 rounded-xl p-5 cursor-pointer transition-all duration-300 hover:border-yellow-500 border border-charcoal-gray"
                  onClick={() => toggleUtility(index)}
                >
                  <div className="flex items-center justify-between mb-3">
                    <div className="flex items-center gap-3">
                      <div className="w-12 h-12 bg-yellow-500/10 rounded-full flex items-center justify-center group-hover:bg-yellow-500/20 transition-colors">
                        <IconComponent size={24} className="text-yellow-500" />
                      </div>
                      <h4 className="font-semibold text-white">{utility.title}</h4>
                    </div>
                    <ChevronDown 
                      size={20} 
                      className={`text-yellow-500 transition-transform duration-300 ${isExpanded ? 'rotate-180' : ''}`} 
                    />
                  </div>
                  <p className={`text-soft-white text-sm transition-all duration-300 ${isExpanded ? 'line-clamp-none' : 'line-clamp-2'}`}>
                    {utility.description}
                  </p>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </section>
  );
}