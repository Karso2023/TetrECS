package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Lobby Scene, some of the codes are from ECS Lab7
 */
public class LobbyScene extends BaseScene{

  public static final Logger logger = LogManager.getLogger(LobbyScene.class);
  protected final Communicator communicator;
  private final VBox channelListBox = new VBox(5);

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public LobbyScene(GameWindow gameWindow) {
    super(gameWindow);
    communicator = gameWindow.getCommunicator();
  }

  /**
   * Initialise lobby scene
   */
  @Override
  public void initialise() {

    Multimedia.playBackgroundMusic("menu.mp3");
    keyListener();
    communicator.send("LIST");
    communicator.addListener((message) -> Platform.runLater(() -> this.receivedCommand(message)));
    repeatTimer();
  }

  /**
   * Build lobby scene
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

    var lobbyPane = new StackPane();
    lobbyPane.setMaxWidth(gameWindow.getWidth());
    lobbyPane.setMaxHeight(gameWindow.getHeight());
    lobbyPane.getStyleClass().add("menu-background");
    root.getChildren().add(lobbyPane);

    var mainPane = new BorderPane();
    lobbyPane.getChildren().add(mainPane);

    //Title UI
    HBox sceneTitle = new HBox();
    sceneTitle.setAlignment(Pos.TOP_CENTER);
    sceneTitle.setPadding(new Insets(20));
    sceneTitle.setSpacing(10);

    Label lobbyTitle = new Label("Multiplayer");
    lobbyTitle.getStyleClass().add("title");
    sceneTitle.getChildren().add(lobbyTitle);
    mainPane.setTop(sceneTitle);


    //Current Games Label
    VBox topLeftUI = new VBox();
    Label currentGamesLabel = new Label("Current Games");
    currentGamesLabel.getStyleClass().add("heading");
    topLeftUI.setAlignment(Pos.TOP_LEFT);
    topLeftUI.setPadding(new Insets(20));
    topLeftUI.setSpacing(20);



    //Add new channel
    VBox newChannelBox = new VBox(10);
    TextField channelNameField = new TextField();
    channelNameField.setPromptText("Enter name to create a new channel");
    channelNameField.setOnKeyPressed((e) -> {
      if (e.getCode() != KeyCode.ENTER) return;
      String channelName = channelNameField.getText();
      if (channelName.isEmpty()) {
        showAlertDialog("Channel name cannot be empty.");
      } else {
        createChannel(channelName);
      }
      channelNameField.clear();
      channelNameField.requestFocus();
    });


    //Create new channel
    Button createChannelButton = new Button("Host New Game");
    createChannelButton.getStyleClass().add("channelButton");
    createChannelButton.setOnAction(e -> {
      String channelName = channelNameField.getText();
      if (channelName.isEmpty()) {
        showAlertDialog("Channel name cannot be empty.");
      } else {
        createChannel(channelName);
      }
      channelNameField.clear();
    });

    newChannelBox.getChildren().addAll(createChannelButton, channelNameField);
    newChannelBox.setMaxWidth((double) gameWindow.getWidth()/4);
    //Channel list
    channelListBox.setAlignment(Pos.BOTTOM_LEFT);
    topLeftUI.getChildren().addAll(currentGamesLabel, newChannelBox, channelListBox);
    mainPane.setCenter(topLeftUI);


  }


  /**
   * Create channel
   * @param channelName channelName
   */
  private void createChannel(String channelName) {
    communicator.send("CREATE <Channel>" + channelName);
  }

  /**
   * Show alert dialog
   *
   * @param message message
   */
  private void showAlertDialog(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Error");
    alert.setHeaderText(null);  // You can set this to something specific if needed
    alert.setContentText(message);
    alert.showAndWait();
  }

  /**
   * The LobbyScene on opening should start a repeating timer requesting current channels using
   * the Communicator from the server.
   */
  private void repeatTimer() {
    TimerTask requestChannels = new TimerTask() {
      @Override
      public void run() {
        communicator.send("LIST");
      }
    };

    Timer timer = new Timer();
    timer.schedule(requestChannels,0, 5000);
  }

  /**
   * Use ReceivedMessage to receive different commands from the communicator
   * @param receivedMessage receivedMessage
   */
  private void receivedCommand(String receivedMessage) {

    logger.info("Received message: {}", receivedMessage);


      String[] messages = receivedMessage.split(" ", 2);

    switch (messages[0]) {
      case "CHANNELS":
        if (messages.length > 1) {
          updateChannelList(messages[1]);
        }
        break;
      case "ERROR":
        if (messages.length > 1) {
          showAlertDialog(messages[1]);
        }
      case "JOIN":
      case "HOST":
      case "NICK":
      case "USERS":
        break;
      default:
        logger.error("Unhandled command: {}", messages[0]);
        break;
    }
  }

  /**
   * Update channel list
   * @param channelData channelData
   */
  private void updateChannelList(String channelData) {
    try {
      Platform.runLater(() -> {
        String[] channelNames = channelData.split(",");
        channelListBox.getChildren().clear();
        for (String name : channelNames) {
          Label channelLabel = new Label(name);
          channelLabel.getStyleClass().add("channelItem");
          channelLabel.setOnMouseClicked(event -> joinChannel(name));
          channelListBox.getChildren().add(channelLabel);
        }
      });
    } catch (Exception e) {
      logger.error("Cannot create channel list, give up bro. Error: {}", e.toString());
    }
  }

  /**
   * Join Channel
   * @param channelName channelName
   */
  private void joinChannel(String channelName) {
    try{
      communicator.send("JOIN <Channel>" + channelName);
    } catch (Exception e) {
      logger.error("Cannot send . Error: {}", e.toString());
    }

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
   * Escape score scene and go back to start menu.
   */
  private void escapeScene() {
    try {
      Game.stop();
      gameWindow.startMenu();
    } catch (Exception e) {
      logger.error("Fail to stop the game, {}", e.toString());
    }
  }

}
