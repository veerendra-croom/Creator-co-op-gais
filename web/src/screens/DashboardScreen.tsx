import React from 'react';
import { Users, FolderKanban, FileText, Activity, Star, AlertTriangle, Cloud } from 'lucide-react';

export const DashboardScreen: React.FC = () => {
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
    <div className="space-y-6">
      {/* Welcome Banner */}
      <div className="p-6 rounded-2xl glass-panel relative overflow-hidden flex items-center justify-between border border-divider">
        <div className="absolute top-0 right-0 w-64 h-64 bg-accentBlue/5 rounded-full blur-3xl pointer-events-none"></div>
        <div>
          <span className="text-xs font-bold text-accentBlue uppercase tracking-wider">Operational Dashboard</span>
          <h2 className="text-2xl font-black text-white mt-1">CREATOR CO-OP COCKPIT</h2>
          <p className="text-sm text-textSecondary mt-2">
            Welcomed as Executive Platform Administrator. All system states, local Room DB deltas, and integrations are green.
          </p>
        </div>
        <div className="hidden md:flex items-center gap-3 bg-surfaceLight border border-divider rounded-xl px-4 py-3">
          <Cloud className="w-5 h-5 text-neonEmerald animate-pulse" />
          <div className="text-left">
            <p className="text-xs text-textMuted font-bold">REPLICATION STATE</p>
            <p className="text-sm font-bold text-white">Supabase Live Sync</p>
          </div>
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
    </div>
  );
};
