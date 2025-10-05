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
 * Enumeration of DataMatrix symbol shape preferences.
 * <p>
 * DataMatrix symbols can be square or rectangular. The special constant {@link #AUTO}
 * allows automatic shape selection based on the data.
 * <p>
 * The unique integer IDs (0-2) assigned to the constants can be used for efficient
 * storage in files or databases. The IDs are small enough to be safely cast to byte
 * if needed. See {@link #getID()} and {@link #valueOf(int id)}.
 */
public enum DataMatrixShape {

  /** Select optimal shape based on data */  AUTO      ( 0 ),
  /** Force square symbol shape */           SQUARE    ( 1 ),
  /** Force rectangular symbol shape */      RECTANGLE ( 2 );

  private final int myID;



  private DataMatrixShape(int id) {
    myID = id;
  }



  private static final DataMatrixShape[] cachedValues = values();
  /**
   * Returns the enum constant of this class associated with the specified integer ID.
   *
   * @param id the ID of the enum constant to be returned (0-2)
   * @return the enum constant associated with the specified ID
   * @throws IllegalArgumentException if this enum class has no constant associated
   *                                  with the specified ID
   */
  public static DataMatrixShape valueOf(int id) {
    if (id < 0 || id > 2)
      throw new IllegalArgumentException("Invalid DataMatrix shape ID: " + id);
    return cachedValues[id];
  }



  /**
   * {@return the integer ID associated with this shape preference}
   * <p>
   * Returns {@code 0} for {@link #AUTO}, {@code 1} for {@link #SQUARE},
   * or {@code 2} for {@link #RECTANGLE}.
   *
   * @see #valueOf(int id)
   */
  public int getID() {
    return myID;
  }

}
