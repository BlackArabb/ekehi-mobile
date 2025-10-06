import { useState } from 'react';
import { Leaf, Users, Globe, Eye, Shield, Target, ChevronDown } from 'lucide-react';

const features = [
  {
    icon: Leaf,
    title: 'Sustainable Blockchain',
    description: 'Powered by energy-efficient consensus mechanisms that minimize environmental impact while maintaining security and decentralization.',
  },
  {
    icon: Users,
    title: 'Community Driven',
    description: 'Governance by the people, for the people. Token holders have a direct voice in the project\'s direction and decision-making.',
  },
  {
    icon: Globe,
    title: 'Inclusive Finance',
    description: 'Breaking down barriers to financial access. Banking the unbanked with low fees and accessible technology.',
  },
  {
    icon: Eye,
    title: 'Transparency',
    description: 'Complete visibility into project operations, funds allocation, and development progress. No hidden agendas.',
  },
  {
    icon: Shield,
    title: 'Security First',
    description: 'Multi-layer security protocols, audited smart contracts, and battle-tested infrastructure to protect your assets.',
  },
  {
    icon: Target,
    title: 'Global Impact',
    description: 'Creating real-world value through partnerships, community initiatives, and sustainable development projects worldwide.',
  },
];

const badges = [
  { label: '🌱 Eco-Friendly', bgColor: 'bg-green-600', textColor: 'text-white' },
  { label: '👥 Community Focused', bgColor: 'bg-yellow-500', textColor: 'text-black' },
  { label: '🔍 Transparent', bgColor: 'bg-blue-600', textColor: 'text-white' },
];

export default function AboutSection() {
  const [expandedFeature, setExpandedFeature] = useState<number | null>(null);

  const toggleFeature = (index: number) => {
    setExpandedFeature(expandedFeature === index ? null : index);
  };

  return (
    <section id="about" className="section-padding bg-rich-charcoal">
      <div className="container">
        {/* Section Header */}
        <div className="text-center mb-12 md:mb-16">
          <h2 className="text-h2 font-display text-gradient-gold mb-4 md:mb-6">
            About Ekehi
          </h2>
          <p className="text-body text-soft-white max-w-4xl mx-auto px-4 md:px-0">
            Ekehi is more than just a cryptocurrency—it's a movement towards sustainable, 
            transparent, and inclusive finance. We're building a blockchain ecosystem that 
            prioritizes environmental responsibility, community governance, and global accessibility.
          </p>
          
          {/* Badges */}
          <div className="flex flex-wrap justify-center gap-3 md:gap-4 mt-8 mb-10 md:mb-12">
            {badges.map((badge, index) => (
              <span
                key={index}
                className={`px-3 py-1.5 md:px-4 md:py-2 rounded-full text-xs md:text-sm font-semibold ${badge.bgColor} ${badge.textColor} animate-fade-in`}
                style={{ animationDelay: `${index * 100}ms` }}
              >
                {badge.label}
              </span>
            ))}
          </div>
        </div>

        {/* Features Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 md:gap-8 mb-12">
          {features.map((feature, index) => {
            const IconComponent = feature.icon;
            const isExpanded = expandedFeature === index;
            
            return (
              <div
                key={index}
                className="card group hover:shadow-gold transition-all duration-300 cursor-pointer"
                onClick={() => toggleFeature(index)}
              >
                <div className="mb-4 md:mb-6">
                  <div className="flex items-center justify-between mb-4">
                    <div className="w-12 h-12 md:w-16 md:h-16 bg-yellow-500/10 rounded-full flex items-center justify-center mb-4 group-hover:bg-yellow-500/20 transition-colors">
                      <IconComponent size={24} className="md:size-8 text-yellow-500" />
                    </div>
                    <ChevronDown 
                      size={20} 
                      className={`text-yellow-500 transition-transform duration-300 ${isExpanded ? 'rotate-180' : ''}`} 
                    />
                  </div>
                  <h3 className="text-h4 text-white mb-3">{feature.title}</h3>
                  <p className={`text-body-small md:text-body text-soft-white leading-relaxed transition-all duration-300 ${isExpanded ? 'line-clamp-none' : 'line-clamp-3'}`}>
                    {feature.description}
                  </p>
                </div>
              </div>
            );
          })}
        </div>

        {/* Community CTA */}
        <div className="text-center">
          <a 
            href="https://t.me/ekehi_official" 
            target="_blank" 
            rel="noopener noreferrer"
            className="btn-secondary inline-flex items-center gap-2 group py-3 px-6 md:py-4 md:px-8 text-sm md:text-base"
          >
            <Users size={20} />
            Join Our Telegram Community
            <span className="text-xs opacity-70 group-hover:opacity-100 transition-opacity">
              10K+ Members
            </span>
          </a>
        </div>
      </div>
    </section>
  );
}