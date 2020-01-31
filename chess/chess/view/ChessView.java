package chess.view;

import static java.util.Objects.requireNonNull;

import chess.model.GameMode;
import chess.model.Model;
import chess.model.Phase;
import chess.model.Player;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

/**
 * The ChessView class corresponds to the chess.view component of the MVC design pattern and is linked to
 * the chess.model and the controller class of the chess game. It provides the GUI of the chess game
 * including a display of the current player, an option to restart the game and the game board
 * itself. Also, it informs the player about the winner of a finished game. The user input, which
 * comes in form of different actions with the mouse (click or drag), is handled by forwarding it to
 * the controller class.
 */
public class ChessView extends JFrame implements View, PropertyChangeListener {

  private static final long serialVersionUID = 1L;

  private DrawBoard drawBoard;

  private Controller controller;
  private Model model;

  private static final Color BACKGROUND = new Color(246, 255, 244);
  private static final Color BUTTON_FILL = new Color(62, 122, 126);
  private static final Color BUTTON_FONT = new Color(255, 255, 255);
  private static final Color TOP_BOTTOM_FILL = new Color(97, 189, 193);
  private static final Color TOP_FONT = new Color(255, 255, 255);

  private JFrame gameWindow;
  private Dimension gameWindowDim = new Dimension(600, 600);
  private JLabel topText;
  private JPanel topBar;

  private static final int TOP_BOTTOM_HEIGHT = 30;

  /**
   * The constructor of the ChessView class creates a ChessView object by calling the super
   * constructor of the JFrame class, initializing the variables chess.model, controller and drawBoard and
   * setting up the GUI that holds the game board. Also, the listeners for the GUI frame are set up.
   *
   * @param model the chess.model that provides the business logic of the game
   * @param controller the class that is responsible for the data manipulation: it updates the
   *     ChessView class with the data that comes from the chess.model
   */
  public ChessView(Model model, Controller controller) {
    super("Pawn Game");
    this.model = requireNonNull(model);
    this.controller = requireNonNull(controller);
    drawBoard =
        new DrawBoard(gameWindowDim.width, gameWindowDim.height, this.model, this.controller);
    initializeGui();
    addWindowResizeListener();
    addWindowMouseListener();
    chooseGameModeDialog();
  }

  /** Initializes the GUI by setting up the layout of the game window. */
  private void initializeGui() {
    setUpGameWindow();
    topBar = createTopBar();
    gameWindow.getContentPane().add(topBar, BorderLayout.NORTH);
    JPanel bottomBar = createBottomBar();
    gameWindow.getContentPane().add(bottomBar, BorderLayout.SOUTH);
    JButton bottomButton = createBottomButton();
    bottomBar.add(bottomButton, BorderLayout.CENTER);
    gameWindow.getContentPane().add(drawBoard, BorderLayout.CENTER);

    bottomButton.addActionListener(
        e -> {
          Toolkit.getDefaultToolkit().beep();
          resetConfirmation();
        });
  }

  /** Set up the game window including layout and color scheme. */
  private void setUpGameWindow() {
    customizeDialogWindows();
    gameWindow = new JFrame();
    gameWindow.setLayout(new BorderLayout());
    gameWindow.setPreferredSize(gameWindowDim);
    gameWindow.getContentPane().setBackground(BACKGROUND);
    gameWindow.pack();
    gameWindow.setLocationRelativeTo(null);
    gameWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    gameWindow.addWindowListener(
        new WindowAdapter() {
          public void windowClosing(WindowEvent evt) {
            if (model.getGameMode() == GameMode.NETWORK && model.getMyPlayer() == Player.WHITE) {
              controller.quitClientConnection();
            }
          }
        });
  }

  @Override
  public void updateGuiDependingOnNetworkPlayer(String role) {
    Border topPanelBorder = BorderFactory.createLineBorder(TOP_BOTTOM_FILL, 2);
    topBar.setBorder(topPanelBorder);
    topText.setForeground(TOP_BOTTOM_FILL);
    if (role.equals("ServerClient")) {
      topBar.setBackground(Color.WHITE);
    } else if (role.equals("Client")) {
      topBar.setBackground(Color.BLACK);
    }
  }

