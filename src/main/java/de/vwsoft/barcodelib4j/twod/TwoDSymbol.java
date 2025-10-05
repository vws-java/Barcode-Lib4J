/*
 * Copyright (c) 2025 Viktor Wedel
 *
 * Website EN: https://www.vw-software.com/java-barcode-library/
 * Website DE: https://www.vwsoft.de/barcode-library-for-java/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.vwsoft.barcodelib4j.twod;

import de.vwsoft.barcodelib4j.twod.zxing.BitMatrix;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Objects;


/**
 * Drawable 2D code symbol that uses Graphics2D for rendering.
 * <p>
 * Instances can be obtained by calling the {@link TwoDCode#buildSymbol buildSymbol} method of the
 * {@code TwoDCode} class.
 */
public class TwoDSymbol {

  // Former bits of the BitMatrix, grouped into contiguous rectangles.
  private final Rectangle[] myChunks;

  // Width and height of the 2D code symbol (in bits), including its quiet zones.
  private final Dimension mySize;



  /**
   * Constructs an instance using the binary representation (bit matrix) of a 2D code.
   * <p>
   * For an explanation of what quiet zones are and the default quiet zone sizes for each 2D code
   * type, see {@link TwoDType#getDefaultQuietZone()}.
   *
   * @param bitMatrix      the binary representation of the 2D code
   * @param quietZoneSize  the size of the quiet zone to be appended around the 2D code
   * @throws NullPointerException      if the provided bit matrix is {@code null}
   * @throws IllegalArgumentException  if the specified quiet zone size is negative
   */
  public TwoDSymbol(BitMatrix bitMatrix, int quietZoneSize) {
    Objects.requireNonNull(bitMatrix, "BitMatrix cannot be null");
    if (quietZoneSize < 0)
      throw new IllegalArgumentException("Quiet zone size cannot be negative: " + quietZoneSize);

    final int matrixWidth  = bitMatrix.getWidth();
    final int matrixHeight = bitMatrix.getHeight();
    final ArrayList<Rectangle> chunks = new ArrayList<>(matrixWidth * matrixHeight / 3);

    // Horizontal chunks
    for (int j=0; j<matrixHeight; j++) {
      int positionCounter = quietZoneSize, widthCounter = 0;
      boolean barOrSpace = bitMatrix.get(0, j);
      for (int i=0; i<matrixWidth; i++) {
        boolean bit = bitMatrix.get(i, j);
        if (bit != barOrSpace) {
          if (!bit)
            chunks.add(new Rectangle(positionCounter, j + quietZoneSize, widthCounter, 1));
          barOrSpace = bit;
          positionCounter += widthCounter;
          widthCounter = 1;
        } else {
          widthCounter++;
        }
      }
      // ... and the last one ...
      if (bitMatrix.get(matrixWidth - 1, j))
        chunks.add(new Rectangle(positionCounter, j + quietZoneSize, widthCounter, 1));
    }

    // Vertical chunks, this time without detection of 1x1-sized chunks (see "heightCounter > 1")
    for (int j=0; j<matrixWidth; j++) {
      int positionCounter = quietZoneSize, heightCounter = 0;
      boolean barOrSpace = bitMatrix.get(j, 0);
      for (int i=0; i<matrixHeight; i++) {
        boolean bit = bitMatrix.get(j, i);
        if (bit != barOrSpace) {
          if (!bit && heightCounter > 1)
            chunks.add(new Rectangle(j + quietZoneSize, positionCounter, 1, heightCounter));
          barOrSpace = bit;
          positionCounter += heightCounter;
          heightCounter = 1;
        } else {
          heightCounter++;
        }
      }
      // ... and the last one ...
      if (bitMatrix.get(j, matrixHeight - 1))
        chunks.add(new Rectangle(j + quietZoneSize, positionCounter, 1, heightCounter));
    }

    myChunks = chunks.toArray(Rectangle[]::new);
    quietZoneSize <<= 1;
    mySize = new Dimension(matrixWidth + quietZoneSize, matrixHeight + quietZoneSize);
  }



