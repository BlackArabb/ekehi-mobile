import { useState, useEffect } from 'react';
import { Download, Smartphone, Zap, Users, Shield, Award, ChevronDown, Play } from 'lucide-react';

const appFeatures = [
  {
    icon: Zap,
    title: 'Easy Mining',
    description: 'Tap-to-mine functionality that rewards you for simple interactions. No complex setup required.',
  },
  {
    icon: Shield,
    title: 'Secure Wallet',
    description: 'Built-in secure wallet to store your EKH tokens with advanced encryption and backup options.',
  },
  {
    icon: Users,
    title: 'Referral System',
    description: 'Earn additional rewards by inviting friends to join the Ekehi ecosystem.',
  },
  {
    icon: Award,
    title: 'Daily Rewards',
    description: 'Complete daily tasks and challenges to earn bonus EKH tokens and boost your mining power.',
  },
  {
    icon: Smartphone,
    title: 'Cross-Platform',
    description: 'Available on both iOS and Android devices with seamless synchronization across platforms.',
  },
  {
    icon: Play,
    title: 'Social Tasks',
    description: 'Engage with our social platforms to earn extra rewards and stay updated with the latest news.',
  },
];

const appStats = [
  { label: 'Status', value: 'Available Now' },
  { label: 'EKH Distribution', value: 'Pre-Sale' },
  { label: 'Mining Type', value: '24/7 Auto' },
  { label: 'Release', value: 'Q1 2026' },
];

export default function MiningAppSection() {
  const [expandedFeature, setExpandedFeature] = useState<number | null>(null);
  // State to control whether the app is ready for download or coming soon
  const [isAppReady, setIsAppReady] = useState(true); // Set to true for "Available Now"

  // Load the app status from localStorage when component mounts
  useEffect(() => {
    // Set the localStorage value to ensure persistence
    localStorage.setItem('ekehiAppReady', 'true');
    
    const savedStatus = localStorage.getItem('ekehiAppReady');
    if (savedStatus !== null) {
      setIsAppReady(savedStatus === 'true');
    }
  }, []);

  const toggleFeature = (index: number) => {
    setExpandedFeature(expandedFeature === index ? null : index);
  };

  return (
    <section id="mining-app" className="section-padding bg-rich-charcoal">
      <div className="container">
        {/* Section Header */}
        <div className="text-center mb-12 md:mb-16">
          <h2 className="text-h2 font-display text-gradient-gold mb-4 md:mb-6">
            Ekehi Mining App
          </h2>
          {/* Paragraph for when app is ready */}
          {isAppReady ? (
            <p className="text-body text-soft-white max-w-3xl mx-auto px-4 md:px-0">
              Download our mobile app now and start mining EKH tokens right from your smartphone. 
              Simple, secure, and rewarding way to participate in the Ekehi ecosystem.
            </p>
          ) : (
            /* Paragraph for when app is coming soon */
            <p className="text-body text-soft-white max-w-3xl mx-auto px-4 md:px-0">
              Mobile app coming soon! Be among the first to mine EKH tokens right from your smartphone. 
              Simple, secure, and rewarding way to participate in the Ekehi ecosystem.
            </p>
          )}
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 md:gap-12 mb-12 md:mb-16">
          {/* App Preview */}
          <div className="flex flex-col items-center">
            <div className="relative w-48 h-48 sm:w-56 sm:h-56 md:w-80 md:h-80 mb-8">
              <div className="absolute inset-0 bg-gradient-to-br from-yellow-500 to-amber-600 rounded-3xl transform rotate-6"></div>
              <div className="absolute inset-0 bg-gradient-to-br from-yellow-500 to-amber-600 rounded-3xl transform -rotate-6"></div>
              <div className="relative bg-black rounded-3xl w-full h-full flex items-center justify-center border-8 border-yellow-500/20 shadow-gold-lg">
                <img src='/logo.png' alt="Ekehi Logo" className="w-24 h-24 sm:w-28 sm:h-28 md:w-36 md:h-36 object-contain" />
              </div>
            </div>
            
            {/* App Stats */}
            <div className="grid grid-cols-2 gap-3 w-full max-w-xs sm:max-w-sm">
              {appStats.map((stat, index) => (
                <div key={index} className="bg-dark-slate rounded-lg p-2 sm:p-3 text-center border border-charcoal-gray">
                  <div className="text-lg sm:text-xl font-bold text-gradient-gold">{stat.value}</div>
                  <div className="text-xs text-medium-gray">{stat.label}</div>
                </div>
              ))}
            </div>

            {/* Download CTA */}
            <div className="text-center mt-10">
              {isAppReady ? (
                <>
                  <p className="text-yellow-500 font-bold mb-2 text-center">Start Mining Now</p>
                  <a 
                    href="https://archive.org/embed/ekehi"
                    className="btn-primary inline-flex items-center gap-2 group py-3 px-6 md:py-4 md:px-8 text-sm md:text-base centered-button"
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    <Download size={20} />
                    Download Mining App
                  </a>
                  <div className="mt-4 text-sm text-medium-gray">
                    <p>Direct APK download. Not available on app stores yet.</p>
                  </div>
                </>
              ) : (
                <div className="bg-dark-slate rounded-xl p-6 border border-charcoal-gray max-w-md mx-auto">
                  <div className="flex flex-col items-center text-center">
                    <Smartphone size={48} className="text-yellow-500 mb-4" />
                    <h3 className="text-xl font-bold text-white mb-2">Mining App Coming Soon</h3>
                    <p className="text-soft-white mb-4">
                      Mobile app currently in development. Be among the first to mine EKH tokens right from your smartphon, Stay tuned.
                    </p>
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* App Features */}
          <div>
            <h3 className="text-h3 text-white mb-6">App Features</h3>
            <div className="space-y-4 md:space-y-6">
              {appFeatures.map((feature, index) => {
                const IconComponent = feature.icon;
                const isExpanded = expandedFeature === index;
                
                return (
                  <div
                    key={index}
                    className="bg-dark-slate rounded-xl p-4 md:p-5 border border-charcoal-gray hover:border-yellow-500 transition-all duration-300 cursor-pointer group"
                    onClick={() => toggleFeature(index)}
                  >
                    <div className="flex items-start justify-between mb-3">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 bg-yellow-500/10 rounded-full flex items-center justify-center group-hover:bg-yellow-500/20 transition-colors">
                          <IconComponent size={20} className="text-yellow-500" />
                        </div>
                        <h4 className="font-semibold text-white">{feature.title}</h4>
                      </div>
                      <ChevronDown 
                        size={20} 
                        className={`text-yellow-500 transition-transform duration-300 ${isExpanded ? 'rotate-180' : ''}`} 
                      />
                    </div>
                    <p className={`text-soft-white text-sm md:text-base transition-all duration-300 ${isExpanded ? 'line-clamp-none' : 'line-clamp-2'}`}>
                      {feature.description}
                    </p>
                  </div>
                );
              })}
            </div>
          </div>
        </div>

        
      </div>
    </section>
  );
}