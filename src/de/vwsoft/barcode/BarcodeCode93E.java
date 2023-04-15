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


public class BarcodeCode93E extends BarcodeCode93 {
  private static final String[] CHARS_EXT = { "bU", "aA", "aB", "aC", "aD", "aE", "aF", "aG", "aH",
      "aI", "aJ", "aK", "aL", "aM", "aN", "aO", "aP", "aQ", "aR", "aS", "aT", "aU", "aV", "aW",
      "aX", "aY", "aZ", "bA", "bB", "bC", "bD", "bE", " ", "cA", "cB", "cC", "cD", "cE", "cF", "cG",
      "cH", "cI", "cJ", "cK", "cL", "-", ".", "cO", "0", "1", "2", "3", "4", "5", "6", "7", "8",
      "9", "cZ", "bF", "bG", "bH", "bI", "bJ", "bV", "A", "B", "C", "D", "E", "F", "G", "H", "I",
      "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "bK",
      "bL", "bM", "bN", "bO", "bW", "dA", "dB", "dC", "dD", "dE", "dF", "dG", "dH", "dI", "dJ",
      "dK", "dL", "dM", "dN", "dO", "dP", "dQ", "dR", "dS", "dT", "dU", "dV", "dW", "dX", "dY",
      "dZ", "bP", "bQ", "bR", "bS", "bT" };


  //----
  public BarcodeCode93E(String number) throws IllegalArgumentException {
    super(number, false);
  }


  //----
  public BarcodeCode93E() throws IllegalArgumentException {
    super("Code 93 Ext", false);
  }


  //----
  public boolean isCompletionSupported() {
    return false;
  }


  //----
  public void setNumber(String number, boolean autoComplete, boolean addOptChecksum)
      throws IllegalArgumentException {
    checkEmpty(number);
    checkAscii(number);

    final int len = number.length();
    StringBuilder sb = new StringBuilder(len << 1);
    for (int i=0; i<len; i++)
      sb.append(CHARS_EXT[number.charAt(i)]);

    myNumber = sb.toString();
    myHumanReadableNumber = number;
    reset();
  }

}
