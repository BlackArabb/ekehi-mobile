import { useState, useEffect } from 'react';
import { Download, FileText, ChevronRight, BookOpen, Zap, Users, Shield, TrendingUp, Leaf } from 'lucide-react';

// Sample whitepaper content structure
const whitepaperContent = {
  title: "Ekehi Whitepaper v2.0",
  subtitle: "A Sustainable Approach to Decentralized Finance",
  version: "2.0",
  lastUpdated: "January 2025",
  chapters: [
    {
      id: 1,
      title: "Introduction",
      icon: BookOpen,
      content: `
        <h3 class="text-xl font-bold text-white mb-4">1.1 What is Ekehi?</h3>
        <p class="text-soft-white mb-4">Ekehi represents a revolutionary approach to cryptocurrency and decentralized finance, built on principles of sustainability, transparency, and community empowerment. Our mission is to create a blockchain ecosystem that minimizes environmental impact while maximizing user benefits.</p>
        
        <h3 class="text-xl font-bold text-white mb-4 mt-6">1.2 The Problem with Current Blockchains</h3>
        <p class="text-soft-white mb-4">Traditional blockchain networks consume enormous amounts of energy, contributing to environmental degradation. Additionally, many projects lack transparency and community governance, leading to centralized control and potential misuse of funds.</p>
        
        <h3 class="text-xl font-bold text-white mb-4 mt-6">1.3 Our Solution</h3>
        <p class="text-soft-white mb-4">Ekehi addresses these issues through energy-efficient consensus mechanisms, transparent operations, and community-driven governance. We're building a sustainable future for decentralized finance.</p>
      `
    },
    {
      id: 2,
      title: "Technology Overview",
      icon: Zap,
      content: `
        <h3 class="text-xl font-bold text-white mb-4">2.1 Consensus Mechanism</h3>
        <p class="text-soft-white mb-4">Ekehi utilizes a Proof-of-Stake variant called EcoStake, which reduces energy consumption by 99.9% compared to traditional Proof-of-Work systems. Validators are chosen based on their token holdings and staking duration.</p>
        
        <h3 class="text-xl font-bold text-white mb-4 mt-6">2.2 Network Architecture</h3>
        <p class="text-soft-white mb-4">Our network consists of three layers:</p>
        <ul class="list-disc list-inside text-soft-white mb-4 ml-4">
          <li>Consensus Layer: Handles transaction validation and network security</li>
          <li>Execution Layer: Processes smart contracts and dApp interactions</li>
          <li>Settlement Layer: Manages cross-chain transactions and liquidity</li>
        </ul>
        
        <h3 class="text-xl font-bold text-white mb-4 mt-6">2.3 Security Features</h3>
        <p class="text-soft-white mb-4">Ekehi implements multi-layer security protocols including:</p>
        <ul class="list-disc list-inside text-soft-white mb-4 ml-4">
          <li>Formal verification of smart contracts</li>
          <li>Regular third-party security audits</li>
          <li>Decentralized validator network</li>
          <li>Automated bug bounty program</li>
        </ul>
      `
    },
    {
      id: 3,
      title: "Tokenomics",
      icon: TrendingUp,
      content: `
        <h3 class="text-xl font-bold text-white mb-4">3.1 Token Distribution</h3>
        <p class="text-soft-white mb-4">Ekehi has a total supply of 1 billion tokens distributed as follows:</p>
        <ul class="list-disc list-inside text-soft-white mb-4 ml-4">
          <li>Community Allocation: 40% (400,000,000 EKH)</li>
          <li>Liquidity: 7% (70,000,000 EKH)</li>
          <li>Team: 15% (150,000,000 EKH)</li>
          <li>Marketing: 10% (100,000,000 EKH)</li>
          <li>Development: 15% (150,000,000 EKH)</li>
          <li>Reserves: 10% (100,000,000 EKH)</li>
          <li>Community Rewards: 3% (30,000,000 EKH)</li>
        </ul>
        
        <h3 class="text-xl font-bold text-white mb-4 mt-6">3.2 Token Utility</h3>
        <p class="text-soft-white mb-4">EKH tokens serve multiple purposes within the ecosystem:</p>
        <ul class="list-disc list-inside text-soft-white mb-4 ml-4">
          <li>Governance: Vote on protocol changes and upgrades</li>
          <li>Staking: Coming soon - Earn rewards by securing the network</li>
          <li>Fee Discounts: Reduced transaction fees across all platforms</li>
          <li>Liquidity Mining: Earn additional tokens by providing liquidity</li>
        </ul>
        
        <h3 class="text-xl font-bold text-white mb-4 mt-6">3.3 Deflationary Mechanism</h3>
        <p class="text-soft-white mb-4">0.5% of all transaction fees are automatically burned, reducing the total supply over time and increasing token scarcity.</p>
      `
    },
    {
      id: 4,
      title: "Roadmap",
      icon: ChevronRight,
      content: `
        <h3 class="text-xl font-bold text-white mb-4">4.1 Phase 1: Foundation (Q4 2024)</h3>
        <ul class="list-disc list-inside text-soft-white mb-4 ml-4">
          <li>Concept development and whitepaper v1.0</li>
          <li>Team formation and advisory board setup</li>
          <li>Smart contract architecture design</li>
          <li>Initial community building</li>
        </ul>
        
        <h3 class="text-xl font-bold text-white mb-4 mt-6">4.2 Phase 2: Development (Q1-Q2 2025)</h3>
        <ul class="list-disc list-inside text-soft-white mb-4 ml-4">
          <li>Token contract development</li>
          <li>Ekehi Wallet beta launch</li>
          <li>Staking protocol implementation and testing</li>
          <li>Security audit completion</li>
        </ul>
        
        <h3 class="text-xl font-bold text-white mb-4 mt-6">4.3 Phase 3: Launch (Q3 2025)</h3>
        <ul class="list-disc list-inside text-soft-white mb-4 ml-4">
          <li>Token Generation Event (TGE)</li>
          <li>DEX listings</li>
          <li>Ekehi Exchange beta launch</li>
          <li>Community governance activation</li>
        </ul>
      `
    },
    {
      id: 5,
      title: "Team & Advisors",
      icon: Users,
      content: `
        <h3 class="text-xl font-bold text-white mb-4">5.1 Core Team</h3>
        <p class="text-soft-white mb-4">Our team consists of experienced blockchain developers, financial experts, and sustainability advocates:</p>
        <ul class="list-disc list-inside text-soft-white mb-4 ml-4">
          <li>Alex Chen - CEO & Lead Developer (10+ years in blockchain)</li>
          <li>Sarah Johnson - CFO & Financial Strategist (Former investment banker)</li>
          <li>Miguel Rodriguez - CTO & Security Expert (Cybersecurity specialist)</li>
          <li>Dr. Yuki Tanaka - Sustainability Advisor (Environmental scientist)</li>
        </ul>
        
        <h3 class="text-xl font-bold text-white mb-4 mt-6">5.2 Advisory Board</h3>
        <p class="text-soft-white mb-4">We're supported by industry leaders and experts:</p>
        <ul class="list-disc list-inside text-soft-white mb-4 ml-4">
          <li>Prof. Michael Roberts - Blockchain Academic (MIT)</li>
          <li>Lisa Wang - DeFi Specialist (Former Uniswap team)</li>
          <li>David Kim - Regulatory Advisor (Former SEC attorney)</li>
        </ul>
      `
    },
    {
      id: 6,
      title: "Risk Factors",
      icon: Shield,
      content: `
        <h3 class="text-xl font-bold text-white mb-4">6.1 Market Risks</h3>
        <p class="text-soft-white mb-4">Cryptocurrency markets are highly volatile and subject to rapid price fluctuations. Investors should be prepared for potential losses.</p>
        
        <h3 class="text-xl font-bold text-white mb-4 mt-6">6.2 Regulatory Risks</h3>
        <p class="text-soft-white mb-4">Changing regulations in various jurisdictions may impact the development and adoption of Ekehi. We are actively working with legal experts to ensure compliance.</p>
        
        <h3 class="text-xl font-bold text-white mb-4 mt-6">6.3 Technical Risks</h3>
        <p class="text-soft-white mb-4">Despite rigorous testing, smart contracts may contain vulnerabilities. We conduct regular audits and maintain a bug bounty program to mitigate these risks.</p>
        
        <h3 class="text-xl font-bold text-white mb-4 mt-6">6.4 Competition Risks</h3>
        <p class="text-soft-white mb-4">The blockchain space is highly competitive. Ekehi differentiates itself through sustainability, transparency, and community focus.</p>
      `
    }
  ]
};

