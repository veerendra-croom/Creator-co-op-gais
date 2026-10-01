import React, { useState, useEffect, useRef } from 'react';
import { MessageSquare, Send, Search, CheckCheck, FolderPlus, ArrowLeft } from 'lucide-react';
import { dbService, DirectMessage } from '../dbService';

interface Creator {
  id: string;
  name: string;
  username: string;
  avatar: string;
  online: boolean;
  lastMsg: string;
  unread: number;
}

export const DirectMessagesScreen: React.FC = () => {
  const [selectedCreator, setSelectedCreator] = useState<Creator | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [inputText, setInputText] = useState('');
  const [messages, setMessages] = useState<DirectMessage[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  
  const [creators, setCreators] = useState<Creator[]>([
    { id: '1', name: 'Alex Mercer', username: 'alex_tech', avatar: 'Alex', online: true, lastMsg: 'Storyboards are looking incredible!', unread: 1 },
    { id: '2', name: 'Maya Lin', username: 'maya_3d', avatar: 'Maya', online: true, lastMsg: 'VFX render is finishing up now.', unread: 0 },
    { id: '3', name: 'Thomas Wright', username: 'thomas_audio', avatar: 'Thomas', online: false, lastMsg: 'Stems are aligned and mixed.', unread: 0 },
    { id: '4', name: 'David Miller', username: 'miller_cine', avatar: 'David', online: true, lastMsg: 'Can we schedule a fast sync?', unread: 2 },
  ]);

  // Load messages whenever a creator is selected
  useEffect(() => {
    if (!selectedCreator) {
      setMessages([]);
      return;
    }

    const fetchMessages = async () => {
      setIsLoading(true);
      const data = await dbService.getDirectMessages(selectedCreator.id);
      setMessages(data);
      setIsLoading(false);
    };

    fetchMessages();
  }, [selectedCreator]);

  const messageEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    messageEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputText.trim() || !selectedCreator) return;

    const trimmedText = inputText.trim();
    setInputText('');

    // Append instantly to state for snappy UI
    const tempMsg: DirectMessage = {
      id: `temp_${Date.now()}`,
      senderId: 'me',
      recipientId: selectedCreator.id,
      text: trimmedText,
      createdAt: Date.now()
    };
    setMessages(prev => [...prev, tempMsg]);

    // Save to database/Supabase
    const savedMsg = await dbService.sendDirectMessage('me', selectedCreator.id, trimmedText);
    
    // Replace temp with saved msg
    setMessages(prev => prev.map(m => m.id === tempMsg.id ? savedMsg : m));

    // Update last message in sidebar
    setCreators(prev => prev.map(c => c.id === selectedCreator.id ? { ...c, lastMsg: trimmedText } : c));

    // Simulate real-time creator reply after a short delay for interactive sandbox
    setTimeout(async () => {
      const botReplyText = `⚡ RECEIVED CO-OP DISPATCH: Thanks for syncing! I've logged this to our milestone workspace. Let's do a huddle call later.`;
      
      const botMsg = await dbService.sendDirectMessage(selectedCreator.id, 'me', botReplyText);
      setMessages(prev => [...prev, botMsg]);

      // Update last message in sidebar
      setCreators(prev => prev.map(c => c.id === selectedCreator.id ? { ...c, lastMsg: botReplyText } : c));
    }, 1500);
  };

  const sendWorkspaceInvite = async () => {
    if (!selectedCreator) return;
    const inviteText = `📌 WORKSPACE INVITE: Hey @${selectedCreator.username}, I'd like to invite you to collaborate on our active milestone workspace! Click here to accept.`;
    
    const inviteMsg = await dbService.sendDirectMessage('me', selectedCreator.id, inviteText);
    setMessages(prev => [...prev, inviteMsg]);
  };

  const filteredCreators = creators.filter(c => 
    c.name.toLowerCase().includes(searchQuery.toLowerCase()) || 
    c.username.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const formatTime = (timestamp: number) => {
    return new Date(timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  return (
    <div className="min-h-[80vh] flex flex-col md:flex-row bg-background border border-divider rounded-2xl overflow-hidden shadow-2xl relative text-left select-none animate-fadeIn">
      {/* Sidebar List */}
      <div className={`w-full md:w-80 bg-surface border-r border-divider flex flex-col ${selectedCreator ? 'hidden md:flex' : 'flex'}`}>
        <div className="p-4 border-b border-divider space-y-3">
          <div className="flex items-center gap-2">
            <MessageSquare className="w-5 h-5 text-accentBlue" />
            <h2 className="text-sm font-black text-white uppercase tracking-wider">CREATOR MESSENGER</h2>
          </div>
          <div className="relative">
            <span className="absolute inset-y-0 left-0 pl-2.5 flex items-center text-textMuted">
              <Search className="w-3.5 h-3.5" />
            </span>
            <input
              type="text"
              placeholder="Search active creators..."
              className="w-full bg-surfaceLight border border-divider rounded-xl py-1.5 pl-8 pr-4 text-xs text-white placeholder-textMuted focus:outline-none"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
        </div>

        {/* Contact List */}
        <div className="flex-1 overflow-y-auto divide-y divide-divider/30">
          {filteredCreators.map((c) => (
            <div
              key={c.id}
              onClick={() => {
                setSelectedCreator(c);
                // Reset unread count
                setCreators(prev => prev.map(item => item.id === c.id ? { ...item, unread: 0 } : item));
              }}
              className={`p-4 flex items-center gap-3 cursor-pointer transition ${
                selectedCreator?.id === c.id ? 'bg-surfaceLight/80' : 'hover:bg-surfaceLight/30'
              }`}
            >
              <div className="relative shrink-0">
                <div className="w-10 h-10 rounded-full bg-background border border-divider overflow-hidden">
                  <img src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${c.avatar}`} alt={c.name} className="w-full h-full" />
                </div>
                {c.online && (
                  <span className="absolute bottom-0 right-0 w-2.5 h-2.5 bg-neonEmerald rounded-full border-2 border-surface"></span>
                )}
              </div>

              <div className="flex-1 min-w-0">
                <div className="flex justify-between items-center mb-0.5">
                  <h4 className="text-xs font-bold text-white truncate">{c.name}</h4>
                  {c.unread > 0 && (
                    <span className="bg-accentBlue text-background text-[9px] font-black px-1.5 py-0.5 rounded-full">
                      {c.unread}
                    </span>
                  )}
                </div>
                <p className="text-[10px] text-textSecondary truncate">{c.lastMsg}</p>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Active Chat Viewport */}
      {selectedCreator ? (
        <div className="flex-1 flex flex-col justify-between bg-background">
          {/* Chat Header */}
          <div className="p-4 bg-surface border-b border-divider flex items-center justify-between">
            <div className="flex items-center gap-3">
              <button
                type="button"
                onClick={() => setSelectedCreator(null)}
                className="md:hidden text-textSecondary hover:text-white"
              >
                <ArrowLeft className="w-5 h-5" />
              </button>

              <div className="relative shrink-0">
                <div className="w-9 h-9 rounded-full overflow-hidden border border-divider">
                  <img src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${selectedCreator.avatar}`} alt={selectedCreator.name} className="w-full h-full" />
                </div>
                {selectedCreator.online && (
                  <span className="absolute bottom-0 right-0 w-2 h-2 bg-neonEmerald rounded-full border border-surface"></span>
                )}
              </div>

              <div>
                <h3 className="text-xs font-black text-white">{selectedCreator.name}</h3>
                <span className="text-[9px] text-neonEmerald font-bold uppercase tracking-wider">
                  {selectedCreator.online ? 'Online' : 'Offline'}
                </span>
              </div>
            </div>

            {/* Header actions */}
            <div className="flex gap-1">
              <button
                type="button"
                onClick={sendWorkspaceInvite}
                className="px-3 py-1.5 bg-surfaceLight border border-divider text-white hover:text-accentBlue rounded-xl text-[10px] font-black flex items-center gap-1.5 transition"
                title="Send Workspace Invite"
              >
                <FolderPlus className="w-3.5 h-3.5" /> INVITE TO CO-OP
              </button>
            </div>
          </div>

          {/* Messages Container */}
          <div className="flex-1 p-4 space-y-4 overflow-y-auto max-h-[50vh] md:max-h-none">
            {isLoading ? (
              <div className="text-center py-8">
                <div className="w-6 h-6 border-2 border-accentBlue border-t-transparent rounded-full animate-spin mx-auto mb-2"></div>
                <p className="text-[10px] text-textSecondary font-bold">DECRYPTING SECURE COMM CHANNELS...</p>
              </div>
            ) : (
              messages.map((msg) => {
                const isMe = msg.senderId === 'me';
                return (
                  <div key={msg.id} className={`flex ${isMe ? 'justify-end' : 'justify-start'}`}>
                    <div className={`max-w-[75%] rounded-2xl p-3 text-xs space-y-1 ${
                      isMe 
                        ? 'bg-accentBlue text-background rounded-tr-none' 
                        : 'bg-surface border border-divider text-white rounded-tl-none'
                    }`}>
                      <p className="leading-relaxed whitespace-pre-wrap">{msg.text}</p>
                      <div className={`flex justify-end items-center gap-1 text-[8px] ${
                        isMe ? 'text-background/70' : 'text-textMuted'
                      }`}>
                        <span>{formatTime(msg.createdAt)}</span>
                        {isMe && <CheckCheck className="w-3 h-3" />}
                      </div>
                    </div>
                  </div>
                );
              })
            )}
            <div ref={messageEndRef} />
          </div>

          {/* Form message composer */}
          <form onSubmit={handleSendMessage} className="p-4 bg-surface border-t border-divider flex gap-2">
            <input
              type="text"
              placeholder={`Send message to @${selectedCreator.username}...`}
              className="flex-grow bg-background border border-divider rounded-xl px-4 py-2.5 text-xs text-white placeholder-textMuted focus:outline-none focus:border-accentBlue"
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
            />
            <button
              type="submit"
              className="p-3 bg-accentBlue hover:bg-accentBlue/90 rounded-xl text-background transition"
            >
              <Send className="w-4 h-4" />
            </button>
          </form>
        </div>
      ) : (
        /* Empty/Placeholder state */
        <div className="flex-1 flex flex-col items-center justify-center p-8 bg-gradient-to-b from-background to-surface text-center">
          <div className="p-4 bg-surface rounded-full border border-divider text-textMuted mb-4">
            <MessageSquare className="w-8 h-8 animate-pulse" />
          </div>
          <h3 className="text-md font-black text-white uppercase tracking-wider">No Active Chat Selected</h3>
          <p className="text-xs text-textSecondary max-w-xs mt-1">
            Choose a creator from the co-op directory in the sidebar to sync storyboards, audio stems, or milestone agreements.
          </p>
        </div>
      )}
    </div>
  );
};
