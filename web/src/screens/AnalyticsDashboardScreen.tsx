import React from 'react';
import { TrendingUp } from 'lucide-react';

export const AnalyticsDashboardScreen: React.FC = () => {
  return (
    <div className="space-y-6 text-left">
      {/* Banner */}
      <div className="p-6 rounded-2xl glass-panel relative overflow-hidden flex flex-col md:flex-row items-center justify-between border border-divider">
        <div className="absolute top-0 right-0 w-64 h-64 bg-accentBlue/5 rounded-full blur-3xl pointer-events-none"></div>
        <div className="space-y-1">
          <span className="text-xs font-bold text-accentBlue uppercase tracking-wider flex items-center gap-1">
            <TrendingUp className="w-3.5 h-3.5" /> KPI Telemetry
          </span>
          <h2 className="text-2xl font-black text-white">SAAS METRICS DASHBOARD</h2>
          <p className="text-sm text-textSecondary max-w-2xl leading-relaxed">
            Real-time visual graphs measuring co-op growth, activation funnels, retention indexes, and referral coefficients.
          </p>
        </div>
      </div>

      {/* SVG Charts Area */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Chart 1: DAU / WAU Growth (SVG Area) */}
        <div className="p-5 rounded-xl glass-panel border border-divider space-y-4">
          <div>
            <h3 className="text-xs font-black uppercase tracking-wider text-white">Daily Active Cohort (DAU)</h3>
            <p className="text-[10px] text-textSecondary mt-0.5">Aggregated user sessions over the past 7 days</p>
          </div>

          <div className="h-48 w-full bg-background/50 rounded-lg p-2 border border-divider flex items-end relative">
            {/* SVG Area graph */}
            <svg className="w-full h-full" viewBox="0 0 100 100" preserveAspectRatio="none">
              <defs>
                <linearGradient id="chartGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="#38BDF8" stopOpacity="0.4"/>
                  <stop offset="100%" stopColor="#38BDF8" stopOpacity="0"/>
                </linearGradient>
              </defs>
              <path
                d="M 0 100 Q 15 60, 30 75 T 60 40 T 90 20 T 100 10 L 100 100 Z"
                fill="url(#chartGrad)"
              />
              <path
                d="M 0 100 Q 15 60, 30 75 T 60 40 T 90 20 T 100 10"
                fill="none"
                stroke="#38BDF8"
                strokeWidth="2"
              />
            </svg>
            <span className="absolute bottom-2 left-2 text-[9px] text-textMuted font-bold">MON</span>
            <span className="absolute bottom-2 right-2 text-[9px] text-textMuted font-bold">SUN</span>
          </div>
        </div>

        {/* Chart 2: Activation Funnel Conversion (SVG Bar) */}
        <div className="p-5 rounded-xl glass-panel border border-divider space-y-4">
          <div>
            <h3 className="text-xs font-black uppercase tracking-wider text-white">Activation Conversion Funnel</h3>
            <p className="text-[10px] text-textSecondary mt-0.5">Registration to signed agreement milestones</p>
          </div>

          <div className="space-y-4 h-48 flex flex-col justify-center">
            {/* Step 1 */}
            <div className="space-y-1">
              <div className="flex justify-between text-[10px] font-bold text-textSecondary">
                <span>1. REGISTERED ACCOUNT</span>
                <span className="text-white">100%</span>
              </div>
              <div className="w-full h-3 bg-background border border-divider rounded-full overflow-hidden">
                <div className="h-full bg-accentBlue rounded-full" style={{ width: '100%' }}></div>
              </div>
            </div>

            {/* Step 2 */}
            <div className="space-y-1">
              <div className="flex justify-between text-[10px] font-bold text-textSecondary">
                <span>2. WORKSPACE CREATION</span>
                <span className="text-accentBlue">76%</span>
              </div>
              <div className="w-full h-3 bg-background border border-divider rounded-full overflow-hidden">
                <div className="h-full bg-accentBlue rounded-full" style={{ width: '76%' }}></div>
              </div>
            </div>

            {/* Step 3 */}
            <div className="space-y-1">
              <div className="flex justify-between text-[10px] font-bold text-textSecondary">
                <span>3. AGREEMENTS SIGNED</span>
                <span className="text-neonEmerald">54%</span>
              </div>
              <div className="w-full h-3 bg-background border border-divider rounded-full overflow-hidden">
                <div className="h-full bg-neonEmerald rounded-full" style={{ width: '54%' }}></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
