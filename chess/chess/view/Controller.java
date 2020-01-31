package chess.view;

import chess.model.Cell;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Optional;

/**
 * The main controller interface of the chess game. It takes the actions from the user and handles
 * them accordingly. This is by either invoking the necessary chess.model-methods, or by directly telling
 * the chess.view to change its graphical user-interface.
 */
public interface Controller {

  /**
   * Set the chess.view that the controller will use afterwards.
   *
   * @param view The {@link View}.
   */
  void setView(View view);

  /** Initializes and starts the user interface. */
  void start();

  /** Reset a game such that the game is in its initial state afterwards. */
  void resetGame();

  /**
   * Delegates the chess.model to start the client-server..
   *
   * @throws IOException exception that could occur within chess.network application
   */
  void startServerConnection() throws IOException;

  /** Closes the client connection if the player leaves the game. */
  void quitClientConnection();

  /**
   * Delegates the chess.model to start the client.
   *
   * @param role of the player: Server and Client or just Server
   * @param serverAddress the address of the server that is handling the communication
   */
  void startClientConnection(String role, InetAddress serverAddress);

  /**
   * Check whether the given coordinates lie within the game field or not and calculate the cell
   * matching the board position.
   *
   * @param x the x coordinate of the mouse click
   * @param y the y coordinate of the mouse click
   * @return Optional.empty() if the given coordinates lie outside the game field, the specific cell
   *     with the matching board position, if the coordinates lie within the game field.
   */
  Optional<Cell> calculateGameFieldPosition(int x, int y);

  /**
   * Handles the mouse click event that occurs in the class chess.view and selects further actions
   * depending on the given button that was clicked and the situation. The action includes checking
   * (and potentially updating) the attribute moveStartPoint which represents the cell that was
   * selected before. Also, if the coordinates of the current left mouse click lie outside the
   * bounds of the game board or the right button has been clicked, all previously made selections
   * are cancelled.
   *
   * @param event the mouse click event that triggered this method
   */
  void handleMouseClick(MouseEvent event);

  /**
   * Gets the currently selected cell.
   *
   * @return the cell if there is one currently selected, Optional.empty() otherwise.
   */
  Optional<Cell> getMoveStartPoint();

  /**
   * Execute a step on the chess board.
   *
   * @param from The {@link Cell source cell}.
   * @param to The {@link Cell target cell}.
   * @return <code>true</code> if the move was executed successfully; <code>false</code> otherwise.
   */
  boolean move(Cell from, Cell to);

  /** Dispose any remaining resources. */
  void dispose();
}
