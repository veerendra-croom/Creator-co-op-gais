-- ============================================================================
-- Creator Co-Op Database Schema Verification Tool (Supabase / Postgres 15+)
-- ============================================================================
-- Run this entire script in your Supabase SQL Editor to verify tables, views,
-- columns, Row Level Security (RLS) policies, triggers, and functional write paths.
--
-- This script contains:
--  1. Status tables check that validates physical table integrity.
--  2. View validation mapping checking alias mapping compliance with Room schemas.
--  3. Row level security status audits on all tables.
--  4. ACTIVE Policy details listing.
--  5. Active trigger mapping including "INSTEAD OF" triggers.
--  6. A complete transactional sandbox sequence verifying write path and compatibility.
--     (Auto-rolled back at the end so it leaves ZERO test traces/junk).
-- ============================================================================

SELECT 'START DIAGNOSTICS & VERIFICATION' AS "diagnostic_status";

-- ----------------------------------------------------------------------------
-- 1. PHYSICAL TABLE INTEGRITY VERIFICATION
-- ----------------------------------------------------------------------------
SELECT 
    table_name AS "table",
    is_insertable_into AS "is_insertable",
    (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'public' AND table_name = t.table_name) AS "column_count"
FROM information_schema.tables t
WHERE table_schema = 'public' 
  AND table_type = 'BASE TABLE'
  AND table_name IN (
    'users_table', 'forum_posts_table', 'forum_comments_table', 
    'syndicate_projects_table', 'team_agreements_table', 'agreement_acknowledgments_table',
    'workspace_members_table', 'production_tasks_table', 'messages_table', 
    'reports_table', 'admin_audit_logs_table', 'ad_placements_table', 'global_settings_table'
  )
ORDER BY table_name;


-- ----------------------------------------------------------------------------
-- 2. JETPACK COMPOSE MAPPED VIEW STATUS VERIFICATION
-- ----------------------------------------------------------------------------
SELECT 
    table_name AS "view_name",
    is_updatable AS "is_updatable"
FROM information_schema.views
WHERE table_schema = 'public'
  AND table_name IN ('user_profiles', 'workspaces', 'workspace_members', 'production_tasks', 'messages', 'posts', 'comments', 'users')
ORDER BY view_name;


-- ----------------------------------------------------------------------------
-- 3. ROW-LEVEL SECURITY (RLS) STATE VERIFICATION
-- ----------------------------------------------------------------------------
SELECT 
    tablename AS "table_rls",
    rowsecurity AS "rls_active"
FROM pg_tables
WHERE schemaname = 'public'
  AND tablename IN (
    'users_table', 'forum_posts_table', 'forum_comments_table', 
    'syndicate_projects_table', 'team_agreements_table', 'agreement_acknowledgments_table',
    'workspace_members_table', 'production_tasks_table', 'messages_table', 
    'reports_table', 'admin_audit_logs_table', 'ad_placements_table', 'global_settings_table'
  )
ORDER BY tablename;


-- ----------------------------------------------------------------------------
-- 4. CONFIGURED POLICIES ANALYSIS
-- ----------------------------------------------------------------------------
SELECT 
    tablename AS "policy_target_table",
    policyname AS "policy_name",
    permissive AS "is_permissive",
    roles AS "target_roles",
    cmd AS "secured_operation",
    qual AS "using_clause"
FROM pg_policies
WHERE schemaname = 'public'
ORDER BY tablename, policyname;


-- ----------------------------------------------------------------------------
-- 5. FUNCTION & TRIGGER ATTACHMENTS VERIFICATION
-- ----------------------------------------------------------------------------
SELECT 
    event_object_table AS "associated_with",
    trigger_name AS "trigger_identifier",
    action_timing AS "firing_timing",
    action_orientation AS "firing_scope",
    action_statement AS "function_invocation"
FROM information_schema.triggers
WHERE trigger_schema = 'public'
ORDER BY event_object_table, trigger_name;


-- ----------------------------------------------------------------------------
-- 6. TRANSACTIONAL WRITE PATHS VERIFICATION (AUTO-ROUNDS TO CLEANUP)
-- ----------------------------------------------------------------------------
BEGIN;

