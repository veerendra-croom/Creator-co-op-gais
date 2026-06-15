



CREATOR CO-OP ECOSYSTEM
BACKEND_STRUCTURE.md
Backend Architecture & API Reference
Version 1.0 | June 2026





1. Architecture
1.1 Folder Structure
/creator-coop-api
▸	├── /src
–	│   ├── /modules
–	│   │   ├── /auth          # Registration, login, JWT, OTP
–	│   │   ├── /users         # Profiles, karma, roles
–	│   │   ├── /square        # Posts, comments, spaces, voting
–	│   │   ├── /syndicate     # Projects, pitches, matching
–	│   │   ├── /workspace     # Chat, assets, video huddle
–	│   │   ├── /contracts     # Smart contract wizard, DocuSign
–	│   │   ├── /finance       # Stripe, splits, ledger, tax
–	│   │   └── /notifications # FCM push, email via Resend
▸	│   ├── /plugins           # Fastify plugins (auth, cors, rate-limit)
▸	│   ├── /jobs              # BullMQ job definitions (payout-split, tax-gen)
▸	│   ├── /prisma            # Schema, migrations
▸	│   ├── /utils             # Shared helpers
▸	│   └── server.js          # Entry point
▸	├── docker-compose.yml
▸	├── .env.example
▸	└── package.json

1.2 Layer Separation
Route Layer	Fastify route handlers. Input schema validation (JSON Schema). No business logic.
Service Layer	Core business logic. Prisma queries. External API calls (Stripe, DocuSign, YouTube).
Repository Layer	Prisma client wrappers. Reusable query functions. No business decisions.
Job Layer	BullMQ workers. Long-running tasks: payout processing, tax generation, email batches.
Plugin Layer	Fastify plugins: JWT auth guard, rate limiter, CORS, multipart file upload.


2. Core Database Schemas
users
Field	Type	Required	Notes
id	UUID	Yes	Primary key, auto-generated
email	VARCHAR(255)	Yes	Unique, indexed
phone	VARCHAR(20)	No	Unique when set, E.164 format
display_name	VARCHAR(80)	Yes	
avatar_url	TEXT	No	Supabase Storage URL
role	ENUM	Yes	Values: editor, host, writer, animator, thumbnail_designer, channel_manager
secondary_roles	JSONB	No	Array of additional roles
karma_score	INTEGER	Yes	Default: 0, min: 0
is_verified_pro	BOOLEAN	Yes	Default: false, auto-set at 500 karma
stripe_account_id	VARCHAR	No	Stripe Express account ID
portfolio_links	JSONB	No	Array of {platform, url}
created_at	TIMESTAMP	Yes	Auto
updated_at	TIMESTAMP	Yes	Auto

projects
Field	Type	Required	Notes
id	UUID	Yes	Primary key
manager_id	UUID FK → users	Yes	Project owner
title	VARCHAR(120)	Yes	
niche	VARCHAR(60)	Yes	
content_strategy	TEXT	Yes	
subscriber_count	INTEGER	No	Current channel size
equity_breakdown	JSONB	Yes	Array of {role, percentage}. Must sum to 100.
status	ENUM	Yes	Values: draft, published, negotiating, contracted, paused, completed
created_at	TIMESTAMP	Yes	

contracts
Field	Type	Required	Notes
id	UUID	Yes	
project_id	UUID FK → projects	Yes	
docusign_envelope_id	VARCHAR	Yes	DocuSign tracking
equity_snapshot	JSONB	Yes	Immutable copy of agreed equity at signing
jurisdiction	VARCHAR(60)	Yes	e.g. 'US-CA', 'IN', 'UK'
exit_clauses	JSONB	Yes	Array of {trigger, consequence}
status	ENUM	Yes	Values: draft, sent, partially_signed, fully_signed
signed_at	TIMESTAMP	No	Set when fully_signed
signatories	JSONB	Yes	Array of {user_id, signed_at, docusign_status}

payouts
Field	Type	Required	Notes
id	UUID	Yes	
workspace_id	UUID FK → workspaces	Yes	
gross_amount	DECIMAL(12,2)	Yes	Total before platform fee
platform_fee	DECIMAL(12,2)	Yes	2.5% of gross_amount
net_amount	DECIMAL(12,2)	Yes	gross - platform_fee
splits	JSONB	Yes	Array of {user_id, amount, stripe_transfer_id, status}
status	ENUM	Yes	Values: pending, processing, distributed, failed, disputed
payout_date	DATE	Yes	
revenue_source	ENUM	Yes	Values: adsense, sponsorship, super_chat, other
created_at	TIMESTAMP	Yes	


3. API Endpoints
3.1 Auth
Method	Endpoint	Description	Auth
POST	/api/v1/auth/register	Register new user with email/phone + password	No
POST	/api/v1/auth/login	Login, returns access + refresh JWT	No
POST	/api/v1/auth/refresh	Exchange refresh token for new access token	No
POST	/api/v1/auth/otp/send	Send OTP to phone number	No
POST	/api/v1/auth/otp/verify	Verify OTP, returns JWT	No
POST	/api/v1/auth/logout	Invalidate refresh token	Yes
POST	/api/v1/auth/google	Google OAuth callback, returns JWT	No
POST	/api/v1/auth/apple	Apple OAuth callback, returns JWT	No

3.2 Users / Profiles
Method	Endpoint	Description	Auth
GET	/api/v1/users/me	Get current authenticated user profile	Yes
PATCH	/api/v1/users/me	Update profile (name, bio, portfolio links)	Yes
GET	/api/v1/users/:id	Get public profile of any user	Yes
GET	/api/v1/users/search	Search talent directory (?q=&role=&niche=&lang=)	Yes
POST	/api/v1/users/me/stripe/connect	Initiate Stripe Express onboarding	Yes
GET	/api/v1/users/me/stripe/status	Check Stripe account verification status	Yes

