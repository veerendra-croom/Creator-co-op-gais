# Creator Co-Op: UX & Technical Friction Log (v1.0)

This log documents 34 identified friction points discovered during a "Real-User" simulation audit.

---

## 🛑 Category A: High-Severity UX Friction (Immediate Action Required)

1. **Silent Failures in AI Drafting**: If the Gemini API fails (network error or quota), the UI simply shows a Toast and leaves the user looking at a blank text field. There is no "Retry AI Generation" button inside the text area.
2. **Missing "Exit" Paths**: Several deep-nested screens (like `SyndicateScreen` and `LegalScreen`) lack a dedicated Back Button in the TopBar, forcing users to rely on system back gestures which can be unintuitive on tablets.
3. **Hardcoded Kanban Lanes**: The strings `"TODO"`, `"IN_PROGRESS"`, and `"DONE"` are hardcoded throughout the app. If the team decides to rename a lane to "Review", the entire logic breaks.
4. **Instant Financial Actions**: "Linking" a Stripe account or "Signing" a contract is too fast. It feels fake. Users need a 2-second "Verifying..." state to trust the security of the transaction.
5. **Touch Target Deficit**: Many `IconButton` elements (especially in `AdminDashboard`) use the default size which results in a touch area smaller than the Material 3 mandated 48dp.
6. **No Empty States for Search**: Searching for a creator in the `Cohort Directory` that doesn't exist shows a blank screen. It should show the "No Results" EmptyStateConfig.
7. **Fragmented Search**: Every screen has its own local search. There is no "Universal Search" (Command Palette style) to find a task, a file, and a member at once.
8. **Wallet Opacity**: The Wallet balance update is instant. It should use an `AnimatedContent` number-counter for visual satisfaction.
9. **Agreement Friction**: signing a contract is a single button. It should require a "Swipe to Sign" or a simple Canvas signature to feel legally significant.
10. **Missing "Draft" Indicator**: When a user is drafting a production brief, if they navigate away, their work is lost. There is no local "Draft Saved" auto-persistence.

---

## 🛠️ Category B: Technical Debt & Stability

11. **Repo Error Handling**: `AppRepository` catches exceptions but often returns `Unit` or logs to `Log.e`. The UI remains in a "Loading" state forever if an error occurs.
12. **Unoptimized LazyColumn Keys**: Lists in `AdminAuditScreen` do not use `key = { it.id }`, leading to unnecessary re-compositions of the entire list when a single item changes.
13. **Seeded Data Explosion**: The `AppRepository` populates the DB on every start if empty. This logic is prone to race conditions if the app is closed during the first-run prep.
14. **Date Inconsistency**: Timestamps are formatted differently across 4 different screens. No centralized `DateUtils`.
15. **Context Leak in ViewModels**: Some ViewModels reference `Repository` methods that might take longer than the ViewModel lifecycle, but don't handle cancellation explicitly.
16. **Missing "Offline" Awareness**: The `SyncState` is a simulated enum. It doesn't actually check the `ConnectivityManager`.
17. **Hardcoded Asset URLs**: Image placeholders are hardcoded string URLs. They should be moved to a `Constants` or `BuildConfig` object.
18. **Room Schema Rigidity**: Adding a field to `UserProfile` currently requires a full DB wipe because auto-migrations aren't fully configured.
19. **Feature Flag Scoping**: Flags are global. There is no ability to enable a "Beta Feature" for just one specific workspace.
20. **ViewModel Bloat**: `AdminViewModel` is nearly 600 lines. It handles everything from Audit logs to Feature Flags. It should be split into `AuditViewModel`, `GovernanceViewModel`, etc.

---

## 🎨 Category C: Visual Polish & Feedback

21. **Reputation Pop-ups**: Gaining reputation is a silent DB update. It needs a "floating +10" animation in the corner.
22. **Huddle Staticism**: The `VideoHuddleScreen` has no "Pulse" or active-speaker detection simulation.
23. **Asset Upload Simulation**: File uploads in `WorkspaceFilesHub` are instant. Needs a mock progress bar.
24. **Theme Blinks**: Switching between Light/Dark mode doesn't use an `animateColorAsState` transition, leading to a jarring flash.
25. **Skeleton Shimmers**: High-latency screens (Dashboard) use simple "Loading..." text instead of modern M3 Skeletons.
26. **Haptic Feedback**: No vibration on "Approve" or "Deny" actions in the Admin console.
27. **Typography Scale**: Some secondary text (like logs) is 10sp, which is unreadable on high-density displays. Minimum should be 12sp.
28. **Collaborator "Ghosts"**: Deleting a user doesn't immediately remove their name from the "Assigned To" fields in Kanban cards (Foreign Key issues).
29. **Notification Overlap**: Toasts triggered in rapid succession overlap each other.
30. **CRM Note Attribution**: CRM notes don't show the avatar of the Admin who wrote them.

---

## 📈 Category D: Growth & Operational Gaps

31. **Referral Code Generation**: The logic in `GlobalViewModel` is purely random. It doesn't check for collisions in the local DB.
32. **Support Ticket priority**: "CRITICAL" tickets look exactly like "LOW" tickets in the list. Needs color coding (Red/Yellow).
33. **Member Limits**: There is no logic to prevent a user from adding 1,000 members to a workspace (Performance risk).
34. **Export Format**: "Export Audit Log" is a mock action. It should actually generate a CSV/JSON file in the `Downloads` folder.
