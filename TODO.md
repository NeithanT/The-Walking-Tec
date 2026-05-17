# The Walking TEC - Refactoring TODO

This document tracks the architectural improvements for the game, moving from a monolithic `GameManager` to a decoupled, maintainable structure.

## Phase 1: Concurrency & Stability [COMPLETED]
- [x] Use `AtomicInteger` for thread-safe Entity IDs.
- [x] Mark shared state variables as `volatile` (`healthPoints`, `isPaused`, etc.).
- [x] Implement thread-safe `CombatLog`.
- [x] Synchronize `MatrixManager` access.
- [x] Refactor `GameBoard` to use typed `ArrayList<Zombie>`.

## Phase 2: Decoupling & State Management
### 1. Extract GameState
- [ ] Create `GameLogic.GameState` to hold all reactive variables:
    - `level`, `baseHealth`, `coins`
    - `isPaused`, `roundActive`, `waveGenerated`
    - `zombiesRemaining`, `totalZombiesInWave`
- [ ] Implement `GameStateObserver` or use `PropertyChangeListener` to notify UI of state changes (e.g., updating coin labels without `GameManager` calling the UI directly).

### 2. Extract Specialized Managers
- [ ] **CombatManager**: Extract all logic from `GameManager.processCombat()` and `processAttackThreaded()`.
- [ ] **PlacementManager**: Extract defense placement logic (`placeDefences`, `sellDefenseAt`) and its interaction with `MatrixManager`.
- [ ] **WaveOrchestrator**: Refine `WaveManager` to handle the full lifecycle of a wave (generation -> spawn timer -> cleanup).

### 3. Event-Driven UI
- [ ] Decouple `SidePanel` and `GameBoard` from `GameManager`.
- [ ] Use events (e.g., `OnEntitySpawn`, `OnCombatEvent`) instead of direct method calls between logic and UI.

## Phase 3: Performance & Extensibility
- [ ] **Thread Pooling**: Replace `new Thread()` in every Entity with a `ScheduledExecutorService`.
- [ ] **Entity Factory**: Centralize entity creation to simplify adding new Zombie/Defense types.
- [ ] **Spatial Partitioning**: Optimize collision/range detection (currently O(N^2)) for large waves.
