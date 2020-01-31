package chess.view;

import java.beans.PropertyChangeListener;

/** The main interface of the view. It gets the state it displays directly from the model. */
public interface View extends PropertyChangeListener {

  /** Show the graphical user interface of the chess game. */
  void showGame();

  /**
   * Get the draw board attribute of the ChessView class.
   *
   * @return the drawBoard
   */
  DrawBoard getDrawBoard();

  /** Handle the case that the server was selected for the network game twice. */
  void handleServerAlreadyInUse();

  /**
   * Handle the case that the client couldn't connect to the chosen server because the server was
   * shut down.
   */
  void handleConnectionRefusal();

  /** Handle the case that the client tries to connect to an unknown host. */
  void handleUnknownHost();

  /**
   * Handle the case that the client couldn't connect to the chosen server because the server
   * already accepted two clients and has no further capacities.
   */
  void handleTooManyConnections();

  /** Handle the case that the other player left the game while the game was still running. */
  void handleOtherPlayerLeftGame();

  /** Adjust the coloring of the gui depending on the role of the player. */
  void updateGuiDependingOnNetworkPlayer(String role);

  /** Handle the case that the server connection was interrupted. */
  void handleConnectionLoss();
}