-- Preseed internal authentication accounts for the context of testing RLS operations
-- Note: Sub-transaction allows this to fail gracefully if the target authentication schema differs.
DO $$
BEGIN
    BEGIN
        EXECUTE 'INSERT INTO auth.users (id, instance_id, aud, role, email, encrypted_password, email_confirmed_at, raw_user_meta_data)
        VALUES 
            (''d0000000-0000-0000-0000-000000000001'', ''00000000-0000-0000-0000-000000000000'', ''authenticated'', ''authenticated'', ''diag_user_1@test.com'', ''$2y$10$7qXUj7.WIsj5'', now(), ''{}''),
            (''d0000000-0000-0000-0000-000000000002'', ''00000000-0000-0000-0000-000000000000'', ''authenticated'', ''authenticated'', ''diag_user_2@test.com'', ''$2y$10$7qXUj7.WIsj5'', now(), ''{}'')
        ON CONFLICT (id) DO NOTHING';
    EXCEPTION WHEN OTHERS THEN
        -- Safely ignore if auth.users schema cannot be written to in this context
    END;
END;
$$;

-- Initialize profiles on users_table directly so view constraints can reference them safely even if auth is omitted.
INSERT INTO users_table (user_id, email, display_name, system_role)
VALUES 
    ('d0000000-0000-0000-0000-000000000001', 'diag_user_1@test.com', 'Diag Master', 'REGISTERED_USER'),
    ('d0000000-0000-0000-0000-000000000002', 'diag_user_2@test.com', 'Diag Guest', 'REGISTERED_USER')
ON CONFLICT (user_id) DO UPDATE SET display_name = EXCLUDED.display_name;


-- Test 6.1: Upsert via "user_profiles" View (INSTEAD OF Trigger Verify)
INSERT INTO user_profiles (id, email, display_name, primary_specialty, avatar_url, system_role)
VALUES (
    'd0000000-0000-0000-0000-000000000001', 
    'diag_user_1_updated@test.com', 
    'Diag Master Updated', 
    'Lead Editor', 
    'https://example.com/diag_avatar.png', 
    'REGISTERED_USER'
);

-- Test 6.1b: Upsert via "users" View (INSTEAD OF Trigger Verify)
INSERT INTO users (id, email, display_name, primary_specialty, avatar_url, system_role)
VALUES (
    'd0000000-0000-0000-0000-000000000002', 
    'diag_user_2_updated@test.com', 
    'Diag Guest Updated', 
    'Lead Animator', 
    'https://example.com/diag_avatar2.png', 
    'REGISTERED_USER'
);


-- Test 6.2: Forum Post Insertion via View Trigger
INSERT INTO posts (id, "authorId", title, body, "spaceName")
VALUES (
    'd1000000-0000-0000-0000-000000000001',
    'd0000000-0000-0000-0000-000000000001',
    'Diagnostic Thread Verification',
    'Simulated discussion posting on Jetpack Compose mapped view elements.',
    'Editing'
);


-- Test 6.3: Forum Comment Insertion via View Trigger
INSERT INTO comments (id, "authorId", "postId", text)
VALUES (
    'd2000000-0000-0000-0000-000000000001',
    'd0000000-0000-0000-0000-000000000002',
    'd1000000-0000-0000-0000-000000000001',
    'Verified comments mapping functionality!'
);


-- Test 6.4: Workspace Project Insertion via View Trigger
INSERT INTO workspaces (id, name, created_by, is_archived)
VALUES (
    'd3000000-0000-0000-0000-000000000001',
    'Diagnostic Team Production Workspace',
    'd0000000-0000-0000-0000-000000000001',
    FALSE
);


-- Test 6.5: Workspace Members Insertion via View Trigger
INSERT INTO workspace_members (id, workspace_id, user_id, assigned_role_title, can_modify_production)
VALUES (
    'd4000000-0000-0000-0000-000000000001',
    'd3000000-0000-0000-0000-000000000001',
    'd0000000-0000-0000-0000-000000000001',
    'Lead Creator',
    TRUE
);


-- Test 6.6: Production Tasks Generation via View Trigger
INSERT INTO production_tasks (id, workspace_id, creator_id, title, content_body, state_scope, kanban_lane)
VALUES (
    'd5000000-0000-0000-0000-000000000001',
    'd3000000-0000-0000-0000-000000000001',
    'd0000000-0000-0000-0000-000000000001',
    'Record Video Intro',
    'Verifying Kotlin Compose entity task generation mappings.',
    'ROUGH_SANDBOX',
    'TODO'
);


-- Test 6.7: Instant Message Insertion via View Trigger
INSERT INTO messages (id, workspace_id, sender_id, message_body)
VALUES (
    'd6000000-0000-0000-0000-000000000001',
    'd3000000-0000-0000-0000-000000000001',
    'd0000000-0000-0000-0000-000000000001',
    'Verification message payload'
);


-- Output Result Matrix
SELECT 
    'auth.users seeds' AS step_tag,
    CASE WHEN EXISTS (
        SELECT 1 FROM auth.users 
        WHERE id IN ('d0000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000002')
    ) THEN 'SUCCESS' ELSE 'SKIPPED' END AS result_status,
    'Local auth users preseeded for testing RLS contexts.' AS details

