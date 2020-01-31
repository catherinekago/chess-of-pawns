package chess.model;

import chess.network.Client;
import chess.network.Server;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.util.Set;

/**
 * The main interface of the chess model. It provides all necessary methods for accessing and
 * manipulating the data such that a game can be played successfully.
 *
 * <p>When something changes in the model, the model notifies its observers by firing a {@link
 * PropertyChangeEvent change-event}.
 */
public interface Model extends PropertyChangeListener {

  /** Initialize the attribute miniMax of the class Chess. */
  void initializeMiniMax();

  /**
   * Getter: get the attribute MimiMax of the class chess.
   *
   * @return the MiniMax object
   */
  MiniMaxAlgorithm getMiniMax();

  /**
   * Set the game mode of the current game.
   *
   * @param mode "Single" for a game against an AI, "Hotseat" for a game where both sides are
   *     playable.
   */
  void setGameMode(GameMode mode);

  /**
   * Get the game mode of the current game.
   *
   * @return the game mode that is active for the current game.
   */
  GameMode getGameMode();

  /**
   * Sets the established server as an attribute of the corresponding model.
   *
   * @param server the established server
   */
  void setServer(Server server);

  /**
   * Sets the player of this model for the network game.
   *
   * @param player Player.WHITE if the player initialized the game with the server, Player.BLACK if
   *     he chose just the client.
   */
  void setMyPlayer(Player player);

  /**
   * Gets the player of this model in a network game.
   *
   * @return Player.WHITE if the player initialized the game with the server, Player.BLACK * if he
   *     chose just the client.
   */
  Player getMyPlayer();

  /**
   * Set up the client for the network game.
   *
   * @param server the InetAddress of the server
   * @return the client that was set up
   * @throws ConnectException exception that can occur within the network application
   */
  Client setUpClient(InetAddress server) throws IOException;

  /**
   * Set up the server for the network game.
   *
   * @throws IOException exception that can occur within the network application
   */
  void setUpServer() throws IOException;

  /**
   * Set the received state as the current state of the model the client is bound to.
   *
   * @param receivedState the state that was received from the Server
   */
  void setGameStateToReceivedState(GameState receivedState);

  String NEW_MOVE = "New Move";
  String NETWORK_UPDATE = "Network Update";

  /**
   * Add a {@link PropertyChangeListener} to the model that will be notified about the changes made
   * to the chess board.
   *
   * @param pcl the view that implements the listener.
   */
  void addPropertyChangeListener(PropertyChangeListener pcl);

  /**
   * Remove a listener from the model, which will then no longer be notified about any events in the
   * model.
   *
   * @param pcl the view that then no longer receives notifications from the model.
   */
  void removePropertyChangeListener(PropertyChangeListener pcl);

  /**
   * Move pawn from one cell to another and deal with the consequences. Moving a pawn only works if
   * their is currently a game running, if the given cell contains a pawn of the current player, and
   * if moving to the other cell is a valid chess move. After the move, it will be the turn of the
   * next player, unless he has to miss a turn because he can't move any pawn or the game is over.
   *
   * @param from cell to move pawn from
   * @param to cell to move pawn to
   * @return <code>true</code> if the move was successful, <code>false</code> otherwise
   */
  boolean move(Cell from, Cell to);

  /**
   * Return the {@link GameState} specific to this class.
   *
   * @return The <code>GameState</code>-object.
   */
  GameState getState();

  /**
   * Computes all possible moves for a selected cell. There are in total four moves possible for a
   * single pawn, depending on the current position of the pawn as well as the position of pawns
   * from the opponent player.
   *
   * @param cell The {@link Cell cell} that the {@link Pawn pawn} is currently positioned.
   * @return A set of cells with all possible moves for the current selected pawn.
   */
  Set<Cell> getPossibleMovesForPawn(Cell cell);
}
