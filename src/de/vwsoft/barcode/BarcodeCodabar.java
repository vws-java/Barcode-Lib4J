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


public class BarcodeCodabar extends BarcodeWithRatio {
  private static final String CHARS = "0123456789-$:/.+ABCD";
  private static final int BARS[] = { 3,6,9,96,18,66,33,36,48,72,12,24,69,81,84,21,26,41,11,14 };


  //----
  public BarcodeCodabar(String number, boolean autoComplete) throws IllegalArgumentException {
    setNumber(number, autoComplete, false);
  }


  //----
  public BarcodeCodabar() throws IllegalArgumentException {
    this("1234567890", true);
  }


  //----
  protected String computeBars() {
    final String[] bars   = { repeat('1', myRatio.y), repeat('1', myRatio.x) };
    final String[] spaces = { repeat('0', myRatio.y), repeat('0', myRatio.x) };

    final int leftQuietZone = myIsQuietZonesIncluded ? getQuietZoneLeft() * myRatio.y : 0;
    final int rightQuietZone = myIsQuietZonesIncluded ? getQuietZoneRight() * myRatio.y : 0;

    final int len = myNumber.length();

    StringBuilder sb = new StringBuilder(len * (3 * myRatio.x + 5 * myRatio.y) +
        leftQuietZone + rightQuietZone);

    sb.append(repeat('0', leftQuietZone));
    for (int i=0; i<len; i++) {
      final int n = BARS[CHARS.indexOf(myNumber.charAt(i))];
      for (int j=6; j>0; ) {
        sb.append(    bars[(n >> (j--)) & 1]  );
        sb.append(  spaces[(n >> (j--)) & 1]  );
      }
      sb.append(bars[n & 1]);
      if (i < len - 1)
        sb.append(spaces[0]);
    }
    sb.append(repeat('0', rightQuietZone));

    return sb.toString();
  }


  //----
  protected double calculateModuleFactor() {
    String number = myNumber;

    int narrowBarCount = number.length() - 1;
    int wideBarCount = 0;
    for (int i=narrowBarCount; i>=0; i--) {
      int n = BARS[CHARS.indexOf(number.charAt(i))];
      for (int j=6; j>=0; j--) {
        if (((n >> j) & 1) == 0)
          narrowBarCount++;
        else
          wideBarCount++;
      }
    }

    double result = narrowBarCount + wideBarCount * (double)myRatio.x / myRatio.y;
    if (myIsQuietZonesIncluded)
      result += getQuietZoneLeft() + getQuietZoneRight();
    return result;
  }


  //----
  public void setNumber(String number, boolean autoComplete, boolean addOptChecksum)
      throws IllegalArgumentException {
    checkEmpty(number);

    // start sign
    char startChar = Character.toUpperCase(number.charAt(0));
    if (isStartOrStopChar(startChar))
      number = startChar + number.substring(1);
    else if (autoComplete)
      number = 'A' + number;
    else
      throwIAE(number);

    // stop sign
    char stopChar = Character.toUpperCase(number.charAt(number.length() - 1));
    if (isStartOrStopChar(stopChar))
      number = number.substring(0, number.length() - 1) + stopChar;
    else if (autoComplete)
      number = number + 'A';
    else
      throwIAE(number);

    // check if all characters (besides the start and the stop character) are valid
    String validChars = CHARS.substring(0, 16);
    for (int i=number.length()-2; i!=0; i--) {
      char c = number.charAt(i);
      if ((validChars.indexOf(c) < 0))
        throwIAE(number);
    }

    myNumber = number;
    reset();
  }


  //----
  private boolean isStartOrStopChar(char c) {
    return c == 'A' || c == 'B' || c == 'C' || c == 'D';
  }

}
