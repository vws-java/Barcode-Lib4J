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
import java.awt.font.*;
import java.awt.geom.*;


public abstract class BarcodeUPCFamily extends Barcode {
  public static int ADDON_RIGHT_QUIET_ZONE = 5; // modules

  private static final int BARS[] = { 13, 25, 19, 61, 35, 49, 47, 59, 55, 11 };
  private static final int[] ADDON5_PARITIES = { 24, 20, 18, 17, 12, 6, 3, 10, 9, 5 };

  protected String myAddOn;
  protected boolean myIsAddOnOnBottom;
  private int[] myDash;

  protected transient String myPart1, myPart2, myPart3, myPart4;


  //---- abstract methods
  protected abstract String getBarLengthPattern();
  protected abstract double getPositionOfPart2();
  protected abstract double getPositionOfPart3();
  protected abstract void computeBarsOfPart2(StringBuilder sb);
  protected abstract void computeBarsOfPart3(StringBuilder sb);


  //----
  public void setAddOn(String addOn) throws IllegalArgumentException {
    if (addOn != null) {
      if (addOn.length() != 2 && addOn.length() != 5)
        throw new IllegalArgumentException("Invalid Add-On length: " + addOn.length());
      if (!isInteger(addOn))
        throw new IllegalArgumentException("Add-On must be an integer value");
      myAddOn = addOn;
    } else {
      myAddOn = null;
    }
    reset();
  }


  //----
  public final boolean hasAddOn() {
    return myAddOn != null;
  }


  //----
  public boolean isAddOnSupported() {
    return true;
  }


  //----
  public void setAddOnOnBottom(boolean b) {
    myIsAddOnOnBottom = b;
  }


  //----
  public boolean isAddOnOnBottom() {
    return myIsAddOnOnBottom;
  }


  //----
  public boolean isSettingHumanReadableNumberSupported() {
    return false;
  }


  //----
  public boolean isNumberOnTopSupported() {
    return false;
  }


  //---- 0-weight, 1-y-pos, 2-offset
  public void setTestBar(int[] params) {
    myDash = params;
  }


  //----
  protected String computeBars() {
    StringBuilder sb = new StringBuilder(165);      // 165 modules = ean-13 + quiet zone + addon-5
    if (myIsQuietZonesIncluded)
      sb.append(repeat('0', getQuietZoneLeft()));
    sb.append("101");                               // left guards
    computeBarsOfPart2(sb);
    sb.append("01010");                             // center guards
    computeBarsOfPart3(sb);
    sb.append("101");                               // right guards

    if (hasAddOn()) {
      sb.append(repeat('0', getQuietZoneRight()));
      sb.append("1011");
      if (myAddOn.length() == 2) {
        int m = Integer.parseInt(myAddOn) % 4;
        sb.append(m < 2 ? get7BitA(myAddOn.charAt(0) - 48) : get7BitB(myAddOn.charAt(0) - 48));
        sb.append("01");
        sb.append(m % 2 == 0 ? get7BitA(myAddOn.charAt(1) - 48) : get7BitB(myAddOn.charAt(1) - 48));
      } else { // if (myAddOn.length() == 5)
        int m = 0;
        for (int i=4; i>=0; i--)
          m += (i % 2 == 0 ? 3 : 9) * (myAddOn.charAt(i) - 48);
        int parity = ADDON5_PARITIES[m % 10];
        for (int i=4; i>=0; i--) {
          sb.append(((parity >> i) & 0x1) == 0 ?
                    get7BitA(myAddOn.charAt(4 - i) - 48) : get7BitB(myAddOn.charAt(4 - i) - 48));
          if (i != 0)
            sb.append("01");
        }
      }
      if (myIsQuietZonesIncluded)
        sb.append(repeat('0', ADDON_RIGHT_QUIET_ZONE));
    } else if (myIsQuietZonesIncluded) {
      sb.append(repeat('0', getQuietZoneRight()));
    }

    return sb.toString();
  }


