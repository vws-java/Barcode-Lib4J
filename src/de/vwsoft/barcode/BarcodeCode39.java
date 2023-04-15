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


public class BarcodeCode39 extends BarcodeWithRatio {
  private static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. $/+%*";
  private static final int[] BARS = { 52, 289, 97, 352, 49, 304, 112, 37, 292, 100, 265, 73, 328,
      25, 280, 88, 13, 268, 76, 28, 259, 67, 322, 19, 274, 82, 7, 262, 70, 22, 385, 193, 448, 145,
     400, 208, 133, 388, 196, 168, 162, 138, 42, 148 };

  protected boolean myIsOptionalChecksumUsed;
  protected boolean myIsOptionalChecksumVisible;
  protected char myOptionalChecksum;


  //----
  public BarcodeCode39(String number, boolean autoComplete, boolean addOptChecksum)
      throws IllegalArgumentException {
    setNumber(number, autoComplete, addOptChecksum);
  }


  //----
  public BarcodeCode39() throws IllegalArgumentException {
    this("CODE 39", true, false);
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
  protected String computeBars() {
    String number = myNumber;
    if (myIsOptionalChecksumUsed)
      number += myOptionalChecksum;
    number = '*' + number + '*';

    final String[] narr = { repeat('1', myRatio.y), repeat('0', myRatio.y) };
    final String[] wide = { repeat('1', myRatio.x), repeat('0', myRatio.x) };

    final int leftQuietZone = myIsQuietZonesIncluded ? getQuietZoneLeft() * myRatio.y : 0;
    final int rightQuietZone = myIsQuietZonesIncluded ? getQuietZoneRight() * myRatio.y : 0;

    StringBuilder sb = new StringBuilder((3 * myRatio.x + (6 + 1) * myRatio.y) * number.length() +
        leftQuietZone + rightQuietZone);

    sb.append(repeat('0', leftQuietZone));
    final int k = number.length() - 1;
    for (int i=0; i<=k; i++) {
      int barMask = BARS[CHARS.indexOf(number.charAt(i))];
      for (int j=0; j<9; j++)
        sb.append((barMask & (1 << (8 - j))) == 0 ? narr[j % 2] : wide[j % 2]);
      if (i < k)
        sb.append(narr[1]);
    }
    sb.append(repeat('0', rightQuietZone));

    return sb.toString();
  }


  //----
  protected double calculateModuleFactor() {
    int len = myNumber.length() + (myIsOptionalChecksumUsed ? 3 : 2);
    double result = 7 * len + 3 * len * (double)myRatio.x / myRatio.y - 1;
    if (myIsQuietZonesIncluded)
      result += getQuietZoneLeft() + getQuietZoneRight();
    return result;
  }


  //----
  public void setNumber(String number, boolean autoComplete, boolean addOptChecksum)
      throws IllegalArgumentException {
    checkEmpty(number);

    if (autoComplete)
      number = number.toUpperCase();

    for (int i=number.length()-1; i>=0; i--) {
      char c = number.charAt(i);
      if (c == '*' || CHARS.indexOf(c) < 0)
        throwIAE(number);
    }

    myIsOptionalChecksumUsed = addOptChecksum;
    myNumber = number;
    if (myIsOptionalChecksumUsed)
      initOptionalChecksum();
    initHumanReadableNumber();
    reset();
  }


  //----
  protected void initOptionalChecksum() {
    int sum = 0;
    for (int i=myNumber.length()-1; i>=0; i--)
      sum += CHARS.indexOf(myNumber.charAt(i));
    myOptionalChecksum = CHARS.charAt(sum % 43);
  }


  //----
  protected void initHumanReadableNumber() {
    myHumanReadableNumber = myNumber;
    if (myIsOptionalChecksumUsed && myIsOptionalChecksumVisible)
      myHumanReadableNumber += myOptionalChecksum;
  }

}
