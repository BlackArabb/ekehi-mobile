import { Check, Target, Clock, Calendar, TrendingUp, Rocket } from 'lucide-react';

const roadmapPhases = [
  {
    phase: 'Phase 1: Foundation',
    period: 'Q4 2024',
    status: 'completed',
    progress: 100,
    milestones: [
      { text: 'Concept development and whitepaper v1.0', completed: true },
      { text: 'Team formation and advisory board setup', completed: true },
      { text: 'Smart contract architecture design', completed: true },
      { text: 'Initial community building (Telegram, Twitter, Discord)', completed: true },
      { text: 'Website and branding launch', completed: true },
      { text: 'Legal structure and compliance framework', completed: true },
      { text: 'Tokenomics finalization', completed: true },
      { text: 'Security audit partners selected', completed: true }
    ]
  },
  {
    phase: 'Phase 2: Development',
    period: 'Q1-Q2 2025',
    status: 'in-progress',
    progress: 65,
    milestones: [
      { text: 'Presale smart contract development', completed: true },
      { text: 'Ekehi Wallet beta launch', completed: true },
      { text: 'Staking protocol implementation', completed: true },
      { text: 'Security audit completion (CertiK)', completed: false, inProgress: true },
      { text: 'Presale campaign launch', completed: false, inProgress: true },
      { text: 'Exchange integration negotiations', completed: false, inProgress: true },
      { text: 'DEX listing preparations', completed: false },
      { text: 'Marketing campaign phase 1', completed: false }
    ]
  },
  {
    phase: 'Phase 3: Launch',
    period: 'Q3 2025',
    status: 'upcoming',
    progress: 0,
    milestones: [
      { text: 'Token Generation Event (TGE)', completed: false },
      { text: 'DEX listings (Uniswap, PancakeSwap)', completed: false },
      { text: 'CEX listings (Tier 2 exchanges)', completed: false },
      { text: 'Ekehi Exchange beta launch', completed: false },
      { text: 'NFT Marketplace development start', completed: false },
      { text: 'Strategic partnerships announcements', completed: false },
      { text: 'Community governance activation', completed: false },
      { text: 'Staking rewards program expansion', completed: false }
    ]
  },
  {
    phase: 'Phase 4: Expansion',
    period: 'Q4 2025 - 2026',
    status: 'planned',
    progress: 0,
    milestones: [
      { text: 'Tier 1 CEX listings (Binance, Coinbase target)', completed: false },
      { text: 'Ekehi DeFi Hub launch', completed: false },
      { text: 'NFT Marketplace public launch', completed: false },
      { text: 'Mobile app enhancements', completed: false },
      { text: 'Cross-chain bridge implementation', completed: false },
      { text: 'Global payment integrations', completed: false },
      { text: 'Real-world use case partnerships', completed: false },
      { text: 'Ecosystem grants program', completed: false },
      { text: 'Regulatory licenses (multiple jurisdictions)', completed: false },
      { text: '1M+ users milestone', completed: false }
    ]
  }
];

const getStatusIcon = (status: string) => {
  switch (status) {
    case 'completed':
      return <Check size={16} className="text-green-500" />;
    case 'in-progress':
      return <Target size={16} className="text-yellow-500 animate-pulse" />;
    case 'upcoming':
      return <Calendar size={16} className="text-blue-500" />;
    case 'planned':
      return <Clock size={16} className="text-gray-500" />;
    default:
      return <Clock size={16} className="text-gray-500" />;
  }
};

const getStatusColor = (status: string) => {
  switch (status) {
    case 'completed':
      return 'border-green-500 bg-green-500';
    case 'in-progress':
      return 'border-yellow-500 bg-yellow-500';
    case 'upcoming':
      return 'border-blue-500 bg-transparent';
    case 'planned':
      return 'border-gray-500 bg-transparent';
    default:
      return 'border-gray-500 bg-transparent';
  }
};

const getMilestoneIcon = (milestone: any) => {
  if (milestone.completed) {
    return <Check size={16} className="text-green-500" />;
  } else if (milestone.inProgress) {
    return <Target size={16} className="text-yellow-500 animate-pulse" />;
  } else {
    return <Clock size={16} className="text-gray-500" />;
  }
};

