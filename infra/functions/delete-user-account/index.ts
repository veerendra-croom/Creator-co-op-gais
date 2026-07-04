import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2"

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
  'Access-Control-Allow-Methods': 'POST, OPTIONS',
}

serve(async (req) => {
  // Handle CORS preflight requests
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  try {
    // 1. Get client authentication token from the headers to verify identity
    const authHeader = req.headers.get('Authorization')
    if (!authHeader) {
      return new Response(
        JSON.stringify({ error: 'Missing Authorization header' }),
        { status: 401, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
      )
    }

    // Initialize Supabase Client with target Environment Secrets (Service Role Token is mandatory)
    const supabaseUrl = Deno.env.get('SUPABASE_URL') ?? ""
    const supabaseServiceKey = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? ""

    if (!supabaseUrl || !supabaseServiceKey) {
      console.error("Missing critical environment configurations (SUPABASE_URL or SUPABASE_SERVICE_ROLE_KEY)")
      return new Response(
        JSON.stringify({ error: 'Internal Server Configuration Error' }),
        { status: 500, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
      )
    }

    // Initialize the Admin-authenticated Supabase client
    const supabaseAdmin = createClient(supabaseUrl, supabaseServiceKey)

    // 2. Validate the requester's JWT using Supabase Auth
    const token = authHeader.replace('Bearer ', '')
    const { data: { user }, error: authError } = await supabaseAdmin.auth.getUser(token)

    if (authError || !user) {
      return new Response(
        JSON.stringify({ error: 'Unauthorized or invalid token: ' + (authError?.message ?? 'unknown') }),
        { status: 401, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
      )
    }

    // Parse the body payload containing target userId to delete (or default to current verified user)
    const body = await req.json().catch(() => ({}))
    const targetUserId = body.userId || user.id

    // Protection: Users should only be allowed to delete their own account
    // (Except if system administrative triggers are verified, but for standard cascade, enforce matching ID)
    if (targetUserId !== user.id) {
       return new Response(
         JSON.stringify({ error: 'Forbidden: You cannot delete another user\'s authentication record' }),
         { status: 403, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
       )
    }

    console.log(`Processing cascade account deletion for authenticated user ID: ${targetUserId}`)

    // 3. Delete user account from Supabase Auth (invokes background PostgreSQL triggers if defined, other profiles)
    const { error: deleteError } = await supabaseAdmin.auth.admin.deleteUser(targetUserId)

    if (deleteError) {
      console.error(`Failed to delete Auth record for user ${targetUserId}: ${deleteError.message}`)
      return new Response(
        JSON.stringify({ error: 'Supabase Auth admin deletion failed: ' + deleteError.message }),
        { status: 500, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
      )
    }

    console.log(`Successfully deleted auth instance for User ID: ${targetUserId}`)

    return new Response(
      JSON.stringify({ success: true, message: `Auth record for user ${targetUserId} permanently deleted.` }),
      { status: 200, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
    )

  } catch (err: any) {
    console.error("Unhandled exception in delete-user-account function:", err)
    return new Response(
      JSON.stringify({ error: err.message || 'Unknown server error' }),
      { status: 500, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
    )
  }
})
