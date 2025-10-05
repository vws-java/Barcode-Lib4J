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
package de.vwsoft.barcodelib4j.image;


/**
 * Enumeration of supported image formats (PDF, EPS, SVG, PNG, BMP, JPG).
 * <p>
 * Each format has a unique integer ID which can be used for efficient storage in a file or
 * database. The IDs are small positive integers (1-6) that can be safely cast to byte if needed.
 * See {@link #getID()} and {@link #valueOf(int id)}.
 */
public enum ImageFormat {

  // Vector
  /** Portable Document Format (PDF) */   PDF (1),
  /** Encapsulated PostScript (EPS) */    EPS (2),
  /** Scalable Vector Graphics (SVG) */   SVG (3),
  // Raster
  /** Portable Network Graphics (PNG) */  PNG (4),
  /** Bitmap (BMP) */                     BMP (5),
  /** JPEG/JPG */                         JPG (6);

  private final int myID;



  private ImageFormat(int id) {
    myID = id;
  }



  // This static cache helps to avoid repeated array creation that occurs internally in 'values()'.
  // The constant is used only by the 'valueOf(int)' method and is therefore declared next to it.
  private static final ImageFormat[] cachedValues = values();
  /**
   * Returns the enum constant of this class associated with the specified integer ID.
   *
   * @param id the ID of the enum constant to be returned
   * @return the enum constant associated with the specified ID
   * @throws IllegalArgumentException  if this enum class has no constant associated
   *                                   with the specified ID
   */
  public static ImageFormat valueOf(int id) {
    if (id < 1 || id > 6)
      throw new IllegalArgumentException("Invalid image format ID: " + id);
    return cachedValues[id - 1];
  }



  /**
   * {@return the integer ID associated with this image format}
   *
   * @see #valueOf(int id)
   */
  public int getID() {
    return myID;
  }



  /**
   * Checks whether this image format is a raster format.
   *
   * @return {@code true} if this format is a raster format (PNG, BMP, or JPG),
   *         {@code false} if it is a vector format (PDF, EPS, or SVG)
   */
  public boolean isRasterFormat() {
    return this == PNG || this == BMP || this == JPG;
  }



  /**
   * Checks whether this image format supports transparent backgrounds.
   *
   * @return {@code true} if this format supports transparent backgrounds (PDF, EPS, SVG, or PNG),
   *         {@code false} otherwise (BMP or JPG)
   */
  public boolean supportsTransparency() {
    return this == PDF || this == EPS || this == SVG || this == PNG;
  }



  /**
   * Checks whether this image format supports CMYK color model.
   * <p>
   * <b>Note:</b> RGB color model is supported by all included formats.
   *
   * @return {@code true} if this format supports CMYK (PDF or EPS),
   *         {@code false} otherwise (SVG, PNG, BMP, or JPG)
   */
  public boolean supportsCMYK() {
    return this == PDF || this == EPS;
  }

}
