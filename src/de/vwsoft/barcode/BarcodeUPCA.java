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


public class BarcodeUPCA extends BarcodeUPCFamily {
  private static final String LENGTH_PATTERN = repeat('1', 10) + repeat('0', 35) + repeat('1', 5) +
      repeat('0', 35) + repeat('1', 10) + repeat('2', 9 + 47);


  //----
  public BarcodeUPCA(String number, boolean autoComplete) throws IllegalArgumentException {
    setNumber(number, autoComplete, false);
  }


  //----
  public BarcodeUPCA() throws IllegalArgumentException {
    this("00000000000", true);
  }


  //---- implementation of abstract methods from 'BarcodeUPCFamily'
  protected String getBarLengthPattern() { return LENGTH_PATTERN; }
  protected double getPositionOfPart2() { return 10.5; }
  protected double getPositionOfPart3() { return 49.5; }
  protected int getQuietZoneLeft() { return 9; }
  protected int getQuietZoneRight() { return 9; }


  //----
  public void setNumber(String number, boolean autoComplete, boolean addOptChecksum)
      throws IllegalArgumentException {
    checkEmpty(number);
    checkInteger(number);

    if (autoComplete && number.length() == 11) {
      number += calculateModulo10(number);
    } else {
      checkFixedLength(number, 12);
      checkModulo10(number);
    }

    myPart1 = number.substring(0, 1);
    myPart2 = number.substring(1, 6);
    myPart3 = number.substring(6, 11);
    myPart4 = number.substring(11, 12);

    myNumber = number;
    reset();
  }


  //----
  protected void computeBarsOfPart2(StringBuilder sb) {
    for (int i=0; i!=6; i++)
      sb.append(get7BitA(myNumber.charAt(i) - 48));
  }


  //----
  protected void computeBarsOfPart3(StringBuilder sb) {
    for (int i=6; i!=12; i++)
      sb.append(get7BitC(myNumber.charAt(i) - 48));
  }

}
