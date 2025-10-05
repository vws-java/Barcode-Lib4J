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
 * Implementation of EAN-13 (also known as GTIN-13).
 */
public class ImplEAN13 extends UPCEANFamily {

  private static final String BAR_LENGTH_PATTERN = "1".repeat(3) + "0".repeat(42) +
      "1".repeat(5) + "0".repeat(42) + "1".repeat(3) + "2".repeat(7 + 47);

  private static final int[] CHARSET_PATTERN = { 0, 11, 13, 14, 19, 25, 28, 21, 22, 26 };



  ImplEAN13() {
    this("1234567890128");
  }



  ImplEAN13(String content) {
    try {
      setContent(content, false, false);
    } catch (BarcodeException ex) {}
  }



  @Override String getBarLengthPattern() { return BAR_LENGTH_PATTERN; }
  @Override int getQuietZoneLeft() { return 11; }
  @Override int getQuietZoneRight() { return 7; }



  /**
   * Sets the EAN-13 number to be encoded in the barcode.
   * <p>
   * The number must be either 13 (with check digit) or 12 (without check digit) in length.
   * <ul>
   *   <li>If the number contains 13 digits, the 13th digit is checked to see if it is a valid check
   *     digit according to the EAN-13 standard. If the check digit is invalid, a
   *     {@code BarcodeException} is thrown.</li>
   *   <li>If the number contains 12 digits and {@code autoComplete} is set to {@code true},
   *     the method calculates the missing check digit and appends it to the specified number. If
   *     {@code autoComplete} is set to {@code false}, a {@code BarcodeException} is thrown.</li>
   * </ul>
   *
   * @param content                the EAN-13 number to be encoded in the barcode
   * @param autoComplete           whether to automatically calculate and append a check digit if
   *                               it is missing
   * @param appendOptionalChecksum has no function, as EAN-13 uses a fixed check digit
   *                               which is not optional
   * @throws BarcodeException      if the content is empty, contains non-numeric characters, is of
   *                               invalid length, or has an invalid check digit
   */
  @Override
  public void setContent(String content, boolean autoComplete, boolean appendOptionalChecksum)
      throws BarcodeException {
    validateNotEmpty(content);
    validateDigits(content);

    if (autoComplete && content.length() == 12) {
      content += calculateModulo10(content);
    } else {
      validateFixedLength(content, 13);
      validateModulo10(content);
    }

    myContent = content;
    invalidateDrawing(); // Reset cached bars to force recalculation on the next drawing
  }



  @Override
  void encodeLeftPart(StringBuilder sb) {
    final int pattern = CHARSET_PATTERN[myContent.charAt(0) - 48];
    for (int i=1; i<7; i++)
      sb.append((pattern & (1 << (6 - i))) == 0 ?
          encodeA(myContent.charAt(i) - 48) : encodeB(myContent.charAt(i) - 48));
  }



  @Override
  void encodeRightPart(StringBuilder sb) {
    for (int i=7; i<13; i++)
      sb.append(encodeC(myContent.charAt(i) - 48));
  }



  @Override
  void prepareDrawing() {
    myNumberPart1 = myContent.substring(0, 1);
    myNumberPart2 = myContent.substring(1, 7);
    myNumberPart3 = myContent.substring(7, 13);
    super.prepareDrawing();
  }

}
