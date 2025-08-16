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
package de.vwsoft.barcodelib4j.oned;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;


/**
 * Abstract representation of a 1D barcode used as a basis for specific barcode implementations.
 * <p>
 * <b>Important:</b> This class provides all the necessary methods to access and manipulate
 * properties of derived classes and corresponding barcode types. The advantage of this is that
 * instances can be created simply by using one of the available static
 * {@link #newInstance(BarcodeType) newInstance} methods, rather than having to deal with specific
 * classes. For example:
 *
 * <pre>    Barcode bc = Barcode.newInstance(BarcodeType.CODE128);</pre>
 *
 * <b>Note:</b> Boolean methods of the pattern {@code "supportsXYZ"} indicate whether the barcode
 * type (not just the individual instance) supports a particular property. However, assigning an
 * unsupported property has no effect and does not throw an exception.
 */
public abstract class Barcode implements Cloneable {

  // Growth step to gradually adjust the font size based on the barcode symbol, if requested
  static final float FONT_SIZE_INCREMENT = 0.1F;

  // The validated content, encoded in the barcode
  String myContent;

  // The human readable representation of the content as drawn
  String myText;

  // Specifies whether or not the human readable text is drawn
  boolean myIsTextVisible = true;

  // Specifies whether the human readable text is drawn above or below the barcode symbol
  boolean myIsTextOnTop;

  // (Vertical) space between the barcode symbol and the human readable text
  double myTextOffset;

  // Font used to draw the human readable text; If set to 'null', Graphics2D's font will be used
  Font myFont;

  // If set to 'true', the font size is automatically adjusted to the size of the barcode symbol
  boolean myIsFontSizeAdjusted;

  // Note: instance variables that exist only at runtime and store temporary data are marked as
  // 'transient' throughout the package; this may also be useful for a possible later serialization

  // Representation of the barcode symbol as a paired list of positions and widths,
  // taking into account quiet zones
  transient int[] myBars;

  // Corresponds to the number of modules for barcodes without a ratio,
  // otherwise something of a larger value
  transient int myBarsCount;

  // Factor for the conversion between module width and barcode width;
  // equals to 'myBarsCount' for barcodes without ratio
  transient double myModuleFactor;



  // Hide default constructor from JavaDoc by making it package-private
  Barcode() {}



  /**
   * Creates a new instance of an implementation of this class that matches the specified barcode
   * type.
   *
   * @param type the type of barcode to instantiate
   * @return a new instance of an implementation of the {@code Barcode} class
   */
  public static Barcode newInstance(BarcodeType type) {
    return type.newInstance();
  }



  /**
   * Creates a new instance of an implementation of this class that matches the specified barcode
   * type. Then sets its content using the {@link #setContent(String, boolean, boolean) setContent}
   * method, along with parameters that determine how the content is handled.
   *
   * @param type the type of barcode to instantiate
   * @param content the content to be encoded in the barcode
   * @param autoComplete If {@code true}, the content will be automatically completed according to
   *                     certain criteria (e.g., padding or formatting rules)
   * @param appendOptionalChecksum If {@code true}, an optional checksum will be calculated and
   *                               appended to the barcode if the barcode type supports it
   * @return a new instance of an implementation of the {@code Barcode} class
   * @throws BarcodeException if the provided content cannot be encoded by the given barcode type
   * @see #setContent(String content, boolean autoComplete, boolean appendOptionalChecksum)
   */
  public static Barcode newInstance(BarcodeType type, String content, boolean autoComplete,
      boolean appendOptionalChecksum)
      throws BarcodeException {
    Barcode bc = type.newInstance();
    bc.setContent(content, autoComplete, appendOptionalChecksum);
    return bc;
  }



  // encodes the content into a binary string
  abstract CharSequence encode();



