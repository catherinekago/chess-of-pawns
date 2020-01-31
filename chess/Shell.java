package chess;

import chess.model.Cell;
import chess.model.Chess;
import chess.model.GameField;
import chess.model.GameMode;
import chess.model.Pawn;
import chess.model.Phase;
import chess.model.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * The class Shell works as the interface for the user of the chess program. The user can interact
 * with the program through commands. Once an object of the class Shell is created, it communicates
 * with the methods of the class Chess in order to execute the user's commands.
 */
public class Shell {

  private static final char START_LETTER = 'A';
  private static final int START_ROW = 1;
  private static final String PROMPT = "Chess> ";
  private static final String ERR_MSG = "Error! ";
  private static final String WHITE_PAWN = "w";
  private static final String BLACK_PAWN = "b";
  private static final String NO_PAWN = ".";
  private static final String HELP_MSG =
      "This is a chess game that is played with just the pawns. "
          + "Here's a list of valid user input:"
          + "\r\n"
          + "NEW - start a new game."
          + "\r\n"
          + "MOVE from to - move a pawn from one location "
          + "to another (like 'MOVE D3 D4')."
          + "\r\n"
          + "The rules by which a pawn "
          + "can be moved "
          + "are the same as in a normal game of chess."
          + "\r\n"
          + "PRINT - the current game field will be displayed, in company with the "
          + "current game phase and the current player."
          + "\r\n"
          + "HELP - this will open up this little overview."
          + "\r\n"
          + "QUIT - this ends the current game."
          + "\r\n"
          + "The player who reaches the other side first wins the game. Good luck mate!";

  private Chess chess;

  /**
   * Read and process input until the quit command has been entered.
   *
   * @param args Command line arguments.
   * @throws IOException Error reading from in.
   */
  public static void main(String[] args) throws IOException {
    final Shell shell = new Shell();
    shell.run();
  }

  /**
   * Read the user's input and execute his commands by communicating with the methods of the Chess
   * class. Also, prints an error message if the user input doesn't match a valid option.
   *
   * @throws IOException Error reading from in.
   */
  public void run() throws IOException {
    BufferedReader in =
        new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    boolean quit = false;
    while (!quit) {
      System.out.print(PROMPT);
      String input = in.readLine();
      if (input == null) {
        System.out.println(ERR_MSG + "Empty command. Type HELP in order to see valid options.");
        break;
      }
      String[] tokens = input.trim().split(" ");
      if (tokens.length == 1) {
        String userCommand = tokens[0].toUpperCase();
        switch (userCommand) {
          case "PRINT":
            if (chess != null) {
              printGameField(this.chess);
            } else {
              System.out.println(
                  ERR_MSG
                      + "The game hasn't been initialized yet. "
                      + "Type NEW to start a new game.");
            }
            break;
          case "HELP":
            System.out.println(HELP_MSG);
            break;
          case "QUIT":
            quit = true;
            break;
          default:
            System.out.println(
                ERR_MSG
                    + "Invalid input: '"
                    + input
                    + "'. Type HELP "
                    + "in order to see valid options.");
            break;
        }
      } else if (tokens[0].toUpperCase().equals("NEW")
          && tokens[1].toUpperCase().equals("HOTSEAT")) {
        chess = new Chess();
        chess.setGameMode(GameMode.HOTSEAT);
      } else if (tokens[0].toUpperCase().equals("NEW")
          && tokens[1].toUpperCase().equals("SINGLE")) {
        chess = new Chess();
        chess.setGameMode(GameMode.SINGLE);
      } else if (tokens[0].toUpperCase().equals("MOVE") && tokens.length == 3) {
        handleMove(tokens);
      } else {
        System.out.println(
            ERR_MSG
                + "Invalid input: '"
                + input
                + "'. Type HELP "
                + "in order to see valid options.");
      }
    }
  }

