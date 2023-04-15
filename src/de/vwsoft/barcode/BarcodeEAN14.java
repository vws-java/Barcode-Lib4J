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


public class BarcodeEAN14 extends BarcodeEAN128 {

  //----
  public BarcodeEAN14() throws IllegalArgumentException {
    super("12345678901231");
  }


  //----
  public boolean isCompletionSupported() {
    return true;
  }


  //----
  public void setNumber(String number, boolean autoComplete, boolean addOptChecksum)
      throws IllegalArgumentException {

    if (autoComplete && number.length() == 13) {
      checkInteger(number);
      number += calculateModulo10(number);
    }

    super.setNumber("01" + number, false, false);
  }

}

