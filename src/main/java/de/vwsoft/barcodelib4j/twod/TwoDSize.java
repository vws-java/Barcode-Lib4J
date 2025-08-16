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


/**
 * Immutable pair of width and height.
 */
public class TwoDSize implements Comparable<TwoDSize> {

  /** The width value. */
  public final int width;

  /** The height value. */
  public final int height;



  /**
   * Creates a new instance with the specified width and height.
   *
   * @param width  the width
   * @param height the height
   */
  public TwoDSize(int width, int height) {
    this.width = width;
    this.height = height;
  }



  /**
   * Compares this object with another {@code TwoDSize} object. First, widths are compared; if they
   * are equal, heights are compared.
   *
   * @param other the {@code TwoDSize} to be compared
   * @return a negative integer, zero, or a positive integer if this object is less than, equal to,
   *     or greater than the specified object
   */
  @Override
  public int compareTo(TwoDSize other) {
    int wDiff = width - other.width;
    return wDiff != 0 ? wDiff : height - other.height;
  }



  /**
   * Indicates whether some other object is "equal to" this one. Returns {@code true} if and only if
   * the parameter is also an instance of this class and both instances have the same width and
   * height values.
   *
   * @param obj the reference object with which to compare
   * @return {@code true} if this object equals to {@code obj}, {@code false} otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof TwoDSize) {
      TwoDSize size = (TwoDSize)obj;
      return size.width == width && size.height == height;
    }
    return false;
  }



  /**
   * {@return the hash code calculated from the width and height values}
   */
  @Override
  public int hashCode() {
    return (31 * 17 + width) * 31 + height;
  }



  /**
   * {@return a string representation of this instance} This method is intended to be used only for
   * debugging purposes.
   */
  @Override
  public String toString() {
    return getClass().getName() + "[width=" + width + ",height=" + height + "]";
  }

}
