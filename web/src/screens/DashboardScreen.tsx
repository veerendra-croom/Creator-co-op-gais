import React, { useState } from 'react';
import { Users, FolderKanban, FileText, Activity, Star, AlertTriangle, Edit3, Eye } from 'lucide-react';

export const DashboardScreen: React.FC = () => {
  // Modals state
  const [showEditProfile, setShowEditProfile] = useState(false);
  const [showPublicView, setShowPublicView] = useState(false);
  const [selectedPortfolio, setSelectedPortfolio] = useState<any | null>(null);

  // Profile data
  const [displayName, setDisplayName] = useState('Botla Veerendra');
  const [bio, setNewBio] = useState('Founder & Lead SRE. Crafting decentralized video collaboration vaults.');
  const [portfolioLink, setPortfolioLink] = useState('https://github.com/veerendrabotla');

  // Portfolios list (PortfolioDetailScreen parity)
  const portfolios = [
    { id: 'p1', title: 'Cyberpunk VFX Soundscapes Reel', desc: 'Synthesized audio elements layered over geometric visualizers.', duration: '2:14 mins', filesCount: 3 },
    { id: 'p2', title: 'SaaS Agreement Ledger Walkthrough', desc: 'Step-by-step presentation demonstrating DocuSign and splits ledger schemas.', duration: '8:45 mins', filesCount: 2 }
  ];

  const handleUpdateProfile = (e: React.FormEvent) => {
    e.preventDefault();
    alert('User Profile settings successfully written through to Room SQL databases!');
    setShowEditProfile(false);
  };

  // Mock Data aligned with Android App SQLite entities
  const metrics = [
    { label: 'Registered Cohort', value: '18/25', change: 'Beta Slots Active', icon: Users, color: 'text-neonEmerald', bg: 'bg-neonEmerald/10' },
    { label: 'Active Workspaces', value: '12', change: '+3 this week', icon: FolderKanban, color: 'text-accentBlue', bg: 'bg-accentBlue/10' },
    { label: 'Mutual Agreements', value: '9', change: '100% Cryptographic', icon: FileText, color: 'text-crispAmber', bg: 'bg-crispAmber/10' },
    { label: 'Ledger Audit Actions', value: '1,482', change: 'Real-time delta', icon: Activity, color: 'text-textSecondary', bg: 'bg-surfaceLight' }
  ];

  const powerUsers = [
    { name: 'Botla Veerendra', score: 98, role: 'Platform Admin', avatar: 'BV' },
    { name: 'Macha Praveen', score: 95, role: 'Platform Co-Founder', avatar: 'MP' },
    { name: 'Srinivas Rao', score: 84, role: 'Verified Editor', avatar: 'SR' }
  ];

  const riskSignals = [
    { name: 'User_4829', reason: 'Abnormal API rate-limiting triggered', status: 'Vetted', color: 'text-crispAmber' },
    { name: 'Anonymous_User_3', reason: 'Copyright flag inside Commons', status: 'Suspended', color: 'text-accentRed' }
  ];

  const recentLogs = [
    { action: 'SECURE_HANDSHAKE', details: 'Database replication verified with local Room SQLite delta', time: '2 mins ago' },
    { action: 'SIGNATURE_ANCHORED', details: 'DocuSign handshake securely hashed with SHA-256', time: '14 mins ago' },
    { action: 'ROLE_CREATED', details: 'Syndicate "Senior Visual Effects Designer" role posted', time: '1 hour ago' },
    { action: 'HEALTH_CHECK_OK', details: 'Latency checked: DB (24ms) | API (56ms) | GC (0ms)', time: '2 hours ago' }
  ];

  return (
    <div className="space-y-6 text-left">
      {/* Welcome Banner / Profile Quick-Edit */}
      <div className="p-6 rounded-2xl glass-panel relative overflow-hidden flex flex-col md:flex-row md:items-center justify-between border border-divider gap-4">
        <div className="absolute top-0 right-0 w-64 h-64 bg-accentBlue/5 rounded-full blur-3xl pointer-events-none"></div>
        <div className="flex flex-col sm:flex-row items-start sm:items-center gap-4">
          <div className="w-16 h-16 rounded-full bg-gradient-to-tr from-accentBlue to-neonEmerald text-background flex items-center justify-center font-black text-xl shadow-lg border border-divider">
            BV
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-xl font-black text-white">{displayName}</h2>
              <span className="text-[8px] bg-accentBlue/10 text-accentBlue border border-accentBlue/20 px-1.5 py-0.5 rounded font-black tracking-wider uppercase">
                PLATFORM_ADMIN
              </span>
            </div>
            <p className="text-xs text-textSecondary mt-1 leading-relaxed max-w-lg">{bio}</p>
            <p className="text-[10px] text-textMuted font-mono mt-1 select-all">🔗 {portfolioLink}</p>
          </div>
        </div>

        <div className="flex gap-2 self-start md:self-auto shrink-0">
          <button
            type="button"
            onClick={() => setShowEditProfile(true)}
            className="px-3 py-1.5 bg-surfaceLight border border-divider hover:border-accentBlue/30 text-[10px] font-black text-white uppercase rounded-lg flex items-center gap-1.5 transition"
          >
            <Edit3 className="w-3.5 h-3.5 text-accentBlue" /> Edit Profile
          </button>
          <button
            type="button"
            onClick={() => setShowPublicView(true)}
            className="px-3 py-1.5 bg-surfaceLight border border-divider hover:border-accentBlue/30 text-[10px] font-black text-white uppercase rounded-lg flex items-center gap-1.5 transition"
          >
            <Eye className="w-3.5 h-3.5 text-neonEmerald" /> Public View
          </button>
        </div>
      </div>

      {/* Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {metrics.map((m, idx) => {
          const Icon = m.icon;
          return (
            <div key={idx} className="p-5 rounded-xl glass-panel border border-divider flex items-center gap-4 hover:border-accentBlue/30 transition-all duration-300">
              <div className={`p-3 rounded-lg ${m.bg}`}>
                <Icon className={`w-6 h-6 ${m.color}`} />
              </div>
              <div>
                <p className="text-xs text-textSecondary font-bold">{m.label}</p>
                <p className="text-xl font-black text-white mt-1">{m.value}</p>
                <p className="text-[10px] text-textMuted font-medium mt-1">{m.change}</p>
              </div>
            </div>
          );
        })}
      </div>

      {/* Grid Area: Signals & Insights */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Power Users Panel */}
        <div className="lg:col-span-4 p-5 rounded-xl glass-panel border border-divider space-y-4">
          <div className="flex items-center gap-2 pb-2 border-b border-divider">
            <Star className="w-4 h-4 text-neonEmerald" />
            <h3 className="text-xs font-bold uppercase tracking-wider text-white">Power Users</h3>
          </div>
          <div className="space-y-3">
            {powerUsers.map((pu, idx) => (
              <div key={idx} className="flex items-center justify-between p-3 rounded-lg bg-surfaceLight border border-divider hover:border-neonEmerald/30 transition">
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-full bg-neonEmerald/10 text-neonEmerald border border-neonEmerald/20 flex items-center justify-center font-bold text-xs">
                    {pu.avatar}
                  </div>
                  <div>
                    <p className="text-xs font-bold text-white">{pu.name}</p>
                    <p className="text-[10px] text-textSecondary">{pu.role}</p>
                  </div>
                </div>
                <div className="text-right">
                  <span className="text-xs font-bold text-crispAmber">⭐ {pu.score}</span>
                  <p className="text-[9px] text-textMuted uppercase font-bold">SCORE</p>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Risk Signals Panel */}
        <div className="lg:col-span-4 p-5 rounded-xl glass-panel border border-divider space-y-4">
          <div className="flex items-center gap-2 pb-2 border-b border-divider">
            <AlertTriangle className="w-4 h-4 text-accentRed" />
            <h3 className="text-xs font-bold uppercase tracking-wider text-white">Risk Signals</h3>
          </div>
          <div className="space-y-3">
            {riskSignals.map((rs, idx) => (
              <div key={idx} className="p-3 rounded-lg bg-surfaceLight border border-divider flex justify-between items-start hover:border-accentRed/30 transition">
                <div className="space-y-1 pr-2">
                  <p className="text-xs font-bold text-white">{rs.name}</p>
                  <p className="text-[10px] text-textSecondary leading-normal">{rs.reason}</p>
                </div>
                <span className={`text-[9px] font-black uppercase tracking-wider border rounded px-1.5 py-0.5 shrink-0 ${
                  rs.status === 'Suspended' ? 'border-accentRed/30 bg-accentRed/10 text-accentRed' : 'border-crispAmber/30 bg-crispAmber/10 text-crispAmber'
                }`}>
                  {rs.status}
                </span>
              </div>
            ))}
          </div>
        </div>

        {/* Recent Ledger Logs Panel */}
        <div className="lg:col-span-4 p-5 rounded-xl glass-panel border border-divider space-y-4">
          <div className="flex items-center gap-2 pb-2 border-b border-divider">
            <Activity className="w-4 h-4 text-accentBlue" />
            <h3 className="text-xs font-bold uppercase tracking-wider text-white">Recent Ledger Logs</h3>
          </div>
          <div className="space-y-3 max-h-[220px] overflow-y-auto pr-1">
            {recentLogs.map((log, idx) => (
              <div key={idx} className="p-3 rounded-lg bg-background border border-divider text-left text-xs space-y-1">
                <div className="flex justify-between items-center">
                  <span className="font-mono text-[9px] font-bold text-accentBlue bg-accentBlue/10 border border-accentBlue/20 px-1 py-0.5 rounded">
                    {log.action}
                  </span>
                  <span className="text-[9px] text-textMuted font-medium">{log.time}</span>
                </div>
                <p className="text-textSecondary text-[10px] leading-relaxed mt-1">
                  {log.details}
                </p>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Modal 1: Edit Profile Screen Dialog */}
      {showEditProfile && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-md flex items-center justify-center p-4 z-50">
          <form onSubmit={handleUpdateProfile} className="w-full max-w-md bg-surface border border-divider rounded-2xl p-6 space-y-5">
            <div className="flex justify-between items-center pb-2 border-b border-divider">
              <h3 className="text-sm font-black text-white uppercase tracking-wider">Configure Profile Settings</h3>
              <button type="button" onClick={() => setShowEditProfile(false)} className="text-xs font-bold text-textMuted hover:text-white">
                Close ×
              </button>
            </div>

            <div className="space-y-1.5">
              <label className="block text-[10px] font-bold text-textSecondary uppercase">Display Name</label>
              <input
                type="text"
                placeholder="Your name"
                required
                className="w-full bg-surfaceLight border border-divider rounded-lg px-3 py-2 text-white placeholder-textMuted text-xs focus:outline-none focus:border-accentBlue"
                value={displayName}
                onChange={(e) => setDisplayName(e.target.value)}
              />
            </div>

            <div className="space-y-1.5">
              <label className="block text-[10px] font-bold text-textSecondary uppercase">Creator Bio</label>
              <textarea
                rows={3}
                placeholder="Creator specialties, VFX specialties, audio highlights..."
                className="w-full bg-surfaceLight border border-divider rounded-lg px-3 py-2 text-white placeholder-textMuted text-xs focus:outline-none focus:border-accentBlue resize-none"
                value={bio}
                onChange={(e) => setNewBio(e.target.value)}
              />
            </div>

            <div className="space-y-1.5">
              <label className="block text-[10px] font-bold text-textSecondary uppercase">Portfolio URL</label>
              <input
                type="url"
                placeholder="https://..."
                className="w-full bg-surfaceLight border border-divider rounded-lg px-3 py-2 text-white placeholder-textMuted text-xs focus:outline-none focus:border-accentBlue"
                value={portfolioLink}
                onChange={(e) => setPortfolioLink(e.target.value)}
              />
            </div>

            <button
              type="submit"
              className="w-full py-2 bg-accentBlue text-background font-black text-xs rounded-xl hover:bg-accentBlue/90 transition"
            >
              ANCHOR PROFILE PRESETS
            </button>
          </form>
        </div>
      )}

      {/* Modal 2: Public Profile Screen Dialog */}
      {showPublicView && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-md flex items-center justify-center p-4 z-50">
          <div className="w-full max-w-md bg-surface border border-divider rounded-2xl p-6 space-y-6">
            <div className="flex justify-between items-center pb-2 border-b border-divider">
              <h3 className="text-xs font-black text-textMuted uppercase tracking-wider">Public Profile View</h3>
              <button type="button" onClick={() => setShowPublicView(false)} className="text-xs font-bold text-textMuted hover:text-white">
                Close ×
              </button>
            </div>

            <div className="space-y-4">
              <div className="flex items-center gap-3 pb-3 border-b border-divider">
                <div className="w-10 h-10 rounded-full bg-accentBlue/10 text-accentBlue border border-accentBlue/20 flex items-center justify-center font-black text-sm">
                  BV
                </div>
                <div>
                  <h4 className="text-sm font-black text-white">{displayName}</h4>
                  <p className="text-[9px] text-accentBlue uppercase font-bold tracking-wider mt-0.5">⭐ 98 Reputation Score • Vetted</p>
                </div>
              </div>

              <div className="space-y-1 text-left">
                <h5 className="text-[10px] font-bold text-textSecondary uppercase">Specialty Scope</h5>
                <p className="text-xs text-white leading-relaxed">{bio}</p>
              </div>

              {/* Portfolios list */}
              <div className="space-y-2.5 text-left">
                <h5 className="text-[10px] font-bold text-textSecondary uppercase">Showcased Portfolios</h5>
                {portfolios.map((port) => (
                  <div
                    key={port.id}
                    onClick={() => {
                      setSelectedPortfolio(port);
                      setShowPublicView(false);
                    }}
                    className="p-3 bg-surfaceLight border border-divider rounded-lg hover:border-accentBlue/40 cursor-pointer transition flex items-center justify-between"
                  >
                    <div>
                      <h6 className="text-xs font-bold text-white leading-snug">{port.title}</h6>
                      <p className="text-[9px] text-textMuted mt-0.5">{port.duration} • {port.filesCount} deliverables</p>
                    </div>
                    <span className="text-[10px] text-accentBlue font-bold shrink-0">View Details →</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal 3: Portfolio Details Dialog */}
      {selectedPortfolio && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-md flex items-center justify-center p-4 z-50">
          <div className="w-full max-w-sm bg-surface border border-divider rounded-2xl p-6 space-y-4">
            <div className="flex justify-between items-center pb-2 border-b border-divider">
              <h3 className="text-xs font-black text-white uppercase tracking-wider">Portfolio Details</h3>
              <button
                type="button"
                onClick={() => {
                  setSelectedPortfolio(null);
                  setShowPublicView(true);
                }}
                className="text-xs font-bold text-textMuted hover:text-white"
              >
                Back to Profile
              </button>
            </div>

            <div className="space-y-3">
              <div>
                <span className="text-[9px] bg-accentBlue/10 text-accentBlue border border-accentBlue/20 px-1.5 py-0.5 rounded font-black uppercase tracking-wider">
                  VFX DELIVERABLE
                </span>
                <h4 className="text-sm font-black text-white mt-1.5 leading-snug">{selectedPortfolio.title}</h4>
                <p className="text-xs text-textSecondary mt-1 leading-relaxed">{selectedPortfolio.desc}</p>
              </div>

              <div className="p-3 bg-surfaceLight border border-divider rounded-lg flex items-center justify-between text-xs font-bold">
                <span className="text-textSecondary">CLIP RUNTIME:</span>
                <span className="text-white">{selectedPortfolio.duration}</span>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
