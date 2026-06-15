



CREATOR CO-OP ECOSYSTEM
IMPLEMENTATION_PLAN.md
Phased Build Execution Plan
Version 1.0 | June 2026





Build Philosophy
Build thin vertical slices: every phase produces a working, testable feature end-to-end.
No phase ends with only backend OR only frontend done — always ship a complete user flow.
Estimated hours assume 1 senior full-stack dev + 1 mobile dev working in parallel.


Phase 1: Project Setup & Infrastructure
Target: Working dev environment + CI/CD pipeline + empty app skeleton

Task ID	Task	Files to Create/Update	Est. Hours
1.1	Initialize React Native project	package.json, App.tsx, .env.example, babel.config.js	2h
1.2	Install and configure all frontend dependencies (exact versions)	package.json (locked), yarn.lock	2h
1.3	Initialize Fastify backend project	/src/server.js, package.json, .env.example	2h
1.4	Configure PostgreSQL via Prisma: initial schema for users table	/src/prisma/schema.prisma, migration 001	3h
1.5	Dockerize backend: Dockerfile, docker-compose (API + Postgres + Redis)	Dockerfile, docker-compose.yml	3h
1.6	Configure GitHub Actions CI: lint, test, deploy-to-staging on merge to main	.github/workflows/ci.yml	3h
1.7	Set up React Navigation: 4-tab bottom navigator (empty screens)	/src/navigation/MainTabNavigator.tsx, all screen placeholder files	2h
1.8	Set up NativeWind + design tokens (colors, typography, spacing)	tailwind.config.js, /src/theme/tokens.ts	3h
1.9	Configure Sentry for both React Native and Fastify	App.tsx (Sentry init), /src/plugins/sentry.js	2h
1.10	Deploy backend skeleton to AWS ECS staging environment	AWS ECS task definition, ECR repo	4h


Phase 2: Core Backend — Auth + Users + Database
Target: Working authentication API + user profiles in database

Task ID	Task	Files to Create/Update	Est. Hours
2.1	Build /auth/register endpoint with email + phone + password support	/src/modules/auth/auth.routes.js, auth.service.js	3h
2.2	Build /auth/login with JWT access + refresh token generation	/src/modules/auth/auth.service.js, /src/plugins/jwt.js	3h
2.3	Integrate Firebase Auth for OTP (send + verify endpoints)	/src/modules/auth/otp.service.js, auth.routes.js	4h
2.4	Google OAuth 2.0 callback endpoint	/src/modules/auth/google.service.js	3h
2.5	Apple OAuth 2.0 callback endpoint	/src/modules/auth/apple.service.js	3h
2.6	Build JWT auth guard Fastify plugin (preHandler hook)	/src/plugins/auth-guard.js	2h
2.7	Extend Prisma schema: full users table (all fields from schema doc)	/src/prisma/schema.prisma, migration 002	2h
2.8	Build /users/me GET and PATCH endpoints	/src/modules/users/users.routes.js, users.service.js	3h
2.9	Build /users/:id public profile GET	/src/modules/users/users.routes.js	1h
2.10	Integrate Meilisearch: index users on creation/update	/src/modules/users/users.service.js, meilisearch.js plugin	4h
2.11	Build /users/search endpoint (query Meilisearch)	/src/modules/users/users.routes.js	2h
2.12	Write unit tests: auth service + user service	/src/modules/auth/__tests__, /src/modules/users/__tests__	4h


Phase 3: Core Frontend — Onboarding + Auth Screens
Target: User can register, log in, and complete onboarding on device

