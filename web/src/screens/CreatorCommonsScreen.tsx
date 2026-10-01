import React, { useState, useEffect } from 'react';
import { Send, ThumbsUp, MessageSquare, Award, Sparkles, MessageCircle, Video, PhoneOff, Mic, MicOff, VideoOff } from 'lucide-react';

interface FeedPost {
  id: string;
  creator: string;
  role: string;
  avatar: string;
  content: string;
  time: string;
  likes: number;
  comments: number;
  hasLiked?: boolean;
}

interface Message {
  id: string;
  sender: string;
  avatar: string;
  content: string;
  time: string;
  self: boolean;
}

export const CreatorCommonsScreen: React.FC = () => {
  const [activeSubTab, setActiveTab] = useState<'FORUM' | 'DIRECT_MESSAGES' | 'VIDEO_HUDDLE'>('FORUM');

  // Sub-Tab 1: Discussion Feed (Forum)
  const [posts, setPosts] = useState<FeedPost[]>([
    {
      id: 'p1',
      creator: 'Botla Veerendra',
      role: 'Lead Founder / SRE',
      avatar: 'BV',
      content: '🚨 ANNOUNCEMENT: Platform Core v1.5 is fully deployed! Offline write-through triggers inside Room SQLite db are successfully verified. Let me know if any latency concerns are detected.',
      time: '12 mins ago',
      likes: 8,
      comments: 2
    },
    {
      id: 'p2',
      creator: 'Macha Praveen',
      role: 'Co-Founder / Ops',
      avatar: 'MP',
      content: '🚀 Syndicate Treasury split models are now live on the test cockpit. Make sure to update your contract signatures inside the Agreement Vault screen before milestone payouts trigger.',
      time: '1 hour ago',
      likes: 6,
      comments: 1
    },
    {
      id: 'p3',
      creator: 'Srinivas Rao',
      role: 'Verified Editor',
      avatar: 'SR',
      content: 'Synthesized some modular filters and uploaded the wav presets inside the Commons soundscapes CDN! Download link anchors are SHA-256 verified.',
      time: '3 hours ago',
      likes: 4,
      comments: 0
    }
  ]);
  const [newPostText, setNewPostText] = useState('');

  // Sub-Tab 2: Direct Messages (Interactive chat)
  const [chatMessages, setChatMessages] = useState<Message[]>([
    { id: 'm1', sender: 'Macha Praveen', avatar: 'MP', content: 'Hey Veerendra, did you check the latency logs inside the SRE Command screen?', time: '10:14 AM', self: false },
    { id: 'm2', sender: 'Botla Veerendra', avatar: 'BV', content: 'Yes! Mapped at 28ms. Everything is fully synchronized with the Room database schemas.', time: '10:15 AM', self: true },
    { id: 'm3', sender: 'Macha Praveen', avatar: 'MP', content: 'Perfect. I will proceed with the visual updates on Vercel deployment preview.', time: '10:16 AM', self: false }
  ]);
  const [newChatMessage, setNewChatMessage] = useState('');

  // Sub-Tab 3: Video Huddle (Simulated WebRTC)
  const [isMuted, setIsMuted] = useState(false);
  const [isVideoOff, setIsVideoOff] = useState(false);
  const [activeSpeaker, setActiveSpeaker] = useState('Botla Veerendra');

  useEffect(() => {
    let interval: any;
    if (activeSubTab === 'VIDEO_HUDDLE') {
      const speakers = ['Botla Veerendra', 'Macha Praveen', 'Srinivas Rao'];
      interval = setInterval(() => {
        const randomSpeaker = speakers[Math.floor(Math.random() * speakers.length)];
        setActiveSpeaker(randomSpeaker);
      }, 3000);
    }
    return () => clearInterval(interval);
  }, [activeSubTab]);

  const handleCreatePost = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newPostText) return;
    const newPost: FeedPost = {
      id: `post_${Date.now()}`,
      creator: 'Botla Veerendra',
      role: 'Lead Founder / SRE',
      avatar: 'BV',
      content: newPostText,
      time: 'Just Now',
      likes: 0,
      comments: 0
    };
    setPosts([newPost, ...posts]);
    setNewPostText('');
  };

  const handleLike = (id: string) => {
    setPosts(posts.map(p => {
      if (p.id === id) {
        return {
          ...p,
          likes: p.hasLiked ? p.likes - 1 : p.likes + 1,
          hasLiked: !p.hasLiked
        };
      }
      return p;
    }));
  };

  const handleSendChatMessage = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newChatMessage) return;
    const newMsg: Message = {
      id: `msg_${Date.now()}`,
      sender: 'Botla Veerendra',
      avatar: 'BV',
      content: newChatMessage,
      time: 'Just Now',
      self: true
    };
    setChatMessages([...chatMessages, newMsg]);
    setNewChatMessage('');
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 text-left">
      {/* Sub navigation bar */}
      <div className="lg:col-span-12 flex border-b border-divider bg-surface p-1 rounded-lg">
        <button
          type="button"
          onClick={() => setActiveTab('FORUM')}
          className={`flex-1 py-2 font-bold text-xs uppercase tracking-wider flex items-center justify-center gap-1.5 transition ${
            activeSubTab === 'FORUM' ? 'bg-surfaceLight text-white rounded' : 'text-textSecondary hover:text-white'
          }`}
        >
          <MessageSquare className="w-3.5 h-3.5" /> Commons Feed
        </button>
        <button
          type="button"
          onClick={() => setActiveTab('DIRECT_MESSAGES')}
          className={`flex-1 py-2 font-bold text-xs uppercase tracking-wider flex items-center justify-center gap-1.5 transition ${
            activeSubTab === 'DIRECT_MESSAGES' ? 'bg-surfaceLight text-white rounded' : 'text-textSecondary hover:text-white'
          }`}
        >
          <MessageCircle className="w-3.5 h-3.5" /> Private Chats
        </button>
        <button
          type="button"
          onClick={() => setActiveTab('VIDEO_HUDDLE')}
          className={`flex-1 py-2 font-bold text-xs uppercase tracking-wider flex items-center justify-center gap-1.5 transition ${
            activeSubTab === 'VIDEO_HUDDLE' ? 'bg-surfaceLight text-white rounded' : 'text-textSecondary hover:text-white'
          }`}
        >
          <Video className="w-3.5 h-3.5" /> Video Huddles
        </button>
      </div>

      {/* Primary tab views */}
      <div className="lg:col-span-8 space-y-6">
        {activeSubTab === 'FORUM' && (
          <div className="space-y-6">
            {/* Create forum post form */}
            <div className="p-5 rounded-xl glass-panel border border-divider bg-surface">
              <form onSubmit={handleCreatePost} className="space-y-4">
                <textarea
                  rows={3}
                  placeholder="What are you working on? Share a production brief, sound preset, or milestone..."
                  required
                  className="w-full bg-surfaceLight border border-divider rounded-xl p-3 text-white placeholder-textMuted text-xs focus:outline-none focus:border-accentBlue resize-none"
                  value={newPostText}
                  onChange={(e) => setNewPostText(e.target.value)}
                />
                <div className="flex justify-between items-center">
                  <span className="text-[10px] text-textMuted uppercase font-bold flex items-center gap-1">
                    <Sparkles className="w-3.5 h-3.5 text-accentBlue" /> Arena Social Feed
                  </span>
                  <button
                    type="submit"
                    className="px-4 py-1.5 bg-accentBlue text-background font-black text-xs rounded-xl hover:bg-accentBlue/90 flex items-center gap-1.5 transition"
                  >
                    <Send className="w-3.5 h-3.5" /> POST BRIEF
                  </button>
                </div>
              </form>
            </div>

            {/* Posts feed list */}
            <div className="space-y-4">
              {posts.map((post) => (
                <div key={post.id} className="p-5 rounded-xl bg-surface border border-divider text-left space-y-4">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className="w-9 h-9 rounded-full bg-accentBlue/10 text-accentBlue border border-accentBlue/25 flex items-center justify-center font-black text-xs">
                        {post.avatar}
                      </div>
                      <div>
                        <h4 className="text-xs font-black text-white">{post.creator}</h4>
                        <p className="text-[9px] text-accentBlue uppercase font-bold tracking-wider mt-0.5">{post.role}</p>
                      </div>
                    </div>
                    <span className="text-[10px] text-textMuted font-bold">{post.time}</span>
                  </div>

                  <p className="text-xs text-textSecondary leading-relaxed">{post.content}</p>

                  <div className="flex gap-4 pt-3 border-t border-divider/50 text-[11px] text-textMuted">
                    <button
                      type="button"
                      onClick={() => handleLike(post.id)}
                      className={`flex items-center gap-1.5 hover:text-white transition font-bold ${
                        post.hasLiked ? 'text-accentBlue' : ''
                      }`}
                    >
                      <ThumbsUp className="w-3.5 h-3.5" /> {post.likes} Endorsement{post.likes !== 1 ? 's' : ''}
                    </button>
                    <div className="flex items-center gap-1.5 font-bold">
                      <MessageSquare className="w-3.5 h-3.5" /> {post.comments} Comment{post.comments !== 1 ? 's' : ''}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {activeSubTab === 'DIRECT_MESSAGES' && (
          <div className="p-5 rounded-xl bg-surface border border-divider flex flex-col h-[500px] justify-between">
            {/* Direct messages header */}
            <div className="pb-3 border-b border-divider flex items-center gap-3">
              <div className="w-8 h-8 bg-neonEmerald/10 text-neonEmerald border border-neonEmerald/20 rounded-full flex items-center justify-center font-black text-xs">
                MP
              </div>
              <div>
                <h4 className="text-xs font-black text-white">Macha Praveen</h4>
                <p className="text-[8px] text-neonEmerald uppercase font-bold">Strategic Operations</p>
              </div>
            </div>

            {/* Chat message streams list */}
            <div className="flex-1 overflow-y-auto py-4 space-y-4 pr-1">
              {chatMessages.map((msg) => (
                <div key={msg.id} className={`flex items-start gap-3 max-w-[80%] ${msg.self ? 'ml-auto flex-row-reverse text-right' : ''}`}>
                  <div className={`w-7 h-7 rounded-full flex items-center justify-center font-bold text-[10px] ${
                    msg.self ? 'bg-accentBlue/10 text-accentBlue border border-accentBlue/20' : 'bg-surfaceLight text-white border border-divider'
                  }`}>
                    {msg.avatar}
                  </div>
                  <div className={`p-3 rounded-2xl text-xs leading-normal ${
                    msg.self ? 'bg-accentBlue text-background rounded-tr-none font-medium' : 'bg-surfaceLight text-white rounded-tl-none border border-divider'
                  }`}>
                    <p className="font-semibold text-[10px] opacity-75 mb-0.5">{msg.sender}</p>
                    <p>{msg.content}</p>
                    <span className="block text-[8px] opacity-60 mt-1">{msg.time}</span>
                  </div>
                </div>
              ))}
            </div>

            {/* Send chat form */}
            <form onSubmit={handleSendChatMessage} className="pt-3 border-t border-divider flex gap-2">
              <input
                type="text"
                placeholder="Type your message..."
                required
                className="flex-1 bg-surfaceLight border border-divider rounded-xl px-4 py-2 text-white placeholder-textMuted text-xs focus:outline-none focus:border-accentBlue"
                value={newChatMessage}
                onChange={(e) => setNewChatMessage(e.target.value)}
              />
              <button
                type="submit"
                className="px-4 py-2 bg-accentBlue text-background font-black text-xs rounded-xl hover:bg-accentBlue/90 transition"
              >
                Send
              </button>
            </form>
          </div>
        )}

        {activeSubTab === 'VIDEO_HUDDLE' && (
          <div className="p-5 rounded-xl bg-surface border border-divider space-y-6">
            <div className="flex justify-between items-center pb-3 border-b border-divider">
              <div>
                <h3 className="text-xs font-black text-white uppercase tracking-wider">Sync Video Huddle</h3>
                <p className="text-[10px] text-textSecondary">WebRTC secure communication channel</p>
              </div>
              <span className="text-[9px] bg-accentRed/15 text-accentRed border border-accentRed/30 px-2 py-0.5 rounded font-black tracking-wider animate-pulse">
                🎙️ LIVE HUDDLE
              </span>
            </div>

            {/* Video Streams grid simulation */}
            <div className="grid grid-cols-2 gap-4">
              {/* Stream 1: User stream */}
              <div className={`aspect-video rounded-xl bg-background border flex flex-col justify-between p-3 relative overflow-hidden ${
                activeSpeaker === 'Botla Veerendra' ? 'border-accentBlue ring-2 ring-accentBlue/20' : 'border-divider'
              }`}>
                <div className="absolute top-2 right-2 p-1 bg-background/50 rounded-md text-[8px] font-bold text-accentBlue">
                  {activeSpeaker === 'Botla Veerendra' ? '🎤 SPEAKING' : 'Idle'}
                </div>
                <div className="flex-1 flex items-center justify-center">
                  {isVideoOff ? (
                    <div className="text-textMuted text-xs font-bold uppercase tracking-wider">Video Muted</div>
                  ) : (
                    <div className="w-12 h-12 bg-accentBlue/10 text-accentBlue border border-accentBlue/20 rounded-full flex items-center justify-center font-black text-sm">
                      BV
                    </div>
                  )}
                </div>
                <span className="text-[9px] font-bold bg-background/50 px-2 py-0.5 rounded text-white self-start">
                  Botla Veerendra (You)
                </span>
              </div>

              {/* Stream 2: Partner stream */}
              <div className={`aspect-video rounded-xl bg-background border flex flex-col justify-between p-3 relative overflow-hidden ${
                activeSpeaker === 'Macha Praveen' ? 'border-neonEmerald ring-2 ring-neonEmerald/20' : 'border-divider'
              }`}>
                <div className="absolute top-2 right-2 p-1 bg-background/50 rounded-md text-[8px] font-bold text-neonEmerald">
                  {activeSpeaker === 'Macha Praveen' ? '🎤 SPEAKING' : 'Idle'}
                </div>
                <div className="flex-1 flex items-center justify-center">
                  <div className="w-12 h-12 bg-neonEmerald/10 text-neonEmerald border border-neonEmerald/20 rounded-full flex items-center justify-center font-black text-sm animate-bounce">
                    MP
                  </div>
                </div>
                <span className="text-[9px] font-bold bg-background/50 px-2 py-0.5 rounded text-white self-start">
                  Macha Praveen
                </span>
              </div>
            </div>

            {/* Stream Call controls */}
            <div className="flex justify-center gap-3 pt-4 border-t border-divider">
              <button
                type="button"
                onClick={() => setIsMuted(!isMuted)}
                className={`p-3 rounded-full border transition ${
                  isMuted ? 'bg-accentRed/10 border-accentRed/30 text-accentRed' : 'bg-surfaceLight border-divider hover:text-white'
                }`}
                title={isMuted ? 'Unmute Mic' : 'Mute Mic'}
              >
                {isMuted ? <MicOff className="w-4 h-4" /> : <Mic className="w-4 h-4" />}
              </button>
              <button
                type="button"
                onClick={() => setIsVideoOff(!isVideoOff)}
                className={`p-3 rounded-full border transition ${
                  isVideoOff ? 'bg-accentRed/10 border-accentRed/30 text-accentRed' : 'bg-surfaceLight border-divider hover:text-white'
                }`}
                title={isVideoOff ? 'Turn Video On' : 'Turn Video Off'}
              >
                {isVideoOff ? <VideoOff className="w-4 h-4" /> : <Video className="w-4 h-4" />}
              </button>
              <button
                type="button"
                onClick={() => setActiveTab('FORUM')}
                className="p-3 bg-accentRed text-white rounded-full hover:bg-accentRed/90 transition"
                title="Disconnect Call"
              >
                <PhoneOff className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Social Sidebar */}
      <div className="lg:col-span-4 space-y-6">
        {/* Endorsements / Reputations Guide */}
        <div className="p-5 rounded-xl glass-panel border border-divider space-y-4 text-left">
          <h3 className="text-xs font-black uppercase tracking-wider text-accentBlue flex items-center gap-1.5">
            <Award className="w-4 h-4 text-accentBlue" /> Reputation Matrix
          </h3>
          <p className="text-[11px] text-textSecondary leading-normal">
            By endorsing posts inside Creator Commons, you award peers with verified **Reputation points**. Users with scores &gt; 80 obtain priority SRE access and premium vetting.
          </p>

          <div className="pt-2 border-t border-divider text-[10px] text-textMuted">
            ⚡ Always endorse constructive briefs, reliable audio presets, or complete milestone deliveries.
          </div>
        </div>
      </div>
    </div>
  );
};
