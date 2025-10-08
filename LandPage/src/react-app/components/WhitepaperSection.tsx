import { useState, useEffect } from 'react';
import { Download, FileText, ChevronRight, BookOpen, Zap, Users, Shield, TrendingUp, Leaf, ChevronDown, ChevronUp } from 'lucide-react';

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
        <h3 class="text-xl font-bold text-white mb-4">3.1 The Ekehi Token</h3>
        <p class="text-soft-white mb-4">The EKEHI token is the native cryptocurrency of the ecosystem, designed to be a leader in sustainable cryptocurrency. It is environmentally friendly, energy-efficient, and supports global sustainability goals.</p>
        
        <h3 class="text-xl font-bold text-white mb-4 mt-6">3.2 Token Distribution</h3>
        <p class="text-soft-white mb-4">Ekehi has a total supply of 1 billion tokens distributed as follows:</p>
        <ul class="list-disc list-inside text-soft-white mb-4 ml-4">
          <li>Community: 50% (500,000,000 EKH)</li>
          <li>Public Sale: 20% (200,000,000 EKH)</li>
          <li>Liquidity: 10% (100,000,000 EKH)</li>
          <li>Reserve: 10% (100,000,000 EKH)</li>
          <li>Team: 5% (50,000,000 EKH)</li>
          <li>Staking: 5% (50,000,000 EKH)</li>
        </ul>
        
        <h3 class="text-xl font-bold text-white mb-4 mt-6">3.3 Token Utility</h3>
        <p class="text-soft-white mb-4">EKH tokens serve multiple purposes within the ecosystem:</p>
        <ul class="list-disc list-inside text-soft-white mb-4 ml-4">
          <li>Governance: Vote on protocol changes and upgrades</li>
          <li>Staking: Coming soon - Earn rewards by securing the network</li>
          <li>Fee Discounts: Reduced transaction fees across all platforms</li>
          <li>Liquidity Mining: Earn additional tokens by providing liquidity</li>
        </ul>
        
        <h3 class="text-xl font-bold text-white mb-4 mt-6">3.4 Deflationary Mechanism</h3>
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
        <p class="text-soft-white mb-4">Our team consists of experienced blockchain developers, financial experts, and product designers:</p>
        
        <div class="mb-4">
          <h4 class="text-lg font-bold text-yellow-500 mb-2">Ahmad A – CEO & Co-Founder</h4>
          <p class="text-soft-white mb-4">Blockchain strategist with over 5 years of experience in decentralized technologies and financial systems. Ahmad leads the overall vision, strategy, and external partnerships, ensuring alignment with the project's long-term goals.</p>
        </div>

         <div class="mb-4">
          <h4 class="text-lg font-bold text-yellow-500 mb-2">Kamal M. Saleh – COO, CFO & Product Designer, Co-Founder</h4>
          <p class="text-soft-white mb-4">Experienced in go-to-market strategies for DeFi projects and product development. Kamal manages operations and financial systems while also contributing to product design and front-end user experience, bridging business strategy with user-focused design.</p>
        </div>
        
        <div class="mb-4">
          <h4 class="text-lg font-bold text-yellow-500 mb-2">Suleiman Akaaba – CTO & CIO, Co-Founder</h4>
          <p class="text-soft-white mb-4">Specialist in Layer-2 scaling solutions, cryptography, and smart contract development. Suleiman leads the technical direction, security, and scalability of the EKEHI ecosystem.</p>
        </div>
        
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


