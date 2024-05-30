package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Score scene
 * Functions :
 * Save and read high scores to a scores file
 * Prompt for a name on getting a high score
 */
public class ScoreScene extends BaseScene{

  /**
   * Debug
   */
  public static final Logger logger = LogManager.getLogger(ScoreScene.class);

  /**
   * mainPane
   */
  private BorderPane mainPane;

  /**
   * scoresList
   */
  List<Pair<String, Integer>> scoresList = loadScores();

  /**
   * ObservableScores
   */
  ObservableList<Pair<String, Integer>> observableScores = FXCollections.observableArrayList(scoresList);

  /**
   * LocalScoresProperty
   */
  SimpleListProperty<Pair<String, Integer>> localScoresProperty = new SimpleListProperty<>(observableScores);

  /**
   * onlineScoreListBox
   */
  private final VBox onlineScoreListBox = new VBox();

  /**
   * communicator
   */
  private final Communicator communicator;

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public ScoreScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Score Scene");
    communicator = gameWindow.getCommunicator();
  }


  /**
   * Initialise
   */
  @Override
  public void initialise() {
    logger.info("Initialising Score scene");
    loadOnlineScores();
    keyListener();
  }

  /**
   * Build score scene
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());
    var ScoreScene = new StackPane();
    ScoreScene.setMaxWidth(gameWindow.getWidth());
    ScoreScene.setMaxHeight(gameWindow.getHeight());
    ScoreScene.getStyleClass().add("menu-background");
    root.getChildren().add(ScoreScene);
    mainPane = new BorderPane();
    ScoreScene.getChildren().add(mainPane);

    Image TetrEcsTitle = new Image(String.valueOf(Objects.requireNonNull(getClass().getResource("/images/TetrECS.png"))));
      ImageView titleImageView = new ImageView(TetrEcsTitle);
      titleImageView.setFitWidth((double) gameWindow.getWidth() / 2);
      titleImageView.setPreserveRatio(true);
      var imageTitleAdjust = new VBox();
      imageTitleAdjust.getChildren().add(titleImageView);
      imageTitleAdjust.setAlignment(Pos.TOP_CENTER);
      imageTitleAdjust.setPadding(new Insets(20));

      //Game over label
      Label gameOverLabel = new Label("Game Over");
      gameOverLabel.getStyleClass().add("bigtitle");
      var gameOverAdjust = new VBox();
      gameOverAdjust.getChildren().add(gameOverLabel);
      gameOverAdjust.setAlignment(Pos.TOP_CENTER);


      //High scores label
      Label highScores = new Label("High Scores");
      highScores.getStyleClass().add("title");
      var highScoresAdjust = new VBox();
      highScoresAdjust.getChildren().add(highScores);
      highScoresAdjust.setAlignment(Pos.TOP_CENTER);

     VBox topComponents = new VBox(imageTitleAdjust, gameOverAdjust, highScoresAdjust);
     topComponents.setAlignment(Pos.TOP_CENTER);
     mainPane.setTop(topComponents);


    //Local scores label
      Label localScores = new Label("Local Scores");
      localScores.getStyleClass().add("heading");
      var localScoresAdjust = new VBox();
      localScoresAdjust.getChildren().add(localScores);
      localScoresAdjust.setAlignment(Pos.CENTER_LEFT);
      localScoresAdjust.setPadding(new Insets((double) gameWindow.getWidth() / 22));

      //Local scores list
      VBox localScoresVBox = new VBox(localScoresAdjust, localScoresList());
      localScoresVBox.setPadding(new Insets(20));
      localScoresVBox.setAlignment(Pos.TOP_LEFT);
      mainPane.setLeft(localScoresVBox);

      //Local scores label
      Label onlineScoresLabel = new Label("Online Scores");
      onlineScoresLabel.getStyleClass().add("heading");
      var onlineScoresAdjust = new VBox();
      onlineScoresAdjust.getChildren().add(onlineScoresLabel);
      onlineScoresAdjust.setAlignment(Pos.CENTER_LEFT);
      onlineScoresAdjust.setPadding(new Insets((double) gameWindow.getWidth() / 22));

      //Online scores list
      VBox onlineScoresVBox = new VBox(onlineScoresAdjust, onlineScoreListBox);
      onlineScoresVBox.setAlignment(Pos.BOTTOM_RIGHT);
      VBox.setVgrow(onlineScoreListBox, Priority.ALWAYS);

      mainPane.setRight(onlineScoresVBox);

      onlineScoresVBox.setPadding(new Insets(20));
  }


  /**
   * If the player press Esc, escape instruction scene and return to menu
   */
  private void keyListener() {
    scene.setOnKeyPressed(event -> {
      if(event.getCode() == KeyCode.ESCAPE) {
        escapeScene();
      }
    });
  }

  /**
   * Set up local scores list
   * @return scoreListBox
   */
  private VBox localScoresList() {
    VBox scoreListBox = new VBox();
    scoreListBox.setAlignment(Pos.BOTTOM_LEFT);
    scoreListBox.setPadding(new Insets(20));
    scoreListBox.setSpacing(15);

      for (Pair<String, Integer> score : localScoresProperty.get()) {
        Label scoreLabel = new Label(score.getKey() + " : " + score.getValue());
        scoreLabel.getStyleClass().add("scorelist");
        scoreListBox.getChildren().add(scoreLabel);
      }

    return scoreListBox;
  }

  /**
   * Save scores by using bufferedWriter
   * The method will remove the first score when it reaches 10
   * @param scores scores
   */
  private void saveScores(List<Pair<String, Integer>> scores) {
    try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("highscores.txt"))) {

      for (Pair<String, Integer> score : scores) {

          logger.info("Adding scores into file");
          writer.write(score.getKey() + ":" + score.getValue() + "\n");

      }
    } catch (IOException e) {
      logger.error("Failed to write scores", e);
    }
  }

  /**
   * Load scores
   * @return new ArrayList
   */
  private List<Pair<String, Integer>> loadScores() {
    try (Stream<String> lines = Files.lines(Paths.get("highscores.txt"))) {
      return lines.map(line -> {
        String[] parts = line.split(":");
        return new Pair<>(parts[0], Integer.parseInt(parts[1]));
      }).collect(Collectors.toList());
    } catch (IOException e) {
      logger.error("Failed to read scores", e);
      return new ArrayList<>();
    }
  }

  /**
   * Add score
   * @param newScore new score
   */
  public void addScore(Pair<String, Integer> newScore) {
    ObservableList<Pair<String, Integer>> scores = localScoresProperty.get();
    scores.add(newScore);
    saveScores(new ArrayList<>(scores));
  }

  /**
   * Save current scores
   */
  public void saveCurrentScores() {
    saveScores(new ArrayList<>(localScoresProperty.get()));
  }

  /**
   * Load online score
   */
  private void loadOnlineScores() {
    logger.info("Loading online scores");
    gameWindow.getCommunicator().send("HISCORES");
    communicator.addListener(message -> {
      if (message.startsWith("HISCORES")) {
        String scoresData = message.substring("HISCORES".length()).trim();
        Platform.runLater(() -> onlineScoreList(scoresData));
      }
    });
  }

  /**
   * Update onlineScoreList
   * @param scoreData scoreData
   */
  private void onlineScoreList(String scoreData) {

    Platform.runLater(() -> {
      // Split the scores by newlines to separate entries
      String[] scoreEntries = scoreData.split("\n");
      onlineScoreListBox.getChildren().clear();
      for (String entry : scoreEntries) {
        // Split each entry by colon to separate name and score
        String[] details = entry.split(":");
        if (details.length == 2) {
          String playerName = details[0].trim();
          String score = details[1].trim();
          Label scoreLabel = new Label(playerName + " : " + score);
          scoreLabel.getStyleClass().add("scorelist");
          onlineScoreListBox.getChildren().add(scoreLabel);
        } else {
          logger.error("Invalid score entry: " + entry);
        }
      }
    });

  }

  /**
   * Escape score scene and go back to start menu.
   */
  private void escapeScene(){
    try {
      Game.stop();
      gameWindow.startMenu();
    } catch (Exception e){
      logger.error("Fail to stop the game, {}", e.toString());
    }


  }
}
