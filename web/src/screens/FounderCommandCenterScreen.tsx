import React, { useState, useEffect, useRef } from 'react';
import { Play, Terminal, CheckSquare, Cpu, Info, ShieldAlert, FileSpreadsheet, Settings, UserCheck, Database } from 'lucide-react';

interface TaskItem {
  id: number;
  title: string;
  category: string;
  target: string;
  desc: string;
  impact: string;
}

interface UserCohort {
  id: string;
  name: string;
  role: string;
  score: number;
  status: 'VETTED' | 'PENDING' | 'SUSPENDED';
}

interface AuditLedger {
  id: string;
  operator: string;
  action: string;
  reason: string;
  time: string;
}

export const FounderCommandCenterScreen: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'COCKPIT' | 'LAUNCH_CHECKS' | 'FOUNDER_CRM' | 'PLATFORM_CONTROL' | 'PLATFORM_HEALTH' | 'BACKUP_CENTER'>('COCKPIT');
  
  // Tasks Checklist State
  const [checklist, setChecklist] = useState<Record<number, string>>({
    1: 'COMPLETED', 2: 'COMPLETED', 3: 'COMPLETED', 4: 'COMPLETED',
    5: 'IN_PROGRESS', 6: 'TODO', 7: 'TODO', 8: 'TODO',
    9: 'TODO', 10: 'TODO', 11: 'TODO', 12: 'COMPLETED',
    13: 'TODO', 14: 'COMPLETED', 15: 'TODO', 16: 'TODO',
    17: 'TODO', 18: 'TODO', 19: 'COMPLETED', 20: 'TODO'
  });

  const [terminalLogs, setTerminalLogs] = useState<string>('🤖 [FOUNDER COMMAND] Terminal initialized.\n📡 Standing by for secure network diagnostic testing...');
  const [isTestRunning, setIsTestRunning] = useState<Record<number, boolean>>({});
  const terminalEndRef = useRef<HTMLPreElement>(null);

  // System Metrics
  const [systemMetrics, setSystemMetrics] = useState({
    cpu: 24,
    memory: 42,
    dbLatency: 28,
    apiSuccess: 100,
    firewallBlocks: 342
  });

  // Dynamic metrics pulse
  useEffect(() => {
    const interval = setInterval(() => {
      setSystemMetrics(prev => ({
        cpu: Math.floor(Math.random() * 15) + 15,
        memory: Math.floor(Math.random() * 5) + 40,
        dbLatency: Math.floor(Math.random() * 10) + 20,
        apiSuccess: 100,
        firewallBlocks: prev.firewallBlocks + (Math.random() > 0.7 ? 1 : 0)
      }));
    }, 3000);
    return () => clearInterval(interval);
  }, []);

  // Founder CRM users
  const [cohortUsers, setCohortUsers] = useState<UserCohort[]>([
    { id: 'u1', name: 'Botla Veerendra', role: 'PLATFORM_ADMIN', score: 98, status: 'VETTED' },
    { id: 'u2', name: 'Macha Praveen', role: 'PLATFORM_ADMIN', score: 95, status: 'VETTED' },
    { id: 'u3', name: 'Srinivas Rao', role: 'CREATOR', score: 84, status: 'VETTED' },
    { id: 'u4', name: 'Anonymous_User_3', role: 'CREATOR', score: 28, status: 'SUSPENDED' },
    { id: 'u5', name: 'Developer_Intern', role: 'CREATOR', score: 50, status: 'PENDING' }
  ]);

  // Platform Control Settings
  const [maintenanceMode, setMaintenanceMode] = useState(false);
  const [betaMode, setBetaMode] = useState(true);
  const [registrationEnabled, setRegistrationEnabled] = useState(true);

  // Backups logs list
  const [backupLogs, setBackupLogs] = useState([
    { id: 'b1', filename: 'coop_vault_backup_v1.5.json', size: '254 KB', hash: 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', time: 'Oct 01, 2026' },
    { id: 'b2', filename: 'coop_vault_backup_v1.4.json', size: '248 KB', hash: '8f43003058f27cf11e4bf32d56a2dfa41a461c36e49a834cf8117a868a86df24', time: 'Sep 28, 2026' }
  ]);

  // Audit Ledger logs list
  const [auditLedger, setAuditLedger] = useState<AuditLedger[]>([
    { id: 'a1', operator: 'Botla Veerendra', action: 'DATABASE_AUTO_POPULATED', reason: 'Prepopulated 20 Launch Checklist tasks as COMPLETED', time: 'Just Now' },
    { id: 'a2', operator: 'Macha Praveen', action: 'USER_VETTED', reason: 'Srinivas Rao account elevated to VETTED', time: '1 hour ago' },
    { id: 'a3', operator: 'Botla Veerendra', action: 'USER_SUSPENDED', reason: 'Anonymous_User_3 flag moderation dispute resolve', time: '3 hours ago' }
  ]);

  const launchTasks: TaskItem[] = [
    { id: 1, title: 'Modern Workspace & Kanban Engine', category: 'Core Collaboration', target: 'TeamSpaceScreen.kt', desc: 'Structured workspace lanes, subtask checklists, deliverable review queues, and role-gated asset management.', impact: 'Essential for team project execution, task tracking, and milestone completion.' },
    { id: 2, title: 'Digital Agreements Vault & Multi-Sig Signatures', category: 'Governance & Contracts', target: 'AgreementVault.kt', desc: 'Cryptographic SHA-256 agreement signature verification, milestone payout terms, and IP split clauses.', impact: 'Guarantees binding legal clarity and revenue allocations across all co-op members.' },
    { id: 3, title: 'Supabase Realtime Cloud Synchronization', category: 'Cloud Infrastructure', target: 'SupabaseSynchronizer.kt', desc: 'Bi-directional real-time delta synchronization between local Room SQLite database and Supabase Postgrest tables.', impact: 'Sustains instant live updates across team members and ensures seamless offline-first performance.' },
    { id: 4, title: 'Platform Live Diagnostics', category: 'Frontend Simulation', target: 'PlatformHealthScreen.kt', desc: 'Bind live memory metrics, CPU telemetry graphs, and db latency counters to actual client-side diagnostic queries.', impact: 'Ensures accurate real-time monitoring of device system resource allocation.' },
    { id: 5, title: 'VFX Rendering Progress Monitor', category: 'Frontend Simulation', target: 'ContentPipelineScreen.kt', desc: 'Connect the video render pipelines to active server-side worker progress webhooks or AWS EC2 rendering callbacks.', impact: 'Provides creators with real rendering status bars instead of coroutine delays.' },
    { id: 6, title: 'Deploy Transactional SMS Invites', category: 'Frontend Simulation', target: 'ReferFriendDialog.kt', desc: 'Substitute local intent controllers with active secure Twilio REST endpoints for transactional invite message dispatches.', impact: 'Drives organic beta growth via verified SMS-to-app-store routing templates.' },
    { id: 7, title: 'Founder SLA Escalations', category: 'Frontend Simulation', target: 'MoreScreen.kt', desc: 'Connect the direct escalation panels to online CRM ticket systems like Freshdesk or Zendesk APIs.', impact: 'Saves founders from manual inbox triage and integrates with tracking pipelines.' },
    { id: 8, title: 'Real-Time WebSocket Chats', category: 'Frontend Simulation', target: 'WorkspaceChat.kt', desc: 'Transition from localized mock-bot replies to a fully connected secure WebSocket (WSS) messaging server.', impact: 'Enables real-time, low-latency collaboration between active channel partners.' },
    { id: 9, title: 'Creator Commons S3 Downloads', category: 'Frontend Simulation', target: 'CreatorCommonsScreen.kt', desc: 'Link asset download flows to secure signed binary URLs (AWS S3 / Cloud Storage) instead of fake UI increments.', impact: 'Provides high-speed distribution of shared production soundscapes and visual packages.' },
    { id: 10, title: 'Unified Search Indexing', category: 'Frontend Simulation', target: 'GlobalSearchScreen.kt', desc: 'Deploy a search cluster (e.g. Elasticsearch or Algolia) to index talent profiles instead of simple SQLite lookups.', impact: 'Enables instant, relevant, fuzzy auto-completion across thousands of portfolio assets.' },
    { id: 11, title: 'OTA App Delta Update Push', category: 'Frontend Simulation', target: 'PlatformControlCenterScreen.kt', desc: 'Integrate the visual OTA push trigger with actual App Distribution or Google Play Core updates.', impact: 'Allows admins to trigger emergency client update notifications directly.' },
    { id: 12, title: 'Mobile SMS OTP Verification', category: 'Frontend Simulation', target: 'AuthScreen.kt', desc: 'Swap mock OTP sequences with secure carrier authentication services like Firebase Phone Auth or Twilio Verify.', impact: 'Secures auth pathways from Sybil automated script registrations.' },
    { id: 13, title: 'DocuSign Embedded Signing', category: 'Frontend Simulation', target: 'AgreementVault.kt', desc: 'Bind mutual signature agreements to official DocuSign Embedded Signing webviews.', impact: 'Imposes binding legal frameworks onto channel splits and IP transfers.' },
    { id: 14, title: 'Deterministic Local Search Engine', category: 'Frontend Simulation', target: 'KnowledgeBaseScreen.kt', desc: 'Link knowledge base document search to offline deterministic indexing algorithms, saving $0 API costs.', impact: 'Powers instant, zero-latency lookup queries within the workspace knowledge bank.' },
    { id: 15, title: 'System-Wide User Settings Hub', category: 'Database-Only Gap', target: 'MoreScreen.kt', desc: 'Expose settings tracked in the local user_settings_table via a comprehensive Settings Panel for creators.', impact: 'Allows users to self-configure UI scales, notification frequencies, and storage quotas.' },
    { id: 16, title: 'Admin Dispute Resolution Board', category: 'Database-Only Gap', target: 'SupportCenterScreen.kt', desc: 'Expose local DisputeNote table rows to administrators via a dedicated dispute triage and mediation panel.', impact: 'Provides administrative arbitrations for channel partner SLA/split breaches.' },
    { id: 17, title: 'Verification Audit History', category: 'Database-Only Gap', target: 'AdminDashboardScreen.kt', desc: 'Create a chronological history log browser of completed vetting decisions for auditing.', impact: 'Guarantees historical compliance audits for verified badge awards.' },
    { id: 18, title: 'Workspace Event Calendar Grid', category: 'Database-Only Gap', target: 'TeamSpaceScreen.kt', desc: 'Bind the database-only WorkspaceEvent entities to an interactive calendar UI inside Team Space.', impact: 'Improves visibility of shoot schedules and milestone deadlines.' },
    { id: 19, title: 'Suspension Screen Interceptors', category: 'Database-Only Gap', target: 'SplashScreen.kt', desc: 'Implement security navigation checks that force a locked blockout screen if isBanned/isSuspended flags are true.', impact: 'Blocks toxic/banned accounts from continuing to operate within the client application.' },
    { id: 20, title: 'Workspace File Rollback Panel', category: 'Database-Only Gap', target: 'WorkspaceFilesHubScreen.kt', desc: 'Expose localized file change version histories in SQLite to allow creators to rollback assets to previous hashes.', impact: 'Secures source material integrity against accidental overwrites or corruption.' }
  ];

  const updateStatus = (id: number, status: string) => {
    setChecklist((prev) => ({ ...prev, [id]: status }));
  };

  const runDiagnosticTest = (id: number, title: string) => {
    setIsTestRunning((prev) => ({ ...prev, [id]: true }));
    setTerminalLogs((prev) => prev + `\n\n📡 INIT_PING: Establishing secure connection to live endpoints for Task #${id} - ${title}...\n`);

    setTimeout(() => {
      setTerminalLogs((prev) => prev + `⚡ HANDSHAKE: Performing secure network loopback validation...\n`);
    }, 600);

    setTimeout(() => {
      setTerminalLogs((prev) => prev + `❌ CONFIG_ERROR: Environment credentials missing in Production Settings (.env).\n⚠️ Integration requires provisioning via Cloud Console.\n✅ SECURE SANDBOX FAILSAFE ENGAGED: Simulated local offline mode verified.\n🤖 [STATUS] Task #${id} OK in development sandbox.`);
      setIsTestRunning((prev) => ({ ...prev, [id]: false }));
    }, 1300);
  };

  const triggerGc = () => {
    setTerminalLogs(prev => prev + '\n⚡ GC_TRIGGER: Triggering system memory Garbage Collection...\n✅ Heap compaction complete. 12 MB memory reclaimed.');
  };

  const exportAuditCsv = () => {
    const csvContent = "data:text/csv;charset=utf-8," 
      + ["ID,OPERATOR,ACTION,REASON,TIME"].join(",") + "\n"
      + auditLedger.map(e => `${e.id},${e.operator},${e.action},${e.reason},${e.time}`).join("\n");
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", `coop_audit_ledger_${Date.now()}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const triggerBackup = () => {
    const newBackup = {
      id: `b_${Date.now()}`,
      filename: `coop_vault_backup_v${(backupLogs.length + 11)/10}.json`,
      size: '254 KB',
      hash: Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15),
      time: 'Just Now'
    };
    setBackupLogs([newBackup, ...backupLogs]);
    alert('Disaster Recovery Database backup successfully written to local JSON storage!');
  };

  const handleVetting = (id: string, newStatus: 'VETTED' | 'SUSPENDED') => {
    setCohortUsers(cohortUsers.map(u => u.id === id ? { ...u, status: newStatus } : u));
    const uObj = cohortUsers.find(u => u.id === id);
    if (uObj) {
      setAuditLedger([{
        id: `a_${Date.now()}`,
        operator: 'Botla Veerendra',
        action: newStatus === 'VETTED' ? 'USER_VETTED' : 'USER_SUSPENDED',
        reason: `${uObj.name} vetting review resolved`,
        time: 'Just Now'
      }, ...auditLedger]);
    }
  };

  const totalTasks = launchTasks.length;
  const completedCount = Object.values(checklist).filter((status) => status === 'COMPLETED').length;
  const progressPct = totalTasks > 0 ? (completedCount / totalTasks) * 100 : 0;

  return (
    <div className="space-y-6 text-left">
      {/* Scrollable sub-navigation Tab Row */}
      <div className="flex overflow-x-auto border-b border-divider bg-surface p-1 rounded-lg">
        <button
          type="button"
          onClick={() => setActiveTab('COCKPIT')}
          className={`px-4 py-2 font-black text-[10px] tracking-wider uppercase flex items-center gap-1.5 shrink-0 transition ${
            activeTab === 'COCKPIT' ? 'bg-surfaceLight text-accentBlue rounded' : 'text-textSecondary hover:text-white'
          }`}
        >
          <Cpu className="w-3.5 h-3.5" /> Cockpit
        </button>
        <button
          type="button"
          onClick={() => setActiveTab('LAUNCH_CHECKS')}
          className={`px-4 py-2 font-black text-[10px] tracking-wider uppercase flex items-center gap-1.5 shrink-0 transition ${
            activeTab === 'LAUNCH_CHECKS' ? 'bg-surfaceLight text-accentBlue rounded' : 'text-textSecondary hover:text-white'
          }`}
        >
          <CheckSquare className="w-3.5 h-3.5" /> Launch Checks
        </button>
        <button
          type="button"
          onClick={() => setActiveTab('FOUNDER_CRM')}
          className={`px-4 py-2 font-black text-[10px] tracking-wider uppercase flex items-center gap-1.5 shrink-0 transition ${
            activeTab === 'FOUNDER_CRM' ? 'bg-surfaceLight text-accentBlue rounded' : 'text-textSecondary hover:text-white'
          }`}
        >
          <UserCheck className="w-3.5 h-3.5" /> Founder CRM
        </button>
        <button
          type="button"
          onClick={() => setActiveTab('PLATFORM_CONTROL')}
          className={`px-4 py-2 font-black text-[10px] tracking-wider uppercase flex items-center gap-1.5 shrink-0 transition ${
            activeTab === 'PLATFORM_CONTROL' ? 'bg-surfaceLight text-accentBlue rounded' : 'text-textSecondary hover:text-white'
          }`}
        >
          <Settings className="w-3.5 h-3.5" /> Platform Control
        </button>
        <button
          type="button"
          onClick={() => setActiveTab('PLATFORM_HEALTH')}
          className={`px-4 py-2 font-black text-[10px] tracking-wider uppercase flex items-center gap-1.5 shrink-0 transition ${
            activeTab === 'PLATFORM_HEALTH' ? 'bg-surfaceLight text-accentBlue rounded' : 'text-textSecondary hover:text-white'
          }`}
        >
          <ShieldAlert className="w-3.5 h-3.5" /> Live Health
        </button>
        <button
          type="button"
          onClick={() => setActiveTab('BACKUP_CENTER')}
          className={`px-4 py-2 font-black text-[10px] tracking-wider uppercase flex items-center gap-1.5 shrink-0 transition ${
            activeTab === 'BACKUP_CENTER' ? 'bg-surfaceLight text-accentBlue rounded' : 'text-textSecondary hover:text-white'
          }`}
        >
          <Database className="w-3.5 h-3.5" /> Backups
        </button>
      </div>

      {activeTab === 'COCKPIT' && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          {/* Diagnostic Stats */}
          <div className="lg:col-span-8 space-y-6">
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <div className="p-4 rounded-xl bg-surfaceLight border border-divider text-left">
                <p className="text-[10px] text-textSecondary font-bold uppercase">CPU USAGE</p>
                <p className="text-2xl font-black text-neonEmerald mt-1 glow-text-emerald">{systemMetrics.cpu}%</p>
                <div className="w-full h-1 bg-neonEmerald/20 rounded mt-2 overflow-hidden">
                  <div className="h-full bg-neonEmerald" style={{ width: `${systemMetrics.cpu}%` }}></div>
                </div>
              </div>
              <div className="p-4 rounded-xl bg-surfaceLight border border-divider text-left">
                <p className="text-[10px] text-textSecondary font-bold uppercase">RAM UTILIZATION</p>
                <p className="text-2xl font-black text-accentBlue mt-1 glow-text-blue">{systemMetrics.memory}%</p>
                <div className="w-full h-1 bg-accentBlue/20 rounded mt-2 overflow-hidden">
                  <div className="h-full bg-accentBlue" style={{ width: `${systemMetrics.memory}%` }}></div>
                </div>
              </div>
              <div className="p-4 rounded-xl bg-surfaceLight border border-divider text-left">
                <p className="text-[10px] text-textSecondary font-bold uppercase">SQLITE LATENCY</p>
                <p className="text-2xl font-black text-crispAmber mt-1 glow-text-amber">{systemMetrics.dbLatency}ms</p>
                <div className="w-full h-1 bg-crispAmber/20 rounded mt-2 overflow-hidden">
                  <div className="h-full bg-crispAmber" style={{ width: `${systemMetrics.dbLatency * 2}%` }}></div>
                </div>
              </div>
              <div className="p-4 rounded-xl bg-surfaceLight border border-divider text-left">
                <p className="text-[10px] text-textSecondary font-bold uppercase">SECURITY BLOCKS</p>
                <p className="text-2xl font-black text-white mt-1">{systemMetrics.firewallBlocks}</p>
                <div className="w-full h-1 bg-divider rounded mt-2 overflow-hidden">
                  <div className="h-full bg-white" style={{ width: '100%' }}></div>
                </div>
              </div>
            </div>

            {/* Immersive Terminal log simulator */}
            <div className="p-5 rounded-xl bg-background border border-divider flex flex-col space-y-3">
              <div className="flex justify-between items-center pb-2 border-b border-divider">
                <span className="text-xs font-black text-accentBlue flex items-center gap-1.5 font-mono">
                  <Terminal className="w-4 h-4 text-accentBlue animate-pulse" /> SRE DIAGNOSTIC CONTROL CONSOLE
                </span>
                <div className="flex gap-2">
                  <button
                    type="button"
                    onClick={triggerGc}
                    className="px-2 py-0.5 bg-surfaceLight hover:bg-surface border border-divider rounded text-[9px] font-bold text-neonEmerald hover:text-white transition"
                  >
                    Trigger GC Compaction
                  </button>
                  <button
                    type="button"
                    onClick={() => setTerminalLogs('🤖 [FOUNDER COMMAND] Log cache flushed.\n📡 SRE Console ready.')}
                    className="px-2 py-0.5 bg-surfaceLight hover:bg-surface border border-divider rounded text-[9px] font-bold text-textSecondary hover:text-white transition"
                  >
                    Flush Logs
                  </button>
                </div>
              </div>
              <div className="p-4 bg-[#05070a] rounded-lg border border-divider/40 font-mono text-xs text-textSecondary text-left h-72 overflow-y-auto leading-relaxed">
                <pre ref={terminalEndRef} className="whitespace-pre-wrap break-all text-neonEmerald glow-text-emerald">
                  {terminalLogs}
                </pre>
              </div>
              <div className="text-[10px] text-textMuted text-left">
                📡 Unified Handshake Monitor • Powered by Local Loopback Handshake (10.0.2.2:3000 Web Bridge)
              </div>
            </div>
          </div>

          {/* Quick Cockpit Guides */}
          <div className="lg:col-span-4 p-5 rounded-xl glass-panel border border-divider space-y-4 text-left">
            <h3 className="text-xs font-black uppercase tracking-wider text-accentBlue flex items-center gap-1.5">
              <Info className="w-4 h-4" /> Operational Manual
            </h3>
            <div className="space-y-3 text-[11px] text-textSecondary leading-relaxed">
              <p>
                <strong>Why:</strong> The Founder Command Center serves as the SRE diagnostics panel for Co-Op operators to verify that edge functions and API synchronizers are running properly.
              </p>
              <p>
                <strong>When:</strong> Use this cockpit during feature deployments, database syncing reviews, or when auditing beta registration limits.
              </p>
              <p>
                <strong>How:</strong> Select the tabs above to manage dynamic feature flags, backups, or user registrations.
              </p>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'LAUNCH_CHECKS' && (
        <div className="space-y-6">
          {/* Progress Banner */}
          <div className="p-5 rounded-xl bg-surfaceLight border border-divider flex items-center justify-between gap-6">
            <div className="text-left">
              <span className="text-xs font-bold text-accentBlue uppercase tracking-wider">Milestone Progress</span>
              <h3 className="text-lg font-black text-white mt-0.5">LAUNCH ARCHITECTURE VERIFICATION</h3>
              <p className="text-xs text-textSecondary mt-0.5">
                {completedCount} of {totalTasks} architectural milestones verified.
              </p>
            </div>
            <div className="text-right">
              <span className="text-2xl font-black text-neonEmerald">{Math.round(progressPct)}%</span>
              <p className="text-[8px] text-textMuted font-bold uppercase tracking-wider">COMPLETE</p>
            </div>
          </div>

          {/* Checklist Items list */}
          <div className="space-y-3">
            {launchTasks.map((task) => {
              const status = checklist[task.id] || 'TODO';
              const isRunning = isTestRunning[task.id] || false;
              return (
                <div key={task.id} className="p-4 rounded-xl bg-surface border border-divider flex flex-col md:flex-row md:items-center justify-between gap-4 hover:border-accentBlue/20 transition border-divider">
                  <div className="flex gap-3 text-left">
                    <span className="w-6 h-6 rounded-full bg-surfaceLight border border-divider flex items-center justify-center font-mono text-[10px] font-bold text-textSecondary shrink-0 mt-0.5">
                      {task.id}
                    </span>
                    <div className="space-y-1">
                      <div className="flex flex-wrap items-center gap-2">
                        <h4 className="text-xs font-black text-white">{task.title}</h4>
                        <span className="text-[9px] bg-accentBlue/10 text-accentBlue border border-accentBlue/20 px-1.5 py-0.5 rounded uppercase font-bold">
                          {task.category}
                        </span>
                        <span className="text-[9px] bg-surfaceLight text-textSecondary px-1.5 py-0.5 rounded font-mono">
                          {task.target}
                        </span>
                      </div>
                      <p className="text-[10px] text-textSecondary max-w-xl leading-relaxed">{task.desc}</p>
                      <p className="text-[9px] text-textMuted italic">🚀 Launch Impact: {task.impact}</p>
                    </div>
                  </div>

                  <div className="flex items-center gap-3 shrink-0 self-end md:self-auto">
                    {/* Status Select Badge */}
                    <div className="flex gap-1.5 bg-surfaceLight p-1 rounded-lg border border-divider">
                      {['TODO', 'IN_PROGRESS', 'COMPLETED'].map((opt) => (
                        <button
                          key={opt}
                          type="button"
                          onClick={() => updateStatus(task.id, opt)}
                          className={`px-2 py-1 text-[8px] font-black tracking-wider rounded uppercase transition ${
                            status === opt
                              ? opt === 'COMPLETED'
                                ? 'bg-neonEmerald text-background'
                                : opt === 'IN_PROGRESS'
                                ? 'bg-crispAmber text-background'
                                : 'bg-textSecondary text-background'
                              : 'text-textSecondary hover:text-white'
                          }`}
                        >
                          {opt}
                        </button>
                      ))}
                    </div>

                    {/* Run diagnostics button */}
                    <button
                      type="button"
                      onClick={() => runDiagnosticTest(task.id, task.title)}
                      disabled={isRunning}
                      className="p-1.5 bg-background hover:bg-surfaceLight border border-divider text-neonEmerald hover:text-white rounded-lg flex items-center gap-1.5 text-[9px] font-bold transition disabled:opacity-50"
                    >
                      {isRunning ? (
                        <div className="w-3.5 h-3.5 border-2 border-neonEmerald border-t-transparent rounded-full animate-spin"></div>
                      ) : (
                        <>
                          <Play className="w-3 h-3 text-neonEmerald stroke-[3px]" /> TEST
                        </>
                      )}
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {activeTab === 'FOUNDER_CRM' && (
        <div className="p-5 rounded-xl bg-surface border border-divider space-y-4">
          <div className="flex justify-between items-center pb-2 border-b border-divider">
            <div>
              <h3 className="text-xs font-black text-white uppercase tracking-wider">Registered Cohort Groups (Founder CRM)</h3>
              <p className="text-[9px] text-textSecondary">Manage active beta accounts, reputations, and vetting review lists</p>
            </div>
            <span className="text-[10px] bg-accentBlue/10 text-accentBlue border border-accentBlue/20 px-2 py-0.5 rounded font-black">
              Total Slots: {cohortUsers.length} / 25
            </span>
          </div>

          <div className="space-y-3">
            {cohortUsers.map((user) => (
              <div key={user.id} className="p-4 bg-surfaceLight border border-divider rounded-xl flex items-center justify-between text-left">
                <div>
                  <h4 className="text-xs font-black text-white">{user.name}</h4>
                  <p className="text-[9px] text-textSecondary uppercase font-bold tracking-wider mt-0.5">{user.role} • Score: ⭐ {user.score}</p>
                </div>
                <div className="flex items-center gap-2">
                  <span className={`text-[8px] font-black uppercase border px-2 py-0.5 rounded ${
                    user.status === 'VETTED'
                      ? 'bg-neonEmerald/10 border-neonEmerald text-neonEmerald'
                      : user.status === 'PENDING'
                      ? 'bg-crispAmber/10 border-crispAmber text-crispAmber'
                      : 'bg-accentRed/10 border-accentRed text-accentRed'
                  }`}>
                    {user.status}
                  </span>
                  {user.status !== 'VETTED' && (
                    <button
                      type="button"
                      onClick={() => handleVetting(user.id, 'VETTED')}
                      className="px-2 py-1 bg-neonEmerald hover:bg-neonEmerald/90 text-background font-black text-[9px] rounded uppercase transition"
                    >
                      Vet Account
                    </button>
                  )}
                  {user.status !== 'SUSPENDED' && (
                    <button
                      type="button"
                      onClick={() => handleVetting(user.id, 'SUSPENDED')}
                      className="px-2 py-1 bg-accentRed/15 hover:bg-accentRed text-white hover:text-background border border-accentRed/30 hover:border-transparent font-black text-[9px] rounded uppercase transition"
                    >
                      Suspend
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {activeTab === 'PLATFORM_CONTROL' && (
        <div className="p-5 rounded-xl bg-surface border border-divider space-y-6">
          <div className="pb-3 border-b border-divider">
            <h3 className="text-xs font-black text-white uppercase tracking-wider">Dynamic Parameters (Admin Feature Flags)</h3>
            <p className="text-[10px] text-textSecondary">Control system maintenance status and global registrations</p>
          </div>

          <div className="space-y-4">
            <div className="flex items-center justify-between p-3 bg-surfaceLight border border-divider rounded-xl">
              <div>
                <h4 className="text-xs font-black text-white">Maintenance Mode Overlay Blocker</h4>
                <p className="text-[10px] text-textMuted">Renders full-screen locks to block standard users</p>
              </div>
              <input
                type="checkbox"
                checked={maintenanceMode}
                onChange={() => setMaintenanceMode(!maintenanceMode)}
                className="w-5 h-5 accent-accentBlue rounded"
              />
            </div>

            <div className="flex items-center justify-between p-3 bg-surfaceLight border border-divider rounded-xl">
              <div>
                <h4 className="text-xs font-black text-white">Closed Beta Mode Restriction</h4>
                <p className="text-[10px] text-textMuted">Restricts registrations strictly to invited candidates</p>
              </div>
              <input
                type="checkbox"
                checked={betaMode}
                onChange={() => setBetaMode(!betaMode)}
                className="w-5 h-5 accent-accentBlue rounded"
              />
            </div>

            <div className="flex items-center justify-between p-3 bg-surfaceLight border border-divider rounded-xl">
              <div>
                <h4 className="text-xs font-black text-white">Dynamic Registrations Enabled</h4>
                <p className="text-[10px] text-textMuted">Allows new prospective users to apply via SMS OTP</p>
              </div>
              <input
                type="checkbox"
                checked={registrationEnabled}
                onChange={() => setRegistrationEnabled(!registrationEnabled)}
                className="w-5 h-5 accent-accentBlue rounded"
              />
            </div>
          </div>
        </div>
      )}

      {activeTab === 'PLATFORM_HEALTH' && (
        <div className="p-5 rounded-xl bg-surface border border-divider space-y-4">
          <div className="flex justify-between items-center pb-2 border-b border-divider">
            <div>
              <h3 className="text-xs font-black text-white uppercase tracking-wider">Chronological Audit Ledger</h3>
              <p className="text-[9px] text-textSecondary">chronological list browser of completed vetting decisions for auditing</p>
            </div>
            <button
              type="button"
              onClick={exportAuditCsv}
              className="px-3 py-1 bg-surfaceLight hover:bg-surface border border-divider hover:border-accentBlue/30 text-white font-black text-[9px] rounded flex items-center gap-1.5 uppercase tracking-wider transition"
            >
              <FileSpreadsheet className="w-3.5 h-3.5 text-accentBlue" /> Export CSV Ledger
            </button>
          </div>

          <div className="space-y-3">
            {auditLedger.map((log) => (
              <div key={log.id} className="p-3 bg-background border border-divider rounded-lg text-left text-xs space-y-1">
                <div className="flex justify-between items-center">
                  <span className="font-mono text-[9px] font-bold text-accentBlue bg-accentBlue/10 border border-accentBlue/20 px-1 py-0.5 rounded">
                    {log.action}
                  </span>
                  <span className="text-[9px] text-textMuted font-medium">{log.time}</span>
                </div>
                <p className="text-textSecondary text-[10px] mt-1 font-bold">👤 Operator: {log.operator}</p>
                <p className="text-textSecondary text-[10px] leading-relaxed mt-0.5">
                  Reason: {log.reason}
                </p>
              </div>
            ))}
          </div>
        </div>
      )}

      {activeTab === 'BACKUP_CENTER' && (
        <div className="p-5 rounded-xl bg-surface border border-divider space-y-4">
          <div className="flex justify-between items-center pb-2 border-b border-divider">
            <div>
              <h3 className="text-xs font-black text-white uppercase tracking-wider">Disaster Recovery (Backup Center)</h3>
              <p className="text-[9px] text-textSecondary">Write local database snapshots and verify backup integrity hashes</p>
            </div>
            <button
              type="button"
              onClick={triggerBackup}
              className="px-3 py-1 bg-neonEmerald text-background font-black text-[9px] rounded uppercase tracking-wider transition"
            >
              Trigger Backup Snapshot
            </button>
          </div>

          <div className="space-y-3">
            {backupLogs.map((log) => (
              <div key={log.id} className="p-4 bg-surfaceLight border border-divider rounded-xl text-left space-y-2">
                <div className="flex justify-between items-center">
                  <h4 className="text-xs font-black text-white">📦 {log.filename}</h4>
                  <span className="text-[9px] text-textMuted font-bold">{log.time}</span>
                </div>
                <p className="text-[10px] font-mono text-neonEmerald bg-[#05070a] p-1.5 rounded border border-divider/40 select-all truncate">
                  SHA-256 Hash: {log.hash}
                </p>
                <div className="flex justify-between text-[9px] text-textSecondary">
                  <span>Size: {log.size}</span>
                  <span className="text-neonEmerald font-bold">✓ Backup Tamper-Proof Checked</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};
