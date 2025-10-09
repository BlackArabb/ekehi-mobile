import { Home } from 'lucide-react';
import ContactSection from '@/react-app/components/ContactSection';

export default function TermsPage() {
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
              Terms and Conditions
            </h1>
            <p className="text-xl text-medium-gray max-w-3xl mx-auto">
              Last Updated: October 9, 2025
            </p>
          </div>

      <div className="max-w-4xl mx-auto bg-black/30 border border-charcoal-gray rounded-xl p-6 md:p-8">
        <div className="prose prose-invert max-w-none">
          <h2 className="text-2xl font-bold text-white mb-4">Introduction</h2>
          <p className="text-medium-gray mb-6">
            These Terms and Conditions ("Terms") govern your access to and use of the Ekehi Network mobile application and website (collectively, the "Service"), operated by Ekehi Network ("we," "our," or "us"). By accessing or using the Service, you agree to be bound by these Terms. If you disagree with any part of the terms, then you may not access the Service.
          </p>

          <h2 className="text-2xl font-bold text-white mb-4 mt-8">Eligibility</h2>
          <p className="text-medium-gray mb-6">
            By using the Service, you represent and warrant that you are at least 18 years of age and have the legal capacity to enter into these Terms. If you are using the Service on behalf of an entity, you represent and warrant that you have the authority to bind that entity to these Terms.
          </p>

          <h2 className="text-2xl font-bold text-white mb-4 mt-8">Account Registration</h2>
          <p className="text-medium-gray mb-4">
            To access certain features of the Service, you may be required to register for an account. You agree to:
          </p>
          <ul className="list-disc list-inside text-medium-gray mb-6 ml-4">
            <li>Provide accurate, current, and complete information during registration</li>
            <li>Maintain and promptly update your account information</li>
            <li>Maintain the security of your account credentials</li>
            <li>Notify us immediately of any unauthorized use of your account</li>
          </ul>

          <h2 className="text-2xl font-bold text-white mb-4 mt-8">Use of Service</h2>
          <p className="text-medium-gray mb-4">
            You agree not to:
          </p>
          <ul className="list-disc list-inside text-medium-gray mb-6 ml-4">
            <li>Use the Service for any illegal or unauthorized purpose</li>
            <li>Interfere with or disrupt the Service or servers connected to the Service</li>
            <li>Attempt to gain unauthorized access to any portion of the Service</li>
            <li>Transmit any viruses or malicious code</li>
            <li>Use the Service to transmit spam or unsolicited commercial communications</li>
          </ul>

          <h2 className="text-2xl font-bold text-white mb-4 mt-8">Cryptocurrency and Wallet Services</h2>
          <p className="text-medium-gray mb-4">
            The Service may facilitate interactions with cryptocurrency tokens. You acknowledge that:
          </p>
          <ul className="list-disc list-inside text-medium-gray mb-6 ml-4">
            <li>Cryptocurrency transactions are irreversible</li>
            <li>We are not responsible for any loss of tokens due to user error</li>
            <li>Token values are highly volatile and subject to market risks</li>
            <li>We do not provide investment advice</li>
          </ul>

          <h2 className="text-2xl font-bold text-white mb-4 mt-8">Intellectual Property</h2>
          <p className="text-medium-gray mb-6">
            The Service and its original content, features, and functionality are and will remain the exclusive property of Ekehi Network and its licensors. The Service is protected by copyright, trademark, and other laws of both the United States and foreign countries.
          </p>

          <h2 className="text-2xl font-bold text-white mb-4 mt-8">Termination</h2>
          <p className="text-medium-gray mb-6">
            We may terminate or suspend access to our Service immediately, without prior notice or liability, for any reason whatsoever, including without limitation if you breach the Terms.
          </p>

          <h2 className="text-2xl font-bold text-white mb-4 mt-8">Limitation of Liability</h2>
          <p className="text-medium-gray mb-6">
            In no event shall Ekehi Network, nor its directors, employees, partners, agents, suppliers, or affiliates, be liable for any indirect, incidental, special, consequential or punitive damages, including without limitation, loss of profits, data, use, goodwill, or other intangible losses, resulting from your access to or use of or inability to access or use the Service.
          </p>

          <h2 className="text-2xl font-bold text-white mb-4 mt-8">Changes to Terms</h2>
          <p className="text-medium-gray mb-6">
            We reserve the right, at our sole discretion, to modify or replace these Terms at any time. If a revision is material, we will provide at least 30 days' notice prior to any new terms taking effect. What constitutes a material change will be determined at our sole discretion.
          </p>

          <h2 className="text-2xl font-bold text-white mb-4 mt-8">Contact Us</h2>
          <p className="text-medium-gray mb-6">
            If you have any questions about these Terms, please contact us at:
            <br /><br />
            Email: legal@ekehi.xyz
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