import { Home } from 'lucide-react';
import ContactSection from '@/react-app/components/ContactSection';

export default function PrivacyPolicyPage() {
  return (
    <div className="min-h-screen bg-black text-white">
      {/* Simple Navigation */}
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

      {/* Mobile Navigation */}
      <nav className="md:hidden flex items-center justify-between py-4 px-6 bg-black/80 backdrop-blur-md border-b border-charcoal-gray sticky top-0 z-50">
        <div className="flex items-center">
          <a href="/" className="flex items-center">
            <img src="/header.jpg" alt="EKEHI Logo" className="h-10 w-auto mr-2" />
          </a>
        </div>

        <a
          href="/"
          className="text-white hover:text-yellow-500 transition-colors flex items-center gap-1.5 text-sm font-medium"
        >
          <Home size={20} className="text-ekehi-gold" />
        </a>
      </nav>

      <div className="pt-20 pb-16 bg-gradient-to-b from-black to-charcoal-gray">
        <div className="container mx-auto px-6">
          <div className="text-center mb-12">
            <h1 className="text-4xl md:text-5xl font-bold mb-6 bg-gradient-to-r from-ekehi-gold to-yellow-500 bg-clip-text text-transparent">
              Privacy Policy
            </h1>
            <p className="text-xl text-medium-gray max-w-3xl mx-auto">
              Last Updated: October 9, 2025
            </p>
          </div>

          <div className="max-w-4xl mx-auto bg-black/30 border border-charcoal-gray rounded-xl p-6 md:p-8">
            <div className="prose prose-invert max-w-none">
              <h2 className="text-2xl font-bold text-white mb-4">Introduction</h2>
              <p className="text-medium-gray mb-6">
                Ekehi Network ("we," "our," or "us") is committed to protecting your privacy. This Privacy Policy explains how we collect, use, disclose, and safeguard your information when you use our mobile application and website. Please read this privacy policy carefully. If you do not agree with the terms of this privacy policy, please do not access the site or use the application.
              </p>

              <h2 className="text-2xl font-bold text-white mb-4 mt-8">Information We Collect</h2>
              <h3 className="text-xl font-semibold text-white mb-3">Personal Information</h3>
              <p className="text-medium-gray mb-4">
                We may collect personally identifiable information, such as your:
              </p>
              <ul className="list-disc list-inside text-medium-gray mb-6 ml-4">
                <li>Name</li>
                <li>Email address</li>
                <li>Phone number</li>
                <li>Google account information (when using Google Sign-In)</li>
                <li>Wallet address</li>
                <li>Referral information</li>
              </ul>

              <h3 className="text-xl font-semibold text-white mb-3">Usage Data</h3>
              <p className="text-medium-gray mb-4">
                We may also collect information that your browser or device sends whenever you visit our site or use our application ("Usage Data"). This Usage Data may include information such as:
              </p>
              <ul className="list-disc list-inside text-medium-gray mb-6 ml-4">
                <li>Your computer's Internet Protocol address (e.g., IP address)</li>
                <li>Browser type and version</li>
                <li>Pages of our site that you visit</li>
                <li>Time and date of your visit</li>
                <li>Time spent on those pages</li>
                <li>Unique device identifiers</li>
                <li>Device characteristics</li>
              </ul>

              <h2 className="text-2xl font-bold text-white mb-4 mt-8">Use of Your Information</h2>
              <p className="text-medium-gray mb-4">
                Having accurate information about you permits us to provide you with a smooth, efficient, and customized experience. Specifically, we may use information collected about you via the application or website to:
              </p>
              <ul className="list-disc list-inside text-medium-gray mb-6 ml-4">
                <li>Create and manage your account</li>
                <li>Facilitate cryptocurrency mining activities</li>
                <li>Process transactions and manage wallet functions</li>
                <li>Administer referral programs</li>
                <li>Send administrative information</li>
                <li>Respond to your comments and questions</li>
                <li>Improve our application and website</li>
                <li>Monitor and analyze usage and trends</li>
              </ul>

              <h2 className="text-2xl font-bold text-white mb-4 mt-8">Disclosure of Your Information</h2>
              <p className="text-medium-gray mb-4">
                We may share information we have collected about you in certain situations. Your information may be disclosed as follows:
              </p>

              <h3 className="text-xl font-semibold text-white mb-3">By Law or to Protect Rights</h3>
              <p className="text-medium-gray mb-6">
                If we believe the release of information about you is necessary to respond to legal process, to investigate or remedy potential violations of our policies, or to protect the rights, property, and safety of others, we may share your information as permitted or required by any applicable law, rule, or regulation.
              </p>

              <h3 className="text-xl font-semibold text-white mb-3">Third-Party Service Providers</h3>
              <p className="text-medium-gray mb-6">
                We may share your information with third parties that perform services for us or on our behalf, including payment processing, data analysis, email delivery, hosting services, customer service, and marketing assistance.
              </p>

              <h2 className="text-2xl font-bold text-white mb-4 mt-8">Security of Your Information</h2>
              <p className="text-medium-gray mb-6">
                We use administrative, technical, and physical security measures to help protect your personal information. While we have taken reasonable steps to secure the personal information you provide to us, please be aware that despite our efforts, no security measures are perfect or impenetrable, and no method of data transmission can be guaranteed against any interception or other type of misuse.
              </p>

              <h2 className="text-2xl font-bold text-white mb-4 mt-8">Contact Us</h2>
              <p className="text-medium-gray mb-6">
                If you have questions or comments about this Privacy Policy, please contact us at:
                <br /><br />
                Email: privacy@ekehi.xyz
              </p>
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