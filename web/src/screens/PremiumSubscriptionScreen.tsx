import React from 'react';
import { Check, Sparkles, ShieldCheck } from 'lucide-react';

export const PremiumSubscriptionScreen: React.FC = () => {
  const plans = [
    {
      name: 'CREATOR BASIC',
      price: '$0',
      period: 'Forever',
      desc: 'Perfect for individual content writers, editing interns, and standalone vloggers.',
      features: [
        'Up to 3 active workspaces',
        'Standard Kanban pipeline lanes',
        'Local SQLite data persistence',
        '10 S3 download tokens per month'
      ],
      cta: 'Current Plan',
      active: true,
      accent: 'border-divider',
      button: 'bg-surfaceLight hover:bg-surface text-white border border-divider'
    },
    {
      name: 'CREATOR PREMIUM',
      price: '$29',
      period: '/mo',
      desc: 'Engineered for full-time channels, video editors, and established content creators.',
      features: [
        'Unlimited workspaces and files',
        'Custom adaptive render pipelines',
        'Automatic cloud sync with Supabase',
        'DocuSign mutual agreements vault',
        'Prioritized 12h Founder SLA tickets'
      ],
      cta: 'Upgrade to Premium',
      active: false,
      popular: true,
      accent: 'border-accentBlue ring-2 ring-accentBlue/20',
      button: 'bg-accentBlue text-background font-black hover:bg-accentBlue/90 shadow-lg'
    },
    {
      name: 'SYNDICATE ENTERPRISE',
      price: '$99',
      period: '/mo',
      desc: 'Optimized for visual effect agencies, video production companies, and collaborative co-ops.',
      features: [
        'Everything in Creator Premium',
        'Dynamic treasury splits panel',
        'Role-gated multi-signature contracts',
        'Real-time WebSocket huddle channels',
        'Founder SLA escalation priority queue'
      ],
      cta: 'Provision Syndicate',
      active: false,
      accent: 'border-crispAmber/40',
      button: 'bg-crispAmber text-background font-black hover:bg-crispAmber/90 shadow-lg'
    }
  ];

  return (
    <div className="space-y-6">
      {/* Visual Hero */}
      <div className="p-6 rounded-2xl glass-panel relative overflow-hidden flex flex-col md:flex-row items-center justify-between border border-divider">
        <div className="absolute top-0 right-0 w-64 h-64 bg-crispAmber/5 rounded-full blur-3xl pointer-events-none"></div>
        <div className="text-left space-y-2">
          <span className="text-xs font-bold text-crispAmber uppercase tracking-wider flex items-center gap-1">
            <Sparkles className="w-3.5 h-3.5" /> Platform Packages
          </span>
          <h2 className="text-2xl font-black text-white">UNLOCK CREATOR PREMIUM</h2>
          <p className="text-sm text-textSecondary max-w-2xl">
            Power up your co-op with automatic cloud synchronization, legal DocuSign agreement vaults, real-time splits auditing, and direct Founder SLA support.
          </p>
        </div>
      </div>

      {/* Subscription cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {plans.map((p, idx) => (
          <div
            key={idx}
            className={`p-6 rounded-2xl bg-surface border flex flex-col justify-between text-left relative transition hover:scale-[1.01] duration-300 ${p.accent}`}
          >
            {p.popular && (
              <span className="absolute top-4 right-4 bg-accentBlue/10 text-accentBlue border border-accentBlue/30 text-[9px] font-black tracking-wider uppercase px-2 py-0.5 rounded-full">
                Most Popular
              </span>
            )}

            <div className="space-y-5">
              <div>
                <h3 className="text-xs font-black text-textSecondary uppercase tracking-wider">{p.name}</h3>
                <div className="flex items-baseline gap-1 mt-2">
                  <span className="text-4xl font-black text-white">{p.price}</span>
                  <span className="text-xs text-textSecondary">{p.period}</span>
                </div>
                <p className="text-[11px] text-textSecondary mt-3 leading-relaxed">{p.desc}</p>
              </div>

              <div className="space-y-2.5 pt-4 border-t border-divider">
                {p.features.map((f, fIdx) => (
                  <div key={fIdx} className="flex items-start gap-2 text-xs">
                    <Check className="w-3.5 h-3.5 text-neonEmerald shrink-0 mt-0.5" />
                    <span className="text-textSecondary leading-normal">{f}</span>
                  </div>
                ))}
              </div>
            </div>

            <button
              type="button"
              onClick={() => {
                if (!p.active) {
                  alert(`Initializing Stripe checkout pipeline for ${p.name}...`);
                }
              }}
              className={`w-full py-2.5 px-4 rounded-xl text-xs font-black transition mt-6 ${p.button}`}
            >
              {p.active ? 'Active Plan' : p.cta}
            </button>
          </div>
        ))}
      </div>

      {/* Trust banner */}
      <div className="p-4 rounded-xl bg-surfaceLight border border-divider flex items-center justify-center gap-2.5 max-w-xl mx-auto">
        <ShieldCheck className="w-5 h-5 text-neonEmerald shrink-0" />
        <p className="text-[10px] text-textSecondary font-bold text-left uppercase tracking-wider">
          PCI-Compliant Encryption • End-to-end stripe checkout handshakes securely configured
        </p>
      </div>
    </div>
  );
};
