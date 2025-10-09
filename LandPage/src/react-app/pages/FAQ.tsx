import { useState } from 'react';
import { ChevronDown, ChevronUp, Home, Menu, X } from 'lucide-react';
import ContactSection from '@/react-app/components/ContactSection';

interface FAQItem {
  question: string;
  answer: string;
}

const faqData: FAQItem[] = [
  {
    question: "What is Ekehi Network?",
    answer: "Ekehi Network is a cryptocurrency platform featuring a mobile application that allows users to mine EKH tokens through an interactive tap-to-mine system. The platform also includes social task completion, a referral program, token presale, wallet integration, and an achievement system."
  },
  {
    question: "What platforms does Ekehi Network support?",
    answer: "The Ekehi Network mobile app is built with React Native and Expo, providing cross-platform support for iOS devices, Android devices, and web browsers (through Expo web build). Additionally, there is a separate landing page built with React and Vite, and an admin dashboard built with Next.js."
  },
  {
    question: "How does the tap-to-mine system work?",
    answer: "The tap-to-mine system is the core feature of the Ekehi Network app. Users can earn EKH tokens by tapping on a large circular mining button on the main screen. Each tap provides haptic feedback and visual effects, with the amount earned based on your mining power. The system also includes electric effects around the button, glow effect on interaction, session stats showing earned coins and taps, and a progress bar for daily mining limits."
  },
  {
    question: "What is the EKH token?",
    answer: "EKH is the native cryptocurrency token of the Ekehi Network. Users can earn EKH tokens through the tap-to-mine system, complete social tasks, participate in the referral program, and purchase tokens during the presale. EKH tokens can be stored in the integrated wallet, sent to other users, and used within the Ekehi ecosystem."
  },
  {
    question: "How do I create an account?",
    answer: "To create an account on Ekehi Network: 1. Open the app and tap 'Start Mining' 2. Choose your preferred authentication method: Continue with Google (OAuth) or Continue with Email (email/password) 3. Follow the prompts to complete registration 4. If using email authentication, you'll receive a verification email"
  },
  {
    question: "Can I use Google to sign in?",
    answer: "Yes, Ekehi Network supports Google OAuth authentication. This is the primary authentication method and provides a seamless sign-in experience. To use Google sign-in: 1. Tap 'Continue with Google' on the authentication screen 2. Select your Google account 3. Grant necessary permissions 4. You'll be redirected back to the app once authentication is complete"
  },
  {
    question: "What is the mining rate?",
    answer: "The default mining rate is 2 EKH tokens per day, which equates to approximately 0.83 EKH per hour. Your actual mining rate may be higher based on referral bonuses (0.2 EKH/second increase per referral), streak bonuses, and special promotions or events."
  },
  {
    question: "How do I connect my wallet?",
    answer: "To connect your wallet in the Ekehi Network app: 1. Navigate to the 'Wallet' tab 2. If your wallet isn't connected, you'll see a 'Connect Wallet' button 3. Tap 'Connect Wallet' 4. The app will generate a wallet address for you 5. Your wallet is now connected and ready to use"
  },
  {
    question: "How does the referral system work?",
    answer: "The Ekehi Network referral system allows you to earn rewards by inviting friends: 1. Each user has a unique referral code 2. Share your referral code with friends 3. When friends use your code, both you and your friend receive rewards 4. Your mining rate increases with each successful referral"
  },
  {
    question: "What are social tasks?",
    answer: "Social tasks are activities you can complete on various social media platforms to earn additional EKH tokens. These tasks help promote the Ekehi Network while providing rewards to users. Currently, Ekehi Network supports social tasks on Twitter (X), YouTube, Telegram, Discord, and other platforms may be added in future updates."
  },
  {
    question: "How much can I earn from referrals?",
    answer: "The referral reward system works as follows: For Referees: New users receive 2.0 EKH coins for using a referral code. For Referrers: You receive an increased mining rate of 0.2 EKH/second for each referral. Maximum Referrals: Each user can refer up to 50 new users."
  },
  {
    question: "How do I send tokens to others?",
    answer: "To send EKH tokens to another user: 1. Navigate to the 'Wallet' tab 2. Ensure your wallet is connected 3. Enter the recipient's wallet address in the 'Recipient Address' field 4. Enter the amount of EKH tokens you wish to send 5. Tap 'Send Tokens' 6. Confirm the transaction in the confirmation dialog"
  },
  {
    question: "Is there a limit to how many people I can refer?",
    answer: "Yes, each user can successfully refer up to 50 new users. After reaching this limit, you won't be able to earn additional referral bonuses, but you can continue to use the app normally."
  },
  {
    question: "What should I do if I forget my password?",
    answer: "If you've signed up with email authentication and forget your password: 1. On the sign-in screen, tap 'Forgot Password' 2. Enter your email address 3. Check your email for a password reset link 4. Follow the link and set a new password 5. You'll be redirected back to the app after resetting your password"
  },
  {
    question: "How do streaks work?",
    answer: "Ekehi Network rewards daily logins with streak bonuses: Log in each day to maintain your streak. Longer streaks provide increasing bonus multipliers. Missing a day will reset your streak to zero. Streak bonuses are applied to your mining rate."
  }
];