3.3 The Square
Method	Endpoint	Description	Auth
GET	/api/v1/posts	Feed with sort (?sort=trending|new|top&space=)	Yes
POST	/api/v1/posts	Create post (text/image_url/video_url)	Yes
GET	/api/v1/posts/:id	Get post detail with first 20 comments	Yes
DELETE	/api/v1/posts/:id	Delete own post	Yes
POST	/api/v1/posts/:id/vote	Upvote or downvote (body: {direction: 'up'|'down'})	Yes
GET	/api/v1/posts/:id/comments	Paginated comments (?cursor=)	Yes
POST	/api/v1/posts/:id/comments	Create comment or reply (body: {text, parent_id?})	Yes
DELETE	/api/v1/comments/:id	Delete own comment	Yes
GET	/api/v1/spaces	List all spaces with member counts	Yes
GET	/api/v1/spaces/:slug/posts	Posts within a specific space	Yes
POST	/api/v1/media-feedback/:postId/comments	Create timestamp comment (body: {timestamp_ms, text})	Yes

3.4 Syndicate Board
Method	Endpoint	Description	Auth
GET	/api/v1/projects	Browse projects (?niche=&min_equity=&lang=)	Yes
POST	/api/v1/projects	Create new project posting	Yes
GET	/api/v1/projects/:id	Get project deep-dive	Yes
PATCH	/api/v1/projects/:id	Edit project (only if status=draft|published)	Yes
DELETE	/api/v1/projects/:id	Delete project (only if status=draft)	Yes
POST	/api/v1/projects/:id/pitches	Submit pitch to project	Yes
GET	/api/v1/projects/:id/pitches	List pitches received (manager only)	Yes
PATCH	/api/v1/pitches/:id/accept	Accept a pitch (triggers contract wizard)	Yes
PATCH	/api/v1/pitches/:id/decline	Decline a pitch	Yes
GET	/api/v1/swiper/cards	Fetch next 10 swiper cards	Yes
POST	/api/v1/swiper/:projectId/swipe	Record swipe (body: {direction: 'right'|'left'})	Yes

3.5 Contracts & Finance
Method	Endpoint	Description	Auth
POST	/api/v1/contracts	Generate contract from project equity + terms	Yes
GET	/api/v1/contracts/:id	View contract details (parties only)	Yes
POST	/api/v1/contracts/:id/sign	Trigger DocuSign signing URL for current user	Yes
POST	/api/v1/webhooks/docusign	DocuSign event webhook (signature complete)	No (signed)
POST	/api/v1/webhooks/stripe	Stripe Connect payout webhook	No (signed)
POST	/api/v1/webhooks/youtube	YouTube revenue notification	No (signed)
GET	/api/v1/wallet	Get wallet balance + pending payouts	Yes
GET	/api/v1/wallet/history	Paginated payout history	Yes
POST	/api/v1/wallet/withdraw	Trigger Stripe Express transfer to bank	Yes


4. Business Logic — Core Rules
Karma Calculation	Upvote = +1 karma. Downvote = -1 karma to poster. Floor = 0 karma. Batch updates every 30 seconds via Redis. Written to DB every 5 minutes.
Equity Validation	On project creation and contract generation: sum(equity_breakdown.percentages) must === 100. Reject with 422 if not.
Payout Split Formula	user_amount = (net_payout × user_equity_%) − (net_payout × treasury_%). Treasury % deducted first.
Platform Fee	platform_fee = gross_amount × 0.025. Applied before user splits. Non-negotiable, non-refundable.
Auto Workspace Creation	Triggered when: all required contract signatories have signed. Creates workspace record, default channels (#general, #scripts, #video-drafts). Sends push notification to all members.
Verified Pro Status	BullMQ job checks karma every hour. If karma ≥ 500 and is_verified_pro = false: set true, send congratulation push, boost Syndicate Board ranking.


5. Error Handling — Standard Responses
400 Bad Request	{ error: 'VALIDATION_ERROR', message: '...', fields: [{field, message}] }
401 Unauthorized	{ error: 'UNAUTHORIZED', message: 'Invalid or expired token' }
403 Forbidden	{ error: 'FORBIDDEN', message: 'You do not have permission for this action' }
404 Not Found	{ error: 'NOT_FOUND', message: 'Resource not found' }
409 Conflict	{ error: 'CONFLICT', message: '...' } (e.g. duplicate pitch)
422 Unprocessable	{ error: 'BUSINESS_RULE_VIOLATION', message: 'Equity must total 100%' }
429 Too Many Requests	{ error: 'RATE_LIMITED', retryAfter: 60 }
500 Server Error	{ error: 'INTERNAL_ERROR', message: 'Something went wrong', requestId: 'uuid' }


6. Security
▸	JWT: Access tokens expire in 15 minutes. Refresh tokens: 30 days, stored in httpOnly cookie.
▸	Rate limiting: /auth/* endpoints: 10 requests/min per IP. All other API: 100 req/min per user.
▸	All Stripe and DocuSign webhooks must be signature-verified before processing.
▸	Escrow account: no manual transfer endpoint exists. All transfers triggered only by verified webhook events.
▸	Contract equity_snapshot is immutable after signing. Any update attempt returns 403.
▸	All financial queries use parameterized Prisma queries. No raw SQL with user input.
▸	CORS: allow only app bundle IDs and admin dashboard domain. No wildcard origins in production.
