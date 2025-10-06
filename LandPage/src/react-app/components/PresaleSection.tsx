import { useState } from 'react';
import { Copy, ExternalLink, DollarSign, TrendingUp, Shield, Clock, Check, ChevronDown, Info } from 'lucide-react';

const paymentMethods = [
  {
    name: 'USDT',
    network: 'TRC20/ERC20',
    address: 'TQn9Y2khEsLJW1ChVWFMSMeRDow5oNHmz1',
    icon: '₮',
    color: 'text-green-500',
  },
  {
    name: 'BNB',
    network: 'BEP20',
    address: '0x742d35Cc622C4532e1b4c22f5b8B4D5D87b0F9b8',
    icon: '⚡',
    color: 'text-yellow-500',
  },
  {
    name: 'ETH',
    network: 'ERC20',
    address: '0x742d35Cc622C4532e1b4c22f5b8B4D5D87b0F9b8',
    icon: 'Ξ',
    color: 'text-blue-400',
  },
  {
    name: 'MATIC',
    network: 'Polygon',
    address: '0x742d35Cc622C4532e1b4c22f5b8B4D5D87b0F9b8',
    icon: '◆',
    color: 'text-purple-500',
  },
  {
    name: 'ARB',
    network: 'Arbitrum',
    address: '0x742d35Cc622C4532e1b4c22f5b8B4D5D87b0F9b8',
    icon: '🔷',
    color: 'text-blue-600',
  },
  {
    name: 'SOL',
    network: 'Solana',
    address: 'HN7cABqLq46Es1jh92dQQisAq662SmxELLLsHHe4YWrH',
    icon: '◉',
    color: 'text-gradient-purple',
  },
];

const tokenInfo = {
  name: 'Ekehi (EKH)',
  standard: 'ERC-20',
  presalePrice: 0.05,
  launchPrice: 0.10,
  minPurchase: 50,
  maxPurchase: 50000,
  tokensSold: 45650000,
  totalTokens: 400000000,
  raisedAmount: 2282500,
  targetAmount: 10000000,
};

const faqItems = [
  {
    question: "How do I purchase EKH tokens?",
    answer: "Select your preferred payment method, send the amount to the provided wallet address, and submit the purchase form with your transaction details."
  },
  {
    question: "When will I receive my tokens?",
    answer: "Tokens are distributed within 48 hours after successful verification of your transaction."
  },
  {
    question: "Is there a minimum purchase amount?",
    answer: "Yes, the minimum purchase is $50 worth of tokens."
  }
];

