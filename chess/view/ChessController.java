package chess.view;

import static java.util.Objects.requireNonNull;

import chess.model.Cell;
import chess.model.Chess;
import chess.model.GameMode;
import chess.model.Model;
import chess.model.Phase;
import chess.model.Player;
import chess.network.Client;
import chess.network.Server;

import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Optional;
import javax.swing.SwingWorker;

/**
 * The class ChessController is responsible for the data manipulation. It handles the user input
 * that is forwarded by the ChessView object and invokes methods of the model or initiates an update
 * of the ChessView object.
 */
public class ChessController implements Controller, PropertyChangeListener {

  private static final String CONNECTION_LOSS = "Socket closed due to connection loss";
  private static final String SERVER_FULL = "Server full";
  private static final String WINDOW_CLOSE = "Socket closed due to window closing event";
  private Client client;
  private Model model;
  private View view;
  private Optional<Cell> moveStartPoint;

  private SwingWorker<Boolean, Void> moveWorker;

  /**
   * Constructs a new object of the class ChessController by setting the given model to its
   * attribute model and initializing the moveStartPoint attribute.
   *
   * @param chess the game model that includes the business logic of the game
   */
  public ChessController(Model chess) {
    model = requireNonNull(chess);
    moveStartPoint = Optional.empty();
  }

  @Override
  public void startServerConnection() throws IOException {
    if (Server.isPortAvailable(Server.PORT)) {
      model.setUpServer();
      InetAddress serverAddress = InetAddress.getLocalHost();
      startClientConnection("ServerClient", serverAddress);
    } else {
      view.handleServerAlreadyInUse();
    }
  }

  @Override
  public void quitClientConnection() {
    client.quitConnection();
  }

  @Override
  public void startClientConnection(String role, InetAddress server) {
    try {
      client = model.setUpClient(server);
      client.addPropertyChangeListener(this);
      setUpModelForNetworkGame(role);
      view.updateGuiDependingOnNetworkPlayer(role);
    } catch (SocketException e) {
      switch (e.getMessage()) {
        case "Connection refused: connect":
          view.handleConnectionRefusal();
          break;
        case "Server is full.":
          view.handleTooManyConnections();
          break;
        case "Connection reset":
          view.handleOtherPlayerLeftGame();
          break;
        default:
          break;
      }
    } catch (UnknownHostException e1) {
      view.handleUnknownHost();
    } catch (IOException e) {
      System.err.println("IOException occurred!");
    }
  }

  /**
   * Based on previous choices within the network setup dialog, the model is updated accordingly.
   *
   * @param option a string that corresponds to the chosen mode within the networks setup dialog.
   */
  private void setUpModelForNetworkGame(String option) {
    if (option.equals("Client")) {
      model.getState().setCurrentPhase(Phase.RUNNING);
      model.setMyPlayer(Player.BLACK);
    } else if (option.equals("ServerClient")) {
      model.setMyPlayer(Player.WHITE);
    }
  }

  @Override
  public void handleMouseClick(MouseEvent event) {
    int x = event.getX();
    int y = event.getY();
    if (event.getButton() == MouseEvent.BUTTON1) {
      Optional<Cell> wasClicked = calculateGameFieldPosition(x, y);
      if (wasClicked.isPresent()) {
        if (moveStartPoint.isPresent()
            && model.getPossibleMovesForPawn(moveStartPoint.get()).contains(wasClicked.get())) {
          move(moveStartPoint.get(), wasClicked.get());
          moveStartPoint = Optional.empty();
          if (model.getGameMode() == GameMode.SINGLE
              && model.getState().getCurrentPlayer() == Player.BLACK) {
            executeKiInBackground();
          }
        } else if (model.getGameMode() == GameMode.NETWORK
            && model.getMyPlayer() == model.getState().getCurrentPlayer()
            && model.getState().getField().get(wasClicked.get()).isPresent()
            && model.getState().getField().get(wasClicked.get()).get().getPlayer()
                == model.getState().getCurrentPlayer()) {
          moveStartPoint = wasClicked;
        } else if (!(model.getGameMode() == GameMode.NETWORK)
            && model.getState().getField().get(wasClicked.get()).isPresent()
            && model.getState().getField().get(wasClicked.get()).get().getPlayer()
                == model.getState().getCurrentPlayer()) {
          moveStartPoint = wasClicked;
        } else {
          moveStartPoint = Optional.empty();
        }
      } else {
        if (moveStartPoint.isPresent()) {
          moveStartPoint = Optional.empty();
        }
      }
    } else if (event.getButton() == MouseEvent.BUTTON3) {
      if (moveStartPoint.isPresent()) {
        moveStartPoint = Optional.empty();
      }
    }
  }

  /**
   * This method schedules the execution of the time consuming KI calculation task on a different
   * thread while the GUI still remains responsive.
   */
  private void executeKiInBackground() {
    moveWorker =
        new SwingWorker<>() {

          @Override
          protected Boolean doInBackground() {
            ((Chess) model).executeKiMove();
            return true;
          }
        };
    moveWorker.execute();
  }

  @Override
  public Optional<Cell> calculateGameFieldPosition(int x, int y) {
    int drawBoardXStart = view.getDrawBoard().getBorderX();
    int drawBoardXEnd = drawBoardXStart + view.getDrawBoard().getBoardSize().width;
    int drawBoardYStart =
        view.getDrawBoard().getBorderY() + (view.getDrawBoard().getCellSize().width / 2);
    int drawBoardYEnd = drawBoardYStart + view.getDrawBoard().getBoardSize().height;

    if (x < drawBoardXStart || x > drawBoardXEnd || y < drawBoardYStart || y > drawBoardYEnd) {
      return Optional.empty();
    } else {
      int columnValue = 0;
      int rowValue = 0;
      for (int col = 0; col < 8; col++) {
        if (x > drawBoardXStart + col * view.getDrawBoard().getCellSize().width
            && x < drawBoardXStart + (col + 1) * view.getDrawBoard().getCellSize().width) {
          columnValue = col;
        }
      }

      for (int row = 8; row > 0; row--) {
        if (y > drawBoardYStart + (8 - row) * view.getDrawBoard().getCellSize().height
            && y < drawBoardYStart + (8 - (row - 1)) * view.getDrawBoard().getCellSize().height) {
          rowValue = row - 1;
        }
      }
      return Optional.of(new Cell(columnValue, rowValue));
    }
  }

  @Override
  public Optional<Cell> getMoveStartPoint() {
    return moveStartPoint;
  }

  @Override
  public void setView(View view) {
    this.view = requireNonNull(view);
  }

  @Override
  public void start() {
    view.showGame();
  }

  @Override
  public void resetGame() {
    model = new Chess();
    view = new ChessView(model, this);
    moveStartPoint = Optional.empty();
    model.addPropertyChangeListener(view);
    start();
  }

  @Override
  public boolean move(Cell from, Cell to) {
    return model.move(from, to);
  }

  @Override
  public void dispose() {
    model.removePropertyChangeListener(view);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    switch (evt.getPropertyName()) {
      case CONNECTION_LOSS:
        view.handleConnectionLoss();
        break;
      case WINDOW_CLOSE:
        view.handleOtherPlayerLeftGame();
        break;
      case SERVER_FULL:
        view.handleTooManyConnections();
        break;
      default:
        break;
    }
  }
}
