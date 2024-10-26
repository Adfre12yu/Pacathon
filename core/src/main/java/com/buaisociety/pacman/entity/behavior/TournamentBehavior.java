package com.buaisociety.pacman.entity.behavior;

import com.buaisociety.pacman.Searcher;
import com.buaisociety.pacman.entity.Direction;
import com.buaisociety.pacman.entity.Entity;
import com.buaisociety.pacman.entity.PacmanEntity;
import com.buaisociety.pacman.maze.Tile;
import com.buaisociety.pacman.maze.TileState;
import com.cjcrafter.neat.compute.Calculator;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TournamentBehavior implements Behavior {

    private final Calculator calculator;
    private @Nullable PacmanEntity pacman;

    private int previousScore = 0;
    private int framesSinceScoreUpdate = 0;

    public TournamentBehavior(Calculator calculator) {
        this.calculator = calculator;
    }

    /**
     * Returns the desired direction that the entity should move towards.
     *
     * @param entity the entity to get the direction for
     * @return the desired direction for the entity
     */
    @NotNull
    @Override
    public Direction getDirection(@NotNull Entity entity) {
        // --- DO NOT REMOVE ---
        if (pacman == null) {
            pacman = (PacmanEntity) entity;
        }

        int newScore = pacman.getMaze().getLevelManager().getScore();
        if (previousScore != newScore) {
            previousScore = newScore;
            framesSinceScoreUpdate = 0;
        } else {
            framesSinceScoreUpdate++;
        }

        if (framesSinceScoreUpdate > 60 * 40) {
            pacman.kill();
            framesSinceScoreUpdate = 0;
        }
        // --- END OF DO NOT REMOVE ---

        // TODO: Put all your code for info into the neural network here


        

        Direction forward = pacman.getDirection();
        Direction left = pacman.getDirection().left();
        Direction right = pacman.getDirection().right();
        Direction behind = pacman.getDirection().behind();
        float randomNumber = 0;
        boolean canMoveForward = pacman.canMove(forward);
        boolean canMoveLeft = pacman.canMove(left);
        boolean canMoveRight = pacman.canMove(right);
        boolean canMoveBehind = pacman.canMove(behind);

        float numPelOnF = 0;
        float numPelOnB = 0;
        float numPelOnR = 0;
        float numPelOnL = 0;

        
        // Input nodes 1, 2, 3, and 4 show if the pacman can move in the forward, left, right, and behind directions
        



        for (int i = 0; i < 100; i++) {
            TileState tState = pacman.getMaze()
                    .getTile(pacman.getTilePosition().x + (forward.getDx() * i),
                            pacman.getTilePosition().y + (forward.getDy() * i))
                    .getState();
            if (tState.equals(TileState.WALL) || tState.equals(TileState.GHOST_PEN)) {
                break;
            } else if (tState
                    .equals(TileState.PELLET)) {
                numPelOnF++;
            }
        }
        for (int i = 0; i < 100; i++) {
            TileState tState = pacman.getMaze()
                .getTile(pacman.getTilePosition().x + (behind.getDx() * i), pacman.getTilePosition().y + (behind.getDy() * i)).getState();
            if(
                    tState.equals(TileState.WALL) || tState.equals(TileState.GHOST_PEN)) {
                break;
            }
            else if(tState
                    .equals(TileState.PELLET)) {
                numPelOnB++;
            }
        }
        for (int i = 0; i < 100; i++) {
            TileState tState = pacman.getMaze()
                    .getTile(pacman.getTilePosition().x + (right.getDx() * i),
                            pacman.getTilePosition().y + (right.getDy() * i))
                    .getState();
            if (
                    tState.equals(TileState.WALL) || tState.equals(TileState.GHOST_PEN))

    {
                break;
            } else if (tState
                    .equals(TileState.PELLET)) {
                numPelOnR++;
            }
        }
        for (int i = 0; i < 100; i++) {
            TileState tState = pacman.getMaze()
                    .getTile(pacman.getTilePosition().x + (left.getDx() * i),
                            pacman.getTilePosition().y + (left.getDy() * i))
                    .getState();
            if (
                    tState.equals(TileState.WALL) || tState.equals(TileState.GHOST_PEN)) {
                break;
            } else if (tState
                    .equals(TileState.PELLET)) {
                numPelOnL++;
            }
        }
        
        boolean pelOnForw = pacman.getMaze()
                .getTile(pacman.getTilePosition().x + forward.getDx(), pacman.getTilePosition().y + forward.getDy()).getState()
                .equals(TileState.PELLET);
        boolean pelOnBehind = pacman.getMaze()
                .getTile(pacman.getTilePosition().x + behind.getDx(), pacman.getTilePosition().y + behind.getDy()).getState()
                .equals(TileState.PELLET);
        boolean pelOnRight = pacman.getMaze()
                .getTile(pacman.getTilePosition().x + right.getDx(), pacman.getTilePosition().y + right.getDy()).getState()
                .equals(TileState.PELLET);
        boolean pelOnLeft = pacman.getMaze()
                .getTile(pacman.getTilePosition().x + left.getDx(), pacman.getTilePosition().y + left.getDy()).getState()
                .equals(TileState.PELLET);
        

        Tile currentTile = pacman.getMaze().getTile(pacman.getTilePosition());
        Map<Direction, Searcher.SearchResult> nearestPellets = Searcher.findTileInAllDirections(currentTile, tile -> tile.getState() == TileState.PELLET);

        int maxDistance = -1;
        for (Searcher.SearchResult result : nearestPellets.values()) {
            if (result != null) {
                maxDistance = Math.max(maxDistance, result.getDistance());
            }
        }

        float nearestPelletForward = nearestPellets.get(forward) != null ? 1 - (float) nearestPellets.get(forward).getDistance() / maxDistance : 0;
        float nearestPelletLeft = nearestPellets.get(left) != null ? 1 - (float) nearestPellets.get(left).getDistance() / maxDistance : 0;
        float nearestPelletRight = nearestPellets.get(right) != null ? 1 - (float) nearestPellets.get(right).getDistance() / maxDistance : 0;
        float nearestPelletBehind = nearestPellets.get(behind) != null ? 1 - (float) nearestPellets.get(behind).getDistance() / maxDistance : 0;


        float[] inputs = new float[] {
            canMoveForward ? 1f : 0f,
            canMoveLeft ? 1f : 0f,
            canMoveRight ? 1f : 0f,
                canMoveBehind ? 1f : 0f, 
                pelOnForw ? 2f : 0f,
            pelOnLeft ? 2f : 0f,
            pelOnBehind ? 2f : 0f,
                pelOnRight ? 2f : 0f,
                randomNumber,
                numPelOnB,
                numPelOnF,
                numPelOnL,
                numPelOnR,
                nearestPelletBehind,
                nearestPelletForward,
                nearestPelletLeft,
                nearestPelletRight,
        };
        float[] outputs = calculator.calculate(inputs).join();

        // Chooses the maximum output as the direction to go... feel free to change this ofc!
        // Adjust this to whatever you used in the NeatPacmanBehavior.class
        int index = 0;
        float max = outputs[0];
        for (int i = 1; i < outputs.length; i++) {
            if (outputs[i] > max) {
                max = outputs[i];
                index = i;
            }
        }

        return switch (index) {
            case 0 -> pacman.getDirection();
            case 1 -> pacman.getDirection().left();
            case 2 -> pacman.getDirection().right();
            case 3 -> pacman.getDirection().behind();
            default -> throw new IllegalStateException("Unexpected value: " + index);
        };
    }
}
