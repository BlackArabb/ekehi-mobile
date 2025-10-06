import { useState } from 'react';
import { Mail, MessageSquare, Phone, MapPin, Send, ExternalLink, Users, ChevronDown, CheckCircle } from 'lucide-react';

const contactMethods = [
  {
    icon: Mail,
    title: 'Email Us',
    description: 'Get in touch for partnerships and support',
    contact: 'hello@ekehi.com',
    link: 'mailto:hello@ekehi.com'
  },
  {
    icon: MessageSquare,
    title: 'Telegram',
    description: 'Join our community discussion',
    contact: '@ekehi_official',
    link: 'https://t.me/ekehi_official'
  },
  {
    icon: Phone,
    title: 'Business Inquiries',
    description: 'For institutional partnerships',
    contact: 'partnerships@ekehi.com',
    link: 'mailto:partnerships@ekehi.com'
  },
  {
    icon: MapPin,
    title: 'Location',
    description: 'Registered in Delaware, USA',
    contact: 'Global Remote Team',
    link: '#'
  }
];

const socialLinks = [
  { name: 'Telegram', url: 'https://t.me/ekehi_official', users: '10K+', icon: 'T' },
  { name: 'Twitter', url: 'https://twitter.com/ekehi_official', users: '25K+', icon: 'X' },
  { name: 'Discord', url: 'https://discord.gg/ekehi', users: '5K+', icon: 'D' },
  { name: 'Reddit', url: 'https://reddit.com/r/ekehi', users: '3K+', icon: 'R' }
];

