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
 * Enumeration of QR Code error correction levels.
 * <p>
 * QR Codes include built-in redundancy that allows them to remain readable even when partially
 * damaged, dirty, or obscured. Higher error correction levels add more redundant data, enabling
 * recovery from greater damage but requiring more space in the symbol.
 * <p>
 * The four standard levels are:
 * <ul>
 * <li>{@code L} - Low (~7% redundancy)</li>
 * <li>{@code M} - Medium (~15% redundancy)</li>
 * <li>{@code Q} - Quartile (~25% redundancy)</li>
 * <li>{@code H} - High (~30% redundancy)</li>
 * </ul>
 * <p>
 * The unique integer IDs (1-4) assigned to the constants can be used for efficient storage in
 * files or databases. The IDs are small enough to be safely cast to byte if needed.
 * See {@link #getID()} and {@link #valueOf(int id)}.
 */
public enum QRCodeErrorCorrection {

  /** QR Code error correction level L (~7% redundancy) */   L ( 1,  7, "Low" ),
  /** QR Code error correction level M (~15% redundancy) */  M ( 2, 15, "Medium" ),
  /** QR Code error correction level Q (~25% redundancy) */  Q ( 3, 25, "Quartile" ),
  /** QR Code error correction level H (~30% redundancy) */  H ( 4, 30, "High" );

  private final int myID, myRedundancy;
  private final String myDescription;



  private QRCodeErrorCorrection(int id, int redundancy, String description) {
    myID          = id;
    myRedundancy  = redundancy;
    myDescription = description;
  }



  // This static cache helps to avoid repeated array creation that occurs internally in 'values()'.
  // The constant is used only by the 'valueOf(int)' method and is therefore declared next to it.
  private static final QRCodeErrorCorrection[] cachedValues = values();
  /**
   * Returns the enum constant of this class associated with the specified integer ID.
   *
   * @param id the ID of the enum constant to be returned (1-4)
   * @return the enum constant associated with the specified ID
   * @throws IllegalArgumentException if this enum class has no constant associated
   *                                  with the specified ID
   */
  public static QRCodeErrorCorrection valueOf(int id) {
    if (id > 0 && id < 5)
      return cachedValues[id - 1];
    throw new IllegalArgumentException("Invalid QR Code error correction ID: " + id);
  }



  /**
   * {@return the integer ID associated with this error correction level}
   * <p>
   * 1 for {@code L}, 2 for {@code M}, 3 for {@code Q}, 4 for {@code H}.
   *
   * @see #valueOf(int id)
   */
  public int getID() {
    return myID;
  }



  /**
   * {@return the human-readable description of this error correction level}
   * <p>
   * "Low" for {@code L}, "Medium" for {@code M}, "Quartile" for {@code Q}, "High" for {@code H}.
   */
  public String getDescription() {
    return myDescription;
  }



  /**
   * {@return the approximate built-in redundancy as a percentage}
   * <p>
   * This value can be combined with {@link #getDescription()} to create complete descriptions like
   * "Medium (~15% redundancy)" for user interfaces or logging.
   */
  public int getRedundancy() {
    return myRedundancy;
  }

}