  /**
   * Draws the 2D code symbol.
   * <p>
   * Special attention is paid to the quality and, consequently, the later readability of the
   * resulting 2D code. This is mainly ensured by the following four parameters:
   * <p>
   * <b>dotSize</b> - Specifies the size of a single point on the output medium, calculated from its
   * resolution. For a printer, this should be the size of a dot, determined by the printer's
   * resolution (DPI). For a bitmap image, it should be the "physical" size of a pixel, calculated
   * from the pixel density (PPI). The value should be specified in the same unit as the other
   * parameters. When using millimeters, for a resolution of 300 DPI/PPI, the formula would be:
   * 25.4 / 300. When using inches: 1 / 300. This adjustment may be negligible in high-resolution
   * output scenarios, where the value can be set to 0.0.
   * <p>
   * Note: If the output medium has differing horizontal and vertical resolutions, use the smaller
   * resolution for {@code dotSize} calculation. Example: For 300x600 DPI, use 300 DPI.
   * <p>
   * <b>moduleSize</b> - Allows it to specify a fixed size of the modules (bits), that will affect
   * the overall size of the 2D code symbol. If set to 0.0, the method will automatically calculate
   * an appropriate module size based on the dimensions of the bounding box and the value of
   * {@code dotSize}. In any case, if {@code dotSize} is greater than 0.0, the module size is
   * adjusted to ensure that each module has a size that is a multiple of {@code dotSize}.
   * <p>
   * <b>hor- &amp; verBarWidthCorrection</b> - Adjusts the size of the modules (bits) of the 2D code
   * symbol. A positive value increases the size of the modules, while a negative value reduces
   * them. For example, in the case of a printer, where ink bleeding may occur, a negative value
   * may be necessary to compensate for the ink bleeding and ensure accurate module size. Similarly,
   * for output scenarios where undesirable effects don't occur, the value can be set to 0.0.
   *
   * @param g2d the graphics context to draw on
   * @param x the x-coordinate of the top-left corner of the bounding box
   * @param y the y-coordinate of the top-left corner of the bounding box
   * @param w the width of the bounding box
   * @param h the height of the bounding box
   * @param dotSize the size of a single point on the output medium or 0.0
   * @param moduleSize the size of each module (bit) of the 2D code symbol or 0.0
   * @param horBarWidthCorrection horizontal bar width correction factor or 0.0
   * @param verBarWidthCorrection vertical bar width correction factor or 0.0
   */
  public void draw(Graphics2D g2d, double x, double y, double w, double h, double dotSize,
      double moduleSize, double horBarWidthCorrection, double verBarWidthCorrection) {

    if (moduleSize == 0.0)
      moduleSize = Math.min(w / mySize.width, h / mySize.height);
    if (dotSize > 0.0)
      moduleSize = (int)(moduleSize / dotSize) * dotSize;

    final double shiftedX = x + (w - moduleSize * mySize.width) / 2.0 - horBarWidthCorrection;
    final double shiftedY = y + (h - moduleSize * mySize.height) / 2.0 - verBarWidthCorrection;
    final double horBWC2 = horBarWidthCorrection * 2.0;
    final double verBWC2 = verBarWidthCorrection * 2.0;
    final Rectangle2D.Double chunk = new Rectangle2D.Double();
    for (Rectangle r : myChunks) {
      chunk.x = shiftedX + r.x * moduleSize;
      chunk.y = shiftedY + r.y * moduleSize;
      chunk.width = r.width * moduleSize + horBWC2;
      chunk.height = r.height * moduleSize + verBWC2;
      g2d.fill(chunk);
    }
  }



  /**
   * Draws the 2D code symbol.
   * <p>
   * This method variant with a shortened parameter list only considers {@code dotSize} from the
   * quality settings and uses 0.0 for the rest.
   * <p>
   * These settings are typically suitable for printing on a thermal transfer printer, where the low
   * resolution of the printer must be taken into account, and where no bar width correction is
   * required for accurate printing.
   * <p>
   * The settings are also well suited to exporting the 2D code as a bitmap image.
   * <p>
   * Please refer to the main
   * {@link #draw(Graphics2D, double, double, double, double, double, double, double, double) draw}
   * method for a detailed parameter description.
   *
   * @param g2d the graphics context to draw on
   * @param x the x-coordinate of the top-left corner of the bounding box
   * @param y the y-coordinate of the top-left corner of the bounding box
   * @param w the width of the bounding box
   * @param h the height of the bounding box
   * @param dotSize the size of a single point on the output medium or 0.0
   */
  public void draw(Graphics2D g2d, double x, double y, double w, double h, double dotSize) {
    draw(g2d, x, y, w, h, dotSize, 0.0, 0.0, 0.0);
  }



  /**
   * Draws the 2D code symbol.
   * <p>
   * This method variant with a shortened parameter list does not consider any quality settings.
   * It is typically suitable for printing on laser printers or exporting as vector graphics.
   * <p>
   * Please refer to the main
   * {@link #draw(Graphics2D, double, double, double, double, double, double, double, double) draw}
   * method for a detailed parameter description.
   *
   * @param g2d the graphics context to draw on
   * @param x the x-coordinate of the top-left corner of the bounding box
   * @param y the y-coordinate of the top-left corner of the bounding box
   * @param w the width of the bounding box
   * @param h the height of the bounding box
   */
  public void draw(Graphics2D g2d, double x, double y, double w, double h) {
    draw(g2d, x, y, w, h, 0.0, 0.0, 0.0, 0.0);
  }

}
