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

  const closeMobileMenu = () => {
    setIsMobileMenuOpen(false);
  };

  // Handle navigation link clicks
  const handleNavClick = (e: React.MouseEvent<HTMLAnchorElement>, href: string) => {
    e.preventDefault();
    closeMobileMenu();
    
    const targetElement = document.querySelector(href);
    if (targetElement) {
      const offsetTop = targetElement.getBoundingClientRect().top + window.pageYOffset;
      window.scrollTo({
        top: offsetTop - 80, // Account for fixed navbar height
        behavior: 'smooth'
      });
    }
  };

  return (
    <>
      <nav
        className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${
          isScrolled
            ? 'bg-black/95 backdrop-blur-lg border-b border-yellow-500/10 h-16'
            : 'bg-transparent h-20'
        }`}
      >
        <div className="container mx-auto px-4 h-full flex items-center justify-between">
          {/* Logo */}
          <div className="flex items-center">
            <a
              href="#home"
              className="hover:glow-gold transition-all duration-300"
              onClick={(e) => handleNavClick(e, '#home')}
            >
              <img 
                src="/logo.png" 
                alt="EKEHI Logo" 
                className="h-10 w-auto"
              />
            </a>
          </div>

          {/* Desktop Navigation */}
          <div className="hidden md:flex items-center space-x-8">
            {navigationLinks.map((link) => (
              <a
                key={link.href}
                href={link.href}
                onClick={(e) => handleNavClick(e, link.href)}
                className="text-white hover:text-yellow-500 transition-colors duration-300 relative group text-body"
              >
                {link.label}
                <span className="absolute bottom-0 left-0 w-0 h-0.5 bg-yellow-500 transition-all duration-300 group-hover:w-full"></span>
              </a>
            ))}
          </div>

          {/* Connect Wallet Button */}
          <div className="hidden md:flex items-center">
            <button className="btn-primary">
              Connect Wallet
            </button>
          </div>

          {/* Mobile Menu Button - Hide when navigation panel is open */}
          <button
            onClick={toggleMobileMenu}
            className={`md:hidden text-yellow-500 hover:text-yellow-400 transition-colors duration-300 focus:outline-none focus:ring-2 focus:ring-yellow-500 rounded-md p-2 ${
              isMobileMenuOpen ? 'opacity-0 pointer-events-none' : 'opacity-100'
            }`}
            aria-label="Open menu"
            aria-expanded={isMobileMenuOpen}
          >
            <Menu size={24} />
          </button>
        </div>
      </nav>

      {/* Mobile Menu Sidebar */}
      {isMobileMenuOpen && (
        <>
          {/* Backdrop */}
          <div 
            className="fixed inset-0 z-40 bg-black/50 md:hidden"
            onClick={closeMobileMenu}
          ></div>
          
          {/* Sidebar Panel */}
          <div 
            className="fixed top-0 left-0 h-full w-4/5 max-w-sm z-50 bg-dark-slate border-r border-charcoal-gray shadow-2xl md:hidden transform transition-transform duration-300 ease-in-out"
          >
            <div className="flex flex-col h-full">
              {/* Sidebar Header - Show logo and X button only here */}
              <div className="flex items-center justify-between p-4 border-b border-charcoal-gray">
                <a
                  href="#home"
                  onClick={(e) => handleNavClick(e, '#home')}
                >
                  <img 
                    src="/logo.png" 
                    alt="EKEHI Logo" 
                    className="h-8 w-auto"
                  />
                </a>
                <button
                  onClick={closeMobileMenu}
                  className="text-yellow-500 hover:text-yellow-400 transition-colors duration-300 focus:outline-none focus:ring-2 focus:ring-yellow-500 rounded-md p-2"
                  aria-label="Close menu"
                >
                  <X size={24} />
                </button>
              </div>

              {/* Sidebar Content */}
              <div className="flex-1 overflow-y-auto py-6 px-4">
                <div className="space-y-2">
                  {navigationLinks.map((link, index) => {
                    const IconComponent = link.icon;
                    return (
                      <a
                        key={link.href}
                        href={link.href}
                        onClick={(e) => handleNavClick(e, link.href)}
                        className="flex items-center gap-4 p-4 rounded-xl bg-black/20 hover:bg-yellow-500/10 transition-colors duration-300"
                      >
                        <IconComponent size={20} className="text-yellow-500 flex-shrink-0" />
                        <span className="text-white text-lg font-medium">{link.label}</span>
                      </a>
                    );
                  })}
                </div>

                {/* Connect Wallet Button - Mobile Version */}
                <div className="mt-8 px-4">
                  <button 
                    className="btn-primary w-full py-4 text-lg flex items-center justify-center gap-2"
                    onClick={closeMobileMenu}
                  >
                    <Wallet size={20} />
                    Connect Wallet
                  </button>
                </div>

                {/* Social Links */}
                <div className="mt-8 px-4">
                  <div className="flex justify-center gap-6">
                    <a href="#" className="text-yellow-500 hover:text-yellow-400 transition-colors">
                      <div className="w-10 h-10 rounded-full bg-black/20 flex items-center justify-center">
                        <span className="font-bold">T</span>
                      </div>
                    </a>
                    <a href="#" className="text-yellow-500 hover:text-yellow-400 transition-colors">
                      <div className="w-10 h-10 rounded-full bg-black/20 flex items-center justify-center">
                        <span className="font-bold">D</span>
                      </div>
                    </a>
                    <a href="#" className="text-yellow-500 hover:text-yellow-400 transition-colors">
                      <div className="w-10 h-10 rounded-full bg-black/20 flex items-center justify-center">
                        <span className="font-bold">M</span>
                      </div>
                    </a>
                  </div>
                </div>
              </div>

              {/* Sidebar Footer */}
              <div className="p-4 border-t border-charcoal-gray">
                <div className="text-center text-medium-gray text-sm">
                  © 2025 Ekehi. All rights reserved.
                </div>
              </div>
            </div>
          </div>
        </>
      )}
    </>
  );
}