  /**
   * Sets the content to be encoded in the barcode, along with parameters that determine how the
   * content is to be handled.
   * <p>
   * <b>This is an abstract method.</b> Please refer to the specific implementation of this method
   * in the respective barcode type class for detailed information:
   * <ul>
   *   <li>{@link ImplCodabar#setContent(String, boolean, boolean) Codabar}</li>
   *   <li>{@link ImplCode11#setContent(String, boolean, boolean) Code 11}</li>
   *   <li>{@link ImplCode128#setContent(String, boolean, boolean) Code 128}</li>
   *   <li>{@link ImplCode128A#setContent(String, boolean, boolean) Code 128 A}</li>
   *   <li>{@link ImplCode128B#setContent(String, boolean, boolean) Code 128 B}</li>
   *   <li>{@link ImplCode128C#setContent(String, boolean, boolean) Code 128 C}</li>
   *   <li>{@link ImplCode39#setContent(String, boolean, boolean) Code 39}</li>
   *   <li>{@link ImplCode39E#setContent(String, boolean, boolean) Code 39 Extended}</li>
   *   <li>{@link ImplCode93#setContent(String, boolean, boolean) Code 93}</li>
   *   <li>{@link ImplCode93E#setContent(String, boolean, boolean) Code 93 Extended}</li>
   *   <li>{@link ImplEAN13#setContent(String, boolean, boolean) EAN-13 (GTIN-13)}</li>
   *   <li>{@link ImplEAN14#setContent(String, boolean, boolean) EAN-14 (GTIN-14)}</li>
   *   <li>{@link ImplEAN8#setContent(String, boolean, boolean) EAN-8 (GTIN-8)}</li>
   *   <li>{@link ImplEAN128#setContent(String, boolean, boolean) GS1-128 (UCC/EAN-128)}</li>
   *   <li>{@link ImplITF#setContent(String, boolean, boolean) Interleaved 2 of 5}</li>
   *   <li>{@link ImplISBN13#setContent(String, boolean, boolean) ISBN-13}</li>
   *   <li>{@link ImplISMN#setContent(String, boolean, boolean) ISMN}</li>
   *   <li>{@link ImplPZN#setContent(String, boolean, boolean) PZN}</li>
   *   <li>{@link ImplPZN8#setContent(String, boolean, boolean) PZN8}</li>
   *   <li>{@link ImplSSCC18#setContent(String, boolean, boolean) SSCC-18 (NVE/EAN-18)}</li>
   *   <li>{@link ImplUPCA#setContent(String, boolean, boolean) UPC-A}</li>
   *   <li>{@link ImplUPCE#setContent(String, boolean, boolean) UPC-E}</li>
   * </ul>
   *
   * @param content the content to be encoded in the barcode
   *
   * @param autoComplete controls whether the content is automatically completed according to
   * certain criteria. For example, for certain barcode types, such as those that require padding or
   * formatting rules, enabling {@code autoComplete} will ensure that these requirements are met.
   * Some barcode types are forced to calculate a missing checksum. The effect of
   * {@code autoComplete} varies between barcode types.
   *
   * @param appendOptionalChecksum applies to barcode types that may or may not contain a checksum.
   * If enabled, the checksum will be calculated and appended to the barcode. However, this
   * parameter has no effect on barcode types for which a checksum is either mandatory or not
   * provided by their specification.
   *
   * @throws BarcodeException  if a barcode object is assigned an invalid content that cannot be
   *                           encoded by the given barcode type
   */
  public abstract void setContent(String content,
                                  boolean autoComplete,
                                  boolean appendOptionalChecksum)
      throws BarcodeException;



  /**
   * {@return the raw content as encoded in the barcode} Note that depending on the given barcode
   * type (e.g., {@link ImplCode128 Code 128} and {@link ImplEAN128 EAN-128}) the returned
   * string may contain unreadable characters, such as ASCII values from 0 to 31 and others. For
   * human readable text, consider using the {@link #getText()} method instead.
   */
  public final String getContent() {
    return myContent;
  }



