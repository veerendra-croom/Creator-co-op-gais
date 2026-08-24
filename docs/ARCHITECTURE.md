# Creator Co-Op: System Architecture & Design Specification

---

## 1. High-Level System Architecture

Creator Co-Op is built on an **Offline-First Reactive Architecture** designed to deliver instant local UI responsiveness while ensuring reliable background synchronization with Supabase cloud infrastructure.

```
   ┌────────────────────────────────────────────────────────┐
   │               Jetpack Compose UI Layer                  │
   │  (Obsidian Slate Theme, Responsive Adaptive Insets)    │
   └───────────────┬────────────────────────▲───────────────┘
                   │ User Actions           │ StateFlow (Unidirectional Data Flow)
   ┌───────────────▼────────────────────────┴───────────────┐
   │               Reactive ViewModels                      │
   │  (Global, Workspace, Agreement, Syndicate, Auth, CRM)   │
   └───────────────┬────────────────────────▲───────────────┘
                   │ Repository Operations  │ Kotlin Coroutine Flows
   ┌───────────────▼────────────────────────┴───────────────┐
   │                AppRepository Layer                     │
   │  - Read Path: Local Room DB (Immediate UI Response)   │
   │  - Write Path: Room DB First + Queued Delta Sync       │
   └───────┬────────────────────────────────────────▲───────┘
           │                                        │
    Local Operations                         Remote Reconcile
           │                                        │
   ┌───────▼──────────────┐             ┌───────────┴──────────┐
   │   Room SQLite DB     │             │ Supabase PostgreSQL  │
   │ (Single Source of    │◄───────────►│ (Realtime WebSocket, │
   │      Truth)          │   WorkMgr   │  PostgREST Auth/RLS) │
   └──────────────────────┘             └──────────────────────┘
```

---

## 2. Core Architectural Pillars

### 2.1 Single Source of Truth (SSOT)
- All user-facing components observe state exclusively from the local **Room SQLite Database** via Kotlin `Flow<T>`.
- The UI never waits for network handshakes to render updates. Operations such as moving Kanban tasks, drafting agreements, or updating profiles take effect immediately on device.

### 2.2 Offline Delta Synchronization Engine
- Mutations enqueue an immutable event into `central_sync_queue`.
- `CentralDeltaSyncWorker` runs periodically via Android `WorkManager` (and on network reconnection) to drain the queue and execute batch upserts against Supabase PostgREST endpoints.
- Remote server updates reconcile seamlessly into the local Room database, emitting fresh state to observing ViewModels.

### 2.3 Reactive ViewModel Pattern
- ViewModels instantiate cleanly using `AppViewModelFactory` with standard constructor injection through `AppContainer`.
- UI State is exposed as immutable `StateFlow<T>` models collected using Compose `collectAsState()` / `collectAsStateWithLifecycle()`.

---

## 3. High Pro Max Obsidian Slate Design System

### 3.1 Color Hierarchy & Surface Depth
- `PrimaryBackground`: `#090B0F` (Deep obsidian canvas)
- `SurfaceColor`: `#11151D` (Card and panel base)
- `SurfaceLight`: `#171D28` (Interactive hover/elevation state)
- `ColorDivider`: `#222B3A` (Subtle 1dp glassmorphic border)
- `AccentBlue`: `#38BDF8` (Primary actions, pulse telemetry)
- `NeonEmerald`: `#10B981` (Verified badges, agreement confirmations)
- `CrispAmber`: `#F59E0B` (Syndicate radar sweeps, pending alerts)
- `AccentRed`: `#EF4444` (Dispute flags, security escalation)

### 3.2 Dynamic Canvas Telemetry
- **Pulse Wave Visualizer**: Smooth Sine wave rendering displaying real-time system throughput and database latency.
- **Concentric Radar Scanner**: Multi-ring animated radar sweep indicating creator proximity and syndicate matchmaking status.
- **Luminous Sparklines**: Cubic bezier CRM performance curves featuring vertical alpha gradients and dual-layer glow endpoints.
