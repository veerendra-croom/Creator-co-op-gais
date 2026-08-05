# Creator Co-Op: Phase 5.5 Fix Plan (Hardening & Polish)

Based on the **Friction Log**, I will execute a 4-step hardening sprint to move the platform from "Functional" to "Polished".

## Step 1: Architectural Hardening (Category B)
- **Centralized Enums**: Convert Kanban lanes and Support priorities to `Sealed Classes`/`Enums`.
- **Date Utility**: Create `com.example.util.DateTimeUtils` for consistent formatting.
- **LazyColumn Optimization**: Add unique `keys` to all major lists.
- **Repository Resilience**: Implement `Result<T>` patterns in `AppRepository` for safer error propagation to UI.

## Step 2: UX "Feel" & Animation (Category A & C)
- **Simulated Latency**: Add `delay(1500)` to Stripe/Contract signing flows with a "Processing..." state.
- **Wallet Polish**: Implement `AnimatedContent` for balance updates.
- **Haptic Integration**: Add `LocalHapticFeedback` to all critical Admin buttons.
- **Skeleton Screens**: Implement `shimmerModifier` for Dashboard and Support screens.

## Step 3: Reliability & Edge Cases (Category A & D)
- **AI Retry Flow**: Add "AI Failed - Retry" UI logic to all Gemini-enabled text fields.
- **Empty State Enforcement**: Audit every list and ensure `EmptyStateConfig` is shown when `count == 0`.
- **Draft Persistence**: Add a "Local Draft" table to Room to save production briefs during composition.
- **Touch Target Audit**: Increase all smaller icon buttons to 48dp minimum.

## Step 4: Admin & CRM Refinement (Category D)
- **CSV Export Logic**: Implement a basic `Uri`-based file export for Audit logs.
- **Priority Highlighting**: Color-code the Support tickets and Audit logs based on severity.
- **Member Caps**: Add a logic check in `WorkspaceViewModel` for max-member counts.

## Step 5: Simplification, Polish & Structural Consolidation Plan
To transition the platform past the closed beta, a thorough consolidation and pruning lifecycle has been drafted:
- **Consolidate Ad Placement**: Fully deprecate unused `AdPlacement` classes, entities, and DAOs, replacing any remaining telemetry fields with generic metrics logs.
- **De-duplicate Help Content**: Merge the massive, redundant dialogs inside `AdminHelpDialog.kt` into the centralized `SupportCenterScreen.kt` and `CommunityGuidelinesScreen.kt` files to reduce binary overhead and code bloat.
- **Unify Workspace Assets**: Complete the asset manager and integrate Google Drive OAuth metadata with actual Room database assets, establishing clean folder nodes.
- **Unify User Settings**: Introduce a centralized User Settings Screen inside the More Screen tab to expose key-value preferences cached in the `UserSetting` table.
- **Verify with Room Auto-Migrations**: Configure schema auto-migrations to safely handle new profiles and setting definitions without destructuring existing databases.

---
**Verification**: Each step will be verified with `compile_applet`.
