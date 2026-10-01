import React, { useState, useEffect } from 'react';
import { Video, Mic, MicOff, VideoOff, ScreenShare, MessageSquare, PhoneOff, Users, Send, CheckCircle2, MonitorUp, User, ArrowRight } from 'lucide-react';

interface ChatMessage {
  id: string;
  sender: string;
  senderName: string;
  message: string;
  time: string;
}

export const VideoHuddleScreen: React.FC = () => {
  const [isJoined, setIsJoined] = useState(false);
  const [micEnabled, setMicEnabled] = useState(true);
  const [cameraEnabled, setCameraEnabled] = useState(true);
  const [screenShareEnabled, setScreenShareEnabled] = useState(false);
  const [showChatDrawer, setShowChatDrawer] = useState(false);
  const [chatMessageText, setChatMessageText] = useState('');
  const [callDuration, setCallDuration] = useState(0);

  // Volume meter heights for simulation
  const [alexVolume, setAlexVolume] = useState(15);
  const [mayaVolume, setMayaVolume] = useState(10);
  const [thomasVolume, setThomasVolume] = useState(5);
  const [yourVolume, setYourVolume] = useState(0);

  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([
    { id: '1', sender: 'alex', senderName: 'Alex Mercer', message: "Let's review the storyboards for the intro hook.", time: '2 mins ago' },
    { id: '2', sender: 'maya', senderName: 'Maya Lin', message: "Blender VFX models are fully compiled and rendering.", time: '1 min ago' },
  ]);

  // Call duration counter
  useEffect(() => {
    let interval: any;
    if (isJoined) {
      setCallDuration(0);
      interval = setInterval(() => {
        setCallDuration(prev => prev + 1);
      }, 1000);
    } else {
      setCallDuration(0);
    }
    return () => clearInterval(interval);
  }, [isJoined]);

  // Voice activity simulator
  useEffect(() => {
    let interval: any;
    if (isJoined) {
      interval = setInterval(() => {
        setAlexVolume(Math.random() > 0.4 ? Math.floor(Math.random() * 85) + 15 : 10);
        setMayaVolume(Math.random() > 0.5 ? Math.floor(Math.random() * 75) + 15 : 10);
        setThomasVolume(Math.random() > 0.6 ? Math.floor(Math.random() * 95) + 5 : 5);
        setYourVolume(micEnabled && Math.random() > 0.7 ? Math.floor(Math.random() * 90) + 10 : 0);
      }, 700);
    } else {
      setAlexVolume(0);
      setMayaVolume(0);
      setThomasVolume(0);
      setYourVolume(0);
    }
    return () => clearInterval(interval);
  }, [isJoined, micEnabled]);

  // Simulated bot messages
  useEffect(() => {
    if (isJoined) {
      const timers = [
        setTimeout(() => {
          setChatMessages(prev => [
            ...prev,
            { id: '3', sender: 'thomas', senderName: 'Thomas Wright', message: "Audio stems are aligned. Ready to drop them in.", time: 'Just now' }
          ]);
        }, 6000),
        setTimeout(() => {
          setChatMessages(prev => [
            ...prev,
            { id: '4', sender: 'alex', senderName: 'Alex Mercer', message: "Excellent, let's coordinate the final export settings.", time: 'Just now' }
          ]);
        }, 14000)
      ];
      return () => timers.forEach(clearTimeout);
    }
  }, [isJoined]);

  const handleSendMessage = (e: React.FormEvent) => {
    e.preventDefault();
    if (!chatMessageText.trim()) return;
    const newMessage: ChatMessage = {
      id: Date.now().toString(),
      sender: 'me',
      senderName: 'You (Operator)',
      message: chatMessageText.trim(),
      time: 'Just now'
    };
    setChatMessages(prev => [...prev, newMessage]);
    setChatMessageText('');
  };

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  return (
    <div className="min-h-[80vh] flex flex-col bg-background border border-divider rounded-2xl overflow-hidden shadow-2xl relative text-left select-none">
      {/* Header Bar */}
      <div className="bg-surface border-b border-divider p-4 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-accentBlue/15 rounded-xl text-accentBlue">
            <Video className="w-5 h-5 animate-pulse" />
          </div>
          <div>
            <h1 className="text-md font-black tracking-tight text-white uppercase">Decentralized Video Huddle</h1>
            <p className="text-[10px] text-textSecondary uppercase tracking-wider font-semibold">WebRTC Secure Mesh Room</p>
          </div>
        </div>

        {isJoined && (
          <div className="flex items-center gap-4 bg-background/60 px-4 py-1.5 rounded-xl border border-divider text-xs">
            <span className="flex h-2 w-2 relative">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-accentRed opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2 w-2 bg-accentRed"></span>
            </span>
            <span className="font-mono text-white font-black">{formatTime(callDuration)}</span>
            <span className="text-divider">|</span>
            <span className="text-textSecondary flex items-center gap-1 font-bold">
              <Users className="w-3.5 h-3.5 text-accentBlue" /> 4 ACTIVE
            </span>
          </div>
        )}
      </div>

      {!isJoined ? (
        /* --- STATE 1: PRE-JOIN LOBBY --- */
        <div className="flex-1 flex flex-col md:flex-row items-center justify-center p-6 md:p-12 gap-8 bg-gradient-to-b from-background to-surface">
          {/* Bezel Camera Preview Sandbox */}
          <div className="w-full max-w-sm aspect-video bg-black rounded-2xl border border-divider overflow-hidden relative flex flex-col items-center justify-center">
            {cameraEnabled ? (
              <div className="absolute inset-0 bg-surface/30 flex flex-col items-center justify-center">
                {/* Simulated webcam scan lines & feedback */}
                <div className="absolute inset-0 bg-radial-gradient opacity-40 pointer-events-none"></div>
                <div className="w-16 h-16 rounded-full bg-accentBlue/10 border border-accentBlue/30 flex items-center justify-center text-accentBlue mb-2">
                  <User className="w-8 h-8 animate-pulse" />
                </div>
                <p className="text-[10px] text-accentBlue tracking-widest uppercase font-black">
                  📷 VIDEO HARVEST FEED ONLINE
                </p>
              </div>
            ) : (
              <div className="absolute inset-0 bg-surface/80 flex flex-col items-center justify-center text-textMuted">
                <VideoOff className="w-12 h-12 mb-2" />
                <p className="text-xs font-bold uppercase tracking-wider">Camera Hardware Blocked</p>
              </div>
            )}

            {/* Micro toggles over screen */}
            <div className="absolute bottom-4 left-4 right-4 flex justify-center gap-3 z-10">
              <button
                type="button"
                onClick={() => setMicEnabled(!micEnabled)}
                className={`p-2.5 rounded-xl border transition ${
                  micEnabled 
                    ? 'bg-surface border-divider text-white hover:bg-surfaceLight' 
                    : 'bg-accentRed/10 border-accentRed/30 text-accentRed hover:bg-accentRed/20'
                }`}
              >
                {micEnabled ? <Mic className="w-4 h-4" /> : <MicOff className="w-4 h-4" />}
              </button>
              <button
                type="button"
                onClick={() => setCameraEnabled(!cameraEnabled)}
                className={`p-2.5 rounded-xl border transition ${
                  cameraEnabled 
                    ? 'bg-surface border-divider text-white hover:bg-surfaceLight' 
                    : 'bg-accentRed/10 border-accentRed/30 text-accentRed hover:bg-accentRed/20'
                }`}
              >
                {cameraEnabled ? <Video className="w-4 h-4" /> : <VideoOff className="w-4 h-4" />}
              </button>
            </div>
          </div>

          {/* Lobby Entry Options */}
          <div className="max-w-md space-y-6 text-center md:text-left">
            <div className="space-y-2">
              <span className="px-2.5 py-1 bg-accentBlue/10 border border-accentBlue/25 text-accentBlue rounded-full text-[10px] font-black uppercase tracking-wider">
                CO-OP ROOM #HUDDLE-404
              </span>
              <h2 className="text-2xl font-black text-white">READY TO SYNC?</h2>
              <p className="text-xs text-textSecondary leading-relaxed">
                Review your micro-inputs and join our sandbox huddle to coordinate live milestone completions, video edits, and distribution agreements in real-time.
              </p>
            </div>

            {/* SRE Compliance warning */}
            <div className="bg-surfaceLight p-4 rounded-xl border border-divider text-xs space-y-2">
              <div className="flex items-center gap-2 text-neonEmerald font-bold">
                <CheckCircle2 className="w-4 h-4" /> COMPLIANCE GUARANTEE
              </div>
              <p className="text-textSecondary text-[11px]">
                Audio/Video feeds utilize secure peer-to-peer encryption keys and comply with digital identity standards.
              </p>
            </div>

            <button
              type="button"
              onClick={() => setIsJoined(true)}
              className="w-full bg-gradient-to-r from-accentBlue to-accentBlue/80 text-background font-black py-3 px-4 rounded-xl flex items-center justify-center gap-2 hover:from-accentBlue/90 hover:to-accentBlue transition shadow-lg text-sm"
            >
              CONNECT P2P FEED <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      ) : (
        /* --- STATE 2: JOINED HUDDLE GRID --- */
        <div className="flex-1 flex flex-col md:flex-row min-h-[60vh] bg-background relative">
          {/* Main Feed Grid Container */}
          <div className="flex-1 p-6 flex flex-col justify-between">
            {/* Grid of participants */}
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 flex-1 mb-6">
              {/* Participant 1: Alex Mercer */}
              <div className="bg-surface border border-divider rounded-2xl relative overflow-hidden flex flex-col items-center justify-center p-4">
                <div className="relative w-16 h-16 rounded-full overflow-hidden border border-divider bg-background/50 flex items-center justify-center mb-3">
                  <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=Alex" alt="Alex Mercer" className="w-full h-full" />
                  {/* Glowing speaking ring */}
                  <div 
                    className="absolute inset-0 rounded-full border-2 border-accentBlue animate-pulse transition-opacity duration-300"
                    style={{ opacity: alexVolume > 15 ? (alexVolume / 100) : 0 }}
                  ></div>
                </div>
                <div className="text-center">
                  <p className="text-xs font-bold text-white">Alex Mercer</p>
                  <span className="text-[9px] text-textSecondary uppercase tracking-widest">CREATOR VLOG</span>
                </div>
                {/* Dynamic voice decibel feed meter */}
                <div className="absolute bottom-3 left-3 right-3 flex justify-center gap-0.5 h-3 items-end">
                  <div className="w-1 bg-accentBlue/40 rounded-t" style={{ height: `${alexVolume * 0.4}%` }}></div>
                  <div className="w-1 bg-accentBlue/70 rounded-t animate-pulse" style={{ height: `${alexVolume * 0.7}%` }}></div>
                  <div className="w-1 bg-accentBlue rounded-t" style={{ height: `${alexVolume}%` }}></div>
                </div>
              </div>

              {/* Participant 2: Maya Lin */}
              <div className="bg-surface border border-divider rounded-2xl relative overflow-hidden flex flex-col items-center justify-center p-4">
                <div className="relative w-16 h-16 rounded-full overflow-hidden border border-divider bg-background/50 flex items-center justify-center mb-3">
                  <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=Maya" alt="Maya Lin" className="w-full h-full" />
                  <div 
                    className="absolute inset-0 rounded-full border-2 border-neonEmerald animate-pulse transition-opacity duration-300"
                    style={{ opacity: mayaVolume > 15 ? (mayaVolume / 100) : 0 }}
                  ></div>
                </div>
                <div className="text-center">
                  <p className="text-xs font-bold text-white">Maya Lin</p>
                  <span className="text-[9px] text-textSecondary uppercase tracking-widest">VFX COMPOSITOR</span>
                </div>
                <div className="absolute bottom-3 left-3 right-3 flex justify-center gap-0.5 h-3 items-end">
                  <div className="w-1 bg-neonEmerald/40 rounded-t" style={{ height: `${mayaVolume * 0.4}%` }}></div>
                  <div className="w-1 bg-neonEmerald/70 rounded-t animate-pulse" style={{ height: `${mayaVolume * 0.7}%` }}></div>
                  <div className="w-1 bg-neonEmerald rounded-t" style={{ height: `${mayaVolume}%` }}></div>
                </div>
              </div>

              {/* Participant 3: Thomas Wright */}
              <div className="bg-surface border border-divider rounded-2xl relative overflow-hidden flex flex-col items-center justify-center p-4">
                <div className="relative w-16 h-16 rounded-full overflow-hidden border border-divider bg-background/50 flex items-center justify-center mb-3">
                  <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=Thomas" alt="Thomas Wright" className="w-full h-full" />
                  <div 
                    className="absolute inset-0 rounded-full border-2 border-crispAmber animate-pulse transition-opacity duration-300"
                    style={{ opacity: thomasVolume > 15 ? (thomasVolume / 100) : 0 }}
                  ></div>
                </div>
                <div className="text-center">
                  <p className="text-xs font-bold text-white">Thomas Wright</p>
                  <span className="text-[9px] text-textSecondary uppercase tracking-widest">AUDIO ENGINEER</span>
                </div>
                <div className="absolute bottom-3 left-3 right-3 flex justify-center gap-0.5 h-3 items-end">
                  <div className="w-1 bg-crispAmber/40 rounded-t" style={{ height: `${thomasVolume * 0.4}%` }}></div>
                  <div className="w-1 bg-crispAmber/70 rounded-t animate-pulse" style={{ height: `${thomasVolume * 0.7}%` }}></div>
                  <div className="w-1 bg-crispAmber rounded-t" style={{ height: `${thomasVolume}%` }}></div>
                </div>
              </div>

              {/* Participant 4: You (Web Webcam representation) */}
              <div className="bg-surface border border-divider rounded-2xl relative overflow-hidden flex flex-col items-center justify-center p-4">
                <div className="relative w-16 h-16 rounded-full overflow-hidden border border-divider bg-background/50 flex items-center justify-center mb-3">
                  <div className="w-full h-full bg-accentBlue/10 flex items-center justify-center text-accentBlue font-black text-sm">
                    YOU
                  </div>
                  {cameraEnabled && (
                    <div className="absolute inset-0 bg-gradient-to-tr from-accentBlue/20 to-transparent"></div>
                  )}
                  {yourVolume > 15 && (
                    <div className="absolute inset-0 rounded-full border-2 border-accentBlue animate-pulse"></div>
                  )}
                </div>
                <div className="text-center">
                  <p className="text-xs font-bold text-white">You (Operator)</p>
                  <span className="text-[9px] text-textSecondary uppercase tracking-widest">PLATFORM ADMIN</span>
                </div>
                <div className="absolute bottom-3 left-3 right-3 flex justify-center gap-0.5 h-3 items-end">
                  <div className="w-1 bg-accentBlue/40 rounded-t" style={{ height: `${yourVolume * 0.4}%` }}></div>
                  <div className="w-1 bg-accentBlue/70 rounded-t animate-pulse" style={{ height: `${yourVolume * 0.7}%` }}></div>
                  <div className="w-1 bg-accentBlue rounded-t" style={{ height: `${yourVolume}%` }}></div>
                </div>
              </div>
            </div>

            {/* Screen Share Simulated Active Overlay */}
            {screenShareEnabled && (
              <div className="mb-6 p-4 bg-accentBlue/10 border border-accentBlue/25 rounded-2xl flex flex-col sm:flex-row items-center justify-between gap-4 animate-fadeIn">
                <div className="flex items-center gap-3">
                  <div className="p-2.5 bg-accentBlue rounded-xl text-background">
                    <MonitorUp className="w-5 h-5 animate-bounce" />
                  </div>
                  <div>
                    <h4 className="text-xs font-black text-white uppercase">Active Screen Broadcast Active</h4>
                    <p className="text-[10px] text-textSecondary">P2P clients are capturing your display viewport stems</p>
                  </div>
                </div>
                <button
                  type="button"
                  onClick={() => setScreenShareEnabled(false)}
                  className="px-4 py-1.5 bg-accentRed hover:bg-accentRed/90 text-white font-black text-xs rounded-xl transition"
                >
                  HALT BROADCAST
                </button>
              </div>
            )}

            {/* Bottom Controls bar */}
            <div className="bg-surface/60 border border-divider p-4 rounded-2xl flex flex-wrap justify-center gap-3 items-center">
              <button
                type="button"
                onClick={() => setMicEnabled(!micEnabled)}
                className={`p-3 rounded-xl border transition ${
                  micEnabled 
                    ? 'bg-surface border-divider text-white hover:bg-surfaceLight' 
                    : 'bg-accentRed/10 border-accentRed/30 text-accentRed hover:bg-accentRed/20'
                }`}
                title={micEnabled ? "Mute Microphone" : "Unmute Microphone"}
              >
                {micEnabled ? <Mic className="w-5 h-5" /> : <MicOff className="w-5 h-5" />}
              </button>

              <button
                type="button"
                onClick={() => setCameraEnabled(!cameraEnabled)}
                className={`p-3 rounded-xl border transition ${
                  cameraEnabled 
                    ? 'bg-surface border-divider text-white hover:bg-surfaceLight' 
                    : 'bg-accentRed/10 border-accentRed/30 text-accentRed hover:bg-accentRed/20'
                }`}
                title={cameraEnabled ? "Disable Camera" : "Enable Camera"}
              >
                {cameraEnabled ? <Video className="w-5 h-5" /> : <VideoOff className="w-5 h-5" />}
              </button>

              <button
                type="button"
                onClick={() => setScreenShareEnabled(!screenShareEnabled)}
                className={`p-3 rounded-xl border transition ${
                  screenShareEnabled 
                    ? 'bg-accentBlue/20 border-accentBlue/40 text-accentBlue hover:bg-accentBlue/30' 
                    : 'bg-surface border-divider text-white hover:bg-surfaceLight'
                }`}
                title="Share Screen"
              >
                <ScreenShare className="w-5 h-5" />
              </button>

              <button
                type="button"
                onClick={() => setShowChatDrawer(!showChatDrawer)}
                className={`p-3 rounded-xl border transition ${
                  showChatDrawer 
                    ? 'bg-accentBlue/20 border-accentBlue/40 text-accentBlue hover:bg-accentBlue/30' 
                    : 'bg-surface border-divider text-white hover:bg-surfaceLight'
                }`}
                title="Huddle Chat"
              >
                <MessageSquare className="w-5 h-5" />
              </button>

              <div className="w-px h-6 bg-divider mx-2"></div>

              <button
                type="button"
                onClick={() => setIsJoined(false)}
                className="bg-accentRed hover:bg-accentRed/90 text-white font-black py-3 px-6 rounded-xl flex items-center gap-2 transition text-xs shadow-lg"
              >
                <PhoneOff className="w-4 h-4" /> DISCONNECT
              </button>
            </div>
          </div>

          {/* Collapsible Chat Drawer Panel */}
          {showChatDrawer && (
            <div className="w-full md:w-80 bg-surface border-t md:border-t-0 md:border-l border-divider flex flex-col justify-between animate-slideLeft z-10">
              <div className="p-4 border-b border-divider flex items-center justify-between">
                <span className="text-xs font-black text-white uppercase tracking-wider flex items-center gap-1.5">
                  <MessageSquare className="w-4 h-4 text-accentBlue" /> HUDDLE COMM CHAT
                </span>
                <button
                  type="button"
                  onClick={() => setShowChatDrawer(false)}
                  className="text-xs text-textSecondary hover:text-white font-bold"
                >
                  Hide ×
                </button>
              </div>

              {/* Chat Message Lists */}
              <div className="flex-1 p-4 space-y-3 overflow-y-auto max-h-[300px] md:max-h-none">
                {chatMessages.map((msg) => (
                  <div key={msg.id} className="space-y-1 text-xs">
                    <div className="flex justify-between font-bold">
                      <span className={
                        msg.sender === 'me' 
                          ? 'text-accentBlue' 
                          : msg.sender === 'alex' 
                            ? 'text-neonEmerald' 
                            : msg.sender === 'maya' 
                              ? 'text-accentRed' 
                              : 'text-crispAmber'
                      }>{msg.senderName}</span>
                      <span className="text-[9px] text-textMuted">{msg.time}</span>
                    </div>
                    <p className="bg-background/40 border border-divider rounded-xl p-2.5 text-textSecondary text-[11px] leading-relaxed">
                      {msg.message}
                    </p>
                  </div>
                ))}
              </div>

              {/* Message composer */}
              <form onSubmit={handleSendMessage} className="p-3 border-t border-divider flex gap-2">
                <input
                  type="text"
                  placeholder="Transmit to channel..."
                  className="flex-grow bg-background border border-divider rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-accentBlue focus:ring-1 focus:ring-accentBlue transition placeholder-textMuted"
                  value={chatMessageText}
                  onChange={(e) => setChatMessageText(e.target.value)}
                />
                <button
                  type="submit"
                  className="p-2 bg-accentBlue hover:bg-accentBlue/90 rounded-xl text-background transition"
                >
                  <Send className="w-4 h-4" />
                </button>
              </form>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
