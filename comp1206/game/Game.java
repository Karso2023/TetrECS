package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.scene.ScoreScene;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    /**
     * Use logger to debug
     */
    private static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Use random to spawn piece
     */
    private final Random random = new Random();

    /**
     * nextLoop scheduler
     */
    private  ScheduledFuture<?> nextLoop;

    /**
     * GameLoopListener
     */
    private GameLoopListener gameLoopListener = null;

    /**
     * executorService
     */
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    /**
     * NextPieceListener field
     */
    private List<NextPieceListener> nextPieceListener = new ArrayList<>();

    /**
     * lineClearedListener
     */
    private LineClearedListener lineClearedListener = null;

    /**
     * gameWindow
     */
    private GameWindow gameWindow;

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    /**
     * gamePiece for currentPiece
     */
    private GamePiece currentPiece;

    /**
     * gamePiece for followingPiece
     */
    private GamePiece followingPiece;

    /**
     * Bindable game properties
     * 0 score
     */
    protected final SimpleIntegerProperty scores = new SimpleIntegerProperty(0);

    /**
     * Bindable game properties
     * level 0
     */
    protected final SimpleIntegerProperty level = new SimpleIntegerProperty(0);

    /**
     * Bindable game properties
     * 3 lives
     */
    protected final SimpleIntegerProperty lives = new SimpleIntegerProperty(3);

    /**
     * Bindable game properties
     * 1 x multiplier
     */
    protected final SimpleIntegerProperty multiplier = new SimpleIntegerProperty(1);

    //for level up sound effect
    private int n = 0;
    protected Game game;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows, GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        this.cols = cols;
        this.rows = rows;
        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
        startGameLoop();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");

        //Initialise followingPiece at the start of the game.
        followingPiece = spawnPiece();

    }

    /**
     * Stop the game
     */
    public static void stop() {
        logger.info("Stopping the game !");
        //From oracle.com
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.shutdownNow();
    }

    /**
     * Set NextPieceListener
     * @param listener nextPieceListener
     */
    public void setNextPieceListener(NextPieceListener listener) {
        nextPieceListener.add(listener);
    }

    /**
     * Replace the current piece with a new piece
     * @return currentPiece
     */
    public GamePiece nextPiece(){
            currentPiece = followingPiece;
            followingPiece = spawnPiece();
            for (NextPieceListener listener : nextPieceListener) {
                listener.nextPiece(currentPiece);
            }
            logger.info("The current piece is {}", currentPiece);
            logger.info("Next Piece: {}", followingPiece);
            return currentPiece;

    }

    /**
     * Return a GamePiece
     * @return piece
     */
    public GamePiece spawnPiece(){
        var maxPieces = GamePiece.PIECES;
        var randomPiece = random.nextInt(maxPieces);
        logger.info("Picking random piece: {} ", randomPiece);
        var piece = GamePiece.createPiece(randomPiece);
        return piece;
    }


    /**
     * Get spawn place
     * @return gamePiece
     */
    public GamePiece getSpawnPiece() {
        return followingPiece;
    }

    /**
     * Clear any full vertical/horizontal lines that have been made
     */
    public void afterPiece(){
        //Store number of lines to clear
        int lines =0;
        //blocks to clear
        HashSet<GameBlockCoordinate> clearBlocks = new HashSet<>();

        //I will separate rows and columns into different methods to make the code clear.
        lines += clearRows(lines, clearBlocks);
        lines = clearColumns(lines, clearBlocks);
        score(lines, clearBlocks);
    }

    /**
     * Clear rows
     * @param lines store number of lines to clear
     * @param clearBlocks blocks to clear
     * @return lines
     */
    public int clearRows(int lines, HashSet<GameBlockCoordinate> clearBlocks) {
        for (int y = 0; y < getRows(); y++){
            boolean isRowFull = true;
            for(int x = 0; x < cols; x++){
                if (grid.get(x,y) == 0){
                    isRowFull = false;
                    break;
                }
            }

            if(isRowFull){
                logger.info("Clearing rows");
                clearColumns(lines, clearBlocks);
                lines++;
                for (int x = 0; x<getCols();x++){
                    grid.set(x,y,0);
                    logger.info("Playing clear row sound effect");
                    Multimedia.playAudioFile("clear.wav");
                    clearBlocks.add(new GameBlockCoordinate(x,y));
                }
            }
        }
        return lines;
    }

    /**
     * Clear columns
     * @param lines store number of lines to clear
     * @param clearBlocks blocks to clear
     * @return lines
     */
    public int clearColumns(int lines, HashSet<GameBlockCoordinate> clearBlocks){
        for(int x = 0; x < getCols(); x++){
            boolean isColFull = true;
            for(int y = 0; y < rows; y++){
                if(grid.get(x,y) == 0){
                    isColFull = false;
                    break;
                }
            }

            if(isColFull){
                logger.info("Clearing column");
                lines++;
                for(int y = 0 ; y < getRows() ; y++){
                    grid.set(x,y,0);
                    logger.info("Playing clear column sound effect");
                    Multimedia.playAudioFile("clear.wav");
                    clearBlocks.add(new GameBlockCoordinate(x,y));
                }
            }
        }
        return lines;
    }

    /**
     * Add a score based on the following formula
     * @param lines number of lines
     * @param clearBlocks number of blocks
     */
    public void score(int lines, HashSet<GameBlockCoordinate> clearBlocks){
        int scoreFormula = lines * clearBlocks.size() * 10 * multiplier.get();
        if(lines > 0){
            scores.set(getScores() + scoreFormula);
            //Multiplier
            multiplier.set(getMultiplier() + 1);
            levelUP();
        }
        else {
            //If player can't do combo, reset to default value
            multiplier.set(1);
        }

        if (lineClearedListener != null){
            lineClearedListener.lineCleared(clearBlocks);
        }
    }

    /**
     * LevelUp every 1000
     */
    public void levelUP(){
        level.set(scores.get()/1000);
        if(getLevel() > 0 && getLevel() == n+1){
            Multimedia.playAudioFile("level.wav");
            n++;
        }
    }

    /**
     * Swap the current and following pieces
     */
    public void swapCurrentPiece(){
        logger.info("Swapping piece !");
        GamePiece tmp = currentPiece;
        currentPiece = followingPiece;
        followingPiece = tmp;

        for (NextPieceListener listener : nextPieceListener) {
            listener.nextPiece(currentPiece);
        }

    }

    /**
     * Rotate current piece
     */
    public void rotateCurrentPiece(){
        logger.info("Rotating current piece !");
        Multimedia.playAudioFile("rotate.wav");

        currentPiece.rotate(9);

    }

    /**
     * reverse rotate current piece
     */
    public void reversedRotateCurrentPiece(){
        logger.info("Reverse rotating current piece !");
        Multimedia.playAudioFile("rotate.wav");

        currentPiece.rotateReversed(9);

    }

    /**
     * Stop the game and go back to start menu.
     */
    public void stopGame(){
        try {
            stopGameLoop();
            ScoreScene scoreScene = new ScoreScene(gameWindow);
            scoreScene.addScore(new Pair<>("Karso", scores.get()));
            scoreScene.saveCurrentScores();
            stop();
            gameWindow.startScore();
        } catch (Exception e){
            logger.error("Fail to stop the game, {}", e.toString());
        }

    }

    /**
     * Start game loop
     */
    public void startGameLoop() {
        nextLoop = executorService.schedule(this::gameLoop, getTimerDelay(), TimeUnit.MILLISECONDS);
        if(gameLoopListener != null) {
            gameLoopListener.gameLoopListener(getTimerDelay());
        }
    }

    /**
     * When gameLoop fires (the timer reaches 0): lose a life,
     * the current piece is discarded and the timer restarts.
     * The multiplier is set back to 1.
     */
    public void gameLoop() {
        logger.info("Wait what the gameLoop() is working ??? Am i dreaming ???");
        if(multiplier.get() > 1) {
            logger.info("Reset multiplier : {}", getMultiplier());
            multiplier.set(1);
        }

        if(lives.get() > 0) {
            logger.info("Current lives: {}", lives.get());
            executorService.schedule(() -> {
                Platform.runLater(() -> {
                    // Any modification to JavaFX properties or UI components
                    lives.set(lives.get() - 1);
                });
            }, 1, TimeUnit.MILLISECONDS);
            logger.info("Lives now : {}", getLives());
            Multimedia.playAudioFile("lifelose.wav");
            int timerDelay = getTimerDelay();

            if(gameLoopListener != null) {
                gameLoopListener.gameLoopListener(timerDelay);
            }

            nextLoop = executorService.schedule(this::gameLoop, timerDelay, TimeUnit.MILLISECONDS);
        }
        else {
            logger.info("Please shutdown the game pleaseeeeeeeeeeeeee");
            stopGameLoop();
            Platform.runLater(this::stopGame);
        }

        nextPiece();

    }


    /**
     * Restart game loop
     */
    public void restartGameLoop() {
        logger.info("restarting game loop !");
        nextLoop.cancel(false);
            startGameLoop();
    }


    /**
     * Fpr restart game loop
     * @param gameBlock gameBlock
     * @return true
     */
    public boolean blockAction(GameBlock gameBlock) {
        int x = gameBlock.getX();
        int y = gameBlock.getY();
        logger.info("Block clicked: {},{}", Integer.valueOf(x), Integer.valueOf(y));
        if (this.currentPiece == null) {
            logger.error("No current piece");
            return false;
        }
        nextPiece();
        return true;
    }


    /**
     * Stop game loop
     */
    public void stopGameLoop() {
        if (nextLoop != null && !nextLoop.isCancelled()) {
            nextLoop.cancel(true);
        }
        Platform.runLater(executorService::shutdownNow);

    }



    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();

        //Get the new value for this block
        if(grid.canPlayPiece(currentPiece,x,y)) {
            logger.info("Playing place music");
            Multimedia.playAudioFile("place.wav");
            grid.playPiece(currentPiece, x, y);
            restartGameLoop();
            afterPiece();
            nextPiece();

        } else{
            logger.info("Playing fail music");
            Multimedia.playAudioFile("fail.wav");
            logger.error("Cannot play the piece at {} {}", x,y);
        }
    }

    /**
     * LineClearedListener
     * @param lineClearedListener lineClearedListener
     */
    public void setLineClearedListener(LineClearedListener lineClearedListener) {
        this.lineClearedListener = lineClearedListener;
    }

    /**
     * Set gane loop listener
     * @param gameLoopListener gameLoopListener
     */
    public void setOnGameLoopListener(GameLoopListener gameLoopListener) {
        this.gameLoopListener = gameLoopListener;
    }

    /**
     *  Calculate the delay at the maximum of either 2500 milliseconds
     *  or 12000 - 500 * the current level
     * @return startDelay
     */
    public int getTimerDelay() {
        int startDelay = 12000;
        int maxDelay = 2500;
            startDelay = startDelay - 500 * getLevel();
        return Math.max(startDelay, maxDelay);
    }



    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Get scores
     * @return scores
     */
    public int getScores() {
        return scores.get();
    }

    /**
     * Get scores (For UI)
     * @return scores
     */
    public IntegerProperty scoresProperty() {
        return scores;
    }

    /**
     * Get level
     * @return level
     */
    public int getLevel() {
        return level.get();
    }

    /**
     * Get level (for UI)
     * @return level
     */
    public IntegerProperty levelProperty() {
        return level;
    }

    /**
     * Get lives
     * @return lives
     */
    public int getLives() {
        return lives.get();
    }

    /**
     * Get lives (For UI)
     * @return lives
     */
    public IntegerProperty livesProperty() {
        return lives;
    }

    /**
     * Get multiplier
     * @return multiplier
     */
    public int getMultiplier() {
        return multiplier.get();
    }

    /**
     * Get multiplier (for UI)
     * @return multiplier
     */
    public IntegerProperty multiplierProperty() {
        return multiplier;
    }

    /**
     * Get current piece
     * @return currentPiece
     */
    public GamePiece getCurrentPiece() {
        return currentPiece;
    }

    /**
     * Get following piece
     * @return followingPiece
     */
    public GamePiece getFollowingPiece() {
        return followingPiece;
    }
}
