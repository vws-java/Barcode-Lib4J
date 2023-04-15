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
import java.util.*;


public abstract class Barcode implements Cloneable {

  // growth factor to gradually adjust the font size to the barcode symbol,
  // if requested (= if font size is set to 0, see below)
  public static float FONT_SIZE_STEP = 0.1F;

  // the valid and complete barcode number, including (if applicable) checksum, leading zero etc.
  protected String myNumber;

  // barcode number as it is finally drawn (does not include optional add-on in case of ean)
  protected String myHumanReadableNumber;

  // specifies whether or not the barcode number is drawn as human readable text
  protected boolean myIsNumberVisible = true;

  // specifies whether the barcode number is drawn above or below the barcode symbol
  protected boolean myIsNumberOnTop;

  // (vertical) space between the barcode symbol and the human readable number
  protected double myNumberOffset;

  // if set to 'null' the preset font of the Graphics2D object is used.
  // in any case the size of the font is ignored and must be specified separately
  protected Font myFont;

  // if set to '0', the font size is automatically adjusted to the size of the barcode symbol
  protected float myFixedFontSize;

  // specifies whether or not the quiet zones should be included
  protected boolean myIsQuietZonesIncluded = true;

  // representation of the barcode symbol as a paired list of positions and widths,
  // taking into account quiet zones
  protected transient int[] myBars;

  // corresponds to the number of modules for barcodes without ratio,
  // otherwise something of a larger value
  protected transient int myBarsCount;

  // factor for conversion between module width and barcode width;
  // equals to 'myBarsCount' for barcodes without ratio
  protected transient double myModuleFactor;


  //----
  protected abstract String computeBars();


  //----
  public abstract void setNumber(String number, boolean autoComplete, boolean addOptChecksum)
      throws IllegalArgumentException;


  //----
  public String getNumber() {
    return myNumber;
  }


  //----
  public boolean isSettingHumanReadableNumberSupported() {
    return true;
  }


  //----
  public void setHumanReadableNumber(String hrn) {
    myHumanReadableNumber = hrn;
  }


  //----
  public String getHumanReadableNumber() {
    return myHumanReadableNumber != null ? myHumanReadableNumber : myNumber;
  }


  //----
  public boolean isAddOnSupported() {
    return false;
  }


  //----
  public boolean hasAddOn() {
    return false;
  }


  //----
  public void setAddOn(String addOn) throws IllegalArgumentException {
  }


  //----
  public String getAddOn() {
    return null;
  }


  //----
  public void setAddOnOnBottom(boolean b) {
  }


  //----
  public boolean isAddOnOnBottom() {
    return false;
  }


  //---- 0-weight, 1-y-pos, 2-offset
  public void setTestBar(int[] params) {
  }


  //----
  public final void setNumberVisible(boolean visible) {
    myIsNumberVisible = visible;
  }


  //----
  public final boolean isNumberVisible() {
    return myIsNumberVisible;
  }


  //----
  public boolean isNumberOnTopSupported() {
    return true;
  }


  //----
  public void setNumberOnTop(boolean onTop) {
    myIsNumberOnTop = onTop;
  }


  //----
  public boolean isNumberOnTop() {
    return myIsNumberOnTop;
  }


  //----
  public final void setNumberOffset(double offset) {
    myNumberOffset = offset;
  }


  //----
  public final double getNumberOffset() {
    return myNumberOffset;
  }


  //----
  public final void setFont(Font font) {
    myFont = font;
  }


  //----
  public final void setFixedFontSize(float fontSize) {
    myFixedFontSize = fontSize;
  }


  //----
  public boolean isCompletionSupported() {
    return true;
  }


  //----
  public boolean isOptionalChecksumSupported() {
    return false;
  }


  //----
  public boolean isOptionalChecksumUsed() {
    return false;
  }


  //----
  public void setOptionalChecksumVisible(boolean visible) {
  }


  //----
  public boolean isOptionalChecksumVisible() {
    return false;
  }


  //----
  public final boolean isRatioSupported() {
    return getSupportedRatios().length != 0;
  }


  //----
  public float[] getSupportedRatios() {
    return new float[0];
  }


  //----
  public float getDefaultRatio() {
    return 0F;
  }


  //----
  public void setRatio(float ratio) {
  }


  //----
  public float getRatio() {
    return 0F;
  }


  //----
  protected int getQuietZoneLeft() {
    return 10;
  }


  //----
  protected int getQuietZoneRight() {
    return 10;
  }


  //----
  public void setQuietZonesIncluded(boolean b) {
    if (myIsQuietZonesIncluded != b) {
      myIsQuietZonesIncluded = b;
      reset();
    }
  }


  //----
  public boolean isQuietZonesIncluded() {
    return myIsQuietZonesIncluded;
  }


  //----
  protected double calculateModuleFactor() {
    return (double)myBarsCount;
  }


  //----
  protected final void reset() {
    // get the bars and spaces of the barcode symbol as a binary string, where '1'=bar, '0'=space
    final String barsBinary = computeBars();

    // group bars into continuous blocks for efficient drawing; build pairs of positions and widths
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

    // convert ArrayList<Integer> into an int-array - for a faster access when drawing
    final int n = barCoords.size();
    final int[] bars = new int[n];
    for (int i=n-1; i>=0; i--)
      bars[i] = barCoords.get(i);

    // set instance variables
    myBars = bars;
    myBarsCount = barsBinaryLength;

    // now that we have the above two values...
    myModuleFactor = calculateModuleFactor();
  }


