



CREATOR CO-OP ECOSYSTEM
APP_FLOW.md
Navigation Architecture & User Flow Document
Version 1.0 | June 2026





1. Entry Points
▸	Fresh install → Splash Screen → Onboarding
▸	Returning user (token valid) → Skip to last active tab (default: Tab 1 — The Square)
▸	Returning user (token expired) → Login Screen
▸	Deep link from push notification → Direct to relevant screen after auth check
▸	Google Play Store install referral → Splash with referral code captured


2. Page Inventory (All Screens)
Route	Screen Name	Auth Required
/splash	Splash / Animated Logo Screen	No
/onboarding/role	Role Selection Screen	No
/onboarding/portfolio	Portfolio Setup Screen	No
/onboarding/payout	Stripe Express Setup (Optional)	No
/auth/login	Login Screen (Apple/Google/Phone)	No
/auth/signup	Sign Up Screen	No
/auth/otp	OTP Verification Screen	No
/square	The Square — Main Feed (Tab 1)	Yes
/square/post/:id	Post Detail + Comments Thread	Yes
/square/create	Create Post Modal	Yes
/square/spaces	Spaces Directory	Yes
/square/spaces/:slug	Individual Space Feed	Yes
/square/media-feedback/:id	Media Feedback Tool (Video Player)	Yes
/syndicate	Syndicate Board — Main Screen (Tab 2)	Yes
/syndicate/projects	Project Feed (Find Projects mode)	Yes
/syndicate/projects/:id	Project Deep-Dive Screen	Yes
/syndicate/projects/:id/pitch	Submit Pitch Screen	Yes
/syndicate/talent	Talent Directory (Find Talent mode)	Yes
/syndicate/swiper	Swiper UI (Fullscreen Card)	Yes
/syndicate/create-project	Create Project Posting Form	Yes
/workspaces	Workspace List Screen (Tab 3)	Yes
/workspaces/:id	Active Workspace Dashboard	Yes
/workspaces/:id/chat/:channel	Team Chat Channel	Yes
/workspaces/:id/assets	Asset Pipeline Grid	Yes
/workspaces/:id/contract	Contract Vault (Read-Only)	Yes
/workspaces/:id/huddle	Voice/Video Huddle Room	Yes
/workspaces/:id/contract/sign	Contract Signing Screen	Yes
/profile	Profile + Wallet Dashboard (Tab 4)	Yes
/profile/edit	Edit Profile Screen	Yes
/profile/:userId	Public Profile View	Yes
/wallet/withdraw	Withdrawal Flow Screen	Yes
/wallet/history	Full Payout History Ledger	Yes
/settings	Settings Screen	Yes
/settings/notifications	Notification Preferences	Yes
/settings/security	Security & Password	Yes
/error/404	Not Found Screen	No
/error/500	Server Error Screen	No


3. User Flows — Step by Step
3.1 New User Onboarding Flow
Step	Screen / Action	Trigger	Next State
1	Splash Screen (2s animated logo)	App launch	Auto-advance
2	Login / Sign Up choice screen	Splash complete	User taps 'Sign Up'
3	Sign Up: Enter phone or Google/Apple SSO	Tap Sign Up	Submit credentials
4	OTP Verification Screen (phone only)	Phone sign up	Enter 6-digit OTP
5	Role Selection Screen	Auth success	Select role(s) + tap Next
6	Portfolio Setup Screen	Role selected	Enter links + years exp + tap Next
7	Stripe Express Setup (optional, skippable)	Portfolio saved	Link bank or tap 'Do Later'
8	Land on The Square (Tab 1)	Onboarding complete	Persistent bottom nav visible

3.2 Channel Manager — Full Journey
Step	Screen / Action	Trigger	Next State
1	Login → The Square	App open	Browse feed
2	Tap Tab 2 → Syndicate Board	Bottom nav tap	Toggle to 'Find Talent' mode
3	Tap 'Post a Project' FAB	Tap FAB	Open Create Project form
4	Fill project form + set equity pie chart (must = 100%)	Form open	Tap 'Publish Project'
5	Project appears in Syndicate Board feed	Publish success	Await pitches
6	Notification: New pitch received	Pitcher submits	Tap notification → Project page
7	Review pitch (portfolio, message, samples)	Open notification	Tap 'Accept' or 'Decline'
8	Accept → Contract Wizard opens	Tap Accept	Fill contract terms + jurisdiction
9	Smart contract generated and sent to talent for signing	Wizard complete	Await signature
10	All parties sign → Workspace auto-created and unlocked	All signed	Redirect to Workspace
11	Use Workspace: chat, upload assets, timeline feedback	Ongoing	Production phase
12	YouTube payout hits → Auto-split executed → Wallet updated	Payout trigger	All members notified