Task ID	Task	Files to Create/Update	Est. Hours
3.1	Build Splash Screen with animated logo	/src/screens/SplashScreen.tsx	3h
3.2	Build Login Screen (Google/Apple/Phone options)	/src/screens/auth/LoginScreen.tsx	4h
3.3	Build Sign Up Screen with React Hook Form validation	/src/screens/auth/SignUpScreen.tsx	4h
3.4	Build OTP Verification Screen	/src/screens/auth/OTPScreen.tsx	3h
3.5	Connect auth screens to backend via TanStack Query	/src/api/auth.ts, /src/stores/authStore.ts (Zustand)	4h
3.6	Build Role Selection Screen (multi-checkbox)	/src/screens/onboarding/RoleSelectionScreen.tsx	3h
3.7	Build Portfolio Setup Screen	/src/screens/onboarding/PortfolioScreen.tsx	3h
3.8	Build Stripe Express Setup Screen (WebView-based onboarding)	/src/screens/onboarding/PayoutSetupScreen.tsx	4h
3.9	Implement auth state persistence (token storage via Keychain)	/src/utils/tokenStorage.ts	2h
3.10	Implement conditional navigation: onboarding vs main app	/src/navigation/RootNavigator.tsx	2h


Phase 4: The Square (Forum) — Backend + Frontend
Target: Fully working community feed with posts, voting, comments, spaces

Task ID	Task	Files to Create/Update	Est. Hours
4.1	Prisma schema: posts, comments, votes, spaces tables	/src/prisma/schema.prisma, migration 003	3h
4.2	Build POST /posts and GET /posts (with trending algorithm)	/src/modules/square/square.routes.js, square.service.js	5h
4.3	Build POST /posts/:id/vote with karma update (Redis cache)	/src/modules/square/square.service.js, karma.job.js	4h
4.4	Build nested comments API (GET + POST /posts/:id/comments)	/src/modules/square/comments.service.js	4h
4.5	Build GET /spaces and GET /spaces/:slug/posts	/src/modules/square/spaces.service.js	3h
4.6	Build timestamp comments API (Media Feedback Tool)	/src/modules/square/mediaFeedback.service.js	3h
4.7	Frontend: Build The Square screen with FlashList feed	/src/screens/square/SquareScreen.tsx	5h
4.8	Frontend: Build PostCard component with upvote/downvote	/src/components/PostCard.tsx	4h
4.9	Frontend: Build Post Detail + Comments thread screen	/src/screens/square/PostDetailScreen.tsx	5h
4.10	Frontend: Build Create Post modal (text/image link/video link)	/src/screens/square/CreatePostModal.tsx	4h
4.11	Frontend: Build Spaces Directory + individual Space screen	/src/screens/square/SpacesScreen.tsx, SpaceFeedScreen.tsx	4h
4.12	Frontend: Build Media Feedback Tool (video player + timestamp comments)	/src/screens/square/MediaFeedbackScreen.tsx	6h
4.13	Frontend: Real-time karma counter update via Socket.IO	/src/screens/square/PostCard.tsx (socket listener)	3h


Phase 5: Syndicate Board — Matchmaking Engine
Target: Managers can post projects, freelancers can pitch, matches create workspaces

Task ID	Task	Files to Create/Update	Est. Hours
5.1	Prisma schema: projects, pitches, swipes tables	/src/prisma/schema.prisma, migration 004	3h
5.2	Build Create Project API with equity validation (must = 100%)	/src/modules/syndicate/projects.service.js	5h
5.3	Build GET /projects with filters (Meilisearch integration)	/src/modules/syndicate/projects.routes.js	4h
5.4	Build pitch submission API + business rule: max 3 pitches per project	/src/modules/syndicate/pitches.service.js	4h
5.5	Build accept/decline pitch API + workspace auto-creation trigger	/src/modules/syndicate/pitches.service.js, workspace.service.js	5h
5.6	Build swiper card API + swipe recording	/src/modules/syndicate/swiper.service.js	3h
5.7	Frontend: Build Syndicate Board main screen (toggle Find Projects / Find Talent)	/src/screens/syndicate/SyndicateScreen.tsx	3h
5.8	Frontend: Build Project Card with equity donut chart (Victory Native)	/src/components/ProjectCard.tsx	5h
5.9	Frontend: Build Project Deep-Dive screen + Submit Pitch flow	/src/screens/syndicate/ProjectDetailScreen.tsx, SubmitPitchScreen.tsx	5h
5.10	Frontend: Build Swiper UI with Reanimated card animations	/src/screens/syndicate/SwiperScreen.tsx	6h
5.11	Frontend: Build Create Project form with live equity pie chart	/src/screens/syndicate/CreateProjectScreen.tsx	6h
5.12	Frontend: Build Talent Directory with filter/search	/src/screens/syndicate/TalentDirectoryScreen.tsx	4h