export default function RoadmapSection() {
  return (
    <section id="roadmap" className="section-padding bg-black">
      <div className="container">
        {/* Section Header */}
        <div className="text-center mb-12 md:mb-16">
          <h2 className="text-h2 font-display text-gradient-gold mb-4 md:mb-6">
            Roadmap
          </h2>
          <p className="text-body text-soft-white max-w-3xl mx-auto px-4 md:px-0">
            Our journey from concept to global adoption. Track our progress and see what's coming next.
          </p>
        </div>

        {/* Timeline */}
        <div className="relative max-w-4xl mx-auto">
          {/* Vertical Timeline Line */}
          <div className="absolute left-4 md:left-1/2 top-0 bottom-0 w-0.5 bg-gradient-to-b from-yellow-500 via-yellow-500/50 to-gray-500 transform md:-translate-x-1/2"></div>

          {/* Timeline Items */}
          <div className="space-y-8 md:space-y-12">
            {roadmapPhases.map((phase, index) => (
              <div
                key={index}
                className="relative flex items-start"
              >
                {/* Timeline Dot */}
                <div className={`absolute left-4 md:left-1/2 w-4 h-4 md:w-6 md:h-6 rounded-full border-2 md:border-4 transform -translate-x-1/2 md:-translate-x-1/2 ${getStatusColor(phase.status)} z-10`}>
                  {phase.status === 'in-progress' && (
                    <div className="absolute inset-0 rounded-full bg-yellow-500 animate-pulse opacity-50"></div>
                  )}
                </div>

                {/* Content Card */}
                <div className="ml-10 md:ml-0 md:w-full w-[calc(100%-2rem)]">
                  <div className="card group hover:border-yellow-500 hover:shadow-gold transition-all duration-300 p-4 md:p-6">
                    {/* Phase Header */}
                    <div className="flex flex-col md:flex-row md:items-center justify-between mb-3 md:mb-4 gap-2">
                      <div>
                        <h3 className="text-h4 md:text-h3 text-white mb-1">{phase.phase}</h3>
                        <div className="flex items-center gap-2">
                          {getStatusIcon(phase.status)}
                          <span className="text-medium-gray text-xs md:text-sm">{phase.period}</span>
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="text-xl md:text-2xl font-bold text-gradient-gold">{phase.progress}%</div>
                        <div className="text-xs text-medium-gray capitalize">{phase.status}</div>
                      </div>
                    </div>

                    {/* Progress Bar */}
                    <div className="relative h-1.5 md:h-2 bg-charcoal-gray rounded-full mb-4 md:mb-6 overflow-hidden">
                      <div 
                        className="absolute inset-0 bg-gradient-to-r from-yellow-500 to-amber-glow rounded-full transition-all duration-1000 ease-out"
                        style={{ width: `${phase.progress}%` }}
                      >
                        {phase.status === 'in-progress' && (
                          <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/20 to-transparent animate-pulse"></div>
                        )}
                      </div>
                    </div>

                    {/* Milestones */}
                    <div className="space-y-2 md:space-y-3">
                      {phase.milestones.map((milestone, idx) => (
                        <div key={idx} className="flex items-start gap-2 md:gap-3 text-xs md:text-sm">
                          {getMilestoneIcon(milestone)}
                          <span className={`${milestone.completed ? 'text-white' : 'text-soft-white'} transition-colors`}>
                            {milestone.text}
                          </span>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Long-term Vision */}
        <div className="mt-12 md:mt-16 bg-dark-slate rounded-2xl p-6 md:p-8 border border-charcoal-gray">
          <div className="text-center mb-6 md:mb-8">
            <Rocket size={32} className="text-yellow-500 mx-auto mb-3 md:mb-4" />
            <h3 className="text-h3 md:text-h3 text-white mb-3 md:mb-4">Long-term Vision (2026+)</h3>
            <p className="text-body-small md:text-body text-soft-white max-w-2xl mx-auto px-2">
              Beyond our initial roadmap, we're building the infrastructure for the future of decentralized finance.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 md:gap-6">
            {[
              'Layer 2 scaling solution',
              'Enterprise blockchain solutions', 
              'Global remittance corridors',
              'Decentralized identity platform',
              'Carbon credit marketplace',
              'Educational initiatives and blockchain academy'
            ].map((vision, index) => (
              <div key={index} className="flex items-center gap-2 md:gap-3 bg-black/40 rounded-lg p-3 md:p-4">
                <TrendingUp size={16} className="text-yellow-500 flex-shrink-0" />
                <span className="text-white text-xs md:text-sm">{vision}</span>
              </div>
            ))}
          </div>
        </div>

        {/* CTA Section */}
        <div className="text-center mt-12 md:mt-16 px-4">
          <h3 className="text-h3 md:text-h3 text-white mb-6">Ready to Join Our Journey?</h3>
          <div className="flex flex-col sm:flex-row gap-3 md:gap-4 justify-center">
            <a href="#presale" className="btn-primary text-sm md:text-base py-3 px-6 md:py-4 md:px-8">
              Join Presale Now
            </a>
            <a href="#whitepaper" className="btn-secondary text-sm md:text-base py-3 px-6 md:py-4 md:px-8">
              Download Whitepaper
            </a>
            <a href="https://t.me/ekehi_official" target="_blank" rel="noopener noreferrer" className="btn-secondary text-sm md:text-base py-3 px-6 md:py-4 md:px-8">
              Join Community
            </a>
          </div>
        </div>
      </div>
    </section>
  );
}