3.3 Freelancer / Talent — Full Journey
Step	Screen / Action	Trigger	Next State
1	Login → The Square → Build karma via posts	App open	Explore community
2	Tab 2 → Syndicate Board → 'Find Projects' mode	Nav tap	Browse project cards
3	Use filters (niche, equity %, language)	Filter icon	Filtered list updates
4	Open Swiper UI → Swipe right on match	Tap Swiper	Card deck loads
5	Open Project Deep-Dive → Tap 'Submit Pitch'	Tap card	Pitch form opens
6	Write pitch message, attach portfolio samples → Submit	Form filled	Pitch delivered to manager
7	Notification: Pitch accepted → Contract arrives	Manager accepts	Open contract signing screen
8	Review contract terms → Sign digitally	Contract screen	DocuSign signature flow
9	Workspace unlocked → Join team chat, upload work	All signed	Enter Workspace
10	Payout day → Split arrives in Wallet	Auto-trigger	View earnings, withdraw


4. Conditional Flows
Not logged in → any protected route	Redirect to /auth/login. After login, deep-link to original destination.
Onboarding incomplete	Forced back to onboarding step. Cannot access main tabs until role + portfolio steps complete.
Stripe Express not set up on payout day	Funds held in escrow. Push notification: 'Set up payout account to receive your earnings'. Wallet shows pending balance.
Contract not signed by all parties	Workspace locked. Chat visible but sends placeholder: 'Awaiting signatures'. Upload/asset features disabled.
Pitch submitted to own project	Blocked. Toast error: 'You cannot pitch to your own project.'
Network offline	Show offline banner. Queue actions (upvotes, post drafts) locally. Sync on reconnect.
Session token expired mid-session	Silent token refresh attempted. If fails, redirect to /auth/login with toast: 'Session expired, please log in again.'


5. State Transitions
5.1 Project Lifecycle
DRAFT	Manager is filling the project form. Not visible to others.
PUBLISHED	Project live on Syndicate Board. Accepting pitches.
NEGOTIATING	At least one pitch accepted. Contract wizard in progress.
CONTRACTED	All parties signed. Workspace unlocked. Production active.
PAUSED	Manager or Admin pauses project (e.g., dispute).
COMPLETED	Project marked done by manager. Revenue split finalized. Workspace archived.

5.2 Payout Lifecycle
PENDING	Revenue hit escrow. Awaiting split calculation.
PROCESSING	Split triggered. Routing to individual Stripe Express accounts.
DISTRIBUTED	All individual accounts credited. Ledger updated.
FAILED	Routing error. Funds held. Retry initiated.
DISPUTED	Member flags discrepancy. Admin review triggered.


6. Forbidden Flows
These Flows Must NEVER Occur
✗  A user can never access Workspace chat/assets before all parties have signed the contract.
✗  A Channel Manager can never edit equity percentages after the contract is signed.
✗  A freelancer can never submit more than 3 pitches to the same project.
✗  Platform can never allow manual alteration of split percentages post-signing.
✗  Payout funds can never route directly to personal bank — must pass through Stripe Connect escrow.
✗  Karma score can never decrease below zero.
✗  A user can never view another user's private wallet balance or payout history.

---

## 7. Closed Beta Implementation & Structural Gap Directory

To align design expectation with execution reality, all application features are audited and mapped based on their actual state of implementation.

