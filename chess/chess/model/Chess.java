package chess.model;

import static java.util.Objects.requireNonNull;

import chess.network.Client;
import chess.network.Server;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * The class Chess handles the game logic of the chess game by accessing the methods of the other
 * involved classes and changing the state of the chess.model.
 */
public class Chess implements Model {

  static final int FIRST_COLUMN = 0;
  static final int FIRST_ROW = 0;
  static final int LAST_ROW = 7;

  private final PropertyChangeSupport support;

  private GameMode gameMode;
  private GameState state;
  private MiniMaxAlgorithm miniMax;
  private Server server;
  private static final int TWO_FIELDS = 2;
  private Player myPlayer;

  /**
   * By initializing an new object of the class Chess, a new game is set up. This means that all the
   * fields of the board are initialized and player white can start the game with his first move.
   */
  public Chess() {
    support = new PropertyChangeSupport(this);
    state = new GameState();
    state.setGameState();
    initializeMiniMax();
    initializeField();
  }

  Chess(Chess otherChess) {
    support = new PropertyChangeSupport(this);
    this.gameMode = otherChess.getGameMode();
    this.state = new GameState();
    this.state.setGameState();
    String player = (otherChess.state.getCurrentPlayer().toString().toUpperCase());
    this.state.setCurrentPlayer(Player.valueOf(player));
    this.miniMax = otherChess.miniMax;
    for (int row = LAST_ROW; row >= FIRST_ROW; row--) {
      for (int col = FIRST_COLUMN; col < GameField.SIZE; col++) {
        chess.model.Cell currentCell = new chess.model.Cell(col, row);
        Optional<Pawn> maybePawn = otherChess.getState().getField().get(currentCell);
        if (maybePawn.isPresent()) {
          this.state.getField().set(currentCell, maybePawn.get());
        }
      }
    }
  }

  @Override
  public void setMyPlayer(Player player) {
    this.myPlayer = player;
  }

  @Override
  public Player getMyPlayer() {
    return this.myPlayer;
  }

  @Override
  public void setServer(Server server) {
    this.server = server;
    server.addPropertyChangeListener(this);
  }

  /**
   * Execute a move of the KI by determining the best possible move with the minimax algorithm and
   * executing the move that was declared the best move.
   */
  public void executeKiMove() {
    Cell moveTo = null;
    getMiniMax().executeMinimax(this);
    for (Node child : getMiniMax().getTree().getRoot().getChildren()) {
      if (child.getEvaluationValue() == getMiniMax().getTree().getRoot().getEvaluationValue()) {
        move(child.getMovedFrom(), child.getMovedTo());
        moveTo = child.getMovedTo();
        break;
      }
    }
    if (this.getState().getCurrentPlayer() == Player.BLACK) {
      this.handleWinningCase(moveTo);
    }
  }

  @Override
  public Client setUpClient(InetAddress server) throws IOException {
    return (new Client(this, server));
  }

  @Override
  public void setUpServer() {
    Server server = new Server();
    server.startServer();
    setServer(server);
  }

  @Override
  public void setGameStateToReceivedState(GameState receivedState) {
    this.getState().setGameState(receivedState);
    notifyListeners(NETWORK_UPDATE);
  }

  @Override
  public void initializeMiniMax() {
    this.miniMax = new MiniMaxAlgorithm();
  }

  @Override
  public MiniMaxAlgorithm getMiniMax() {
    return this.miniMax;
  }

  @Override
  public void setGameMode(GameMode mode) {
    this.gameMode = mode;
  }

  @Override
  public GameMode getGameMode() {
    return this.gameMode;
  }

