-- Creator Co-Op Master Database Schema (Supabase / PostgreSQL 15+)
-- This script initializes the core tables and Row-Level Security (RLS) policies.

-- 1. Create Extensions & Schema Safely
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Clean up existing database objects to ensure fresh installation of modified tables and prevent alter type / column errors.
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN (
        SELECT table_name 
        FROM information_schema.views 
        WHERE table_schema = 'public' 
          AND table_name IN ('user_profiles', 'workspaces', 'workspace_members', 'production_tasks', 'messages', 'posts', 'comments', 'endorsements', 'users')
    ) LOOP
        EXECUTE 'DROP VIEW IF EXISTS ' || quote_ident(r.table_name) || ' CASCADE;';
    END LOOP;

    FOR r IN (
        SELECT table_name 
        FROM information_schema.tables 
        WHERE table_schema = 'public' 
          AND table_type = 'BASE TABLE'
          AND table_name IN ('user_profiles', 'workspaces', 'workspace_members', 'production_tasks', 'messages', 'posts', 'comments', 'endorsements_table', 'users')
    ) LOOP
        EXECUTE 'DROP TABLE IF EXISTS ' || quote_ident(r.table_name) || ' CASCADE;';
    END LOOP;
END;
$$;
DROP TABLE IF EXISTS global_settings_table CASCADE;
DROP TABLE IF EXISTS ad_placements_table CASCADE;
DROP TABLE IF EXISTS admin_audit_logs_table CASCADE;
DROP TABLE IF EXISTS reports_table CASCADE;
DROP TABLE IF EXISTS messages_table CASCADE;
DROP TABLE IF EXISTS production_tasks_table CASCADE;
DROP TABLE IF EXISTS workspace_members_table CASCADE;
DROP TABLE IF EXISTS agreement_acknowledgments_table CASCADE;
DROP TABLE IF EXISTS team_agreements_table CASCADE;
DROP TABLE IF EXISTS syndicate_projects_table CASCADE;
DROP TABLE IF EXISTS forum_comments_table CASCADE;
DROP TABLE IF EXISTS forum_posts_table CASCADE;
DROP TABLE IF EXISTS endorsements_table CASCADE;
DROP TABLE IF EXISTS users_table CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- 2. Users Table
CREATE TABLE users_table (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email VARCHAR(255) UNIQUE NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    system_role VARCHAR(20) NOT NULL CHECK (system_role IN ('PUBLIC_USER', 'REGISTERED_USER', 'PLATFORM_ADMIN', 'SUSPENDED')),
    portfolio_tags TEXT[], -- Array of strings e.g. ['Video Editor', 'Scriptwriter']
    avatar_url TEXT,
    bio TEXT DEFAULT '',
    website_url TEXT DEFAULT '',
    skills_json JSONB DEFAULT '[]',
    portfolio_json JSONB DEFAULT '[]',
    social_links_json JSONB DEFAULT '{}',
    availability_status VARCHAR(20) DEFAULT 'OPEN_TO_PROJECTS' CHECK (availability_status IN ('OPEN_TO_PROJECTS', 'NOT_AVAILABLE')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Pre-seed test users into auth.users (Supabase managed table) and create profiles
DO $$
BEGIN
    BEGIN
        INSERT INTO auth.users (
            id,
            instance_id,
            aud,
            role,
            email,
            encrypted_password,
            email_confirmed_at,
            raw_app_meta_data,
            raw_user_meta_data,
            created_at,
            updated_at
        ) VALUES (
            '00000000-0000-0000-0000-00000000000a',
            '00000000-0000-0000-0000-000000000000',
            'authenticated',
            'authenticated',
            'test_user_a@example.com',
            '$2a$10$7qXUj7.WIsj5qI7XNq8K9e.hI0/86pCgRkZbeCInS1U818K/YkXmG',
            now(),
            '{"provider": "email", "providers": ["email"]}',
            '{}',
            now(),
            now()
        ),
        (
            '00000000-0000-0000-0000-00000000000b',
            '00000000-0000-0000-0000-000000000000',
            'authenticated',
            'authenticated',
            'test_user_b@example.com',
            '$2a$10$7qXUj7.WIsj5qI7XNq8K9e.hI0/86pCgRkZbeCInS1U818K/YkXmG',
            now(),
            '{"provider": "email", "providers": ["email"]}',
            '{}',
            now(),
            now()
        ) ON CONFLICT (id) DO NOTHING;
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Could not pre-seed auth.users: %', SQLERRM;
    END;

    -- Pre-seed profiles for test users if and only if they exist in auth.users
    BEGIN
        IF EXISTS (SELECT 1 FROM auth.users WHERE id = '00000000-0000-0000-0000-00000000000a') THEN
            INSERT INTO users_table (user_id, email, display_name, system_role)
            VALUES ('00000000-0000-0000-0000-00000000000a', 'test_user_a@example.com', 'Test User A', 'REGISTERED_USER')
            ON CONFLICT (user_id) DO NOTHING;
        END IF;

        IF EXISTS (SELECT 1 FROM auth.users WHERE id = '00000000-0000-0000-0000-00000000000b') THEN
            INSERT INTO users_table (user_id, email, display_name, system_role)
            VALUES ('00000000-0000-0000-0000-00000000000b', 'test_user_b@example.com', 'Test User B', 'REGISTERED_USER')
            ON CONFLICT (user_id) DO NOTHING;
        END IF;
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Could not pre-seed users_table profiles: %', SQLERRM;
    END;
END;
$$;

-- 3. Forum Posts Table (The Square)
CREATE TABLE forum_posts_table (
    post_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    author_id UUID REFERENCES users_table(user_id),
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    space_category VARCHAR(50), -- e.g. 'Editing', 'Strategy', 'Showcase'
    upvote_count INTEGER DEFAULT 0,
    downvote_count INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE forum_comments_table (
    comment_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    post_id UUID REFERENCES forum_posts_table(post_id) ON DELETE CASCADE,
    author_id UUID REFERENCES users_table(user_id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. Syndicate Projects Table
CREATE TABLE IF NOT EXISTS syndicate_projects_table (
    project_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    manager_id UUID REFERENCES users_table(user_id) ON DELETE CASCADE,
    channel_name VARCHAR(100) NOT NULL,
    niche VARCHAR(50),
    style_description TEXT,
    required_roles JSONB, -- Array of strings or requirement objects
    status VARCHAR(20) DEFAULT 'MATCHMAKING',
    is_archived BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. Team Agreements Table (Non-financial mutual understanding)
CREATE TABLE IF NOT EXISTS team_agreements_table (
    agreement_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID REFERENCES syndicate_projects_table(project_id) ON DELETE CASCADE,
    version INTEGER DEFAULT 1,
    content_text TEXT NOT NULL,
    is_locked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. Agreement Acknowledgments
CREATE TABLE IF NOT EXISTS agreement_acknowledgments_table (
    acknowledgment_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    agreement_id UUID REFERENCES team_agreements_table(agreement_id) ON DELETE CASCADE,
    user_id UUID REFERENCES users_table(user_id) ON DELETE CASCADE,
    acknowledgment_hash TEXT NOT NULL, -- SHA-256 of agreement text
    acknowledged_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(agreement_id, user_id)
);

-- 7. Workspace Members
CREATE TABLE IF NOT EXISTS workspace_members_table (
    member_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workspace_id UUID REFERENCES syndicate_projects_table(project_id) ON DELETE CASCADE,
    user_id UUID REFERENCES users_table(user_id) ON DELETE CASCADE,
    assigned_role_title VARCHAR(100) DEFAULT 'Creator',
    can_modify_production BOOLEAN DEFAULT FALSE,
    UNIQUE(workspace_id, user_id)
);

-- 8. Production Tasks
CREATE TABLE IF NOT EXISTS production_tasks_table (
    task_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workspace_id UUID REFERENCES syndicate_projects_table(project_id) ON DELETE CASCADE,
    creator_id UUID REFERENCES users_table(user_id),
    title VARCHAR(255) NOT NULL,
    content_body TEXT NOT NULL,
    media_preview_url TEXT,
    state_scope VARCHAR(50) DEFAULT 'ROUGH_SANDBOX', -- ROUGH_SANDBOX or PRODUCTION_READY
    kanban_lane VARCHAR(50) DEFAULT 'TODO',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 9. Messages
CREATE TABLE IF NOT EXISTS messages_table (
    message_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workspace_id UUID REFERENCES syndicate_projects_table(project_id) ON DELETE CASCADE,
    sender_id UUID REFERENCES users_table(user_id),
    message_body TEXT NOT NULL,
    attachment_json_meta JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 10. Reports Table
CREATE TABLE IF NOT EXISTS reports_table (
    report_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    reporter_id UUID REFERENCES users_table(user_id),
    target_type VARCHAR(50) NOT NULL, -- 'POST', 'COMMENT', 'MESSAGE', 'USER'
    target_id UUID NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING', -- 'PENDING', 'RESOLVED', 'DISMISSED'
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 11. Admin Audit Logs
CREATE TABLE IF NOT EXISTS admin_audit_logs_table (
    log_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    admin_id UUID REFERENCES users_table(user_id),
    action_taken VARCHAR(100) NOT NULL, -- 'SUSPEND_USER', 'DELETE_POST', 'DISMISS_REPORT', etc.
    target_type VARCHAR(50) NOT NULL,
    target_id UUID NOT NULL,
    reason TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 12. Ad Placements Table
CREATE TABLE IF NOT EXISTS ad_placements_table (
    slot_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    slot_name VARCHAR(100) NOT NULL,
    screen_location VARCHAR(100) NOT NULL,
    ad_type VARCHAR(20) NOT NULL, -- 'BANNER', 'INTERSTITIAL'
    is_enabled BOOLEAN DEFAULT TRUE,
    allowed_locations TEXT[], -- Array of valid locations
    last_modified_by_admin_id UUID REFERENCES users_table(user_id),
    last_modified_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 12.5 endorsements_table
CREATE TABLE IF NOT EXISTS endorsements_table (
    giver_id UUID REFERENCES users_table(user_id) ON DELETE CASCADE,
    receiver_id UUID REFERENCES users_table(user_id) ON DELETE CASCADE,
    workspace_id UUID REFERENCES syndicate_projects_table(project_id) ON DELETE CASCADE,
    tags TEXT[] NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (giver_id, receiver_id, workspace_id)
);

-- 13. Global Settings Table
CREATE TABLE IF NOT EXISTS global_settings_table (
    setting_key VARCHAR(50) PRIMARY KEY,
    setting_value VARCHAR(50) NOT NULL -- 'AUTOMATIC', 'MANAGED', 'OFF'
);

-- 14. Saved Searches Table
CREATE TABLE IF NOT EXISTS saved_searches_table (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users_table(user_id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    filter_json TEXT NOT NULL DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_notified_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 15. Looking for Work Table
CREATE TABLE IF NOT EXISTS looking_for_work_table (
    user_id UUID PRIMARY KEY REFERENCES users_table(user_id) ON DELETE CASCADE,
    details_json TEXT NOT NULL DEFAULT '{}',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- --- SECURITY LAYER: ROW-LEVEL SECURITY (RLS) ---

-- Enable RLS on all tables
ALTER TABLE users_table ENABLE ROW LEVEL SECURITY;
ALTER TABLE endorsements_table ENABLE ROW LEVEL SECURITY;
ALTER TABLE saved_searches_table ENABLE ROW LEVEL SECURITY;
ALTER TABLE looking_for_work_table ENABLE ROW LEVEL SECURITY;
ALTER TABLE forum_posts_table ENABLE ROW LEVEL SECURITY;
ALTER TABLE forum_comments_table ENABLE ROW LEVEL SECURITY;
ALTER TABLE syndicate_projects_table ENABLE ROW LEVEL SECURITY;
ALTER TABLE team_agreements_table ENABLE ROW LEVEL SECURITY;
ALTER TABLE agreement_acknowledgments_table ENABLE ROW LEVEL SECURITY;
ALTER TABLE workspace_members_table ENABLE ROW LEVEL SECURITY;
ALTER TABLE production_tasks_table ENABLE ROW LEVEL SECURITY;
ALTER TABLE messages_table ENABLE ROW LEVEL SECURITY;
ALTER TABLE reports_table ENABLE ROW LEVEL SECURITY;
ALTER TABLE admin_audit_logs_table ENABLE ROW LEVEL SECURITY;
ALTER TABLE ad_placements_table ENABLE ROW LEVEL SECURITY;
ALTER TABLE global_settings_table ENABLE ROW LEVEL SECURITY;

-- Trigger for cleaning up private sandbox tasks when a member is removed
CREATE OR REPLACE FUNCTION delete_private_sandbox_tasks() RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM production_tasks_table 
    WHERE workspace_id = OLD.workspace_id 
    AND creator_id = OLD.user_id 
    AND state_scope = 'ROUGH_SANDBOX';
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS on_member_removed ON workspace_members_table;
CREATE TRIGGER on_member_removed
AFTER DELETE ON workspace_members_table
FOR EACH ROW EXECUTE FUNCTION delete_private_sandbox_tasks();

-- HELPER FUNCTIONS FOR PERMISSIONS

CREATE OR REPLACE FUNCTION is_not_suspended() RETURNS BOOLEAN AS $$
BEGIN
  RETURN EXISTS (
    SELECT 1 FROM users_table 
    WHERE user_id = auth.uid() 
    AND system_role != 'SUSPENDED'
  );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION is_workspace_member(ws_id UUID) RETURNS BOOLEAN AS $$
BEGIN
  RETURN EXISTS (
    SELECT 1 FROM workspace_members_table 
    WHERE workspace_id = ws_id AND user_id = auth.uid()
  );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION can_modify_prod(ws_id UUID) RETURNS BOOLEAN AS $$
BEGIN
  RETURN EXISTS (
    SELECT 1 FROM workspace_members_table 
    WHERE workspace_id = ws_id AND user_id = auth.uid()
    AND (can_modify_production = TRUE OR assigned_role_title IN ('Lead Creator', 'Head'))
  );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- POLICIES FOR users_table
DROP POLICY IF EXISTS select_own_full_profile ON users_table;
CREATE POLICY select_own_full_profile ON users_table
FOR SELECT USING (auth.uid() = user_id);

DROP POLICY IF EXISTS select_public_profiles ON users_table;
CREATE POLICY select_public_profiles ON users_table
FOR SELECT USING (true);

DROP POLICY IF EXISTS insert_own_profile ON users_table;
CREATE POLICY insert_own_profile ON users_table
FOR INSERT WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS update_own_profile ON users_table;
CREATE POLICY update_own_profile ON users_table
FOR UPDATE USING (auth.uid() = user_id)
WITH CHECK (
    -- Prevent users from changing their own roles to escalate privileges
    (SELECT system_role FROM users_table WHERE user_id = auth.uid()) = system_role
);

-- POLICIES FOR forum_posts_table
DROP POLICY IF EXISTS select_all_posts ON forum_posts_table;
CREATE POLICY select_all_posts ON forum_posts_table
FOR SELECT USING (true);

DROP POLICY IF EXISTS insert_post ON forum_posts_table;
CREATE POLICY insert_post ON forum_posts_table
FOR INSERT WITH CHECK (is_not_suspended() AND auth.uid() = author_id);

DROP POLICY IF EXISTS manage_own_posts ON forum_posts_table;
CREATE POLICY manage_own_posts ON forum_posts_table
FOR ALL USING (is_not_suspended() AND auth.uid() = author_id);

-- POLICIES FOR forum_comments_table
DROP POLICY IF EXISTS select_all_comments ON forum_comments_table;
CREATE POLICY select_all_comments ON forum_comments_table
FOR SELECT USING (true);

DROP POLICY IF EXISTS insert_comment ON forum_comments_table;
CREATE POLICY insert_comment ON forum_comments_table
FOR INSERT WITH CHECK (is_not_suspended() AND auth.uid() = author_id);

DROP POLICY IF EXISTS manage_own_comments ON forum_comments_table;
CREATE POLICY manage_own_comments ON forum_comments_table
FOR ALL USING (is_not_suspended() AND auth.uid() = author_id);

-- POLICIES FOR syndicate_projects_table (Workspaces/Proposals)
DROP POLICY IF EXISTS select_all_workspaces ON syndicate_projects_table;
CREATE POLICY select_all_workspaces ON syndicate_projects_table
FOR SELECT USING (true);

DROP POLICY IF EXISTS create_workspace ON syndicate_projects_table;
CREATE POLICY create_workspace ON syndicate_projects_table
FOR INSERT WITH CHECK (is_not_suspended() AND auth.uid() = manager_id);

DROP POLICY IF EXISTS manage_own_workspaces ON syndicate_projects_table;
CREATE POLICY manage_own_workspaces ON syndicate_projects_table
FOR ALL USING (is_not_suspended() AND auth.uid() = manager_id);

-- POLICIES FOR production_tasks_table

-- 1. SELECT
DROP POLICY IF EXISTS select_sandbox_tasks ON production_tasks_table;
CREATE POLICY select_sandbox_tasks ON production_tasks_table
FOR SELECT USING (
    state_scope = 'ROUGH_SANDBOX' AND creator_id = auth.uid()
);

DROP POLICY IF EXISTS select_team_tasks ON production_tasks_table;
CREATE POLICY select_team_tasks ON production_tasks_table
FOR SELECT USING (
    state_scope != 'ROUGH_SANDBOX' AND is_workspace_member(workspace_id)
);

-- 2. INSERT
DROP POLICY IF EXISTS insert_sandbox_task ON production_tasks_table;
CREATE POLICY insert_sandbox_task ON production_tasks_table
FOR INSERT WITH CHECK (
    is_not_suspended() AND
    state_scope = 'ROUGH_SANDBOX' AND
    creator_id = auth.uid() AND
    is_workspace_member(workspace_id)
);

DROP POLICY IF EXISTS insert_team_task ON production_tasks_table;
CREATE POLICY insert_team_task ON production_tasks_table
FOR INSERT WITH CHECK (
    is_not_suspended() AND
    state_scope != 'ROUGH_SANDBOX' AND
    is_workspace_member(workspace_id) AND
    can_modify_prod(workspace_id) AND
    creator_id = auth.uid()
);

-- 3. UPDATE
DROP POLICY IF EXISTS update_sandbox_task ON production_tasks_table;
CREATE POLICY update_sandbox_task ON production_tasks_table
FOR UPDATE USING (
    state_scope = 'ROUGH_SANDBOX' AND
    creator_id = auth.uid()
);

DROP POLICY IF EXISTS update_team_task ON production_tasks_table;
CREATE POLICY update_team_task ON production_tasks_table
FOR UPDATE USING (
    is_not_suspended() AND
    state_scope != 'ROUGH_SANDBOX' AND
    is_workspace_member(workspace_id) AND
    can_modify_prod(workspace_id)
);

-- 4. DELETE
DROP POLICY IF EXISTS delete_sandbox_task ON production_tasks_table;
CREATE POLICY delete_sandbox_task ON production_tasks_table
FOR DELETE USING (
    state_scope = 'ROUGH_SANDBOX' AND
    creator_id = auth.uid()
);

DROP POLICY IF EXISTS delete_team_task ON production_tasks_table;
CREATE POLICY delete_team_task ON production_tasks_table
FOR DELETE USING (
    is_not_suspended() AND
    state_scope != 'ROUGH_SANDBOX' AND
    (
        auth.uid() = creator_id OR 
        EXISTS (
            SELECT 1 FROM workspace_members_table 
            WHERE workspace_id = production_tasks_table.workspace_id 
            AND user_id = auth.uid() 
            AND assigned_role_title IN ('Lead Creator', 'Head')
        )
    )
);

-- POLICIES FOR messages_table
DROP POLICY IF EXISTS select_workspace_messages ON messages_table;
CREATE POLICY select_workspace_messages ON messages_table
FOR SELECT USING (is_workspace_member(workspace_id));

DROP POLICY IF EXISTS insert_message ON messages_table;
CREATE POLICY insert_message ON messages_table
FOR INSERT WITH CHECK (is_not_suspended() AND is_workspace_member(workspace_id) AND sender_id = auth.uid());

-- POLICIES FOR reports_table
DROP POLICY IF EXISTS insert_report ON reports_table;
CREATE POLICY insert_report ON reports_table
FOR INSERT WITH CHECK (is_not_suspended() AND reporter_id = auth.uid());

DROP POLICY IF EXISTS select_all_reports ON reports_table;
CREATE POLICY select_all_reports ON reports_table
FOR SELECT USING (
    EXISTS (
        SELECT 1 FROM users_table 
        WHERE user_id = auth.uid() AND system_role = 'PLATFORM_ADMIN'
    )
);

-- POLICIES FOR admin_audit_logs_table
DROP POLICY IF EXISTS select_all_logs ON admin_audit_logs_table;
CREATE POLICY select_all_logs ON admin_audit_logs_table
FOR SELECT USING (
    EXISTS (
        SELECT 1 FROM users_table 
        WHERE user_id = auth.uid() AND system_role = 'PLATFORM_ADMIN'
    )
);

DROP POLICY IF EXISTS insert_log ON admin_audit_logs_table;
CREATE POLICY insert_log ON admin_audit_logs_table
FOR INSERT WITH CHECK (
    EXISTS (
        SELECT 1 FROM users_table 
        WHERE user_id = auth.uid() AND system_role = 'PLATFORM_ADMIN'
    ) AND admin_id = auth.uid()
);

-- HELPER FOR LEAVING WORKSPACE
CREATE OR REPLACE FUNCTION can_leave_workspace(ws_id UUID, leaving_user_id UUID) RETURNS BOOLEAN AS $$
DECLARE
    total_leads INT;
    total_members INT;
    is_leaving_lead BOOLEAN;
BEGIN
    SELECT COUNT(*) INTO total_leads FROM workspace_members_table 
    WHERE workspace_id = ws_id AND assigned_role_title IN ('Lead Creator', 'Head');

    SELECT COUNT(*) INTO total_members FROM workspace_members_table 
    WHERE workspace_id = ws_id;

    SELECT EXISTS (
        SELECT 1 FROM workspace_members_table 
        WHERE workspace_id = ws_id AND user_id = leaving_user_id AND assigned_role_title IN ('Lead Creator', 'Head')
    ) INTO is_leaving_lead;

    IF is_leaving_lead AND total_leads = 1 AND total_members > 1 THEN
        RETURN FALSE;
    END IF;

    RETURN TRUE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- POLICIES FOR workspace_members_table
DROP POLICY IF EXISTS select_workspace_members ON workspace_members_table;
CREATE POLICY select_workspace_members ON workspace_members_table
FOR SELECT USING (is_workspace_member(workspace_id));

DROP POLICY IF EXISTS insert_workspace_member ON workspace_members_table;
CREATE POLICY insert_workspace_member ON workspace_members_table
FOR INSERT WITH CHECK (
    auth.uid() = user_id 
    OR 
    EXISTS (
        SELECT 1 FROM syndicate_projects_table 
        WHERE project_id = workspace_id AND manager_id = auth.uid()
    )
    OR
    EXISTS (
        SELECT 1 FROM workspace_members_table wmt
        WHERE wmt.workspace_id = workspace_members_table.workspace_id 
        AND wmt.user_id = auth.uid() 
        AND wmt.assigned_role_title IN ('Lead Creator', 'Head')
    )
);

DROP POLICY IF EXISTS update_workspace_member ON workspace_members_table;
CREATE POLICY update_workspace_member ON workspace_members_table
FOR UPDATE USING (
    EXISTS (
        SELECT 1 FROM workspace_members_table wmt
        WHERE wmt.workspace_id = workspace_members_table.workspace_id 
        AND wmt.user_id = auth.uid() 
        AND wmt.assigned_role_title IN ('Lead Creator', 'Head')
    )
);

DROP POLICY IF EXISTS delete_workspace_member ON workspace_members_table;
CREATE POLICY delete_workspace_member ON workspace_members_table
FOR DELETE USING (
    (
        auth.uid() != user_id AND 
        EXISTS (
            SELECT 1 FROM workspace_members_table wmt
            WHERE wmt.workspace_id = workspace_members_table.workspace_id 
            AND wmt.user_id = auth.uid() 
            AND wmt.assigned_role_title IN ('Lead Creator', 'Head')
        )
    )
    OR
    (
        auth.uid() = user_id AND can_leave_workspace(workspace_id, auth.uid())
    )
);

-- POLICIES FOR team_agreements_table
DROP POLICY IF EXISTS select_team_agreement ON team_agreements_table;
CREATE POLICY select_team_agreement ON team_agreements_table
FOR SELECT USING (is_workspace_member(project_id));

DROP POLICY IF EXISTS insert_team_agreement ON team_agreements_table;
CREATE POLICY insert_team_agreement ON team_agreements_table
FOR INSERT WITH CHECK (
    EXISTS (
        SELECT 1 FROM workspace_members_table 
        WHERE workspace_id = project_id 
        AND user_id = auth.uid() 
        AND assigned_role_title IN ('Lead Creator', 'Head')
    )
);

DROP POLICY IF EXISTS update_team_agreement ON team_agreements_table;
CREATE POLICY update_team_agreement ON team_agreements_table
FOR UPDATE USING (
    is_locked = FALSE 
    AND 
    EXISTS (
        SELECT 1 FROM workspace_members_table 
        WHERE workspace_id = project_id 
        AND user_id = auth.uid() 
        AND assigned_role_title IN ('Lead Creator', 'Head')
    )
) WITH CHECK (
    EXISTS (
        SELECT 1 FROM workspace_members_table 
        WHERE workspace_id = project_id 
        AND user_id = auth.uid() 
        AND assigned_role_title IN ('Lead Creator', 'Head')
    )   
);

-- POLICIES FOR agreement_acknowledgments_table
DROP POLICY IF EXISTS select_agreement_ack ON agreement_acknowledgments_table;
CREATE POLICY select_agreement_ack ON agreement_acknowledgments_table
FOR SELECT USING (
    EXISTS (
        SELECT 1 FROM team_agreements_table 
        WHERE agreement_id = agreement_acknowledgments_table.agreement_id 
        AND is_workspace_member(project_id)
    )
);

DROP POLICY IF EXISTS insert_agreement_ack ON agreement_acknowledgments_table;
CREATE POLICY insert_agreement_ack ON agreement_acknowledgments_table
FOR INSERT WITH CHECK (
    auth.uid() = user_id
);

-- --- POLICIES FOR endorsements_table ---
DROP POLICY IF EXISTS select_all_endorsements ON endorsements_table;
CREATE POLICY select_all_endorsements ON endorsements_table
FOR SELECT USING (true);

DROP POLICY IF EXISTS insert_endorsement ON endorsements_table;
CREATE POLICY insert_endorsement ON endorsements_table
FOR INSERT WITH CHECK (
    is_not_suspended() AND
    auth.uid() = giver_id AND
    giver_id != receiver_id AND
    EXISTS (
        SELECT 1 FROM workspace_members_table
        WHERE workspace_id = endorsements_table.workspace_id AND user_id = auth.uid()
    ) AND
    EXISTS (
        SELECT 1 FROM workspace_members_table
        WHERE workspace_id = endorsements_table.workspace_id AND user_id = endorsements_table.receiver_id
    ) AND
    EXISTS (
        SELECT 1 FROM syndicate_projects_table
        WHERE project_id = endorsements_table.workspace_id AND is_archived = TRUE
    )
);

-- AD MANAGEMENT POLICIES
DROP POLICY IF EXISTS select_ad_placements ON ad_placements_table;
CREATE POLICY select_ad_placements ON ad_placements_table
FOR SELECT USING (true);

DROP POLICY IF EXISTS manage_ad_placements ON ad_placements_table;
CREATE POLICY manage_ad_placements ON ad_placements_table
FOR ALL USING (
    EXISTS (
        SELECT 1 FROM users_table 
        WHERE user_id = auth.uid() AND system_role = 'PLATFORM_ADMIN'
    )
);

DROP POLICY IF EXISTS select_global_settings ON global_settings_table;
CREATE POLICY select_global_settings ON global_settings_table
FOR SELECT USING (true);

DROP POLICY IF EXISTS manage_global_settings ON global_settings_table;
CREATE POLICY manage_global_settings ON global_settings_table
FOR ALL USING (
    EXISTS (
        SELECT 1 FROM users_table 
        WHERE user_id = auth.uid() AND system_role = 'PLATFORM_ADMIN'
    )
);

-- SAVED SEARCHES POLICIES
DROP POLICY IF EXISTS manage_own_saved_searches ON saved_searches_table;
CREATE POLICY manage_own_saved_searches ON saved_searches_table
FOR ALL USING (
    is_not_suspended() AND auth.uid() = user_id
);

-- LOOKING FOR WORK POLICIES
DROP POLICY IF EXISTS select_all_looking_for_work ON looking_for_work_table;
CREATE POLICY select_all_looking_for_work ON looking_for_work_table
FOR SELECT USING (true);

DROP POLICY IF EXISTS manage_own_looking_for_work ON looking_for_work_table;
CREATE POLICY manage_own_looking_for_work ON looking_for_work_table
FOR ALL USING (
    is_not_suspended() AND auth.uid() = user_id
);


-- --- APPLICATION ALIAS VIEWS & INSTEAD OF TRIGGERS ---
-- Avoid mismatches between raw SQL table structures and target Jetpack Compose model mappings.

-- 1. user_profiles
CREATE OR REPLACE VIEW user_profiles AS
SELECT 
    user_id AS id,
    email,
    display_name,
    avatar_url,
    'APP_USER'::TEXT AS global_role,
    COALESCE(portfolio_tags[1], '') AS primary_specialty,
    false AS is_verified_pro,
    system_role,
    bio,
    website_url,
    skills_json::TEXT AS skills_json,
    portfolio_json::TEXT AS portfolio_json,
    social_links_json::TEXT AS social_links_json,
    availability_status
FROM users_table;

CREATE OR REPLACE FUNCTION insert_into_user_profiles_func() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO users_table (user_id, email, display_name, system_role, avatar_url, portfolio_tags, bio, website_url, skills_json, portfolio_json, social_links_json, availability_status)
    VALUES (
        NEW.id,
        NEW.email,
        NEW.display_name,
        COALESCE(NEW.system_role, 'REGISTERED_USER'),
        NEW.avatar_url,
        CASE WHEN NEW.primary_specialty IS NOT NULL AND NEW.primary_specialty != '' THEN ARRAY[NEW.primary_specialty] ELSE '{}'::TEXT[] END,
        COALESCE(NEW.bio, ''),
        COALESCE(NEW.website_url, ''),
        COALESCE(NEW.skills_json::JSONB, '[]'::JSONB),
        COALESCE(NEW.portfolio_json::JSONB, '[]'::JSONB),
        COALESCE(NEW.social_links_json::JSONB, '{}'::JSONB),
        COALESCE(NEW.availability_status, 'OPEN_TO_PROJECTS')
    ) ON CONFLICT (user_id) DO UPDATE SET
        email = EXCLUDED.email,
        display_name = EXCLUDED.display_name,
        system_role = EXCLUDED.system_role,
        avatar_url = EXCLUDED.avatar_url,
        portfolio_tags = EXCLUDED.portfolio_tags,
        bio = EXCLUDED.bio,
        website_url = EXCLUDED.website_url,
        skills_json = EXCLUDED.skills_json,
        portfolio_json = EXCLUDED.portfolio_json,
        social_links_json = EXCLUDED.social_links_json,
        availability_status = EXCLUDED.availability_status;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS insert_into_user_profiles_trg ON user_profiles;
CREATE TRIGGER insert_into_user_profiles_trg
INSTEAD OF INSERT OR UPDATE ON user_profiles
FOR EACH ROW EXECUTE FUNCTION insert_into_user_profiles_func();


-- 1b. users (Alias View mapping for AppRepository users queries)
CREATE OR REPLACE VIEW users AS
SELECT 
    user_id AS id,
    email,
    display_name,
    avatar_url,
    'APP_USER'::TEXT AS global_role,
    COALESCE(portfolio_tags[1], '') AS primary_specialty,
    false AS is_verified_pro,
    system_role,
    bio,
    website_url,
    skills_json::TEXT AS skills_json,
    portfolio_json::TEXT AS portfolio_json,
    social_links_json::TEXT AS social_links_json,
    availability_status
FROM users_table;

CREATE OR REPLACE FUNCTION insert_into_users_func() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO users_table (user_id, email, display_name, system_role, avatar_url, portfolio_tags, bio, website_url, skills_json, portfolio_json, social_links_json, availability_status)
    VALUES (
        NEW.id,
        NEW.email,
        NEW.display_name,
        COALESCE(NEW.system_role, 'REGISTERED_USER'),
        NEW.avatar_url,
        CASE WHEN NEW.primary_specialty IS NOT NULL AND NEW.primary_specialty != '' THEN ARRAY[NEW.primary_specialty] ELSE '{}'::TEXT[] END,
        COALESCE(NEW.bio, ''),
        COALESCE(NEW.website_url, ''),
        COALESCE(NEW.skills_json::JSONB, '[]'::JSONB),
        COALESCE(NEW.portfolio_json::JSONB, '[]'::JSONB),
        COALESCE(NEW.social_links_json::JSONB, '{}'::JSONB),
        COALESCE(NEW.availability_status, 'OPEN_TO_PROJECTS')
    ) ON CONFLICT (user_id) DO UPDATE SET
        email = EXCLUDED.email,
        display_name = EXCLUDED.display_name,
        system_role = EXCLUDED.system_role,
        avatar_url = EXCLUDED.avatar_url,
        portfolio_tags = EXCLUDED.portfolio_tags,
        bio = EXCLUDED.bio,
        website_url = EXCLUDED.website_url,
        skills_json = EXCLUDED.skills_json,
        portfolio_json = EXCLUDED.portfolio_json,
        social_links_json = EXCLUDED.social_links_json,
        availability_status = EXCLUDED.availability_status;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS insert_into_users_trg ON users;
CREATE TRIGGER insert_into_users_trg
INSTEAD OF INSERT OR UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION insert_into_users_func();


-- 2. workspaces
CREATE OR REPLACE VIEW workspaces AS
SELECT
    project_id AS id,
    channel_name AS name,
    'YOUTUBE'::TEXT AS platform_type,
    manager_id AS created_by,
    is_archived,
    EXTRACT(EPOCH FROM created_at)::BIGINT * 1000 AS created_at
FROM syndicate_projects_table;

CREATE OR REPLACE FUNCTION insert_into_workspaces_func() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO syndicate_projects_table (project_id, manager_id, channel_name, status, is_archived)
    VALUES (
        COALESCE(NEW.id, uuid_generate_v4()),
        COALESCE(NEW.created_by, auth.uid()),
        NEW.name,
        'MATCHMAKING',
        COALESCE(NEW.is_archived, FALSE)
    ) ON CONFLICT (project_id) DO UPDATE SET
        channel_name = EXCLUDED.channel_name,
        is_archived = EXCLUDED.is_archived;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS insert_into_workspaces_trg ON workspaces;
CREATE TRIGGER insert_into_workspaces_trg
INSTEAD OF INSERT OR UPDATE ON workspaces
FOR EACH ROW EXECUTE FUNCTION insert_into_workspaces_func();


-- 3. workspace_members
CREATE OR REPLACE VIEW workspace_members AS
SELECT
    member_id AS id,
    workspace_id,
    user_id,
    assigned_role_title,
    can_modify_production
FROM workspace_members_table;

CREATE OR REPLACE FUNCTION insert_into_workspace_members_func() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO workspace_members_table (member_id, workspace_id, user_id, assigned_role_title, can_modify_production)
    VALUES (
        COALESCE(NEW.id, uuid_generate_v4()),
        NEW.workspace_id,
        NEW.user_id,
        NEW.assigned_role_title,
        NEW.can_modify_production
    ) ON CONFLICT (member_id) DO UPDATE SET
        assigned_role_title = EXCLUDED.assigned_role_title,
        can_modify_production = EXCLUDED.can_modify_production;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS insert_into_workspace_members_trg ON workspace_members;
CREATE TRIGGER insert_into_workspace_members_trg
INSTEAD OF INSERT OR UPDATE ON workspace_members
FOR EACH ROW EXECUTE FUNCTION insert_into_workspace_members_func();


-- 4. production_tasks
CREATE OR REPLACE VIEW production_tasks AS
SELECT
    task_id AS id,
    workspace_id,
    creator_id,
    title,
    content_body,
    media_preview_url,
    state_scope,
    kanban_lane,
    EXTRACT(EPOCH FROM created_at)::BIGINT * 1000 AS created_at
FROM production_tasks_table;

CREATE OR REPLACE FUNCTION insert_into_production_tasks_func() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO production_tasks_table (task_id, workspace_id, creator_id, title, content_body, media_preview_url, state_scope, kanban_lane)
    VALUES (
        COALESCE(NEW.id, uuid_generate_v4()),
        NEW.workspace_id,
        NEW.creator_id,
        NEW.title,
        NEW.content_body,
        NEW.media_preview_url,
        NEW.state_scope,
        NEW.kanban_lane
    ) ON CONFLICT (task_id) DO UPDATE SET
        title = EXCLUDED.title,
        content_body = EXCLUDED.content_body,
        media_preview_url = EXCLUDED.media_preview_url,
        state_scope = EXCLUDED.state_scope,
        kanban_lane = EXCLUDED.kanban_lane;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS insert_into_production_tasks_trg ON production_tasks;
CREATE TRIGGER insert_into_production_tasks_trg
INSTEAD OF INSERT OR UPDATE ON production_tasks
FOR EACH ROW EXECUTE FUNCTION insert_into_production_tasks_func();


-- 5. messages
CREATE OR REPLACE VIEW messages AS
SELECT
    m.message_id AS id,
    m.workspace_id,
    m.sender_id,
    NULL::UUID AS recipient_id,
    COALESCE(u.display_name, 'Unknown') AS sender_name,
    COALESCE(u.system_role, 'REGISTERED_USER') AS sender_role,
    m.message_body,
    m.attachment_json_meta,
    EXTRACT(EPOCH FROM m.created_at)::BIGINT * 1000 AS timestamp
FROM messages_table m
LEFT JOIN users_table u ON m.sender_id = u.user_id;

CREATE OR REPLACE FUNCTION insert_into_messages_func() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO messages_table (message_id, workspace_id, sender_id, message_body, attachment_json_meta)
    VALUES (
        COALESCE(NEW.id, uuid_generate_v4()),
        NEW.workspace_id,
        NEW.sender_id,
        NEW.message_body,
        NEW.attachment_json_meta
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS insert_into_messages_trg ON messages;
CREATE TRIGGER insert_into_messages_trg
INSTEAD OF INSERT ON messages
FOR EACH ROW EXECUTE FUNCTION insert_into_messages_func();


-- 6. posts
CREATE OR REPLACE VIEW posts AS
SELECT
    p.post_id AS id,
    p.author_id AS "authorId",
    p.title,
    COALESCE(u.display_name, 'Anonymous') AS "authorName",
    COALESCE(u.system_role, 'PUBLIC_USER') AS "authorRole",
    COALESCE(u.avatar_url, '') AS "authorAvatarUrl",
    p.content AS body,
    COALESCE(p.space_category, 'General') AS "spaceName",
    EXTRACT(EPOCH FROM p.created_at)::BIGINT * 1000 AS timestamp,
    p.upvote_count AS upvotes,
    p.downvote_count AS downvotes,
    0 AS "commentCount",
    'none'::TEXT AS "userVote",
    ''::TEXT AS "mediaUrl"
FROM forum_posts_table p
LEFT JOIN users_table u ON p.author_id = u.user_id;

CREATE OR REPLACE FUNCTION insert_into_posts_func() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO forum_posts_table (post_id, author_id, title, content, space_category, upvote_count, downvote_count)
    VALUES (
        COALESCE(NEW.id, uuid_generate_v4()),
        NEW."authorId",
        NEW.title,
        NEW.body,
        NEW."spaceName",
        COALESCE(NEW.upvotes, 0),
        COALESCE(NEW.downvotes, 0)
    ) ON CONFLICT (post_id) DO UPDATE SET
        title = EXCLUDED.title,
        content = EXCLUDED.content,
        space_category = EXCLUDED.space_category;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS insert_into_posts_trg ON posts;
CREATE TRIGGER insert_into_posts_trg
INSTEAD OF INSERT OR UPDATE ON posts
FOR EACH ROW EXECUTE FUNCTION insert_into_posts_func();


-- 7. comments
CREATE OR REPLACE VIEW comments AS
SELECT
    c.comment_id AS id,
    c.author_id AS "authorId",
    c.post_id AS "postId",
    COALESCE(u.display_name, 'Anonymous') AS "authorName",
    COALESCE(u.system_role, 'REGISTERED_USER') AS "authorRole",
    c.content AS text,
    EXTRACT(EPOCH FROM c.created_at)::BIGINT * 1000 AS timestamp,
    EXTRACT(EPOCH FROM c.created_at)::BIGINT * 1000 AS "timestampMs"
FROM forum_comments_table c
LEFT JOIN users_table u ON c.author_id = u.user_id;

CREATE OR REPLACE FUNCTION insert_into_comments_func() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO forum_comments_table (comment_id, post_id, author_id, content)
    VALUES (
        COALESCE(NEW.id, uuid_generate_v4()),
        NEW."postId",
        NEW."authorId",
        NEW.text
    ) ON CONFLICT (comment_id) DO UPDATE SET
        content = EXCLUDED.content;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS insert_into_comments_trg ON comments;
CREATE TRIGGER insert_into_comments_trg
INSTEAD OF INSERT OR UPDATE ON comments
FOR EACH ROW EXECUTE FUNCTION insert_into_comments_func();


-- 8. endorsements view mapping
CREATE OR REPLACE VIEW endorsements AS
SELECT
    giver_id,
    receiver_id,
    workspace_id,
    tags,
    created_at
FROM endorsements_table;

CREATE OR REPLACE FUNCTION insert_into_endorsements_func() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO endorsements_table (giver_id, receiver_id, workspace_id, tags, created_at)
    VALUES (
        NEW.giver_id,
        NEW.receiver_id,
        NEW.workspace_id,
        NEW.tags,
        COALESCE(NEW.created_at, CURRENT_TIMESTAMP)
    ) ON CONFLICT (giver_id, receiver_id, workspace_id) DO UPDATE SET
        tags = EXCLUDED.tags,
        created_at = EXCLUDED.created_at;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS insert_into_endorsements_trg ON endorsements;
CREATE TRIGGER insert_into_endorsements_trg
INSTEAD OF INSERT OR UPDATE ON endorsements
FOR EACH ROW EXECUTE FUNCTION insert_into_endorsements_func();




