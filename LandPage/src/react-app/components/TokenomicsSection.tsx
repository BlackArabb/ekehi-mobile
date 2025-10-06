import { useState, useRef } from 'react';
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js';
import { Doughnut } from 'react-chartjs-2';
import { TrendingUp, Users, Shield, Zap, DollarSign, Globe, Info, ChevronDown } from 'lucide-react';

ChartJS.register(ArcElement, Tooltip, Legend);

const tokenDistribution = [
  { label: 'Presale', percentage: 40, amount: 400000000, color: '#ffa000', description: 'Available to early supporters at discounted rates', vesting: 'Unlocked at TGE' },
  { label: 'Liquidity', percentage: 7, amount: 70000000, color: '#ffb333', description: 'DEX liquidity pools and market making', vesting: 'Locked for 2 years' },
  { label: 'Team', percentage: 15, amount: 150000000, color: '#cc8000', description: 'Core team and early contributors', vesting: '1-year cliff, 3-year linear vesting' },
  { label: 'Marketing', percentage: 10, amount: 100000000, color: '#ff9800', description: 'Brand awareness and user acquisition', vesting: '6-month cliff, 2-year linear vesting' },
  { label: 'Development', percentage: 15, amount: 150000000, color: '#2196f3', description: 'Product development and ecosystem growth', vesting: 'Released quarterly based on milestones' },
  { label: 'Reserves', percentage: 10, amount: 100000000, color: '#9e9e9e', description: 'Strategic partnerships and future opportunities', vesting: 'Multi-sig controlled, community governed' },
  { label: 'Community Rewards', percentage: 3, amount: 30000000, color: '#4caf50', description: 'Airdrops, contests, and community initiatives', vesting: 'Distributed over 5 years' }
];

const tokenStats = [
  { icon: DollarSign, label: 'Total Supply', value: '1B EKH', subtext: '1,000,000,000 tokens' },
  { icon: TrendingUp, label: 'Initial Market Cap', value: '$21M', subtext: 'At $0.05 presale price' },
  { icon: Shield, label: 'Circulating Supply', value: '420M', subtext: '42% at launch' },
  { icon: Globe, label: 'Fully Diluted Value', value: '$50M', subtext: 'At $0.05 price' }
];

const tokenUtilities = [
  { icon: Users, title: 'Governance', description: 'Vote on proposals and protocol changes' },
  { icon: Zap, title: 'Staking', description: 'Earn rewards by locking tokens' },
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
        callbacks: {
          label: function(context: any) {
            return `${context.label}: ${context.parsed}% (${tokenDistribution[context.dataIndex].amount.toLocaleString()} EKH)`;
          }
        }
      }
    },
    animation: {
      animateRotate: true,
      duration: 1500
    },
    onClick: (event: any, elements: any) => {
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
                <div className="text-center">
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
              <p className="text-body-small md:text-body text-soft-white">
                Deflationary tokenomics: 0.5% of all transaction fees automatically burned, 
                reducing supply over time and increasing scarcity.
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
                  activeDistribution === index ? 'border-yellow-500 bg-dark-slate/80' : 'border-charcoal-gray'
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