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
 * Immutable representation of a PDF417 symbol size.
 * <p>
 * PDF417 symbols are defined by their column count (1-30) and row count (3-90). The special value
 * {@code 0} can be used for either dimension to indicate automatic sizing based on the data to be
 * encoded.
 * <p>
 * Examples:
 * <pre>
 *   // Fixed size: 10 columns, 20 rows
 *   PDF417Size fixed = new PDF417Size(10, 20);
 *
 *   // Auto columns, fixed 20 rows
 *   PDF417Size autoCols = new PDF417Size(0, 20);
 *
 *   // Fully automatic sizing
 *   PDF417Size auto = new PDF417Size(0, 0);
 * </pre>
 */
public class PDF417Size {

  /** Minimum number of rows in a valid PDF417 symbol; value: 3. */
  public static final int ROWS_MIN = 3;

  /** Maximum number of rows in a valid PDF417 symbol; value: 90. */
  public static final int ROWS_MAX = 90;

  /** Minimum number of columns (code words) in a valid PDF417 symbol; value: 1. */
  public static final int COLS_MIN = 1;

  /** Maximum number of columns (code words) in a valid PDF417 symbol; value: 30. */
  public static final int COLS_MAX = 30;



  /** The number of columns, or {@code 0} for automatic sizing. */
  public final int cols;

  /** The number of rows, or {@code 0} for automatic sizing. */
  public final int rows;



  /**
   * Creates a new instance with the specified column and row counts.
   * <p>
   * Use {@code 0} for either dimension to enable automatic sizing for that dimension.
   *
   * @param cols the number of columns (1-30, or 0 for auto)
   * @param rows the number of rows (3-90, or 0 for auto)
   * @throws IllegalArgumentException  if column count or row count is outside the
   *                                   respective valid range
   */
  public PDF417Size(int cols, int rows) {
    if (cols != 0 && (cols < COLS_MIN || cols > COLS_MAX))
      throw new IllegalArgumentException("Invalid PDF417 column count: " + cols);
    if (rows != 0 && (rows < ROWS_MIN || rows > ROWS_MAX))
      throw new IllegalArgumentException("Invalid PDF417 row count: " + rows);
    this.cols = cols;
    this.rows = rows;
  }



  /**
   * {@return {@code true} if this size equals the specified object}
   * <p>
   * Two {@code PDF417Size} objects are equal if they have the same column and row counts.
   *
   * @param obj the object to compare with
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PDF417Size) {
      PDF417Size other = (PDF417Size)obj;
      return cols == other.cols && rows == other.rows;
    }
    return false;
  }



  /**
   * {@return the hash code calculated from the columns and rows values}
   */
  @Override
  public int hashCode() {
    return 31 * cols + rows;
  }



  /**
   * {@return a string representation of this instance}
   * <p>
   * This method is intended to be used only for debugging purposes.
   */
  @Override
  public String toString() {
    return getClass().getName() + "[cols=" + cols + ",rows=" + rows + "]";
  }

}
