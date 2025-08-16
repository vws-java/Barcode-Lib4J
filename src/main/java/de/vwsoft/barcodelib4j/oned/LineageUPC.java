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
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;


/**
 * Abstract class that provides common functionality for UPC barcode types and their derivatives.
 * <p>
 * This class is implemented by:
 * <ul>
 *   <li>{@link ImplUPCA UPC-A}</li>
 *   <li>{@link ImplUPCE UPC-E}</li>
 *   <li>{@link ImplEAN13 EAN-13}</li>
 *   <li>{@link ImplEAN8 EAN-8}</li>
 *   <li>{@link ImplISBN13 ISBN-13}</li>
 *   <li>{@link ImplISMN ISMN}</li>
 * </ul>
 */
public abstract class LineageUPC extends Barcode {

  private static final int[] BARS = { 13, 25, 19, 61, 35, 49, 47, 59, 55, 11 };
  private static final int[] ADDON5_PARITIES = { 24, 20, 18, 17, 12, 6, 3, 10, 9, 5 };
  private static final int ADDON_RIGHT_QUIET_ZONE = 5; // modules, according to specs

  private String myAddOn;
  private int[] myTestBar;

  transient String myNumberPart1, myNumberPart2, myNumberPart3, myNumberPart4;



  // Hide default constructor from JavaDoc by making it package-private
  LineageUPC() {}



  // Abstract methods
  abstract String getBarLengthPattern();



  @Override
  public void setAddOn(String addOnNumber) throws BarcodeException {
    if (addOnNumber != null) {
      if (addOnNumber.isEmpty())
        throw new BarcodeException(BarcodeException.ADDON_EMPTY,
            "Add-On number must not be empty",
            "Add-On-Nummer darf nicht leer sein");
      if (addOnNumber.length() != 2 && addOnNumber.length() != 5)
        throw new BarcodeException(BarcodeException.ADDON_LENGTH_INVALID,
            "Add-On number length must be either 2 or 5; Provided: %s",
            "L\u00E4nge der Add-On-Nummer muss entweder 2 oder 5 sein; Aktuell: %s",
            addOnNumber.length());
      if (findNonDigitPosition(addOnNumber) >= 0)
        throw new BarcodeException(BarcodeException.ADDON_NOT_DIGITS,
            "Add-On number must consist only of digits",
            "Add-On-Nummer darf nur aus Ziffern bestehen");
    }
    myAddOn = addOnNumber;
    myBars = null; // Reset bars to trigger recalculation next time drawing occurs
  }



  @Override
  public String getAddOn() {
    return myAddOn;
  }



  /** @hidden */
  @Override
  public boolean supportsAddOn() {
    return true;
  }



  /** @hidden */
  @Override
  public void setCustomText(String text) {
  }



  /** @hidden */
  @Override
  public boolean supportsCustomText() {
    return false;
  }



  /** @hidden */
  @Override
  public boolean supportsTextOnTop() {
    return false;
  }



  /**
   * Sets the parameters for drawing a non-specification-compliant line across the barcode symbol.
   * This method is implemented by UPC family types only and is not intended for general use.
   *
   * @param params an integer array containing the parameters for drawing the test bar:
   *               - params[0]: The weight of the test bar.
   *               - params[1]: The y-position of the test bar.
   *               - params[2]: The offset of the test bar.
   * @hidden
   */
  public void setTestBar(int[] params) {
    myTestBar = params != null && params.length == 3 ?
        java.util.Arrays.copyOf(params, params.length) : null;
  }



  @Override
  CharSequence encode() {
    StringBuilder sb = new StringBuilder(165);
    sb.append(repeat('0', getQuietZoneLeft()));     // left quiet zone
    sb.append("101");                               // left guards
    encodeLeftPart(sb);
    sb.append("01010");                             // center guards
    encodeRightPart(sb);
    sb.append("101");                               // right guards
    sb.append(repeat('0', getQuietZoneRight()));    // right quiet zone
    encodeAddOn(sb);                                // add-on, if present
    return sb;
  }



  void encodeLeftPart(StringBuilder sb) {
  }
  void encodeRightPart(StringBuilder sb) {
  }



  void encodeAddOn(StringBuilder sb) {
    if (myAddOn != null) {
      sb.append("1011");
      if (myAddOn.length() == 2) {
        int m = Integer.parseInt(myAddOn) % 4;
        sb.append(m < 2 ? encodeA(myAddOn.charAt(0) - 48) : encodeB(myAddOn.charAt(0) - 48));
        sb.append("01");
        sb.append(m % 2 == 0 ? encodeA(myAddOn.charAt(1) - 48) : encodeB(myAddOn.charAt(1) - 48));
      } else { // if (myAddOn.length() == 5)
        int m = 0;
        for (int i=4; i>=0; i--)
          m += (i % 2 == 0 ? 3 : 9) * (myAddOn.charAt(i) - 48);
        int parity = ADDON5_PARITIES[m % 10];
        for (int i=4; i>=0; i--) {
          sb.append(((parity >> i) & 0x1) == 0 ?
                    encodeA(myAddOn.charAt(4 - i) - 48) : encodeB(myAddOn.charAt(4 - i) - 48));
          if (i != 0)
            sb.append("01");
        }
      }
      sb.append(repeat('0', ADDON_RIGHT_QUIET_ZONE));
    }
  }



