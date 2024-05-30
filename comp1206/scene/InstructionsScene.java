package uk.ac.soton.comp1206.scene;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.Objects;

/**
 * Instruction Scene, add tutorial in this scene
 * Create a GridPane of PieceBoards
 * Add keyboard listeners to allow the user to press escape to exit the challenge
 * or instructions or the game itself
 * -
 * Tip: You want to listen for keyboard input on the scene -
 * if you try to add it to a control, how would it know which control should receive the event?
 * -
 * Tip: The initialise method on the Scene is an appropriate place to add this listener,
 * once the scene has been initialised
 * -
 * Tip: You will need to add a method to shut down the game in the ChallengeScene
 * to end and clean up all parts of the game, before going back - or it'll keep playing!
 * -
 * Design :
 * Title: Instructions -> Description -> Image -> Grid
 * I am going to use VBox to structure the instruction scene
 */
public class InstructionsScene extends BaseScene{

  /**
   * mainPane
   */
  private BorderPane mainPane;
  /**
   * Debug
   */
  public static final Logger logger = LogManager.getLogger(InstructionsScene.class);

  /**
   * Build
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());
    var instructionsScene = new StackPane();
    instructionsScene.setMaxWidth(gameWindow.getWidth());
    instructionsScene.setMaxHeight(gameWindow.getHeight());
    instructionsScene.getStyleClass().add("menu-background");
    root.getChildren().add(instructionsScene);
    mainPane = new BorderPane();
    instructionsScene.getChildren().add(mainPane);

    //Structure
    VBox vBox = structure();
    title(vBox);
    description(vBox);
    image(vBox);
    grid(vBox);
  }

  /**
   * initialise instruction scene
   */
  @Override
  public void initialise() {
    logger.info("Initialising Instructions");

    keyListener();
  }

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public InstructionsScene(GameWindow gameWindow) {
    super(gameWindow);
  }

  /**
   * Title
   * @param vBox vBox
   */
  private void title(VBox vBox){
    Text title = new Text("Instructions");
    title.getStyleClass().add("title");
    vBox.getChildren().add(title);
  }

  /**
   * Description
   * @param vBox vBox
   */
  private void description(VBox vBox){
    Text descriptionText = new Text("TetrECS is a fast-paced gravity-free block placement game, where you must " +
            "survive by clearing rows through careful placement of the upcoming blocks before the time runs out. Lose all 3 lives and you're destroyed!");
    descriptionText.getStyleClass().add("instructions");
    TextFlow descriptionFlow = new TextFlow(descriptionText);
    descriptionFlow.setTextAlignment(TextAlignment.CENTER);
    vBox.getChildren().add(descriptionFlow);
  }

  /**
   * Image method
   * @param vBox vBox
   */
  private void image(VBox vBox){
    Image instructionsImage = new Image(String.valueOf(Objects.requireNonNull(getClass().getResource("/images/Instructions.png"))));
    ImageView instructionsImageView = new ImageView(instructionsImage);
    instructionsImageView.setFitWidth((double) gameWindow.getWidth() / 1.5);
    instructionsImageView.setPreserveRatio(true);
    vBox.getChildren().add(instructionsImageView);
  }

  /**
   * Grid
   * @param vBox vBox
   */
  private void grid(VBox vBox){
    logger.info("Generating instruction grids");
    Text pieces = new Text("Game Pieces");
    pieces.getStyleClass().add("heading");
    vBox.getChildren().add(pieces);
    GridPane gridPane = new GridPane();
    gridPane.setHgap(10);
    gridPane.setVgap(10);
    vBox.getChildren().add(gridPane);

    //Set col and row = 0, if col reaches 5,
    // add row so gridPane.add can keep adding grids.
    int col =0;
    int row =0;
    for(int i = 0; i < GamePiece.PIECES; i++){
      GamePiece gamePiece = GamePiece.createPiece(i);
      PieceBoard pieceBoard = new PieceBoard(3,3, 50, 50);
      pieceBoard.setPiece(gamePiece);
      gridPane.add(pieceBoard, col, row);
      gridPane.setAlignment(Pos.BOTTOM_CENTER);
      //Since it loops from 0 to 15, col will keep adding and we cannot make the col stops at 5
      //unless we change it value back to 0 (0,0) ... (4,0) -> (0,1) ... (0,4)
      //5 * 3 in the end
      col++;
      if(col == 5) {
        col = 0;
        row++;
      }
    }
  }

  /**
   * Structure
   * @return return
   */
  private VBox structure(){
    VBox vBox = new VBox();
    BorderPane.setAlignment(vBox, Pos.TOP_CENTER);
    vBox.setAlignment(Pos.TOP_CENTER);
    mainPane.setCenter(vBox);
    return vBox;
  }

  /**
   * If the player press Esc, escape instruction scene and return to menu
   */
  public void keyListener() {
    scene.setOnKeyPressed(event -> {
      if(event.getCode() == KeyCode.ESCAPE) {
        escapeScene();
      }
    });
  }

  /**
   * Escape instruction scene and go back to start menu.
   */
  public void escapeScene(){
    try {
      Game.stop();
      gameWindow.startMenu();
    } catch (Exception e){
      logger.error("Fail to stop the game, {}", e.toString());
    }


  }

}