  /**
   * {@return whether the given barcode type supports customization of the human readable text}
   *
   * @see #setCustomText(String text)
   */
  public boolean supportsCustomText() {
    return true;
  }



  /**
   * Sets a custom text for the barcode to replace the automatically generated human readable text.
   * This must be supported by the given barcode type.
   *
   * @param text the custom text to set for the barcode
   * @throws IllegalArgumentException if the custom text is {@code null} or empty
   * @see #supportsCustomText()
   */
  public void setCustomText(String text) {
    if (text == null)
      throw new IllegalArgumentException("Custom text cannot be null");
    if (text.isEmpty())
      throw new IllegalArgumentException("Custom text cannot be empty");
    myText = text;
  }



  /**
   * {@return the human readable representation of the content encoded in the barcode}
   */
  public final String getText() {
    return myText != null ? myText : myContent;
  }



  /**
   * {@return whether the given barcode type supports the addition of supplementary barcode symbols}
   * This feature is specific to barcode types in the UPC family, such as {@link ImplUPCA UPC-A},
   * {@link ImplUPCE UPC-E}, {@link ImplEAN13 EAN-13}, {@link ImplEAN8 EAN-8},
   * {@link ImplISBN13 ISBN-13} and {@link ImplISMN ISMN}.
   */
  public boolean supportsAddOn() {
    return false;
  }



  /**
   * Sets the Add-On number for this barcode object.
   *
   * @param addOnNumber a number consisting of either 2 or 5 digits or {@code null} (default)
   * @throws BarcodeException if the provided value does not match the expected format
   */
  public void setAddOn(String addOnNumber) throws BarcodeException {
  }



  /**
   * {@return the Add-On number assigned to this barcode object or {@code null} if no Add-On number
   * is assigned}
   */
  public String getAddOn() {
    return null;
  }



  /**
   * Sets whether the human readable representation of the content encoded in the barcode is visible
   * or whether only the barcode symbol is drawn.
   *
   * @param visible {@code true} if the human readable text should be visible,
   *                {@code false} otherwise
   */
  public final void setTextVisible(boolean visible) {
    myIsTextVisible = visible;
  }



  /**
   * {@return whether the human readable representation of the content encoded in the barcode is
   * visible or whether only the barcode symbol is drawn}
   */
  public final boolean isTextVisible() {
    return myIsTextVisible;
  }



  /**
   * {@return whether the given barcode type supports placing the human readable text above the
   * barcode symbol}
   */
  public boolean supportsTextOnTop() {
    return true;
  }



  /**
   * Sets whether the human readable text is to be placed above the barcode symbol. This option
   * must be supported by the given barcode type.
   *
   * @param onTop {@code true} to place the human readable text above the barcode symbol,
   *              {@code false} to place it below
   * @see #supportsTextOnTop()
   */
  public final void setTextOnTop(boolean onTop) {
    myIsTextOnTop = onTop;
  }



  /**
   * {@return whether the human readable text is placed above the barcode symbol}
   */
  public final boolean isTextOnTop() {
    return myIsTextOnTop;
  }



  /**
   * Sets the offset for the position of the human readable text relative to the barcode symbol.
   * Positive values increase the distance between the text and the symbol, while negative values
   * decrease it.
   *
   * @param offset the offset value for adjusting the position of the human readable text
   */
  public final void setTextOffset(double offset) {
    myTextOffset = offset;
  }



  /**
   * {@return the offset for the position of the human readable text relative to the barcode symbol}
   */
  public final double getTextOffset() {
    return myTextOffset;
  }



  /**
   * Sets the font to be used for drawing the human readable text in the barcode. If set to
   * {@code null} (default), the font assigned to the {@code Graphics2D} context will be used.
   * <p>
   * Note: If automatic font size adjustment is enabled (see
   * {@link #setFontSizeAdjusted(boolean) setFontSizeAdjusted}),
   * the size property of the specified font will be ignored.
   *
   * @param font the font for the human readable text, or {@code null} to use the font assigned to
   *             the {@code Graphics2D} context
   */
  public void setFont(Font font) {
    myFont = font;
  }



