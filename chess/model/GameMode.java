package chess.model;

/**
 * The enumeration GameMode has two options to choose from: hotseat is chosen if both sides should
 * be playable, single if the player wants to play against an AI.
 */
public enum GameMode {
  HOTSEAT("Hotseat"),
  SINGLE("Single"),
  NETWORK("Network");

  private final String modeName;

  /**
   * Constructor of the GameMode enumeration: sets up the variable modeName.
   *
   * @param modeName a string that is either "hotseat" or "single"
   */
  GameMode(String modeName) {
    this.modeName = modeName;
  }

  @Override
  public String toString() {
    return modeName;
  }
}
