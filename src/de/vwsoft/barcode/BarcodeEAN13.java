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


public class BarcodeEAN13 extends BarcodeUPCFamily {
  private static final int[] CHARSET_FLAGS = { 0, 11, 13, 14, 19, 25, 28, 21, 22, 26 };

  private static final String LENGTH_PATTERN = repeat('1', 3) + repeat('0', 42) + repeat('1', 5) +
      repeat('0', 42) + repeat('1', 3) + repeat('2', 7 + 47);


  //----
  public BarcodeEAN13(String number, boolean autoComplete) throws IllegalArgumentException {
    setNumber(number, autoComplete, false);
  }


  //----
  public BarcodeEAN13() throws IllegalArgumentException {
    this("000000000000", true);
  }


  //---- implementation of abstract methods from 'BarcodeUPCFamily'
  protected String getBarLengthPattern() { return LENGTH_PATTERN; }
  protected double getPositionOfPart2() { return 3.5; }
  protected double getPositionOfPart3() { return 49.5; }
  protected int getQuietZoneLeft() { return 11; }
  protected int getQuietZoneRight() { return 7; }


  //----
  public void setNumber(String number, boolean autoComplete, boolean addOptChecksum)
      throws IllegalArgumentException {
    checkEmpty(number);
    checkInteger(number);

    if (autoComplete && number.length() == 12) {
      number += calculateModulo10(number);
    } else {
      checkFixedLength(number, 13);
      checkModulo10(number);
    }

    myPart1 = number.substring(0, 1);
    myPart2 = number.substring(1, 7);
    myPart3 = number.substring(7, 13);

    myNumber = number;
    reset();
  }


  //----
  protected void computeBarsOfPart2(StringBuilder sb) {
    final int charsetFlags = CHARSET_FLAGS[myNumber.charAt(0) - 48];
    for (int i=1; i<7; i++)
      sb.append((charsetFlags & (1 << (6 - i))) == 0 ?
          get7BitA(myNumber.charAt(i) - 48) : get7BitB(myNumber.charAt(i) - 48));
  }


  //----
  protected void computeBarsOfPart3(StringBuilder sb) {
    for (int i=7; i<13; i++)
      sb.append(get7BitC(myNumber.charAt(i) - 48));
  }

}
