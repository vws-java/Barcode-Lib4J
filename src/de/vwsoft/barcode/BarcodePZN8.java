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


public class BarcodePZN8 extends BarcodeCode39 {

  //----
  public BarcodePZN8(String number, boolean autoComplete) throws IllegalArgumentException {
    super(number, autoComplete, false);
  }


  //----
  public BarcodePZN8() throws IllegalArgumentException {
    super("12345678", false, false);
  }


  //----
  public boolean isOptionalChecksumSupported() {
    return false;
  }


  //----
  public boolean isOptionalChecksumUsed() {
    return false;
  }


  //----
  public void setOptionalChecksumVisible(boolean visible) {
  }


  //----
  public boolean isOptionalChecksumVisible() {
    return false;
  }


  //----
  public void setNumber(String number, boolean autoComplete, boolean addOptChecksum)
      throws IllegalArgumentException {
    checkEmpty(number);
    checkInteger(number);

    if (autoComplete && number.length() == 7) {
      int checksum = calculateChecksum(number);
      if (checksum == 10)
        throwIAE(number);
      number += checksum;
    } else {
      checkFixedLength(number, 8);
      if (number.charAt(7) - 48 != calculateChecksum(number))
        throwIAE(number);
    }

    myNumber = '-' + number;
    initHumanReadableNumber();
    reset();
  }


  //----
  private int calculateChecksum(String barcode) {
    int sum = 0;
    for (int i=6; i>=0; i--)
      sum += (i + 1) * (barcode.charAt(i) - 48);
    return sum % 11;
  }


  //----
  protected void initHumanReadableNumber() {
    myHumanReadableNumber = "PZN " + myNumber;
  }

}