Phase 6: Workspace + Contracts + Finance
Target: Teams can collaborate, sign contracts, and receive automated payouts

Task ID	Task	Files to Create/Update	Est. Hours
6.1	Prisma schema: workspaces, workspace_channels, messages, contracts, payouts tables	/src/prisma/schema.prisma, migration 005	4h
6.2	Build workspace chat via Socket.IO (join room, send message, receive message)	/src/modules/workspace/chat.gateway.js	6h
6.3	Build Asset Pipeline API (list files, link to Google Drive/Dropbox via OAuth)	/src/modules/workspace/assets.service.js	5h
6.4	Integrate DocuSign: contract generation + signing URL endpoint	/src/modules/contracts/docusign.service.js	8h
6.5	Build DocuSign webhook handler: detect all-signed → unlock workspace	/src/modules/contracts/webhooks.js	4h
6.6	Integrate Stripe Connect: escrow account setup + Express account creation flow	/src/modules/finance/stripe.service.js	6h
6.7	Build YouTube webhook handler: detect payout event → trigger split job	/src/modules/finance/youtube.webhook.js	5h
6.8	Build BullMQ payout-split job (calculate splits, route via Stripe, update ledger)	/src/jobs/payoutSplit.job.js	8h
6.9	Build tax form generation (1099, W-8BEN PDF generation)	/src/jobs/taxFormGen.job.js	6h
6.10	Frontend: Build Workspace List screen	/src/screens/workspace/WorkspaceListScreen.tsx	3h
6.11	Frontend: Build Workspace Dashboard (chat + asset grid + contract vault tabs)	/src/screens/workspace/WorkspaceDashboardScreen.tsx	6h
6.12	Frontend: Build real-time chat UI with Socket.IO client	/src/screens/workspace/ChatScreen.tsx	5h
6.13	Frontend: Build Contract Vault screen (read-only contract view + ledger)	/src/screens/workspace/ContractVaultScreen.tsx	4h
6.14	Frontend: Build Wallet + Profile screen (earnings, payout history, withdraw button)	/src/screens/profile/WalletScreen.tsx	5h
6.15	Integrate Daily.co SDK for voice/video huddle (up to 8 users)	/src/screens/workspace/HuddleScreen.tsx	5h


Phase 7: Testing, Polish & Play Store Launch
Target: Production-ready app published to Google Play Store

Task ID	Task	Files to Create/Update	Est. Hours
7.1	Integration tests: full user journey (register → pitch → contract → payout)	/src/tests/integration/userJourney.test.js	8h
7.2	Load test: payout processing under 500 simultaneous splits (k6 scripts)	/src/tests/load/payoutLoad.js	4h
7.3	Security audit: OWASP Mobile Top 10 checklist for React Native app	Security audit report document	6h
7.4	Accessibility pass: WCAG 2.1 AA on all 35 screens	Component updates across all screen files	6h
7.5	Performance: Profile app on low-end Android (FlashList, image caching, JS bundle)	Multiple component files	4h
7.6	App Store assets: icon (512×512), feature graphic (1024×500), 8 screenshots	/assets/store/	4h
7.7	Create Google Play Store developer account + submit app for review	Play Console	2h
7.8	Configure CodePush for OTA JS updates post-launch	appcenter-config.json, App.tsx	2h
7.9	Configure Datadog APM dashboards: API p95 latency, payout success rate, DAU	Datadog dashboard JSON	3h
7.10	Configure crash alerting: PagerDuty + Sentry for payout job failures	PagerDuty integration + Sentry alert rules	2h


Total Estimated Timeline (2-Dev Team)
Phase 1 — Setup:           ~26 hours
Phase 2 — Backend Auth:    ~35 hours
Phase 3 — Auth Frontend:   ~34 hours
Phase 4 — The Square:      ~53 hours
Phase 5 — Syndicate Board: ~53 hours
Phase 6 — Workspace:       ~80 hours
Phase 7 — Launch:          ~41 hours
─────────────────────────────────────
TOTAL:                    ~322 hours  (~10–12 weeks at full capacity)

