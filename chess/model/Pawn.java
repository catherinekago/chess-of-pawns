package chess.model;

import java.io.Serializable;

/**
 * The class Pawn includes information about the player and methods to assign a player to the pawn
 * and to get player of the considered pawn.
 */
public class Pawn implements Serializable {

  private final Player player;

  Pawn(Player player) {
    this.player = player;
  }

  /**
   * This method gets the player of the pawn.
   *
   * @return the player of the selected pawn
   */
  public Player getPlayer() {
    return player;
  }
}
