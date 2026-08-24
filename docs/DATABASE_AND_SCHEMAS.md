# Creator Co-Op: Database Schemas & Routing Specification

---

## 1. Room SQLite & Supabase PostgreSQL Schemas

### 1.1 `user_profiles`
```sql
CREATE TABLE user_profiles (
    id TEXT PRIMARY KEY NOT NULL,
    email TEXT NOT NULL,
    full_name TEXT NOT NULL,
    bio TEXT NOT NULL DEFAULT '',
    avatar_url TEXT NOT NULL DEFAULT '',
    creator_tier TEXT NOT NULL DEFAULT 'CREATOR',
    primary_skills TEXT NOT NULL DEFAULT '',
    is_verified INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL
);
```

### 1.2 `workspaces` & `workspace_members`
```sql
CREATE TABLE workspaces (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    created_by TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    cover_image_url TEXT NOT NULL DEFAULT '',
    is_archived INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE workspace_members (
    id TEXT PRIMARY KEY NOT NULL,
    workspace_id TEXT NOT NULL,
    user_id TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'MEMBER', -- OWNER, ADMIN, MEMBER, VIEWER
    joined_at INTEGER NOT NULL,
    FOREIGN KEY(workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE
);
```

### 1.3 `production_tasks` (Kanban Pipeline)
```sql
CREATE TABLE production_tasks (
    id TEXT PRIMARY KEY NOT NULL,
    workspace_id TEXT NOT NULL,
    title TEXT NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    status TEXT NOT NULL DEFAULT 'TODO', -- TODO, IN_PROGRESS, IN_REVIEW, DONE
    priority TEXT NOT NULL DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, URGENT
    assigned_to_user_id TEXT,
    due_date INTEGER,
    created_at INTEGER NOT NULL,
    order_index INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY(workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE
);
```

### 1.4 `team_agreements` & `agreement_acknowledgments`
```sql
CREATE TABLE team_agreements (
    id TEXT PRIMARY KEY NOT NULL,
    workspace_id TEXT NOT NULL,
    title TEXT NOT NULL,
    content_text TEXT NOT NULL,
    content_hash TEXT NOT NULL, -- SHA-256 Digest of canonical content_text
    version INTEGER NOT NULL DEFAULT 1,
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    created_by TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    FOREIGN KEY(workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE
);

CREATE TABLE agreement_acknowledgments (
    id TEXT PRIMARY KEY NOT NULL,
    agreement_id TEXT NOT NULL,
    user_id TEXT NOT NULL,
    signature_hash TEXT NOT NULL,
    acknowledged_at INTEGER NOT NULL,
    FOREIGN KEY(agreement_id) REFERENCES team_agreements(id) ON DELETE CASCADE
);
```

### 1.5 `syndicate_pitches`
```sql
CREATE TABLE syndicate_pitches (
    id TEXT PRIMARY KEY NOT NULL,
    creator_id TEXT NOT NULL,
    project_title TEXT NOT NULL,
    pitch_deck_summary TEXT NOT NULL,
    target_budget REAL NOT NULL DEFAULT 0.0,
    equity_split_percent REAL NOT NULL DEFAULT 0.0,
    required_roles TEXT NOT NULL, -- Serialized JSON array of skills/roles
    status TEXT NOT NULL DEFAULT 'OPEN', -- OPEN, MATCHED, FUNDED, CLOSED
    created_at INTEGER NOT NULL
);
```

---

## 2. Dynamic Deep Linking Routing Table

The application registers verified intent filters in `AndroidManifest.xml` with `android:autoVerify="true"` for `https://creator-studio.app` alongside custom scheme fallback `creatorstudio://`.

| URL Pattern | Scheme Variant | Destination Screen | View / Navigation Action |
| :--- | :--- | :--- | :--- |
| `https://creator-studio.app/u/{userId}` | `creatorstudio://user/{userId}` | Public Creator Profile | `navigateToPublicProfile(userId)` |
| `https://creator-studio.app/pitch/{pitchId}` | `creatorstudio://pitch/{pitchId}` | Syndicate Pitch Hub | `navigateToTab("SYNDICATE")` |
| `https://creator-studio.app/role/{roleId}` | `creatorstudio://role/{roleId}` | Co-Op Open Roles | `navigateToTab("SYNDICATE")` |
| `https://creator-studio.app/workspace/{id}` | `creatorstudio://workspace/{id}` | Workspace Hub & Kanban | `navigateToWorkspaceSettings(id)` |
| `https://creator-studio.app/project/{id}` | `creatorstudio://project/{id}` | Portfolio Detail View | `navigateToPortfolioDetail(id)` |
| `https://creator-studio.app/invite/{code}` | `creatorstudio://invite/{code}` | Co-Op Onboarding Wizard | `navigateToTab("WORKSPACES")` |