UNION ALL

SELECT 
    'Preseed Base Profiles' AS step_tag,
    CASE WHEN EXISTS (
        SELECT 1 FROM users_table 
        WHERE user_id = 'd0000000-0000-0000-0000-000000000001' 
          AND display_name = 'Diag Master'
    ) THEN 'SUCCESS' ELSE 'FAILED' END AS result_status,
    'Base user records registered successfully on physical users_table.' AS details

UNION ALL

SELECT 
    'upsert_user_profiles_view' AS step_tag,
    CASE WHEN EXISTS (
        SELECT 1 FROM users_table 
        WHERE user_id = 'd0000000-0000-0000-0000-000000000001' 
          AND display_name = 'Diag Master Updated' 
          AND email = 'diag_user_1_updated@test.com'
          AND avatar_url = 'https://example.com/diag_avatar.png'
    ) THEN 'SUCCESS' ELSE 'FAILED' END AS result_status,
    'UPSERT write route to users_table via instead-of trigger verified.' AS details

UNION ALL

SELECT 
    'upsert_users_view' AS step_tag,
    CASE WHEN EXISTS (
        SELECT 1 FROM users_table 
        WHERE user_id = 'd0000000-0000-0000-0000-000000000002' 
          AND display_name = 'Diag Guest Updated' 
          AND email = 'diag_user_2_updated@test.com'
          AND avatar_url = 'https://example.com/diag_avatar2.png'
    ) THEN 'SUCCESS' ELSE 'FAILED' END AS result_status,
    'UPSERT write route to users_table via users view trigger verified.' AS details

UNION ALL

SELECT 
    'forum_posts_view_trigger' AS step_tag,
    CASE WHEN EXISTS (
        SELECT 1 FROM forum_posts_table 
        WHERE post_id = 'd1000000-0000-0000-0000-000000000001' 
          AND space_category = 'Editing'
    ) THEN 'SUCCESS' ELSE 'FAILED' END AS result_status,
    'Post mapped successfully from view and saved in forum_posts_table.' AS details

UNION ALL

SELECT 
    'forum_comments_view_trigger' AS step_tag,
    CASE WHEN EXISTS (
        SELECT 1 FROM forum_comments_table 
        WHERE comment_id = 'd2000000-0000-0000-0000-000000000001' 
          AND content = 'Verified comments mapping functionality!'
    ) THEN 'SUCCESS' ELSE 'FAILED' END AS result_status,
    'Comment propagated successfully from comments view into forum_comments_table.' AS details

UNION ALL

SELECT 
    'workspaces_view_trigger' AS step_tag,
    CASE WHEN EXISTS (
        SELECT 1 FROM syndicate_projects_table 
        WHERE project_id = 'd3000000-0000-0000-0000-000000000001' 
          AND channel_name = 'Diagnostic Team Production Workspace'
    ) THEN 'SUCCESS' ELSE 'FAILED' END AS result_status,
    'Workspace mapped smoothly into syndicate_projects_table.' AS details

UNION ALL

SELECT 
    'workspace_members_view_trigger' AS step_tag,
    CASE WHEN EXISTS (
        SELECT 1 FROM workspace_members_table 
        WHERE member_id = 'd4000000-0000-0000-0000-000000000001' 
          AND assigned_role_title = 'Lead Creator'
    ) THEN 'SUCCESS' ELSE 'FAILED' END AS result_status,
    'Member saved correctly in workspace_members_table.' AS details

UNION ALL

SELECT 
    'production_tasks_view_trigger' AS step_tag,
    CASE WHEN EXISTS (
        SELECT 1 FROM production_tasks_table 
        WHERE task_id = 'd5000000-0000-0000-0000-000000000001' 
          AND state_scope = 'ROUGH_SANDBOX'
    ) THEN 'SUCCESS' ELSE 'FAILED' END AS result_status,
    'Task inserted correctly into production_tasks_table.' AS details

UNION ALL

SELECT 
    'messages_view_trigger' AS step_tag,
    CASE WHEN EXISTS (
        SELECT 1 FROM messages_table 
        WHERE message_id = 'd6000000-0000-0000-0000-000000000001' 
          AND message_body = 'Verification message payload'
    ) THEN 'SUCCESS' ELSE 'FAILED' END AS result_status,
    'Message saved successfully in messages_table.' AS details;

-- Ensure no test traces persist in the environment database
ROLLBACK;

SELECT 'DIAGNOSTIC TEST COMPLETED - ALL WRITES SAFELY CLEANED' AS "test_result";