  /**
   * {@return the font to be used for drawing the human readable text in the barcode}
   */
  public Font getFont() {
    return myFont;
  }



  /**
   * Sets whether the font size is to be automatically adjusted based on the size of the barcode
   * symbol. The default is {@code false}.
   *
   * @param b {@code true} to automatically adjust the font size,
   *          {@code false} to keep the font size constant
   */
  public final void setFontSizeAdjusted(boolean b) {
    myIsFontSizeAdjusted = b;
  }



  /**
   * {@return whether the font size is automatically adjusted based on the size of the barcode
   * symbol}
   */
  public final boolean isFontSizeAdjusted() {
    return myIsFontSizeAdjusted;
  }



  /**
   * {@return whether the given barcode type supports auto-completion} The returned value indicates
   * whether passing {@code true} as the {@code autoComplete} parameter to the
   * {@link #setContent(String, boolean, boolean) setContent} method has any effect.
   *
   * @see #setContent(String content, boolean autoComplete, boolean appendOptionalChecksum)
   */
  public boolean supportsAutoCompletion() {
    return true;
  }



  /**
   * {@return whether the given barcode type supports an optional checksum} The returned value
   * indicates whether passing {@code true} as the {@code appendOptionalChecksum} parameter to the
   * {@link #setContent(String, boolean, boolean) setContent} method has any effect.
   *
   * @see #setContent(String content, boolean autoComplete, boolean appendOptionalChecksum)
   */
  public boolean supportsOptionalChecksum() {
    return false;
  }



  /**
   * Sets whether the optional checksum, if encoded in the barcode, is to be visible within
   * the human readable text.
   *
   * @param visible {@code true} to display the optional checksum in the human readable text,
   *                {@code false} to hide it
   */
  public void setOptionalChecksumVisible(boolean visible) {
  }



  /**
   * {@return whether the optional checksum, if encoded in the barcode, is visible within
   * the human readable text}
   */
  public boolean isOptionalChecksumVisible() {
    return false;
  }



  /**
   * {@return whether the given barcode type supports setting the ratio between the widths of the
   * wide and narrow bars in the barcode symbol} This feature is used in two-width barcode types
   * such as {@link ImplITF 2 of 5 Interleaved (ITF)}, {@link ImplCode39 Code 39},
   * {@link ImplCode11 Code 11} and {@link ImplCodabar Codabar}.
   *
   * @see #setRatio(float ratio)
   */
  public boolean supportsRatio() {
    return false;
  }



  /**
   * Sets the ratio between the width of the wide bars and the width of the narrow bars in two-width
   * barcode types. The value must be in the range 2.0F to 3.0F, corresponding to ratios of
   * 2.0:1 to 3.0:1. If not explicitly set, the default ratio is
   * {@link LineageTwoWidth#DEFAULT_RATIO}.
   *
   * @param ratio the ratio between the width of the wide bars and the width of the narrow bars
   *              in the barcode symbol in the range 2.0F to 3.0F
   */
  public void setRatio(float ratio) {
  }



  /**
   * {@return the ratio value}
   */
  public float getRatio() {
    return 0F;
  }



  int getQuietZoneLeft() {
    return 10;
  }



  int getQuietZoneRight() {
    return 10;
  }



  double calculateModuleFactor() {
    return (double)myBarsCount;
  }