  /**
   * Create the top panel of the game window where the current player is displayed.
   *
   * @return the created top panel
   */
  private JPanel createTopBar() {
    JPanel topFrame = new JPanel();
    topText = new JLabel("Current Player: " + model.getState().getCurrentPlayer(), JLabel.CENTER);
    topFrame.setBackground(TOP_BOTTOM_FILL);
    topText.setForeground(TOP_FONT);
    topFrame.setSize(getWidth(), TOP_BOTTOM_HEIGHT);
    topText.setVisible(true);
    topFrame.add(topText);
    topFrame.setVisible(true);
    return topFrame;
  }

  /**
   * Create the bottom panel of the game field.
   *
   * @return the created bottom panel
   */
  private JPanel createBottomBar() {
    JPanel bottomFrame = new JPanel();
    bottomFrame.setBackground(TOP_BOTTOM_FILL);
    bottomFrame.setSize(getWidth(), TOP_BOTTOM_HEIGHT);
    return bottomFrame;
  }

  /**
   * Create the reset button on the bottom panel.
   *
   * @return the reset button
   */
  private JButton createBottomButton() {
    JButton bottomButton = new JButton("      Reset      ");
    bottomButton.setBorder(new LineBorder(BUTTON_FILL));
    bottomButton.setVisible(true);
    return bottomButton;
  }

  /** Re-color the elements of the dialog window. */
  private void customizeDialogWindows() {
    UIManager ui = new UIManager();
    ui.put("Button.background", BUTTON_FILL);
    ui.put("Button.foreground", BUTTON_FONT);
    ui.put("OptionPane.background", TOP_BOTTOM_FILL);
    ui.put("OptionPane.messageForeground", BUTTON_FONT);
    ui.put("Panel.background", TOP_BOTTOM_FILL);
  }

  /** Opens the dialog that lets the player choose between the modes hotseat, single and chess.network. */
  private void chooseGameModeDialog() {
    JOptionPane gameModeDialog = new JOptionPane();
    Object[] modes = {"Hotseat", "Single", "Network"};

    int userChoice =
        gameModeDialog.showOptionDialog(
            gameWindow,
            "What type of game do you want to play?",
            "Choose a Game Mode",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            modes,
            modes[0]);
    if (userChoice == 0) {
      this.model.setGameMode(GameMode.HOTSEAT);
    } else if (userChoice == 1) {
      this.model.setGameMode(GameMode.SINGLE);
    } else if (userChoice == 2) {
      this.model.setGameMode(GameMode.NETWORK);
      this.model.getState().setCurrentPhase(Phase.WAITING);
      networkOptionsDialog();
    } else {
      System.exit(0);
    }
  }

