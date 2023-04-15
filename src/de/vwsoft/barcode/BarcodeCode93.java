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


public class BarcodeCode93 extends Barcode {
  protected static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. $/+%abcd*";
  private static final int[] BARS = { 276, 328, 324, 322, 296, 292, 290, 336, 274, 266, 424, 420,
      418, 404, 402, 394, 360, 356, 354, 308, 282, 344, 332, 326, 300, 278, 436, 434, 428, 422, 406,
      410, 364, 358, 310, 314, 302, 468, 466, 458, 366, 374, 430, 294, 474, 470, 306, 350 };


  //----
  public BarcodeCode93(String number, boolean autoComplete) throws IllegalArgumentException {
    setNumber(number, autoComplete, false);
  }


  //----
  public BarcodeCode93() throws IllegalArgumentException {
    this("CODE 93", true);
  }


  //----
  protected String computeBars() {
    StringBuilder sb = new StringBuilder(myNumber.length() + 1 + 1 + 2);
    sb.append('*').append(myNumber).append(calculateChecksum(myNumber)).append('*');

    String number = sb.toString();
    final int len = number.length();

    final int leftQuietZone = myIsQuietZonesIncluded ? getQuietZoneLeft() : 0;
    final int rightQuietZone = myIsQuietZonesIncluded ? getQuietZoneRight() : 0;
    sb = new StringBuilder(9 * len + 1 + leftQuietZone + rightQuietZone);

    sb.append(repeat('0', leftQuietZone));
    for (int i=0; i<len; i++)
      sb.append(Integer.toBinaryString(BARS[CHARS.indexOf(number.charAt(i))]));
    sb.append('1'); // "termination bar"
    sb.append(repeat('0', rightQuietZone));

    return sb.toString();
  }


  //----
  public void setNumber(String number, boolean autoComplete, boolean addOptChecksum)
      throws IllegalArgumentException {
    checkEmpty(number);

    if (autoComplete)
      number = number.toUpperCase();

    final String validChars = CHARS.substring(0, 43);
    for (int i=number.length()-1; i>=0; i--)
      if (validChars.indexOf(number.charAt(i)) < 0)
        throwIAE(number);

    myNumber = number;
    reset();
  }


  //----
  private String calculateChecksum(String barcode) {
    // calculate 'check character C'
    int sum = 0, count = 0;
    for (int i=barcode.length()-1; i>=0; i--) {
      sum += (++count) * CHARS.indexOf(barcode.charAt(i));
      if (count == 20) count = 0;
    }
    char checkCharacterC = CHARS.charAt(sum % 47);

    // calculate 'check character K'
    sum = CHARS.indexOf(checkCharacterC);
    count = 1;
    for (int i=barcode.length()-1; i>=0; i--) {
      sum += (++count) * CHARS.indexOf(barcode.charAt(i));
      if (count == 15) count = 0;
    }
    char checkCharacterK = CHARS.charAt(sum % 47);

    return checkCharacterC + "" + checkCharacterK;
  }

}
