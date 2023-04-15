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


public class BarcodeEAN8 extends BarcodeUPCFamily {
  private static final String LENGTH_PATTERN = repeat('1', 3) + repeat('0', 28) + repeat('1', 5) +
      repeat('0', 28) + repeat('1', 3) + repeat('2', 7 + 47);


  //----
  public BarcodeEAN8(String number, boolean autoComplete) throws IllegalArgumentException {
    setNumber(number, autoComplete, false);
  }


  //----
  public BarcodeEAN8() throws IllegalArgumentException {
    this("0000000", true);
  }


  //---- implementation of abstract methods from 'BarcodeUPCFamily'
  protected String getBarLengthPattern() { return LENGTH_PATTERN; }
  protected double getPositionOfPart2() { return 3.5; }
  protected double getPositionOfPart3() { return 35.5; }
  protected int getQuietZoneLeft() { return 7; }
  protected int getQuietZoneRight() { return 7; }


  //----
  public void setNumber(String number, boolean autoComplete, boolean addOptChecksum)
      throws IllegalArgumentException {
    checkEmpty(number);
    checkInteger(number);

    if (autoComplete && number.length() == 7) {
      number += calculateModulo10(number);
    } else {
      checkFixedLength(number, 8);
      checkModulo10(number);
    }

    myPart2 = number.substring(0, 4);
    myPart3 = number.substring(4, 8);

    myNumber = number;
    reset();
  }


  //----
  protected void computeBarsOfPart2(StringBuilder sb) {
    for (int i=0; i<4; i++)
      sb.append(get7BitA(Integer.parseInt(myNumber.substring(i, i + 1))));
  }


  //----
  protected void computeBarsOfPart3(StringBuilder sb) {
    for (int i=4; i<8; i++)
      sb.append(get7BitC(Integer.parseInt(myNumber.substring(i, i + 1))));
  }

}
