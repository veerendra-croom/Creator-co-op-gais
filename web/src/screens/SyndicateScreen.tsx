import React, { useState } from 'react';
import { PieChart, ShieldCheck, Mail, Send, Award } from 'lucide-react';

export const SyndicateScreen: React.FC = () => {
  const [referralEmail, setReferralEmail] = useState('');
  const [referralRole, setReferralRole] = useState('Editor');

  // Active Syndicate splits (Interactive)
  const [splits, setSplits] = useState([
    { name: 'Botla Veerendra (Founder/SRE)', role: 'Lead Director', share: 45 },
    { name: 'Macha Praveen (Co-Founder/Ops)', role: 'Strategic Operations', share: 40 },
    { name: 'Srinivas Rao (Beta Member)', role: 'Video Editor & Compositor', share: 15 }
  ]);

  const handleSendReferral = (e: React.FormEvent) => {
    e.preventDefault();
    if (!referralEmail) return;
    setTimeout(() => {
      setReferralEmail('');
      alert(`Handshake referral invitation sent to ${referralEmail}!`);
    }, 200);
  };

  const updateSplit = (index: number, newShare: number) => {
    const updated = [...splits];
    updated[index].share = Math.max(0, Math.min(100, newShare));
    setSplits(updated);
  };

  const totalSplit = splits.reduce((acc, curr) => acc + curr.share, 0);

  return (
    <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
      {/* Treasury Share Allocation Splits */}
      <div className="lg:col-span-8 p-6 rounded-xl glass-panel border border-divider space-y-6">
        <div>
          <span className="text-xs font-bold text-accentBlue uppercase tracking-wider">Revenue Vault</span>
          <h2 className="text-xl font-black text-white mt-1 flex items-center gap-2">
            <PieChart className="w-5 h-5 text-accentBlue" /> SYNDICATE TREASURY ALLOCATIONS
          </h2>
          <p className="text-xs text-textSecondary mt-1">
            Dynamic milestone payout shares allocated to each co-op member. Splits are cryptographic proofs.
          </p>
        </div>

        <div className="space-y-4">
          {splits.map((s, idx) => (
            <div key={idx} className="p-4 rounded-xl bg-surfaceLight border border-divider space-y-3">
              <div className="flex justify-between items-center">
                <div>
                  <h4 className="text-xs font-bold text-white flex items-center gap-1.5">
                    {s.name} {idx < 2 && <Award className="w-3.5 h-3.5 text-accentBlue inline" />}
                  </h4>
                  <p className="text-[10px] text-textSecondary">{s.role}</p>
                </div>
                <div className="flex items-center gap-3">
                  <input
                    type="number"
                    className="w-16 bg-background border border-divider rounded px-2 py-1 text-right text-xs text-white font-bold"
                    value={s.share}
                    onChange={(e) => updateSplit(idx, parseInt(e.target.value) || 0)}
                  />
                  <span className="text-xs text-textMuted font-bold">%</span>
                </div>
              </div>
              <div className="w-full h-2 bg-background rounded-full overflow-hidden border border-divider">
                <div
                  className="h-full bg-accentBlue transition-all duration-300"
                  style={{ width: `${s.share}%` }}
                ></div>
              </div>
            </div>
          ))}
        </div>

        <div className="pt-4 border-t border-divider flex justify-between items-center text-xs font-bold">
          <span className="text-textSecondary">CURRENT TOTAL ALLOCATION</span>
          <span className={`${totalSplit === 100 ? 'text-neonEmerald' : 'text-accentRed'}`}>
            {totalSplit}% {totalSplit === 100 ? '(Perfect Anchored)' : '(Split imbalance!)'}
          </span>
        </div>
      </div>

      {/* Right Column: Invite & Refer Member */}
      <div className="lg:col-span-4 space-y-6">
        {/* Founders Executive Info Card */}
        <div className="p-5 rounded-xl glass-panel border border-divider space-y-4 text-left">
          <h3 className="text-xs font-black uppercase tracking-wider text-accentBlue flex items-center gap-1.5">
            <ShieldCheck className="w-4 h-4" /> Operational Leadership
          </h3>
          <p className="text-[11px] text-textSecondary leading-normal">
            Creator Co-Op was founded on the principles of direct collaboration, shared equity, and cryptographic dispute resolutions.
          </p>

          <div className="space-y-3 pt-2">
            <div className="p-3 rounded-lg bg-surfaceLight border border-divider space-y-1.5">
              <p className="text-xs font-bold text-white">Botla Veerendra</p>
              <p className="text-[9px] text-accentBlue font-bold uppercase tracking-wider">Founder & Executive Director</p>
              <a
                href="mailto:veerendrabotla@gmail.com"
                className="inline-flex items-center gap-1.5 text-[10px] text-textSecondary hover:text-white transition"
              >
                <Mail className="w-3 h-3 text-accentBlue" /> veerendrabotla@gmail.com
              </a>
            </div>

            <div className="p-3 rounded-lg bg-surfaceLight border border-divider space-y-1.5">
              <p className="text-xs font-bold text-white">Macha Praveen</p>
              <p className="text-[9px] text-neonEmerald font-bold uppercase tracking-wider">Co-Founder & Operations Director</p>
              <a
                href="mailto:praveenmacha777@gmail.com"
                className="inline-flex items-center gap-1.5 text-[10px] text-textSecondary hover:text-white transition"
              >
                <Mail className="w-3.5 h-3.5 text-neonEmerald" /> praveenmacha777@gmail.com
              </a>
            </div>
          </div>
        </div>

        {/* Refer a Teammate Sandbox Form */}
        <div className="p-5 rounded-xl glass-panel border border-divider space-y-4">
          <h3 className="text-xs font-bold uppercase tracking-wider text-white">
            Refer Teammate (SMS)
          </h3>
          <form onSubmit={handleSendReferral} className="space-y-4">
            <div className="space-y-1.5">
              <label className="block text-[10px] font-bold text-textSecondary uppercase">Teammate Email</label>
              <input
                type="email"
                placeholder="editor@creatorcoop.com"
                required
                className="w-full bg-surfaceLight border border-divider rounded-lg px-3 py-2 text-white placeholder-textMuted text-xs focus:outline-none focus:border-accentBlue"
                value={referralEmail}
                onChange={(e) => setReferralEmail(e.target.value)}
              />
            </div>

            <div className="space-y-1.5">
              <label className="block text-[10px] font-bold text-textSecondary uppercase">Proposed Syndicate Role</label>
              <select
                className="w-full bg-surfaceLight border border-divider rounded-lg px-3 py-2 text-white text-xs focus:outline-none focus:border-accentBlue"
                value={referralRole}
                onChange={(e) => setReferralRole(e.target.value)}
              >
                <option value="Editor">Video Editor</option>
                <option value="Audio SRE">Sound Engineer</option>
                <option value="VFX Lead">Visual Effects Artist</option>
                <option value="Scriptwriter">Co-Writer</option>
              </select>
            </div>

            <button
              type="submit"
              className="w-full bg-accentBlue text-background font-black text-xs py-2 px-4 rounded-lg flex items-center justify-center gap-1.5 hover:bg-accentBlue/90 transition"
            >
              <Send className="w-3 h-3" /> SEND HANDSHAKE REFERRAL
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};
