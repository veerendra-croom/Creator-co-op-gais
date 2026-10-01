import React, { useState } from 'react';
import { LandingScreen } from './screens/LandingScreen';
import { DashboardScreen } from './screens/DashboardScreen';
import { ContentPipelineScreen } from './screens/ContentPipelineScreen';
import { SyndicateScreen } from './screens/SyndicateScreen';
import { FounderCommandCenterScreen } from './screens/FounderCommandCenterScreen';
import { SupportCenterScreen } from './screens/SupportCenterScreen';
import { PremiumSubscriptionScreen } from './screens/PremiumSubscriptionScreen';
import { WorkspaceHub } from './screens/WorkspaceHub';
import { CreatorCommonsScreen } from './screens/CreatorCommonsScreen';
import { AnalyticsDashboardScreen } from './screens/AnalyticsDashboardScreen';
import {
  Bolt,
  LayoutDashboard,
  Film,
  Users2,
  Terminal,
  HelpCircle,
  Gem,
  LogOut,
  Menu,
  X,
  FolderOpen,
  MessageSquare,
  TrendingUp
} from 'lucide-react';

type ScreenID = 
  | 'DASHBOARD' 
  | 'CONTENT_PIPELINE' 
  | 'SYNDICATE' 
  | 'FOUNDER_COMMAND' 
  | 'SUPPORT_CENTER' 
  | 'PREMIUM'
  | 'WORKSPACE_HUB'
  | 'CREATOR_COMMONS'
  | 'ANALYTICS';

interface User {
  displayName: string;
  role: string;
}

