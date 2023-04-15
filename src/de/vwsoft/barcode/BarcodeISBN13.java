/*
 * Copyright 2023 by Viktor Wedel, https://www.vwsoft.de/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.vwsoft.barcode;
import java.awt.*;
import java.util.*;


public class BarcodeISBN13 extends BarcodeEAN13 {

  // number of modules by which the plain isbn drawn above the barcode symbol may extend to the left
  // and to the right beyond the respective guard bars
  public static int LEDGE = 5;


  //----
  public BarcodeISBN13(String number, boolean autoComplete) throws IllegalArgumentException {
    super(number, autoComplete);
  }


  //----
  public BarcodeISBN13() throws IllegalArgumentException {
    super("000-0-0000-0000-0", false);
  }


  //----
  public void setNumber(String number, boolean autoComplete, boolean addOptChecksum)
      throws IllegalArgumentException {
    checkEmpty(number);

    // split the ISBN number into parts; make sure that it consists only of valid characters
    // (digits and '-') and does not contain more than one '-' in sequence; make sure that the
    // number does not start with '-'; it may end with '-', but then it must consist of only 4
    // parts, i.e. it must not contain a check digit
    ArrayList<String> tokens = new ArrayList<>(5);
    StringBuilder sb = new StringBuilder(7);
    for (int i=0; i!=number.length(); i++) {
      char c = number.charAt(i);
      if (isDigit(c)) {
        sb.append(c);
      } else if (c == '-' && sb.length() != 0 && tokens.size() != 4) {
        tokens.add(sb.toString());
        sb.setLength(0);
      } else {
        throwIAE(number);
      }
    }
    if (sb.length() != 0) // collect last token
      tokens.add(sb.toString());

    final int numberOfTokens = tokens.size();
    if (numberOfTokens != 5 && !(autoComplete && numberOfTokens == 4))
      throwIAE(number);

    String isbnPrefix    = tokens.get(0);
    String isbnGroup     = tokens.get(1);
    String isbnPublisher = tokens.get(2);
    String isbnTitle     = tokens.get(3);
    if ( isbnPrefix.length()   != 3 ||
         isbnGroup.length()     > 5 ||
         isbnPublisher.length() > 7 ||
         isbnTitle.length()     > 6 )
      throwIAE(number);

    String ean13 = isbnPrefix + isbnGroup + isbnPublisher + isbnTitle;
    if (ean13.length() != 12)
      throwIAE(number);

    if (numberOfTokens == 5) {
      String checkDigit = tokens.get(4);
      if (checkDigit.length() != 1)
        throwIAE(number);
      ean13 += checkDigit;
      checkModulo10(ean13);
    } else { // if (autoComplete && numberOfTokens == 4) already checked above
      int checkDigit = calculateModulo10(ean13);
      ean13 += checkDigit;
      if (number.charAt(number.length() - 1) != '-')
        number += '-';
      number += checkDigit;
    }

    myHumanReadableNumber = number;
    super.setNumber(ean13, false, false);
  }


  //----
  public void draw(Graphics2D g2d, double x, double y, double w, double h,
      double barWidthCorrection) {
    super.draw(g2d, x, y, w, h, barWidthCorrection);

    final double widthOfASingleBar = w / myBarsCount;
    final double width = (LEDGE * 2 + 95) * widthOfASingleBar;

    g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 1));
    final String isbnStr = "ISBN " + myHumanReadableNumber;
    float fs = 0F;
    do {
      fs += FONT_SIZE_STEP;
      g2d.setFont(g2d.getFont().deriveFont(fs));
    } while (g2d.getFontMetrics().getStringBounds(isbnStr, g2d).getWidth() < width);
    fs -= FONT_SIZE_STEP;

    if (fs > 0F) {
      g2d.setFont(g2d.getFont().deriveFont(fs));
      int leftQuietZone = myIsQuietZonesIncluded ? getQuietZoneLeft() : 0;
      g2d.drawString(isbnStr, (float)(x + (leftQuietZone - LEDGE) * widthOfASingleBar + (width -
          g2d.getFontMetrics().getStringBounds(isbnStr, g2d).getWidth()) / 2.0), (float)(y -
          g2d.getFontMetrics().getLineMetrics(isbnStr, g2d).getDescent()));
    }
  }

}
