package chess.model;

import java.io.Serializable;
import java.util.Optional;

/**
 * The class GameState contains all the information about the ongoing chess game and methods to
 * manipulate their values. This information includes the current phase, current player, game field
 * setup and the winner (if there is one). There are also methods implemented for setting phase,
 * player and winner, and getting all of these attributes.
 */
public class GameState implements Serializable {

  private static final long serialVersionUID = 495L;

  private int age = 0;
  private Phase currentPhase;
  private Player currentPlayer;
  private GameField gameField;
  private Player winner;

  /** Set the values corresponding to the gameField attribute. */
  void setGameState() {
    this.gameField = new GameField();
    this.currentPlayer = Player.WHITE;
    this.currentPhase = Phase.RUNNING;
  }

  /**
   * Set all the values of the GameState to the values of another GameState.
   *
   * @param otherState the state whose values are taken over.
   */
  void setGameState(GameState otherState) {
    this.setGameState();
    this.setCurrentPlayer(otherState.getCurrentPlayer());
    this.gameField = otherState.getField();
    this.age = otherState.getAge();
    this.currentPhase = otherState.getCurrentPhase();
    if (currentPhase == Phase.FINISHED) {
      if (otherState.getWinner().isPresent()) {
        this.setWinner(otherState.getWinner().get());
      }
    }
  }

  /** Set the current phase of the game. */
  public void setCurrentPhase(Phase newPhase) {
    this.currentPhase = newPhase;
  }

  /** Set the current player of the game. */
  void setCurrentPlayer(Player newPlayer) {
    this.currentPlayer = newPlayer;
  }

  /** Set the winner of the game. */
  void setWinner(Player newWinner) {
    this.winner = newWinner;
  }

  /** Increase current age of GameState by one. */
  void increaseAge() {
    this.age++;
  }

  /**
   * Get age of the current GameState.
   *
   * @return the age of the GameState.
   */
  public int getAge() {
    return this.age;
  }

  /**
   * Return the current phase of the game.
   *
   * @return the current phase.
   */
  public Phase getCurrentPhase() {
    return currentPhase;
  }

  public GameField getField() {
    return gameField;
  }

  /**
   * Return the player that is currently allowed to make a move.
   *
   * @return the current player
   */
  public Player getCurrentPlayer() {
    return currentPlayer;
  }

  /**
   * Return the winner of the current game. This method may only be called if the current game is
   * finished.
   *
   * @return {@link Optional#empty()} if the game's a draw. Otherwise an optional that contains the
   *     winner
   */
  public Optional<Player> getWinner() {
    if (currentPhase != Phase.FINISHED) {
      throw new IllegalStateException(
          String.format(
              "Expected current phase to be %s, but instead it is %s",
              Phase.FINISHED, currentPhase));
    }
    return Optional.ofNullable(winner);
  }
}
