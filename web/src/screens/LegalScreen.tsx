import React from 'react';
import { ShieldCheck, Info, Shield, Scale } from 'lucide-react';

export const LegalScreen: React.FC = () => {
  return (
    <div className="space-y-6 text-left select-none animate-fadeIn">
      {/* Header Panel */}
      <div className="bg-surface border border-divider p-6 rounded-2xl flex flex-col sm:flex-row items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="p-3 bg-accentRed/15 rounded-2xl text-accentRed">
            <Scale className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-xl font-black text-white uppercase tracking-tight">Legal & Compliance Vault</h1>
            <p className="text-xs text-textSecondary uppercase tracking-wider font-semibold">Effective Date: June 20, 2026 • Version 1.0.0</p>
          </div>
        </div>
        
        <div className="px-3 py-1 bg-accentRed/10 border border-accentRed/30 rounded-xl text-accentRed text-[10px] font-black uppercase tracking-wider">
          Compliance Verified
        </div>
      </div>

      {/* PRE-LAUNCH DISCLAIMER */}
      <div className="bg-crispAmber/10 border border-crispAmber/30 rounded-2xl p-6 flex gap-4">
        <Info className="w-6 h-6 text-crispAmber shrink-0 mt-0.5" />
        <div className="space-y-1">
          <h3 className="text-xs font-black text-crispAmber uppercase tracking-wider">PRE-LAUNCH LEGAL REVIEW NOTICE</h3>
          <p className="text-xs text-textSecondary leading-relaxed">
            This document represents the complete Terms of Service, Privacy Policy, and deletion cascades for the Creator Co-Op platform. While it is fully detailed, we recommend a secondary quick human review by legal advisors before executing full commercial production pipelines.
          </p>
        </div>
      </div>

      {/* Main Content Sections */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Terms of Service Card */}
        <div className="bg-surface border border-divider p-6 rounded-2xl space-y-4">
          <h3 className="text-sm font-black text-white uppercase tracking-wider flex items-center gap-2 border-b border-divider pb-3">
            <Shield className="w-4 h-4 text-accentRed" /> TERMS OF SERVICE
          </h3>

          <div className="space-y-4 text-xs">
            <div className="space-y-1">
              <h4 className="font-bold text-white">1. Creator Collaboration Scope</h4>
              <p className="text-textSecondary leading-relaxed">
                Creator Co-Op is a peer-to-peer workspace coordination, scheduling, and joint media production syndicate tracker. It provides tools for team alignment, collaborative workflows, and draft logging.
              </p>
            </div>

            <div className="space-y-1">
              <h4 className="font-bold text-white">2. No Financial or Investment Advice</h4>
              <p className="text-textSecondary leading-relaxed">
                All milestone splits, splits visualization, or referral multipliers displayed on this application are non-binding simulation tools and do not constitute registered investment, broker-dealer, or tax advisory services.
              </p>
            </div>

            <div className="space-y-1">
              <h4 className="font-bold text-white">3. Content Intellectual Property</h4>
              <p className="text-textSecondary leading-relaxed">
                Users retain full copyright of media assets uploaded to their workspace vaults. Creator Co-Op asserts no ownership over workspace files or channel metrics metadata synchronized via Supabase.
              </p>
            </div>
          </div>
        </div>

        {/* Privacy Policy Card */}
        <div className="bg-surface border border-divider p-6 rounded-2xl space-y-4">
          <h3 className="text-sm font-black text-white uppercase tracking-wider flex items-center gap-2 border-b border-divider pb-3">
            <ShieldCheck className="w-4 h-4 text-neonEmerald" /> PRIVACY & DATA CASCADE
          </h3>

          <div className="space-y-4 text-xs">
            <div className="space-y-1">
              <h4 className="font-bold text-white">1. Data Collected & Encryption</h4>
              <p className="text-textSecondary leading-relaxed">
                We collect email addresses, chosen usernames, channel statistics (subscriber metrics), support logs, and peer agreements. Feeds are transferred over TLS-1.3 with full AES-GCM-256 cloud encryption.
              </p>
            </div>

            <div className="space-y-1">
              <h4 className="font-bold text-white">2. Absolute Account Deletion Cascade</h4>
              <p className="text-textSecondary leading-relaxed">
                To guarantee absolute creator integrity, deleting your account initiates a complete cascade sequence: all profiles, active agreements, support tickets, file logs, and metrics stored on our ledger or Supabase are deleted in real-time.
              </p>
            </div>

            <div className="space-y-1">
              <h4 className="font-bold text-white">3. Direct Escalation Pathways</h4>
              <p className="text-textSecondary leading-relaxed flex flex-wrap gap-2 items-center">
                For privacy compliance inquiries, you may escalate directly to our founding team: 
                <a href="mailto:veerendrabotla@gmail.com" className="text-accentBlue hover:underline">veerendrabotla@gmail.com</a> (Botla Veerendra) or 
                <a href="mailto:praveenmacha777@gmail.com" className="text-accentBlue hover:underline">praveenmacha777@gmail.com</a> (Macha Praveen).
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
