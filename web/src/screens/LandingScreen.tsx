import React, { useState } from 'react';
import { Bolt, ShieldCheck, ArrowRight, Smartphone } from 'lucide-react';

interface LandingScreenProps {
  onLogin: (user: { displayName: string; role: string }) => void;
}

export const LandingScreen = ({ onLogin }: LandingScreenProps) => {
  const [phoneNumber, setPhoneNumber] = useState('');
  const [otp, setOtp] = useState('');
  const [isOtpSent, setIsOtpSent] = useState(false);
  const [name, setName] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSendOtp = (e: React.FormEvent) => {
    e.preventDefault();
    if (!phoneNumber) {
      setError('Please enter your phone number.');
      return;
    }
    setError('');
    setIsLoading(true);
    setTimeout(() => {
      setIsLoading(false);
      setIsOtpSent(true);
    }, 1200);
  };

  const handleVerifyOtp = (e: React.FormEvent) => {
    e.preventDefault();
    if (!otp) {
      setError('Please enter the OTP verification code.');
      return;
    }
    if (!name && isOtpSent) {
      setError('Please enter your display name.');
      return;
    }
    setError('');
    setIsLoading(true);
    setTimeout(() => {
      setIsLoading(false);
      // Determine user privilege: If user signs up as Platform Admin or Admin, give them admin roles
      const systemRole = name.toLowerCase().includes('founder') || name.toLowerCase().includes('admin') || name === 'Botla Veerendra' || name === 'Macha Praveen' ? 'PLATFORM_ADMIN' : 'CREATOR';
      onLogin({ displayName: name || 'Collaborator', role: systemRole });
    }, 1000);
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4 cyber-bg relative text-left">
      {/* Decorative Blur Orbs */}
      <div className="absolute top-1/4 left-1/4 w-72 h-72 bg-accentBlue/10 rounded-full blur-3xl pointer-events-none"></div>
      <div className="absolute bottom-1/4 right-1/4 w-72 h-72 bg-neonEmerald/10 rounded-full blur-3xl pointer-events-none"></div>

      <div className="w-full max-w-md glass-panel rounded-2xl p-8 border border-divider shadow-glass z-10 transition-all duration-300">
        <div className="text-center mb-8">
          <div className="inline-flex p-3 bg-gradient-to-tr from-accentBlue to-neonEmerald rounded-2xl mb-4 shadow-lg animate-pulse">
            <Bolt className="w-8 h-8 text-white" />
          </div>
          <h1 className="text-3xl font-black tracking-tight text-white mb-2">
            CREATOR CO-OP
          </h1>
          <p className="text-sm text-textSecondary max-w-xs mx-auto">
            Secure workspace syndication, analytics, and content pipeline workflows for video creators.
          </p>
        </div>

        {error && (
          <div className="bg-accentRed/10 border border-accentRed/30 text-accentRed text-xs px-4 py-3 rounded-lg mb-6">
            {error}
          </div>
        )}

        {!isOtpSent ? (
          <form onSubmit={handleSendOtp} className="space-y-5">
            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-textSecondary mb-2">
                Carrier Verification
              </label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 flex items-center pl-3 text-textMuted text-sm">
                  <Smartphone className="w-4 h-4" />
                </span>
                <input
                  type="tel"
                  placeholder="+1 (555) 000-0000"
                  className="w-full bg-surfaceLight border border-divider rounded-xl py-3 pl-10 pr-4 text-white placeholder-textMuted focus:outline-none focus:border-accentBlue focus:ring-1 focus:ring-accentBlue transition text-sm"
                  value={phoneNumber}
                  onChange={(e) => setPhoneNumber(e.target.value)}
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-textSecondary mb-2">
                Display Name
              </label>
              <input
                type="text"
                placeholder="Your Full Name (e.g. Botla Veerendra)"
                className="w-full bg-surfaceLight border border-divider rounded-xl py-3 px-4 text-white placeholder-textMuted focus:outline-none focus:border-accentBlue focus:ring-1 focus:ring-accentBlue transition text-sm"
                value={name}
                onChange={(e) => setName(e.target.value)}
              />
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="w-full bg-gradient-to-r from-accentBlue to-accentBlue/80 text-background font-black py-3 px-4 rounded-xl flex items-center justify-center gap-2 hover:from-accentBlue/90 hover:to-accentBlue transition shadow-lg text-sm disabled:opacity-50"
            >
              {isLoading ? (
                <div className="w-5 h-5 border-2 border-background border-t-transparent rounded-full animate-spin"></div>
              ) : (
                <>
                  REQUEST SMS VERIFICATION <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>
          </form>
        ) : (
          <form onSubmit={handleVerifyOtp} className="space-y-5">
            <div className="bg-surfaceLight p-4 rounded-xl border border-divider flex items-center gap-3">
              <ShieldCheck className="w-5 h-5 text-neonEmerald shrink-0" />
              <div>
                <p className="text-xs text-textSecondary">Verification SMS sent to</p>
                <p className="text-sm font-bold text-white">{phoneNumber}</p>
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-textSecondary mb-2">
                6-Digit Verification Code
              </label>
              <input
                type="text"
                maxLength={6}
                placeholder="0 0 0 0 0 0"
                className="w-full bg-surfaceLight border border-divider rounded-xl py-3 text-center tracking-widest text-lg font-mono text-white focus:outline-none focus:border-neonEmerald focus:ring-1 focus:ring-neonEmerald transition"
                value={otp}
                onChange={(e) => setOtp(e.target.value)}
              />
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="w-full bg-gradient-to-r from-neonEmerald to-neonEmerald/80 text-background font-black py-3 px-4 rounded-xl flex items-center justify-center gap-2 hover:from-neonEmerald/90 hover:to-neonEmerald transition shadow-lg text-sm disabled:opacity-50"
            >
              {isLoading ? (
                <div className="w-5 h-5 border-2 border-background border-t-transparent rounded-full animate-spin"></div>
              ) : (
                <>
                  CONFIRM SECURE HANDSHAKE <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>

            <button
              type="button"
              className="w-full text-center text-xs text-textSecondary hover:text-white transition mt-2"
              onClick={() => setIsOtpSent(false)}
            >
              Edit Name or Number
            </button>
          </form>
        )}

        <div className="mt-8 pt-6 border-t border-divider text-center text-[10px] text-textMuted tracking-wider uppercase font-bold">
          Co-Op Platform • Founder: Botla Veerendra • Co-Founder: Macha Praveen
        </div>
      </div>
    </div>
  );
};
