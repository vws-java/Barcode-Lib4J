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
package de.vwsoft.barcodelib4j.image;
import java.awt.Color;


/**
 * Stores a color independently in the RGB and CMYK color models.
 * <p>
 * There are two types of constructors available:
 * <ul>
 *   <li>The first type accepts colors in both RGB and CMYK color models as parameters and stores
 *     them separately.</li>
 *   <li>The second type accepts either an RGB or a CMYK color as a parameter and converts the other
 *     color using a simple algorithm based on the provided color.</li>
 * </ul>
 * The second type of constructor is likely the one you will use most often, as you typically work
 * with just one of the two color models.
 * <p>
 * Note: Since the CMYK color model does not support transparency, any transparency information
 * passed as a parameter for RGB colors is ignored.
 */
public class CompoundColor extends Color {

  /** Constant representing the color black in both RGB and CMYK color models. */
  public static final CompoundColor CC_BLACK = new CompoundColor(0x000000, 0x00000064);

  /** Constant representing the color white in both RGB and CMYK color models. */
  public static final CompoundColor CC_WHITE = new CompoundColor(0xFFFFFF, 0x00000000);

  /** The CMYK color value. The value for RGB is stored in the java.awt.Color superclass. */
  private int myCMYKColor;



  /**
   * Constructs a new instance with the specified RGB and CMYK values. No conversion
   * between RGB and CMYK color models takes place.
   *
   * @param r the red component of the RGB color (0-255)
   * @param g the green component of the RGB color (0-255)
   * @param b the blue component of the RGB color (0-255)
   * @param c the cyan component of the CMYK color (0-100)
   * @param m the magenta component of the CMYK color (0-100)
   * @param y the yellow component of the CMYK color (0-100)
   * @param k the key (black) component of the CMYK color (0-100)
   */
  public CompoundColor(int r, int g, int b, int c, int m, int y, int k) {
    super(r, g, b, 255);
    myCMYKColor = toInteger(c, m, y, k);
  }



  /**
   * Constructs a new instance with the specified RGB and CMYK values. No conversion
   * between RGB and CMYK color models takes place.
   *
   * @param rgb the RGB value of the color
   * @param cmyk the CMYK value of the color
   */
  public CompoundColor(int rgb, int cmyk) {
    super(rgb);
    myCMYKColor = cmyk;
  }



  /**
   * Constructs a new instance with the specified RGB and CMYK values. No conversion
   * between RGB and CMYK color models takes place. The RGB value must occupy the higher 32 bits of
   * the {@code long} parameter, while the CMYK value must occupy the lower 32 bits.
   *
   * @param rgbAndCmyk a {@code long} value representing both the RGB and CMYK colors
   * @see #getRGBandCMYK()
   */
  public CompoundColor(long rgbAndCmyk) {
    super((int)(rgbAndCmyk >> 32));
    myCMYKColor = (int)rgbAndCmyk;
  }



  /**
   * Constructs a new instance with the specified RGB values. The CMYK values are
   * calculated internally from the given RGB values, striving for the closest approximation.
   *
   * @param r the red component of the RGB color (0-255)
   * @param g the green component of the RGB color (0-255)
   * @param b the blue component of the RGB color (0-255)
   */
  public CompoundColor(int r, int g, int b) {
    super(r, g, b, 255);
    myCMYKColor = toCMYK(r, g, b);
  }



  /**
   * Constructs a new instance with the specified CMYK values. The RGB values are
   * calculated internally from the given CMYK values, striving for the closest approximation.
   *
   * @param c the cyan component of the CMYK color (0-100)
   * @param m the magenta component of the CMYK color (0-100)
   * @param y the yellow component of the CMYK color (0-100)
   * @param k the key (black) component of the CMYK color (0-100)
   */
  public CompoundColor(int c, int m, int y, int k) {
    super(toRGB(c, m, y, k), true);
    myCMYKColor = toInteger(c, m, y, k);
  }



  /**
   * Constructs a new instance with the specified {@code java.awt.Color}. The CMYK values are
   * calculated internally from the given RGB values, striving for the closest approximation.
   *
   * @param rgbColor  the {@code java.awt.Color} object from which to construct the
   *                  {@code CompoundColor}
   */
  public CompoundColor(Color rgbColor) {
    super(rgbColor.getRGB());
    myCMYKColor = toCMYK(rgbColor.getRGB());
  }



