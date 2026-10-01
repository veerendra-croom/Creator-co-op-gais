import React, { useState } from 'react';
import { Users, UserPlus, Search, Filter, AlertTriangle, Star, ChevronRight, PhoneCall, Sparkles, X, Mail } from 'lucide-react';

interface CrmRecord {
  id: string;
  name: string;
  email: string;
  channelName: string;
  subscribers: number;
  status: 'ACTIVE' | 'NEEDS_FOLLOW_UP' | 'CHAMPION' | 'CHURN_RISK';
  notes: string;
  lastContactDate: string;
}

export const FounderCrmScreen: React.FC = () => {
  const [selectedTab, setSelectedTab] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('All');
  const [showAddModal, setShowAddModal] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState<CrmRecord | null>(null);

  // Add record form states
  const [newName, setNewName] = useState('');
  const [newEmail, setNewEmail] = useState('');
  const [newChannel, setNewChannel] = useState('');
  const [newSubs, setNewSubs] = useState(10000);
  const [newStatus, setNewStatus] = useState<'ACTIVE' | 'NEEDS_FOLLOW_UP' | 'CHAMPION' | 'CHURN_RISK'>('ACTIVE');
  const [newNotes, setNewNotes] = useState('');

  const [records, setRecords] = useState<CrmRecord[]>([
    { id: '1', name: 'Alex Mercer', email: 'alex@creatorcoop.com', channelName: 'Alex Tech Hacks', subscribers: 250000, status: 'CHAMPION', notes: 'Top advocate. Ready for early smart contract milestone splits testing.', lastContactDate: '2026-09-28' },
    { id: '2', name: 'Maya Lin', email: 'maya@blenderhq.org', channelName: 'Maya 3D Creations', subscribers: 180000, status: 'ACTIVE', notes: 'VFX designer. Wants integration for secure media vault asset distribution.', lastContactDate: '2026-09-29' },
    { id: '3', name: 'Thomas Wright', email: 'thomas@stems.net', channelName: 'Wright Stems & Beats', subscribers: 95000, status: 'NEEDS_FOLLOW_UP', notes: 'Audio technician. Needs confirmation on stereo stem routing ratios.', lastContactDate: '2026-09-15' },
    { id: '4', name: 'David Miller', email: 'david@lensvlog.com', channelName: 'Miller Cine Vlogs', subscribers: 340000, status: 'CHURN_RISK', notes: 'Delayed integration. Has had 3 connection timeouts on local build.', lastContactDate: '2026-09-10' },
    { id: '5', name: 'Sophia Chen', email: 'sophia@gamingdaily.cn', channelName: 'Daily Game Reviews', subscribers: 520000, status: 'CHAMPION', notes: 'Extremely active. Eager to onboard her secondary co-producers.', lastContactDate: '2026-09-30' },
  ]);

  const handleAddRecord = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newName || !newEmail) return;

    const newRec: CrmRecord = {
      id: Date.now().toString(),
      name: newName,
      email: newEmail,
      channelName: newChannel || 'Independent Creator',
      subscribers: Number(newSubs) || 0,
      status: newStatus,
      notes: newNotes || 'No additional records documented.',
      lastContactDate: new Date().toISOString().split('T')[0]
    };

    setRecords(prev => [newRec, ...prev]);
    setShowAddModal(false);
    
    // Reset forms
    setNewName('');
    setNewEmail('');
    setNewChannel('');
    setNewSubs(10000);
    setNewStatus('ACTIVE');
    setNewNotes('');
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'CHAMPION': return 'text-neonEmerald border-neonEmerald/30 bg-neonEmerald/10';
      case 'NEEDS_FOLLOW_UP': return 'text-crispAmber border-crispAmber/30 bg-crispAmber/10';
      case 'CHURN_RISK': return 'text-accentRed border-accentRed/30 bg-accentRed/10';
      default: return 'text-accentBlue border-accentBlue/30 bg-accentBlue/10';
    }
  };

  // Filter records
  const filteredRecords = records.filter(rec => {
    const matchesSearch = rec.name.toLowerCase().includes(searchQuery.toLowerCase()) || 
                          rec.channelName.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          rec.email.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesStatus = statusFilter === 'All' || rec.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  // Calculate statistics
  const totalSubscribers = records.reduce((sum, r) => sum + r.subscribers, 0);
  const championCount = records.filter(r => r.status === 'CHAMPION').length;
  const followUpCount = records.filter(r => r.status === 'NEEDS_FOLLOW_UP').length;
  const churnRiskCount = records.filter(r => r.status === 'CHURN_RISK').length;

  return (
    <div className="space-y-6 text-left select-none">
      {/* Header Panel */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-surface border border-divider p-6 rounded-2xl">
        <div className="flex items-center gap-3">
          <div className="p-3 bg-neonEmerald/15 rounded-2xl text-neonEmerald">
            <Users className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-xl font-black text-white uppercase tracking-tight">YC Beta Cohort CRM Command</h1>
            <p className="text-xs text-textSecondary uppercase tracking-wider font-semibold">Founder Pipeline & Creator Health telemetry</p>
          </div>
        </div>

        <div className="flex gap-2">
          <button
            type="button"
            onClick={() => setShowAddModal(true)}
            className="px-4 py-2 bg-gradient-to-r from-neonEmerald to-neonEmerald/80 hover:from-neonEmerald/90 hover:to-neonEmerald text-background text-xs font-black rounded-xl flex items-center gap-2 transition shadow-md"
          >
            <UserPlus className="w-4 h-4" /> ONBOARD CREATOR
          </button>
        </div>
      </div>

      {/* SLA TABS */}
      <div className="flex border-b border-divider">
        <button
          type="button"
          onClick={() => setSelectedTab(0)}
          className={`px-6 py-3 text-xs font-black uppercase tracking-wider border-b-2 transition ${
            selectedTab === 0 
              ? 'border-neonEmerald text-white font-black' 
              : 'border-transparent text-textSecondary hover:text-white'
          }`}
        >
          📈 Executive Analytics
        </button>
        <button
          type="button"
          onClick={() => setSelectedTab(1)}
          className={`px-6 py-3 text-xs font-black uppercase tracking-wider border-b-2 transition ${
            selectedTab === 1 
              ? 'border-neonEmerald text-white font-black' 
              : 'border-transparent text-textSecondary hover:text-white'
          }`}
        >
          🔍 Founders View
        </button>
        <button
          type="button"
          onClick={() => setSelectedTab(2)}
          className={`px-6 py-3 text-xs font-black uppercase tracking-wider border-b-2 transition ${
            selectedTab === 2 
              ? 'border-neonEmerald text-white font-black' 
              : 'border-transparent text-textSecondary hover:text-white'
          }`}
        >
          📖 Cohort Directory ({records.length})
        </button>
      </div>

      {/* CONTENT TABS */}
      {selectedTab === 0 && (
        /* --- TAB 0: EXECUTIVE ANALYTICS --- */
        <div className="space-y-6 animate-fadeIn">
          {/* Metric Dashboard cards */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="bg-surface p-5 border border-divider rounded-2xl flex flex-col justify-between">
              <span className="text-[10px] font-bold text-textSecondary uppercase tracking-widest">Total Creators</span>
              <p className="text-3xl font-black text-white mt-2">{records.length}</p>
              <span className="text-[9px] text-neonEmerald font-black mt-2">100% RETENTION PROJ.</span>
            </div>

            <div className="bg-surface p-5 border border-divider rounded-2xl flex flex-col justify-between">
              <span className="text-[10px] font-bold text-textSecondary uppercase tracking-widest">Aggregate Reach</span>
              <p className="text-3xl font-black text-white mt-2">{(totalSubscribers / 1000000).toFixed(2)}M</p>
              <span className="text-[9px] text-accentBlue font-black mt-2">SUBSCRIBERS YC FEED</span>
            </div>

            <div className="bg-surface p-5 border border-divider rounded-2xl flex flex-col justify-between">
              <span className="text-[10px] font-bold text-textSecondary uppercase tracking-widest">Champions List</span>
              <p className="text-3xl font-black text-neonEmerald mt-2">{championCount}</p>
              <span className="text-[9px] text-neonEmerald/80 font-black mt-2">HIGH CONVERSIONS</span>
            </div>

            <div className="bg-surface p-5 border border-divider rounded-2xl flex flex-col justify-between">
              <span className="text-[10px] font-bold text-textSecondary uppercase tracking-widest">Critical Risks</span>
              <p className="text-3xl font-black text-accentRed mt-2">{churnRiskCount}</p>
              <span className="text-[9px] text-accentRed/80 font-black mt-2">IMMEDIATE CALL REQ.</span>
            </div>
          </div>

          {/* CRM Health Check Guidance */}
          <div className="bg-surfaceLight p-6 border border-divider rounded-2xl flex flex-col md:flex-row items-center gap-6">
            <div className="p-4 bg-crispAmber/15 text-crispAmber rounded-2xl">
              <Sparkles className="w-8 h-8 animate-pulse" />
            </div>
            <div className="space-y-1">
              <h3 className="text-sm font-black text-white uppercase tracking-wider">CREATOR SLA COMPLIANCE POLICY</h3>
              <p className="text-xs text-textSecondary leading-relaxed">
                Platform Co-Founders <strong className="text-white font-bold">Botla Veerendra</strong> and <strong className="text-white font-bold">Macha Praveen</strong> mandate that any Beta Creator flagged as <span className="text-crispAmber font-bold">NEEDS_FOLLOW_UP</span> must be contacted within a 72-hour window. Direct escalation pathways are mapped for VIP creators exceeding $10k recurring value.
              </p>
            </div>
          </div>
        </div>
      )}

      {selectedTab === 1 && (
        /* --- TAB 1: FOUNDERS VIEW --- */
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 animate-fadeIn">
          {/* Needing follow up list */}
          <div className="bg-surface border border-divider rounded-2xl p-5 space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-divider">
              <h3 className="text-xs font-black text-crispAmber uppercase tracking-wider flex items-center gap-2">
                <PhoneCall className="w-4 h-4" /> NEEDING FOLLOW-UP ({followUpCount})
              </h3>
            </div>
            <div className="space-y-3">
              {records.filter(r => r.status === 'NEEDS_FOLLOW_UP').map(rec => (
                <div key={rec.id} className="p-3 bg-surfaceLight border border-divider rounded-xl space-y-2 hover:border-crispAmber/40 transition">
                  <div className="flex justify-between items-center">
                    <span className="text-xs font-bold text-white">{rec.name}</span>
                    <span className="text-[9px] text-textMuted font-mono">LC: {rec.lastContactDate}</span>
                  </div>
                  <p className="text-[10px] text-textSecondary leading-relaxed italic">{rec.notes}</p>
                </div>
              ))}
              {followUpCount === 0 && <p className="text-xs text-textMuted text-center py-4">All clear! No followups scheduled.</p>}
            </div>
          </div>

          {/* High potential champions list */}
          <div className="bg-surface border border-divider rounded-2xl p-5 space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-divider">
              <h3 className="text-xs font-black text-neonEmerald uppercase tracking-wider flex items-center gap-2">
                <Star className="w-4 h-4" /> PLATFORM CHAMPIONS ({championCount})
              </h3>
            </div>
            <div className="space-y-3">
              {records.filter(r => r.status === 'CHAMPION').map(rec => (
                <div key={rec.id} className="p-3 bg-surfaceLight border border-divider rounded-xl space-y-2 hover:border-neonEmerald/40 transition">
                  <div className="flex justify-between items-center">
                    <span className="text-xs font-bold text-white">{rec.name}</span>
                    <span className="text-[9px] text-textMuted font-mono">Reach: {(rec.subscribers/1000).toFixed(0)}k</span>
                  </div>
                  <p className="text-[10px] text-textSecondary leading-relaxed">{rec.notes}</p>
                </div>
              ))}
            </div>
          </div>

          {/* Churn Risk Lists */}
          <div className="bg-surface border border-divider rounded-2xl p-5 space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-divider">
              <h3 className="text-xs font-black text-accentRed uppercase tracking-wider flex items-center gap-2">
                <AlertTriangle className="w-4 h-4" /> CHURN WARNINGS ({churnRiskCount})
              </h3>
            </div>
            <div className="space-y-3">
              {records.filter(r => r.status === 'CHURN_RISK').map(rec => (
                <div key={rec.id} className="p-3 bg-surfaceLight border border-divider rounded-xl space-y-2 border-accentRed/20 hover:border-accentRed/40 transition">
                  <div className="flex justify-between items-center">
                    <span className="text-xs font-bold text-white">{rec.name}</span>
                    <span className="text-[9px] text-accentRed font-bold font-mono">CRITICAL</span>
                  </div>
                  <p className="text-[10px] text-textSecondary leading-relaxed italic">{rec.notes}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {selectedTab === 2 && (
        /* --- TAB 2: COHORT DIRECTORY --- */
        <div className="bg-surface border border-divider rounded-2xl overflow-hidden animate-fadeIn space-y-4 p-5">
          {/* Controls filtering bar */}
          <div className="flex flex-col sm:flex-row gap-3">
            <div className="flex-1 relative">
              <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-textMuted">
                <Search className="w-4 h-4" />
              </span>
              <input
                type="text"
                placeholder="Query name, channel, or registration email..."
                className="w-full bg-surfaceLight border border-divider rounded-xl py-2 pl-9 pr-4 text-xs text-white placeholder-textMuted focus:outline-none focus:border-neonEmerald transition"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            <div className="flex gap-2">
              <div className="relative">
                <span className="absolute inset-y-0 left-0 pl-2.5 flex items-center text-textMuted">
                  <Filter className="w-3.5 h-3.5" />
                </span>
                <select
                  className="bg-surfaceLight border border-divider rounded-xl py-2 pl-8 pr-4 text-xs text-white focus:outline-none focus:border-neonEmerald appearance-none transition"
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value)}
                >
                  <option value="All">All Statuses</option>
                  <option value="ACTIVE">Active</option>
                  <option value="CHAMPION">Champion</option>
                  <option value="NEEDS_FOLLOW_UP">Needs Follow-Up</option>
                  <option value="CHURN_RISK">Churn Risk</option>
                </select>
              </div>
            </div>
          </div>

          {/* Directory Grid */}
          <div className="space-y-2">
            {filteredRecords.map((rec) => (
              <div 
                key={rec.id} 
                onClick={() => setSelectedRecord(rec)}
                className="p-4 bg-surfaceLight hover:bg-background border border-divider rounded-xl flex flex-col sm:flex-row items-center justify-between gap-4 cursor-pointer transition-all"
              >
                <div className="flex items-center gap-3 w-full sm:w-auto">
                  <div className="w-9 h-9 rounded-full bg-background flex items-center justify-center border border-divider overflow-hidden shrink-0">
                    <img src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${rec.name}`} alt={rec.name} className="w-full h-full" />
                  </div>
                  <div>
                    <h4 className="text-xs font-bold text-white">{rec.name}</h4>
                    <p className="text-[10px] text-textMuted">{rec.email}</p>
                  </div>
                </div>

                <div className="flex items-center justify-between sm:justify-end gap-6 w-full sm:w-auto">
                  <div className="text-left sm:text-right">
                    <p className="text-xs font-semibold text-textSecondary">{rec.channelName}</p>
                    <p className="text-[9px] text-textMuted">{(rec.subscribers/1000).toFixed(0)}k Subscribers</p>
                  </div>

                  <span className={`px-2 py-0.5 rounded-full border text-[9px] font-black tracking-wider ${getStatusColor(rec.status)}`}>
                    {rec.status}
                  </span>

                  <ChevronRight className="w-4 h-4 text-textMuted hidden sm:block" />
                </div>
              </div>
            ))}
            {filteredRecords.length === 0 && (
              <p className="text-xs text-textMuted text-center py-8">No CRM records matched the query filter criteria.</p>
            )}
          </div>
        </div>
      )}

      {/* DETAILED RECORD MODAL VIEW */}
      {selectedRecord && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="w-full max-w-md bg-surface border border-divider rounded-2xl p-6 space-y-6 relative text-left">
            <button 
              type="button" 
              onClick={() => setSelectedRecord(null)}
              className="absolute top-4 right-4 text-textMuted hover:text-white"
            >
              <X className="w-5 h-5" />
            </button>

            <div className="flex items-center gap-3 pb-4 border-b border-divider">
              <div className="w-12 h-12 rounded-full overflow-hidden border border-divider">
                <img src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${selectedRecord.name}`} alt={selectedRecord.name} className="w-full h-full" />
              </div>
              <div>
                <h3 className="text-md font-black text-white">{selectedRecord.name}</h3>
                <p className="text-xs text-textSecondary">{selectedRecord.email}</p>
              </div>
            </div>

            <div className="space-y-4 text-xs">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <span className="block text-[10px] uppercase font-bold text-textMuted">Channel Profile</span>
                  <span className="font-bold text-white">{selectedRecord.channelName}</span>
                </div>
                <div>
                  <span className="block text-[10px] uppercase font-bold text-textMuted">Platform Status</span>
                  <span className={`inline-block px-2 py-0.5 rounded-full border text-[9px] font-bold mt-1 ${getStatusColor(selectedRecord.status)}`}>
                    {selectedRecord.status}
                  </span>
                </div>
              </div>

              <div>
                <span className="block text-[10px] uppercase font-bold text-textMuted">Audit & Founder Notes</span>
                <p className="p-3 bg-surfaceLight border border-divider rounded-xl text-textSecondary text-[11px] leading-relaxed mt-1">
                  {selectedRecord.notes}
                </p>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <span className="block text-[10px] uppercase font-bold text-textMuted">Subscribers Reach</span>
                  <span className="font-bold text-white">{(selectedRecord.subscribers/1000).toFixed(0)}k</span>
                </div>
                <div>
                  <span className="block text-[10px] uppercase font-bold text-textMuted">Last Contact Log</span>
                  <span className="font-bold text-white">{selectedRecord.lastContactDate}</span>
                </div>
              </div>
            </div>

            <div className="pt-4 border-t border-divider flex gap-2">
              <a 
                href={`mailto:${selectedRecord.email}`}
                className="flex-1 bg-accentBlue text-background font-black py-2.5 rounded-xl flex items-center justify-center gap-2 hover:bg-accentBlue/90 transition text-xs"
              >
                <Mail className="w-4 h-4" /> DISPATCH SLA EMAIL
              </a>
              <button
                type="button"
                onClick={() => {
                  setRecords(prev => prev.filter(r => r.id !== selectedRecord.id));
                  setSelectedRecord(null);
                }}
                className="px-3 bg-accentRed/10 hover:bg-accentRed/20 border border-accentRed/30 text-accentRed rounded-xl transition text-xs font-bold"
              >
                EXPEL
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ADD CREATOR OVERLAY MODAL */}
      {showAddModal && (
        <div className="fixed inset-0 bg-black/85 backdrop-blur-md flex items-center justify-center p-4 z-50">
          <form onSubmit={handleAddRecord} className="w-full max-w-md bg-surface border border-divider rounded-2xl p-6 space-y-4 text-left relative animate-scaleUp">
            <button 
              type="button" 
              onClick={() => setShowAddModal(false)}
              className="absolute top-4 right-4 text-textMuted hover:text-white"
            >
              <X className="w-5 h-5" />
            </button>

            <h3 className="text-md font-black text-white uppercase tracking-tight flex items-center gap-2">
              <UserPlus className="w-5 h-5 text-neonEmerald" /> Onboard Creator Lead
            </h3>
            <p className="text-[10px] text-textSecondary uppercase tracking-widest font-semibold pb-2 border-b border-divider">
              Enter details for local SQLite database synchronization
            </p>

            <div className="space-y-3 text-xs">
              <div>
                <label className="block font-bold text-textSecondary mb-1">Full Name *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Jimmy Donaldson"
                  className="w-full bg-surfaceLight border border-divider rounded-xl py-2.5 px-3 text-white focus:outline-none focus:border-neonEmerald transition"
                  value={newName}
                  onChange={(e) => setNewName(e.target.value)}
                />
              </div>

              <div>
                <label className="block font-bold text-textSecondary mb-1">Email Address *</label>
                <input
                  type="email"
                  required
                  placeholder="e.g. creator@coop.com"
                  className="w-full bg-surfaceLight border border-divider rounded-xl py-2.5 px-3 text-white focus:outline-none focus:border-neonEmerald transition"
                  value={newEmail}
                  onChange={(e) => setNewEmail(e.target.value)}
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-bold text-textSecondary mb-1">Channel Name</label>
                  <input
                    type="text"
                    placeholder="e.g. Beast Tech"
                    className="w-full bg-surfaceLight border border-divider rounded-xl py-2.5 px-3 text-white focus:outline-none focus:border-neonEmerald transition"
                    value={newChannel}
                    onChange={(e) => setNewChannel(e.target.value)}
                  />
                </div>
                <div>
                  <label className="block font-bold text-textSecondary mb-1">Subscribers Count</label>
                  <input
                    type="number"
                    placeholder="10000"
                    className="w-full bg-surfaceLight border border-divider rounded-xl py-2.5 px-3 text-white focus:outline-none focus:border-neonEmerald transition"
                    value={newSubs}
                    onChange={(e) => setNewSubs(Number(e.target.value))}
                  />
                </div>
              </div>

              <div>
                <label className="block font-bold text-textSecondary mb-1">Initial Segment Status</label>
                <select
                  className="w-full bg-surfaceLight border border-divider rounded-xl py-2.5 px-3 text-white focus:outline-none focus:border-neonEmerald appearance-none transition"
                  value={newStatus}
                  onChange={(e) => setNewStatus(e.target.value as any)}
                >
                  <option value="ACTIVE">Active Cohort Member</option>
                  <option value="CHAMPION">Platform Champion</option>
                  <option value="NEEDS_FOLLOW_UP">Needing Follow-Up</option>
                  <option value="CHURN_RISK">Churn Warning</option>
                </select>
              </div>

              <div>
                <label className="block font-bold text-textSecondary mb-1">Founders Case Log Notes</label>
                <textarea
                  placeholder="Review findings, SLA agreements, or meeting feedback summaries..."
                  rows={3}
                  className="w-full bg-surfaceLight border border-divider rounded-xl py-2 px-3 text-white focus:outline-none focus:border-neonEmerald transition"
                  value={newNotes}
                  onChange={(e) => setNewNotes(e.target.value)}
                />
              </div>
            </div>

            <button
              type="submit"
              className="w-full bg-gradient-to-r from-neonEmerald to-neonEmerald/85 text-background font-black py-2.5 rounded-xl transition text-xs uppercase"
            >
              SAVE TO LEDGER
            </button>
          </form>
        </div>
      )}
    </div>
  );
};
