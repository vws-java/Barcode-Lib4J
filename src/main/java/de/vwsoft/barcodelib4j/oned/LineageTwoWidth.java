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
package de.vwsoft.barcodelib4j.oned;
import java.awt.Point;


/**
 * Abstract class used as the basis for barcode types characterized by bars with only two distinct
 * widths.
 * <p>
 * Such barcodes are commonly referred to as two-width barcodes.
 * <p>
 * The ratio between the two widths can typically be freely selected within the range from
 * 2:1 to 3:1. The primary objective is to optimize scanning efficiency. A higher ratio tends to
 * make it easier for the scanner to distinguish between wide and narrow bars. This feature thus
 * allows the barcode to adapt to different conditions and ensures readability resilience against
 * factors such as poor print quality, environmental influences or physical damage.
 */
public abstract class LineageTwoWidth extends Barcode {

  /** The default value is 2.5F, corresponding to a ratio of 2.5:1. */
  public static final float DEFAULT_RATIO = 2.5F;

  Point myRatio = ratioAsIntegerPair(DEFAULT_RATIO);



  // Hide default constructor from JavaDoc by making it package-private
  LineageTwoWidth() {}



  /** @hidden */
  @Override
  public final boolean supportsRatio() {
    return true;
  }



  @Override
  public void setRatio(float ratio) {
    myRatio = ratioAsIntegerPair(Math.max(2F, Math.min(3F, ratio)));
    myBars = null; // Reset bars to trigger recalculation next time drawing occurs
  }



  @Override
  public float getRatio() {
    return (float)myRatio.x / myRatio.y;
  }



  private static Point ratioAsIntegerPair(float ratio) {
    int narrLen = 10;
    int wideLen = Math.round(ratio * 10F);
    for (int i=10; i>1; i--) {
      if (narrLen % i == 0 && wideLen % i == 0) {
        narrLen /= i;
        wideLen /= i;
        break;
      }
    }
    return new Point(wideLen, narrLen);
  }

}