  /**
   * The observable (= server) has just published that it has changed its state. The Model needs to
   * be updated accordingly here.
   */
  private void handleChangeEvent() {
    if (server.areTwoPlayersConnected() && getState().getCurrentPhase() == Phase.WAITING) {
      getState().setCurrentPhase(Phase.RUNNING);
      notifyListeners(NETWORK_UPDATE);
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    handleChangeEvent();
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener pcl) {
    requireNonNull(pcl);
    support.addPropertyChangeListener(pcl);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener pcl) {
    requireNonNull(pcl);
    support.removePropertyChangeListener(pcl);
  }

  /**
   * Invokes the firing of an event, such that any attached observer (i.e., {@link
   * PropertyChangeListener}) is notified that a change happened to this chess.model.
   */
  private void notifyListeners(String change) {
    if (change.equals(NEW_MOVE)) {
      support.firePropertyChange(NEW_MOVE, null, this);
    } else if (change.equals(NETWORK_UPDATE)) {
      support.firePropertyChange(NETWORK_UPDATE, null, this);
    }
  }

  @Override
  public GameState getState() {
    return state;
  }

  @Override
  public boolean move(Cell from, Cell to) {
    Player currentPlayer = state.getCurrentPlayer();
    Phase currentPhase = state.getCurrentPhase();
    /*
    Check whether the pawn on the cell that is selected to be moved actually
    does exist and does have the right color.
    */
    boolean fromValid = checkFromCell(from, currentPlayer);
    boolean toWithinBounds = state.getField().isWithinBounds(to);
    if (currentPhase == Phase.RUNNING && fromValid && toWithinBounds) {
      if (getPossibleMovesForPawn(from).contains(to)) {
        executeMove(from, to);
        if (this.getGameMode() == GameMode.NETWORK) {
          this.state.increaseAge();
        }
        notifyListeners(NEW_MOVE);
        return true;
      }
    }
    return false;
  }

  /**
   * The winner of the game, depending on the circumstances of how the game ended, is determined. In
   * the case of a pawn reaching the opponent's starting line the player of that move is declared a
   * winner. In the case of the inability to move of both players the winner is determined by
   * counting the remaining pawns on the field in order to find the player with the maximum amount
   * of pawns.
   *
   * @param movedTo the cell that the pawn was set to in the last successful move
   */
  private void handleWinningCase(Cell movedTo) {
    state.setCurrentPhase(Phase.FINISHED);
    int movedToRow = movedTo.getRow();
    boolean reachedFinishLine = movedToRow == FIRST_ROW || movedToRow == LAST_ROW;
    if (reachedFinishLine) {
      state.setWinner(state.getCurrentPlayer());
    } else {
      countPawns();
    }
  }

  /**
   * Set pawn on the new field, remove the pawn from the starting field and handle both winning
   * scenario and scenario of the continued game, depending on which case occurs.
   *
   * @param fromC the cell that is the starting point of the move
   * @param toC the cell that is the end point of the move
   */
  private void executeMove(Cell fromC, Cell toC) {
    state.getField().set(toC, new Pawn(state.getCurrentPlayer()));
    state.getField().remove(fromC);
    if (isGameOver(toC)) {
      handleWinningCase(toC);
    } else {
      state.setCurrentPlayer(handleNextTurn());
    }
  }

  /**
   * The next current player is determined and returned in order to become the new CurrentPlayer.
   * This means that the player who regularly should be next is examined whether he is able to move
   * with at least on of his pawns or not.
   *
   * @return the player that is determined to be set as the new current player for the next round
   */
  private Player handleNextTurn() {
    if (!canMove(otherPlayer(state.getCurrentPlayer()))) {
      return state.getCurrentPlayer();
    } else if (this.getGameMode() == GameMode.SINGLE && !canMove(Player.WHITE)) {
      return otherPlayer(state.getCurrentPlayer());
    } else {
      return otherPlayer(state.getCurrentPlayer());
    }
  }

  /**
   * Count all the remaining pawns on the field in order to determine the player with the most pawns
   * left and set this player as the winner of the game.
   */
  private void countPawns() {
    int countBlack = 0;
    int countWhite = 0;
    for (int row = LAST_ROW; row >= FIRST_ROW; row--) {
      for (int col = FIRST_COLUMN; col < GameField.SIZE; col++) {
        chess.model.Cell currentCell = new chess.model.Cell(col, row);
        Optional<Pawn> cellInput = state.getField().get(currentCell);
        if (cellInput.isPresent() && cellInput.get().getPlayer().toString().equals("White")) {
          countWhite++;
        } else if (cellInput.isPresent()
            && cellInput.get().getPlayer().toString().equals("Black")) {
          countBlack++;
        }
      }
    }
    setWinnerByPawnCount(countWhite, countBlack);
  }

  /**
   * Determine the winner of the game by comparing the number of remaining pawns of the two players.
   * If the number of pawn is the same, the winner is set null (since there is no winner).
   *
   * @param white int that represents the number of white pawns left
   * @param black int that represents the number of black pawns left
   */
  private void setWinnerByPawnCount(int white, int black) {
    if (white > black) {
      state.setWinner(Player.WHITE);
    } else if (black > white) {
      state.setWinner(Player.BLACK);
    } else {
      state.setWinner(null);
    }
  }

  /**
   * Determine whether the game has been finished through the previous successful move.
   *
   * @param movedTo the cell that the pawn was set to in the last successful move
   * @return true if one of the two conditions of a finished game are met, false otherwise.
   */
  private boolean isGameOver(Cell movedTo) {
    int movedToRow = movedTo.getRow();
    boolean reachedFinishLine = movedToRow == FIRST_ROW || movedToRow == LAST_ROW;
    boolean canWhiteMove = canMove(Player.WHITE);
    boolean canBlackMove = canMove(Player.BLACK);

    return (reachedFinishLine || (!canWhiteMove && !canBlackMove));
  }

  /**
   * Check if the selected player has at least one option to do a move or if he has to miss a turn.
   *
   * @param player the selected player whose options are examined
   * @return boolean: <code>true</code> if there actually is at least one possible move, <code>false
   *     </code> otherwise.
   */
  private boolean canMove(Player player) {
    for (int row = LAST_ROW; row >= FIRST_ROW; row--) {
      for (int col = FIRST_COLUMN; col < GameField.SIZE; col++) {
        chess.model.Cell currentCell = new chess.model.Cell(col, row);
        Optional<Pawn> cellInput = state.getField().get(currentCell);
        if (cellInput.isPresent()
            && cellInput.get().getPlayer() == player
            && !getPossibleMovesForPawn(currentCell).isEmpty()) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * This function helps to determine the direction of a move. This is necessary because the pawns
   * of the white player move UP (by +1 or +2 steps) but the pawns of the black player move DOWN (by
   * -1 or -2 steps). This method returns an int that is used to specify the direction of the move -
   * positive for up, negative for down.
   *
   * @param player a String that represent the color of the current player.
   * @return an int: 1 it the player is white, -1 it the player is black.
   */
  int determineMoveDirection(Player player) {
    if (player == Player.WHITE) {
      return 1;
    } else {
      return -1;
    }
  }

  /**
   * This method determines the player, that is currently NOT playing. This is necessary for the
   * validation of an attempted move.
   *
   * @param curPlayer the current player
   * @return the other player
   */
  Player otherPlayer(Player curPlayer) {
    if (curPlayer == Player.WHITE) {
      return Player.BLACK;
    } else {
      return Player.WHITE;
    }
  }

  /**
   * This method checks the cell that was selected for the starting point of the move. It returns
   * whether the pawn on the field is matching the current player. Also, if the given cell is out of
   * bounds or there is no pawn on the field or a pawn of the wrong color, the method returns false.
   *
   * @param inspectedField the inspected cell
   * @param curPlay the current Player
   * @return boolean, <code>true</code> when the pawn on the cell matches the current player, <code>
   *     false</code> otherwise
   */
  private boolean checkFromCell(Cell inspectedField, Player curPlay) {
    boolean withinBounds = state.getField().isWithinBounds(inspectedField);
    if (withinBounds && state.getField().get(inspectedField).isPresent()) {
      return state.getField().get(inspectedField).get().getPlayer() == curPlay;
    } else {
      return false;
    }
  }

  /** Fill the field with pawns using the initial lineup. */
  private void initializeField() {
    Pawn whitePawn = new Pawn(Player.WHITE);
    Pawn blackPawn = new Pawn(Player.BLACK);
    for (int c = FIRST_COLUMN; c < GameField.SIZE; c++) {
      Cell cell = new Cell(c, 0);
      state.getField().set(cell, whitePawn);
    }
    for (int c = FIRST_COLUMN; c < GameField.SIZE; c++) {
      Cell cell = new Cell(c, 7);
      state.getField().set(cell, blackPawn);
    }
  }

  @Override
  public Set<Cell> getPossibleMovesForPawn(Cell cell) {
    Set<Cell> possibleMoves = new HashSet<>();
    Optional<Pawn> pawnOnCell = state.getField().get(cell);
    int moveDirection = determineMoveDirection(pawnOnCell.get().getPlayer());
    Cell possibleCellForMoveDiagonallyLeft =
        new Cell(cell.getColumn() - 1, cell.getRow() + moveDirection);
    if (state.getField().isWithinBounds(possibleCellForMoveDiagonallyLeft)
        && state.getField().get(possibleCellForMoveDiagonallyLeft).isPresent()) {
      if (state.getField().get(possibleCellForMoveDiagonallyLeft).get().getPlayer()
          == otherPlayer(pawnOnCell.get().getPlayer())) {
        possibleMoves.add(possibleCellForMoveDiagonallyLeft);
      }
    }
    Cell possibleCellForMoveStraight = new Cell(cell.getColumn(), cell.getRow() + moveDirection);
    if (state.getField().isWithinBounds(possibleCellForMoveStraight)
        && state.getField().get(possibleCellForMoveStraight).equals(Optional.empty())) {
      possibleMoves.add(possibleCellForMoveStraight);
    }
    Cell possibleCellForMoveTwoStepsAhead =
        new Cell(cell.getColumn(), cell.getRow() + (TWO_FIELDS * moveDirection));
    if (state.getField().isWithinBounds(possibleCellForMoveStraight)
        && state.getField().isWithinBounds(possibleCellForMoveTwoStepsAhead)
        && (cell.getRow() == FIRST_ROW || cell.getRow() == LAST_ROW)
        && state.getField().get(possibleCellForMoveStraight).equals(Optional.empty())
        && state.getField().get(possibleCellForMoveTwoStepsAhead).equals(Optional.empty())) {
      possibleMoves.add(possibleCellForMoveTwoStepsAhead);
    }
    Cell possibleCellForMoveDiagonallyRight =
        new Cell(cell.getColumn() + 1, cell.getRow() + moveDirection);
    if (state.getField().isWithinBounds(possibleCellForMoveDiagonallyRight)
        && state.getField().get(possibleCellForMoveDiagonallyRight).isPresent()) {
      if (state.getField().get(possibleCellForMoveDiagonallyRight).get().getPlayer()
          == otherPlayer(pawnOnCell.get().getPlayer())) {
        possibleMoves.add(possibleCellForMoveDiagonallyRight);
      }
    }
    return possibleMoves;
  }
}
