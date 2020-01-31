package chess.network;

import static java.util.Objects.requireNonNull;

import chess.model.GameState;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 * The server class of the chess.network chess game. It handles the server initialization and the
 * connections to the server.
 */
public class Server {

  private final PropertyChangeSupport support = new PropertyChangeSupport(this);

  private static final String STATE_CHANGED = "State changed";
  private static final String CONNECTION_LOSS = "Socket closed due to connection loss";
  private static final String SERVER_FULL = "Server full";
  private static final String WINDOW_CLOSE = "Socket closed due to window closing event";
  private static ServerSocket serverSocket;
  private static ObjectOutputStream outToFirst;
  private static ObjectOutputStream outToSecond;
  private static ObjectInputStream inFromFirst;
  private static ObjectInputStream inFromSecond;
  public static final int PORT = 43200;

  private ArrayList<Socket> connections;
  private boolean networkConnection;

  /**
   * This method invokes the server. A new thread is started in order to guarantee multithreaded
   * program execution.
   */
  public void startServer() {
    connections = new ArrayList<>();
    networkConnection = true;
    Thread connectorThread = new Thread(this::startNetworkCommunication);
    connectorThread.setDaemon(true);
    connectorThread.start();
  }

  /** Accept socket connections and handle receiving and sending of messages through the chess.network. */
  private void startNetworkCommunication() {
    try {
      serverSocket = new ServerSocket(PORT);
      while (networkConnection) {
        Socket newConnection = serverSocket.accept();
        if (newConnection != null && acceptConnections(newConnection)) {
          newConnection.setSoTimeout(30 * 1000);
          Thread receivingThread = new Thread(() -> receiveStates(newConnection));
          receivingThread.start();
        }
      }

    } catch (SocketException socketE) {
      if (socketE.getMessage().equals("Interrupted function call: accept failed")) {
        try {
          serverSocket.close();
        } catch (IOException ioE) {
          ioE.printStackTrace();
        }
      } else if (socketE.getMessage().equals("Connection reset by peer: socket write error")) {
        System.err.println("Peer cancelled connection");
      }
    } catch (IOException e) {
      System.err.println("IO Exception occurred!");
      e.printStackTrace();
    }
  }

  /**
   * Accept incoming requests for establishing server connections and initiate input and output
   * streams of the previously connected socket, if the server's capacity for connections isn't
   * exceeded.
   *
   * @return true if connection is accepted, false if the client is to be dismissed because of the
   *     lack of capacity.
   */
  private boolean acceptConnections(Socket newSocket) {
    try {
      if (connections.size() == 0) {
        connections.add(newSocket);
        outToFirst = new ObjectOutputStream(newSocket.getOutputStream());
        inFromFirst = new ObjectInputStream(newSocket.getInputStream());
        return true;
      } else if (connections.size() == 1) {
        connections.add(newSocket);
        outToSecond = new ObjectOutputStream(newSocket.getOutputStream());
        inFromSecond = new ObjectInputStream(newSocket.getInputStream());
        notifyListeners();
        return true;
      } else {
        ObjectOutputStream outToDismissable = new ObjectOutputStream(newSocket.getOutputStream());
        outToDismissable.writeObject(SERVER_FULL);
        outToDismissable.close();
      }
    } catch (IOException e2) {
      System.err.println(
          "Some IO Exception while server tried to accept connections from clients.");
      e2.printStackTrace();
    }
    return false;
  }

  /**
   * Checks whether the port is already in use.
   *
   * @param port the port that is inspected.
   * @return true if the port is available, false otherwise.
   */
  public static boolean isPortAvailable(int port) {
    boolean portAvailable;
    try (var ignored = new ServerSocket(port)) {
      portAvailable = true;
    } catch (IOException e) {
      portAvailable = false;
    }
    return portAvailable;
  }

  /**
   * Receives information from the clients and starts a new thread for sending this information to
   * all connected clients.
   *
   * @param conn the socket that sent the information to the server
   */
  private void receiveStates(Socket conn) {
    while (networkConnection) {
      try {
        GameState receivedState;
        if (conn == connections.get(0)) {
          Object received = inFromFirst.readObject();
          if (received.toString().equals("Quit")) {
            closeServer(WINDOW_CLOSE);
          } else {
            receivedState = (GameState) received;
            sendToAllClients(receivedState);
          }
        } else if (conn == connections.get(1)) {
          Object received = inFromSecond.readObject();
          if (received.toString().equals("Quit")) {
            closeServer(WINDOW_CLOSE);
          } else {
            receivedState = (GameState) received;
            sendToAllClients(receivedState);
          }
        }
      } catch (SocketException e) {
        if (e.getMessage().equals("Connection reset")) {
          closeServer(WINDOW_CLOSE);
        }
      } catch (SocketTimeoutException timeout) {
        System.err.println("Timeout occurred!");
        closeServer(CONNECTION_LOSS);
      } catch (EOFException eofE) {
        closeServer(CONNECTION_LOSS);
      } catch (IOException e1) {
        System.err.println("Some IO Exception while server tried to receive states from clients.");
        e1.printStackTrace();
      } catch (ClassNotFoundException e2) {
        System.err.println("Ooops. Class not found?");
        e2.printStackTrace();
      }
    }
  }

  /**
   * Close the server connection.
   *
   * @param trigger the trigger that causes the shut down of the server.
   */
  private void closeServer(String trigger) {
    try {
      sendToAllClients(trigger);
      serverSocket.close();
      networkConnection = false;
    } catch (IOException ioE) {
      System.err.println("Couldn't close the connection.");
    }
  }

  /**
   * Takes the GameState that was received from a client and sends it to all clients that are
   * connected to the server.
   *
   * @param obj the object to be send
   */
  private void sendToAllClients(Object obj) {
    Thread sendingThread =
        new Thread(
            () -> {
              try {
                if (connections.size() >= 1) {
                  outToFirst.writeObject(obj);
                  outToFirst.flush();
                }
                if (connections.size() == 2) {
                  outToSecond.writeObject(obj);
                  outToSecond.writeObject(obj);
                }
              } catch (SocketException sockE) {
                if (sockE
                    .getMessage()
                    .equals("Connection reset " + "by peer: socket write error")) {
                  closeServer(WINDOW_CLOSE);
                }
              } catch (IOException e1) {
                System.err.println(
                    "Some IO exception occurred while trying to send a state from server to a "
                        + "client.");
                e1.printStackTrace();
              }
            });
    sendingThread.setDaemon(true);
    sendingThread.start();
  }

  /**
   * Checks whether two connections have already been established or not.
   *
   * @return true if two connections are present, false otherwise.
   */
  public boolean areTwoPlayersConnected() {
    return connections.size() == 2;
  }

  /**
   * Invokes the firing of an event, such that any attached observer (i.e., {@link
   * PropertyChangeListener}) is notified that a change happened to this server.
   */
  private void notifyListeners() {
    support.firePropertyChange(STATE_CHANGED, null, this);
  }

  /**
   * Adds new listeners to this observable.
   *
   * @param pcl the listener that is about to be registered as listener of the observable
   */
  public void addPropertyChangeListener(PropertyChangeListener pcl) {
    requireNonNull(pcl);
    support.addPropertyChangeListener(pcl);
  }
}
