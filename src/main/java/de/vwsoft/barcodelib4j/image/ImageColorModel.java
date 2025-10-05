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
 * Enumeration of supported color models (RGB, CMYK).
 * <p>
 * Defines the color models that can be used when exporting barcode images to vector formats
 * (PDF and EPS). RGB is typically used for screen display and digital distribution, while CMYK
 * is preferred for professional printing workflows.
 * <p>
 * Each color model has a unique integer ID which can be used for efficient storage in a file or
 * database. The IDs are small positive integers (1-2) that can be safely cast to byte if needed.
 * See {@link #getID()} and {@link #valueOf(int id)}.
 */
public enum ImageColorModel {

  /** Red-Green-Blue color model. */
  RGB (1),

  /** Cyan-Magenta-Yellow-Key color model. */
  CMYK (2);

  private final int myID;



  private ImageColorModel(int id) {
    myID = id;
  }



  /**
   * Returns the enum constant of this class associated with the specified integer ID.
   *
   * @param id the ID of the enum constant to be returned
   * @return the enum constant associated with the specified ID
   * @throws IllegalArgumentException  if this enum class has no constant associated
   *                                   with the specified ID
   */
  public static ImageColorModel valueOf(int id) {
    if (id < 1 || id > 2)
      throw new IllegalArgumentException("Invalid color model ID: " + id);
    return id == 1 ? RGB : CMYK;
  }



  /**
   * {@return the integer ID associated with this color model}
   *
   * @see #valueOf(int id)
   */
  public int getID() {
    return myID;
  }

}