  void prepareDrawing() {
    // Get the bars and spaces of the barcode symbol as a binary string, where '1'=bar, '0'=space
    final CharSequence barsBinary = encode();

    // Group bars into continuous blocks for efficient drawing; build pairs of positions and widths
    final int barsBinaryLength = barsBinary.length();
    final ArrayList<Integer> barCoords = new ArrayList<>(113); // 113 seems a reasonable value...
    int positionCounter = 0, widthCounter = 0;
    char barOrSpace = barsBinary.charAt(0);
    for (int i=0; i<barsBinaryLength; i++) {
      char c = barsBinary.charAt(i);
      if (c != barOrSpace) {
        if (c == '0') {
          barCoords.add(positionCounter);
          barCoords.add(widthCounter);
        }
        barOrSpace = c;
        positionCounter += widthCounter;
        widthCounter = 1;
      } else {
        widthCounter++;
      }
    }
    // ... and the last one ...
    if (barsBinary.charAt(barsBinaryLength - 1) == '1') {
      barCoords.add(positionCounter);
      barCoords.add(widthCounter);
    }

    // Convert ArrayList<Integer> into an int-array - for a faster access when drawing
    final int[] bars = new int[barCoords.size()];
    for (int i=bars.length-1; i>=0; i--)
      bars[i] = barCoords.get(i);

    // Set instance variables
    myBars = bars;
    myBarsCount = barsBinaryLength;

    // Now that we have the above two values...
    myModuleFactor = calculateModuleFactor();
  }



  /**
   * Draws the barcode symbol.
   * <p>
   * Special attention is paid to the quality and, consequently, the later readability of the
   * resulting barcode. This is mainly ensured by the following three parameters:
   * <p>
   * <b>dotSize</b> - Specifies the size of a single point on the output medium, calculated from its
   * resolution. For a printer, this should be the size of a dot, determined by the printer's
   * resolution (DPI). For a bitmap image, it should be the "physical" size of a pixel, calculated
   * from the pixel density (PPI). The value should be specified in the same unit as the other
   * parameters. When using millimeters, for a resolution of 300 DPI/PPI, the formula would be:
   * 25.4 / 300. When using inches: 1 / 300. This adjustment may be negligible in high-resolution
   * output scenarios, where the value can be set to 0.0.
   * <p>
   * Note: For 1D barcodes, which mainly consist of vertical bars, only one of the two resolutions
   * is relevant. For example, when printing a 1D barcode at a 90&deg; or 270&deg; angle, the
   * vertical resolution is crucial as the bar widths must be adjusted to it. Similarly, at a 0&deg;
   * or 180&deg; angle, the horizontal resolution is important.
   * <p>
   * <b>moduleSize</b> - Allows it to specify a fixed size of the modules (bars), that will affect
   * the overall width of the barcode symbol. If set to 0.0, the method will automatically calculate
   * an appropriate module size based on the width of the bounding box and the value of
   * {@code dotSize}. In any case, if {@code dotSize} is greater than 0.0, the module size is
   * adjusted to ensure that each module has a size that is a multiple of {@code dotSize}.
   * <p>
   * <b>barWidthCorrection</b> - Adjusts the size of the modules (bars) of the barcode symbol. A
   * positive value increases the size of the modules, while a negative value reduces them. For
   * example, in the case of a printer, where ink bleeding may occur, a negative value may be
   * necessary to compensate for the ink bleeding and ensure accurate module size. Similarly, for
   * output scenarios where undesirable effects don't occur, the value can be set to 0.0.
   *
   * @param g2d the graphics context to draw on
   * @param x the x-coordinate of the top-left corner of the bounding box
   * @param y the y-coordinate of the top-left corner of the bounding box
   * @param w the width of the bounding box
   * @param h the height of the bounding box
   * @param dotSize the size of a single point on the output medium or 0.0
   * @param moduleSize the size of each module (bar) of the barcode symbol or 0.0
   * @param barWidthCorrection the bar width correction factor or 0.0
   */
  public void draw(Graphics2D g2d, double x, double y, double w, double h,
      double dotSize, double moduleSize, double barWidthCorrection) {
    if (myBars == null)
      prepareDrawing();

    if (moduleSize > 0.0) {
      if (dotSize > 0.0)
        moduleSize = (int)(moduleSize / dotSize) * dotSize;
      double d = moduleSize * myModuleFactor;
      x += (w - d) / 2.0;
      w = d;
    } else if (dotSize > 0.0) {
      double d = w / myModuleFactor;
      d = w * (int)(d / dotSize) * dotSize / d;
      x += (w - d) / 2.0;
      w = d;
    }
    draw(g2d, x, y, w, h, barWidthCorrection);
  }



