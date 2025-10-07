import { useState, useEffect } from 'react';
import { Menu, X, Home, Info, FileText, Rocket, Globe, PieChart, Map, Mail, Wallet } from 'lucide-react';

const navigationLinks = [
  { href: '#home', label: 'Home', icon: Home },
  { href: '#about', label: 'About', icon: Info },
  { href: '#whitepaper', label: 'Whitepaper', icon: FileText },
  { href: '#presale', label: 'Presale', icon: Rocket },
  { href: '#ecosystem', label: 'Ecosystem', icon: Globe },
  { href: '#tokenomics', label: 'Tokenomics', icon: PieChart },
  { href: '#roadmap', label: 'Roadmap', icon: Map },
  { href: '#contact', label: 'Contact', icon: Mail },
];

export default function Navigation() {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isScrolled, setIsScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 50);
    };

    window.addEventListener('scroll', handleScroll);
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

  return (
    <>
      {/* Desktop Navigation */}
      <nav className={`hidden md:flex items-center justify-between py-4 px-6 transition-all duration-300 ${
        isScrolled ? 'bg-black/80 backdrop-blur-md border-b border-charcoal-gray' : 'bg-transparent'
      }`}>
        <div className="flex items-center">
          <a href="#home" className="flex items-center">
            <img src="/logo.png" alt="EKEHI Logo" className="h-8 w-auto mr-2" />
            <span className="text-xl font-bold text-white">EKEHI</span>
          </a>
        </div>

        <div className="flex items-center space-x-8">
          {navigationLinks.map((link) => (
            <a
              key={link.label}
              href={link.href}
              className="text-medium-gray hover:text-yellow-500 transition-colors flex items-center gap-1.5 text-sm font-medium"
            >
              <link.icon size={16} />
              {link.label}
            </a>
          ))}
        </div>

        {/* Connect Wallet Button - REMOVED as per request */}
        <div className="hidden md:flex items-center">
          {/* Connect Wallet button removed */}
        </div>
      </nav>

      {/* Mobile Navigation */}
      <nav className={`md:hidden flex items-center justify-between py-4 px-6 transition-all duration-300 ${
        isScrolled ? 'bg-black/80 backdrop-blur-md border-b border-charcoal-gray' : 'bg-transparent'
      }`}>
        <div className="flex items-center">
          <a href="#home" className="flex items-center">
            <img src="/logo.png" alt="EKEHI Logo" className="h-8 w-auto mr-2" />
            <span className="text-xl font-bold text-white">EKEHI</span>
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
            <div className="flex flex-col space-y-6 flex-grow">
              {navigationLinks.map((link) => (
                <a
                  key={link.label}
                  href={link.href}
                  className="text-2xl text-white hover:text-yellow-500 transition-colors flex items-center gap-3 py-3 border-b border-charcoal-gray"
                  onClick={() => setIsMobileMenuOpen(false)}
                >
                  <link.icon size={24} />
                  {link.label}
                </a>
              ))}
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