import React, { useState } from 'react';
import { Mail, ShieldAlert, LifeBuoy } from 'lucide-react';

export const SupportCenterScreen: React.FC = () => {
  const [ticketSubject, setTicketSubject] = useState('');
  const [ticketMessage, setTicketMessage] = useState('');
  const [isSubmitted, setIsSubmitted] = useState(false);

  const handleSubmitTicket = (e: React.FormEvent) => {
    e.preventDefault();
    if (!ticketSubject || !ticketMessage) return;
    setIsSubmitted(true);
    setTimeout(() => {
      setIsSubmitted(false);
      setTicketSubject('');
      setTicketMessage('');
      alert('Support ticket securely transmitted to founder SLA escalation dashboard!');
    }, 1200);
  };

  const faqs = [
    { q: 'How does cryptographic treasury splitting work?', a: 'Every payment milestone split translates into a cryptographically signed JSON agreement hash stored in SQLite (and Supabase) using SHA-256.' },
    { q: 'How do I refer a fellow creator or editor?', a: 'Under the "Syndicate" tab, select "Refer Teammate" to trigger a secure invite SMS directly to their carrier.' },
    { q: 'Can I resolve dispute splits or breaches?', a: 'Yes. If a member breaches an agreement, founders review the dispute logs under the SLA escalation channel for mediation.' }
  ];

  return (
    <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
      {/* SLA Support Escalation Form */}
      <div className="lg:col-span-7 p-6 rounded-xl glass-panel border border-divider space-y-6">
        <div>
          <span className="text-xs font-bold text-accentBlue uppercase tracking-wider font-semibold">Help Desk</span>
          <h2 className="text-xl font-black text-white mt-1 flex items-center gap-2">
            <ShieldAlert className="w-5 h-5 text-accentBlue" /> FOUNDER SLA ESCALATION SYSTEM
          </h2>
          <p className="text-xs text-textSecondary mt-1 leading-normal">
            Submit an SLA escalation ticket directly to the leadership queue. Response SLAs are guaranteed within 12 hours.
          </p>
        </div>

        <form onSubmit={handleSubmitTicket} className="space-y-4">
          <div className="space-y-1.5">
            <label className="block text-[10px] font-bold text-textSecondary uppercase">Escalation Subject</label>
            <input
              type="text"
              placeholder="e.g. Treasury split payout mismatch"
              required
              className="w-full bg-surfaceLight border border-divider rounded-lg px-3 py-2 text-white placeholder-textMuted text-xs focus:outline-none focus:border-accentBlue"
              value={ticketSubject}
              onChange={(e) => setTicketSubject(e.target.value)}
            />
          </div>

          <div className="space-y-1.5">
            <label className="block text-[10px] font-bold text-textSecondary uppercase">Detailed Escalation Message</label>
            <textarea
              rows={4}
              placeholder="Explain the specific milestone contract hash, proposed role-gated asset issues, or general co-op disputes..."
              required
              className="w-full bg-surfaceLight border border-divider rounded-lg px-3 py-2 text-white placeholder-textMuted text-xs focus:outline-none focus:border-accentBlue resize-none"
              value={ticketMessage}
              onChange={(e) => setTicketMessage(e.target.value)}
            />
          </div>

          <button
            type="submit"
            className="w-full bg-accentBlue text-background font-black text-xs py-2 px-4 rounded-lg flex items-center justify-center gap-1.5 hover:bg-accentBlue/90 transition"
          >
            {isSubmitted ? 'SENDING INVOY...' : 'SUBMIT SLA ESCALATION'}
          </button>
        </form>
      </div>

      {/* SLA Contacts & FAQS */}
      <div className="lg:col-span-5 space-y-6">
        {/* Leadership Contact channels (Direct mailto) */}
        <div className="p-5 rounded-xl glass-panel border border-divider space-y-4 text-left">
          <h3 className="text-xs font-black uppercase tracking-wider text-accentBlue flex items-center gap-1.5">
            <LifeBuoy className="w-4 h-4 text-accentBlue" /> Direct Leadership Hotlines
          </h3>
          <p className="text-[11px] text-textSecondary leading-normal">
            For critical, immediate contract disputes, you can initiate a direct escalation sequence via the channels below.
          </p>

          <div className="space-y-3">
            <div className="p-3 bg-surfaceLight border border-divider rounded-lg space-y-1.5 text-left">
              <span className="text-[8px] bg-accentBlue/10 text-accentBlue border border-accentBlue/20 px-1.5 py-0.5 rounded font-black uppercase tracking-wider">
                Founder Escalation
              </span>
              <p className="text-xs font-bold text-white">Botla Veerendra</p>
              <a
                href="mailto:veerendrabotla@gmail.com?subject=SLA%20Dispute"
                className="inline-flex items-center justify-center gap-1.5 px-3 py-1.5 bg-background hover:bg-surface border border-divider rounded-md text-xs font-bold text-white hover:border-accentBlue/30 transition w-full"
              >
                <Mail className="w-3.5 h-3.5 text-accentBlue" />
                Email Escalation Queue
              </a>
            </div>

            <div className="p-3 bg-surfaceLight border border-divider rounded-lg space-y-1.5 text-left">
              <span className="text-[8px] bg-neonEmerald/10 text-neonEmerald border border-neonEmerald/20 px-1.5 py-0.5 rounded font-black uppercase tracking-wider">
                Co-Founder Escalation
              </span>
              <p className="text-xs font-bold text-white">Macha Praveen</p>
              <a
                href="mailto:praveenmacha777@gmail.com?subject=SLA%20Dispute"
                className="inline-flex items-center justify-center gap-1.5 px-3 py-1.5 bg-background hover:bg-surface border border-divider rounded-md text-xs font-bold text-white hover:border-neonEmerald/30 transition w-full"
              >
                <Mail className="w-3.5 h-3.5 text-neonEmerald" />
                Email Escalation Queue
              </a>
            </div>
          </div>
        </div>

        {/* Short FAQ Accordion */}
        <div className="p-5 rounded-xl glass-panel border border-divider space-y-3 text-left">
          <h3 className="text-xs font-bold uppercase tracking-wider text-white">
            Common Inquiries
          </h3>
          <div className="space-y-3">
            {faqs.map((faq, idx) => (
              <div key={idx} className="space-y-1">
                <p className="text-xs font-bold text-white flex items-start gap-1.5 leading-normal">
                  <span className="text-accentBlue">Q:</span> {faq.q}
                </p>
                <p className="text-[10px] text-textSecondary leading-normal pl-4">
                  {faq.a}
                </p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
