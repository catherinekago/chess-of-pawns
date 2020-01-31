package chess.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The class GameField holds the information about the chess board and how to check upon the content
 * of the cells and manipulate the cells of the field. This means that there are methods to check on
 * a cell on the board whether it contains a pawn or not, to set a pawn to a specific cell and to
 * remove one from a cell. Also, the method throwErrorWhenOutOfBounds checks whether the row and
 * column values of a cell are within the bounds of the chess board which comes in handy when
 * checking if a given moving action is valid or not.
 */
public class GameField implements Serializable {

  public static final int SIZE = 8;

  private Pawn[][] field = new Pawn[SIZE][SIZE];

  /**
   * Check whether there is a pawn set on a specific cell or not. A cell can be empty, which is why
   * the Optional class is used.
   *
   * @param cell the cell that is checked if it contains a pawn or not
   * @return an Optional pawn if non-null, otherwise returns an empty Optional.
   */
  public Optional<Pawn> get(Cell cell) {
    throwErrorWhenOutOfBounds(cell);
    int cellRow = cell.getRow();
    int cellCol = cell.getColumn();
    return Optional.ofNullable(field[cellRow][cellCol]);
  }

  /**
   * Returns all {@link Cell cells} that are currently occupied by a pawn.
   *
   * @return A map with all cells that have a pawn on them.
   */
  public Map<Cell, Player> getCellsOccupiedWithPawns() {
    Map<Cell, Player> occupiedCells = new HashMap<Cell, Player>();
    for (int row = 7; row >= 0; row--) {
      for (int col = 0; col < GameField.SIZE; col++) {
        chess.model.Cell currentCell = new chess.model.Cell(col, row);
        Optional<Pawn> cellInput = get(currentCell);
        if (cellInput.isPresent()) {
          occupiedCells.put(currentCell, cellInput.get().getPlayer());
        }
      }
    }
    return occupiedCells;
  }

  /**
   * Set a pawn on the given cell. Any pawns already on that cell will be overridden.
   *
   * @param cell cell to set pawn on
   * @param newValue new value (pawn) to set on the cell
   * @throws IllegalArgumentException if given cell is out of field bounds
   */
  void set(Cell cell, Pawn newValue) {
    int cellRow = cell.getRow();
    int cellCol = cell.getColumn();
    field[cellRow][cellCol] = newValue;
  }

  /**
   * Remove pawn from the given cell. This method only has to work if there is a pawn on the cell.
   *
   * @param cell cell to remove any pawn from
   * @return the pawn that was removed
   * @throws IllegalArgumentException if given cell is out of field bounds
   */
  Pawn remove(Cell cell) {
    int cellRow = cell.getRow();
    int cellCol = cell.getColumn();
    Optional<Pawn> toBeRemoved = get(cell);
    assert toBeRemoved.isPresent()
        : "Selected cell " + cell + " is empty. Could not execute " + "method 'remove'";
    field[cellRow][cellCol] = null;
    return toBeRemoved.get();
  }

  /**
   * Checks a cell for its bounds and throws an exception in case of failure.
   *
   * @param cell The cell to be checked.
   */
  private void throwErrorWhenOutOfBounds(Cell cell) {
    if (!isWithinBounds(cell)) {
      throw new IllegalArgumentException("Coordinates of cell are out of bounds: " + cell);
    }
  }

  /**
   * Checks whether a given cell lies wihin the bound of the game field or not.
   *
   * @param cell the inspected cell
   * @return true if the cell lies within the bounds, false otherwise
   */
  public boolean isWithinBounds(Cell cell) {
    return cell.getColumn() >= 0
        && cell.getColumn() < SIZE
        && cell.getRow() >= 0
        && cell.getRow() < SIZE;
  }
}