export default function WhitepaperSection() {
  const [activeChapter, setActiveChapter] = useState(1);
  const [isScrolled, setIsScrolled] = useState(false);
  const [mobileExpandedChapter, setMobileExpandedChapter] = useState<number | null>(null);

  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 50);
    };

    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  // Get the active chapter content
  const activeChapterContent = whitepaperContent.chapters.find(chapter => chapter.id === activeChapter) || whitepaperContent.chapters[0];
  
  // Handle mobile chapter toggle
  const toggleMobileChapter = (chapterId: number) => {
    setMobileExpandedChapter(mobileExpandedChapter === chapterId ? null : chapterId);
  };

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
            <div className={`lg:sticky top-20 transition-all duration-300`}>
              <h3 className="text-h3 text-white mb-6">Table of Contents</h3>
              <div className="space-y-3">
                {whitepaperContent.chapters.map((chapter) => {
                  const IconComponent = chapter.icon;
                  const isMobileExpanded = mobileExpandedChapter === chapter.id;
                  const isDesktopActive = activeChapter === chapter.id;
                  
                  return (
                    <div key={chapter.id}>
                      {/* Chapter Item */}
                      <div
                        className={`flex items-center gap-4 p-4 rounded-xl transition-all duration-300 cursor-pointer group ${
                          isDesktopActive 
                            ? 'bg-yellow-500/10 border border-yellow-500' 
                            : 'bg-dark-slate border border-charcoal-gray hover:border-yellow-500 hover:bg-yellow-500/5'
                        }`}
                        onClick={() => {
                          // Desktop behavior
                          setActiveChapter(chapter.id);
                          // Mobile behavior
                          toggleMobileChapter(chapter.id);
                        }}
                      >
                        <div className={`w-10 h-10 rounded-full flex items-center justify-center group-hover:scale-110 transition-transform ${
                          isDesktopActive 
                            ? 'bg-yellow-500 text-black' 
                            : 'bg-black/50 text-yellow-500'
                        }`}>
                          <IconComponent size={20} />
                        </div>
                        <span className={`font-medium transition-colors flex-grow ${
                          isDesktopActive 
                            ? 'text-yellow-500' 
                            : 'text-white group-hover:text-yellow-500'
                        }`}>
                          {chapter.title}
                        </span>
                        {/* Mobile expand/collapse icon */}
                        <div className="md:hidden">
                          {isMobileExpanded ? 
                            <ChevronUp size={20} className="text-white" /> : 
                            <ChevronDown size={20} className="text-white" />
                          }
                        </div>
                      </div>
                      
                      {/* Mobile Content Expansion */}
                      <div className="md:hidden">
                        {isMobileExpanded && (
                          <div className="bg-dark-slate border-2 border-yellow-500 rounded-2xl p-6 mt-2">
                            <div className="mb-4">
                              <div className="flex items-center gap-3 mb-4">
                                <FileText size={24} className="text-yellow-500" />
                                <div>
                                  <h4 className="text-h4 text-white">{whitepaperContent.title}</h4>
                                  <p className="text-medium-gray text-sm">{whitepaperContent.subtitle}</p>
                                </div>
                              </div>
                              
                              <div className="flex flex-wrap gap-2 mb-4">
                                <span className="bg-yellow-500/10 text-yellow-500 px-2 py-1 rounded-full text-xs">
                                  Version {whitepaperContent.version}
                                </span>
                                <span className="bg-blue-500/10 text-blue-500 px-2 py-1 rounded-full text-xs">
                                  Updated {whitepaperContent.lastUpdated}
                                </span>
                              </div>
                              
                              {/* Chapter Content */}
                              <div className="prose prose-invert max-w-none">
                                <div 
                                  className="text-soft-white text-sm"
                                  dangerouslySetInnerHTML={{ __html: chapter.content }} 
                                />
                              </div>
                            </div>
                            
                            {/* Mobile Navigation */}
                            <div className="flex justify-between items-center pt-4 border-t border-charcoal-gray">
                              <button 
                                onClick={(e) => {
                                  e.stopPropagation();
                                  setActiveChapter(Math.max(1, chapter.id - 1));
                                  setMobileExpandedChapter(Math.max(1, chapter.id - 1));
                                }}
                                disabled={chapter.id === 1}
                                className="btn-secondary py-1 px-3 text-xs disabled:opacity-50 disabled:cursor-not-allowed"
                              >
                                ← Previous
                              </button>
                              
                              <div className="text-medium-gray text-xs">
                                Chapter {chapter.id} of {whitepaperContent.chapters.length}
                              </div>
                              
                              <button 
                                onClick={(e) => {
                                  e.stopPropagation();
                                  setActiveChapter(Math.min(whitepaperContent.chapters.length, chapter.id + 1));
                                  setMobileExpandedChapter(Math.min(whitepaperContent.chapters.length, chapter.id + 1));
                                }}
                                disabled={chapter.id === whitepaperContent.chapters.length}
                                className="btn-secondary py-1 px-3 text-xs disabled:opacity-50 disabled:cursor-not-allowed"
                              >
                                Next →
                              </button>
                            </div>
                          </div>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>

             
            </div>
          </div>

          {/* Desktop Document Content (Hidden on mobile) */}
          <div className="hidden md:block lg:col-span-3">
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
            <a href="/Whitepaperv2.0.pdf" target="_blank" rel="noopener noreferrer" className="btn-primary py-3 px-6 md:py-4 md:px-8 text-sm md:text-base">
              <Download size={18} className="md:size-20 mr-2" />
              Download PDF
            </a>
          </div>
        </div>
      </div>
    </section>
  );
}