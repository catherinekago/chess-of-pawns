package chess.view;

import static java.lang.Math.min;

import chess.model.Cell;
import chess.model.Model;
import chess.model.Phase;
import chess.model.Player;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.swing.JPanel;

/**
 * The class DrawBoard is responsible for the display of the game board and the pawns on it in the
 * view class. Changes to the game model that affect the setup of the game board are made visible by
 * the class DrawBoard through repainting the components on the board.
 */
class DrawBoard extends JPanel {

  private static final long serialVersionUID = 1L;

  private static final Color BLACK_FIELD = new Color(150, 191, 193, 255);
  private static final Color BLACK_FIELD_WAITING = new Color(131, 127, 133, 255);
  private static final Color BORDER = new Color(97, 189, 193);
  private static final Color SELECTED_FIELD = new Color(157, 201, 68);
  private static final Color WHITE_FIELD = new Color(204, 228, 228, 255);

  private static final int BOARD_CELL_RATIO = 8;
  private static final int BOARD_STROKE = 4;
  private static final int CELL_STROKE = 2;
  private static final int CUT_PADDING_IN_HALF = 2;
  private static final int LAST_ROW = 7;
  private static final int PAWN_PADDING = 5;
  private static final int BOARD_CELLS_TIMES_TWO = 4;
  private static final int TWO_CELLS = 2;
  private static final int WINDOW_BOARD_RATIO = 6;

  private Dimension boardSize;
  private int borderX;
  private int borderY;
  private Dimension cellSize;
  private Dimension windowSize;

  private Controller controller;
  private Model model;

  /**
   * The game board is constructed by initializing the corresponding game model and controller class
   * that handles the data manipulation and setting up the container of the game board.
   *
   * @param width The width of the game window that serves as the outermost container of the GUI.
   * @param height The height of the game window that serves as the outermost container of the GUI.
   * @param model The model that holds the business logic of the game.
   * @param controller The controller that is responsible for the data manipulation.
   */
  DrawBoard(int width, int height, Model model, Controller controller) {
    this.model = model;
    this.controller = controller;
    updateDimensions(new Dimension(width, height));
    JPanel panel = new JPanel();
    panel.setSize(boardSize);
    panel.setVisible(true);
  }

  /**
   * Updates the dimensions of the window, game board and cells according to the size of the window.
   *
   * @param dim The dimensions of the game window.
   */
  void updateDimensions(Dimension dim) {
    windowSize = dim;
    int minValueOfDim = min(dim.width, dim.height);
    boardSize =
        new Dimension(
            (((minValueOfDim / WINDOW_BOARD_RATIO) * BOARD_CELLS_TIMES_TWO) / BOARD_CELL_RATIO)
                * BOARD_CELL_RATIO,
            (((minValueOfDim / WINDOW_BOARD_RATIO) * BOARD_CELLS_TIMES_TWO) / BOARD_CELL_RATIO)
                * BOARD_CELL_RATIO);
    cellSize =
        new Dimension(boardSize.width / BOARD_CELL_RATIO, boardSize.height / BOARD_CELL_RATIO);
    borderX = (windowSize.width - boardSize.width) / CUT_PADDING_IN_HALF;
    borderY = (windowSize.height - boardSize.height) / CUT_PADDING_IN_HALF - cellSize.width;
  }

  /**
   * Get the current dimensions of the cells on the game board.
   *
   * @return The dimensions of a cell.
   */
  Dimension getCellSize() {
    return cellSize;
  }

  /**
   * Get the current dimensions of the game board.
   *
   * @return The dimensions of the board.
   */
  Dimension getBoardSize() {
    return boardSize;
  }

  /**
   * Get the value that represents the leftmost point of the game field.
   *
   * @return An int that represents the leftmost point of the game field.
   */
  int getBorderX() {
    return borderX;
  }

  /**
   * Get the value that represents the uppermost point of the game field.
   *
   * @return An int that represents the uppermost point of the game field.
   */
  int getBorderY() {
    return borderY;
  }

  @Override
  protected void paintComponent(Graphics g) {
    paintFieldForPhase(g, model.getState().getCurrentPhase());
  }

  /**
   * Paints the game field depending on the current phase of the game.
   * @param g the graphics element that is used
   * @param currentPhase the phase that the game is currently in
   */
  private void paintFieldForPhase(Graphics g, Phase currentPhase) {
    if (currentPhase == Phase.WAITING) {
      g.setColor(BLACK_FIELD_WAITING);
    } else {
      g.setColor(BLACK_FIELD);
    }
    g.fillRect(borderX, borderY, boardSize.width, boardSize.height);
    Graphics2D g2 = (Graphics2D) g;

    if (currentPhase == Phase.WAITING) {
      g2.setColor(BLACK_FIELD_WAITING);
    } else {
      g.setColor(BORDER);
    }

    g2.setStroke(new BasicStroke(BOARD_STROKE));
    g2.drawRect(
        borderX - CELL_STROKE,
        borderY - CELL_STROKE,
        boardSize.width + BOARD_STROKE,
        boardSize.height + BOARD_STROKE);

    if (currentPhase == Phase.WAITING) {
      g2.setColor(BLACK_FIELD_WAITING);
    } else {
      g2.setColor(WHITE_FIELD);
    }

    g2.setStroke(new BasicStroke(CELL_STROKE));
    g2.drawRect(borderX, borderY, boardSize.width, boardSize.height);

    paintChessField(g, g2);
    paintPossibleMovesForSelectedCell(g2);

    if (!(model.getState().getCurrentPhase() == Phase.WAITING)) {
      Map<chess.model.Cell, chess.model.Player> occupiedCellMap =
          model.getState().getField().getCellsOccupiedWithPawns();

      occupiedCellMap.forEach(
          (cell, player) ->
              drawPawn(
                  player,
                  g,
                  PAWN_PADDING,
                  borderX + cell.getColumn() * cellSize.width,
                  borderY + ((LAST_ROW - cell.getRow()) * cellSize.height),
                  cellSize.width,
                  cellSize.height));
    }
  }