export default function FAQPage() {
  const [openIndex, setOpenIndex] = useState<number | null>(null);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const toggleFAQ = (index: number) => {
    setOpenIndex(openIndex === index ? null : index);
  };

  const toggleMobileMenu = () => {
    setIsMobileMenuOpen(!isMobileMenuOpen);
  };

  return (
    <div className="min-h-screen bg-black text-white">
      {/* Simplified Navigation for FAQ Page */}
      <nav className="hidden md:flex items-center justify-between py-4 px-6 bg-black/80 backdrop-blur-md border-b border-charcoal-gray sticky top-0 z-50">
        <div className="flex items-center">
          <a href="/" className="flex items-center">
            <img src="/header.jpg" alt="EKEHI Logo" className="h-12 w-auto mr-2" />
          </a>
        </div>

        <div className="flex items-center space-x-8">
          <a
            href="/"
            className="text-white hover:text-yellow-500 transition-colors flex items-center gap-1.5 text-sm font-medium"
          >
            <Home size={16} className="text-ekehi-gold" />
            Back to Home
          </a>
        </div>
      </nav>

      {/* Mobile Navigation for FAQ Page */}
      <nav className="md:hidden flex items-center justify-between py-4 px-6 bg-black/80 backdrop-blur-md border-b border-charcoal-gray sticky top-0 z-50">
        <div className="flex items-center">
          <a href="/" className="flex items-center">
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
            <div className="flex flex-col space-y-6 flex-grow">
              <a
                href="/"
                className="text-2xl text-white hover:text-yellow-500 transition-colors flex items-center gap-3 py-3 border-b border-charcoal-gray"
                onClick={() => setIsMobileMenuOpen(false)}
              >
                <Home size={24} className="text-ekehi-gold" />
                Back to Home
              </a>
            </div>
          </div>
        </div>
      )}

      <div className="pt-20 pb-16 bg-gradient-to-b from-black to-charcoal-gray">
        <div className="container mx-auto px-6">
          <div className="text-center mb-12">
            <h1 className="text-4xl md:text-5xl font-bold mb-6 bg-gradient-to-r from-ekehi-gold to-yellow-500 bg-clip-text text-transparent">
              Frequently Asked Questions
            </h1>
            <p className="text-xl text-medium-gray max-w-3xl mx-auto">
              Find answers to common questions about Ekehi Network, mining, referrals, and more.
            </p>
          </div>

          <div className="max-w-4xl mx-auto">
            <div className="space-y-4">
              {faqData.map((faq, index) => (
                <div 
                  key={index} 
                  className="bg-black/30 border border-charcoal-gray rounded-xl overflow-hidden transition-all duration-300 hover:border-ekehi-gold/50"
                >
                  <button
                    className="w-full flex justify-between items-center p-6 text-left"
                    onClick={() => toggleFAQ(index)}
                    aria-expanded={openIndex === index}
                  >
                    <h3 className="text-lg md:text-xl font-semibold text-white pr-4">
                      {faq.question}
                    </h3>
                    <div className="flex-shrink-0">
                      {openIndex === index ? (
                        <ChevronUp className="text-ekehi-gold" size={24} />
                      ) : (
                        <ChevronDown className="text-ekehi-gold" size={24} />
                      )}
                    </div>
                  </button>
                  
                  {openIndex === index && (
                    <div className="px-6 pb-6 text-medium-gray">
                      <p>{faq.answer}</p>
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
      
      {/* Contact Section */}
      <ContactSection />
      
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
  );
}