import { ChevronDown, Download, Sparkles } from 'lucide-react';
import { useEffect, useState } from 'react';

export default function HeroSection() {
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    setIsVisible(true);
  }, []);

  return (
    <section id="home" className="relative min-h-screen flex items-center justify-center overflow-hidden">
      {/* Animated Background */}
      <div className="absolute inset-0 bg-gradient-to-b from-black via-gray-900/50 to-black">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_50%_50%,rgba(255,160,0,0.1),transparent_50%)]"></div>
        
        {/* Floating Particles */}
        {Array.from({ length: 20 }).map((_, i) => (
          <div
            key={i}
            className="absolute w-2 h-2 bg-yellow-500/30 rounded-full animate-float"
            style={{
              left: `${Math.random() * 100}%`,
              top: `${Math.random() * 100}%`,
              animationDelay: `${Math.random() * 6}s`,
              animationDuration: `${6 + Math.random() * 4}s`,
            }}
          ></div>
        ))}
      </div>

      {/* Content */}
      <div className="relative z-10 text-center max-w-5xl mx-auto px-4">
        {/* Main Title */}
        <h1 
          className={`text-hero font-display text-gradient-gold glow-gold mb-4 md:mb-6 transition-all duration-1000 ${
            isVisible ? 'animate-slide-up' : 'opacity-0 translate-y-8'
          }`}
          style={{ animationDelay: '0.2s' }}
        >
          Ekehi: Redefining Cryptocurrency
        </h1>

        {/* Tagline */}
        <p 
          className={`text-body-large text-soft-white max-w-3xl mx-auto mb-8 md:mb-10 transition-all duration-1000 ${
            isVisible ? 'animate-slide-up' : 'opacity-0 translate-y-8'
          }`}
          style={{ animationDelay: '0.4s' }}
        >
          Building a Sustainable and Inclusive Financial Future for Everyone
        </p>

        {/* CTA Buttons */}
        <div 
          className={`flex flex-col sm:flex-row gap-4 justify-center items-center mb-12 md:mb-16 transition-all duration-1000 hero-section ${
            isVisible ? 'animate-slide-up' : 'opacity-0 translate-y-8'
          }`}
          style={{ animationDelay: '0.6s' }}
        >
          <a href="https://chat.whatsapp.com/HXP3npB9Ygi18yquPsThx4?mode=ems_share_t" target="_blank" rel="noopener noreferrer" className="btn-primary group">
            Join Private Sale
            <Sparkles size={16} className="opacity-0 group-hover:opacity-100 transition-opacity" />
          </a>
          <a href="#mining-app" className="btn-secondary group">
            Claim Free EKH
            <span className="w-2 h-2 bg-green-500 rounded-full ml-2"></span>
          </a>
        </div>

        {/* Stats Row */}
        <div 
          className={`grid grid-cols-2 md:grid-cols-4 gap-6 md:gap-8 max-w-3xl mx-auto mb-12 md:mb-16 transition-all duration-1000 ${
            isVisible ? 'animate-fade-in' : 'opacity-0'
          }`}
          style={{ animationDelay: '0.8s' }}
        >
          <div className="text-center">
            <div className="text-2xl md:text-3xl font-bold text-gradient-gold">10M+</div>
            <div className="text-medium-gray text-sm">Target Community</div>
          </div>
          <div className="text-center">
            <div className="text-2xl md:text-3xl font-bold text-gradient-gold">$60M</div>
            <div className="text-medium-gray text-sm">Target Cap</div>
          </div>
          <div className="text-center">
            <div className="text-2xl md:text-3xl font-bold text-gradient-gold">100+</div>
            <div className="text-medium-gray text-sm">Target Countries</div>
          </div>
          <div className="text-center">
            <div className="text-2xl md:text-3xl font-bold text-gradient-gold">24/7</div>
            <div className="text-medium-gray text-sm">Support</div>
          </div>
        </div>
      </div>

      {/* Scroll Indicator */}
      <div 
        className={`absolute bottom-8 left-1/2 transform -translate-x-1/2 transition-all duration-1000 scroll-indicator ${
          isVisible ? 'animate-fade-in' : 'opacity-0'
        }`}
        style={{ animationDelay: '1s' }}
      >
        <a 
          href="#about" 
          className="flex flex-col items-center text-medium-gray hover:text-yellow-500 transition-colors group"
        >
          <span className="text-sm mb-2">Scroll to explore</span>
          <ChevronDown 
            size={24} 
            className="animate-bounce group-hover:text-yellow-500" 
          />
        </a>
      </div>
    </section>
  );
}
