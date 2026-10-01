import { supabase, isSupabaseConfigured } from './supabase';

export interface Workspace {
  id: string;
  name: string;
  description: string;
  createdBy: string;
  createdAt: number;
}

export interface ProductionTask {
  id: string;
  workspaceId: string;
  creatorId: string;
  title: string;
  kanbanLane: 'IDEA' | 'SCRIPT' | 'FILMING' | 'EDITING' | 'READY' | string;
  createdAt: number;
}

export interface DirectMessage {
  id: string;
  senderId: string;
  recipientId: string;
  text: string;
  createdAt: number;
}

export interface AuditLog {
  id: string;
  action: string;
  operator: string;
  timestamp: string;
}

export interface UserProfile {
  id: string;
  email: string;
  username: string;
  displayName: string;
  role: string;
}

// Memory-based cache fallback for local/offline mock databases
const getLocalData = <T>(key: string, defaults: T[]): T[] => {
  const data = localStorage.getItem(key);
  return data ? JSON.parse(data) : defaults;
};

const setLocalData = <T>(key: string, value: T[]): void => {
  localStorage.setItem(key, JSON.stringify(value));
};

// Initial default state setups for local offline fallback
const defaultWorkspaces: Workspace[] = [
  { id: 'ws1', name: 'Alpha Cinematic Universe', description: 'Production planning for sci-fi VFX series.', createdBy: 'Botla Veerendra', createdAt: Date.now() - 86400000 },
  { id: 'ws2', name: 'Synthesizer Sounds Co-Op', description: 'Audio presets and soundscapes.', createdBy: 'Macha Praveen', createdAt: Date.now() - 36000000 },
  { id: 'ws3', name: 'Founder Investor Pitch', description: 'Milestones and treasury split agreements.', createdBy: 'System', createdAt: Date.now() - 1200000 }
];

const defaultTasks: ProductionTask[] = [
  { id: 't1', workspaceId: 'ws1', creatorId: 'Botla Veerendra', title: 'Render Teaser visualizers inside Blender', kanbanLane: 'IDEA', createdAt: Date.now() - 100000 },
  { id: 't2', workspaceId: 'ws1', creatorId: 'Macha Praveen', title: 'Distribute mutual co-op agreements', kanbanLane: 'SCRIPT', createdAt: Date.now() - 50000 },
  { id: 't3', workspaceId: 'ws1', creatorId: 'Srinivas Rao', title: 'Compile high potential creator leads lists', kanbanLane: 'FILMING', createdAt: Date.now() - 20000 }
];

const defaultMessages: DirectMessage[] = [
  { id: 'm1', senderId: '1', recipientId: 'me', text: "Storyboards are looking incredible!", createdAt: Date.now() - 40000 },
  { id: 'm2', senderId: '2', recipientId: 'me', text: "VFX render is finishing up now.", createdAt: Date.now() - 20000 }
];

const defaultAuditLogs: AuditLog[] = [
  { id: '1', action: 'DATABASE INDEX INTEGRITY VERIFICATION', timestamp: new Date().toISOString(), operator: 'SRE System' },
  { id: '2', action: 'SQLITE ROOM LEDGER VACUUM COMPRESSION', timestamp: new Date().toISOString(), operator: 'Founder Botla' }
];

