/*
 * Copyright (c) 2025 Viktor Wedel
 *
 * Website EN: https://www.vw-software.com/java-barcode-library/
 * Website DE: https://www.vwsoft.de/barcode-library-for-java/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.vwsoft.barcodelib4j.oned;


/**
 * Implementation of Code 93 Extended ("Full ASCII").
 * <p>
 * Code 93 Extended is a variant of the {@link ImplCode93 Code 93} barcode format that supports
 * a broader range of characters, supporting all 128 ASCII characters.
 * <p>
 * <b>Barcode scanner configuration:</b> Please note that Code 93 Extended is not technically
 * different from standard Code 93, so a barcode scanner cannot automatically distinguish between
 * the two. To correctly read and interpret Code 93 Extended barcodes, the scanner must be
 * explicitly configured to interpret Code 93 as Extended.
 */
public class ImplCode93E extends ImplCode93 {

  private static final String[] CHARS_EXT = { "bU", "aA", "aB", "aC", "aD", "aE", "aF", "aG", "aH",
      "aI", "aJ", "aK", "aL", "aM", "aN", "aO", "aP", "aQ", "aR", "aS", "aT", "aU", "aV", "aW",
      "aX", "aY", "aZ", "bA", "bB", "bC", "bD", "bE", " ", "cA", "cB", "cC", "cD", "cE", "cF", "cG",
      "cH", "cI", "cJ", "cK", "cL", "-", ".", "cO", "0", "1", "2", "3", "4", "5", "6", "7", "8",
      "9", "cZ", "bF", "bG", "bH", "bI", "bJ", "bV", "A", "B", "C", "D", "E", "F", "G", "H", "I",
      "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "bK",
      "bL", "bM", "bN", "bO", "bW", "dA", "dB", "dC", "dD", "dE", "dF", "dG", "dH", "dI", "dJ",
      "dK", "dL", "dM", "dN", "dO", "dP", "dQ", "dR", "dS", "dT", "dU", "dV", "dW", "dX", "dY",
      "dZ", "bP", "bQ", "bR", "bS", "bT" };



  ImplCode93E() {
    super("Code 93 Ext");
  }



  /** @hidden */
  @Override
  public boolean supportsAutoCompletion() {
    return false;
  }



  /**
   * Sets the content to be encoded in the barcode.
   *
   * @param content                the content to be encoded in the Code 93 Extended barcode
   * @param autoComplete           has no function in this method implementation
   * @param appendOptionalChecksum has no function, as Code 93 Extended uses a mandatory checksum
   *                               which is not optional
   * @throws BarcodeException      if the content is empty or contains non-ASCII characters
   */
  @Override
  public void setContent(String content, boolean autoComplete, boolean appendOptionalChecksum)
      throws BarcodeException {
    validateNotEmpty(content);
    validateASCII(content);

    final int len = content.length();
    StringBuilder sb = new StringBuilder(len << 1);
    for (int i=0; i<len; i++)
      sb.append(CHARS_EXT[content.charAt(i)]);

    myContent = sb.toString();
    myText = content;
    myBars = null; // Reset bars to trigger recalculation next time drawing occurs
  }

}