  /**
   * Paints the possible moves for a selected cell.
   *
   * @param g2 the graphics2D element
   */
  private void paintPossibleMovesForSelectedCell(Graphics2D g2) {
    if (controller.getMoveStartPoint().isPresent()) {
      Cell startPoint = controller.getMoveStartPoint().get();
      Set<Cell> possibleMove = model.getPossibleMovesForPawn(startPoint);
      g2.setColor(SELECTED_FIELD);
      possibleMove.forEach(
          cell ->
              g2.drawRect(
                  borderX + cell.getColumn() * cellSize.width,
                  borderY + (LAST_ROW - cell.getRow()) * cellSize.height,
                  cellSize.width,
                  cellSize.height));
    }
  }

  /**
   * Paint the chess field.
   *
   * @param g graphics element
   * @param g2 graphics2D element
   */
  private void paintChessField(Graphics g, Graphics2D g2) {
    for (int x = borderX; x < borderX + boardSize.width; x += cellSize.width * TWO_CELLS) {
      for (int y = borderY; y < borderY + boardSize.height; y += cellSize.height * TWO_CELLS) {
        if (model.getState().getCurrentPhase() == Phase.WAITING) {
          g.setColor(BLACK_FIELD_WAITING);
          g2.setColor(BLACK_FIELD_WAITING);
        } else {
          g.setColor(WHITE_FIELD);
          g2.setColor(WHITE_FIELD);
        }
        g.fillRect(x, y, cellSize.width, cellSize.height);
        g2.setStroke(new BasicStroke(CELL_STROKE));
        if (model.getState().getCurrentPhase() == Phase.WAITING) {
          g2.setColor(BLACK_FIELD_WAITING);
        } else {
          g2.setColor(Color.WHITE);
        }
        g2.drawRect(x, y, cellSize.width, cellSize.height);
      }
    }

    for (int x = borderX + cellSize.width;
        x <= borderX + boardSize.width - cellSize.width;
        x += cellSize.width * TWO_CELLS) {
      for (int y = borderY + cellSize.width;
          y <= borderY + boardSize.height - cellSize.height;
          y += cellSize.width * TWO_CELLS) {
        if (model.getState().getCurrentPhase() == Phase.WAITING) {
          g.setColor(BLACK_FIELD_WAITING);
          g2.setColor(BLACK_FIELD_WAITING);
        } else {
          g.setColor(WHITE_FIELD);
          g2.setColor(WHITE_FIELD);
        }
        g.fillRect(x, y, cellSize.width, cellSize.height);
        g2.setStroke(new BasicStroke(CELL_STROKE));
        if (model.getState().getCurrentPhase() == Phase.WAITING) {
          g2.setColor(BLACK_FIELD_WAITING);
        } else {
          g2.setColor(Color.WHITE);
        }
        g2.drawRect(x, y, cellSize.width, cellSize.height);
      }
    }
  }

  /**
   * Draw a pawn on the current selected cell.
   *
   * @param player The player that owns the cell.
   * @param g The {@link Graphics} object that allows to draw on the board.
   * @param padding Used to determine the gap-size between the cell and its border
   * @param x The coordinate marking the left point of the cell.
   * @param y The coordinate marking the upper point of the cell.
   * @param cellWidth The width of the cell.
   * @param cellHeight The height of the cell.
   * @throws IllegalArgumentException if the color of the selected pawn doesn't match the only valid
   *     options.
   */
  private void drawPawn(
      Player player, Graphics g, int padding, int x, int y, int cellWidth, int cellHeight) {
    Optional<Image> imgOpt = Optional.empty();

    switch (player) {
      case WHITE:
        g.setColor(Color.WHITE);
        imgOpt = ResourceLoader.WHITE_PAWN;
        break;
      case BLACK:
        g.setColor(Color.BLACK);
        imgOpt = ResourceLoader.BLACK_PAWN;
        break;
      default:
        throw new RuntimeException("Unhandled player: " + player);
    }

    if (imgOpt.isPresent()) {
      g.drawImage(
          imgOpt.get(),
          x + padding,
          y + padding,
          cellWidth - 2 * padding,
          cellHeight - 2 * padding,
          null);
    } else {
      System.out.println("There is no image available.");
    }
  }
}