  /** @hidden */
  @Override
  public void draw(Graphics2D g2d, double x, double y, double w, double h,
      double barWidthCorrection) {
    if (myBars == null)
      prepareDrawing();

    final String barLengthPattern = getBarLengthPattern();
    final double widthOfASingleBar = w / myBarsCount;
    final int leftQuietZone = getQuietZoneLeft();

    double fontHeight = 0.0;
    if (myIsTextVisible) {
      final Font font = myFont != null ? myFont : g2d.getFont();
      float fontSize = font.getSize2D();
      if (myIsFontSizeAdjusted) {
        fontSize = 0F;
        final double m = (myNumberPart2.length() * 7) * widthOfASingleBar;
        do {
          fontSize += FONT_SIZE_INCREMENT;
          g2d.setFont(font.deriveFont(fontSize));
        } while (g2d.getFontMetrics().getStringBounds(myNumberPart2, g2d).getWidth() < m);
        fontSize -= FONT_SIZE_INCREMENT;
      }

      if (fontSize > 0F) {
        g2d.setFont(font.deriveFont(fontSize));
        FontMetrics fm = g2d.getFontMetrics();
        final double ascent = fm.getLineMetrics(myNumberPart2, g2d).getAscent();
        final double descent = widthOfASingleBar * 1.5; // ignore font's own descent
        fontHeight = ascent + descent + myTextOffset;
        final float fy = (float)(y + h - descent);
        // draw add-on number and calculate position for part 4 at the same time
        int numberOfBars = myBarsCount;
        if (myAddOn != null) {
          final int numberOfAddOnBars = myAddOn.length() == 2 ? 20 : 47;
          numberOfBars -= (numberOfAddOnBars + ADDON_RIGHT_QUIET_ZONE);
          g2d.drawString(myAddOn, (float)(x + widthOfASingleBar *
              numberOfBars + (numberOfAddOnBars * widthOfASingleBar -
                  fm.getStringBounds(myAddOn, g2d).getWidth()) / 2.0),
              (float)(y + myTextOffset + ascent));
        }
        // draw part 1
        if (myNumberPart1 != null)
          g2d.drawString(myNumberPart1, (float)x, fy);
        // draw part 2; part 2 must not be 'null'!
        double part2Pos = barLengthPattern.indexOf('0') + .5;
        g2d.drawString(myNumberPart2, (float)(x + widthOfASingleBar *
            (leftQuietZone + part2Pos) + (myNumberPart2.length() * 7 * widthOfASingleBar -
            fm.getStringBounds(myNumberPart2, g2d).getWidth()) / 2.0), fy);
        // draw part 3
        if (myNumberPart3 != null) {
          double part3Pos = barLengthPattern.indexOf('1', 30) + 4.5;
          g2d.drawString(myNumberPart3, (float)(x + widthOfASingleBar *
              (leftQuietZone + part3Pos) + (myNumberPart3.length() * 7 * widthOfASingleBar -
              fm.getStringBounds(myNumberPart3, g2d).getWidth()) / 2.0), fy);
        }
        // draw part 4
        if (myNumberPart4 != null)
          g2d.drawString(myNumberPart4, (float)(x + widthOfASingleBar *
              numberOfBars - fm.getStringBounds(myNumberPart4, g2d).getWidth()), fy);
      }

      if (myText != null) { // ISBN-13 and ISSN only
        final int overhang = 5;
        final double textWidth = (overhang * 2 + 95) * widthOfASingleBar;

        Font font2 = new Font(Font.SANS_SERIF, Font.PLAIN, 1);
        float fontSize2 = 0F;
        do {
          fontSize2 += FONT_SIZE_INCREMENT;
          g2d.setFont(font2.deriveFont(fontSize2));
        } while (g2d.getFontMetrics().getStringBounds(myText, g2d).getWidth() < textWidth);
        fontSize2 -= FONT_SIZE_INCREMENT;

        if (fontSize2 > 0F) {
          g2d.setFont(font2.deriveFont(fontSize2));
          FontMetrics fm = g2d.getFontMetrics();
          g2d.drawString(myText, (float)(x + widthOfASingleBar * (leftQuietZone - overhang) +
              (textWidth - fm.getStringBounds(myText, g2d).getWidth()) / 2.0),
              (float)(y - fm.getLineMetrics(myText, g2d).getDescent()));
        }
      }
    }

    final double nonGuardBarHeight = h - fontHeight;
    final double guardBarHeight = !myIsTextVisible ? nonGuardBarHeight :
        nonGuardBarHeight + widthOfASingleBar * 5.0; // according to specification
    final double addOnBarY = y + fontHeight;
    final double addOnBarHeight = guardBarHeight - fontHeight;
    final double xShifted = x - barWidthCorrection;
    final double bwcTwice = barWidthCorrection * 2.0;
    final Rectangle2D.Double rect = new Rectangle2D.Double(0.0, y, 0.0, 0.0);
    for (int i=0; i!=myBars.length; i+=2) {
      rect.x = xShifted + widthOfASingleBar * myBars[i];
      rect.width = widthOfASingleBar * myBars[i + 1] + bwcTwice;
      switch (barLengthPattern.charAt(myBars[i] - leftQuietZone)) {
        case '0': rect.height = nonGuardBarHeight;  break;
        case '1': rect.height = guardBarHeight;     break;
        default : rect.height = addOnBarHeight; rect.y = addOnBarY;
      }
      g2d.fill(rect);
    }

    if (myTestBar != null) {
      final int addOnPos = barLengthPattern.indexOf('2', 51);
      rect.x = xShifted + widthOfASingleBar * (leftQuietZone - myTestBar[2]);
      rect.y = y + widthOfASingleBar * myTestBar[1];
      rect.width = widthOfASingleBar * (addOnPos + 2 * myTestBar[2]) + bwcTwice;
      rect.height = widthOfASingleBar * myTestBar[0];
      g2d.fill(rect);
    }
  }



