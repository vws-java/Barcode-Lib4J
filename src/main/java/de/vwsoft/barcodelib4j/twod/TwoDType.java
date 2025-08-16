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
 * Enumeration of all supported 2D code types.
 * <p>
 * Each 2D code type has a unique integer ID which can optionally be used for efficient storage in a
 * file or database (e.g. as a byte type). See {@link #getID()} and {@link #valueOf(int id)}.
 */
public enum TwoDType {

  /** Code type "QR Code"        */
  QRCODE          ("QR Code",         1),

  /** Code type "DataMatrix"     */
  DATAMATRIX      ("DataMatrix",      2),

  /** Code type "PDF 417"        */
  PDF417          ("PDF 417",         3),

  /** Code type "Aztec"          */
  AZTEC           ("Aztec",           4),

  /** Code type "GS1 QR Code"    */
  GS1_QRCODE      ("GS1 QR Code",     5),

  /** Code type "GS1 DataMatrix" */
  GS1_DATAMATRIX  ("GS1 DataMatrix",  6);



  private final String myTypeName;
  private final int myID;



  private TwoDType(String typeName, int id) {
    myTypeName = typeName;
    myID = id;
  }



  /**
   * Returns the enum constant of this class associated with the specified integer ID.
   *
   * @param id the ID of the enum constant to be returned.
   * @return the enum constant associated with the specified ID
   * @throws IllegalArgumentException  if this enum class has no constant associated
   *                                   with the specified ID
   */
  public static TwoDType valueOf(int id) {
    for (TwoDType codeType : values())
      if (codeType.getID() == id)
        return codeType;
    throw new IllegalArgumentException("Invalid code type ID: " + id);
  }



  /**
   * {@return the integer ID associated with this 2D code type}
   */
  public int getID() {
    return myID;
  }



  /**
   * Convenience method that returns whether this enum constant represents a GS1 code type.
   * It is a shortcut for:
   * <pre>    foo == TwoDType.GS1_QRCODE || foo == TwoDType.GS1_DATAMATRIX</pre>
   *
   * @return {@code true} if the enum constant represents a GS1 code type, {@code false} otherwise
   */
  public boolean isGS1() {
    return this == GS1_QRCODE || this == GS1_DATAMATRIX;
  }



  /**
   * {@return the name of this 2D code type}
   */
  public String getTypeName() {
    return myTypeName;
  }



  /**
   * {@return the name of this 2D code type} Equivalent to {@link #getTypeName()}.
   */
  @Override
  public String toString() {
    return myTypeName;
  }

}