  /**
   * Draws the barcode symbol.
   * <p>
   * This method variant with a shortened parameter list does not consider any quality settings.
   * It is typically suitable for printing on laser printers or exporting as vector graphics.
   * <p>
   * Please refer to the main
   * {@link #draw(Graphics2D, double, double, double, double, double, double, double) draw}
   * method for a detailed parameter description.
   *
   * @param g2d the graphics context to draw on
   * @param x the x-coordinate of the top-left corner of the bounding box
   * @param y the y-coordinate of the top-left corner of the bounding box
   * @param w the width of the bounding box
   * @param h the height of the bounding box
   */
  public void draw(Graphics2D g2d, double x, double y, double w, double h) {
    draw(g2d, x, y, w, h, 0.0);
  }



  /**
   * Draws the barcode symbol.
   * <p>
   * This method variant with a shortened parameter list only considers {@code barWidthCorrection}
   * from the quality settings and uses 0.0 for the rest.
   * <p>
   * Please refer to the main
   * {@link #draw(Graphics2D, double, double, double, double, double, double, double) draw}
   * method for a detailed parameter description.
   *
   * @param g2d the graphics context to draw on
   * @param x the x-coordinate of the top-left corner of the bounding box
   * @param y the y-coordinate of the top-left corner of the bounding box
   * @param w the width of the bounding box
   * @param h the height of the bounding box
   * @param barWidthCorrection the bar width correction factor or 0.0
   */
  public void draw(Graphics2D g2d, double x, double y, double w, double h,
      double barWidthCorrection) {
    if (myBars == null)
      prepareDrawing();

    final Rectangle2D.Double rect = new Rectangle2D.Double(0.0, y, 0.0, h);
    if (myIsTextVisible) {
      final String text = getText();
      final double moduleWidth = w / myModuleFactor;
      final double leftQuietZone = moduleWidth * getQuietZoneLeft();
      final double symbolWidth = w - leftQuietZone - moduleWidth * getQuietZoneRight();
      final Font font = myFont != null ? myFont : g2d.getFont();
      float fontSize = font.getSize2D();
      if (myIsFontSizeAdjusted) {
        fontSize = 0F;
        do {
          fontSize += FONT_SIZE_INCREMENT;
          g2d.setFont(font.deriveFont(fontSize));
        } while (g2d.getFontMetrics().getStringBounds(text, g2d).getWidth() < symbolWidth);
        fontSize -= FONT_SIZE_INCREMENT;
      }

      if (fontSize > 0F) {
        g2d.setFont(font.deriveFont(fontSize));
        final FontMetrics fm = g2d.getFontMetrics();
        final LineMetrics lm = fm.getLineMetrics(text, g2d);
        final double offset = lm.getHeight() + myTextOffset;
        final double yPos;
        if (supportsTextOnTop() && myIsTextOnTop) {
          yPos = lm.getAscent();
          rect.y += offset;
        } else {
          yPos = h - lm.getDescent();
        }
        rect.height -= offset;
        g2d.drawString(text, (float)(x + leftQuietZone + (symbolWidth -
            fm.getStringBounds(text, g2d).getWidth()) / 2.0), (float)(y + yPos));
      }
    }

    final double barWidth = w / myBarsCount;
    final double xShifted = x - barWidthCorrection;
    final double bwcTwice = barWidthCorrection * 2.0;
    for (int i=myBars.length-1; i>0; i-=2) {
      rect.x = xShifted + barWidth * myBars[i - 1];
      rect.width = barWidth * myBars[i] + bwcTwice;
      g2d.fill(rect);
    }
  }