const highlights = [
  {
    icon: Leaf,
    stat: '99.9% Less Energy',
    description: 'Compared to traditional proof-of-work systems',
    color: 'text-green-500'
  },
  {
    icon: Users,
    stat: '1 Token = 1 Vote',
    description: 'Democratic decision-making for all holders',
    color: 'text-blue-500'
  },
  {
    icon: TrendingUp,
    stat: '$0.001 Fees',
    description: 'Making DeFi accessible to everyone',
    color: 'text-yellow-500'
  },
  {
    icon: Shield,
    stat: '100% Open',
    description: 'All operations and funds publicly auditable',
    color: 'text-purple-500'
  }
];

const statistics = [
  { label: '1B Total Supply', value: '1B EKH' },
  { label: 'Ecosystem Focus', value: '6 Products' },
  { label: '150+ Validators', value: '150+' },
  { label: '0.5% Burn Rate', value: '0.5%' }
];

const documentDetails = {
  pages: 45,
  size: '2.4 MB',
  version: '2.0',
  lastUpdated: 'January 2025'
};

export default function WhitepaperSection() {
  const [activeChapter, setActiveChapter] = useState(1);
  const [isScrolled, setIsScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 50);
    };

    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  // Get the active chapter content
  const activeChapterContent = whitepaperContent.chapters.find(chapter => chapter.id === activeChapter) || whitepaperContent.chapters[0];

  return (
    <section id="whitepaper" className="section-padding bg-black">
      <div className="container">
        {/* Section Header */}
        <div className="text-center mb-12 md:mb-16">
          <h2 className="text-h2 font-display text-gradient-gold mb-4 md:mb-6">
            Whitepaper
          </h2>
          <p className="text-body text-soft-white max-w-3xl mx-auto px-4 md:px-0">
            Explore our comprehensive whitepaper to understand the technology, vision, 
            and roadmap behind Ekehi. Download or read online to learn how we're revolutionizing cryptocurrency.
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-5 gap-8 md:gap-12 mb-12 md:mb-16">
          {/* Table of Contents */}
          <div className="lg:col-span-2">
            <div className={`sticky top-20 transition-all duration-300 ${isScrolled ? 'pt-4' : ''}`}>
              <h3 className="text-h3 text-white mb-6">Table of Contents</h3>
              <div className="space-y-3">
                {whitepaperContent.chapters.map((chapter) => {
                  const IconComponent = chapter.icon;
                  return (
                    <div
                      key={chapter.id}
                      className={`flex items-center gap-4 p-4 rounded-xl transition-all duration-300 cursor-pointer group ${
                        activeChapter === chapter.id 
                          ? 'bg-yellow-500/10 border border-yellow-500' 
                          : 'bg-dark-slate border border-charcoal-gray hover:border-yellow-500 hover:bg-yellow-500/5'
                      }`}
                      onClick={() => setActiveChapter(chapter.id)}
                    >
                      <div className={`w-10 h-10 rounded-full flex items-center justify-center group-hover:scale-110 transition-transform ${
                        activeChapter === chapter.id 
                          ? 'bg-yellow-500 text-black' 
                          : 'bg-black/50 text-yellow-500'
                      }`}>
                        <IconComponent size={20} />
                      </div>
                      <span className={`font-medium transition-colors ${
                        activeChapter === chapter.id 
                          ? 'text-yellow-500' 
                          : 'text-white group-hover:text-yellow-500'
                      }`}>
                        {chapter.title}
                      </span>
                    </div>
                  );
                })}
              </div>

              {/* Document Details */}
              <div className="mt-8 bg-dark-slate rounded-xl p-5 md:p-6 border border-charcoal-gray">
                <h4 className="text-lg font-semibold text-white mb-4">Document Details</h4>
                <div className="grid grid-cols-2 gap-3 md:gap-4 text-xs md:text-sm">
                  <div className="flex justify-between">
                    <span className="text-medium-gray">Pages:</span>
                    <span className="text-white ml-2">{documentDetails.pages}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-medium-gray">Size:</span>
                    <span className="text-white ml-2">{documentDetails.size}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-medium-gray">Version:</span>
                    <span className="text-white ml-2">{documentDetails.version}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-medium-gray">Updated:</span>
                    <span className="text-white ml-2">{documentDetails.lastUpdated}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Document Content */}
          <div className="lg:col-span-3">
            <div className="bg-dark-slate border-2 border-yellow-500 rounded-2xl md:rounded-3xl p-6 md:p-8 shadow-gold-lg mb-8">
              <div className="mb-6 md:mb-8">
                <div className="flex items-center gap-3 mb-4">
                  <FileText size={32} className="text-yellow-500" />
                  <div>
                    <h3 className="text-h3 text-white">{whitepaperContent.title}</h3>
                    <p className="text-medium-gray">{whitepaperContent.subtitle}</p>
                  </div>
                </div>
                
                <div className="flex flex-wrap gap-2 mb-6">
                  <span className="bg-yellow-500/10 text-yellow-500 px-3 py-1 rounded-full text-xs">
                    Version {whitepaperContent.version}
                  </span>
                  <span className="bg-blue-500/10 text-blue-500 px-3 py-1 rounded-full text-xs">
    Updated {whitepaperContent.lastUpdated}
  </span>
                </div>
                
                {/* Chapter Content */}
                <div className="prose prose-invert max-w-none">
                  <div 
                    className="text-soft-white"
                    dangerouslySetInnerHTML={{ __html: activeChapterContent.content }} 
                  />
                </div>
              </div>
              
              {/* Navigation */}
              <div className="flex justify-between items-center pt-6 border-t border-charcoal-gray">
                <button 
                  onClick={() => setActiveChapter(Math.max(1, activeChapter - 1))}
                  disabled={activeChapter === 1}
                  className="btn-secondary py-2 px-4 text-sm disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  ← Previous
                </button>
                
                <div className="text-medium-gray text-sm">
                  Chapter {activeChapter} of {whitepaperContent.chapters.length}
                </div>
                
                <button 
                  onClick={() => setActiveChapter(Math.min(whitepaperContent.chapters.length, activeChapter + 1))}
                  disabled={activeChapter === whitepaperContent.chapters.length}
                  className="btn-secondary py-2 px-4 text-sm disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Next →
                </button>
              </div>
            </div>

            {/* Key Highlights */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 md:gap-6">
              {highlights.map((highlight, index) => {
                const IconComponent = highlight.icon;
                return (
                  <div key={index} className="card group p-5 md:p-6 hover:border-yellow-500 transition-all duration-300">
                    <div className="flex items-center gap-3 md:gap-4 mb-3 md:mb-4">
                      <div className={`w-10 h-10 md:w-12 md:h-12 rounded-full bg-black/50 flex items-center justify-center group-hover:scale-110 transition-transform`}>
                        <IconComponent size={20} className={`md:size-24 ${highlight.color}`} />
                      </div>
                      <div>
                        <div className="text-lg md:text-xl font-bold text-gradient-gold">
                          {highlight.stat}
                        </div>
                      </div>
                    </div>
                    <p className="text-body-small md:text-body text-soft-white">
                      {highlight.description}
                    </p>
                  </div>
                );
              })}
            </div>
          </div>
        </div>

        {/* Why Ekehi Statistics */}
        <div className="bg-dark-slate rounded-2xl p-6 md:p-8 border border-charcoal-gray">
          <h3 className="text-h3 text-center text-white mb-6 md:mb-8">Why Ekehi?</h3>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-6 md:gap-8">
            {statistics.map((stat, index) => (
              <div key={index} className="text-center group">
                <div className="text-2xl md:text-3xl font-bold text-gradient-gold mb-2 group-hover:scale-110 transition-transform">
                  {stat.value}
                </div>
                <div className="text-medium-gray text-xs md:text-sm">
                  {stat.label}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* CTA Section */}
        <div className="text-center mt-12 md:mt-16 px-4">
          <h3 className="text-h3 text-white mb-4 md:mb-6">Ready to Learn More?</h3>
          <p className="text-body text-soft-white mb-8 max-w-2xl mx-auto">
            Download our whitepaper today and discover how Ekehi is building the future of sustainable finance.
          </p>
          <div className="flex flex-col sm:flex-row gap-3 md:gap-4 justify-center">
            <a href="#" className="btn-primary py-3 px-6 md:py-4 md:px-8 text-sm md:text-base">
              <Download size={18} className="md:size-20 mr-2" />
              Download PDF
            </a>
            <a href="#ecosystem" className="btn-secondary py-3 px-6 md:py-4 md:px-8 text-sm md:text-base">
              Explore Ecosystem
            </a>
          </div>
        </div>
      </div>
    </section>
  );
}