package uk.ac.soton.comp1206.scene;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.Objects;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        //Awful title (BETTER NOW !)
        try {
            Image TetrEcsTitle = new Image(String.valueOf(Objects.requireNonNull(getClass().getResource("/images/TetrECS.png"))));
            ImageView titleImageView = new ImageView(TetrEcsTitle);
            titleImageView.setFitWidth((double) gameWindow.getWidth() / 2);
            titleImageView.setPreserveRatio(true);
            addShakeAnimation(titleImageView);
            var imageTitleAdjust = new VBox();
            imageTitleAdjust.getChildren().add(titleImageView);
            imageTitleAdjust.setAlignment(Pos.TOP_CENTER);
            imageTitleAdjust.setPadding(new Insets(20));
            mainPane.setTop(imageTitleAdjust);

        } catch (Exception e){
            logger.error("Cannot add TetrEcs title image, {}", e.toString());
        }

        //For now, let us just add a button that starts the game. I'm sure you'll do something way better.
        //Not going to add logger for buttons
        var startChallengeButton = menuButtonCss("Single Player");
        startChallengeButton.getStyleClass().add("button");
        //"Multi Player" Button
        var multiPlayerButton = menuButtonCss("Multi Player");
        multiPlayerButton.getStyleClass().add("button");
        //"How to Play" button
        var howToPlayButton = menuButtonCss("How to Play");
        howToPlayButton.getStyleClass().add("button");
        //"Exit" button
        var exit = menuButtonCss("Exit");
        exit.getStyleClass().add("button");


        //Set all buttons position in one go
        VBox root = new VBox();
        root.getChildren().addAll(startChallengeButton , multiPlayerButton , howToPlayButton , exit);
        root.setSpacing(20);
        root.setAlignment(Pos.CENTER);
        mainPane.setCenter(root);

        //Bind the startChallengeButton action to the startGame method in the menu
        startChallengeButton.setOnAction(this::startGame);
        //Bind the multiPlayerButton action to the startGame method in the menu
        multiPlayerButton.setOnAction(this::multiPress);
        //Bind the howToPlayButton action to the startGame method in the menu
        howToPlayButton.setOnAction(this::instructionPress);
        //Bind the exit action to the startGame method in the menu
        exit.setOnAction(this::exitPress);
    }

    /**
     * Create a TranslateTransition for the shake effect
     * @param imageView imageView
     */
    public void addShakeAnimation(ImageView imageView) {
        TranslateTransition shakeTransition = new TranslateTransition(Duration.seconds(6), imageView);
            shakeTransition.setFromX(-50);
            shakeTransition.setToX(50);

            shakeTransition.setAutoReverse(true);
            shakeTransition.setCycleCount(TranslateTransition.INDEFINITE);


            shakeTransition.play();

    }

    /**
     * Use this method to add CSS style to all menu buttons
     * @param text text
     * @return button
     */
    private Button menuButtonCss(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("button");
        // Add any additional common configuration for the buttons here
        return button;
    }
    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {
        //Play background music on the menu
        Multimedia.playBackgroundMusic("menu.mp3");
        keyListener();
    }

    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event) {
        gameWindow.startChallenge();
    }

    /**
     * Handle when the "How to Play" button is pressed
     * @param event event
     */
    private void instructionPress(ActionEvent event){
        gameWindow.howToPlay();
    }

    private void multiPress(ActionEvent event) {
        gameWindow.multiPlayer();
    }

    /**
     * Handle when the "Exit" button is pressed
     * @param event event
     */
    private void exitPress(ActionEvent event){
        stopGame();
    }
    /**
     * If the player press Esc, the game will shut down
     */
    public void keyListener() {
        scene.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ESCAPE) {
                stopGame();
            }
        });
    }

    /**
     * Close the game
     */
    public void stopGame(){
        try {
            System.exit(0);
        } catch (Exception e){
            logger.error("Fail to close the game, {}", e.toString());
        }


    }
}
