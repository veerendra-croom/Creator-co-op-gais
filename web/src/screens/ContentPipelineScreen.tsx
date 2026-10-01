import React, { useState, useEffect } from 'react';
import { Film, Trash2, Plus, RefreshCcw } from 'lucide-react';

interface Task {
  id: string;
  title: string;
  desc: string;
  stage: 'IDEA' | 'SCRIPT' | 'FILMING' | 'EDITING' | 'READY';
  creator: string;
  renderProgress?: number;
}

export const ContentPipelineScreen: React.FC = () => {
  const [tasks, setTasks] = useState<Task[]>([
    { id: 't1', title: 'Cyberpunk Soundscapes VFX Reel', desc: 'Synthesized bass visualizers using Blender Geometry nodes.', stage: 'EDITING', creator: 'Botla Veerendra', renderProgress: 68 },
    { id: 't2', title: 'Creator Monetization Strategy 2026', desc: 'Long-form editorial on co-op models versus traditional venture agencies.', stage: 'SCRIPT', creator: 'Macha Praveen' },
    { id: 't3', title: 'Workspace SQLite Architecture Breakdown', desc: 'Short-form review explaining offline-first Room database sync patterns.', stage: 'FILMING', creator: 'Srinivas Rao' },
    { id: 't4', title: 'Fable and Opus Comparative Breakdown', desc: 'Script review on UI design guidelines inside high-fidelity apps.', stage: 'IDEA', creator: 'Botla Veerendra' },
    { id: 't5', title: 'Beta Co-Op Launch Presentation', desc: 'Secure mutual agreement and sandbox testing walkthrough. Complete.', stage: 'READY', creator: 'Macha Praveen' }
  ]);

  const [isRenderRunning, setIsRenderRunning] = useState(true);
  const [showAddForm, setShowAddForm] = useState(false);
  const [newTitle, setNewTitle] = useState('');
  const [newDesc, setNewDesc] = useState('');
  const [newStage, setNewStage] = useState<'IDEA' | 'SCRIPT' | 'FILMING' | 'EDITING' | 'READY'>('IDEA');

  // Launch Task 5 Simulator: Live VFX Rendering Progress
  useEffect(() => {
    let interval: any;
    if (isRenderRunning) {
      interval = setInterval(() => {
        setTasks((prev) =>
          prev.map((t) => {
            if (t.stage === 'EDITING' && t.renderProgress !== undefined) {
              const nextProgress = t.renderProgress + Math.floor(Math.random() * 4) + 1;
              return {
                ...t,
                renderProgress: nextProgress >= 100 ? 0 : nextProgress
              };
            }
            return t;
          })
        );
      }, 1000);
    }
    return () => clearInterval(interval);
  }, [isRenderRunning]);

  const handleAddTask = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newTitle) return;
    const newTask: Task = {
      id: `t_${Date.now()}`,
      title: newTitle,
      desc: newDesc,
      stage: newStage,
      creator: 'Botla Veerendra',
      renderProgress: newStage === 'EDITING' ? 10 : undefined
    };
    setTasks((prev) => [...prev, newTask]);
    setNewTitle('');
    setNewDesc('');
    setShowAddForm(false);
  };

  const moveTask = (id: string, nextStage: 'IDEA' | 'SCRIPT' | 'FILMING' | 'EDITING' | 'READY') => {
    setTasks((prev) =>
      prev.map((t) => {
        if (t.id === id) {
          return {
            ...t,
            stage: nextStage,
            renderProgress: nextStage === 'EDITING' ? 10 : undefined
          };
        }
        return t;
      })
    );
  };

  const deleteTask = (id: string) => {
    setTasks((prev) => prev.filter((t) => t.id !== id));
  };

  const lanes: { stage: 'IDEA' | 'SCRIPT' | 'FILMING' | 'EDITING' | 'READY'; label: string; color: string; border: string }[] = [
    { stage: 'IDEA', label: 'Hook & Idea', color: 'text-textSecondary', border: 'border-divider' },
    { stage: 'SCRIPT', label: 'Script Writing', color: 'text-crispAmber', border: 'border-crispAmber/20' },
    { stage: 'FILMING', label: 'In Production', color: 'text-accentBlue', border: 'border-accentBlue/20' },
    { stage: 'EDITING', label: 'Editing / VFX', color: 'text-accentRed', border: 'border-accentRed/20' },
    { stage: 'READY', label: 'Ready to Publish', color: 'text-neonEmerald', border: 'border-neonEmerald/20' }
  ];

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 p-5 rounded-2xl glass-panel border border-divider">
        <div>
          <span className="text-xs font-bold text-accentBlue uppercase tracking-wider">Video Production</span>
          <h2 className="text-2xl font-black text-white mt-1 flex items-center gap-2">
            <Film className="w-6 h-6 text-accentBlue" /> CONTENT PIPELINE
          </h2>
          <p className="text-sm text-textSecondary mt-1">
            Standard Kanban board workflow to track idea briefs, script deliverables, rendering queues, and published proof-of-work.
          </p>
        </div>
        <div className="flex gap-2">
          <button
            type="button"
            onClick={() => setIsRenderRunning(!isRenderRunning)}
            className="px-3 py-2 bg-surfaceLight border border-divider rounded-xl hover:border-accentBlue/30 text-xs font-bold text-white flex items-center gap-2 transition"
          >
            <RefreshCcw className={`w-3.5 h-3.5 text-crispAmber ${isRenderRunning ? 'animate-spin' : ''}`} />
            {isRenderRunning ? 'Pause Renders' : 'Resume Renders'}
          </button>
          <button
            type="button"
            onClick={() => setShowAddForm(!showAddForm)}
            className="px-4 py-2 bg-gradient-to-r from-accentBlue to-accentBlue/80 text-background rounded-xl hover:from-accentBlue text-xs font-black flex items-center gap-2 transition"
          >
            <Plus className="w-3.5 h-3.5 stroke-[3px]" /> ADD DELIVERABLE
          </button>
        </div>
      </div>

      {/* Slide-out / Collapsible Add Form */}
      {showAddForm && (
        <form onSubmit={handleAddTask} className="p-5 rounded-xl glass-panel border border-divider grid grid-cols-1 md:grid-cols-4 gap-4 items-end animate-fade-in">
          <div className="space-y-2">
            <label className="block text-xs font-bold text-textSecondary uppercase">Brief Title</label>
            <input
              type="text"
              placeholder="e.g. Cinematic Sound FX Hook"
              required
              className="w-full bg-surfaceLight border border-divider rounded-lg px-3 py-2 text-white placeholder-textMuted text-xs focus:outline-none focus:border-accentBlue"
              value={newTitle}
              onChange={(e) => setNewTitle(e.target.value)}
            />
          </div>
          <div className="space-y-2">
            <label className="block text-xs font-bold text-textSecondary uppercase">Brief Description</label>
            <input
              type="text"
              placeholder="e.g. Synthesize sound FX files"
              className="w-full bg-surfaceLight border border-divider rounded-lg px-3 py-2 text-white placeholder-textMuted text-xs focus:outline-none focus:border-accentBlue"
              value={newDesc}
              onChange={(e) => setNewDesc(e.target.value)}
            />
          </div>
          <div className="space-y-2">
            <label className="block text-xs font-bold text-textSecondary uppercase">Initial Stage</label>
            <select
              className="w-full bg-surfaceLight border border-divider rounded-lg px-3 py-2 text-white text-xs focus:outline-none focus:border-accentBlue"
              value={newStage}
              onChange={(e) => setNewStage(e.target.value as any)}
            >
              <option value="IDEA">Idea brief</option>
              <option value="SCRIPT">Script/Writing</option>
              <option value="FILMING">In Production/Filming</option>
              <option value="EDITING">Post-Production/VFX</option>
              <option value="READY">Ready to anchor</option>
            </select>
          </div>
          <button
            type="submit"
            className="w-full bg-neonEmerald hover:bg-neonEmerald/90 text-background font-black text-xs py-2 px-4 rounded-lg transition"
          >
            CONFIRM POSTING
          </button>
        </form>
      )}

      {/* Kanban Board Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-4 overflow-x-auto pb-4">
        {lanes.map((lane) => {
          const laneTasks = tasks.filter((t) => t.stage === lane.stage);
          return (
            <div key={lane.stage} className="flex flex-col min-w-[220px] bg-surface rounded-xl border border-divider p-4">
              {/* Lane Header */}
              <div className="flex justify-between items-center pb-3 border-b border-divider mb-3">
                <span className={`text-xs font-black uppercase tracking-wider ${lane.color}`}>{lane.label}</span>
                <span className="text-[10px] bg-surfaceLight border border-divider text-white font-black px-2 py-0.5 rounded-full">
                  {laneTasks.length}
                </span>
              </div>

              {/* Lane Tasks Container */}
              <div className="space-y-3 flex-1 min-h-[300px]">
                {laneTasks.length === 0 ? (
                  <div className="h-full flex items-center justify-center p-4 border border-dashed border-divider rounded-lg text-center text-textMuted text-[10px] uppercase font-bold tracking-wider">
                    Lane Empty
                  </div>
                ) : (
                  laneTasks.map((t) => (
                    <div key={t.id} className="p-4 rounded-lg bg-surfaceLight border border-divider hover:border-accentBlue/30 transition shadow space-y-3 relative group">
                      <div>
                        <h4 className="text-xs font-black text-white leading-snug">{t.title}</h4>
                        <p className="text-[10px] text-textSecondary leading-normal mt-1">{t.desc}</p>
                      </div>

                      {/* Display progress bar if VFX render progress is running */}
                      {t.renderProgress !== undefined && (
                        <div className="space-y-1">
                          <div className="flex justify-between text-[8px] font-bold text-accentRed">
                            <span>VFX RENDER WORKER</span>
                            <span>{t.renderProgress}%</span>
                          </div>
                          <div className="w-full h-1.5 bg-background rounded-full overflow-hidden border border-divider">
                            <div
                              className="h-full bg-accentRed transition-all duration-300"
                              style={{ width: `${t.renderProgress}%` }}
                            ></div>
                          </div>
                        </div>
                      )}

                      <div className="flex justify-between items-center pt-2 border-t border-divider/50 text-[9px] text-textMuted">
                        <span className="font-bold">👤 {t.creator}</span>
                        <div className="flex gap-1.5 opacity-60 group-hover:opacity-100 transition">
                          {lane.stage !== 'IDEA' && (
                            <button
                              type="button"
                              onClick={() => {
                                const prevStages: Record<string, 'IDEA' | 'SCRIPT' | 'FILMING' | 'EDITING'> = {
                                  SCRIPT: 'IDEA',
                                  FILMING: 'SCRIPT',
                                  EDITING: 'FILMING',
                                  READY: 'EDITING'
                                };
                                moveTask(t.id, prevStages[lane.stage]);
                              }}
                              className="p-1 hover:bg-surface border border-divider rounded"
                              title="Move Left"
                            >
                              ←
                            </button>
                          )}
                          {lane.stage !== 'READY' && (
                            <button
                              type="button"
                              onClick={() => {
                                const nextStages: Record<string, 'SCRIPT' | 'FILMING' | 'EDITING' | 'READY'> = {
                                  IDEA: 'SCRIPT',
                                  SCRIPT: 'FILMING',
                                  FILMING: 'EDITING',
                                  EDITING: 'READY'
                                };
                                moveTask(t.id, nextStages[lane.stage]);
                              }}
                              className="p-1 hover:bg-surface border border-divider rounded"
                              title="Move Right"
                            >
                              →
                            </button>
                          )}
                          <button
                            type="button"
                            onClick={() => deleteTask(t.id)}
                            className="p-1 hover:bg-accentRed/10 text-accentRed border border-divider rounded"
                            title="Delete"
                          >
                            <Trash2 className="w-2.5 h-2.5" />
                          </button>
                        </div>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
