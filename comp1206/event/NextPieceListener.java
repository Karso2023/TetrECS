package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * NextPieceListener
 */
public interface NextPieceListener {

  /**
   * Takes the next GamePiece as a parameter
   * @param nextPiece nextPiece
   */
  void nextPiece(GamePiece nextPiece);
}