  /** Creates a dialog window with the options for the chess.network game. */
  private void networkOptionsDialog() {
    JPanel radioButtonPanel = new JPanel();
    radioButtonPanel.setLayout(new FlowLayout());
    JRadioButton serverButton = new JRadioButton("Server");
    serverButton.setBackground(TOP_BOTTOM_FILL);
    serverButton.setForeground(TOP_FONT);
    JRadioButton clientButton = new JRadioButton("Client");
    clientButton.setBackground(TOP_BOTTOM_FILL);
    clientButton.setForeground(TOP_FONT);
    ButtonGroup groupRadio = new ButtonGroup();
    groupRadio.add(serverButton);
    groupRadio.add(clientButton);
    radioButtonPanel.add(clientButton);
    radioButtonPanel.add(serverButton);

    JPanel serverAddressPanel = new JPanel();
    JLabel label = new JLabel("Server Adress : ");
    JTextField serverAddress = new JTextField("127.0.0.1", 15);
    JLabel clueChooseRadioButton = new JLabel("Please choose an option to continue.");
    clueChooseRadioButton.setForeground(Color.RED);
    serverAddressPanel.add(label);
    serverAddressPanel.add(serverAddress);
    serverAddressPanel.add(clueChooseRadioButton);

    JDialog networkOptions = new JDialog();

    JButton confirmButton = new JButton("Confirm");
    confirmButton.addActionListener(
        e -> {
          if (!clientButton.isSelected() && !serverButton.isSelected()) {
            clueChooseRadioButton.setVisible(true);
          } else {
            networkOptions.setVisible(false);
            networkOptions.dispose();
            new Thread(
                () -> {
                  try {
                    if (serverButton.isSelected()) {
                      controller.startServerConnection();
                    } else {
                      try {
                        InetAddress serverAd = InetAddress.getByName(serverAddress.getText());
                        controller.startClientConnection("Client", serverAd);
                      } catch (UnknownHostException e12) {
                        handleUnknownHost();
                      }
                    }
                  } catch (ConnectException e1) {
                    gameWindow.setVisible(false);
                    gameWindow.dispose();
                    JOptionPane.showMessageDialog(
                        null,
                        "The selected server has not yet "
                            + "been initialized. Please set up a chess.network game as 'Server' "
                            + "in order to do so.");
                    controller.resetGame();
                  } catch (IOException e2) {
                    e2.printStackTrace();
                  }
                })
               .start();
          }
        });

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(
        e -> {
          networkOptions.setVisible(false);
          networkOptions.dispose();
          gameWindow.setVisible(false);
          gameWindow.dispose();
          controller.resetGame();
        });

    JPanel buttons = new JPanel();
    buttons.setLayout(new FlowLayout());
    buttons.add(confirmButton);
    buttons.add(cancelButton);

    networkOptions.setLayout(new BorderLayout());
    networkOptions.add(radioButtonPanel, BorderLayout.NORTH);
    networkOptions.add(serverAddressPanel, BorderLayout.CENTER);
    networkOptions.add(buttons, BorderLayout.SOUTH);

    networkOptions.setAlwaysOnTop(true);
    networkOptions.setSize(new Dimension(330, 170));
    networkOptions.setResizable(false);
    networkOptions.setLocationRelativeTo(gameWindow);
    networkOptions.setTitle("Please choose:");

    networkOptions.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    networkOptions.addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            networkOptions.setVisible(false);
            networkOptions.dispose();
            gameWindow.setVisible(false);
            gameWindow.dispose();
            controller.resetGame();
          }
        });

    serverButton.addActionListener(
        e -> {
          serverAddress.setVisible(false);
          label.setVisible(false);
        });

    clientButton.addActionListener(
        e -> {
          serverAddress.setVisible(true);
          label.setVisible(true);
        });

    networkOptions.setVisible(true);
    serverAddress.setVisible(false);
    label.setVisible(false);

    clueChooseRadioButton.setVisible(false);
  }

  @Override
  public void handleServerAlreadyInUse() {
    gameWindow.setVisible(false);
    gameWindow.dispose();
    JOptionPane.showMessageDialog(
        null,
        "This server adress is already in"
            + " use. Please start a chess.network game as 'Client' in order to "
            + "connect to the server.");
    controller.resetGame();
  }

  @Override
  public void handleConnectionRefusal() {
    gameWindow.setVisible(false);
    gameWindow.dispose();
    JOptionPane.showMessageDialog(
        null,
        "The client couldn't connect to the specified server because the server was shut down "
            + "previously.");
    controller.resetGame();
  }

  @Override
  public void handleTooManyConnections() {
    gameWindow.setVisible(false);
    gameWindow.dispose();
    JOptionPane.showMessageDialog(
        null,
        "The client couldn't connect to the specified server because there are already two "
            + "players connected to the running game. Please choose another game mode.");
    controller.resetGame();
  }

  @Override
  public void handleOtherPlayerLeftGame() {
    gameWindow.setVisible(false);
    gameWindow.dispose();
    JOptionPane.showMessageDialog(
        null,
        "I am sorry. The other player just left the running game. The game is over. ... Which "
            + "practically means that you won - since you are the last player left at the field! "
            + "CONGRATS!");
    controller.resetGame();
  }

  @Override
  public void handleUnknownHost() {
    gameWindow.setVisible(false);
    gameWindow.dispose();
    JOptionPane.showMessageDialog(
        null,
        "The client couldn't connect to the specified server because the given host is unknown. "
            + "Please make sure to choose the right IP adress for the host.");
    controller.resetGame();
  }

  @Override
  public void handleConnectionLoss() {
    gameWindow.setVisible(false);
    gameWindow.dispose();
    JOptionPane.showMessageDialog(
        null,
        "I am very sorry. The connection of this game was interrupted. Due to this, the game is "
            + "declared to be over.");
    controller.resetGame();
  }

  /** Creates the reset confirmation dialog and handles the user's selection. */
  private void resetConfirmation() {
    ImageIcon icon = new ImageIcon(ResourceLoader.RESET_EMOJI.get());
    int userChoice =
        JOptionPane.showConfirmDialog(
            gameWindow,
            "Do you really want to reset the running game?",
            "Are you absolutely sure?",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            icon);
    if (userChoice == 0) {
      gameWindow.setVisible(false);
      gameWindow.dispose();
      controller.resetGame();
    }
  }

  /** Creates the winning scenario dialog and handles the user's selection. */
  private void winnerDialog() {
    ImageIcon icon = new ImageIcon(ResourceLoader.WINNER.get());
    String msg;
    if (model.getState().getWinner().isPresent()) {
      msg = "There's a winner: \r\n Player " + model.getState().getWinner().get();
    } else {
      msg = "Game over! It's a draw!";
    }
    JOptionPane.showMessageDialog(gameWindow, msg, "Finished! ", JOptionPane.OK_OPTION, icon);
  }

  /** Set up the listener that reacts to changes to the window size of the game window. */
  private void addWindowResizeListener() {
    gameWindow
        .getContentPane()
        .addComponentListener(
            new ComponentAdapter() {
              @Override
              public void componentResized(ComponentEvent e) {
                Component c = (Component) e.getSource();
                gameWindowDim = new Dimension(c.getWidth(), c.getHeight());
                drawBoard.updateDimensions(gameWindowDim);
                gameWindow.validate();
                gameWindow.repaint();
              }
            });
  }

  /** Set up the listener that reacts to mouse clicks within the game window. */
  private void addWindowMouseListener() {
    gameWindow
        .getContentPane()
        .addMouseListener(
            new MouseListener() {
              @Override
              public void mousePressed(MouseEvent me) {
                if (model.getState().getCurrentPhase() == Phase.RUNNING) {
                  controller.handleMouseClick(me);
                  gameWindow.validate();
                  gameWindow.repaint();
                }
              }

              public void mouseReleased(MouseEvent me) {}

              public void mouseEntered(MouseEvent me) {}

              public void mouseExited(MouseEvent me) {}

              public void mouseClicked(MouseEvent me) {}
            });
  }

  @Override
  public DrawBoard getDrawBoard() {
    return drawBoard;
  }

  @Override
  public void showGame() {
    gameWindow.setVisible(true);
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    if (event.getPropertyName().equals(model.NETWORK_UPDATE)) {
      this.validate();
      this.repaint();
    }
    SwingUtilities.invokeLater(() -> handleChangeEvent(event));
  }

  /**
   * The observable (= chess.model) has just published that it has changed its state. The GUI needs to be
   * updated accordingly here.
   *
   * @param event The event that has been fired by the chess.model.
   */
  private void handleChangeEvent(PropertyChangeEvent event) {
    if (event.getPropertyName().equals(model.NETWORK_UPDATE)) {
      gameWindow.validate();
      gameWindow.repaint();
    }
    if (model.getState().getCurrentPhase() == Phase.FINISHED) {
      dispose();
      removePropertyChangeListener(this);
      winnerDialog();
    } else if (model.getGameMode() == GameMode.SINGLE
        && model.getState().getCurrentPlayer() == Player.WHITE) {
      gameWindow.validate();
      gameWindow.repaint();
      topText.setText("Current Player: " + model.getState().getCurrentPlayer());
    } else {
      topText.setText("Current Player: " + model.getState().getCurrentPlayer());
    }
  }
}
