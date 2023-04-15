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


public class BarcodeCode11 extends BarcodeWithRatio {
  private static final String CHARS = "0123456789-";
  private static final int[] BARS = { 1, 17, 9, 24, 5, 20, 12, 3, 18, 16, 4 };

  private boolean myIsOptionalChecksumUsed;
  private String myOptionalChecksum;
  private boolean myIsOptionalChecksumVisible;


  //----
  public BarcodeCode11(String number, boolean addOptChecksum) throws IllegalArgumentException {
    setNumber(number, false, addOptChecksum);
  }


  //----
  public BarcodeCode11() throws IllegalArgumentException {
    this("12345678", true);
  }


  //----
  public boolean isCompletionSupported() {
    return false;
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
    final String number = myIsOptionalChecksumUsed ? myNumber + myOptionalChecksum : myNumber;
    final int len = number.length();

    final String[] bars   = { repeat('1', myRatio.y), repeat('1', myRatio.x) };
    final String[] spaces = { repeat('0', myRatio.y), repeat('0', myRatio.x) };

    final String startAndStop = bars[0] + spaces[0] + bars[1] + spaces[1] + bars[0];

    final int leftQuietZone = myIsQuietZonesIncluded ? getQuietZoneLeft() * myRatio.y : 0;
    final int rightQuietZone = myIsQuietZonesIncluded ? getQuietZoneRight() * myRatio.y : 0;

    StringBuilder sb = new StringBuilder(
        myRatio.x * 4 + myRatio.y * 6 +
        len * ((myRatio.x << 1) + (myRatio.y << 2)) +
        myRatio.y +
        leftQuietZone + rightQuietZone);

    sb.append(repeat('0', leftQuietZone));
    sb.append(startAndStop);
    sb.append(spaces[0]); // first intercharacter space
    for (int i=0; i<len; i++) {
      final int n = BARS[CHARS.indexOf(number.charAt(i))];
      sb.append(    bars[(n >> 4) & 1]  );  // bar
      sb.append(  spaces[(n >> 3) & 1]  );  // space
      sb.append(    bars[(n >> 2) & 1]  );  // bar
      sb.append(  spaces[(n >> 1) & 1]  );  // space
      sb.append(    bars[(n     ) & 1]  );  // bar
      sb.append(  spaces[0]             );  // intercharacter space
    }
    sb.append(startAndStop);
    sb.append(repeat('0', rightQuietZone));

    return sb.toString();
  }


  //----
  protected double calculateModuleFactor() {
    String number = myNumber;
    if (myIsOptionalChecksumUsed)
      number += myOptionalChecksum;

    int narrowBarCount = 3 + (number.length() + 1) + 3;
    int wideBarCount = 2 + 2;
    for (int i=number.length()-1; i>=0; i--) {
      int n = BARS[CHARS.indexOf(number.charAt(i))];
      for (int j=4; j>=0; j--) {
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

    for (int i=number.length()-1; i>=0; i--)
      if (CHARS.indexOf(number.charAt(i)) < 0)
        throwIAE(number);

    myIsOptionalChecksumUsed = addOptChecksum;
    myNumber = number;
    if (myIsOptionalChecksumUsed)
      initOptionalChecksum();
    initHumanReadableNumber();
    reset();
  }


  //----
  private void initOptionalChecksum() {
    // berechne 'check character C'
    int sum = 0, count = 0;
    for (int i=myNumber.length()-1; i>=0; i--) {
      sum += (++count) * CHARS.indexOf(myNumber.charAt(i));
      if (count == 10)
        count = 0;
    }
    char checkCharacterC = CHARS.charAt(sum % 11);

    // berechne 'check character K'
    sum = CHARS.indexOf(checkCharacterC);
    count = 1;
    for (int i=myNumber.length()-1; i>=0; i--) {
      sum += (++count) * CHARS.indexOf(myNumber.charAt(i));
      if (count == 9)
        count = 0;
    }
    char checkCharacterK = CHARS.charAt(sum % 11);

    myOptionalChecksum = checkCharacterC + "" + checkCharacterK;
  }


  //----
  private void initHumanReadableNumber() {
    myHumanReadableNumber = myNumber;
    if (myIsOptionalChecksumUsed && myIsOptionalChecksumVisible)
      myHumanReadableNumber += myOptionalChecksum;
  }

}