  //----
  public void draw(Graphics2D g2d, double x, double y, double w, double h,
      double barWidthCorrection) {
    final double widthOfASingleBar = w / myBarsCount;
    final int leftQuietZoneIfOne = myIsQuietZonesIncluded ? getQuietZoneLeft() : 0;

    double fontHeight = 0.0;
    if (myIsNumberVisible) {
      if (myFont != null) g2d.setFont(myFont);
      float fs = myFixedFontSize;
      if (fs == 0F) {
        final double m = (myPart2.length() * 7) * widthOfASingleBar;
        do {
          fs += FONT_SIZE_STEP;
          g2d.setFont(g2d.getFont().deriveFont(fs));
        } while (g2d.getFontMetrics().getStringBounds(myPart2, g2d).getWidth() < m);
        fs -= FONT_SIZE_STEP;
      }

      if (fs > 0F) {
        g2d.setFont(g2d.getFont().deriveFont(fs));
        FontMetrics fm = g2d.getFontMetrics();
        LineMetrics lm = fm.getLineMetrics(myPart2, g2d);
        final double descent = widthOfASingleBar * 2.0; // ignore font's own descent
        float fy = (float)(y + h - descent);
        final double horOffset = widthOfASingleBar;
        // draw add-on number and calculate position for part 4 at the same time
        int numberOfBars = myBarsCount;
        if (hasAddOn()) {
          final int numberOfAddOnBars = myAddOn.length() == 2 ? 20 : 47;
          numberOfBars -= numberOfAddOnBars;
          if (myIsQuietZonesIncluded)
            numberOfBars -= ADDON_RIGHT_QUIET_ZONE;
          g2d.drawString(myAddOn,
                        (float)(x + numberOfBars * widthOfASingleBar + (numberOfAddOnBars *
                          widthOfASingleBar - fm.getStringBounds(myAddOn, g2d).getWidth()) / 2.0),
                        myIsAddOnOnBottom ? fy : (float)(y + myNumberOffset) + lm.getAscent());
          numberOfBars -= getQuietZoneRight();
        } else if (myIsQuietZonesIncluded) {
          numberOfBars -= getQuietZoneRight();
        }
        // draw part 1
        if (myPart1 != null)
          g2d.drawString(myPart1, (float)(x + widthOfASingleBar * leftQuietZoneIfOne -
              fm.getStringBounds(myPart1, g2d).getWidth() - horOffset), fy);
        // draw part 2; part 2 must not be 'null'!
        g2d.drawString(myPart2, (float)(x + widthOfASingleBar * (leftQuietZoneIfOne +
            getPositionOfPart2()) + (myPart2.length() * 7 * widthOfASingleBar -
            fm.getStringBounds(myPart2, g2d).getWidth()) / 2.0), fy);
        // draw part 3
        if (myPart3 != null)
          g2d.drawString(myPart3, (float)(x + widthOfASingleBar * (leftQuietZoneIfOne +
              getPositionOfPart3()) + (myPart3.length() * 7 * widthOfASingleBar -
              fm.getStringBounds(myPart3, g2d).getWidth()) / 2.0), fy);
        // draw part 4
        if (myPart4 != null)
          g2d.drawString(myPart4, (float)(x + (numberOfBars * widthOfASingleBar) + horOffset), fy);
        fontHeight = lm.getAscent() + descent + myNumberOffset;
      }
    }

    final double nonGuardBarHeight = h - fontHeight;
    final double guardBarHeight = !myIsNumberVisible ? nonGuardBarHeight :
        nonGuardBarHeight + widthOfASingleBar * 5.0; // according to specification
    final String barLengthPattern = getBarLengthPattern();
    final double xShifted = x - barWidthCorrection;
    final double bwcTwice = barWidthCorrection * 2.0;
    final double addOnY, addOnHeight;
    if (myIsAddOnOnBottom) {
      addOnY = y;
      addOnHeight = nonGuardBarHeight;
    } else {
      addOnY = y + fontHeight;
      addOnHeight = guardBarHeight - fontHeight;
    }
    final Rectangle2D.Double rect = new Rectangle2D.Double(0.0, y, 0.0, 0.0);
    for (int i=0; i!=myBars.length; i+=2) {
      final int position = myBars[i];
      rect.x = xShifted + widthOfASingleBar * position;
      rect.width = widthOfASingleBar * myBars[i + 1] + bwcTwice;
      switch (barLengthPattern.charAt(position - leftQuietZoneIfOne)) {
        case '0': rect.height = nonGuardBarHeight;  break;
        case '1': rect.height = guardBarHeight;     break;
        default : rect.height = addOnHeight; rect.y = addOnY;
      }
      g2d.fill(rect);
    }
    if (myDash != null) {
      rect.x = xShifted + widthOfASingleBar * (leftQuietZoneIfOne - myDash[2]);
      rect.y = y + widthOfASingleBar * myDash[1];
      rect.width = widthOfASingleBar * (barLengthPattern.indexOf('2') + 2 * myDash[2]) + bwcTwice;
      rect.height = widthOfASingleBar * myDash[0];
      g2d.fill(rect);
    }
  }


  //----
  protected static String get7BitA(int idx) {
    String s = Integer.toBinaryString(BARS[idx]);
    return "0000000".substring(0, 7 - s.length()) + s;
  }


  //----
  protected static String get7BitB(int idx) {
    String s = get7BitC(idx);
    char[] c = new char[7];
    for (int i=6; i>=0; i--)
      c[6 - i] = s.charAt(i);
    return new String(c);
  }


  //----
  protected static String get7BitC(int idx) {
    String s = get7BitA(idx);
    char[] c = new char[7];
    for (int i=6; i>=0; i--)
      c[i] = s.charAt(i) == '1' ? '0' : '1';
    return new String(c);
  }

}