  /**
   * Checks whether the player before the last valid move and afterwards is the same and prints a
   * conclusive message. If the player didn't change after a valid move, the other player is not
   * able to move and has to miss a turn.
   *
   * @param currentBeforeMove current Player before the move
   * @param currentAfterMove current Player after the move
   * @param tokens the user input that was split up into its components and put into a string array
   */
  private void mustMissATurn(Player currentBeforeMove, Player currentAfterMove, String[] tokens) {
    if (chess.getGameMode() == GameMode.HOTSEAT || currentBeforeMove == Player.WHITE) {
      if (currentBeforeMove.toString().equals(currentAfterMove.toString())) {
        System.out.println(
            chess.getState().getCurrentPlayer() + " moved " + tokens[1] + " to " + tokens[2]);
        System.out.println(otherPlayer(currentBeforeMove).toString() + " must miss a turn");
      } else {
        System.out.println(
            otherPlayer(chess.getState().getCurrentPlayer())
                + " moved "
                + tokens[1]
                + " to "
                + tokens[2]);
      }
    }
  }

  /**
   * Determine the player that is currently NOT playing.
   *
   * @param curPlayer the current player
   * @return the other player
   */
  private Player otherPlayer(Player curPlayer) {
    if (curPlayer == Player.WHITE) {
      return Player.BLACK;
    } else {
      return Player.WHITE;
    }
  }

  /** Print the current lineup of the game field and the current player. */
  private void printGameField(Chess chess) {
    for (int row = 7; row >= 0; row--) {
      String rowNum = row + 1 + " ";
      StringBuilder line = new StringBuilder(rowNum);
      for (int col = 0; col < GameField.SIZE; col++) {
        chess.model.Cell currentCell = new chess.model.Cell(col, row);
        Optional<Pawn> maybePawn = chess.getState().getField().get(currentCell);
        if (maybePawn.isPresent()) {
          String inputPlayer = maybePawn.get().getPlayer().toString();
          if (inputPlayer.equals("White")) {
            line.append(WHITE_PAWN);
          } else {
            line.append(BLACK_PAWN);
          }
        } else {
          line.append(NO_PAWN);
        }
      }
      System.out.println(line);
    }
    System.out.println("  ABCDEFGH");
    System.out.println("Player's turn: " + chess.getState().getCurrentPlayer());
  }

  /**
   * Parse the row value of a given cell string and return the corresponding index from 0-7.
   *
   * <p>For example, <code>parseRowValue("C1") = 0</code>
   *
   * @param value the string to parse
   * @return the corresponding row index, from 0-7
   * @throws IllegalArgumentException if the the given value is no valid input for the row value
   */
  private int parseRowValue(String value) {
    char number = value.charAt(1);
    if (value.length() != 2) {
      throw new IllegalArgumentException(
          "'"
              + value.substring(1)
              + "' is not a valid valid input for the row value. "
              + " It has to be some number from 1 to 8.");
    } else if (!Character.isDigit(number)) {
      throw new IllegalArgumentException("Char '" + number + "' is not a number.");
    } else {
      return Character.getNumericValue(number) - START_ROW;
    }
  }

  /**
   * Parse the column value of a given cell string and return the corresponding index from 0-7.
   *
   * <p>For example, <code>parseColumnValue("C1") = 2</code>
   *
   * @param value the string to parse
   * @return the corresponding row index, from 0-7
   * @throws IllegalArgumentException if the given value is not a letter
   */
  private int parseColumnValue(String value) {
    char letter = value.charAt(0);
    if (!Character.isLetter(letter)) {
      throw new IllegalArgumentException("Char '" + letter + "' is not a letter.");
    }
    return letter - START_LETTER;
  }