const App: React.FC = () => {
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [currentScreen, setCurrentScreen] = useState<ScreenID>('DASHBOARD');
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleLogin = (user: User) => {
    setCurrentUser(user);
    if (user.role === 'PLATFORM_ADMIN') {
      setCurrentScreen('FOUNDER_COMMAND');
    } else {
      setCurrentScreen('DASHBOARD');
    }
  };

  const handleLogout = () => {
    setCurrentUser(null);
    setCurrentScreen('DASHBOARD');
  };

  if (!currentUser) {
    return <LandingScreen onLogin={handleLogin} />;
  }

  // Render current selected screen (Narrowed to guaranteed non-null User)
  const renderScreen = () => {
    switch (currentScreen) {
      case 'DASHBOARD':
        return <DashboardScreen />;
      case 'CONTENT_PIPELINE':
        return <ContentPipelineScreen />;
      case 'SYNDICATE':
        return <SyndicateScreen />;
      case 'FOUNDER_COMMAND':
        return <FounderCommandCenterScreen />;
      case 'SUPPORT_CENTER':
        return <SupportCenterScreen />;
      case 'PREMIUM':
        return <PremiumSubscriptionScreen />;
      case 'WORKSPACE_HUB':
        return <WorkspaceHub />;
      case 'CREATOR_COMMONS':
        return <CreatorCommonsScreen />;
      case 'ANALYTICS':
        return <AnalyticsDashboardScreen />;
      default:
        return <DashboardScreen />;
    }
  };

  const menuItems = [
    { id: 'DASHBOARD', label: 'Cockpit Dashboard', icon: LayoutDashboard, adminOnly: false },
    { id: 'WORKSPACE_HUB', label: 'Workspace Hub', icon: FolderOpen, adminOnly: false },
    { id: 'CONTENT_PIPELINE', label: 'Content Pipeline', icon: Film, adminOnly: false },
    { id: 'SYNDICATE', label: 'Syndicate Treasury', icon: Users2, adminOnly: false },
    { id: 'CREATOR_COMMONS', label: 'Creator Commons', icon: MessageSquare, adminOnly: false },
    { id: 'ANALYTICS', label: 'KPI Telemetry', icon: TrendingUp, adminOnly: false },
    { id: 'PREMIUM', label: 'Creator Premium', icon: Gem, adminOnly: false },
    { id: 'SUPPORT_CENTER', label: 'SLA Support', icon: HelpCircle, adminOnly: false },
    { id: 'FOUNDER_COMMAND', label: 'Founder Command', icon: Terminal, adminOnly: true }
  ] as const;

  const activeMenuItems = menuItems.filter(item => !item.adminOnly || currentUser.role === 'PLATFORM_ADMIN');

  return (
    <div className="min-h-screen flex flex-col md:flex-row bg-background text-white cyber-bg">
      {/* Mobile Top Navbar */}
      <header className="md:hidden flex items-center justify-between px-6 py-4 bg-surface border-b border-divider z-20">
        <div className="flex items-center gap-2">
          <div className="p-1.5 bg-gradient-to-tr from-accentBlue to-neonEmerald rounded-lg">
            <Bolt className="w-5 h-5 text-white" />
          </div>
          <span className="text-sm font-black tracking-widest text-white">CREATOR CO-OP</span>
        </div>
        <button
          type="button"
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          className="p-1 text-textSecondary hover:text-white transition"
        >
          {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
        </button>
      </header>

      {/* Mobile Overlay Menu */}
      {mobileMenuOpen && (
        <div className="md:hidden fixed inset-0 top-[60px] bg-background/95 backdrop-blur-lg z-30 flex flex-col p-6 space-y-6 border-t border-divider">
          <nav className="flex-1 flex flex-col space-y-2">
            {activeMenuItems.map((item) => {
              const Icon = item.icon;
              const isActive = currentScreen === item.id;
              return (
                <button
                  key={item.id}
                  type="button"
                  onClick={() => {
                    setCurrentScreen(item.id);
                    setMobileMenuOpen(false);
                  }}
                  className={`flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-bold text-left transition ${
                    isActive
                      ? 'bg-accentBlue/10 text-accentBlue border border-accentBlue/20'
                      : 'text-textSecondary hover:text-white hover:bg-surfaceLight'
                  }`}
                >
                  <Icon className={`w-5 h-5 ${isActive ? 'text-accentBlue' : ''}`} />
                  {item.label}
                </button>
              );
            })}
          </nav>
          <div className="pt-4 border-t border-divider space-y-4 text-left">
            <div>
              <p className="text-xs font-bold text-white">{currentUser.displayName}</p>
              <p className="text-[10px] text-textSecondary uppercase tracking-wider font-semibold">{currentUser.role}</p>
            </div>
            <button
              type="button"
              onClick={handleLogout}
              className="flex items-center gap-2 text-xs font-bold text-accentRed hover:text-accentRed/80 transition"
            >
              <LogOut className="w-4 h-4" /> SECURE DISCONNECT
            </button>
          </div>
        </div>
      )}

      {/* Desktop Sidebar Navigation Rail */}
      <aside className="hidden md:flex flex-col w-64 bg-surface border-r border-divider h-screen sticky top-0 py-6 px-4 z-20 justify-between">
        <div className="space-y-8 text-left">
          {/* Platform Header */}
          <div className="flex items-center gap-3 px-2">
            <div className="p-2 bg-gradient-to-tr from-accentBlue to-neonEmerald rounded-xl shadow-lg">
              <Bolt className="w-6 h-6 text-white" />
            </div>
            <div>
              <h1 className="text-md font-black tracking-widest text-white">CREATOR CO-OP</h1>
              <p className="text-[9px] text-textSecondary font-bold uppercase tracking-wider">Web Workspace PWA</p>
            </div>
          </div>

          {/* Navigation rail links */}
          <nav className="space-y-1.5">
            {activeMenuItems.map((item) => {
              const Icon = item.icon;
              const isActive = currentScreen === item.id;
              return (
                <button
                  key={item.id}
                  type="button"
                  onClick={() => setCurrentScreen(item.id)}
                  className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl text-xs font-bold text-left transition border ${
                    isActive
                      ? 'bg-accentBlue/10 text-accentBlue border-accentBlue/25'
                      : 'text-textSecondary border-transparent hover:text-white hover:bg-surfaceLight'
                  }`}
                >
                  <Icon className={`w-4 h-4 shrink-0 ${isActive ? 'text-accentBlue' : ''}`} />
                  {item.label}
                </button>
              );
            })}
          </nav>
        </div>

        {/* Desktop User Footer Card */}
        <div className="pt-4 border-t border-divider space-y-4 text-left">
          <div className="px-2">
            <p className="text-xs font-black text-white truncate">{currentUser.displayName}</p>
            <p className="text-[9px] text-textMuted uppercase tracking-wider font-bold mt-0.5">{currentUser.role}</p>
          </div>
          <button
            type="button"
            onClick={handleLogout}
            className="w-full flex items-center justify-center gap-2 px-4 py-2.5 bg-surfaceLight hover:bg-background border border-divider text-accentRed hover:text-white rounded-xl text-xs font-black transition"
          >
            <LogOut className="w-3.5 h-3.5 shrink-0" /> DISCONNECT SRE
          </button>
          <div className="text-[9px] text-textMuted text-center font-bold tracking-wider uppercase mt-2">
            Ver: 1.0.0 • Founder Esc
          </div>
        </div>
      </aside>

      {/* Main Content Area */}
      <main className="flex-1 p-4 md:p-8 overflow-y-auto h-[calc(100vh-60px)] md:h-screen text-left">
        <div className="max-w-6xl mx-auto">
          {renderScreen()}
        </div>
      </main>
    </div>
  );
};

export default App;
