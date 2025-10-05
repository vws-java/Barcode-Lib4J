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
 * Enumeration of Aztec symbol sizes.
 * <p>
 * Aztec sizes are defined by ISO/IEC 24778 and include both normal and compact variants.
 * Normal variants have 1 to 32 layers with sizes ranging from 19x19 to 151x151 modules.
 * Compact variants have 1 to 4 layers with sizes ranging from 15x15 to 27x27 modules.
 * <p>
 * All Aztec symbols are square. In contrast to other 2D codes Aztec does not require a quiet zone
 * as its bullseye finder pattern enables reliable detection without surrounding white space.
 * <p>
 * The special constant {@link #AUTO} allows automatic size selection, choosing the smallest symbol
 * that can accommodate the data to be encoded.
 * <p>
 * The unique layer counts (1 to 32 for normal sizes, -1 to -4 for compact sizes, 0 for AUTO)
 * assigned to the constants can be used as IDs for efficient storage in files or databases.
 * The numbers are small enough to be safely cast to signed byte if needed.
 * See {@link #getLayerCount()} and {@link #valueOf(int layerCount)}.
 */
public enum AztecSize {

  /** Select optimal size based on data */  AUTO      (  0,   0 ),
  /** Aztec compact 1 (15x15 modules) */    COMPACT01 ( -1,  15 ),
  /** Aztec compact 2 (19x19 modules) */    COMPACT02 ( -2,  19 ),
  /** Aztec compact 3 (23x23 modules) */    COMPACT03 ( -3,  23 ),
  /** Aztec compact 4 (27x27 modules) */    COMPACT04 ( -4,  27 ),
  /** Aztec normal 1 (19x19 modules) */     NORMAL01  (  1,  19 ),
  /** Aztec normal 2 (23x23 modules) */     NORMAL02  (  2,  23 ),
  /** Aztec normal 3 (27x27 modules) */     NORMAL03  (  3,  27 ),
  /** Aztec normal 4 (31x31 modules) */     NORMAL04  (  4,  31 ),
  /** Aztec normal 5 (37x37 modules) */     NORMAL05  (  5,  37 ),
  /** Aztec normal 6 (41x41 modules) */     NORMAL06  (  6,  41 ),
  /** Aztec normal 7 (45x45 modules) */     NORMAL07  (  7,  45 ),
  /** Aztec normal 8 (49x49 modules) */     NORMAL08  (  8,  49 ),
  /** Aztec normal 9 (53x53 modules) */     NORMAL09  (  9,  53 ),
  /** Aztec normal 10 (57x57 modules) */    NORMAL10  ( 10,  57 ),
  /** Aztec normal 11 (61x61 modules) */    NORMAL11  ( 11,  61 ),
  /** Aztec normal 12 (67x67 modules) */    NORMAL12  ( 12,  67 ),
  /** Aztec normal 13 (71x71 modules) */    NORMAL13  ( 13,  71 ),
  /** Aztec normal 14 (75x75 modules) */    NORMAL14  ( 14,  75 ),
  /** Aztec normal 15 (79x79 modules) */    NORMAL15  ( 15,  79 ),
  /** Aztec normal 16 (83x83 modules) */    NORMAL16  ( 16,  83 ),
  /** Aztec normal 17 (87x87 modules) */    NORMAL17  ( 17,  87 ),
  /** Aztec normal 18 (91x91 modules) */    NORMAL18  ( 18,  91 ),
  /** Aztec normal 19 (95x95 modules) */    NORMAL19  ( 19,  95 ),
  /** Aztec normal 20 (101x101 modules) */  NORMAL20  ( 20, 101 ),
  /** Aztec normal 21 (105x105 modules) */  NORMAL21  ( 21, 105 ),
  /** Aztec normal 22 (109x109 modules) */  NORMAL22  ( 22, 109 ),
  /** Aztec normal 23 (113x113 modules) */  NORMAL23  ( 23, 113 ),
  /** Aztec normal 24 (117x117 modules) */  NORMAL24  ( 24, 117 ),
  /** Aztec normal 25 (121x121 modules) */  NORMAL25  ( 25, 121 ),
  /** Aztec normal 26 (125x125 modules) */  NORMAL26  ( 26, 125 ),
  /** Aztec normal 27 (131x131 modules) */  NORMAL27  ( 27, 131 ),
  /** Aztec normal 28 (135x135 modules) */  NORMAL28  ( 28, 135 ),
  /** Aztec normal 29 (139x139 modules) */  NORMAL29  ( 29, 139 ),
  /** Aztec normal 30 (143x143 modules) */  NORMAL30  ( 30, 143 ),
  /** Aztec normal 31 (147x147 modules) */  NORMAL31  ( 31, 147 ),
  /** Aztec normal 32 (151x151 modules) */  NORMAL32  ( 32, 151 );

  private final int myLayerCount, mySize;



  private AztecSize(int layerCount, int size) {
    myLayerCount = layerCount;
    mySize       = size;
  }



  // This static cache helps to avoid repeated array creation that occurs internally in 'values()'.
  // The constant is used only by the 'valueOf(int)' method and is therefore declared next to it.
  private static final AztecSize[] cachedValues = values();
  /**
   * Returns the enum constant of this class corresponding to the specified layer count.
   *
   * @param layerCount the layer count (1 to 32 for normal, -1 to -4 for compact, 0 for AUTO)
   * @return the enum constant corresponding to the specified layer count
   * @throws IllegalArgumentException if this enum class has no constant corresponding
   *                                  to the specified layer count
   */
  public static AztecSize valueOf(int layerCount) {
    if (layerCount < -4 || layerCount > 32)
      throw new IllegalArgumentException("Invalid Aztec layer count: " + layerCount);
    if (layerCount > 0)
      return cachedValues[layerCount + 4];
    return cachedValues[-layerCount];
  }



  /**
   * {@return the layer count corresponding to this Aztec size}
   * <p>
   * Returns positive values (1 to 32) for normal sizes, negative values (-1 to -4)
   * for compact sizes, or 0 for {@link #AUTO}.
   *
   * @see #valueOf(int layerCount)
   */
  public int getLayerCount() {
    return myLayerCount;
  }



  /**
   * {@return the symbol size (width/height in modules) of this Aztec size}
   */
  public int getSize() {
    return mySize;
  }



  /**
   * {@return true if this Aztec size represents a normal symbol}
   */
  public boolean isNormal() {
    return myLayerCount > 0;
  }



  /**
   * {@return true if this Aztec size represents a compact symbol}
   */
  public boolean isCompact() {
    return myLayerCount < 0;
  }

}
