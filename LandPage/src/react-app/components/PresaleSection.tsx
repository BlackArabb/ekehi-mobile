import { DollarSign, Clock, TrendingUp, Shield, Users, Award } from 'lucide-react';

export default function PresaleSection() {
  return (
    <section id="presale" className="section-padding bg-rich-charcoal">
      <div className="container">
        {/* Section Header */}
        <div className="text-center mb-12 md:mb-16">
          <h2 className="text-h2 font-display text-gradient-gold mb-4 md:mb-6">
            Token Presale
          </h2>
          <p className="text-body text-soft-white max-w-3xl mx-auto px-4 md:px-0">
            Join our presale to get early access to EKH tokens at a discounted rate. 
            Be part of our journey to build a sustainable financial future.
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 md:gap-12">
          {/* Presale Info */}
          <div className="lg:col-span-2">
            <div className="bg-dark-slate rounded-2xl p-6 md:p-8 border border-charcoal-gray">
              <h3 className="text-h3 text-white mb-6">Presale Details</h3>
              
              {/* Presale Status */}
              <div className="bg-black/40 rounded-xl p-5 mb-8 border border-yellow-500/30">
                <div className="flex items-center gap-3 mb-4">
                  <div className="w-3 h-3 bg-green-500 rounded-full animate-pulse"></div>
                  <span className="text-lg font-semibold text-green-400">Presale Active</span>
                </div>
                <p className="text-soft-white">
                  Our presale is currently live! Get your EKH tokens at a 40% discount compared to the public sale price.
                </p>
              </div>
              
              {/* Presale Progress */}
              <div className="mb-8">
                <div className="flex justify-between items-center mb-3">
                  <span className="text-white font-medium">Presale Progress</span>
                  <span className="text-yellow-500 font-bold">25%</span>
                </div>
                <div className="w-full bg-charcoal-gray rounded-full h-3">
                  <div 
                    className="bg-gradient-to-r from-yellow-500 to-orange-500 h-3 rounded-full transition-all duration-1000"
                    style={{ width: '25%' }}
                  ></div>
                </div>
                <div className="flex justify-between text-sm text-medium-gray mt-2">
                  <span>1,250,000 EKH sold</span>
                  <span>5,000,000 EKH available</span>
                </div>
              </div>
              
              {/* Presale Benefits */}
              <div>
                <h4 className="text-lg font-semibold text-white mb-4">Presale Benefits</h4>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="flex items-start gap-3">
                    <TrendingUp className="text-green-500 mt-1 flex-shrink-0" size={20} />
                    <div>
                      <h5 className="font-medium text-white">40% Discount</h5>
                      <p className="text-sm text-soft-white">Save compared to public sale price</p>
                    </div>
                  </div>
                  <div className="flex items-start gap-3">
                    <Shield className="text-blue-500 mt-1 flex-shrink-0" size={20} />
                    <div>
                      <h5 className="font-medium text-white">Early Access</h5>
                      <p className="text-sm text-soft-white">Get tokens before public listing</p>
                    </div>
                  </div>
                  <div className="flex items-start gap-3">
                    <Users className="text-purple-500 mt-1 flex-shrink-0" size={20} />
                    <div>
                      <h5 className="font-medium text-white">VIP Community</h5>
                      <p className="text-sm text-soft-white">Access to exclusive presale group</p>
                    </div>
                  </div>
                  <div className="flex items-start gap-3">
                    <Award className="text-yellow-500 mt-1 flex-shrink-0" size={20} />
                    <div>
                      <h5 className="font-medium text-white">Bonus Rewards</h5>
                      <p className="text-sm text-soft-white">Additional tokens for early supporters</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          {/* Presale Info Sidebar */}
          <div>
            <div className="bg-dark-slate rounded-2xl p-6 md:p-8 border border-charcoal-gray sticky top-24">
              <h3 className="text-h3 text-white mb-6">Presale Information</h3>
              
              <div className="space-y-6">
                <div>
                  <h4 className="text-lg font-semibold text-white mb-3 flex items-center gap-2">
                    <DollarSign size={20} className="text-yellow-500" />
                    Token Pricing
                  </h4>
                  <div className="space-y-3">
                    <div className="flex justify-between">
                      <span className="text-medium-gray">Presale Price</span>
                      <span className="text-white font-medium">$0.03</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-medium-gray">Public Sale Price</span>
                      <span className="text-white font-medium">$0.05</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-medium-gray">Discount</span>
                      <span className="text-green-500 font-medium">40%</span>
                    </div>
                  </div>
                </div>
                
                <div>
                  <h4 className="text-lg font-semibold text-white mb-3 flex items-center gap-2">
                    <Clock size={20} className="text-yellow-500" />
                    Presale Timeline
                  </h4>
                  <div className="space-y-3">
                    <div className="flex justify-between">
                      <span className="text-medium-gray">Start Date</span>
                      <span className="text-white font-medium">Jan 15, 2025</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-medium-gray">End Date</span>
                      <span className="text-white font-medium">Mar 15, 2025</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-medium-gray">Token Distribution</span>
                      <span className="text-white font-medium">Mar 20, 2025</span>
                    </div>
                  </div>
                </div>
                
                <div>
                  <h4 className="text-lg font-semibold text-white mb-3">Acceptable Payment Methods</h4>
                  <div className="flex flex-wrap gap-2">
                    <span className="bg-yellow-500/10 text-yellow-500 px-3 py-1 rounded-full text-xs">
                      ETH
                    </span>
                    <span className="bg-yellow-500/10 text-yellow-500 px-3 py-1 rounded-full text-xs">
                      BTC
                    </span>
                    <span className="bg-yellow-500/10 text-yellow-500 px-3 py-1 rounded-full text-xs">
                      USDT
                    </span>
                    <span className="bg-yellow-500/10 text-yellow-500 px-3 py-1 rounded-full text-xs">
                      USDC
                    </span>
                  </div>
                </div>
                
                <div className="bg-black/40 rounded-xl p-4 border border-charcoal-gray">
                  <h4 className="font-semibold text-white mb-2">Important Notice</h4>
                  <p className="text-sm text-soft-white">
                    Tokens will be distributed to your connected wallet within 48 hours after the presale ends.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}