export const dbService = {
  // WORKSPACES CRUD
  async getWorkspaces(): Promise<Workspace[]> {
    if (isSupabaseConfigured) {
      try {
        const { data, error } = await supabase
          .from('workspaces')
          .select('*')
          .order('createdAt', { ascending: false });
        if (error) throw error;
        return (data || []).map(item => ({
          id: item.id,
          name: item.name,
          description: item.description || '',
          createdBy: item.createdBy || 'Unknown',
          createdAt: item.createdAt || Date.now()
        }));
      } catch (e) {
        console.error('Real-time Supabase Fetch error on workspaces:', e);
        return getLocalData('coop_local_workspaces', defaultWorkspaces);
      }
    } else {
      return getLocalData('coop_local_workspaces', defaultWorkspaces);
    }
  },

  async createWorkspace(name: string, description: string, creatorId: string): Promise<Workspace> {
    const newWorkspace: Workspace = {
      id: `ws_${Date.now()}`,
      name,
      description,
      createdBy: creatorId,
      createdAt: Date.now()
    };

    if (isSupabaseConfigured) {
      try {
        const { error } = await supabase.from('workspaces').insert({
          id: newWorkspace.id,
          name,
          description,
          createdBy: creatorId,
          createdAt: newWorkspace.createdAt
        });
        if (error) throw error;
      } catch (e) {
        console.error('Supabase Workspace Upload Failure:', e);
      }
    }

    const current = getLocalData('coop_local_workspaces', defaultWorkspaces);
    setLocalData('coop_local_workspaces', [newWorkspace, ...current]);
    return newWorkspace;
  },

  // TASKS CRUD
  async getTasks(workspaceId: string): Promise<ProductionTask[]> {
    if (isSupabaseConfigured) {
      try {
        const { data, error } = await supabase
          .from('production_tasks')
          .select('*')
          .eq('workspaceId', workspaceId);
        if (error) throw error;
        return (data || []).map(item => ({
          id: item.id,
          workspaceId: item.workspaceId,
          creatorId: item.creatorId,
          title: item.title,
          kanbanLane: item.kanbanLane,
          createdAt: item.createdAt || Date.now()
        }));
      } catch (e) {
        console.error('Real-time Supabase Fetch error on tasks:', e);
        return getLocalData('coop_local_tasks', defaultTasks).filter(t => t.workspaceId === workspaceId);
      }
    } else {
      return getLocalData('coop_local_tasks', defaultTasks).filter(t => t.workspaceId === workspaceId);
    }
  },

  async createTask(workspaceId: string, title: string, kanbanLane: string, creatorId: string): Promise<ProductionTask> {
    const newTask: ProductionTask = {
      id: `task_${Date.now()}`,
      workspaceId,
      creatorId,
      title,
      kanbanLane,
      createdAt: Date.now()
    };

    if (isSupabaseConfigured) {
      try {
        const { error } = await supabase.from('production_tasks').insert({
          id: newTask.id,
          workspaceId,
          creatorId,
          title,
          kanbanLane,
          createdAt: newTask.createdAt
        });
        if (error) throw error;
      } catch (e) {
        console.error('Supabase Task Upload Failure:', e);
      }
    }

    const current = getLocalData('coop_local_tasks', defaultTasks);
    setLocalData('coop_local_tasks', [...current, newTask]);
    return newTask;
  },

  async updateTaskLane(taskId: string, newLane: string): Promise<void> {
    if (isSupabaseConfigured) {
      try {
        const { error } = await supabase
          .from('production_tasks')
          .update({ kanbanLane: newLane })
          .eq('id', taskId);
        if (error) throw error;
      } catch (e) {
        console.error('Supabase Task Lane Update Failure:', e);
      }
    }

    const current = getLocalData('coop_local_tasks', defaultTasks);
    const updated = current.map(t => t.id === taskId ? { ...t, kanbanLane: newLane } : t);
    setLocalData('coop_local_tasks', updated);
  },

  async deleteTask(taskId: string): Promise<void> {
    if (isSupabaseConfigured) {
      try {
        const { error } = await supabase
          .from('production_tasks')
          .delete()
          .eq('id', taskId);
        if (error) throw error;
      } catch (e) {
        console.error('Supabase Task deletion error:', e);
      }
    }

    const current = getLocalData('coop_local_tasks', defaultTasks);
    setLocalData('coop_local_tasks', current.filter(t => t.id !== taskId));
  },

  // CHAT MESSAGES
  async getDirectMessages(recipientId: string): Promise<DirectMessage[]> {
    if (isSupabaseConfigured) {
      try {
        const { data, error } = await supabase
          .from('messages')
          .select('*')
          .eq('recipientId', recipientId)
          .order('createdAt', { ascending: true });
        if (error) throw error;
        return (data || []).map(item => ({
          id: item.id,
          senderId: item.senderId,
          recipientId: item.recipientId,
          text: item.text,
          createdAt: item.createdAt || Date.now()
        }));
      } catch (e) {
        console.error('Real-time Supabase Fetch error on messages:', e);
        return getLocalData('coop_local_messages', defaultMessages);
      }
    } else {
      return getLocalData('coop_local_messages', defaultMessages);
    }
  },

  async sendDirectMessage(senderId: string, recipientId: string, text: string): Promise<DirectMessage> {
    const newMsg: DirectMessage = {
      id: `msg_${Date.now()}`,
      senderId,
      recipientId,
      text,
      createdAt: Date.now()
    };

    if (isSupabaseConfigured) {
      try {
        const { error } = await supabase.from('messages').insert({
          id: newMsg.id,
          senderId,
          recipientId,
          text,
          createdAt: newMsg.createdAt
        });
        if (error) throw error;
      } catch (e) {
        console.error('Supabase Message Dispatch Failure:', e);
      }
    }

    const current = getLocalData('coop_local_messages', defaultMessages);
    setLocalData('coop_local_messages', [...current, newMsg]);
    return newMsg;
  },

  // AUDIT LOGS
  async getAuditLogs(): Promise<AuditLog[]> {
    if (isSupabaseConfigured) {
      try {
        const { data, error } = await supabase
          .from('admin_audit_logs')
          .select('*')
          .order('timestamp', { ascending: false });
        if (error) throw error;
        return (data || []).map(item => ({
          id: item.id,
          action: item.action,
          operator: item.operator || 'System',
          timestamp: item.timestamp || new Date().toISOString()
        }));
      } catch (e) {
        console.error('Supabase Audit Log fetch error:', e);
        return getLocalData('coop_local_audit_logs', defaultAuditLogs);
      }
    } else {
      return getLocalData('coop_local_audit_logs', defaultAuditLogs);
    }
  },

  async addAuditLog(action: string, operator: string): Promise<AuditLog> {
    const newLog: AuditLog = {
      id: `log_${Date.now()}`,
      action,
      operator,
      timestamp: new Date().toISOString()
    };

    if (isSupabaseConfigured) {
      try {
        const { error } = await supabase.from('admin_audit_logs').insert({
          id: newLog.id,
          action,
          operator,
          timestamp: newLog.timestamp
        });
        if (error) throw error;
      } catch (e) {
        console.error('Supabase Audit Log Upload Failure:', e);
      }
    }

    const current = getLocalData('coop_local_audit_logs', defaultAuditLogs);
    setLocalData('coop_local_audit_logs', [newLog, ...current]);
    return newLog;
  }
};
