import Navigation from '@/react-app/components/Navigation';
import HeroSection from '@/react-app/components/HeroSection';
import AboutSection from '@/react-app/components/AboutSection';
import WhitepaperSection from '@/react-app/components/WhitepaperSection';
// PresaleSection import removed
import EcosystemSection from '@/react-app/components/EcosystemSection';
import TokenomicsSection from '@/react-app/components/TokenomicsSection';
import RoadmapSection from '@/react-app/components/RoadmapSection';
import ContactSection from '@/react-app/components/ContactSection';
import MiningAppSection from '@/react-app/components/MiningAppSection';

export default function Home() {
  return (
    <div className="min-h-screen bg-black text-white">
      <Navigation />
      {/* Removed relative z-0 to allow proper sticky positioning */}
      <div>
        <HeroSection />
        <main>
          <AboutSection />
          <WhitepaperSection />
          {/* PresaleSection removed */}
          <EcosystemSection />
          <TokenomicsSection />
          <RoadmapSection />
          <MiningAppSection />
          <ContactSection />
        </main>
        
        {/* Footer */}
        <footer className="bg-black border-t border-charcoal-gray py-6">
          <div className="container">
            <div className="text-center">
              <div className="flex justify-center mb-4">
                <img 
                  src="/header.jpg" 
                  alt="EKEHI Logo" 
                  className="h-12 w-auto"
                />
              </div>
              <p className="text-medium-gray text-sm mb-4">Building a Sustainable and Inclusive Financial Future</p>
              <div className="flex flex-col sm:flex-row justify-center gap-2 sm:gap-4 text-xs text-medium-gray">
                <span>© 2025 Ekehi. All rights reserved.</span>
                <span className="hidden sm:inline text-yellow-500">•</span>
                <a href="/privacy-policy" className="hover:text-yellow-500 transition-colors">Privacy Policy</a>
                <span className="hidden sm:inline text-yellow-500">•</span>
                <a href="/terms" className="hover:text-yellow-500 transition-colors">Terms of Service</a>
              </div>
            </div>
          </div>
        </footer>
      </div>
    </div>
  );
}