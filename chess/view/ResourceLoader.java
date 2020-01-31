package chess.view;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Optional;
import javax.swing.ImageIcon;

/** Class used to load user-specific icons or images. */
class ResourceLoader {

  private static final String RESOURCE_DIR = "Chess" + File.separator + "resource" + File.separator;

  /**
   * Optional containing either an image of a black pawn, or empty if loading the specific
   * source-file failed.
   */
  static final Optional<Image> BLACK_PAWN =
      ResourceLoader.loadImage("images" + File.separator + "pawn_black" + ".png");

  /**
   * Optional containing either an image of a sad reset emoji, or empty if loading the specific
   * source-file failed.
   */
  static final Optional<Image> RESET_EMOJI =
      ResourceLoader.loadImage("images" + File.separator + "reset_emoji" + ".png");
  /**
   * Optional containing either an image of a white pawn, or empty if loading the specific
   * source-file failed.
   */
  static final Optional<Image> WHITE_PAWN =
      ResourceLoader.loadImage("images" + File.separator + "pawn_white" + ".png");

  /**
   * Optional containing either an image of a winning szenario, or empty if loading the specific
   * source-file failed.
   */
  static final Optional<Image> WINNER =
      ResourceLoader.loadImage("images" + File.separator + "winner" + ".png");

  private ResourceLoader() {
    // private constructor that prevents the instantiation from outside of this class.
  }

  /**
   * Returns an optional containing an image with its default size. The optional is empty in case of
   * loading the image failed.
   *
   * @param path The path to the file.
   * @return An optional containing the image in case of success.
   */
  private static Optional<Image> loadImage(String path) {
    Optional<ImageIcon> imgIconOpt = loadImageIcon(path);
    Image image = imgIconOpt.isPresent() ? imgIconOpt.get().getImage() : null;
    return Optional.ofNullable(image);
  }

  /**
   * Returns an optional containing an image with the given size. The optional is empty in case of
   * loading the image failed.
   *
   * @param path The path to the file.
   * @param width The width of the image.
   * @param height The height of the image.
   * @return An optional containing the image in case of success.
   */
  static Optional<ImageIcon> loadScaledImageIcon(String path, int width, int height) {
    Optional<ImageIcon> imgIconOpt = loadImageIcon(path);
    if (imgIconOpt.isEmpty()) {
      return imgIconOpt;
    }

    BufferedImage resizedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = resizedImg.createGraphics();
    g2.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2.drawImage(imgIconOpt.get().getImage(), 0, 0, width, height, null);
    g2.dispose();

    return Optional.of(new ImageIcon(resizedImg));
  }

  /**
   * Create an {@link ImageIcon} for the given path.
   *
   * @param path The path to the file.
   * @return An optional containing an ImageIcon in case of success.
   */
  private static Optional<ImageIcon> loadImageIcon(String path) {
    File file = new File(RESOURCE_DIR + path);
    if (!file.exists() || !file.isFile()) {
      return Optional.empty();
    }

    try {
      ImageIcon imageIcon = new ImageIcon(file.toURI().toURL());
      return Optional.of(imageIcon);
    } catch (MalformedURLException e) {
      // This exception should not occur at this point.
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
