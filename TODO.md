# The Walking TEC - Refactoring TODO

This document tracks the architectural improvements for the game, moving from a monolithic `GameManager` to a decoupled, maintainable structure.

## Phase 1: Concurrency & Stability [COMPLETED]
- [x] Use `AtomicInteger` for thread-safe Entity IDs.
- [x] Mark shared state variables as `volatile` (`healthPoints`, `isPaused`, etc.).
- [x] Implement thread-safe `CombatLog`.
- [x] Synchronize `MatrixManager` access.
- [x] Refactor `GameBoard` to use typed `ArrayList<Zombie>`.

## Phase 2: Decoupling & State Management [COMPLETED]
### 1. Extract GameState
- [x] Create `GameLogic.GameState` to hold all reactive variables (was already present but unused).
- [x] Integrate `GameState` into `GameManager` — all state accessed through `GameState` instead of raw fields.
- [x] `SidePanel` implements `PropertyChangeListener` and auto-updates on state changes.

### 2. Extract Specialized Managers
- [x] **CombatManager**: Extracted all combat logic (`processCombat()`, attacks, explosions, healing, targeting) from `GameManager`.
- [x] **PlacementManager**: Extracted defense placement/sell logic and `MatrixManager` interaction.
- [x] **WaveOrchestrator**: Replaces old `WaveManager` — handles full wave lifecycle (generation → spawn timer → cleanup), owns the spawn timer.

### 3. Event-Driven UI
- [x] `SidePanel` listens to `GameState` property changes via `PropertyChangeListener`.
- [x] Removed circular `GameManager` reference from `MatrixManager` (now uses `SidePanel` directly).

## Phase 3: Performance & Extensibility [PENDING]
- [ ] **Thread Pooling**: Replace `new Thread()` in every Entity with a `ScheduledExecutorService`.
- [ ] **Entity Factory**: Centralize entity creation (DONE — `EntityFactory` created in this refactor).
- [ ] **Spatial Partitioning**: Optimize collision/range detection (currently O(N^2)) for large waves.
