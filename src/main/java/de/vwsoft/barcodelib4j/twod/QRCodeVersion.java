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
 * Enumeration of QR Code versions with their corresponding symbol sizes.
 * <p>
 * QR Code versions are defined by ISO/IEC 18004 and range from version 1 (21x21 modules) to version
 * 40 (177x177 modules). All QR Code symbols are square. Symbol sizes do not include the quiet zone.
 * <p>
 * The special constant {@link #AUTO} allows automatic version selection, choosing the
 * smallest version that can accommodate the data to be encoded.
 * <p>
 * The unique numbers (0-40) assigned to the constants can be used for efficient storage in files or
 * databases. The numbers are small enough to be safely cast to byte if needed.
 * See {@link #getNumber()} and {@link #valueOf(int number)}.
 */
public enum QRCodeVersion {

  /** Select optimal version based on data */  AUTO (  0,   0 ),
  /** QR Code version 1 (21x21 modules) */      V01 (  1,  21 ),
  /** QR Code version 2 (25x25 modules) */      V02 (  2,  25 ),
  /** QR Code version 3 (29x29 modules) */      V03 (  3,  29 ),
  /** QR Code version 4 (33x33 modules) */      V04 (  4,  33 ),
  /** QR Code version 5 (37x37 modules) */      V05 (  5,  37 ),
  /** QR Code version 6 (41x41 modules) */      V06 (  6,  41 ),
  /** QR Code version 7 (45x45 modules) */      V07 (  7,  45 ),
  /** QR Code version 8 (49x49 modules) */      V08 (  8,  49 ),
  /** QR Code version 9 (53x53 modules) */      V09 (  9,  53 ),
  /** QR Code version 10 (57x57 modules) */     V10 ( 10,  57 ),
  /** QR Code version 11 (61x61 modules) */     V11 ( 11,  61 ),
  /** QR Code version 12 (65x65 modules) */     V12 ( 12,  65 ),
  /** QR Code version 13 (69x69 modules) */     V13 ( 13,  69 ),
  /** QR Code version 14 (73x73 modules) */     V14 ( 14,  73 ),
  /** QR Code version 15 (77x77 modules) */     V15 ( 15,  77 ),
  /** QR Code version 16 (81x81 modules) */     V16 ( 16,  81 ),
  /** QR Code version 17 (85x85 modules) */     V17 ( 17,  85 ),
  /** QR Code version 18 (89x89 modules) */     V18 ( 18,  89 ),
  /** QR Code version 19 (93x93 modules) */     V19 ( 19,  93 ),
  /** QR Code version 20 (97x97 modules) */     V20 ( 20,  97 ),
  /** QR Code version 21 (101x101 modules) */   V21 ( 21, 101 ),
  /** QR Code version 22 (105x105 modules) */   V22 ( 22, 105 ),
  /** QR Code version 23 (109x109 modules) */   V23 ( 23, 109 ),
  /** QR Code version 24 (113x113 modules) */   V24 ( 24, 113 ),
  /** QR Code version 25 (117x117 modules) */   V25 ( 25, 117 ),
  /** QR Code version 26 (121x121 modules) */   V26 ( 26, 121 ),
  /** QR Code version 27 (125x125 modules) */   V27 ( 27, 125 ),
  /** QR Code version 28 (129x129 modules) */   V28 ( 28, 129 ),
  /** QR Code version 29 (133x133 modules) */   V29 ( 29, 133 ),
  /** QR Code version 30 (137x137 modules) */   V30 ( 30, 137 ),
  /** QR Code version 31 (141x141 modules) */   V31 ( 31, 141 ),
  /** QR Code version 32 (145x145 modules) */   V32 ( 32, 145 ),
  /** QR Code version 33 (149x149 modules) */   V33 ( 33, 149 ),
  /** QR Code version 34 (153x153 modules) */   V34 ( 34, 153 ),
  /** QR Code version 35 (157x157 modules) */   V35 ( 35, 157 ),
  /** QR Code version 36 (161x161 modules) */   V36 ( 36, 161 ),
  /** QR Code version 37 (165x165 modules) */   V37 ( 37, 165 ),
  /** QR Code version 38 (169x169 modules) */   V38 ( 38, 169 ),
  /** QR Code version 39 (173x173 modules) */   V39 ( 39, 173 ),
  /** QR Code version 40 (177x177 modules) */   V40 ( 40, 177 );

  private final int myNumber, mySize;



  private QRCodeVersion(int number, int size) {
    myNumber = number;
    mySize   = size;
  }



  // This static cache helps to avoid repeated array creation that occurs internally in 'values()'.
  // The constant is used only by the 'valueOf(int)' method and is therefore declared next to it.
  private static final QRCodeVersion[] cachedValues = values();
  /**
   * Returns the enum constant of this class corresponding to the specified version number.
   *
   * @param number the version number (0 for AUTO, 1-40 for specific versions)
   * @return the enum constant corresponding to the specified version number
   * @throws IllegalArgumentException if the specified version number is outside the valid range
   */
  public static QRCodeVersion valueOf(int number) {
    if (number < 0 || number > 40)
      throw new IllegalArgumentException("Invalid QR Code version number: " + number);
    return cachedValues[number];
  }



  /**
   * {@return the version number of this QR Code version}
   * <p>
   * Returns 0 for {@link #AUTO}, or 1-40 for specific versions.
   *
   * @see #valueOf(int number)
   */
  public int getNumber() {
    return myNumber;
  }



  /**
   * {@return the symbol size (width/height in modules) of this QR Code version}
   */
  public int getSize() {
    return mySize;
  }

}
