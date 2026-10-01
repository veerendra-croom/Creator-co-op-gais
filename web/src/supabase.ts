import { createClient } from '@supabase/supabase-js';

const supabaseUrl = (import.meta as any).env?.VITE_SUPABASE_URL || 'https://your-project.supabase.co';
const supabaseKey = (import.meta as any).env?.VITE_SUPABASE_KEY || 'your-supabase-public-anon-key';

export const isSupabaseConfigured = 
  supabaseUrl && 
  !supabaseUrl.includes('your-project') && 
  supabaseKey && 
  !supabaseKey.includes('your-supabase-public');

// Initialize Supabase client
export const supabase = createClient(supabaseUrl, supabaseKey);
