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


public class Barcode2Of5 extends BarcodeWithRatio {
  private static final int[] BARS = { 6, 17, 9, 24, 5, 20, 12, 3, 18, 10 };

  private boolean myIsOptionalChecksumUsed;
  private boolean myIsOptionalChecksumVisible;
  private int myOptionalChecksum;


  //----
  public Barcode2Of5(String number, boolean autoComplete, boolean addOptChecksum)
      throws IllegalArgumentException {
    setNumber(number, autoComplete, addOptChecksum);
  }


  //----
  public Barcode2Of5() throws IllegalArgumentException {
    this("1234567890", true, false);
  }


  //----
  public boolean isOptionalChecksumSupported() {
    return true;
  }


  //----
  public boolean isOptionalChecksumUsed() {
    return myIsOptionalChecksumUsed;
  }


  //----
  public void setOptionalChecksumVisible(boolean visible) {
    myIsOptionalChecksumVisible = visible;
    initHumanReadableNumber();
  }


  //----
  public boolean isOptionalChecksumVisible() {
    return myIsOptionalChecksumVisible;
  }


  //----
  protected int getQuietZoneLeft() {
    return 15;
  }


  //----
  protected int getQuietZoneRight() {
    return 15;
  }


  //----
  protected String computeBars() {
    String number = myNumber;
    if (myIsOptionalChecksumUsed)
      number += myOptionalChecksum;
    final int numberLength = number.length();

    final String[] bars   = { repeat('1', myRatio.y), repeat('1', myRatio.x) };
    final String[] spaces = { repeat('0', myRatio.y), repeat('0', myRatio.x) };

    final int leftQuietZone = myIsQuietZonesIncluded ? getQuietZoneLeft() * myRatio.y : 0;
    final int rightQuietZone = myIsQuietZonesIncluded ? getQuietZoneRight() * myRatio.y : 0;

    StringBuilder sb = new StringBuilder(myRatio.y * 6 + leftQuietZone + rightQuietZone +
        myRatio.x + (2 * myRatio.x + 3 * myRatio.y) * numberLength);

    sb.append(repeat('0', leftQuietZone));                                   // left quiet zone
    sb.append(bars[0]).append(spaces[0]).append(bars[0]).append(spaces[0]);  // start sign
    for (int i=0; i<numberLength; i+=2) {
      final int firstPartOfPair = BARS[(int)number.charAt(i) - 48];
      final int secondPartOfPair = BARS[(int)number.charAt(i + 1) - 48];
      for (int j=4; j>=0; j--) {
        sb.append(bars[(firstPartOfPair >> j) & 1]);
        sb.append(spaces[(secondPartOfPair >> j) & 1]);
      }
    }
    sb.append(bars[1]).append(spaces[0]).append(bars[0]);                    // stop sign
    sb.append(repeat('0', rightQuietZone));                                  // right quiet zone

    return sb.toString();
  }


  //----
  protected double calculateModuleFactor() {
    int len = myNumber.length();
    if (myIsOptionalChecksumUsed)
      len++;
    double ratio = (double)myRatio.x / myRatio.y;
    double result = len * (2 * ratio + 3) + ratio + 6;
    if (myIsQuietZonesIncluded)
      result += getQuietZoneLeft() + getQuietZoneRight();
    return result;
  }


  //----
  public void setNumber(String number, boolean autoComplete, boolean addOptChecksum)
      throws IllegalArgumentException {
    checkEmpty(number);
    checkInteger(number);

    if ( (number.length() % 2 != 0) ^ addOptChecksum ) {
      if (autoComplete)
        number = "0" + number;
      else
        throwNumberLengthNotEven(number.length());
    }

    myIsOptionalChecksumUsed = addOptChecksum;
    myNumber = number;
    if (myIsOptionalChecksumUsed)
      initOptionalChecksum();
    initHumanReadableNumber();
    reset();
  }


  //----
  private void initOptionalChecksum() {
    int weight = 3;
    int sum = 0;
    for (int i=myNumber.length()-1; i>=0; i--) {
      sum += Integer.parseInt(myNumber.substring(i, i + 1)) * weight;
      weight = weight == 3 ? 1 : 3;
    }
    sum = 10 - (sum % 10);
    myOptionalChecksum = sum == 10 ? 0 : sum;
  }


  //----
  private void initHumanReadableNumber() {
    myHumanReadableNumber = myNumber;
    if (myIsOptionalChecksumUsed && myIsOptionalChecksumVisible)
      myHumanReadableNumber += myOptionalChecksum;
  }

}
