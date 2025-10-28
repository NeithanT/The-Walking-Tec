package GameLogic;

import Defense.Defense;
import Defense.DefenseType;
import Table.GameBoard;
import Zombie.Zombie;
import java.util.Random;

final class ZombieMovementController {

    private static final double FRAME_DELTA_TIME = 0.016; // Approx 60 FPS

    private final GameManager gameManager;

    ZombieMovementController(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    /**
     * Move zombie towards a specific target (defense)
     */
    void moveZombieTowardsTarget(Zombie zombie, int targetRow, int targetColumn) {
        if (zombie == null || !zombie.isAlive() || gameManager.isGamePaused()) {
            return;
        }

        if (zombie.getCurrentRow() < 0 || zombie.getCurrentColumn() < 0) {
            spawnZombieAtEdge(zombie);
        }

        int currentRow = zombie.getCurrentRow();
        int currentColumn = zombie.getCurrentColumn();
        int zombieTargetRow = zombie.getTargetRow();
        int zombieTargetColumn = zombie.getTargetColumn();

        boolean needsNewTarget = (zombieTargetRow == currentRow && zombieTargetColumn == currentColumn)
                || (zombieTargetRow < 0 || zombieTargetColumn < 0);

        if (needsNewTarget) {
            int[] nextCell = calculateNextCellTowards(zombie, currentRow, currentColumn, targetRow, targetColumn);
            zombie.setTargetRow(nextCell[0]);
            zombie.setTargetColumn(nextCell[1]);
        }

        GameBoard board = gameManager.getBoard();
        if (board == null) {
            return;
        }

        double targetPixelX = board.cellToPixelX(zombie.getTargetColumn());
        double targetPixelY = board.cellToPixelY(zombie.getTargetRow());

        zombie.moveTowardsTarget(targetPixelX, targetPixelY, FRAME_DELTA_TIME);
    }

    void moveZombieTowardsLifeTree(Zombie zombie) {
        if (zombie == null || !zombie.isAlive() || gameManager.isGamePaused()) {
            return;
        }

        if (isLifeTreeUnavailable()) {
            zombie.setAlive(false);
            return;
        }

        if (zombie.getCurrentRow() < 0 || zombie.getCurrentColumn() < 0) {
            spawnZombieAtEdge(zombie);
        }

        int currentRow = zombie.getCurrentRow();
        int currentColumn = zombie.getCurrentColumn();
        int targetRow = zombie.getTargetRow();
        int targetColumn = zombie.getTargetColumn();

        boolean needsNewTarget = (targetRow == currentRow && targetColumn == currentColumn)
                || (targetRow < 0 || targetColumn < 0);

        if (needsNewTarget) {
            int[] nextCell = calculateNextCellTowardsLifeTree(zombie, currentRow, currentColumn);
            zombie.setTargetRow(nextCell[0]);
            zombie.setTargetColumn(nextCell[1]);
        }

        GameBoard board = gameManager.getBoard();
        if (board == null) {
            return;
        }

        double targetPixelX = board.cellToPixelX(zombie.getTargetColumn());
        double targetPixelY = board.cellToPixelY(zombie.getTargetRow());

        zombie.moveTowardsTarget(targetPixelX, targetPixelY, FRAME_DELTA_TIME);

        if (currentRow == gameManager.getLifeTreeRow() && currentColumn == gameManager.getLifeTreeColumn()) {
            gameManager.zombieReachedLifeTree(zombie);
        }
    }

    private boolean isLifeTreeUnavailable() {
        return gameManager.getLifeTreeRow() < 0
                || gameManager.getLifeTreeColumn() < 0
                || gameManager.getLifeTreeEntity() == null
                || gameManager.getLifeTreeEntity().getHealthPoints() <= 0;
    }

    private void spawnZombieAtEdge(Zombie zombie) {
        Random rnd = gameManager.getRandomGenerator();
        GameBoard board = gameManager.getBoard();
        if (board == null) {
            return;
        }

        int row = -1;
        int column = -1;
        int attempts = 0;
        int maxAttempts = 50; // Evitar loop infinito

        // Intentar encontrar una posición libre en el borde
        while (attempts < maxAttempts) {
            int edge = rnd.nextInt(4);
            switch (edge) {
                case 0 -> { // Borde superior
                    row = rnd.nextInt(2);
                    column = rnd.nextInt(25);
                }
                case 1 -> { // Borde derecho
                    row = rnd.nextInt(25);
                    column = 23 + rnd.nextInt(2);
                }
                case 2 -> { // Borde inferior
                    row = 23 + rnd.nextInt(2);
                    column = rnd.nextInt(25);
                }
                default -> { // Borde izquierdo
                    row = rnd.nextInt(25);
                    column = rnd.nextInt(2);
                }
            }
            
            // Verificar si la posición está libre
            if (!isZombieAt(row, column)) {
                break; // Posición encontrada
            }
            
            attempts++;
        }
        
        // Si no encontramos una posición libre después de varios intentos,
        // usar la última generada (esto es raro pero evita quedarse en loop)
        if (attempts >= maxAttempts) {
            if (gameManager.getSidePanel() != null) {
                gameManager.getSidePanel().appendLog("Warning: Could not find free spawn position after " + maxAttempts + " attempts");
            }
        }

        double pixelX = board.cellToPixelX(column);
        double pixelY = board.cellToPixelY(row);

        zombie.setSpawnPosition(row, column, pixelX, pixelY);
    }

    private int[] calculateNextCellTowardsLifeTree(Zombie zombie, int currentRow, int currentColumn) {
        // NUEVO: Primero buscar si hay una defensa en rango para atacar
        Defense nearestDefense = findNearestDefenseInRange(zombie, currentRow, currentColumn);
        
        int targetRow;
        int targetColumn;
        
        if (nearestDefense != null) {
            // Si hay una defensa cercana, ir hacia ella
            targetRow = nearestDefense.getCurrentRow();
            targetColumn = nearestDefense.getCurrentColumn();
            
            // Movement tracking removed - spam logs disabled
        } else {
            // Si no, ir hacia el Life Tree
            targetRow = gameManager.getLifeTreeRow();
            targetColumn = gameManager.getLifeTreeColumn();
        }

        int rowDiff = targetRow - currentRow;
        int colDiff = targetColumn - currentColumn;

        if (rowDiff == 0 && colDiff == 0) {
            return new int[]{currentRow, currentColumn};
        }

        int nextRow = currentRow;
        int nextColumn = currentColumn;

        if (Math.abs(rowDiff) > Math.abs(colDiff)) {
            if (rowDiff > 0) {
                nextRow = currentRow + 1;
            } else if (rowDiff < 0) {
                nextRow = currentRow - 1;
            }

            if (!isValidZombieMove(nextRow, nextColumn)) {
                nextRow = currentRow;
                if (colDiff > 0) {
                    nextColumn = currentColumn + 1;
                } else if (colDiff < 0) {
                    nextColumn = currentColumn - 1;
                }
            }
        } else {
            if (colDiff > 0) {
                nextColumn = currentColumn + 1;
            } else if (colDiff < 0) {
                nextColumn = currentColumn - 1;
            }

            if (!isValidZombieMove(nextRow, nextColumn)) {
                nextColumn = currentColumn;
                if (rowDiff > 0) {
                    nextRow = currentRow + 1;
                } else if (rowDiff < 0) {
                    nextRow = currentRow - 1;
                }
            }
        }

        if (!isValidZombieMove(nextRow, nextColumn)) {
            nextRow = currentRow + (rowDiff > 0 ? 1 : rowDiff < 0 ? -1 : 0);
            nextColumn = currentColumn + (colDiff > 0 ? 1 : colDiff < 0 ? -1 : 0);
        }

        if (!isValidZombieMove(nextRow, nextColumn)) {
            nextRow = currentRow;
            nextColumn = currentColumn;
        }

        return new int[]{nextRow, nextColumn};
    }
    
    /**
     * Calculate next cell towards a specific target (used for targeting defenses)
     */
    private int[] calculateNextCellTowards(Zombie zombie, int currentRow, int currentColumn, int targetRow, int targetColumn) {
        int rowDiff = targetRow - currentRow;
        int colDiff = targetColumn - currentColumn;

        if (rowDiff == 0 && colDiff == 0) {
            return new int[]{currentRow, currentColumn};
        }

        int nextRow = currentRow;
        int nextColumn = currentColumn;

        // Prioritize movement based on larger difference
        if (Math.abs(rowDiff) > Math.abs(colDiff)) {
            if (rowDiff > 0) {
                nextRow = currentRow + 1;
            } else if (rowDiff < 0) {
                nextRow = currentRow - 1;
            }

            if (!isValidZombieMove(nextRow, nextColumn)) {
                nextRow = currentRow;
                if (colDiff > 0) {
                    nextColumn = currentColumn + 1;
                } else if (colDiff < 0) {
                    nextColumn = currentColumn - 1;
                }
            }
        } else {
            if (colDiff > 0) {
                nextColumn = currentColumn + 1;
            } else if (colDiff < 0) {
                nextColumn = currentColumn - 1;
            }

            if (!isValidZombieMove(nextRow, nextColumn)) {
                nextColumn = currentColumn;
                if (rowDiff > 0) {
                    nextRow = currentRow + 1;
                } else if (rowDiff < 0) {
                    nextRow = currentRow - 1;
                }
            }
        }

        if (!isValidZombieMove(nextRow, nextColumn)) {
            nextRow = currentRow + (rowDiff > 0 ? 1 : rowDiff < 0 ? -1 : 0);
            nextColumn = currentColumn + (colDiff > 0 ? 1 : colDiff < 0 ? -1 : 0);
        }

        if (!isValidZombieMove(nextRow, nextColumn)) {
            nextRow = currentRow;
            nextColumn = currentColumn;
        }

        return new int[]{nextRow, nextColumn};
    }
    
    /**
     * Busca la defensa más cercana que esté dentro del rango de detección del zombie
     * y que el zombie pueda atacar según las reglas de vuelo
     * @param zombie El zombie que busca objetivos
     * @param zombieRow Fila actual del zombie
     * @param zombieCol Columna actual del zombie
     * @return La defensa más cercana o null si no hay ninguna en rango
     */
    private Defense findNearestDefenseInRange(Zombie zombie, int zombieRow, int zombieCol) {
        // Zombies should detect defenses at a range greater than their attack range
        // to give them time to path towards the target
        // Use zombie's attack range + 2 for detection (or minimum of 3)
        final int DETECTION_RANGE = Math.max(zombie.getAttackRange() + 2, 3);
        
        Defense nearestDefense = null;
        int minDistance = Integer.MAX_VALUE;
        
        GameBoard board = gameManager.getBoard();
        if (board == null) {
            return null;
        }
        
        // Buscar en todas las defensas del tablero
        for (Table.PlacedDefense placedDef : board.getDefenses()) {
            if (placedDef == null || placedDef.definition == null) {
                continue;
            }
            
            Defense defense = placedDef.definition;
            
            if (defense.getHealthPoints() <= 0) {
                continue;
            }
            
            // Ignorar el Life Tree - siempre ir al árbol al final
            if ("LIFE TREE".equalsIgnoreCase(defense.getEntityName())) {
                continue;
            }
            
            // Use CombatRules to check if zombie can attack this defense
            // This handles FLYING rules automatically
            // For pathfinding, we only need to check basic compatibility
            // The actual attack will be validated by CombatRules.canAttack() later
            
            int defRow = defense.getCurrentRow();
            int defCol = defense.getCurrentColumn();
            
            // Calcular distancia Chebyshev (la misma que usa el combate)
            int distance = Math.max(Math.abs(defRow - zombieRow), Math.abs(defCol - zombieCol));
            
            // Si está en rango de detección y es más cercana que la anterior
            if (distance <= DETECTION_RANGE && distance < minDistance) {
                // Check if zombie could potentially attack this defense
                // using temporary positions for the check
                int originalZombieRow = zombie.getCurrentRow();
                int originalZombieCol = zombie.getCurrentColumn();
                zombie.setCurrentRow(zombieRow);
                zombie.setCurrentColumn(zombieCol);
                
                boolean canTarget = CombatRules.canAttack(zombie, defense) || 
                                   distance <= zombie.getAttackRange();
                
                zombie.setCurrentRow(originalZombieRow);
                zombie.setCurrentColumn(originalZombieCol);
                
                if (canTarget || distance <= 2) {  // Always path towards very close defenses
                    nearestDefense = defense;
                    minDistance = distance;
                }
            }
        }
        
        return nearestDefense;
    }

    private boolean isValidZombieMove(int row, int column) {
        // Verificar que la posición esté dentro del tablero
        if (!gameManager.getMatrixManager().isValidPosition(row, column)) {
            return false;
        }
        
        // Verificar que no haya una defensa en esa posición
        if (gameManager.getMatrixManager().isOccupied(row, column)) {
            return false;
        }
        
        // Verificar que no haya otro zombie en esa posición
        if (isZombieAt(row, column)) {
            return false;
        }
        
        return true;
    }
    
    private boolean isZombieAt(int row, int column) {
        for (Zombie z : gameManager.getWaveZombiesInternal()) {
            if (z != null && z.isAlive()) {
                int zRow = z.getCurrentRow();
                int zCol = z.getCurrentColumn();
                
                // Verificar posición actual
                if (zRow == row && zCol == column) {
                    return true;
                }
                
                // También verificar posición objetivo (target) para evitar que dos zombies
                // intenten moverse a la misma casilla al mismo tiempo
                int zTargetRow = z.getTargetRow();
                int zTargetCol = z.getTargetColumn();
                if (zTargetRow == row && zTargetCol == column) {
                    return true;
                }
            }
        }
        return false;
    }
}
