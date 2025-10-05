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
 * Enumeration of DataMatrix symbol sizes.
 * <p>
 * DataMatrix symbols are defined by ISO/IEC 16022. The ECC200 standard supports 30 symbol sizes
 * ranging from 10x10 to 144x144 modules. Of these 30 sizes, 24 are square and 6 are rectangular.
 * Rectangular symbols are designed for applications where the available space is distributed over,
 * for example, a long, narrow or rounded surface.
 * <p>
 * Symbol sizes do not include the quiet zone and are sorted by width, then height.
 * <p>
 * The special constant {@link #AUTO} allows automatic size selection, choosing the smallest symbol
 * that can accommodate the data to be encoded.
 * <p>
 * The unique integer IDs (0-30) assigned to the constants can be used for efficient storage in
 * files or databases. The IDs are small enough to be safely cast to byte if needed.
 * See {@link #getID()} and {@link #valueOf(int id)}.
 */
public enum DataMatrixSize {

  /** Select optimal size based on data */    AUTO (  0,   0,   0 ),
  /** DataMatrix size 1 (10x10 modules) */     S01 (  1,  10,  10 ),
  /** DataMatrix size 2 (12x12 modules) */     S02 (  2,  12,  12 ),
  /** DataMatrix size 3 (14x14 modules) */     S03 (  3,  14,  14 ),
  /** DataMatrix size 4 (16x16 modules) */     S04 (  4,  16,  16 ),
  /** DataMatrix size 5 (18x8 modules) */      S05 (  5,  18,   8 ),
  /** DataMatrix size 6 (18x18 modules) */     S06 (  6,  18,  18 ),
  /** DataMatrix size 7 (20x20 modules) */     S07 (  7,  20,  20 ),
  /** DataMatrix size 8 (22x22 modules) */     S08 (  8,  22,  22 ),
  /** DataMatrix size 9 (24x24 modules) */     S09 (  9,  24,  24 ),
  /** DataMatrix size 10 (26x12 modules) */    S10 ( 10,  26,  12 ),
  /** DataMatrix size 11 (26x26 modules) */    S11 ( 11,  26,  26 ),
  /** DataMatrix size 12 (32x8 modules) */     S12 ( 12,  32,   8 ),
  /** DataMatrix size 13 (32x32 modules) */    S13 ( 13,  32,  32 ),
  /** DataMatrix size 14 (36x12 modules) */    S14 ( 14,  36,  12 ),
  /** DataMatrix size 15 (36x16 modules) */    S15 ( 15,  36,  16 ),
  /** DataMatrix size 16 (36x36 modules) */    S16 ( 16,  36,  36 ),
  /** DataMatrix size 17 (40x40 modules) */    S17 ( 17,  40,  40 ),
  /** DataMatrix size 18 (44x44 modules) */    S18 ( 18,  44,  44 ),
  /** DataMatrix size 19 (48x16 modules) */    S19 ( 19,  48,  16 ),
  /** DataMatrix size 20 (48x48 modules) */    S20 ( 20,  48,  48 ),
  /** DataMatrix size 21 (52x52 modules) */    S21 ( 21,  52,  52 ),
  /** DataMatrix size 22 (64x64 modules) */    S22 ( 22,  64,  64 ),
  /** DataMatrix size 23 (72x72 modules) */    S23 ( 23,  72,  72 ),
  /** DataMatrix size 24 (80x80 modules) */    S24 ( 24,  80,  80 ),
  /** DataMatrix size 25 (88x88 modules) */    S25 ( 25,  88,  88 ),
  /** DataMatrix size 26 (96x96 modules) */    S26 ( 26,  96,  96 ),
  /** DataMatrix size 27 (104x104 modules) */  S27 ( 27, 104, 104 ),
  /** DataMatrix size 28 (120x120 modules) */  S28 ( 28, 120, 120 ),
  /** DataMatrix size 29 (132x132 modules) */  S29 ( 29, 132, 132 ),
  /** DataMatrix size 30 (144x144 modules) */  S30 ( 30, 144, 144 );



  private final int myID, myWidth, myHeight;



  private DataMatrixSize(int id, int width, int height) {
    myID     = id;
    myWidth  = width;
    myHeight = height;
  }



  // This static cache helps to avoid repeated array creation that occurs internally in 'values()'.
  // The constant is used only by the 'valueOf(int)' method and is therefore declared next to it.
  private static final DataMatrixSize[] cachedValues = values();
  /**
   * Returns the enum constant of this class associated with the specified integer ID.
   *
   * @param id the ID of the enum constant to be returned (0 for AUTO, 1-30 for specific sizes)
   * @return the enum constant associated with the specified ID
   * @throws IllegalArgumentException  if this enum class has no constant associated
   *                                   with the specified ID
   */
  public static DataMatrixSize valueOf(int id) {
    if (id < 0 || id > 30)
      throw new IllegalArgumentException("Invalid DataMatrix size ID: " + id);
    return cachedValues[id];
  }



  /**
   * {@return the integer ID associated with this DataMatrix size}
   * <p>
   * Returns {@code 0} for {@link #AUTO}, or {@code 1-30} for specific sizes.
   *
   * @see #valueOf(int id)
   */
  public int getID() {
    return myID;
  }



  /**
   * {@return the symbol width in modules of this DataMatrix size}
   */
  public int getWidth() {
    return myWidth;
  }



  /**
   * {@return the symbol height in modules of this DataMatrix size}
   */
  public int getHeight() {
    return myHeight;
  }



  /**
   * {@return true if this DataMatrix size represents a square symbol}
   */
  public boolean isSquare() {
    return myID > 0 && myWidth == myHeight;
  }



  /**
   * {@return true if this DataMatrix size represents a rectangular symbol}
   */
  public boolean isRectangle() {
    return myID > 0 && myWidth != myHeight;
  }

}
