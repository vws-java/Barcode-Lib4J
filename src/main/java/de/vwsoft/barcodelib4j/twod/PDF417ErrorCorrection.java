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
 * Enumeration of PDF417 error correction levels.
 * <p>
 * PDF417 includes built-in error correction to recover from symbol damage. The error correction
 * level determines how much data redundancy is added to the symbol. Higher levels provide better
 * damage recovery but increase the symbol size.
 * <p>
 * PDF417 supports 9 error correction levels (0-8). Level 0 provides error detection only
 * (no correction capability), while levels 1-8 provide increasing error correction capability
 * with each level.
 * <p>
 * The unique level numbers (0-8) assigned to the constants can be used for efficient storage in
 * files or databases. The numbers are small enough to be safely cast to byte if needed.
 * See {@link #getLevelNumber()} and {@link #valueOf(int levelNumber)}.
 */
public enum PDF417ErrorCorrection {

  /** PDF417 error correction level 0 */  EC0 ( 0 ),
  /** PDF417 error correction level 1 */  EC1 ( 1 ),
  /** PDF417 error correction level 2 */  EC2 ( 2 ),
  /** PDF417 error correction level 3 */  EC3 ( 3 ),
  /** PDF417 error correction level 4 */  EC4 ( 4 ),
  /** PDF417 error correction level 5 */  EC5 ( 5 ),
  /** PDF417 error correction level 6 */  EC6 ( 6 ),
  /** PDF417 error correction level 7 */  EC7 ( 7 ),
  /** PDF417 error correction level 8 */  EC8 ( 8 );

  private final int myLevelNumber;



  private PDF417ErrorCorrection(int levelNumber) {
    myLevelNumber = levelNumber;
  }



  // This static cache helps to avoid repeated array creation that occurs internally in 'values()'.
  // The constant is used only by the 'valueOf(int)' method and is therefore declared next to it.
  private static final PDF417ErrorCorrection[] cachedValues = values();
  /**
   * Returns the enum constant of this class associated with the specified level number.
   *
   * @param levelNumber the level number of the enum constant to be returned (0-8)
   * @return the enum constant associated with the specified level number
   * @throws IllegalArgumentException if this enum class has no constant associated
   *                                  with the specified level number
   */
  public static PDF417ErrorCorrection valueOf(int levelNumber) {
    if (levelNumber < 0 || levelNumber > 8)
      throw new IllegalArgumentException("Invalid PDF417 error correction level: " + levelNumber);
    return cachedValues[levelNumber];
  }



  /**
   * {@return the level number (0-8) of this error correction level}
   *
   * @see #valueOf(int levelNumber)
   */
  public int getLevelNumber() {
    return myLevelNumber;
  }



  /**
   * {@return the number of error correction codewords for this level}
   * <p>
   * According to ISO/IEC 15438, PDF417 uses 2^(level+1) error correction codewords,
   * ranging from 2 at level 0 (detection only) to 512 at level 8 (maximum correction).
   * <p>
   * This value can be combined with {@link #getLevelNumber()} to create complete
   * descriptions like "Level 3 (16 codewords)" for user interfaces or logging.
   */
  public int getErrorCorrectionCodewords() {
    return 2 << myLevelNumber;
  }

}
