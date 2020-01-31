package chess.model;

import java.util.Objects;

/**
 * The class Cell is used to handle the fields on the chess board (= a cell) by holding row and
 * column values of the specific field and providing methods to provide this information for further
 * operations.
 */
public class Cell implements Comparable<Cell> {

  private int column;
  private int row;

  /**
   * This constructor takes two integers for the column and the row value and create a object of the
   * class Cell with it.
   *
   * @param column the number that represents the column
   * @param row the number that represents the row
   */
  public Cell(int column, int row) {
    this.column = column;
    this.row = row;
  }

  /**
   * Getter to return the column of this cell as integer index. Column values range from 0 to 7 and
   * describe chess columns A to H, respectively.
   *
   * @return the column of this cell, as integer index
   */
  public int getColumn() {
    return column;
  }

  /**
   * Returns the row of this cell as integer index. Row values range from 0 to 7 and describe chess
   * rows 1 to 8, respectively.
   *
   * @return the row of this cell, as integer index
   */
  public int getRow() {
    return row;
  }

  @Override
  public int compareTo(Cell other) {
    int colDiff = column - other.getColumn();
    if (colDiff == 0) {
      return row - other.getRow();
    }
    return colDiff;
  }

  @Override
  public int hashCode() {
    return Objects.hash(column, row);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Cell)) {
      return false;
    }

    Cell other = (Cell) obj;
    return Objects.equals(column, other.column) && Objects.equals(row, other.row);
  }

  @Override
  public String toString() {
    return "(" + column + "," + row + ")";
  }
}