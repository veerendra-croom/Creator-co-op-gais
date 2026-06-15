



CREATOR CO-OP ECOSYSTEM
FRONTEND_GUIDELINES.md
UI/UX Design System & Component Rules
Version 1.0 | June 2026





1. Design System
1.1 Color Palette
Name	HEX	Usage	Contrast (on white)
primary	#1A1A2E	App background, headers, primary text on light	AAA on white
accent-red	#E94560	CTAs, badges, upvote active, FABs, error borders	AA Large
accent-blue	#0F3460	Secondary headings, link color, info highlights	AAA on white
surface	#16213E	Card backgrounds, sidebar	N/A (dark bg)
surface-light	#F4F4F8	Input backgrounds, table rows, shimmer base	N/A
text-primary	#FFFFFF	All text on dark backgrounds	AAA on primary
text-secondary	#A8A8B8	Metadata, timestamps, muted labels	AA on dark bg
success	#00B37E	Signed badge, payout success, verified	AA Large
warning	#F5A623	Pending payout, draft status	AA Large on white
error	#E94560	Form errors, failed payout, blocked action	AA Large
divider	#2A2A4A	Horizontal rules, table borders (dark mode)	N/A

1.2 Typography
Name	Size	Weight	Usage
Display	32sp	800	Screen titles only (e.g. Wallet total balance)
H1	24sp	700	Tab screen primary headings
H2	20sp	600	Section headings within screens
H3	16sp	600	Card titles, post titles, project names
Body Large	16sp	400	Primary body content, pitch text
Body Regular	14sp	400	Default body, comment text, descriptions
Label	12sp	500	Metadata, timestamps, tags, badges
Micro	10sp	400	Legal footnotes, tax document labels only
Font Family: Inter (all weights). Import via Google Fonts. Fallback: System Sans-Serif.
Line Height: 1.5× font size for body. 1.2× for headings. Letter Spacing: 0 for body, −0.5px for headings.

1.3 Spacing System (8px Base Grid)
Token	Value (px)	Usage
space-1	4px	Micro gaps: icon padding, badge internal spacing
space-2	8px	Compact gaps: between inline elements, tag chips
space-3	12px	Small gaps: list item separators
space-4	16px	Standard gap: card internal padding, form field gap
space-5	20px	Medium gap: section content padding
space-6	24px	Large gap: between major sections within a screen
space-8	32px	XL gap: screen-level vertical padding
space-12	48px	XXL: bottom nav clearance, modal top spacing


