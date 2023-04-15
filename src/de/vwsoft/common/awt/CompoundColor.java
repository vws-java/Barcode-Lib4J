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

package de.vwsoft.common.awt;
import java.awt.*;


/**
Stores a color in two different color models (RGB and CMYK) independently of each other.
*/
public class CompoundColor extends Color {
  public static final CompoundColor CC_BLACK = new CompoundColor(0, 0, 0,         0, 0, 0, 100);
  public static final CompoundColor CC_WHITE = new CompoundColor(255, 255, 255,   0, 0, 0, 0);

  private int myCMYKColor;


  //---- constructors where no conversion between color models takes place
  public CompoundColor(int r, int g, int b, int c, int m, int y, int k) {
    super(r, g, b, 255);
    myCMYKColor = toInteger(c, m, y, k);
  }
  public CompoundColor(int rgb, int cmyk) {
    super(rgb);
    myCMYKColor = cmyk;
  }


  //---- constructors with conversion from one model to another
  public CompoundColor(int r, int g, int b) {
    super(r, g, b, 255);
    myCMYKColor = toCMYK(r, g, b);
  }
  public CompoundColor(int c, int m, int y, int k) {
    super(toRGB(c, m, y, k));
    myCMYKColor = toInteger(c, m, y, k);
  }
  public CompoundColor(int value, boolean isRGB) {
    super(isRGB ? value : toRGB(value));
    myCMYKColor = isRGB ? toCMYK(value) : value;
  }


  //----
  public int getCMYK()    { return myCMYKColor;                }
  public int getCyan()    { return (myCMYKColor >> 24);        }
  public int getMagenta() { return (myCMYKColor >> 16) & 0xFF; }
  public int getYellow()  { return (myCMYKColor >>  8) & 0xFF; }
  public int getKey()     { return (myCMYKColor      ) & 0xFF; }


  //----
  public static int toCMYK(int r, int g, int b) {
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
  public static int toCMYK(int rgb) {
    return toCMYK((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
  }


  //----
  public static int toRGB(int c, int m, int y, int k) {
    final float oneMinusK = 1F - k / 100F;
    final int r = Math.round(255F * (1F - c / 100F) * oneMinusK);
    final int g = Math.round(255F * (1F - m / 100F) * oneMinusK);
    final int b = Math.round(255F * (1F - y / 100F) * oneMinusK);
    return (r << 16) | (g << 8) | b;
  }
  public static int toRGB(int cmyk) {
    return toRGB(cmyk >> 24, (cmyk >> 16) & 0xFF, (cmyk >> 8) & 0xFF, cmyk & 0xFF);
  }


  //----
  public boolean equals(Object o) {
    if (o instanceof CompoundColor) {
      CompoundColor cc = (CompoundColor)o;
      return cc.getRGB() == getRGB() && cc.getCMYK() == getCMYK();
    }
    return false;
  }


  //----
  private static int toInteger(int c, int m, int y, int k) {
    return (c << 24) | (m << 16) | (y << 8) | k;
  }

}
