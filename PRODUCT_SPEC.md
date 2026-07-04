# TechPulse Co-Op: Product Specification (V1 Pivot)

## 1. Core Vision
A collaborative ecosystem specifically designed for content creators (YouTube, Instagram, TikTok) that integrates the community discovery of **Reddit**, the professional networking of **LinkedIn**, and the high-speed execution of **Slack**. 

**Primary Goal**: To become the "Daily Home" for creator teams where actual work happens, eliminating the need to bounce between WhatsApp, Slack, and Google Drive.

---

## 2. User & Admin Personas

### A. App User (The Creator/Collaborator)
*   **Permissions**: Full access to joined workspaces. Ability to create "Rough Work" premises for any project they are part of.
*   **Workflow**: 
    1. Discover teams/projects in **The Square**.
    2. Swipe for matches in **Syndicate**.
    3. Execute in **Workspaces**.
*   **Pain Points Solved**: No more fragmented communication; clear distinction between "Experimenting" and "Final Deliverables."

### B. Group Head (Project Owner)
*   **Permissions**: A special state of a standard user who initiated a project. Can set granular permissions for team members within a specific Workspace.
*   **Role**: Final arbiter for "Production Ready" moves.

### C. System Admin (Platform Manager)
*   **Permissions**: Global oversight.
*   **Features**:
    *   **User Management**: Monitor growth and verify identities.
    *   **Workspace Analytics**: See which niches are trending.
    *   **Content Moderation**: Moderate "The Square" (Reddit-style forums).
    *   **System Integrity**: View security exceptions and sandbox health.

---

## 3. UI/UX Hierarchy (Hybrid Design)

### I. The Square (Reddit/LinkedIn Style)
*   **Feed**: Niche-specific sub-communities for creators.
*   **Interactions**: Upvotes, threaded comments, and "Project Pitch" posts.
*   **UI Style**: High-density cards with rich media support.

### II. Syndicate (Discovery)
*   **Tinder-style matching**: Find editors, writers, or hosts for specific "Workspaces."
*   **Algorithm**: Matching based on "Skill Compatibility" rather than equity percentages.

### III. The Hub: Unified Workspace
*   **State 1: Rough Premise**: An individual "sandbox" for every user within a workspace. No one else can see this until it's "Pitch-Ready."
*   **State 2: Production Stream**: The shared team environment.
*   **Kanban 2.0**: Drag-and-drop tasks from "Rough" to "Production."
*   **Unified Chat**: Sidebar with individual DMs and group-specific channels.
*   **Zero-Cost Vault**: A "Team Agreement" logger where users acknowledge mutual understandings of intent instead of complex legal/financial contracts.

---

## 4. Technical Roadmap (Supabase + Gemini)
*   **Authentication**: Supabase Email Confirmation (Mandatory for V1).
*   **Data Layout**:
    *   `workspaces`: The root project entities.
    *   `workspace_chats`: Real-time messaging.
    *   `workspace_tasks`: Categorized into personal (rough) or shared (production).
*   **AI Integration**: Gemini 1.5 Flash used to summarize "Rough Work" notes into "Production Ready" briefs.
