# The-Walking-Tec

A zombie-themed Tower Defense game built with Java and Swing. Players place various types of defenses on a 25x25 grid to fend off waves of attacking zombies. The project features a customizable entity system and persistent game state.

## Project Overview

*   **Main Technologies:** Java 24, Maven, Swing.
*   **Architecture:**
    *   **Configuration (`Configuration/`):** Manages persistence and loading of game data (zombies, defenses, levels, and admins) using object serialization.
    *   **Game Logic (`GameLogic/`):** Orchestrates game state, waves, combat, and movement.
    *   **UI (`Table/`, `MainMenu/`, `Vanity/`):** Custom Swing components for the game board, side panels, and menus.
    *   **Entities (`Entity/`, `Zombie/`, `Defense/`):** Hierarchical structure for game objects with specialized behaviors (Attacker, Healer, Explosive, Flying, etc.).

## Building and Running

*   **Build:** `mvn clean install`
*   **Run:** `mvn exec:java` (Main class: `MainMenu.MainMenu`)
*   **Main Entry Point:** `TheWalkingTec/src/main/java/MainMenu/MainMenu.java`

## Key Packages & Files

*   `GameLogic/GameManager.java`: The central controller for game state and timers.
*   `Table/GameBoard.java`: The 25x25 grid where combat and rendering take place.
*   `Configuration/ConfigManager.java`: Handles loading/saving data to `.data` files.
*   `src/main/resources/assets/`: Contains image assets for the game.
*   `src/main/resources/Saves/`: Directory for persistent data files (`defenses.data`, `zombies.data`, etc.).

## Development Conventions

*   **Game Loop:** Uses `javax.swing.Timer` for periodic updates (game ticks, combat, spawning).
*   **Thread Safety:** Employs `ReentrantLock` for synchronizing access to shared collections (e.g., in `GameManager` and `GameBoard`).
*   **UI Layout:** Uses NetBeans `AbsoluteLayout` for precise component positioning in some panels.
*   **Custom Components:** Rounded buttons and panels are available in the `Vanity` package for a polished UI.
*   **Serialization:** Most entity classes must implement `Serializable` to support the persistence system.

## Key Development Tasks (TODO)

*   [ ] Improve the sparse `README.md`.
*   [ ] Optimize collision and combat logic for large numbers of entities.
*   [ ] Extend the `CombatLog` for better post-game analytics.
