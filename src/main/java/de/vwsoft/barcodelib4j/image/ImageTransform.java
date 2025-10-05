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
 * Enumeration of supported image transformation types (rotations and mirroring).
 * <p>
 * Each transformation type has a unique integer ID which can be used for efficient storage in a
 * file or database. The IDs are small non-negative integers (0-7) that can be safely cast to byte
 * if needed. See {@link #getID()} and {@link #valueOf(int id)}.
 */
public enum ImageTransform {

  // Standard rotations
  /** No rotation */                                          ROTATE_0        (0),
  /** 90 degrees clockwise rotation */                        ROTATE_90       (1),
  /** 180 degrees rotation */                                 ROTATE_180      (2),
  /** 270 degrees clockwise rotation */                       ROTATE_270      (3),
  // Rotations with horizontal flip
  /** Horizontal flip (no rotation) */                        ROTATE_0_FLIP   (4),
  /** 90 degrees clockwise rotation with horizontal flip */   ROTATE_90_FLIP  (5),
  /** 180 degrees rotation with horizontal flip */            ROTATE_180_FLIP (6),
  /** 270 degrees clockwise rotation with horizontal flip */  ROTATE_270_FLIP (7);

  private final int myID;



  private ImageTransform(int id) {
    myID = id;
  }



  // This static cache helps to avoid repeated array creation that occurs internally in 'values()'.
  // The constant is used only by the 'valueOf(int)' method and is therefore declared next to it.
  private static final ImageTransform[] cachedValues = values();
  /**
   * Returns the enum constant of this class associated with the specified integer ID.
   *
   * @param id the ID of the enum constant to be returned
   * @return the enum constant associated with the specified ID
   * @throws IllegalArgumentException if this enum class has no constant associated
   *                                  with the specified ID
   */
  public static ImageTransform valueOf(int id) {
    if (id < 0 || id > 7)
      throw new IllegalArgumentException("Invalid image transform ID: " + id);
    return cachedValues[id];
  }



  /**
   * {@return the integer ID associated with this image transform}
   *
   * @see #valueOf(int id)
   */
  public int getID() {
    return myID;
  }



  /**
   * Checks whether this transformation type represents a flat rotation (0&deg; or 180&deg;).
   * <p>
   * This is a convenience method that can be used to determine whether the horizontal or vertical
   * resolution of the output medium is relevant in a particular case.
   * <p>
   * For 1D barcodes, which mainly consist of vertical bars, only one of the two resolutions is
   * relevant. For example, when creating a 1D barcode at a 90&deg; or 270&deg; angle, the vertical
   * resolution is crucial as the bar widths must be adjusted to it. Similarly, at a 0&deg; or
   * 180&deg; angle, the horizontal resolution is important.
   * <p>
   * (However, for 2D codes, if the output medium has differing horizontal and vertical resolutions,
   * regardless of the transformation used, always use the smaller resolution. For example, in a
   * setting of 300x600 DPI, use 300 DPI as the relevant resolution.)
   *
   * @return {@code true} if this transformation type represents a 0&deg; or 180&deg; rotation
   *         (ROTATE_0, ROTATE_180, ROTATE_0_FLIP, or ROTATE_180_FLIP),
   *         {@code false} otherwise (90&deg; or 270&deg; rotations)
   */
  public boolean isFlat() {
    return this == ROTATE_0      || this == ROTATE_180
        || this == ROTATE_0_FLIP || this == ROTATE_180_FLIP;
  }



  /**
   * Checks whether this transformation type includes a horizontal flip (mirroring).
   *
   * @return {@code true} if this transformation includes a flip (ROTATE_0_FLIP,
   *         ROTATE_90_FLIP, ROTATE_180_FLIP, or ROTATE_270_FLIP),
   *         {@code false} otherwise
   */
  public boolean hasFlip() {
    return this == ROTATE_0_FLIP   || this == ROTATE_90_FLIP
        || this == ROTATE_180_FLIP || this == ROTATE_270_FLIP;
  }

}