  static String encodeA(int digit) {
    String s = Integer.toBinaryString(BARS[digit]);
    return repeat('0', 7 - s.length()) + s;
  }



  static String encodeB(int digit) {
    return new StringBuilder(encodeC(digit)).reverse().toString();
  }



  static String encodeC(int digit) {
    StringBuilder sb = new StringBuilder(7);
    for (int i=6; i!=0; i--)
      sb.append(((BARS[digit] >> i) & 1) ^ 1);
    return sb.append('0').toString();
  }



  // Sets the content for ISBN-13 and ISSN barcode types only. The 'content' parameter must be
  // non-empty and must have the correct prefix prior to calling this method.
  final void setContentISxN(String content, boolean autoComplete, String typeName)
      throws BarcodeException {

    // Split the ISxN string into parts and ensure it consists only of valid characters
    // (digits and '-') without consecutive '-' characters. The string may end with '-' but,
    // in that case, must consist of exactly 4 parts instead of 5, excluding a check digit.
    ArrayList<String> tokens = new ArrayList<>(5);
    StringBuilder sb = new StringBuilder(7);
    for (int len=content.length(), i=0; i!=len; i++) {
      char c = content.charAt(i);
      if (isDigit(c)) {
        sb.append(c);
      } else if (c == '-') {
        if (tokens.size() == 4)
          throw new BarcodeException(BarcodeException.CONTENT_INVALID,
              "Number has more than 5 segments",
              "Nummer besteht aus mehr als 5 Teilen");
        tokens.add(sb.toString());
        sb.setLength(0);
      } else {
        throw new BarcodeException(BarcodeException.CONTENT_INVALID,
            "Invalid character at position %s",
            "Ung\u00FCltiges Zeichen an Position %s", i);
      }
    }
    if (sb.length() != 0) // Collect the final token
      tokens.add(sb.toString());

    final int numberOfTokens = tokens.size();
    if (numberOfTokens != 5 && !(autoComplete && numberOfTokens == 4))
      throw new BarcodeException(BarcodeException.CONTENT_INVALID,
          "Incorrect number of segments: %s",
          "Falsche Anzahl an Teilen: %s", numberOfTokens);

    String isxnPrefix    = tokens.get(0);
    String isxnGroup     = tokens.get(1);
    String isxnPublisher = tokens.get(2);
    String isxnItem      = tokens.get(3);
    if ( isxnGroup.length()     > 5 ||
         isxnPublisher.length() > 7 ||
         isxnItem.length()      > 6 )
      throw new BarcodeException(BarcodeException.CONTENT_INVALID,
          "Some segments have incorrect length",
          "Einige Teile haben inkorrekte L\u00E4nge");

    String ean13 = isxnPrefix + isxnGroup + isxnPublisher + isxnItem;
    if (ean13.length() != 12)
      throw new BarcodeException(BarcodeException.CONTENT_LENGTH_INVALID,
          "Expected number of digits excluding check digit: 12; Provided: %s",
          "Erwartete Anzahl der Ziffern ohne Pr\u00FCfziffer: 12; Aktuell: %s",
          ean13.length());

    if (numberOfTokens == 5) {
      String checkDigit = tokens.get(4);
      if (checkDigit.length() != 1)
        throw new BarcodeException(BarcodeException.CHECKSUM_INVALID,
            "Check digit must be one character; Provided: %s",
            "Pr\u00FCfziffer muss einstellig sein; Aktuell: %s", checkDigit.length());
      ean13 += checkDigit;
      validateModulo10(ean13);
    } else { // if (autoComplete && numberOfTokens == 4) already checked above
      int checkDigit = calculateModulo10(ean13);
      ean13 += checkDigit;
      if (content.charAt(content.length() - 1) != '-')
        content += '-';
      content += checkDigit;
    }

    myContent = ean13;
    myText = typeName + ' ' + content;
    myBars = null; // Reset bars to trigger recalculation next time drawing occurs
  }

}
