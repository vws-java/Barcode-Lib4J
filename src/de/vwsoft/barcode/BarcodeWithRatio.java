/*
 * Copyright 2023 by Viktor Wedel, https://www.vwsoft.de/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.vwsoft.barcode;
import java.awt.*;


public abstract class BarcodeWithRatio extends Barcode {
  private static final float[] SUPPORTED_RATIOS =
      { 2.0F, 2.1F, 2.2F, 2.3F, 2.4F, 2.5F, 2.6F, 2.7F, 2.8F, 2.9F, 3.0F };
  private static final float DEFAULT_RATIO = SUPPORTED_RATIOS[5];

  protected transient Point myRatio = ratioAsIntegerPair(getDefaultRatio());


  //----
  public void setRatio(float ratio) {
    float[] sr = getSupportedRatios();
    if (ratio < sr[0])
      ratio = sr[0];
    else if (ratio > sr[sr.length - 1])
      ratio = sr[sr.length - 1];
    myRatio = ratioAsIntegerPair(ratio);
    reset();
  }


  //----
  public float getRatio() {
    return (float)myRatio.x / myRatio.y;
  }


  //----
  public float getDefaultRatio() {
    return DEFAULT_RATIO;
  }


  //----
  public float[] getSupportedRatios() {
    return SUPPORTED_RATIOS;
  }


  //----
  protected static Point ratioAsIntegerPair(float ratio) {
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
