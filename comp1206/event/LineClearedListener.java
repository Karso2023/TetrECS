package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlockCoordinate;

import java.util.Set;

/**
 * Line cleared listener
 */
public interface LineClearedListener {

  /**
   * line cleared
   * @param gameBlockCoordinates gameBlockCoordinates
   */
  void lineCleared(Set<GameBlockCoordinate> gameBlockCoordinates);
}
