package chess;

import chess.model.Chess;
import chess.model.Model;
import chess.view.ChessController;
import chess.view.ChessView;
import chess.view.Controller;
import chess.view.View;
import javax.swing.SwingUtilities;

/**
 * Main class of the chess application. Its only purpose is to set up the chess.model-, chess.view-, and
 * controller-objects.
 */
public class ChessMain {

  /**
   * Invoke the actual starting method {@link ChessMain#showPawnGame()} on the <code>
   * AWT event dispatching thread</code>. This causes the method to be executed asynchronously after
   * all pending AWT events have been processed.
   *
   * @param args The command line arguments.
   */
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> showPawnGame());
  }

  /**
   * Initializes the main {@link Model}, {@link View}, and {@link Controller} classes of this game,
   * and sets the appropriate relations between each other accordingly.
   */
  private static void showPawnGame() {
    Model model = (Model) new Chess();
    Controller controller = (Controller) new ChessController(model);
    View view = new ChessView(model, controller);
    controller.setView(view);
    model.addPropertyChangeListener(view);
    controller.start();
  }
}
