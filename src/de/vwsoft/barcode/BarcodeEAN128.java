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


public class BarcodeEAN128 extends BarcodeCode128 {

  //----
  public BarcodeEAN128(String number) throws IllegalArgumentException {
    super(CODESET_ALL, number);
  }


  //----
  public BarcodeEAN128() throws IllegalArgumentException {
    super(CODESET_ALL, "(01)01234567890128");
  }


  //----
  public void setNumber(String number, boolean autoComplete, boolean addOptChecksum)
      throws IllegalArgumentException {
    GS1 gs1 = new GS1(number, FNC1_CHAR);
    myNumber = gs1.getNumber();
    myHumanReadableNumber = gs1.getHumanReadableNumber();
    reset();
  }

}