  //----
  public void draw(Graphics2D g2d, double x, double y, double w, double h, double dotSize,
      double moduleSize, double barWidthCorrection) {
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


  //----
  public void draw(Graphics2D g2d, double x, double y, double w, double h, double dotSize,
      int dotsPerModule, double barWidthCorrection) {
    if (dotSize > 0.0) {
      double d;
      if (dotsPerModule > 0) {
        d = dotsPerModule * dotSize * myModuleFactor;
      } else {
        d = w / myModuleFactor;
        d = w * (int)(d / dotSize) * dotSize / d;
      }
      x += (w - d) / 2.0;
      w = d;
    }
    draw(g2d, x, y, w, h, barWidthCorrection);
  }


  //----
  public void draw(Graphics2D g2d, double x, double y, double w, double h,
      double barWidthCorrection) {
    final double widthOfASingleBar = w / myBarsCount;
    final Rectangle2D.Double rect = myIsNumberVisible ?
        drawNumber(g2d, getHumanReadableNumber(), x, y, w, h) :
        new Rectangle2D.Double(0.0, y, 0.0, h);
    final double xShifted = x - barWidthCorrection;
    final double bwcTwice = barWidthCorrection * 2.0;
    for (int i=myBars.length-1; i>0; i-=2) {
      rect.x = xShifted + widthOfASingleBar * myBars[i - 1];
      rect.width = widthOfASingleBar * myBars[i] + bwcTwice;
      g2d.fill(rect);
    }
  }


  //----
  protected Rectangle2D.Double drawNumber(Graphics2D g2d, String number,
        double x, double y, double w, double h) {
    Rectangle2D.Double result = new Rectangle2D.Double(0.0, y, 0.0, h);

    if (myIsQuietZonesIncluded) {
      double widthOfASingleBar = w / myModuleFactor;
      double lqz = widthOfASingleBar * getQuietZoneLeft();
      x += lqz;
      w -= widthOfASingleBar * getQuietZoneRight() + lqz;
    }

    if (myFont != null)
      g2d.setFont(myFont);
    float fs = myFixedFontSize;
    if (fs == 0F) {
      do {
        fs += FONT_SIZE_STEP;
        g2d.setFont(g2d.getFont().deriveFont(fs));
      } while (g2d.getFontMetrics().getStringBounds(number, g2d).getWidth() < w);
      fs -= FONT_SIZE_STEP;
    }
    if (fs <= 0F)
      return result;

    g2d.setFont(g2d.getFont().deriveFont(fs));

    FontMetrics fm = g2d.getFontMetrics();
    LineMetrics lm = fm.getLineMetrics(number, g2d);

    final double offset = lm.getHeight() + myNumberOffset;
    double yPos;
    if (isNumberOnTopSupported() && isNumberOnTop()) {
      yPos = y + lm.getAscent();
      result.y += offset;
    } else {
      yPos = y + h - lm.getDescent();
    }
    result.height -= offset;

    g2d.drawString(number,
        (float)(x + (w - fm.getStringBounds(number, g2d).getWidth()) / 2.0), (float)yPos);
    return result;
  }


  //----
  public Object clone() {
    try { return super.clone(); } catch (Exception e) { return null; }
  }


  //----
  protected static boolean isInteger(String value) {
    for (int i=value.length()-1; i>=0; i--) {
      char c = value.charAt(i);
      if (c < '0' || c > '9')
        return false;
    }
    return true;
  }


  //----
  protected static boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }


  //----
  protected static String repeat(char c, int count) {
    char[] chars = new char[count];
    for (int i=count-1; i>=0; i--)
      chars[i] = c;
    return new String(chars);
  }


  //----
  protected static void checkEmpty(String number) throws IllegalArgumentException {
    if (number.isEmpty())
      throw new IllegalArgumentException("Barcode number is an empty string");
  }


  //----
  protected static void checkInteger(String number) throws IllegalArgumentException {
    if (!isInteger(number))
      throw new IllegalArgumentException("Barcode number must be an integer value");
  }


  //----
  protected static void checkFixedLength(String number, int length)
      throws IllegalArgumentException {
    if (number.length() != length)
      throw new IllegalArgumentException("Invalid length of the barcode number: " +
                                         number.length() + "; must be " + length);
  }


  //----
  protected static void checkAscii(String number) throws IllegalArgumentException {
    for (int i=number.length()-1; i>=0; i--)
      if (number.charAt(i) > 127)
        throw new IllegalArgumentException("Barcode number contains non 7Bit-ASCII characters");
  }


  //----
  protected static void checkModulo10(String numberWithCheckDigit)
      throws IllegalArgumentException {
    final int len = numberWithCheckDigit.length();
    final int len2 = len - 1;
    if (calculateModulo10(numberWithCheckDigit.substring(0, len2)) !=
        Integer.parseInt(numberWithCheckDigit.substring(len2, len)))
      throw new IllegalArgumentException("Checksum does not match");
  }


  //----
  protected static int calculateModulo10(String numberWithoutCheckDigit) {
    final int len = numberWithoutCheckDigit.length() - 1;
    int sum = 0;
    for (int i=len; i>=0; i--) {
      int k = (int)numberWithoutCheckDigit.charAt(len - i) - 48;
      sum += (i % 2 == 0) ? k * 3 : k;
    }
    sum = 10 - (sum % 10);
    return sum == 10 ? 0 : sum;
  }


  //----
  protected static void throwIAE(String number) throws IllegalArgumentException {
    throw new IllegalArgumentException("Invalid barcode number: " + number);
  }


  //----
  protected static void throwNumberLengthNotEven(int length) throws IllegalArgumentException {
    throw new IllegalArgumentException("Number length must be even but was [" + length + "]");
  }

}
