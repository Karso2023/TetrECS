package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 *  Display an upcoming piece in a 3x3 grid
 *  It extends GameBoard
 */
public class PieceBoard extends GameBoard{

  /**
   * PieceBoard
   * @param cols cols
   * @param rows rows
   * @param width width
   * @param height height
   */
  public PieceBoard(int cols, int rows, double width, double height) {
    super(cols, rows, width, height);
  }

  /**
   * A method for setting a piece to display
   * @param piece the piece to show on the 3*3 grid
   */
  public void display(GamePiece piece){
    grid.playPiece(piece, 1,1);
  }

  /**
   * Set the piece for the board to display.
   *
   * @param piece the game piece to display inside
   */
  public void setPiece(GamePiece piece) {
    grid.clear();
    grid.playPiece(piece, 1, 1);
  }

}
