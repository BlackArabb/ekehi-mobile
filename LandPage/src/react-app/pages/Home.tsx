import Navigation from '@/react-app/components/Navigation';
import HeroSection from '@/react-app/components/HeroSection';
import AboutSection from '@/react-app/components/AboutSection';
import WhitepaperSection from '@/react-app/components/WhitepaperSection';
import PresaleSection from '@/react-app/components/PresaleSection';
import EcosystemSection from '@/react-app/components/EcosystemSection';
import TokenomicsSection from '@/react-app/components/TokenomicsSection';
import RoadmapSection from '@/react-app/components/RoadmapSection';
import ContactSection from '@/react-app/components/ContactSection';

export default function Home() {
  return (
    <div className="min-h-screen bg-black text-white">
      <Navigation />
      <div className="relative z-0">
        <HeroSection />
        <main>
          <AboutSection />
          <WhitepaperSection />
          <PresaleSection />
          <EcosystemSection />
          <TokenomicsSection />
          <RoadmapSection />
          <ContactSection />
        </main>
        
        {/* Footer */}
        <footer className="bg-black border-t border-charcoal-gray py-6">
          <div className="container">
            <div className="text-center">
              <div className="flex justify-center mb-4">
                <img 
                  src="/logo.png" 
                  alt="EKEHI Logo" 
                  className="h-10 w-auto"
                />
              </div>
              <p className="text-medium-gray text-sm mb-4">Building a Sustainable and Inclusive Financial Future</p>
              <div className="flex flex-col sm:flex-row justify-center gap-2 sm:gap-4 text-xs text-medium-gray">
                <span>© 2025 Ekehi. All rights reserved.</span>
                <span className="hidden sm:inline">•</span>
                <a href="#" className="hover:text-yellow-500 transition-colors">Privacy Policy</a>
                <span className="hidden sm:inline">•</span>
                <a href="#" className="hover:text-yellow-500 transition-colors">Terms of Service</a>
              </div>
            </div>
          </div>
        </footer>
      </div>
    </div>
  );
}