  /**
   * {@return a copy of this object} The copy can be considered and used as a "deep copy".
   */
  public Object clone() {
    try { return super.clone(); } catch (Exception e) { return null; }
  }



  static String repeat(char c, int count) {
    StringBuilder sb = new StringBuilder(count);
    for (int i=count; i!=0; i--)
      sb.append(c);
    return sb.toString();
  }



  static boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }



  static int findNonDigitPosition(CharSequence value) {
    for (int len=value.length(), i=0; i!=len; i++)
      if (!isDigit(value.charAt(i)))
        return i;
    return -1;
  }



  static void validateNotEmpty(CharSequence content) throws BarcodeException {
    if (content.length() == 0) // isEmpty() is only available since Java 15
      throw new BarcodeException(BarcodeException.CONTENT_EMPTY,
          "Content is empty",
          "Inhalt ist leer");
  }



  static void validateDigits(CharSequence content) throws BarcodeException {
    int pos = findNonDigitPosition(content);
    if (pos >= 0)
      throw new BarcodeException(BarcodeException.CONTENT_NOT_DIGITS,
          "Character at position %s is not a digit",
          "Zeichen an Position %s ist keine Ziffer", pos);
  }



  static void validateFixedLength(CharSequence content, int length) throws BarcodeException {
    if (content.length() != length)
      throw new BarcodeException(BarcodeException.CONTENT_LENGTH_INVALID,
          "Expected character count: %s; Provided: %s",
          "Erwartete Zeichenanzahl: %s; Aktuell: %s", length, content.length());
  }



  static void validateASCII(CharSequence content) throws BarcodeException {
    for (int len=content.length(), i=0; i!=len; i++)
      if (content.charAt(i) > 127)
        throw new BarcodeException(BarcodeException.CONTENT_NOT_ASCII,
            "Character at position %s is not ASCII",
            "Zeichen an Position %s ist kein ASCII", i);
  }



  static void validateModulo10(CharSequence contentWithCheckDigit) throws BarcodeException {
    final int idx = contentWithCheckDigit.length() - 1;
    validateCheckDigit(contentWithCheckDigit.charAt(idx) - 48,
                       calculateModulo10(contentWithCheckDigit.subSequence(0, idx)));
  }



  static void validateCheckDigit(int inputCheckDigit, int validCheckDigit) throws BarcodeException {
    if (inputCheckDigit != validCheckDigit)
      throw new BarcodeException(BarcodeException.CHECKSUM_INVALID,
          "Expected check digit: %s; Provided: %s",
          "Erwartete Pr\u00FCfziffer: %s; Aktuell: %s", validCheckDigit, inputCheckDigit);
  }



  static int calculateModulo10(CharSequence contentWithoutCheckDigit) {
    final int len = contentWithoutCheckDigit.length() - 1;
    int sum = 0;
    for (int i=len; i>=0; i--) {
      int k = (int)contentWithoutCheckDigit.charAt(len - i) - 48;
      sum += (i % 2 == 0) ? k * 3 : k;
    }
    sum = 10 - (sum % 10);
    return sum == 10 ? 0 : sum;
  }



  static void throwInvalidCharacter(int position) throws BarcodeException {
    throw new BarcodeException(BarcodeException.CONTENT_INVALID,
        "Invalid character at position %s",
        "Ung\u00FCltiges Zeichen an Position %s", position);
  }



  static void throwContentLengthNotEven(int length) throws BarcodeException {
    throw new BarcodeException(BarcodeException.CONTENT_LENGTH_NOT_EVEN,
        "Character count is not even: %s",
        "Zeichenanzahl ist nicht gerade: %s", length);
  }

}
