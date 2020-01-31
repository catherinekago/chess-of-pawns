package chess.model;

/** This class represents the tree structure of the MinMax algorithm. */
class Tree {
  private Node root;

  /**
   * Getter: gets the node that is set as the root of the tree.
   *
   * @return an object of the class Node.
   */
  Node getRoot() {
    return root;
  }

  /**
   * Setter: allows to set an object of the class Node as the root of the tree.
   *
   * @param root an object of the class Node.
   */
  void setRoot(Node root) {
    this.root = root;
  }
}
