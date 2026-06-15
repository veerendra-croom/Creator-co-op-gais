



CREATOR CO-OP ECOSYSTEM
TECH_STACK.md
Technology Stack — Zero Ambiguity Document
Version 1.0 | June 2026





1. Frontend (Mobile App)
Category	Technology	Version	Justification
Framework	React Native	0.74.1	Cross-platform iOS + Android. Single codebase. Large talent pool. Expo-free for Play Store control.
Styling	NativeWind (Tailwind for RN)	4.0.1	Utility-first styling. Design token consistency. No StyleSheet boilerplate.
State Management	Zustand	4.5.2	Lightweight, no boilerplate. No Redux overhead. Works well with React Native.
Navigation	React Navigation	6.1.17	De-facto standard for RN. Stack + Bottom Tab + Modal navigators.
Forms	React Hook Form	7.51.3	Minimal re-renders. Built-in validation. Native keyboard handling.
API Client	TanStack Query (React Query)	5.29.2	Server state cache, background refetch, pagination, offline support.
Charts	Victory Native XL	40.0.0	Reanimated-based. Smooth donut/pie charts for equity display.
Video Player	react-native-video	6.3.0	Timestamp-linked feedback tool. Custom controls.
Real-time Chat	Socket.IO Client	4.7.5	Bidirectional real-time messaging for Workspace chat.
Video Huddle	Daily.co React Native SDK	0.13.0	WebRTC-based. Up to 8 participants. Simple SDK.
Animations	React Native Reanimated	3.9.0	Swiper UI card animations. 60fps gesture-driven transitions.
Swiper	react-native-deck-swiper	2.0.17	Tinder-style swiper for Syndicate Board Swiper UI.


2. Backend
Category	Technology	Version	Justification
Language	Node.js	20.11.0 LTS	Non-blocking I/O. Excellent for real-time + API workloads.
Framework	Fastify	4.26.2	2x faster than Express. Schema-first JSON validation. Plugin architecture.
API Type	REST (JSON:API spec)	—	Simpler than GraphQL for mobile. Easier CDN caching. Standard HTTP tooling.
Real-time Layer	Socket.IO (Server)	4.7.5	Workspace chat, live karma updates, payout notifications.
Task Queue	BullMQ	5.4.3	Redis-backed job queue for payout processing, email jobs, API retries.
Caching	Redis	7.2.4	Session cache, leaderboard/karma cache, rate limiting store.
Email	Resend SDK	3.2.0	Transactional email for OTP, contract alerts, payout confirmations.
Push Notifications	Firebase Cloud Messaging	12.4.0	Both Android and iOS push. Free at scale.


3. Database
Category	Technology	Version	Justification
Primary DB	PostgreSQL	16.2	Relational. ACID transactions critical for financial ledger. JSON column support.
ORM	Prisma	5.12.1	Type-safe. Auto-migrations. Schema-first. Excellent PostgreSQL support.
Search Engine	Meilisearch	1.7.3	Talent directory full-text search. Filterable facets for niche/skills/location.
File Storage	Supabase Storage	2.5.4	S3-compatible. Handles profile images, portfolio thumbnails, asset pipeline uploads.
Time-Series	PostgreSQL (TimescaleDB ext)	2.14.2	Revenue ledger and payout history time-series queries. Single DB stack.


4. Authentication
Category	Technology	Version	Justification
Primary Method	JWT (JSON Web Tokens)	—	Stateless. Works with mobile. Short-lived access (15min) + long-lived refresh (30 days).
Social OAuth	Google OAuth 2.0	—	Play Store primary. Reduced friction.
Apple Sign In	Apple OAuth 2.0	—	Required for App Store compliance.
Phone Auth	Firebase Auth (Phone OTP)	—	SMS OTP for global users. Handles carriers.
Contract Signing	DocuSign eSignature API	v2.1	Legally binding. Audit trail. 175+ countries supported.


5. Financial Infrastructure
Category	Technology	Version	Justification
Payments Platform	Stripe Connect Custom	2024-04-10	Master escrow + individual Express accounts. Global payouts. 135+ currencies.
Revenue Webhooks	YouTube Data API v3	v3	Monitor channel revenue events and payout triggers.
Tax Automation	Stripe Tax + Manual PDF generation	—	1099-K auto-generation. W-8BEN via template system.


6. Deployment & DevOps
Category	Technology	Version	Justification
Cloud Provider	AWS	—	EC2 (API servers), RDS (PostgreSQL), ElastiCache (Redis), S3 (assets).
Containerization	Docker	26.0.1	Consistent environments. Compose for local dev.
Orchestration	AWS ECS (Fargate)	—	Serverless containers. Auto-scaling. No Kubernetes overhead for v1.
CI/CD	GitHub Actions	—	Auto-test + deploy on merge to main. Environment: dev/staging/prod.
CDN	CloudFront	—	Asset delivery, API response caching for feed endpoints.
Monitoring	Datadog APM	—	Trace API response times, payout job success rates, error rates.
Error Tracking	Sentry	8.2.0	React Native + Fastify SDK. Real-time crash reporting.
Mobile OTA Updates	CodePush (App Center)	3.4.1	JS bundle hot updates without Play Store re-submission.


Critical Version Lock Note
All versions listed above are LOCKED for v1.0 development.
No team member may upgrade any dependency without a documented ADR (Architecture Decision Record).
Use exact version pinning in package.json (no ^ or ~ prefixes).

