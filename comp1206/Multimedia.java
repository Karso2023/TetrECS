package uk.ac.soton.comp1206;

import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URI;
import java.util.Objects;


/**
 * Multimedia class to handle bgm and audio
 */
public class Multimedia {
  private static MediaPlayer audioPlayer;
  private static MediaPlayer bgmPlayer;
  private static Media media;
 private static final Logger logger = LogManager.getLogger(Multimedia.class);

  /**
   * playAudioFile
   * @param fileName fileName
   */
  public static void playAudioFile(String fileName){

    try {
      media = new Media(Objects.requireNonNull(Multimedia.class.getResource("/sounds/" + fileName)).toExternalForm());
      audioPlayer = new MediaPlayer(media);
      audioPlayer.play();
      logger.info("Playing audio");
    } catch (Exception e){
      logger.error("Failed to play audio, {}", e.toString());
    }
  }


  /**
   * playBackgroundMusic
   * @param fileName fileName
   */
  public static void playBackgroundMusic(String fileName){

    if(bgmPlayer != null){
      bgmPlayer.stop();
    }

      try {
          media = new Media(Objects.requireNonNull(Multimedia.class.getResource("/music/" + fileName)).toExternalForm());
          bgmPlayer = new MediaPlayer(media);
          bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
          bgmPlayer.play();
      } catch (Exception e){
        logger.error("Failed to play background music, {}", e.toString());
      }
    }
}

