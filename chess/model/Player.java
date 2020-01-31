package chess.model;

import java.io.Serializable;

/** The two possible options for the player: he is either using the black or the white pawns. */
public enum Player implements Serializable {
  WHITE("White"),
  BLACK("Black");

  private final String playerName;

  Player(String playerName) {
    this.playerName = playerName;
  }

  @Override
  public String toString() {
    return playerName;
  }
}
