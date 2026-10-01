import React, { useState } from 'react';
import { Folder, CheckSquare, BookOpen, Trash2, Info } from 'lucide-react';

interface Workspace {
  id: string;
  name: string;
  description: string;
  members: number;
  maxMembers: number;
}

interface WorkspaceFile {
  name: string;
  size: string;
  date: string;
}

interface TaskItem {
  id: string;
  text: string;
  completed: boolean;
  assignedTo: string;
  dueDate: string;
}

export const WorkspaceHub: React.FC = () => {
  const [workspaces, setWorkspaces] = useState<Workspace[]>([
    { id: 'ws1', name: 'Alpha Cinematic Universe', description: 'Production planning for long-form sci-fi VFX series.', members: 4, maxMembers: 10 },
    { id: 'ws2', name: 'Synthesizer Sounds Co-Op', description: 'Collaborative audio presets and soundscapes.', members: 3, maxMembers: 5 },
    { id: 'ws3', name: 'Founder Investor Pitch', description: 'Strategic milestones and treasury split agreements.', members: 2, maxMembers: 5 }
  ]);

  const [selectedWs, setSelectedWs] = useState<string>('ws1');
  const [activeTab, setActiveTab] = useState<'FILES' | 'TASKS' | 'KNOWLEDGE'>('FILES');

  // Modals state
  const [showCreateWs, setShowCreateWs] = useState(false);
  const [showSettings, setShowSettings] = useState(false);
  const [showInvite, setShowInvite] = useState(false);
  const [selectedTaskDetails, setSelectedTaskDetails] = useState<TaskItem | null>(null);

  // Forms inputs
  const [newWsName, setNewWsName] = useState('');
  const [newWsDesc, setNewWsDesc] = useState('');
  const [newWsMaxMembers, setNewWsMaxMembers] = useState(5);
  const [inviteEmail, setInviteEmail] = useState('');
  const [inviteRole, setInviteRole] = useState('Editor');

  // Interactive Files state
  const [files, setFiles] = useState<Record<string, WorkspaceFile[]>>({
    ws1: [
      { name: 'VFX_GeometryNodes_v3.blend', size: '42.8 MB', date: 'Oct 01, 2026' },
      { name: 'ColorGrading_LUT_Rec709.cube', size: '1.4 MB', date: 'Sep 28, 2026' },
      { name: 'Teaser_Voiceover_Raw.wav', size: '18.2 MB', date: 'Sep 25, 2026' }
    ],
    ws2: [
      { name: 'SynthBass_Preset_Massive.nmsv', size: '124 KB', date: 'Sep 30, 2026' },
      { name: 'CoOp_Soundtrack_FinalMix.mp3', size: '9.4 MB', date: 'Sep 24, 2026' }
    ],
    ws3: [
      { name: 'MilestonePayout_SLA_DocuSign.pdf', size: '2.1 MB', date: 'Sep 29, 2026' },
      { name: 'TreasurySplits_Ledger_Proof.json', size: '4 KB', date: 'Sep 29, 2026' }
    ]
  });

  // Interactive Tasks state
  const [tasks, setTasks] = useState<Record<string, TaskItem[]>>({
    ws1: [
      { id: 't1', text: 'Render Teaser visualizers inside Blender', completed: true, assignedTo: 'Botla Veerendra', dueDate: 'Oct 02, 2026' },
      { id: 't2', text: 'Distribute mutual co-op agreements for sign-off', completed: false, assignedTo: 'Macha Praveen', dueDate: 'Oct 05, 2026' },
      { id: 't3', text: 'Scrape YouTube Analytics OAuth for retention coefficients', completed: false, assignedTo: 'Srinivas Rao', dueDate: 'Oct 08, 2026' }
    ],
    ws2: [
      { id: 't4', text: 'Record modular filter sweeps', completed: true, assignedTo: 'Macha Praveen', dueDate: 'Sep 30, 2026' },
      { id: 't5', text: 'Publish presets inside Creator Commons S3', completed: true, assignedTo: 'Srinivas Rao', dueDate: 'Oct 01, 2026' }
    ],
    ws3: [
      { id: 't6', text: 'Verify registered cohort limit is under 25 caps', completed: true, assignedTo: 'Botla Veerendra', dueDate: 'Sep 29, 2026' },
      { id: 't7', text: 'Anchor DocuSign embedded contract triggers', completed: false, assignedTo: 'Macha Praveen', dueDate: 'Oct 12, 2026' }
    ]
  });

  const [newFileText, setNewFileText] = useState('');
  const [newTaskText, setNewTaskText] = useState('');

  const currentWsObj = workspaces.find(w => w.id === selectedWs) || workspaces[0];

  const handleCreateWorkspace = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newWsName) return;
    const newWs: Workspace = {
      id: `ws_${Date.now()}`,
      name: newWsName,
      description: newWsDesc,
      members: 1,
      maxMembers: newWsMaxMembers
    };
    setWorkspaces([...workspaces, newWs]);
    setSelectedWs(newWs.id);
    setNewWsName('');
    setNewWsDesc('');
    setNewWsMaxMembers(5);
    setShowCreateWs(false);
  };

  const handleUpdateSettings = (e: React.FormEvent) => {
    e.preventDefault();
    alert('Workspace settings successfully anchored inside Room SQL Database!');
    setShowSettings(false);
  };

  const handleSendInvite = (e: React.FormEvent) => {
    e.preventDefault();
    if (!inviteEmail) return;
    alert(`Workspace invitation securely dispatched to ${inviteEmail} for the position of [${inviteRole}]!`);
    setInviteEmail('');
    setShowInvite(false);
  };

  const handleAddFile = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newFileText) return;
    const newFile: WorkspaceFile = {
      name: newFileText,
      size: `${(Math.random() * 5 + 1).toFixed(1)} MB`,
      date: 'Just Now'
    };
    setFiles(prev => ({
      ...prev,
      [selectedWs]: [newFile, ...(prev[selectedWs] || [])]
    }));
    setNewFileText('');
  };

  const handleAddTask = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newTaskText) return;
    const newTask: TaskItem = {
      id: `task_${Date.now()}`,
      text: newTaskText,
      completed: false,
      assignedTo: 'Botla Veerendra',
      dueDate: 'Oct 10, 2026'
    };
    setTasks(prev => ({
      ...prev,
      [selectedWs]: [...(prev[selectedWs] || []), newTask]
    }));
    setNewTaskText('');
  };

  const toggleTask = (taskId: string) => {
    setTasks(prev => ({
      ...prev,
      [selectedWs]: (prev[selectedWs] || []).map(t =>
        t.id === taskId ? { ...t, completed: !t.completed } : t
      )
    }));
  };

  const deleteTask = (taskId: string) => {
    setTasks(prev => ({
      ...prev,
      [selectedWs]: (prev[selectedWs] || []).filter(t => t.id !== taskId)
    }));
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 text-left">
      {/* Workspace Sidebar / Selector */}
      <div className="lg:col-span-4 space-y-4">
        <div className="flex justify-between items-center">
          <h3 className="text-xs font-black uppercase tracking-wider text-accentBlue">Active Spaces</h3>
          <button
            type="button"
            onClick={() => setShowCreateWs(true)}
            className="px-2 py-0.5 bg-accentBlue text-background text-[9px] font-black uppercase tracking-widest rounded hover:bg-accentBlue/95 transition"
          >
            + Create Space
          </button>
        </div>

        <div className="space-y-3">
          {workspaces.map((w) => {
            const isSelected = w.id === selectedWs;
            return (
              <div
                key={w.id}
                onClick={() => setSelectedWs(w.id)}
                className={`p-4 rounded-xl text-left border cursor-pointer transition ${
                  isSelected
                    ? 'bg-surfaceLight border-accentBlue/40'
                    : 'bg-surface border-divider hover:border-textSecondary/20'
                }`}
              >
                <div className="flex justify-between items-start">
                  <h4 className="text-xs font-black text-white">{w.name}</h4>
                  <span className="text-[9px] bg-background border border-divider text-textSecondary font-bold px-2 py-0.5 rounded">
                    👥 {w.members} / {w.maxMembers}
                  </span>
                </div>
                <p className="text-[10px] text-textSecondary leading-normal mt-1.5">{w.description}</p>
              </div>
            );
          })}
        </div>
      </div>

      {/* Workspace Detail Tabs Area */}
      <div className="lg:col-span-8 p-5 rounded-xl glass-panel border border-divider flex flex-col space-y-5">
        {/* Workspace Title header */}
        <div className="pb-4 border-b border-divider flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <span className="text-[10px] font-bold text-accentBlue uppercase tracking-wider">COLLABORATION WORKSPACE</span>
            <h2 className="text-lg font-black text-white mt-0.5">{currentWsObj.name}</h2>
            <p className="text-xs text-textSecondary mt-0.5 leading-relaxed">{currentWsObj.description}</p>
          </div>
          <div className="flex gap-2 self-start sm:self-auto">
            <button
              type="button"
              onClick={() => setShowInvite(true)}
              className="px-3 py-1.5 bg-surfaceLight border border-divider hover:border-accentBlue/30 text-[10px] font-black text-white uppercase rounded-lg transition"
            >
              Invite
            </button>
            <button
              type="button"
              onClick={() => setShowSettings(true)}
              className="px-3 py-1.5 bg-surfaceLight border border-divider hover:border-accentBlue/30 text-[10px] font-black text-white uppercase rounded-lg transition"
            >
              Settings
            </button>
          </div>
        </div>

        {/* Workspace Tab headers */}
        <div className="flex border-b border-divider/50 bg-background/50 rounded-lg p-1">
          <button
            type="button"
            onClick={() => setActiveTab('FILES')}
            className={`flex-1 py-1.5 rounded-md font-bold text-[10px] uppercase tracking-wider flex items-center justify-center gap-1.5 transition ${
              activeTab === 'FILES' ? 'bg-surfaceLight text-white shadow-sm' : 'text-textSecondary hover:text-white'
            }`}
          >
            <Folder className="w-3.5 h-3.5" /> File Vault
          </button>
          <button
            type="button"
            onClick={() => setActiveTab('TASKS')}
            className={`flex-1 py-1.5 rounded-md font-bold text-[10px] uppercase tracking-wider flex items-center justify-center gap-1.5 transition ${
              activeTab === 'TASKS' ? 'bg-surfaceLight text-white shadow-sm' : 'text-textSecondary hover:text-white'
            }`}
          >
            <CheckSquare className="w-3.5 h-3.5" /> Checklists
          </button>
          <button
            type="button"
            onClick={() => setActiveTab('KNOWLEDGE')}
            className={`flex-1 py-1.5 rounded-md font-bold text-[10px] uppercase tracking-wider flex items-center justify-center gap-1.5 transition ${
              activeTab === 'KNOWLEDGE' ? 'bg-surfaceLight text-white shadow-sm' : 'text-textSecondary hover:text-white'
            }`}
          >
            <BookOpen className="w-3.5 h-3.5" /> Knowledge Base
          </button>
        </div>

        {/* Tab Contents */}
        <div className="flex-1">
          {activeTab === 'FILES' && (
            <div className="space-y-4">
              {/* Add file form */}
              <form onSubmit={handleAddFile} className="flex gap-2">
                <input
                  type="text"
                  placeholder="Insert asset name (e.g. SFX_Whosh_v2.wav)"
                  required
                  className="flex-1 bg-surfaceLight border border-divider rounded-lg px-3 py-1.5 text-white placeholder-textMuted text-xs focus:outline-none focus:border-accentBlue"
                  value={newFileText}
                  onChange={(e) => setNewFileText(e.target.value)}
                />
                <button
                  type="submit"
                  className="px-4 py-1.5 bg-accentBlue text-background font-black text-xs rounded-lg hover:bg-accentBlue/90 transition"
                >
                  UPLOAD ASSET
                </button>
              </form>

              {/* Files list */}
              <div className="space-y-2">
                {(files[selectedWs] || []).length === 0 ? (
                  <div className="text-center p-8 border border-dashed border-divider rounded-lg text-textMuted text-xs uppercase font-bold tracking-wider">
                    0 Files Uploaded
                  </div>
                ) : (
                  (files[selectedWs] || []).map((file, idx) => (
                    <div key={idx} className="p-3 bg-surfaceLight border border-divider rounded-lg flex items-center justify-between text-left hover:border-accentBlue/20 transition">
                      <div className="flex items-center gap-3">
                        <Folder className="w-4 h-4 text-accentBlue shrink-0" />
                        <div>
                          <p className="text-xs font-bold text-white leading-snug">{file.name}</p>
                          <p className="text-[9px] text-textMuted uppercase font-bold mt-0.5">{file.date} • SHA-256 Anchored</p>
                        </div>
                      </div>
                      <span className="text-[10px] text-textSecondary font-bold shrink-0">{file.size}</span>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}

          {activeTab === 'TASKS' && (
            <div className="space-y-4">
              {/* Add task form */}
              <form onSubmit={handleAddTask} className="flex gap-2">
                <input
                  type="text"
                  placeholder="Create production subtask..."
                  required
                  className="flex-1 bg-surfaceLight border border-divider rounded-lg px-3 py-1.5 text-white placeholder-textMuted text-xs focus:outline-none focus:border-accentBlue"
                  value={newTaskText}
                  onChange={(e) => setNewTaskText(e.target.value)}
                />
                <button
                  type="submit"
                  className="px-4 py-1.5 bg-neonEmerald text-background font-black text-xs rounded-lg hover:bg-neonEmerald/90 transition"
                >
                  ADD TASK
                </button>
              </form>

              {/* Tasks list */}
              <div className="space-y-2">
                {(tasks[selectedWs] || []).length === 0 ? (
                  <div className="text-center p-8 border border-dashed border-divider rounded-lg text-textMuted text-xs uppercase font-bold tracking-wider">
                    0 Active Tasks
                  </div>
                ) : (
                  (tasks[selectedWs] || []).map((task) => (
                    <div
                      key={task.id}
                      className="p-3 bg-surfaceLight border border-divider rounded-lg flex items-center justify-between text-left hover:border-textSecondary/10 transition"
                    >
                      <div className="flex items-center gap-3">
                        <input
                          type="checkbox"
                          className="w-4 h-4 rounded border-divider text-accentBlue focus:ring-accentBlue bg-background accent-accentBlue cursor-pointer"
                          checked={task.completed}
                          onChange={() => toggleTask(task.id)}
                        />
                        <button
                          type="button"
                          onClick={() => setSelectedTaskDetails(task)}
                          className={`text-xs font-bold leading-snug hover:text-accentBlue text-left transition ${
                            task.completed ? 'line-through text-textMuted' : 'text-white'
                          }`}
                        >
                          {task.text}
                        </button>
                      </div>
                      <button
                        type="button"
                        onClick={() => deleteTask(task.id)}
                        className="text-textMuted hover:text-accentRed transition p-1"
                        title="Delete Task"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}

          {activeTab === 'KNOWLEDGE' && (
            <div className="space-y-4 text-left">
              <span className="text-[9px] bg-accentBlue/10 text-accentBlue border border-accentBlue/20 px-2 py-0.5 rounded font-black uppercase tracking-wider">
                Workspace Guidebook (Knowledge Base)
              </span>
              <div className="p-4 bg-background border border-divider rounded-lg space-y-4">
                <div className="space-y-1">
                  <h4 className="text-xs font-bold text-white">How do we manage co-op files?</h4>
                  <p className="text-[11px] text-textSecondary leading-normal">
                    Files are stored in Room SQLite cache and bi-directionally synchronized to Supabase PostgreSQL delta lists. Standard assets are automatically hash-mapped for cryptographic verifications.
                  </p>
                </div>
                <div className="space-y-1">
                  <h4 className="text-xs font-bold text-white">Who signs off on agreements?</h4>
                  <p className="text-[11px] text-textSecondary leading-normal">
                    The Lead Director (Founder) drafts milestone payment rules. Once participants submit fulfillments, a webhook reviews signatures via embedded DocuSign controllers to dispatch splits.
                  </p>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Modal 1: Create Workspace Dialog */}
      {showCreateWs && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-md flex items-center justify-center p-4 z-50">
          <form onSubmit={handleCreateWorkspace} className="w-full max-w-md bg-surface border border-divider rounded-2xl p-6 text-left space-y-5">
            <div className="flex justify-between items-center pb-2 border-b border-divider">
              <h3 className="text-sm font-black text-white uppercase tracking-wider">Provision Workspace</h3>
              <button type="button" onClick={() => setShowCreateWs(false)} className="text-xs font-bold text-textMuted hover:text-white">
                Close ×
              </button>
            </div>

            <div className="space-y-1.5">
              <label className="block text-[10px] font-bold text-textSecondary uppercase">Workspace Name</label>
              <input
                type="text"
                placeholder="e.g. Gamma Audio Preset Co-Op"
                required
                className="w-full bg-surfaceLight border border-divider rounded-lg px-3 py-2 text-white placeholder-textMuted text-xs focus:outline-none focus:border-accentBlue"
                value={newWsName}
                onChange={(e) => setNewWsName(e.target.value)}
              />
            </div>

            <div className="space-y-1.5">
              <label className="block text-[10px] font-bold text-textSecondary uppercase">Brief Description</label>
              <textarea
                rows={3}
                placeholder="Explain the collaborative objectives, video brief lanes, and milestone payout targets..."
                className="w-full bg-surfaceLight border border-divider rounded-lg px-3 py-2 text-white placeholder-textMuted text-xs focus:outline-none focus:border-accentBlue resize-none"
                value={newWsDesc}
                onChange={(e) => setNewWsDesc(e.target.value)}
              />
            </div>

            <div className="space-y-1.5">
              <label className="block text-[10px] font-bold text-textSecondary uppercase">Max Cohort Members Cap</label>
              <input
                type="number"
                min={2}
                max={25}
                className="w-full bg-surfaceLight border border-divider rounded-lg px-3 py-2 text-white text-xs focus:outline-none focus:border-accentBlue"
                value={newWsMaxMembers}
                onChange={(e) => setNewWsMaxMembers(parseInt(e.target.value) || 5)}
              />
            </div>

            <button
              type="submit"
              className="w-full py-2 bg-accentBlue text-background font-black text-xs rounded-xl hover:bg-accentBlue/90 transition"
            >
              LAUNCH SECURE WORKSPACE
            </button>
          </form>
        </div>
      )}

      {/* Modal 2: Workspace Settings Dialog */}
      {showSettings && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-md flex items-center justify-center p-4 z-50">
          <form onSubmit={handleUpdateSettings} className="w-full max-w-md bg-surface border border-divider rounded-2xl p-6 text-left space-y-5">
            <div className="flex justify-between items-center pb-2 border-b border-divider">
              <h3 className="text-sm font-black text-white uppercase tracking-wider">Workspace Policy Editor</h3>
              <button type="button" onClick={() => setShowSettings(false)} className="text-xs font-bold text-textMuted hover:text-white">
                Close ×
              </button>
            </div>

            <div className="p-3 bg-surfaceLight border border-divider rounded-lg flex items-center gap-2">
              <Info className="w-4 h-4 text-accentBlue shrink-0" />
              <p className="text-[10px] text-textSecondary">
                Configure policy caps cached in your local `user_settings_table`. Overrides apply immediately.
              </p>
            </div>

            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <h4 className="text-xs font-bold text-white">Enable S3 Downloads</h4>
                  <p className="text-[9px] text-textMuted">Allow members to download sound packs</p>
                </div>
                <input type="checkbox" defaultChecked className="w-4 h-4 rounded text-accentBlue accent-accentBlue" />
              </div>

              <div className="flex items-center justify-between">
                <div>
                  <h4 className="text-xs font-bold text-white">Force DocuSign Agreements</h4>
                  <p className="text-[9px] text-textMuted">Lock pipeline edits until splits sign-off</p>
                </div>
                <input type="checkbox" defaultChecked className="w-4 h-4 rounded text-accentBlue accent-accentBlue" />
              </div>
            </div>

            <button
              type="submit"
              className="w-full py-2 bg-neonEmerald text-background font-black text-xs rounded-xl hover:bg-neonEmerald/90 transition"
            >
              ANCHOR OVERRIDES
            </button>
          </form>
        </div>
      )}

      {/* Modal 3: Workspace Invitation Dialog */}
      {showInvite && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-md flex items-center justify-center p-4 z-50">
          <form onSubmit={handleSendInvite} className="w-full max-w-md bg-surface border border-divider rounded-2xl p-6 text-left space-y-5">
            <div className="flex justify-between items-center pb-2 border-b border-divider">
              <h3 className="text-sm font-black text-white uppercase tracking-wider">Dispatch Handshake Invitation</h3>
              <button type="button" onClick={() => setShowInvite(false)} className="text-xs font-bold text-textMuted hover:text-white">
                Close ×
              </button>
            </div>

            <div className="space-y-1.5">
              <label className="block text-[10px] font-bold text-textSecondary uppercase">Teammate Email Address</label>
              <input
                type="email"
                placeholder="editor@creatorcoop.com"
                required
                className="w-full bg-surfaceLight border border-divider rounded-lg px-3 py-2 text-white placeholder-textMuted text-xs focus:outline-none focus:border-accentBlue"
                value={inviteEmail}
                onChange={(e) => setInviteEmail(e.target.value)}
              />
            </div>

            <div className="space-y-1.5">
              <label className="block text-[10px] font-bold text-textSecondary uppercase">Assign Syndicate Role</label>
              <select
                className="w-full bg-surfaceLight border border-divider rounded-lg px-3 py-2 text-white text-xs focus:outline-none focus:border-accentBlue"
                value={inviteRole}
                onChange={(e) => setInviteRole(e.target.value)}
              >
                <option value="Editor">Video Editor</option>
                <option value="Compositor">Compositor</option>
                <option value="Audio Engineer">Sound Engineer</option>
                <option value="VFX Lead">Visual Effects Lead</option>
              </select>
            </div>

            <button
              type="submit"
              className="w-full py-2 bg-accentBlue text-background font-black text-xs rounded-xl hover:bg-accentBlue/90 transition"
            >
              DISPATCH CO-OP INVITE
            </button>
          </form>
        </div>
      )}

      {/* Modal 4: Task Details Dialog */}
      {selectedTaskDetails && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-md flex items-center justify-center p-4 z-50">
          <div className="w-full max-sm bg-surface border border-divider rounded-2xl p-6 text-left space-y-5">
            <div className="flex justify-between items-center pb-2 border-b border-divider">
              <h3 className="text-xs font-black text-white uppercase tracking-wider">Milestone Task Details</h3>
              <button type="button" onClick={() => setSelectedTaskDetails(null)} className="text-xs font-bold text-textMuted hover:text-white">
                Close ×
              </button>
            </div>

            <div className="space-y-4">
              <div>
                <h4 className="text-xs text-textSecondary uppercase font-bold">Task Content</h4>
                <p className="text-sm font-bold text-white leading-snug mt-1">{selectedTaskDetails.text}</p>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <h4 className="text-[10px] text-textSecondary uppercase font-bold">Assigned To</h4>
                  <p className="text-xs font-bold text-white mt-1">👤 {selectedTaskDetails.assignedTo}</p>
                </div>
                <div>
                  <h4 className="text-[10px] text-textSecondary uppercase font-bold">Due Date</h4>
                  <p className="text-xs font-bold text-crispAmber mt-1">📅 {selectedTaskDetails.dueDate}</p>
                </div>
              </div>

              <div className="p-3 bg-surfaceLight border border-divider rounded-lg flex items-center justify-between">
                <span className="text-[10px] text-textSecondary uppercase font-bold">Resolution Status</span>
                <span className={`text-[9px] font-black uppercase tracking-wider px-2 py-0.5 rounded border ${
                  selectedTaskDetails.completed 
                    ? 'bg-neonEmerald/10 border-neonEmerald text-neonEmerald'
                    : 'bg-crispAmber/10 border-crispAmber text-crispAmber'
                }`}>
                  {selectedTaskDetails.completed ? 'COMPLETED' : 'PENDING ACTION'}
                </span>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
