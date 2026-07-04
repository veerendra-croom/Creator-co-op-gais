



CREATOR CO-OP ECOSYSTEM
PRD.md
Product Requirements Document
Version 1.0 | June 2026





1. Product Overview
Product Name	Creator Co-Op Ecosystem
Type	Mobile-first iOS & Android application
Category	Creator Economy / Social + FinTech Hybrid
Target Store	Google Play Store & Apple App Store
Version	v1.0 MVP
Status	Pre-Development

1.1 Problem Statement
Independent digital creators (editors, writers, hosts, animators) face three compounding problems:
▸	No trusted infrastructure to form non-financial collaboration agreements with team members.
▸	Manual, error-prone expectation management via scattered chat apps and informal verbal agreements.
▸	No single platform that combines community discovery, project management, production tools, and alignment logs for the creator economy.

1.2 Target Users — Personas
Persona A — The Freelance Editor	Age 18–32. Skilled video/audio editor seeking equity deals instead of one-time Fiverr gigs. Needs portfolio visibility, stable recurring income.
Persona B — The Channel Ideator	Age 20–35. Has content ideas and management skills but needs a production team. Wants structured legal agreements to protect their brand equity.
Persona C — The Creative Talent	Age 18–30. On-camera hosts, voice actors, scriptwriters seeking part-time collaborative channels. Wants income without running a solo channel.
Persona D — The MCN Manager	Age 28–45. Enterprise user managing 5–50 channels. Needs a white-labeled management panel and bulk contract tooling.

1.3 Core Value Proposition
Why Creators Will Choose Creator Co-Op
✦  One app replaces: Reddit + LinkedIn + Stripe + DocuSign + Slack + Google Drive
✦  Legally binding equity contracts auto-generated in minutes
✦  Revenue from YouTube/Twitch/TikTok auto-splits to every team member on payout day
✦  Community karma builds credibility and unlocks higher-value partnerships


2. Features — Strict Specification
Feature A — The Square (Community Forum)
Feature Name	The Square — Algorithmic Community Forum
Priority	MUST HAVE (Core DAU Driver)
User Story	As a creator, I want to browse a niche content feed so I can discover trends, get feedback, and build my platform reputation.

Acceptance Criteria
1.	Users can post text, image links, and video links. Rich text editor NOT included in v1.
2.	Posts display upvote count, downvote count, comment count, and timestamp.
3.	Feed supports three sort modes: Trending (score algorithm), New (chronological), Top (all-time score).
4.	Space directories render list of sub-communities with member counts.
5.	Media Feedback Tool allows timestamp-linked comments on video URLs.
6.	Karma points update in real-time after votes are cast.
7.	'Verified Pro' badge unlocks at 500 karma points and boosts Syndicate Board visibility.

Feature	Description	Scope
INCLUDED	Post creation (text/image link/video link), upvote/downvote, nested comments, Spaces, karma engine, media feedback timestamps, trending algorithm	v1 MVP
OUT OF SCOPE	Native video upload/hosting, GIF support, polls, live streams, DMs via forum, ad placements in feed	Post-MVP

Feature B — Syndicate Board (Team Matchmaker)
Feature Name	Syndicate Board — Project & Talent Matching Engine
Priority	MUST HAVE (Core Monetization Funnel)
User Story	As a channel manager, I want to post a project with equity breakdowns so I can attract qualified talent. As a freelancer, I want to browse and pitch projects matching my skills.

Acceptance Criteria
8.	Managers can create project posts with: channel niche, subscriber count, content strategy brief, and equity pie chart (percentages must total exactly 100%).
9.	Equity pie chart renders visually as an interactive donut chart on project cards.
10.	Freelancers can filter by: niche, minimum equity %, language, software expertise, location.
11.	Swiper UI displays fullscreen portfolio cards. Right swipe = interest, Left = pass.
12.	Pitch submission includes: cover message, portfolio link, and relevant samples.
13.	Match occurs when manager accepts a pitch OR both parties swipe right.
14.	System creates a Workspace automatically upon confirmed match.

Feature	Description	Scope
INCLUDED	Project cards, equity pie chart, directory search, swiper UI, pitch submission, match confirmation, auto workspace creation	v1 MVP
OUT OF SCOPE	AI-powered match recommendations, video pitch submissions, group auditions, public talent auctions	Post-MVP

Feature C — Workspace & Production Pipeline
Feature Name	Workspace — Integrated Production Hub
Priority	MUST HAVE (Retention + Stickiness)
User Story	As a team member, I want a shared workspace with chat, file sharing, and video feedback tools so my team never needs to leave the app to collaborate.

Acceptance Criteria
15.	Each matched team receives one dedicated Workspace with channels: #general, #scripts, #video-drafts.
16.	Text chat supports file attachments (PDF, images, .zip up to 50MB per file).
17.	Voice/video huddle supports up to 8 simultaneous users.
18.	Asset Pipeline integrates with Google Drive and Dropbox via OAuth for file browsing.
19.	Timeline feedback tool allows clicking video timestamps to attach text notes.
20.	Contract Vault displays signed agreement (read-only), current month revenue, payout history.