export default function PresaleSection() {
  const [copiedAddress, setCopiedAddress] = useState('');
  const [showInstructions, setShowInstructions] = useState(false);
  const [activeFaq, setActiveFaq] = useState<number | null>(null);

  const progressPercentage = (tokenInfo.tokensSold / tokenInfo.totalTokens) * 100;

  const copyToClipboard = (address: string, methodName: string) => {
    navigator.clipboard.writeText(address);
    setCopiedAddress(methodName);
    setTimeout(() => setCopiedAddress(''), 2000);
  };

  const toggleFaq = (index: number) => {
    setActiveFaq(activeFaq === index ? null : index);
  };

  return (
    <section id="presale" className="section-padding bg-rich-charcoal">
      <div className="container">
        {/* Section Header */}
        <div className="text-center mb-12 md:mb-16">
          <h2 className="text-h2 font-display text-gradient-gold mb-4 md:mb-6">
            Join the Ekehi Presale
          </h2>
          <p className="text-body text-soft-white max-w-3xl mx-auto px-4 md:px-0 mb-6 md:mb-8">
            Be part of the future of cryptocurrency. Secure your EKH tokens at exclusive 
            presale rates before public launch.
          </p>
          
          {/* Key Benefits */}
          <div className="flex flex-wrap justify-center gap-4 md:gap-6 mb-8">
            <div className="flex items-center gap-2 bg-black/40 px-3 py-1.5 md:px-4 md:py-2 rounded-full">
              <TrendingUp size={14} className="md:size-16 text-green-500" />
              <span className="text-xs md:text-sm text-white">100% ROI at Launch</span>
            </div>
            <div className="flex items-center gap-2 bg-black/40 px-3 py-1.5 md:px-4 md:py-2 rounded-full">
              <Shield size={14} className="md:size-16 text-blue-500" />
              <span className="text-xs md:text-sm text-white">CertiK Audited</span>
            </div>
            <div className="flex items-center gap-2 bg-black/40 px-3 py-1.5 md:px-4 md:py-2 rounded-full">
              <Clock size={14} className="md:size-16 text-yellow-500" />
              <span className="text-xs md:text-sm text-white">Limited Time</span>
            </div>
          </div>
        </div>

        {/* Main Presale Panel */}
        <div className="max-w-6xl mx-auto bg-dark-slate border-t-4 border-yellow-500 rounded-2xl md:rounded-3xl shadow-gold-lg overflow-hidden">
          <div className="p-6 md:p-8 md:p-12">
            {/* Token Information Grid */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 md:gap-6 mb-8 md:mb-10">
              <div className="text-center bg-black/20 rounded-xl p-4">
                <div className="text-xl md:text-2xl font-bold text-gradient-gold">
                  ${tokenInfo.presalePrice}
                </div>
                <div className="text-medium-gray text-xs md:text-sm">Presale Price</div>
              </div>
              <div className="text-center bg-black/20 rounded-xl p-4">
                <div className="text-xl md:text-2xl font-bold text-gradient-gold">
                  ${tokenInfo.launchPrice}
                </div>
                <div className="text-medium-gray text-xs md:text-sm">Launch Price</div>
              </div>
              <div className="text-center bg-black/20 rounded-xl p-4">
                <div className="text-xl md:text-2xl font-bold text-gradient-gold">
                  ${tokenInfo.minPurchase}
                </div>
                <div className="text-medium-gray text-xs md:text-sm">Min Purchase</div>
              </div>
              <div className="text-center bg-black/20 rounded-xl p-4">
                <div className="text-xl md:text-2xl font-bold text-gradient-gold">
                  ${tokenInfo.maxPurchase.toLocaleString()}
                </div>
                <div className="text-medium-gray text-xs md:text-sm">Max Purchase</div>
              </div>
            </div>

            {/* Progress Section */}
            <div className="mb-8 md:mb-10">
              <div className="flex justify-between items-center mb-3 md:mb-4">
                <h3 className="text-h4 text-white">Presale Progress</h3>
                <span className="text-yellow-500 font-semibold text-sm md:text-base">
                  {progressPercentage.toFixed(1)}% Complete
                </span>
              </div>
              
              <div className="relative h-4 md:h-6 bg-charcoal-gray rounded-full overflow-hidden mb-3 md:mb-4">
                <div 
                  className="absolute inset-0 bg-gradient-to-r from-yellow-500 to-amber-glow rounded-full transition-all duration-1000 ease-out"
                  style={{ width: `${progressPercentage}%` }}
                >
                  <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/20 to-transparent animate-pulse"></div>
                </div>
                <div className="absolute inset-0 flex items-center justify-center text-xs font-semibold text-black">
                  {tokenInfo.tokensSold.toLocaleString()} / {tokenInfo.totalTokens.toLocaleString()} EKH
                </div>
              </div>

              <div className="flex justify-between text-xs md:text-sm text-medium-gray">
                <span>Raised: ${tokenInfo.raisedAmount.toLocaleString()}</span>
                <span>Target: ${tokenInfo.targetAmount.toLocaleString()}</span>
              </div>
            </div>

            {/* Payment Methods */}
            <div className="mb-8 md:mb-10">
              <h3 className="text-h4 text-white mb-4 md:mb-6 text-center">Choose Payment Method</h3>
              
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3 md:gap-4">
                {paymentMethods.map((method) => (
                  <div
                    key={method.name}
                    className="card group hover:border-yellow-500 transition-all duration-300 p-4 md:p-5"
                  >
                    <div className="flex items-center justify-between mb-3 md:mb-4">
                      <div className="flex items-center gap-2 md:gap-3">
                        <span className={`text-xl md:text-2xl ${method.color}`}>{method.icon}</span>
                        <div>
                          <div className="font-semibold text-white text-sm md:text-base">{method.name}</div>
                          <div className="text-xs text-medium-gray">{method.network}</div>
                        </div>
                      </div>
                    </div>
                    
                    <div className="bg-black border border-dashed border-yellow-500 rounded-lg p-2 md:p-3 mb-3">
                      <div className="font-mono text-xs text-white break-all">
                        {method.address}
                      </div>
                    </div>
                    
                    <button
                      onClick={() => copyToClipboard(method.address, method.name)}
                      className="w-full flex items-center justify-center gap-2 bg-yellow-500/10 hover:bg-yellow-500/20 text-yellow-500 py-2 px-3 rounded-lg transition-colors text-xs md:text-sm font-medium"
                    >
                      {copiedAddress === method.name ? (
                        <>
                          <Check size={14} className="md:size-16" />
                          Copied!
                        </>
                      ) : (
                        <>
                          <Copy size={14} className="md:size-16" />
                          Copy Address
                        </>
                      )}
                    </button>
                  </div>
                ))}
              </div>
            </div>

            {/* Action Buttons */}
            <div className="flex flex-col sm:flex-row gap-3 md:gap-4 justify-center items-center mb-8">
              <button
                onClick={() => setShowInstructions(true)}
                className="btn-primary group py-3 px-6 md:py-4 md:px-8 text-sm md:text-base"
              >
                <DollarSign size={18} className="md:size-20" />
                How to Purchase
                <ExternalLink size={14} className="md:size-16 opacity-0 group-hover:opacity-100 transition-opacity" />
              </button>
              
              <a
                href="https://forms.google.com/ekehi-presale"
                target="_blank"
                rel="noopener noreferrer"
                className="btn-secondary group py-3 px-6 md:py-4 md:px-8 text-sm md:text-base"
              >
                Submit Purchase Form
                <ExternalLink size={14} className="md:size-16" />
              </a>
            </div>

            {/* FAQ Section */}
            <div className="mb-8">
              <h3 className="text-h4 text-white mb-4 flex items-center gap-2">
                <Info size={20} className="text-yellow-500" />
                Frequently Asked Questions
              </h3>
              <div className="space-y-3">
                {faqItems.map((item, index) => (
                  <div 
                    key={index} 
                    className="bg-black/20 rounded-xl border border-charcoal-gray overflow-hidden"
                  >
                    <button
                      className="w-full flex justify-between items-center p-4 text-left"
                      onClick={() => toggleFaq(index)}
                    >
                      <span className="text-white font-medium text-sm md:text-base">{item.question}</span>
                      <ChevronDown 
                        size={20} 
                        className={`text-yellow-500 transition-transform duration-300 ${activeFaq === index ? 'rotate-180' : ''}`} 
                      />
                    </button>
                    {activeFaq === index && (
                      <div className="px-4 pb-4 text-soft-white text-sm md:text-base">
                        {item.answer}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>

            {/* Important Notes */}
            <div className="bg-black/40 rounded-xl p-5 md:p-6">
              <h4 className="text-lg font-semibold text-white mb-4">⚠️ Important Notes</h4>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3 md:gap-4 text-xs md:text-sm text-soft-white">
                <div className="flex items-start gap-2">
                  <span className="text-red-400 mt-1">⚠️</span>
                  <span>Send only supported cryptocurrencies to respective addresses</span>
                </div>
                <div className="flex items-start gap-2">
                  <span className="text-red-400 mt-1">⚠️</span>
                  <span>Double-check wallet addresses before sending</span>
                </div>
                <div className="flex items-start gap-2">
                  <span className="text-red-400 mt-1">⚠️</span>
                  <span>Minimum purchase: $50 | Maximum: $50,000</span>
                </div>
                <div className="flex items-start gap-2">
                  <span className="text-red-400 mt-1">⚠️</span>
                  <span>Presale tokens locked until official launch</span>
                </div>
                <div className="flex items-start gap-2">
                  <span className="text-green-400 mt-1">✓</span>
                  <span>Smart contract audited by CertiK</span>
                </div>
                <div className="flex items-start gap-2">
                  <span className="text-green-400 mt-1">✓</span>
                  <span>Liquidity locked for 2 years</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Instructions Modal */}
      {showInstructions && (
        <div className="fixed inset-0 z-50 bg-black/90 backdrop-blur-lg flex items-center justify-center p-4">
          <div className="bg-dark-slate border-2 border-yellow-500 rounded-2xl p-6 md:p-8 max-w-2xl w-full max-h-[80vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-6">
              <h3 className="text-h3 text-gradient-gold">How to Purchase EKH Tokens</h3>
              <button
                onClick={() => setShowInstructions(false)}
                className="text-medium-gray hover:text-white transition-colors text-2xl"
              >
                ×
              </button>
            </div>

            <div className="space-y-5">
              <div className="flex gap-3 md:gap-4">
                <div className="w-7 h-7 md:w-8 md:h-8 bg-yellow-500 rounded-full flex items-center justify-center text-black font-bold text-sm">1</div>
                <div>
                  <h4 className="text-base md:text-lg font-semibold text-white mb-2">Choose Your Payment Method</h4>
                  <p className="text-body-small md:text-body text-soft-white">Select from USDT, BNB, ETH, MATIC, ARB, or SOL</p>
                </div>
              </div>

              <div className="flex gap-3 md:gap-4">
                <div className="w-7 h-7 md:w-8 md:h-8 bg-yellow-500 rounded-full flex items-center justify-center text-black font-bold text-sm">2</div>
                <div>
                  <h4 className="text-base md:text-lg font-semibold text-white mb-2">Calculate Your Purchase</h4>
                  <p className="text-body-small md:text-body text-soft-white">
                    Enter amount in USD. 1 EKH = $0.05 during presale. 
                    Example: $100 = 2,000 EKH tokens
                  </p>
                </div>
              </div>

              <div className="flex gap-3 md:gap-4">
                <div className="w-7 h-7 md:w-8 md:h-8 bg-yellow-500 rounded-full flex items-center justify-center text-black font-bold text-sm">3</div>
                <div>
                  <h4 className="text-base md:text-lg font-semibold text-white mb-2">Send Payment</h4>
                  <p className="text-body-small md:text-body text-soft-white">
                    Copy wallet address and send exact amount from your wallet. 
                    Save the transaction hash for verification.
                  </p>
                </div>
              </div>

              <div className="flex gap-3 md:gap-4">
                <div className="w-7 h-7 md:w-8 md:h-8 bg-yellow-500 rounded-full flex items-center justify-center text-black font-bold text-sm">4</div>
                <div>
                  <h4 className="text-base md:text-lg font-semibold text-white mb-2">Submit Transaction</h4>
                  <p className="text-body-small md:text-body text-soft-white">
                    Fill our Google Form with: your wallet address, transaction hash, 
                    amount sent, payment currency, and email address.
                  </p>
                </div>
              </div>

              <div className="flex gap-3 md:gap-4">
                <div className="w-7 h-7 md:w-8 md:h-8 bg-yellow-500 rounded-full flex items-center justify-center text-black font-bold text-sm">5</div>
                <div>
                  <h4 className="text-base md:text-lg font-semibold text-white mb-2">Confirmation</h4>
                  <p className="text-body-small md:text-body text-soft-white">
                    Receive email confirmation within 24 hours. 
                    Tokens distributed within 48 hours to your wallet.
                  </p>
                </div>
              </div>
            </div>

            <div className="mt-6 md:mt-8 text-center">
              <a
                href="https://forms.google.com/ekehi-presale"
                target="_blank"
                rel="noopener noreferrer"
                className="btn-primary py-3 px-6 md:py-4 md:px-8 text-sm md:text-base"
              >
                <ExternalLink size={18} className="md:size-20" />
                Submit Purchase Form
              </a>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}