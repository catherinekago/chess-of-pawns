package chess.model;

import java.util.ArrayList;
import java.util.List;

/** This class represents the nodes of the tree of the MiniMax algorithm. */
class Node {

  private Chess chess;
  private double evaluationValue;
  private boolean isMax;
  private Cell movedFrom;
  private Cell movedTo;
  private List<Node> children;

  /**
   * Construct a node with a value for the chess game and a value for the determination if the node
   * lies on a "max" or a "min" level.
   *
   * @param from the cell the pawn of the last move had previously moved away from. @ param to the
   *     cell the pawn of the last move had previously moved to in order to get this new game set
   *     up.
   * @param chess an object of the class Chess that represents the state that is currently inspected
   *     within the algorithm.
   * @param isMax true if the node is on a "max" level, false otherwise.
   */
  Node(Chess chess, Cell from, Cell to, boolean isMax) {
    this.movedFrom = from;
    this.movedTo = to;
    this.chess = chess;
    this.isMax = isMax;
    children = new ArrayList<>();
  }

  /**
   * Getter method that grants access to its chess attribute.
   *
   * @return the chess attribute of the node.
   */
  Chess getChessStateOfNode() {
    return chess;
  }

  /**
   * Getter method that returns the isMax attribute of the node which states if the node is on a
   * "max" or a "min" level of the algorithm.
   *
   * @return true if the node is on a "max" level, false otherwise.
   */
  boolean isMax() {
    return isMax;
  }

  Cell getMovedFrom() {
    return this.movedFrom;
  }

  Cell getMovedTo() {
    return this.movedTo;
  }

  /**
   * Getter: gets the computed evaluation value attribute of the node.
   *
   * @return value a double that represents the computed evaluation of the node.
   */
  double getEvaluationValue() {
    return this.evaluationValue;
  }

  /**
   * Setter: allows to set the computed evaluation value attribute of the node.
   *
   * @param value a double that represents the computed evaluation of the node.
   */
  void setEvaluationValue(double value) {
    this.evaluationValue = value;
  }

  /**
   * Allows to add a child to the children atttribute of the node.
   *
   * @param newChild the child that is about to be included to the children list.
   */
  void addChildren(Node newChild) {
    children.add(newChild);
  }

  /**
   * Getter: returns a list of the children of the node.
   *
   * @return a list that consists of all noted children.
   */
  List<Node> getChildren() {
    return children;
  }
}
