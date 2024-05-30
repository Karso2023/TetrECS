package uk.ac.soton.comp1206.scene;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene implements NextPieceListener{

  /**
   * logger to debug
   */
    private static final Logger logger = LogManager.getLogger(ChallengeScene.class);

  /**
   * game
   */
  protected Game game;

  protected BooleanProperty keyboardMode = (BooleanProperty)new SimpleBooleanProperty(false);

  /**
   * gameBoard
   */
  protected GameBoard gameBoard;

  /**
   * PieceBoard for next piece
   */
    protected PieceBoard nextPieceBoard;

  /**
   * PieceBoard for current piece
   */
  protected PieceBoard currentPieceBoard;

  /**
   *  GameLoopListener
   */
  protected GameLoopListener gameLoopListener;

    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        //challengePane setup
        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("menu-background");
        root.getChildren().add(challengePane);

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        //Game board setup
        gameBoard = new GameBoard(game.getGrid(), (double) gameWindow.getWidth() /2, (double) gameWindow.getWidth() /2);
        gameBoard.setGame(game);
        gameBoard.getStyleClass().add("gameBox");
        mainPane.setCenter(gameBoard);

        //Left side UI
        VBox leftUI = leftUI(mainPane);
        mainPane.setLeft(leftUI);

        //Right side UI
        VBox rightUI = rightUI(mainPane);
        mainPane.setRight(rightUI);

        //Timer Bar
        VBox timerBar = gameLoopAnimation(mainPane);
        mainPane.setBottom(timerBar);

        //Set challenge scene title
        HBox sceneTitle = new HBox();
        sceneTitle.setAlignment(Pos.TOP_CENTER);
        sceneTitle.setPadding(new Insets(20));
        sceneTitle.setSpacing(10);
        Label challengeSceneTitle = new Label("Challenge Scene");
        challengeSceneTitle.getStyleClass().add("challengeSceneTitle");
        sceneTitle.getChildren().add(challengeSceneTitle);
        mainPane.setTop(sceneTitle);

        //Handle block on game board grid being clicked
        gameBoard.setOnBlockClick(this::blockClicked);
    }


  /**
     * Score UI
     * @param mainPane Border pane
     */
    private VBox leftUI(BorderPane mainPane){
        //Create a VBox for the left side display
        VBox leftDisplay = new VBox();
        leftDisplay.setAlignment(Pos.TOP_LEFT);
        leftDisplay.setPadding(new Insets(20));
        leftDisplay.setSpacing(10);

        //Score display
        Label scoreTitle = new Label("Score");
        scoreTitle.getStyleClass().add("heading");
        Label scoreLabel = new Label();
        scoreLabel.textProperty().bind(game.scoresProperty().asString());
        scoreLabel.getStyleClass().add("score");
        //Multiplier display
        Label multiDisplay = new Label();
        multiDisplay.textProperty().bind(new SimpleStringProperty("Multiplier: X").concat(game.multiplierProperty().asString()));
        multiDisplay.getStyleClass().add("heading");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        multiDisplay.setAlignment(Pos.TOP_LEFT);
        leftDisplay.getChildren().addAll(scoreTitle,scoreLabel,spacer,multiDisplay);

        return leftDisplay;
    }

    /**
     * Use rightUI to adjust the position of lives and level UI
     * @param mainPane mainPane
     * @return rightDisplay
     */
    private VBox rightUI(BorderPane mainPane) {
        //Create a VBox for the right side display
        VBox rightDisplay = new VBox();
        rightDisplay.setAlignment(Pos.TOP_RIGHT);
        rightDisplay.setPadding(new Insets(20));
        rightDisplay.setSpacing(10);

        // Lives title and display
        Label liveTitle = new Label("Lives");
        liveTitle.getStyleClass().add("heading");
        Label livesDisplay = new Label();
        livesDisplay.textProperty().bind(game.livesProperty().asString());
        livesDisplay.getStyleClass().add("lives");

        // Level title and display
        Label levelsDisplay = new Label();
        levelsDisplay.textProperty().bind(new SimpleStringProperty("Level: ").concat(game.levelProperty().asString()));
        levelsDisplay.getStyleClass().add("heading");

        //Current piece board setup
        currentPieceBoard = new PieceBoard(3,3, (double) gameWindow.getWidth() /6, (double) gameWindow.getWidth() /6);
        currentPieceBoard.setPadding(new Insets(20));
        currentPieceBoard.setFocusTraversable(true);
        currentPieceBoard.setOnKeyPressed(event -> {
          if(event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.R) {
            swapPress();
          }
          else if(event.getCode() == KeyCode.Q || event.getCode() == KeyCode.Z || event.getCode() == KeyCode.OPEN_BRACKET) {
            reverseRotatePress();
          }
          else if(event.getCode() == KeyCode.E || event.getCode() == KeyCode.C || event.getCode() == KeyCode.CLOSE_BRACKET) {
            rotatePress();
          }
        });

        currentPieceBoard.setOnBlockClick(this::swap);


        //Next piece board setup
        nextPieceBoard = new PieceBoard(3,3, (double) gameWindow.getWidth() /10, (double) gameWindow.getWidth() /10);
        nextPieceBoard.setPadding(new Insets(20));
        nextPieceBoard.setOnBlockClick(this::swap);

        // Spacer to push the level UI towards the center
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        levelsDisplay.setAlignment(Pos.CENTER_RIGHT);
        rightDisplay.getChildren().addAll(liveTitle, livesDisplay, currentPieceBoard, nextPieceBoard, spacer, levelsDisplay);

        return rightDisplay;
    }

  /**
   * Game loop animation to show countdown UI
   * TranslateTransition is used in this method to move rectangleTimeBar
   */
  private VBox gameLoopAnimation(BorderPane mainPane) {

    Duration firstDurationDelay = Duration.millis(8000);
    VBox timeBar = new VBox();
    final Rectangle rectangleTimeBar = new Rectangle(gameWindow.getWidth(),150);
    rectangleTimeBar.setFill(Color.GREEN);

    TranslateTransition shakeTransition = new TranslateTransition(Duration.millis(game.getTimerDelay()), rectangleTimeBar);
    Timeline colorTimeline = new Timeline(new KeyFrame(Duration.millis(100), e -> {
      double currentTime = shakeTransition.getCurrentTime().toMillis();
      if (currentTime <= firstDurationDelay.toMillis()) {
        rectangleTimeBar.setFill(Color.GREEN);
      }
      else {
        rectangleTimeBar.setFill(Color.RED);
      }
    }));
    colorTimeline.setCycleCount(Timeline.INDEFINITE);
    shakeTransition.setFromX(0);
    shakeTransition.setToX(-gameWindow.getWidth());

    shakeTransition.setAutoReverse(false);
    shakeTransition.setCycleCount(TranslateTransition.INDEFINITE);

    shakeTransition.play();
    colorTimeline.play();

    timeBar.getChildren().add(rectangleTimeBar);

    return timeBar;
    }


  protected void blockAction(GameBlock gameBlock) {
    if (this.game.blockAction(gameBlock)) {
      logger.info("Placed {}", gameBlock);
      this.game.restartGameLoop();
    } else {
      logger.info("Unable to place {}", gameBlock);
    }
  }

  /**
   * Swap the current and next piece
   * @param gameBlock gameBlock
   */
  public void swap(GameBlock gameBlock) {
        logger.info("Swapped by left click");
        game.swapCurrentPiece();
        currentPieceBoard.setPiece(game.getCurrentPiece());
        nextPieceBoard.setPiece(game.getFollowingPiece());

    }

  /**
   * Swap the current and next piece by pressing Space bar or R
   */
  public void swapPress() {
    logger.info("Swapped by pressing space / R");
    game.swapCurrentPiece();
    currentPieceBoard.setPiece(game.getCurrentPiece());
    nextPieceBoard.setPiece(game.getFollowingPiece());

  }

  /**
   * Reverse rotate the current piece using "Q", "Z", "["
   */
  public void reverseRotatePress() {
    game.reversedRotateCurrentPiece();
    currentPieceBoard.display(game.getCurrentPiece());
    currentPieceBoard.setPiece(game.getCurrentPiece());
  }

  /**
   * Rotate the current piece using "E", "C", "]"
   */
  public void rotatePress() {
    game.rotateCurrentPiece();
    currentPieceBoard.setPiece(game.getCurrentPiece());
  }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    private void blockClicked(GameBlock gameBlock) {
      this.keyboardMode.set(false);
      blockAction(gameBlock);
        game.blockClicked(gameBlock);
    }

  /**
     * Set game
     * @param game game
     */
    public void setGame(Game game) {
        this.game = game;
    }

    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5, gameWindow);
    }

    /**
     * Key listener
     */
    public void keyListener() {
       scene.setOnKeyPressed(event -> {
           if(event.getCode() == KeyCode.ESCAPE) {
               game.stopGameLoop();
               stopGame();
           }
       });
    }

    /**
     * Stop the game and go back to start menu.
     */
    public void stopGame(){
        try {
                game.stop();
                gameWindow.startMenu();
        } catch (Exception e){
            logger.error("Fail to stop the game, {}", e.toString());
        }


    }

    /**
     * Trigger the game blocks fade
     * @param gameBlockCoordinateSet gameBlockCoordinateSet
     */
    public void triggerLineClear(Set<GameBlockCoordinate> gameBlockCoordinateSet) {
        logger.info("Bro I can finally trigger the lineClear stuff");
        try {
            gameBoard.fadeOut(gameBlockCoordinateSet);
        } catch (Exception e) {
            logger.error("EXCEPTION in triggerLineClear ! ! ! {}", e.toString());
        }

    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        //Play background music on the challenge scene
        Multimedia.playBackgroundMusic("game.wav");

        game.start();
        game.setLineClearedListener(this::triggerLineClear);
        //stop the game after pressing esc
        keyListener();
        game.setOnGameLoopListener(gameLoopListener);
        game.setNextPieceListener(this);
        game.nextPiece();

    }

  /**
   * Next piece
   * @param nextPiece nextPiece
   */
  @Override
  public void nextPiece(GamePiece nextPiece) {
    currentPieceBoard.setPiece(nextPiece);
    GamePiece gamePiece = game.getFollowingPiece();
    if(gamePiece != null) {
      nextPieceBoard.setPiece(gamePiece);
    }
  }
}
