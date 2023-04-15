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


public class BarcodeISMN extends BarcodeEAN13 {

  // number of modules by which the plain ismn drawn above the barcode symbol may extend to the left
  // and to the right beyond the respective guard bars
  public static int LEDGE = 5;


  //----
  public BarcodeISMN(String number, boolean autoComplete) throws IllegalArgumentException {
    super(number, autoComplete);
  }


  //----
  public BarcodeISMN() throws IllegalArgumentException {
    super("979-0-0000-0000-1", false);
  }


  //----
  public void setNumber(String number, boolean autoComplete, boolean addOptChecksum)
      throws IllegalArgumentException {
    checkEmpty(number);

    final int len = number.length();
    ArrayList<String> tokens = new ArrayList<>(5);
    StringBuilder sb = new StringBuilder(7);
    for (int i=0; i!=len; i++) {
      char c = number.charAt(i);
      if (isDigit(c)) {
        sb.append(c);
      } else if (c == '-' && sb.length() != 0 && i < len - 1) {
        tokens.add(sb.toString());
        sb.setLength(0);
      } else {
        throwIAE(number);
      }
    }
    if (sb.length() != 0) // collect last token
      tokens.add(sb.toString());

    if (tokens.size() < 4) {
      tokens.add(0, "shift");
      tokens.add(0, "shift");
    } else if (!"979".equals(tokens.get(0)) || !"0".equals(tokens.get(1))) {
      throwIAE(number);
    }

    if (tokens.size() != 5 && !(autoComplete && tokens.size() == 4))
      throwIAE(number);

    String ismnPublisher = tokens.get(2);
    String ismnTitle     = tokens.get(3);
    if (ismnPublisher.length() < 3 || ismnPublisher.length() > 7 ||
        ismnPublisher.length() + ismnTitle.length() != 8)
      throwIAE(number);

    String ean13 = "9790" + ismnPublisher + ismnTitle;
    String readable = "979-0-" + ismnPublisher + "-" + ismnTitle + "-";
    if (tokens.size() == 5) {
      String checkDigit = tokens.get(4);
      if (checkDigit.length() != 1)
        throwIAE(number);
      ean13 += checkDigit;
      checkModulo10(ean13);
      readable += checkDigit;
    } else { // if (autoComplete && numberOfTokens == 4) already checked above
      int checkDigit = calculateModulo10(ean13);
      ean13 += checkDigit;
      readable += checkDigit;
    }

    myHumanReadableNumber = readable;
    super.setNumber(ean13, false, false);
  }


  //----
  public void draw(Graphics2D g2d, double x, double y, double w, double h,
      double barWidthCorrection) {
    super.draw(g2d, x, y, w, h, barWidthCorrection);

    final double widthOfASingleBar = w / myBarsCount;
    final double width = (LEDGE * 2 + 95) * widthOfASingleBar;

    g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 1));
    final String ismnStr = "ISMN " + myHumanReadableNumber;
    float fs = 0F;
    do {
      fs += FONT_SIZE_STEP;
      g2d.setFont(g2d.getFont().deriveFont(fs));
    } while (g2d.getFontMetrics().getStringBounds(ismnStr, g2d).getWidth() < width);
    fs -= FONT_SIZE_STEP;

    if (fs > 0F) {
      g2d.setFont(g2d.getFont().deriveFont(fs));
      int leftQuietZone = myIsQuietZonesIncluded ? getQuietZoneLeft() : 0;
      g2d.drawString(ismnStr, (float)(x + (leftQuietZone - LEDGE) * widthOfASingleBar + (width -
          g2d.getFontMetrics().getStringBounds(ismnStr, g2d).getWidth()) / 2.0), (float)(y -
          g2d.getFontMetrics().getLineMetrics(ismnStr, g2d).getDescent()));
    }
  }

}