export default function ContactSection() {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    subject: '',
    message: ''
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitStatus, setSubmitStatus] = useState<'idle' | 'success' | 'error'>('idle');
  const [expandedMethod, setExpandedMethod] = useState<number | null>(null);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    
    try {
      // Simulate form submission
      await new Promise(resolve => setTimeout(resolve, 2000));
      setSubmitStatus('success');
      setFormData({ name: '', email: '', subject: '', message: '' });
    } catch (error) {
      setSubmitStatus('error');
    } finally {
      setIsSubmitting(false);
      setTimeout(() => setSubmitStatus('idle'), 5000);
    }
  };

  const toggleMethod = (index: number) => {
    setExpandedMethod(expandedMethod === index ? null : index);
  };

  return (
    <section id="contact" className="section-padding bg-rich-charcoal">
      <div className="container">
        {/* Section Header */}
        <div className="text-center mb-12 md:mb-16">
          <h2 className="text-h2 font-display text-gradient-gold mb-4 md:mb-6">
            Get in Touch
          </h2>
          <p className="text-body text-soft-white max-w-3xl mx-auto px-4 md:px-0">
            Have questions? Want to partner with us? We'd love to hear from you. 
            Reach out and let's build the future of finance together.
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 md:gap-12">
          {/* Contact Information */}
          <div className="lg:col-span-1 space-y-6 md:space-y-8">
            <div>
              <h3 className="text-h3 text-white mb-6">Contact Information</h3>
              <div className="space-y-4 md:space-y-6">
                {contactMethods.map((method, index) => {
                  const IconComponent = method.icon;
                  const isExpanded = expandedMethod === index;
                  
                  return (
                    <div
                      key={index}
                      className="bg-dark-slate rounded-xl border border-charcoal-gray hover:border-yellow-500 transition-all duration-300 group cursor-pointer"
                      onClick={() => toggleMethod(index)}
                    >
                      <div className="flex items-center gap-4 p-4">
                        <div className="w-12 h-12 bg-yellow-500/10 rounded-full flex items-center justify-center group-hover:bg-yellow-500/20 transition-colors">
                          <IconComponent size={24} className="text-yellow-500" />
                        </div>
                        <div className="flex-1">
                          <h4 className="font-semibold text-white mb-1">{method.title}</h4>
                          <p className="text-soft-white text-xs md:text-sm mb-2">{method.description}</p>
                          <span className="text-yellow-500 text-sm font-medium">{method.contact}</span>
                        </div>
                        <ChevronDown 
                          size={20} 
                          className={`text-yellow-500 transition-transform duration-300 ${isExpanded ? 'rotate-180' : ''}`} 
                        />
                      </div>
                      
                      {isExpanded && (
                        <div className="px-4 pb-4">
                          <a
                            href={method.link}
                            target={method.link.startsWith('http') ? '_blank' : '_self'}
                            rel={method.link.startsWith('http') ? 'noopener noreferrer' : ''}
                            className="btn-secondary w-full py-2 text-center text-sm"
                          >
                            {method.link.startsWith('http') ? 'Visit' : 'Contact'}
                            <ExternalLink size={16} className="inline ml-2" />
                          </a>
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Social Community */}
            <div>
              <h4 className="text-lg font-semibold text-white mb-4 flex items-center gap-2">
                <Users size={20} className="text-yellow-500" />
                Join Our Community
              </h4>
              <div className="grid grid-cols-2 gap-3">
                {socialLinks.map((social, index) => (
                  <a
                    key={index}
                    href={social.url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex items-center justify-between bg-black/40 hover:bg-black/60 rounded-lg p-3 transition-colors group"
                  >
                    <div className="flex items-center gap-2">
                      <div className="w-8 h-8 rounded-full bg-yellow-500/10 flex items-center justify-center text-yellow-500 font-bold text-sm">
                        {social.icon}
                      </div>
                      <span className="text-white text-sm font-medium">{social.name}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-xs text-medium-gray">{social.users}</span>
                      <ExternalLink size={12} className="text-yellow-500 opacity-0 group-hover:opacity-100 transition-opacity" />
                    </div>
                  </a>
                ))}
              </div>
            </div>
          </div>

          {/* Contact Form */}
          <div className="lg:col-span-2">
            <div className="bg-dark-slate rounded-2xl p-6 md:p-8 border border-charcoal-gray">
              <h3 className="text-h3 text-white mb-6 md:mb-8">Send us a Message</h3>
              
              <form onSubmit={handleSubmit} className="space-y-5 md:space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 md:gap-6">
                  <div>
                    <label htmlFor="name" className="block text-white font-medium mb-2 text-sm">
                      Name <span className="text-red-400">*</span>
                    </label>
                    <input
                      type="text"
                      id="name"
                      name="name"
                      value={formData.name}
                      onChange={handleInputChange}
                      required
                      placeholder="Your full name"
                      className="w-full bg-black border border-charcoal-gray rounded-lg px-4 py-3 text-white placeholder-medium-gray focus:border-yellow-500 focus:outline-none transition-colors text-sm"
                    />
                  </div>
                  <div>
                    <label htmlFor="email" className="block text-white font-medium mb-2 text-sm">
                      Email <span className="text-red-400">*</span>
                    </label>
                    <input
                      type="email"
                      id="email"
                      name="email"
                      value={formData.email}
                      onChange={handleInputChange}
                      required
                      placeholder="your.email@example.com"
                      className="w-full bg-black border border-charcoal-gray rounded-lg px-4 py-3 text-white placeholder-medium-gray focus:border-yellow-500 focus:outline-none transition-colors text-sm"
                    />
                  </div>
                </div>

                <div>
                  <label htmlFor="subject" className="block text-white font-medium mb-2 text-sm">
                    Subject <span className="text-red-400">*</span>
                  </label>
                  <input
                    type="text"
                    id="subject"
                    name="subject"
                    value={formData.subject}
                    onChange={handleInputChange}
                    required
                    placeholder="What's this about?"
                    className="w-full bg-black border border-charcoal-gray rounded-lg px-4 py-3 text-white placeholder-medium-gray focus:border-yellow-500 focus:outline-none transition-colors text-sm"
                  />
                </div>

                <div>
                  <label htmlFor="message" className="block text-white font-medium mb-2 text-sm">
                    Message <span className="text-red-400">*</span>
                    {formData.message.length > 0 && (
                      <span className="text-medium-gray text-xs ml-2">
                        ({200 - formData.message.length} characters remaining)
                      </span>
                    )}
                  </label>
                  <textarea
                    id="message"
                    name="message"
                    value={formData.message}
                    onChange={handleInputChange}
                    required
                    maxLength={200}
                    rows={5}
                    placeholder="Tell us more about your inquiry, partnership proposal, or any questions you have about Ekehi..."
                    className="w-full bg-black border border-charcoal-gray rounded-lg px-4 py-3 text-white placeholder-medium-gray focus:border-yellow-500 focus:outline-none transition-colors resize-none text-sm"
                  />
                </div>

                {/* Submit Button */}
                <div className="flex items-center justify-between">
                  <button
                    type="submit"
                    disabled={isSubmitting}
                    className="btn-primary disabled:opacity-50 disabled:cursor-not-allowed py-3 px-6 text-sm"
                  >
                    {isSubmitting ? (
                      <>
                        <div className="w-4 h-4 border-2 border-black/20 border-t-black rounded-full animate-spin mr-2"></div>
                        Sending...
                      </>
                    ) : (
                      <>
                        <Send size={18} className="mr-2" />
                        Send Message
                      </>
                    )}
                  </button>

                  {/* Status Messages */}
                  {submitStatus === 'success' && (
                    <div className="text-green-500 text-sm font-medium flex items-center gap-1">
                      <CheckCircle size={16} />
                      Message sent successfully!
                    </div>
                  )}
                  {submitStatus === 'error' && (
                    <div className="text-red-400 text-sm font-medium">
                      ✗ Failed to send message. Please try again.
                    </div>
                  )}
                </div>
              </form>

              {/* Additional Info */}
              <div className="mt-6 md:mt-8 pt-6 border-t border-charcoal-gray">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-3 md:gap-4 text-xs text-soft-white">
                  <div className="flex items-center gap-2">
                    <span className="text-green-400">⚡</span>
                    <span>Usually respond within 24 hours</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-blue-400">🔒</span>
                    <span>Your information is secure</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-yellow-400">📧</span>
                    <span>All inquiries welcome</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* FAQ Link */}
        <div className="text-center mt-12 md:mt-16 px-4">
          <div className="bg-black/40 rounded-xl p-5 md:p-6 border border-charcoal-gray max-w-2xl mx-auto">
            <h4 className="text-lg font-semibold text-white mb-3">Frequently Asked Questions</h4>
            <p className="text-body-small md:text-body text-soft-white mb-4">
              Looking for quick answers? Check out our comprehensive FAQ section covering 
              tokenomics, presale details, and technical questions.
            </p>
            <a href="#whitepaper" className="btn-secondary inline-flex items-center gap-2 py-2.5 px-5 text-sm">
              View FAQ Section
              <ExternalLink size={14} />
            </a>
          </div>
        </div>
      </div>
    </section>
  );
}