  /**
   * Handle the move action by calling the move(from, to) method from the class Chess and dealing
   * with its outcome.
   *
   * @param token the user input
   */
  private void handleMove(String[] token) {
    if (chess != null) {
      chess.model.Cell fromCell =
          new chess.model.Cell(parseColumnValue(token[1]), parseRowValue(token[1]));
      chess.model.Cell toCell =
          new chess.model.Cell(parseColumnValue(token[2]), parseRowValue(token[2]));
      Player currentPlayerLastMove = chess.getState().getCurrentPlayer();
      if (chess.move(fromCell, toCell)) {
        if (chess.getState().getCurrentPhase() == Phase.FINISHED) {
          System.out.println(
              chess.getState().getCurrentPlayer() + " moved " + token[1] + " to " + token[2]);
          if (chess.getState().getWinner().isPresent()) {
            System.out.println(
                "Game over. " + chess.getState().getWinner().get().toString() + " has won");
          } else {
            System.out.println("Game over. Draw!");
          }
        } else {
          Player currentPlayerNextMove = chess.getState().getCurrentPlayer();
          mustMissATurn(currentPlayerLastMove, currentPlayerNextMove, token);
          if (chess.getGameMode() == GameMode.SINGLE
              && chess.getState().getCurrentPlayer() == Player.BLACK) {
            chess.executeKiMove();
            if (chess.getState().getCurrentPhase() == Phase.FINISHED) {
              if (chess.getState().getWinner().isPresent()) {
                System.out.println(
                    "Game over. " + chess.getState().getWinner().get().toString() + " has won");
              } else {
                System.out.println("Game over. Draw!");
              }
            }
          }
        }

      } else {
        System.out.println(
            ERR_MSG
                + " Could not move "
                + token[1]
                + " to "
                + token[2]
                + "."
                + isGameRunning()
                + isCellWithinBounds(fromCell, toCell)
                + isSelectedCellValid(fromCell));
      }
    } else {
      System.out.println(
          ERR_MSG + "The game hasn't been initialized yet. " + "Type NEW to start a new game.");
    }
  }

  /**
   * Checks if the reason for the invalid move is the fact that the game has ended and returns a
   * corresponding message.
   *
   * @return String with an error message if the game has already ended, an empty String otherwise
   */
  private String isGameRunning() {
    if (chess.getState().getCurrentPhase() == Phase.FINISHED) {
      return (" The current game has been finished. Type NEW to start a new game.");
    } else {
      return "";
    }
  }

  /**
   * Check if the move failed because the user gave a cell that is out of bounds.
   *
   * @param from the cell that was selected to start the move
   * @param to the cell that was selected as the end point of the move
   * @return a message that describes which of the two given cells was not valid. If the first one
   *     was invalid, the second one is not checked anymore. Also, if the given cells are valid, an
   *     empty String is returned.
   */
  private String isCellWithinBounds(Cell from, Cell to) {
    boolean currentPhaseRunning = chess.getState().getCurrentPhase() == Phase.RUNNING;
    boolean fromNotWithinBounds =
        from.getRow() < 0 || from.getRow() > 7 || from.getColumn() < 0 || from.getColumn() > 7;
    boolean toNotWithinBounds =
        to.getRow() < 0 || to.getRow() > 7 || to.getColumn() < 0 || to.getColumn() > 7;
    if (currentPhaseRunning) {
      if (fromNotWithinBounds) {
        return " The selected starting point of the move is outside the bounds of the game field. "
            + "\r\n"
            + "Type PRINT in order to take a look at the dimensions of the game field.";
      } else if (toNotWithinBounds) {
        return " The selected end point of the move is outside the bounds of the game field."
            + "\r\n"
            + "Type PRINT in order to take a look at the dimensions of the game field.";
      }
    }
    return "";
  }

  /**
   * Check if the cell that was selected as the starting point of the move was invalid and therefore
   * causing the move to fail and returns a corresponing message.
   *
   * @param from the cell that was selected as the starting point of the move
   * @return a String containing a message if the selected cell contains a pawn of the opponent or
   *     was empty. Also, the method returns an empty string if the selected cell was valid.
   */
  private String isSelectedCellValid(Cell from) {
    boolean currentPhaseRunning = chess.getState().getCurrentPhase() == Phase.RUNNING;
    boolean fromNotWithinBounds =
        from.getRow() < 0 || from.getRow() > 7 || from.getColumn() < 0 || from.getColumn() > 7;
    if (currentPhaseRunning) {
      if (!fromNotWithinBounds) {
        Optional<Pawn> selected = chess.getState().getField().get(from);
        if (selected.isPresent()
            && selected.get().getPlayer() == otherPlayer(chess.getState().getCurrentPlayer())) {
          return " It's the " + chess.getState().getCurrentPlayer().toString() + " player's turn.";
        } else if (selected.equals(Optional.empty())) {
          return " The selected cell is empty.";
        } else {
          return "";
        }
      } else {
        return "";
      }
    } else {
      return "";
    }
  }
}