  /**
   * Constructs a new instance with the specified value. If {@code isRGB} is
   * {@code true}, the integer value represents an RGB color, otherwise, it represents a CMYK color.
   * The CMYK values are calculated internally if the integer value represents an RGB color, or the
   * RGB values are calculated internally if the integer value represents a CMYK color, striving for
   * the closest approximation.
   *
   * @param value the integer value representing either an RGB or CMYK color
   * @param isRGB {@code true} if the value represents an RGB color, {@code false} if it represents
   *              a CMYK color
   */
  public CompoundColor(int value, boolean isRGB) {
    super(isRGB ? value : toRGB(value));
    myCMYKColor = isRGB ? toCMYK(value) : value;
  }



  /**
   * {@return a {@code long} value representing both the RGB and CMYK colors} The RGB value
   * occupies the higher 32 bits and the CMYK value occupies the lower 32 bits.
   */
  public long getRGBandCMYK() {
    return ((long)getRGB() << 32) | myCMYKColor;
  }



  /**
   * {@return the CMYK color value} The cyan component occupies the higher 8 bits, the magenta
   * component occupies the next 8 bits, the yellow component occupies the next 8 bits, and the
   * key (black) component occupies the lower 8 bits.
   */
  public int getCMYK() {
    return myCMYKColor;
  }



  /**
   * {@return the cyan component of the CMYK color, ranging from 0 to 100}
   */
  public int getCyan() {
    return myCMYKColor >> 24;
  }



  /**
   * {@return the magenta component of the CMYK color, ranging from 0 to 100}
   */
  public int getMagenta() {
    return (myCMYKColor >> 16) & 0xFF;
  }



  /**
   * {@return the yellow component of the CMYK color, ranging from 0 to 100}
   */
  public int getYellow() {
    return (myCMYKColor >> 8) & 0xFF;
  }



  /**
   * {@return the key (black) component of the CMYK color, ranging from 0 to 100}
   */
  public int getKey() {
    return myCMYKColor & 0xFF;
  }



  /**
   * {@return a hash code value for this object}
   */
  @Override
  public int hashCode() {
    return Long.hashCode(getRGBandCMYK());
  }



  /**
   * Determines whether another object is equal to this {@code CompoundColor}. The result is
   * {@code true} if and only if the argument is not {@code null} and is a {@code CompoundColor}
   * object that has the same RGB and CMYK values as this object.
   *
   * @param obj  the object to test for equality with this {@code CompoundColor}
   * @return     {@code true} if the objects are the same, {@code false} otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof CompoundColor) {
      CompoundColor cc = (CompoundColor)obj;
      return cc.getRGB() == getRGB() && cc.getCMYK() == getCMYK();
    }
    return false;
  }



  /**
   * {@return a string representation of this {@code CompoundColor}} This method is intended to be
   * used only for debugging purposes.
   */
  @Override
  public String toString() {
    return getClass().getName() + "[r=" + getRed() + ",g=" + getGreen() + ",b=" + getBlue() + "]" +
        "[c=" + getCyan() + ",m=" + getMagenta() + ",y=" + getYellow() + ",k=" + getKey() + "]";
  }



  private static int toCMYK(int r, int g, int b) {
    float c = (255 - r) / 2.55F;
    float m = (255 - g) / 2.55F;
    float y = (255 - b) / 2.55F;
    float k = Math.min(c, Math.min(m, y));
    float tmp = (100F - k) / 100F;
    if (tmp > 0F) {
      c = (c - k) / tmp;
      m = (m - k) / tmp;
      y = (y - k) / tmp;
    } else {
      c = m = y = 0F;
    }
    return toInteger(Math.round(c), Math.round(m), Math.round(y), Math.round(k));
  }
  private static int toCMYK(int rgb) {
    return toCMYK((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
  }



  private static int toRGB(int c, int m, int y, int k) {
    final float oneMinusK = 1F - k / 100F;
    final int r = Math.round(255F * (1F - c / 100F) * oneMinusK);
    final int g = Math.round(255F * (1F - m / 100F) * oneMinusK);
    final int b = Math.round(255F * (1F - y / 100F) * oneMinusK);
    return 0xFF000000 | (r << 16) | (g << 8) | b;
  }
  private static int toRGB(int cmyk) {
    return toRGB(cmyk >> 24, (cmyk >> 16) & 0xFF, (cmyk >> 8) & 0xFF, cmyk & 0xFF);
  }



  private static int toInteger(int c, int m, int y, int k) {
    return (c << 24) | (m << 16) | (y << 8) | k;
  }

}
