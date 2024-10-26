package com.buaisociety.pacman.entity.behavior;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.buaisociety.pacman.maze.Maze;
import com.buaisociety.pacman.maze.Tile;
import com.buaisociety.pacman.maze.TileState;
import com.buaisociety.pacman.sprite.DebugDrawing;
import com.cjcrafter.neat.Client;
import com.buaisociety.pacman.Searcher;
import com.buaisociety.pacman.entity.Direction;
import com.buaisociety.pacman.entity.Entity;
import com.buaisociety.pacman.entity.PacmanEntity;

import java.util.concurrent.ThreadLocalRandom;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

public class NeatPacmanBehavior implements Behavior {

    private final @NotNull Client client;
    private @Nullable PacmanEntity pacman;

    // Score modifiers help us maintain "multiple pools" of points.
    // This is great for training, because we can take away points from
    // specific pools of points instead of subtracting from all.
    private int scoreModifier = 0;

    private int lastScore = 0;
    Vector2i pacPos;
    private int updatesSinceScoreInc = 0;
    private int sinceLastPacPosUpdate =0 ;
    private float initPellets = 0f;
    private float pelsAte = 0;

    private int[][] posBeenTo = new int[29][37];

    public NeatPacmanBehavior(@NotNull Client client) {
        this.client = client;
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
        if (pacman == null) {
            pacman = (PacmanEntity) entity;
        }

        if (pacPos != pacman.getTilePosition() || pacPos != pacman.getSpawnTile()) {
            pacPos = pacman.getTilePosition();
            sinceLastPacPosUpdate = 0;
            scoreModifier += 0.2;
            // System.out.println("added points for moving");
        } else {
            scoreModifier -= 50;
            System.out.println("took off points for staying in the same pos");

        }


        if (sinceLastPacPosUpdate++ > 60 * 10) {
            pacman.kill();


            return Direction.UP;
        }
        // SPECIAL TRAINING CONDITIONS
        // TODO: Make changes here to help with your training...
        int currentScore = pacman.getMaze().getLevelManager().getScore();
        float scoreBonus = 0;

        float curPosBeenTo = posBeenTo[pacman.getTilePosition().x][pacman.getTilePosition().y]++;

        
        if (pacman.getMaze().getPelletsRemaining() > initPellets) {
            initPellets = pacman.getMaze().getPelletsRemaining();
        }
        if (currentScore > lastScore) {
            updatesSinceScoreInc = 0;
            lastScore = currentScore;
            pelsAte++;
        }

        float scoreMod = (pelsAte / initPellets) * 50;
        
        if (updatesSinceScoreInc++ > 60 * 10 * (1 +(pelsAte/initPellets))) {
            pacman.kill();


            return Direction.UP;
        }
        // END OF SPECIAL TRAINING CONDITIONS

        // We are going to use these directions a lot for different inputs. Get them all once for clarity and brevity
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
        
        
        if (numPelOnB == 0 && numPelOnF == 0 && numPelOnL == 0 && numPelOnR == 0) {
            randomNumber = ThreadLocalRandom.current().nextFloat();
            if (curPosBeenTo > 250) {
                pacman.kill();



            return Direction.UP;
            
        }
        }
        if (updatesSinceScoreInc > 40 * 10 * (1 + (pelsAte / initPellets))) {
            randomNumber = ThreadLocalRandom.current().nextFloat();
            randomNumber += 1;
            if (curPosBeenTo > 250) {
                pacman.kill();




                return Direction.UP;

            }
        }
        if (curPosBeenTo > 10) {
            scoreModifier -= 1;
            // System.out.println("Took off points for repeated square");
        } else if(curPosBeenTo < 1){
            scoreModifier += 5;
            // System.out.println("added points for new square");
        }



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



        
        float[] outputs = client.getCalculator().calculate(new float[]{
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
            
        }).join();
        

       

        
        

       
        int index = 0;
        float max = outputs[0];
        for (int i = 1; i < outputs.length; i++) {
            if (outputs[i] > max) {
                max = outputs[i];
                index = i;
            }
        }
        



        Direction newDirection = switch (index) {
            case 0 -> pacman.getDirection();
            case 1 -> pacman.getDirection().left();
            case 2 -> pacman.getDirection().right();
            case 3 -> pacman.getDirection().behind();
            default -> throw new IllegalStateException("Unexpected value: " + index);
        };
        
        


        
        client.setScore( pacman.getMaze().getLevelManager().getScore() + scoreModifier);
        


        
        return newDirection;
    }


    @Override
    
    public void render(@NotNull SpriteBatch batch) {
        // TODO: You can render debug information here
        /*
        if (pacman != null) {
            DebugDrawing.outlineTile(batch, pacman.getMaze().getTile(pacman.getTilePosition()), Color.RED);
            DebugDrawing.drawDirection(batch, pacman.getTilePosition().x() * Maze.TILE_SIZE, pacman.getTilePosition().y() * Maze.TILE_SIZE, pacman.getDirection(), Color.RED);
        }
         */
    }
}