Feature	Description	Scope
INCLUDED	Multi-channel team chat, file attachments, voice/video huddle (≤8 users), Google Drive/Dropbox OAuth, timeline feedback, contract vault	v1 MVP
OUT OF SCOPE	Screen sharing, AI transcription, task boards/Kanban, calendar integration, custom channel creation	Post-MVP

Feature D — Legal & Financial Splitter
Feature Name	Smart Contract + Automated Revenue Splitter
Priority	MUST HAVE (Core Trust & Monetization Engine)
User Story	As a team, we want our revenue split automatically on payout day, with legal contracts in place, so there are no payment disputes.

Acceptance Criteria
21.	Contract wizard generates agreements based on: team equity %, local jurisdiction selection, exit clauses, and IP ownership terms.
22.	All parties must digitally sign via DocuSign API before Workspace unlocks.
23.	Platform-controlled escrow account receives channel AdSense payouts via YouTube Data API.
24.	On payout trigger date, system auto-splits and routes funds to individual Stripe Express accounts within 24 hours.
25.	Treasury Wallet automatically withholds team-set reinvestment % from each payout.
26.	1099, W-8BEN, and local tax forms auto-generated based on annual payout totals.
27.	Platform takes 2.5% processing fee on every split transaction.

Feature	Description	Scope
INCLUDED	Contract wizard, DocuSign integration, Stripe Connect escrow, automated split ledger, treasury wallet, 1099/W-8BEN generation, 2.5% platform fee	v1 MVP
OUT OF SCOPE	Direct bank integration bypassing Stripe, crypto payouts, multi-currency treasury investment, tax filing submission (generation only)	Post-MVP


3. User Roles & Permissions
Role	Permissions
Channel Manager / Ideator	Create/edit/delete project posts; invite talent; sign contracts; access full workspace; view/manage treasury; initiate payouts
Creative Talent	Browse projects; submit pitches; sign contracts; access assigned workspace channels; view personal wallet; receive splits
Technical Specialist	Same as Creative Talent + upload assets to pipeline; leave timestamp feedback on video drafts
Platform Admin	Access all workspaces; freeze accounts; manage dispute resolution; view all financial ledgers; modify fee structure
Enterprise / MCN	White-labeled management panel; manage up to 50 workspaces; bulk contract management; aggregated analytics dashboard


4. Functional Requirements
ID	Requirement	Priority
FR-01	System must support 10,000 concurrent users without performance degradation (>3s response time).	HIGH
FR-02	All financial transactions must be processed via Stripe Connect Custom; no direct bank transfers in-app.	CRITICAL
FR-03	Smart contracts must be digitally signed by ALL named parties before any workspace feature unlocks.	CRITICAL
FR-04	Revenue split must execute automatically within 24 hours of payout trigger event via YouTube Data API.	HIGH
FR-05	Karma score must update within 5 seconds of a vote being cast.	MEDIUM
FR-06	Swiper UI must preload next 5 portfolio cards for < 200ms perceived transition time.	MEDIUM
FR-07	Tax form generation must correctly apply jurisdiction rules for US (1099), international (W-8BEN), and India (to be defined).	HIGH
FR-08	All video URLs linked in Media Feedback Tool must be validated as public (non-private) before timestamp comments are enabled.	MEDIUM


5. Non-Functional Requirements
Performance	API response time < 300ms (p95). App cold start < 3s on mid-range Android devices.
Scalability	Microservices architecture. Horizontal scaling for community feed and payout processing services independently.
Security	AES-256 encryption at rest. TLS 1.3 in transit. Bank-grade escrow protection. No manual alteration of signed split structures permitted.
Compliance	GDPR (EU), CCPA (California), India IT Act 2000. Financial data: PCI-DSS Level 1 via Stripe.
Uptime	99.5% SLA. Payout processing service: 99.9% SLA (critical path).
Accessibility	WCAG 2.1 AA compliance for all core screens.


6. Edge Cases & Error Handling
Scenario	Expected Behavior
Payout API fails on split day	Retry 3x with exponential backoff. Alert all team members via push notification. Escrow holds funds securely until successful.
Team member exits contract mid-term	System triggers exit clause from smart contract. Equity redistributed per agreed exit terms. Remaining members sign amendment contract.
Equity pie chart doesn't total 100%	Form validation blocks submission. Real-time percentage calculator shows remaining % and highlights discrepancy.
User submits pitch to own project	System blocks pitch. Error: 'You cannot pitch to a project you manage.'
Video huddle drops below 2 users	Auto-end session. Save transcript/notes if any were created.
Stripe Express account unverified on payout	Funds held in escrow. Push notification sent daily until account verified. 30-day maximum hold before dispute resolution.
Duplicate account detection	Match on email + phone. Block second registration. Prompt login recovery.
Platform fee deduction dispute	Display itemized fee breakdown in Contract Vault. Fees are contractually agreed at onboarding. No refunds for processed splits.