### 🔍 7.1: Frontend-Only / Simulated Modules (Category 1 Gaps)
The following elements exist visually in the frontend codebase but are simulated locally in memory or with UI timers (they do not connect to live backend REST/WebSocket servers or external third-party SDKs):
1. **Peer-to-Peer Audio/Video conferencing (`VideoHuddleScreen.kt`)**: Uses local state fluctuating multipliers to simulate speaking grids instead of an active WebRTC engine.
2. **Google Play Billing Sandbox (`PremiumSubscriptionScreen.kt`)**: Mimics subscription SKU purchases via `BillingSimulatorDialog`.
3. **Stripe Express Wallet Connection (`DashboardScreen.kt`)**: Displays loading cycles and simulated triggers to mock Stripe linkings.
4. **Platform System Telemetry Visualizers (`PlatformHealthScreen.kt`)**: Simulates CPU and memory logs on custom Compose `Canvas` layouts.
5. **VFX Rendering Progress Monitor (`ContentPipelineScreen.kt`)**: Delays linear render animations via simple UI timers.
6. **SMS Contact Inviter (`ReferFriendDialog.kt`)**: Launches standard local SMS intent controllers instead of using Twilio APIs.
7. **Founder Direct Email Escalator (`MoreScreen.kt`)**: Fires system-native email intent triggers rather than using SMTP gateways.
8. **Real-time Chat WebSockets (`WorkspaceChat.kt`)**: Runs simulated auto-reply queues under local coroutines.
9. **Creator Commons Downloads (`CreatorCommonsScreen.kt`)**: Animates asset downloads without querying binary content CDN databases.
10. **Universal Search Index Compiling (`GlobalSearchScreen.kt`)**: Queries local SQLite databases using wildcards instead of Elasticsearch.
11. **Platform OTA Push Simulator (`PlatformControlCenterScreen.kt`)**: Animates progress bars to represent remote APK transfers.
12. **SMS Verification OTP Generator (`AuthScreen.kt`)**: Spawns mock random strings locally for immediate confirmation.
13. **DocuSign Contract Signatures (`AgreementVault.kt`)**: Simulates contract completions using click-to-sign visual overrides.
14. **Knowledge Base Artificial Sync (`KnowledgeBaseScreen.kt`)**: Shows a multi-stage loading screen that saves local states.
15. **SMS Spam Filtering Configuration (`RecruitmentAuditingConfig`)**: Settings toggles locally represent ML checks.
16. **YouTube Analytics OAuth API (`AnalyticsDashboardScreen.kt`)**: Populates dynamic Vico graphs with pre-seeded mockup data arrays.
17. **Co-Op Venture Investment Allocations (`SyndicateScreen.kt`)**: Adjustable budget sliders complete transactions locally.
18. **Disaster Recovery Database Snapper (`BackupCenterScreen.kt`)**: Reads and writes serialized local JSON database backups.
19. **Digital Media Split Percentage Sliders (`AgreementVault.kt`)**: IP percentage configurations are tracked locally.
20. **Security Threat Firewall Logs (`PlatformHealthScreen.kt`)**: monospace logs simulate system firewall packet audits.
21. **System Memory Pruning Trigger (`AdminDashboardScreen.kt`)**: Executes JVM garbage collection locally.
22. **Interactive REST Handshake Console (`AdminDashboardScreen.kt`)**: Tests local network statuses using delays.
23. **Community Feed Endorsement Badges (`CreatorCommonsScreen.kt`)**: Modifies visual tags using local state flows.
24. **Dispute Resolution Mediation Decision Dialog (`ReportModerationScreen.kt`)**: Handles local database resolution flags.
25. **Document Printer Spooler Canvas Layout (`DocumentPrintHelper.kt`)**: Generates and transfers PDF data to system print controllers.

### 💾 7.2: Database-Only / ViewModel-Only Backend Modules (Category 2 Gaps)
The following structures exist inside the Room database tables, DAOs, or ViewModels but lack fully realized user-facing screens or complete UI workflows:
1. **`AnnouncementInteraction` Tracking Entity**: Mapped in SQLite to save announcement status but lacks a user feed.
2. **Generic User Settings (`UserSetting` Table)**: Stores themes/fonts but provides no Settings Screen panel for standard users.
3. **`DisputeNote` Moderation Hub**: Mapped to handle admin escalations, but lacks a dedicated resolution view.
4. **`VerificationRequest` History Browser**: Stores L2/L3 applications, but provides no user history log view.
5. **Universal Security State Cache (`SecurityState`)**: Caches login statuses but does not block navigation gates.
6. **Detailed Audit Log Exporter**: Mapped in DB, but the export button does not write actual files to disk.
7. **Custom Workspace Policy Editor**: Policy configuration parameters in SQLite have no UI management forms.
8. **Automated Saved Search Notifications**: Tracks saved search filters but has no push subscription toggle.
9. **Creator Endorsements Tracker**: Mapped in the DB schema, but lacks a public display layout.
10. **Workspace Event Log & Calendar**: Tracks events in SQLite but provides no interactive calendar grid.
11. **Draft Brief Indicator**: Auto-saves unsubmitted workspace project briefs but has no UI indicator.
12. **Global Maintenance Blocker Screen**: Maintenance variables exist in SQLite but lack a fullscreen app blocker.
13. **Platform Feature Flag Segmentation**: Feature flags exist in the database but have no cohort UI overrides.
14. **Workspace Member Count Caps**: ViewModel blocks members at specific thresholds but invitation screens lack descriptive feedback.
15. **Recruitment Auditing Filter Options**: Mapped in SQLite but provides no configuration screen inside Discovery.
16. **User Account Ban Statuses (`isBanned`/`isSuspended`)**: Columns exist in user profiles but lack blocker screens.
17. **Dynamic Empty State Manager**: Schema supports custom configs but visual lists fall back to static text.
18. **Contextual Help Document Repository**: Tracks help texts in DB but does not load them dynamically based on screen focus.
19. **DR Backup File Hash Auditor**: Stores SHA-256 hashes but lacks visual auditing to detect backup tampering.
20. **Workspace Asset Versioning**: Stores revision hashes in SQLite but lacks an asset rollback screen.
21. **Deliverable Completion Verification**: Tracks deliverable completion but provides no sign-off gate for managers.
22. **Project Proposal Categorization**: Proposal tables support classifications but lack category badge UI filters.
23. **Talent Pitch Resume Text Viewer**: Caches resume files as text in SQLite but lacks a viewer panel.
24. **Referral Code Collision Detection**: ViewModels insert referral codes without validating unique keys in the DB.
25. **Security Level Navigation Authorization**: Restricts sidebar displays but lacks dynamic route-level navigation interceptors.


