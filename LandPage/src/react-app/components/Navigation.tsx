import { useState, useEffect } from 'react';
import { Menu, X, Home, Info, FileText, Rocket, Globe, PieChart, Map, Mail, Smartphone } from 'lucide-react';
import { useLocation } from 'react-router';

const navigationLinks = [
  { href: '#home', label: 'Home', icon: Home },
  { href: '#about', label: 'About', icon: Info },
  { href: '#whitepaper', label: 'Whitepaper', icon: FileText },
  { href: '#mining-app', label: 'Mining', icon: Smartphone },
  { href: '#ecosystem', label: 'Ecosystem', icon: Globe },
  { href: '#tokenomics', label: 'Tokenomics', icon: PieChart },
  { href: '#roadmap', label: 'Roadmap', icon: Map },
  { href: '#contact', label: 'Contact', icon: Mail },
  // Removed Mobile Auth link as it's not needed in navigation
];

export default function Navigation() {
  const location = useLocation();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isScrolled, setIsScrolled] = useState(false);
  const [activeSection, setActiveSection] = useState('home');

  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 50);
      
      // Determine active section based on scroll position
      const sections = navigationLinks
        .filter(link => link.href.startsWith('#'))
        .map(link => link.href.substring(1));
      const scrollPosition = window.scrollY + 100;
      
      for (const section of sections) {
        const element = document.getElementById(section);
        if (element) {
          const offsetTop = element.offsetTop;
          const height = element.offsetHeight;
          
          if (scrollPosition >= offsetTop && scrollPosition < offsetTop + height) {
            setActiveSection(section);
            break;
          }
        }
      }
    };

    window.addEventListener('scroll', handleScroll);
    // Initialize on load
    handleScroll();
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  // Close mobile menu when resizing to desktop
  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth >= 768) {
        setIsMobileMenuOpen(false);
      }
    };

    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const toggleMobileMenu = () => {
    setIsMobileMenuOpen(!isMobileMenuOpen);
  };

  // Check if we're on a page route (not a section anchor)
  const isPageRoute = (href: string) => !href.startsWith('#');

  return (
    <>
      {/* Desktop Navigation */}
      <nav className={`hidden md:flex items-center justify-between py-4 px-6 transition-all duration-300 sticky top-0 z-50 ${
        isScrolled ? 'bg-black/80 backdrop-blur-md border-b border-charcoal-gray' : 'bg-transparent'
      }`}>
        <div className="flex items-center">
          <a href="#home" className="flex items-center">
            <img src="/header.jpg" alt="EKEHI Logo" className="h-12 w-auto mr-2" />
          </a>
        </div>

        <div className="flex items-center space-x-8">
          {navigationLinks.map((link) => {
            // For page routes, check if the current location matches
            if (isPageRoute(link.href)) {
              const isActive = location.pathname === link.href;
              return (
                <a
                  key={link.label}
                  href={link.href}
                  className={`transition-colors flex items-center gap-1.5 text-sm font-medium ${
                    isActive 
                      ? 'text-white' // White color for active state
                      : 'text-medium-gray hover:text-yellow-500'
                  }`}
                >
                  <link.icon 
                    size={16} 
                    className={isActive ? 'text-white' : 'text-ekehi-gold'} 
                  />
                  {link.label}
                </a>
              );
            }
            
            // For section anchors, use the existing logic
            const isActive = activeSection === link.href.substring(1);
            return (
              <a
                key={link.label}
                href={link.href}
                className={`transition-colors flex items-center gap-1.5 text-sm font-medium ${
                  isActive 
                    ? 'text-white' // White color for active state
                    : 'text-medium-gray hover:text-yellow-500'
                }`}
              >
                <link.icon 
                  size={16} 
                  className={isActive ? 'text-white' : 'text-ekehi-gold'} 
                />
                {link.label}
              </a>
            );
          })}
        </div>

        {/* Connect Wallet Button - REMOVED as per request */}
        <div className="hidden md:flex items-center">
          {/* Connect Wallet button removed */}
        </div>
      </nav>

      {/* Mobile Navigation */}
      <nav className={`md:hidden flex items-center justify-between py-4 px-6 transition-all duration-300 sticky top-0 z-50 ${
        isScrolled ? 'bg-black/80 backdrop-blur-md border-b border-charcoal-gray' : 'bg-transparent'
      }`}>
        <div className="flex items-center">
          <a href="#home" className="flex items-center">
            <img src="/header.jpg" alt="EKEHI Logo" className="h-10 w-auto mr-2" />
          </a>
        </div>

        <button
          onClick={toggleMobileMenu}
          className="text-white focus:outline-none"
          aria-label="Toggle menu"
        >
          {isMobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
        </button>
      </nav>

      {/* Mobile Menu Overlay */}
      {isMobileMenuOpen && (
        <div className="md:hidden fixed inset-0 bg-black/90 backdrop-blur-lg z-50">
          <div className="flex flex-col h-full pt-20 px-6">
            {/* Close Button */}
            <div className="absolute top-4 right-4">
              <button
                onClick={toggleMobileMenu}
                className="text-white p-2 rounded-full hover:bg-charcoal-gray transition-colors"
                aria-label="Close menu"
              >
                <X size={24} />
              </button>
            </div>
            
            <div className="flex flex-col space-y-6 flex-grow pt-8">
              {navigationLinks.map((link) => {
                // For page routes, check if the current location matches
                if (isPageRoute(link.href)) {
                  const isActive = location.pathname === link.href;
                  return (
                    <a
                      key={link.label}
                      href={link.href}
                      className={`text-base transition-colors flex items-center gap-3 py-2 border-b border-charcoal-gray ${
                        isActive 
                          ? 'text-white' // White color for active state
                          : 'text-white hover:text-yellow-500'
                      }`}
                      onClick={() => setIsMobileMenuOpen(false)}
                    >
                      <link.icon 
                        size={24} 
                        className={isActive ? 'text-white' : 'text-ekehi-gold'} 
                      />
                      {link.label}
                    </a>
                  );
                }
                
                // For section anchors, use the existing logic
                const isActive = activeSection === link.href.substring(1);
                return (
                  <a
                    key={link.label}
                    href={link.href}
                    className={`text-2xl transition-colors flex items-center gap-3 py-3 border-b border-charcoal-gray ${
                      isActive 
                        ? 'text-white' // White color for active state
                        : 'text-white hover:text-yellow-500'
                    }`}
                    onClick={() => setIsMobileMenuOpen(false)}
                  >
                    <link.icon 
                      size={24} 
                      className={isActive ? 'text-white' : 'text-ekehi-gold'} 
                    />
                    {link.label}
                  </a>
                );
              })}
            </div>

            {/* Connect Wallet Button - REMOVED as per request */}
            <div className="py-8 border-t border-charcoal-gray">
              {/* Connect Wallet button removed */}
            </div>
          </div>
        </div>
      )}
    </>
  );
}