2. Component Rules
2.1 Buttons
Primary CTA	Background: accent-red. Text: white. Border-radius: 12px. Height: 52px. Font: Label 600. Full width in forms, auto-width elsewhere.
Secondary Button	Background: transparent. Border: 1px solid accent-blue. Text: accent-blue. Same sizing as Primary.
Ghost / Text Button	No background, no border. Text: text-secondary. Underline on press only.
Destructive Button	Background: error (#E94560). Only for irreversible actions (exit contract, delete account). Always preceded by confirmation modal.
Disabled State	Opacity: 0.4. Not pressable. No visual feedback on tap.
Loading State	Replace button text with ActivityIndicator (white, size small). Disable further taps.
Icon Button	Minimum tap target: 44×44px even if icon is smaller. Use TouchableOpacity with hitSlop.

2.2 Forms
▸	All text inputs: height 52px, border-radius 10px, background surface-light, border 1px divider, focus border accent-blue.
▸	Error state: border-color error (#E94560). Error message in Label 12sp red below the field.
▸	Required fields: asterisk (*) in accent-red after the label text.
▸	Character counters: display below textarea when > 80% of limit reached.
▸	All forms must use React Hook Form. No uncontrolled inputs.
▸	Keyboard type must match field: numberPad for amounts, emailAddress for email.

2.3 Cards
Post Card (Square)	Background surface. Padding 16px. Border-radius 12px. Shadow: 0 2px 8px rgba(0,0,0,0.3). Contains: avatar, username, timestamp, title, body (max 3 lines, ellipsis), action row (upvote / downvote / comment / share).
Project Card (Syndicate)	Same base as Post Card. Contains: channel name, niche badge, subscriber count, equity donut chart (80px diameter), role slots available.
Workspace Card (List)	Horizontal card. Left: channel thumbnail (48×48). Right: channel name, last message preview, unread badge. Full width.
Talent Card (Swiper)	Fullscreen (screen width × 70vh). Background gradient overlay. Contains: avatar (96px), name, role badges, karma score, 2–3 portfolio links.

2.4 Modals
▸	All modals: bottom sheet style (react-native-bottom-sheet). Handle bar visible. Background overlay: rgba(0,0,0,0.7).
▸	Confirmation modals (destructive actions): center modal with icon, title (H2), description (Body), Cancel + Confirm buttons.
▸	Maximum modal height: 90% of screen height. Must be scrollable if content exceeds.
▸	Close modal: swipe down on bottom sheet, tap overlay, or explicit 'Cancel' button. Never auto-close without user action on confirmations.


3. Layout Rules
Bottom Navigation	4 tabs. Icons + labels. Active tab: accent-red. Inactive: text-secondary. Height: 60px + safe area inset.
Screen Padding	Horizontal: 16px on all screens. Top: 12px below nav bar. Bottom: 80px above bottom nav (avoid content hiding).
Safe Areas	Always use SafeAreaView or useSafeAreaInsets. Never hard-code status bar heights.
List Rendering	Always use FlashList (not FlatList) for feeds > 20 items. estimatedItemSize must be set.
Images	All remote images: use expo-image with placeholder shimmer. Never raw Image tag.
Keyboard Avoidance	KeyboardAvoidingView on all forms. Behavior: 'padding' on iOS, 'height' on Android.


4. State Handling
4.1 Loading States
▸	Skeleton shimmer for feed cards (not spinner). Use ShimmerPlaceholder library.
▸	Full-screen loader only for: initial app load, contract generation, payout processing.
▸	Inline ActivityIndicator for: search results, pagination load-more.

4.2 Error States
▸	Network error: Full-screen empty state with 'No connection' icon + 'Try Again' button.
▸	Empty feed: Illustration + contextual message (not generic 'No results').
▸	Form error: Inline field-level error. Toast for submission-level errors.
▸	Toast notifications: react-native-toast-message. Duration: 3s. Position: top. Never stack > 2 toasts.

4.3 Empty States
No projects (Syndicate)	'No matching projects yet. Try adjusting your filters.' + filter shortcut button.
Empty workspace chat	'Send your first message to the team!' with an emoji illustration.
Empty wallet	'Your earnings will appear here once your first payout is processed.'
No karma yet	'Start posting in The Square to build your reputation!'


5. Accessibility Rules
▸	All interactive elements: minimum 44×44px tap target.
▸	Color contrast: WCAG 2.1 AA minimum on all text (4.5:1 for body, 3:1 for large text).
▸	All images: accessibilityLabel prop required. Never empty alt text on informational images.
▸	Dynamic text: support system font scaling up to 1.5× without layout breakage.
▸	Screen readers: all buttons must have accessibilityRole and accessibilityHint.


6. Do's and Don'ts
DO — Always Follow These Rules
✅  Use design tokens (colors, spacing) — never hardcode hex values in component files
✅  Use FlashList for all scrollable lists with > 10 items
✅  Every touchable element must have a pressed opacity (activeOpacity: 0.7)
✅  All async operations must show loading state before and success/error after
✅  Use react-native-haptic-feedback for swipe actions and button confirmations
✅  Test on a real low-end Android device (e.g. Samsung A-series) before PR merge

DON'T — These Are Hard Restrictions
❌  Never use StyleSheet.absoluteFill as a modal overlay — use the bottom sheet component
❌  Never render more than 3 network images above the fold without lazy loading
❌  Never use Text without a defined style — always map to a typography token
❌  Never place primary CTA buttons at the top of screens — always bottom (thumb reach)
❌  Never auto-advance users past confirmation screens (contracts, payouts, account deletion)
❌  Never use system alert() — always use custom modal or toast for user messaging

