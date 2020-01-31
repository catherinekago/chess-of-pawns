package chess.model;

import static chess.model.Chess.FIRST_ROW;
import static chess.model.Chess.LAST_ROW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MiniMaxAlgorithm implements StrategyEvaluation {

  private static final int ALGORITHM_DEPTH = 3;
  private static final double WINNING_EVALUATION_VALUE = 5000;
  private Tree algorithmTree;

  /** Getter: returns the value of the algorithmTree attribute. */
  Tree getTree() {
    return algorithmTree;
  }

  /**
   * Build the corresponding tree for the minimax algorithm from scratch. This means that the
   * current chess game is set as the root of the tree and further notes are calculated by another
   * call of the method buildTree with the root as a starting point.
   *
   * @param chess the chess game that serves as the starting point of the tree.
   */
  private void buildTree(Chess chess) {
    algorithmTree = new Tree();
    Node root = new Node(new Chess(chess), null, null, true);
    algorithmTree.setRoot(root);
    algorithmTree
        .getRoot()
        .getChessStateOfNode()
        .getState()
        .setCurrentPlayer(chess.getState().getCurrentPlayer());
    buildTree(algorithmTree.getRoot(), 0);
  }

  /**
   * Creates a tree for the minimax algorithm that is ALGORITHM_DEPTH levels deep and consists of
   * nodes that represent possible "look ahead" states of the current chess game.
   *
   * @param node the node that serves as the starting point of the calculations.
   * @param depth the current depth level within the built tree
   */
  private void buildTree(Node node, int depth) {
    calculateChildren(node);
    if (depth != ALGORITHM_DEPTH) {
      depth++;
      for (Node child : node.getChildren()) {
        buildTree(child, depth);
      }
    }
  }

  /**
   * Execute the whole miniMax algorithm by creating the corresponding tree and finding the best
   * value among the paths of the tree.
   *
   * @param chess the chess game that serves as starting point of the calculations.
   */
  void executeMinimax(Chess chess) {
    buildTree(chess);
    findBestEvaluation(this.algorithmTree.getRoot(), 0);
  }

  /**
   * Finds the maximum value that equals the maximum of all evaluations of the leaves of the
   * algorithm tree.
   *
   * @param node the node that is currently inspected.
   * @param algorithmDepth the current algorithm depth.
   * @return a double that equals the maximum evaluation value of all possible moves.
   */
  private Double findBestEvaluation(Node node, int algorithmDepth) {
    boolean isLeaf = algorithmTree.getRoot().getChildren().isEmpty();

    if (isLeaf || algorithmDepth == ALGORITHM_DEPTH) {
      node.setEvaluationValue(evaluateState(node, algorithmDepth));
      return evaluateState(node, algorithmDepth);
    }
    double maxValue = -Double.MAX_VALUE;
    double minValue = Double.MAX_VALUE;
    if (node.isMax()) {
      algorithmDepth++;
      for (Node child : node.getChildren()) {
        if (algorithmDepth == ALGORITHM_DEPTH) {
          child.setEvaluationValue(findBestEvaluation(child, algorithmDepth));
        } else {
          child.setEvaluationValue(
              findBestEvaluation(child, algorithmDepth) + evaluateState(child, algorithmDepth));
        }
        maxValue = Math.max(maxValue, child.getEvaluationValue());
      }
      node.setEvaluationValue(maxValue);
      return maxValue;
    } else {
      algorithmDepth++;
      for (Node child : node.getChildren()) {
        if (algorithmDepth == ALGORITHM_DEPTH) {
          child.setEvaluationValue(findBestEvaluation(child, algorithmDepth));
        } else {
          child.setEvaluationValue(
              findBestEvaluation(child, algorithmDepth) + evaluateState(child, algorithmDepth));
          minValue = Math.min(minValue, child.getEvaluationValue());
        }
      }
      node.setEvaluationValue(minValue);
      return minValue;
    }
  }

  /**
   * Calculates all the children nodes of a node and adds them to the node's list of children.
   *
   * @param root the node that is inspected.
   */
  private void calculateChildren(Node root) {
    if (root.getChessStateOfNode().getState().getCurrentPhase() == Phase.RUNNING) {
      List<Cell> allPossibleCells = findAllCellsThatCouldBeMovedTo(root);
      // System.out.println("List of all cells that could be moved to: " + allPossibleCells);
      allPossibleCells.forEach(
          cell -> {
            Map<Cell, Player> allCellsOccupied =
                root.getChessStateOfNode().getState().getField().getCellsOccupiedWithPawns();
            for (Map.Entry<Cell, Player> entry : allCellsOccupied.entrySet()) {
              if (root.getChessStateOfNode()
                  .getPossibleMovesForPawn(entry.getKey())
                  .contains(cell)) {
                Cell startMove = entry.getKey();
                Chess newChess = new Chess(root.getChessStateOfNode());
                newChess.move(startMove, cell);
                Node newNode = new Node(newChess, startMove, cell, !root.isMax());
                if (root.getChessStateOfNode()
                        .getState()
                        .getField()
                        .get(newNode.getMovedFrom())
                        .get()
                        .getPlayer()
                    == root.getChessStateOfNode().getState().getCurrentPlayer()) {
                  root.addChildren(newNode);
                }
              }
            }
          });
    }
  }

  /**
   * Find all the cells that could be move to from the inspected chess field within the inspected
   * node.
   *
   * @param root the node that is inspected.
   * @return a list of all the cells that could be moved to: if there are no valid cells, an empty
   *     list is returned.
   */
  private List<Cell> findAllCellsThatCouldBeMovedTo(Node root) {
    List<Cell> allPossibleCells = new ArrayList<>();
    if (!root.getChessStateOfNode().getState().getField().getCellsOccupiedWithPawns().isEmpty()) {
      for (Map.Entry<Cell, Player> entry :
          root.getChessStateOfNode().getState().getField().getCellsOccupiedWithPawns().entrySet()) {
        if (entry.getValue() == root.getChessStateOfNode().getState().getCurrentPlayer()) {
          allPossibleCells.addAll(
              root.getChessStateOfNode().getPossibleMovesForPawn(entry.getKey()));
        }
      }
      if (root.getChessStateOfNode().getState().getCurrentPlayer() == Player.WHITE) {
        Collections.sort(allPossibleCells);
      } else {
        Collections.sort(allPossibleCells, Collections.reverseOrder());
      }
    }
    return allPossibleCells;
  }

  @Override
  public double evaluateState(Node node, int depth) {
    double valuePawnCountOnField = evaluatePawnCountOnField(node);
    double valueDistanceToOppositeLine = evaluateDistanceToOppositeLine(node);
    double valueRiskOfCapture = evaluateRiskOfCapture(node);
    double valueIsolationOfPawns = evaluateIsolationOfPawns(node);
    double valueClosenessToWinning = evaluateClosenessToWinning(node, depth);
    return valuePawnCountOnField
        + valueDistanceToOppositeLine
        + valueRiskOfCapture
        + valueIsolationOfPawns
        + valueClosenessToWinning;
  }

  @Override
  public double evaluatePawnCountOnField(Node node) {
    Map<Cell, Player> mapOfAllPawns =
        node.getChessStateOfNode().getState().getField().getCellsOccupiedWithPawns();
    double humanPawnCount = 0;
    double machinePawnCount = 0;
    for (Map.Entry<Cell, Player> entry : mapOfAllPawns.entrySet()) {
      if (entry.getValue() == Player.WHITE) {
        humanPawnCount++;
      } else {
        machinePawnCount++;
      }
    }
    return machinePawnCount - (1.5 * humanPawnCount);
  }

  @Override
  public double evaluateDistanceToOppositeLine(Node node) {
    Map<Cell, Player> mapOfAllPawns =
        node.getChessStateOfNode().getState().getField().getCellsOccupiedWithPawns();
    double humanDistanceCount = 0;
    double machineDistanceCount = 0;
    for (int row = FIRST_ROW; row <= LAST_ROW; row++) {
      double countWhitePawns = 0;
      double countBlackPawns = 0;
      for (Map.Entry<Cell, Player> entry : mapOfAllPawns.entrySet()) {
        if (entry.getKey().getRow() == row) {
          if (entry.getValue() == Player.WHITE) {
            countWhitePawns++;
          } else {
            countBlackPawns++;
          }
        }
      }
      humanDistanceCount = humanDistanceCount + countWhitePawns * row;
      machineDistanceCount = machineDistanceCount + (countBlackPawns * (LAST_ROW - row));
    }
    return machineDistanceCount - 1.5 * humanDistanceCount;
  }

  @Override
  public double evaluateRiskOfCapture(Node node) {
    Map<Cell, Player> mapOfAllPawns =
        node.getChessStateOfNode().getState().getField().getCellsOccupiedWithPawns();
    double humanPawnsInRiskOfCapture = 0;
    double machinePawnsInRiskOfCapture = 0;
    for (Map.Entry<Cell, Player> entry : mapOfAllPawns.entrySet()) {
      if (entry.getValue() == Player.WHITE && isPawnAtRiskOfCapture(entry, Player.WHITE, node)) {
        humanPawnsInRiskOfCapture++;
      } else if (entry.getValue() == Player.BLACK
          && isPawnAtRiskOfCapture(entry, Player.BLACK, node)) {
        machinePawnsInRiskOfCapture++;
      }
    }
    return humanPawnsInRiskOfCapture - (1.5 * machinePawnsInRiskOfCapture);
  }

  /**
   * Determines whether an inspected cell is occupied with the chosen pawn.
   *
   * @param node the node that is currently inspected
   * @param entry an entry of a map that lists occupied cells and the player that the pawn on the
   *     cell belongs to.
   * @param horizontalPosition an in that represents the horizontal position on the field. -1 for
   *     one step to the left, 1 for one step to the right. The values correspond to the numbering
   *     of the columns, where a cell on the left has a lower column value than a cell on the right.
   * @param verticalPosition an int that represents the vertical position on the field. -1 for down,
   *     +1 for up. The values correspond to the numbering of the rows, where a cell within the
   *     lower part of the field has a lower row value than a cell within the top part.
   * @param inspectedPlayer the player of the pawn that is expected on the field.
   * @return true if the cell is occupied with the expected pawn, false otherwise.
   */
  private boolean isCellOccupiedWithPawn(
      Node node,
      Map.Entry<Cell, Player> entry,
      int horizontalPosition,
      int verticalPosition,
      Player inspectedPlayer) {
    return node.getChessStateOfNode()
            .getState()
            .getField()
            .isWithinBounds(
                new Cell(
                    entry.getKey().getColumn() - horizontalPosition,
                    entry.getKey().getRow() + verticalPosition))
        && node.getChessStateOfNode()
            .getState()
            .getField()
            .get(
                new Cell(
                    entry.getKey().getColumn() - horizontalPosition,
                    entry.getKey().getRow() + verticalPosition))
            .isPresent()
        && node.getChessStateOfNode()
                .getState()
                .getField()
                .get(
                    new Cell(
                        entry.getKey().getColumn() - horizontalPosition,
                        entry.getKey().getRow() + verticalPosition))
                .get()
                .getPlayer()
            == inspectedPlayer;
  }

  /**
   * Determines if the inspected pawn is at risk of being captured by a nearby pawn of the opponent.
   *
   * @param entry an entry of a map that lists occupied cells and the player that the pawn on the *
   *     cell belongs to.
   * @param playerOfInspectedPawn the player of the pawn that is on the inspected cell.
   * @param node the node that is currently inspected
   * @return true if there is at least one threatening pawn of the opponent nearby and no pawn that
   *     is member of the own color to guard the pawn on the inspected cell, false otherwise.
   */
  private boolean isPawnAtRiskOfCapture(
      Map.Entry<Cell, Player> entry, Player playerOfInspectedPawn, Node node) {
    int moveDirection = node.getChessStateOfNode().determineMoveDirection(playerOfInspectedPawn);
    boolean isLeftOccupiedWithOpponentsPawn =
        isCellOccupiedWithPawn(
            node,
            entry,
            -1,
            moveDirection,
            node.getChessStateOfNode().otherPlayer(playerOfInspectedPawn));
    boolean isRightOccupiedWithOpponentsPawn =
        isCellOccupiedWithPawn(
            node,
            entry,
            1,
            moveDirection,
            node.getChessStateOfNode().otherPlayer(playerOfInspectedPawn));
    boolean isPawnCoveredFromLeft =
        isCellOccupiedWithPawn(node, entry, -1, -1 * moveDirection, playerOfInspectedPawn);
    boolean isPawnCoveredFromRight =
        isCellOccupiedWithPawn(node, entry, 1, -1 * moveDirection, playerOfInspectedPawn);
    return (isLeftOccupiedWithOpponentsPawn || isRightOccupiedWithOpponentsPawn)
        && !(isPawnCoveredFromLeft || isPawnCoveredFromRight);
  }

  @Override
  public double evaluateIsolationOfPawns(Node node) {
    Map<Cell, Player> mapOfAllPawns =
        node.getChessStateOfNode().getState().getField().getCellsOccupiedWithPawns();
    double humanPawnsIsolated = 0;
    double machinePawnsIsolated = 0;
    for (Map.Entry<Cell, Player> entry : mapOfAllPawns.entrySet()) {
      if (entry.getValue() == Player.WHITE && isPawnIsolated(entry, node)) {
        humanPawnsIsolated++;
      } else if (entry.getValue() == Player.BLACK && isPawnIsolated(entry, node)) {
        machinePawnsIsolated++;
      }
    }
    return humanPawnsIsolated - (1.5 * machinePawnsIsolated);
  }

  /**
   * Determines whether an inspected pawn is surrounded by other pawns of its color or not.
   *
   * @param entry the entry of a map that holds all occupied cells and the players of the pawns.
   * @param node the node that is currently inspected
   * @return true if there is at least one pawn of the same color surrounding the inspected pawn,
   *     false otherwise.
   */
  private boolean isPawnIsolated(Map.Entry<Cell, Player> entry, Node node) {
    int oneStepToTheLeft = -1;
    int oneStepToTheRight = 1;
    int oneStepUp = 1;
    int oneStepDown = -1;
    Set<Cell> allCellsSurrounding = new HashSet<Cell>();
    allCellsSurrounding.add(
        new Cell(entry.getKey().getColumn() + oneStepToTheLeft, entry.getKey().getRow()));
    allCellsSurrounding.add(
        new Cell(
            entry.getKey().getColumn() + oneStepToTheLeft, entry.getKey().getRow() + oneStepUp));
    allCellsSurrounding.add(
        new Cell(entry.getKey().getColumn(), entry.getKey().getRow() + oneStepUp));
    allCellsSurrounding.add(
        new Cell(
            entry.getKey().getColumn() + oneStepToTheRight, entry.getKey().getRow() + oneStepUp));
    allCellsSurrounding.add(
        new Cell(entry.getKey().getColumn() + oneStepToTheRight, entry.getKey().getRow()));
    allCellsSurrounding.add(
        new Cell(
            entry.getKey().getColumn() + oneStepToTheRight, entry.getKey().getRow() + oneStepDown));
    allCellsSurrounding.add(
        new Cell(entry.getKey().getColumn(), entry.getKey().getRow() + oneStepDown));
    allCellsSurrounding.add(
        new Cell(
            entry.getKey().getColumn() + oneStepToTheLeft, entry.getKey().getRow() + oneStepDown));
    for (Cell cell : allCellsSurrounding) {
      if (node.getChessStateOfNode().getState().getField().isWithinBounds(cell)
          && node.getChessStateOfNode().getState().getField().get(cell).isPresent()
          && node.getChessStateOfNode().getState().getField().get(cell).get().getPlayer()
              == entry.getValue()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public double evaluateClosenessToWinning(Node node, int lookAhead) {
    if (node.getChessStateOfNode().getState().getCurrentPhase() == Phase.FINISHED
        && node.getChessStateOfNode().getState().getWinner().isPresent()) {
      if (node.getChessStateOfNode().getState().getWinner().get() == Player.WHITE) {
        return WINNING_EVALUATION_VALUE / lookAhead;
      } else {
        return 0 - (1.5 * (WINNING_EVALUATION_VALUE / lookAhead));
      }
    }
    return 0;
  }
}
