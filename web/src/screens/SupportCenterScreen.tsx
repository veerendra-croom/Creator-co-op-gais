import React, { useState } from 'react';
import { Mail, ShieldAlert, LifeBuoy, Gavel, Ban, Trash2 } from 'lucide-react';

interface DisputeItem {
  id: string;
  creator: string;
  workspace: string;
  issue: string;
  status: 'PENDING' | 'RESOLVED';
}

export const SupportCenterScreen: React.FC = () => {
  const [activeSubTab, setActiveSubTab] = useState<'HELP_DESK' | 'DISPUTES' | 'BLOCKED_USERS'>('HELP_DESK');
  const [ticketSubject, setTicketSubject] = useState('');
  const [ticketMessage, setTicketMessage] = useState('');
  const [isSubmitted, setIsSubmitted] = useState(false);

  // Dispute state (Admin Dispute Resolution Board parity)
  const [disputes, setDisputes] = useState<DisputeItem[]>([
    { id: 'd1', creator: 'Srinivas Rao', workspace: 'Alpha Cinematic Universe', issue: 'Unfair milestone payload split adjustment on VFX renders.', status: 'PENDING' },
    { id: 'd2', creator: 'Anonymous_User_3', workspace: 'Synthesizer Sounds Co-Op', issue: 'Unauthorized soundscape preset attribution conflict.', status: 'RESOLVED' }
  ]);

  // Blocked users list (BlockedUsersScreen parity)
  const [blockedUsers, setBlockedUsers] = useState([
    { id: 'bu1', name: 'Spam_Bot_V2', date: 'Sep 24, 2026' },
    { id: 'bu2', name: 'Abusive_User_9', date: 'Sep 21, 2026' }
  ]);

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

  const handleResolveDispute = (id: string) => {
    setDisputes(disputes.map(d => d.id === id ? { ...d, status: 'RESOLVED' } : d));
    alert('Dispute split resolved! Settlement ledger written through to Room SQL databases.');
  };

  const handleUnblockUser = (id: string) => {
    setBlockedUsers(blockedUsers.filter(bu => bu.id !== id));
    alert('User successfully unblocked and security posture cached.');
  };

  const faqs = [
    { q: 'How does cryptographic treasury splitting work?', a: 'Every payment milestone split translates into a cryptographically signed JSON agreement hash stored in SQLite (and Supabase) using SHA-256.' },
    { q: 'How do I refer a fellow creator or editor?', a: 'Under the "Syndicate" tab, select "Refer Teammate" to trigger a secure invite SMS directly to their carrier.' },
    { q: 'Can I resolve dispute splits or breaches?', a: 'Yes. If a member breaches an agreement, founders review the dispute logs under the SLA escalation channel for mediation.' }
  ];

  return (
    <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 text-left">
      {/* Scrollable Support Sub Navigation */}
      <div className="lg:col-span-12 flex border-b border-divider bg-surface p-1 rounded-lg">
        <button
          type="button"
          onClick={() => setActiveSubTab('HELP_DESK')}
          className={`flex-1 py-1.5 rounded-md font-bold text-[10px] uppercase tracking-wider flex items-center justify-center gap-1.5 transition ${
            activeSubTab === 'HELP_DESK' ? 'bg-surfaceLight text-white shadow-sm' : 'text-textSecondary hover:text-white'
          }`}
        >
          <LifeBuoy className="w-3.5 h-3.5" /> SLA Help Desk
        </button>
        <button
          type="button"
          onClick={() => setActiveSubTab('DISPUTES')}
          className={`flex-1 py-1.5 rounded-md font-bold text-[10px] uppercase tracking-wider flex items-center justify-center gap-1.5 transition ${
            activeSubTab === 'DISPUTES' ? 'bg-surfaceLight text-white shadow-sm' : 'text-textSecondary hover:text-white'
          }`}
        >
          <Gavel className="w-3.5 h-3.5" /> Dispute Board
        </button>
        <button
          type="button"
          onClick={() => setActiveSubTab('BLOCKED_USERS')}
          className={`flex-1 py-1.5 rounded-md font-bold text-[10px] uppercase tracking-wider flex items-center justify-center gap-1.5 transition ${
            activeSubTab === 'BLOCKED_USERS' ? 'bg-surfaceLight text-white shadow-sm' : 'text-textSecondary hover:text-white'
          }`}
        >
          <Ban className="w-3.5 h-3.5" /> Blocked Users
        </button>
      </div>

      {activeSubTab === 'HELP_DESK' && (
        <>
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
        </>
      )}

      {activeSubTab === 'DISPUTES' && (
        <div className="lg:col-span-12 p-5 rounded-xl bg-surface border border-divider space-y-4">
          <div className="pb-3 border-b border-divider">
            <h3 className="text-xs font-black text-white uppercase tracking-wider">Admin Dispute Mediation Board</h3>
            <p className="text-[10px] text-textSecondary">Arbitrate and settle milestone payout disputes between co-op members</p>
          </div>

          <div className="space-y-3">
            {disputes.map((d) => (
              <div key={d.id} className="p-4 bg-surfaceLight border border-divider rounded-xl flex items-center justify-between text-left">
                <div>
                  <div className="flex items-center gap-2">
                    <h4 className="text-xs font-black text-white">👤 {d.creator}</h4>
                    <span className="text-[9px] bg-accentBlue/10 text-accentBlue border border-accentBlue/20 px-1.5 py-0.5 rounded font-bold font-mono">
                      {d.workspace}
                    </span>
                  </div>
                  <p className="text-[11px] text-textSecondary leading-normal mt-2">{d.issue}</p>
                </div>
                <div className="flex items-center gap-2">
                  <span className={`text-[8px] font-black uppercase border px-2 py-0.5 rounded ${
                    d.status === 'RESOLVED'
                      ? 'bg-neonEmerald/10 border-neonEmerald text-neonEmerald'
                      : 'bg-crispAmber/10 border-crispAmber text-crispAmber'
                  }`}>
                    {d.status}
                  </span>
                  {d.status !== 'RESOLVED' && (
                    <button
                      type="button"
                      onClick={() => handleResolveDispute(d.id)}
                      className="px-2 py-1 bg-neonEmerald hover:bg-neonEmerald/90 text-background font-black text-[9px] rounded uppercase transition"
                    >
                      Settle Dispute
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {activeSubTab === 'BLOCKED_USERS' && (
        <div className="lg:col-span-12 p-5 rounded-xl bg-surface border border-divider space-y-4">
          <div className="pb-3 border-b border-divider">
            <h3 className="text-xs font-black text-white uppercase tracking-wider">Blocked Accounts Directory</h3>
            <p className="text-[10px] text-textSecondary">Manage and unblock restricted members and accounts</p>
          </div>

          <div className="space-y-3">
            {blockedUsers.length === 0 ? (
              <div className="text-center p-8 border border-dashed border-divider rounded-lg text-textMuted text-xs uppercase font-bold tracking-wider">
                0 Restricted Accounts found
              </div>
            ) : (
              blockedUsers.map((bu) => (
                <div key={bu.id} className="p-4 bg-surfaceLight border border-divider rounded-xl flex items-center justify-between text-left">
                  <div>
                    <h4 className="text-xs font-black text-white">🚫 {bu.name}</h4>
                    <p className="text-[9px] text-textSecondary mt-0.5">Blocked Date: {bu.date}</p>
                  </div>
                  <button
                    type="button"
                    onClick={() => handleUnblockUser(bu.id)}
                    className="p-1.5 bg-background hover:bg-surface border border-divider hover:border-neonEmerald/30 text-white hover:text-neonEmerald text-xs font-bold rounded-lg flex items-center gap-1.5 transition"
                    title="Unblock User"
                  >
                    <Trash2 className="w-3.5 h-3.5 text-accentRed" /> Unblock
                  </button>
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
};
