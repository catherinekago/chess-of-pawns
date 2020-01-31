package chess.model;

public interface StrategyEvaluation {

  /**
   * Combine all evaluations functions to calculate a final value to evaluate the state of the game.
   *
   * @param depth an int that represents how many moves the algorithm is currently looking ahead.
   * @param node the game state that is currently evaluated.
   * @return a double that represents the final evaluation of the state.
   */
  double evaluateState(Node node, int depth);

  /**
   * Evaluate the current setting of the game regarding the remaining pawns on the field.
   *
   * @param node the game state that is currently evaluated.
   * @return a double that represents the evaluation of the ratio of remaining pawns: the number
   *     lies between -12 (no black pawns, all 8 white pawns remaining) and 8 (no white pawns, all 8
   *     black pawns remaining). The higher the value, the better the pawn situation for the black
   *     player.
   */
  double evaluatePawnCountOnField(Node node);

  /**
   * Evaluates the current setting of the game regarding the positions of the pawns according to the
   * starting line of the opponent.
   *
   * @param node the game state that is currently evaluated.
   * @return a double that represents the evaluation of the ratio of already made progress. The
   *     higher the value, the better the progress situation of the black player.
   */
  double evaluateDistanceToOppositeLine(Node node);

  /**
   * Evaluates the current setting of the game regarding the risk of a pawn of getting captured by
   * an opponent's pawn. If a pawn gets covered by another member of his own color, he is not
   * declared as being at risk - even if there is a pawn of the opponent that could capture it.
   *
   * @param node the game state that is currently evaluated.
   * @return a double that represents the evaluation of the ratio of risk of capture. If it is 0,
   *     then no pawn on the field is at risk. The higher the value, the less the risk for the black
   *     pawn, proportionally.
   */
  double evaluateRiskOfCapture(Node node);

  /**
   * Evaluates the current setting of the game regarding the isolation of the pawns. Isolation
   * occurs, if none of the eight cells surrounding a pawn hold a pawn of the same color.
   *
   * @param node the game state that is currently evaluated
   * @return a double that represents the ratio of isolated pawns on the field. It is 0 if no pawn
   *     is isolated and the higher the value, the better the game field situation for the black
   *     pawn, proportionally.
   */
  double evaluateIsolationOfPawns(Node node);

  /**
   * Evaluates the current setting of the game regarding the closeness of a player to winning the
   * game.
   *
   * @param node the game state that is currently evaluated
   * @param lookAhead an int that represents how many moves in the future the currently looked at
   *     state is.
   * @return a double that represents the ratio of winning chances.
   */
  double evaluateClosenessToWinning(Node node, int lookAhead);
}
