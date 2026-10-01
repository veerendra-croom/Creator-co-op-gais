import React, { useState } from 'react';
import { User, Mail, Link, Plus, Trash2, Briefcase, DollarSign, Save } from 'lucide-react';

interface ProfileProps {
  initialUser: {
    displayName: string;
    email: string;
    role: string;
  };
  onSave: (updatedUser: { displayName: string; role: string; email: string }) => void;
}

export const EditProfileScreen: React.FC<ProfileProps> = ({ initialUser, onSave }) => {
  const [displayName, setDisplayName] = useState(initialUser.displayName);
  const [bio, setBio] = useState('Digital Creator working on next-generation storytelling visual pipelines.');
  const [websiteUrl, setWebsiteUrl] = useState('https://creatorcoop.com');
  const [specialty, setSpecialty] = useState('Video Editor');
  const [availabilityStatus, setAvailabilityStatus] = useState('AVAILABLE');
  
  // Skills and portfolio
  const [skills, setSkills] = useState<string[]>(['FCPX', 'After Effects', 'Premiere Pro', 'DaVinci Resolve']);
  const [portfolioItems, setPortfolioItems] = useState<string[]>(['Creative Showreel 2026', 'Milestone Commercial Cut']);
  const [newSkill, setNewSkill] = useState('');
  const [newPortfolio, setNewPortfolio] = useState('');

  // Seeking work listing states
  const [isSeekingWork, setIsSeekingWork] = useState(false);
  const [lfwSkills, setLfwSkills] = useState('');
  const [lfwAvailability, setLfwAvailability] = useState('');
  const [lfwRateExpectations, setLfwRateExpectations] = useState('');

  const [saveSuccess, setSaveSuccess] = useState(false);

  const specialtiesList = [
    "Video Editor", 
    "Scriptwriter", 
    "VFX Artist", 
    "3D Animator", 
    "Sound Engineer", 
    "Growth Strategist", 
    "Thumbnail Designer"
  ];

  const handleAddSkill = () => {
    if (!newSkill.trim() || skills.includes(newSkill.trim())) return;
    setSkills(prev => [...prev, newSkill.trim()]);
    setNewSkill('');
  };

  const handleAddPortfolio = () => {
    if (!newPortfolio.trim() || portfolioItems.includes(newPortfolio.trim())) return;
    setPortfolioItems(prev => [...prev, newPortfolio.trim()]);
    setNewPortfolio('');
  };

  const handleSaveProfile = () => {
    onSave({
      displayName,
      role: initialUser.role,
      email: initialUser.email
    });
    setSaveSuccess(true);
    setTimeout(() => setSaveSuccess(false), 3000);
  };

  return (
    <div className="space-y-6 text-left select-none animate-fadeIn">
      {/* Header Panel */}
      <div className="bg-surface border border-divider p-6 rounded-2xl flex flex-col sm:flex-row items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="p-3 bg-accentBlue/15 rounded-2xl text-accentBlue">
            <User className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-xl font-black text-white uppercase tracking-tight">MANAGE CREATOR IDENTITY</h1>
            <p className="text-xs text-textSecondary uppercase tracking-wider font-semibold">Configure public-key brand portfolio & availability</p>
          </div>
        </div>

        <button
          type="button"
          onClick={handleSaveProfile}
          className="px-5 py-2.5 bg-accentBlue hover:bg-accentBlue/90 text-background font-black text-xs rounded-xl flex items-center gap-2 transition shadow-lg"
        >
          <Save className="w-4 h-4" /> SAVE PROFILE
        </button>
      </div>

      {saveSuccess && (
        <div className="bg-neonEmerald/10 border border-neonEmerald/30 text-neonEmerald text-xs px-4 py-3 rounded-xl font-bold animate-fadeIn">
          ✅ PROFILE CO-OP SYNCHRONIZATION SUCCESSFUL! Updated on ledger database.
        </div>
      )}

      {/* Profile Form Details */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Core fields */}
        <div className="lg:col-span-2 bg-surface border border-divider p-6 rounded-2xl space-y-4">
          <h3 className="text-xs font-black text-white uppercase tracking-wider border-b border-divider pb-3">
            Identity Fields
          </h3>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-textSecondary uppercase mb-1">Display Name *</label>
              <input
                type="text"
                className="w-full bg-surfaceLight border border-divider rounded-xl py-2.5 px-3 text-xs text-white focus:outline-none focus:border-accentBlue transition"
                value={displayName}
                onChange={(e) => setDisplayName(e.target.value)}
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-textSecondary uppercase mb-1">Website URL</label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-textMuted">
                  <Link className="w-3.5 h-3.5" />
                </span>
                <input
                  type="url"
                  className="w-full bg-surfaceLight border border-divider rounded-xl py-2.5 pl-9 pr-3 text-xs text-white focus:outline-none focus:border-accentBlue transition"
                  value={websiteUrl}
                  onChange={(e) => setWebsiteUrl(e.target.value)}
                />
              </div>
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-textSecondary uppercase mb-1">Primary Specialty / Role</label>
            <select
              className="w-full bg-surfaceLight border border-divider rounded-xl py-2.5 px-3 text-xs text-white focus:outline-none focus:border-accentBlue appearance-none transition"
              value={specialty}
              onChange={(e) => setSpecialty(e.target.value)}
            >
              {specialtiesList.map((spec) => (
                <option key={spec} value={spec}>{spec}</option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-bold text-textSecondary uppercase mb-1">Bio Description</label>
            <textarea
              rows={3}
              className="w-full bg-surfaceLight border border-divider rounded-xl py-2 px-3 text-xs text-white focus:outline-none focus:border-accentBlue transition"
              value={bio}
              onChange={(e) => setBio(e.target.value)}
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-textSecondary uppercase mb-1">Availability</label>
              <select
                className="w-full bg-surfaceLight border border-divider rounded-xl py-2.5 px-3 text-xs text-white focus:outline-none focus:border-accentBlue appearance-none transition"
                value={availabilityStatus}
                onChange={(e) => setAvailabilityStatus(e.target.value)}
              >
                <option value="AVAILABLE">🟢 Available for Collaboration</option>
                <option value="BUSY">🟡 In Active Production</option>
                <option value="OFFLINE">🔴 Offline / Sabbatical</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-bold text-textSecondary uppercase mb-1">Email (Read Only)</label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-textMuted">
                  <Mail className="w-3.5 h-3.5" />
                </span>
                <input
                  type="email"
                  readOnly
                  disabled
                  className="w-full bg-background/50 border border-divider rounded-xl py-2.5 pl-9 pr-3 text-xs text-textMuted"
                  value={initialUser.email}
                />
              </div>
            </div>
          </div>
        </div>

        {/* Skills and Portfolio list builders */}
        <div className="bg-surface border border-divider p-6 rounded-2xl space-y-6">
          <h3 className="text-xs font-black text-white uppercase tracking-wider border-b border-divider pb-3">
            Vetted Skills & Portfolios
          </h3>

          {/* Skills Builder */}
          <div className="space-y-2">
            <label className="block text-xs font-bold text-textSecondary uppercase">Vlog Skills List</label>
            <div className="flex gap-2">
              <input
                type="text"
                placeholder="e.g. Photoshop"
                className="flex-grow bg-surfaceLight border border-divider rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-accentBlue"
                value={newSkill}
                onChange={(e) => setNewSkill(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleAddSkill()}
              />
              <button
                type="button"
                onClick={handleAddSkill}
                className="p-2 bg-accentBlue hover:bg-accentBlue/95 text-background rounded-xl"
              >
                <Plus className="w-4 h-4" />
              </button>
            </div>
            <div className="flex flex-wrap gap-1.5 pt-1">
              {skills.map((skill) => (
                <span key={skill} className="inline-flex items-center gap-1 px-2 py-0.5 bg-background border border-divider rounded text-[10px] text-textSecondary">
                  {skill}
                  <button type="button" onClick={() => setSkills(prev => prev.filter(s => s !== skill))} className="text-accentRed font-bold hover:text-white">×</button>
                </span>
              ))}
            </div>
          </div>

          {/* Portfolio Builder */}
          <div className="space-y-2 pt-2 border-t border-divider">
            <label className="block text-xs font-bold text-textSecondary uppercase">External Links Portfolios</label>
            <div className="flex gap-2">
              <input
                type="text"
                placeholder="e.g. YouTube Showcase Link"
                className="flex-grow bg-surfaceLight border border-divider rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-accentBlue"
                value={newPortfolio}
                onChange={(e) => setNewPortfolio(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleAddPortfolio()}
              />
              <button
                type="button"
                onClick={handleAddPortfolio}
                className="p-2 bg-accentBlue hover:bg-accentBlue/95 text-background rounded-xl"
              >
                <Plus className="w-4 h-4" />
              </button>
            </div>
            <div className="space-y-1 pt-1">
              {portfolioItems.map((item) => (
                <div key={item} className="flex justify-between items-center p-2 bg-background/50 border border-divider rounded-xl text-xs text-textSecondary">
                  <span className="truncate">{item}</span>
                  <button type="button" onClick={() => setPortfolioItems(prev => prev.filter(p => p !== item))} className="text-accentRed hover:text-white">
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Seeking Work Listing Subsection */}
      <div className="bg-surface border border-divider p-6 rounded-2xl space-y-4">
        <div className="flex items-center justify-between pb-3 border-b border-divider">
          <h3 className="text-xs font-black text-white uppercase tracking-wider flex items-center gap-2">
            <Briefcase className="w-4 h-4 text-neonEmerald" /> Seeking Active Production Work (LFW Listing)
          </h3>
          <label className="relative inline-flex items-center cursor-pointer">
            <input 
              type="checkbox" 
              className="sr-only peer"
              checked={isSeekingWork}
              onChange={(e) => setIsSeekingWork(e.target.checked)}
            />
            <div className="w-11 h-6 bg-background peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-textSecondary after:border-divider after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-neonEmerald peer-checked:after:bg-background"></div>
          </label>
        </div>

        {isSeekingWork ? (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 animate-slideDown text-xs">
            <div>
              <label className="block font-bold text-textSecondary uppercase mb-1">Production Skills Target</label>
              <input
                type="text"
                className="w-full bg-surfaceLight border border-divider rounded-xl py-2 px-3 text-white focus:outline-none"
                placeholder="e.g. Cinema Editing, VFX Modeling"
                value={lfwSkills}
                onChange={(e) => setLfwSkills(e.target.value)}
              />
            </div>
            <div>
              <label className="block font-bold text-textSecondary uppercase mb-1">Availability Term</label>
              <input
                type="text"
                className="w-full bg-surfaceLight border border-divider rounded-xl py-2 px-3 text-white focus:outline-none"
                placeholder="e.g. 10 hours/week, Immediate"
                value={lfwAvailability}
                onChange={(e) => setLfwAvailability(e.target.value)}
              />
            </div>
            <div>
              <label className="block font-bold text-textSecondary uppercase mb-1">Expected Rate Split</label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-textMuted">
                  <DollarSign className="w-3.5 h-3.5" />
                </span>
                <input
                  type="text"
                  className="w-full bg-surfaceLight border border-divider rounded-xl py-2 pl-8 pr-3 text-white focus:outline-none"
                  placeholder="e.g. $45/hr or 15% revenue split"
                  value={lfwRateExpectations}
                  onChange={(e) => setLfwRateExpectations(e.target.value)}
                />
              </div>
            </div>
          </div>
        ) : (
          <p className="text-xs text-textSecondary">
            Your looking-for-work status is offline. Enable this toggle to broadcast your profile to the global co-op marketplace.
          </p>
        )}
      </div>
    </div>
  );
};
