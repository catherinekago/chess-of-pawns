package chess.network;

import static java.util.Objects.requireNonNull;

import chess.model.GameState;
import chess.model.Model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * The Client class models the endpoint of the network that is established over the server. It is
 * used to send the state of the model to the server and to receive new states that were sent by the
 * server.
 */
public class Client implements PropertyChangeListener {

  private final PropertyChangeSupport support = new PropertyChangeSupport(this);

  private Model model;
  private static final int PORT = 43200;
  private static final String CONNECTION_LOSS = "Socket closed due to connection loss";
  private static final String SERVER_FULL = "Server full";
  private static final String WINDOW_CLOSE = "Socket closed due to window closing event";
  private Socket clientSocket;
  private int lastSend;
  private ObjectOutputStream objectsToServer;
  private ObjectInputStream objectsFromServer;

  /**
   * This constructor initiates an object of the Client class and starts the method that enables
   * receiving data.
   *
   * @param model the model that possesses the business logic this client is referring to.
   * @throws IOException that can occur when setting up a new client and handling input and output
   *     streams.
   */
  public Client(Model model, InetAddress server) throws IOException {
    this.model = model;
    model.addPropertyChangeListener(this);
    lastSend = this.model.getState().getAge();
    this.clientSocket = new Socket(server, PORT);
    receiveStates();
  }

  /**
   * Sends a message to the server that the client wants to quit the connection and triggeres the
   * server to initiate further actions.
   */
  public void quitConnection() {
    try {
      objectsToServer.writeObject("Quit");
    } catch (IOException ioE) {
      ioE.printStackTrace();
    }
  }

  /**
   * This method handles incoming data from the ObjectInputStream and decides whether to received
   * state should be forwarded to the model as is represents an actual update of the state, or not.
   */
  private void receiveStates() {

    Thread receiveFromServer =
        new Thread(
            () -> {
              try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                  ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {
                objectsFromServer = in;
                objectsToServer = out;
                while (true) {
                  Object received = objectsFromServer.readObject();
                  if (received.equals(SERVER_FULL)) {
                    notifyListeners(SERVER_FULL);
                  } else if (received.equals(CONNECTION_LOSS)) {
                    closeConnection(CONNECTION_LOSS);
                  } else if (received.equals(WINDOW_CLOSE)) {
                    closeConnection(WINDOW_CLOSE);
                  } else {
                    GameState newState = (GameState) received;
                    if (newState.getAge() > lastSend) {
                      model.setGameStateToReceivedState(newState);
                    }
                  }
                }
              } catch (ClassNotFoundException e) {
                System.err.println("Class not found!");
              } catch (IOException ioE) {
                System.err.println(
                    "Some error with input or output stream occured during setup "
                        + "or reading from it.");
              }
            });
    receiveFromServer.setDaemon(true);
    receiveFromServer.start();
  }

  /**
   * Closes the input and output streams and the socket that is connecting the client to the server.
   *
   * @param event the event that initializes the closing of the connection
   */
  private void closeConnection(String event) {
    try {
      objectsFromServer.close();
      objectsToServer.close();
      notifyListeners(event);
    } catch (IOException ioE) {
      System.err.println("Couldn't close streams and socket.");
      ioE.printStackTrace();
    }
  }

  /**
   * The observable (= model) has just published that it has changed its state. The Client needs to
   * send the state of the model to the server if the change that was made was a new move. But if
   * the change was just the notification about the server update, nothing happens.
   *
   * @param event The event that has been fired by the model.
   */
  private void handleChangeEvent(PropertyChangeEvent event) {
    if (event.getPropertyName().equals(Model.NEW_MOVE)) {
      GameState toSend = this.model.getState();
      if (toSend.getAge() > lastSend) {
        try {
          objectsToServer.writeObject(toSend);
          objectsToServer.flush();
          objectsToServer.reset();
          lastSend = toSend.getAge();
        } catch (IOException e) {
          System.err.println(
              "IO Exception within Client while trying to write an object to the server occurred.");
        }
      }
    }
  }

  /**
   * Invokes the firing of an event, such that any attached observer (i.e., {@link
   * PropertyChangeListener}) is notified that a change happened to this server.
   *
   * @param event a String that represents the event that led to the socket closing.
   */
  private void notifyListeners(String event) {
    support.firePropertyChange(event, null, null);
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

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    handleChangeEvent(evt);
  }
}
