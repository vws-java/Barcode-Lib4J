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
 * Implementation of EAN-8 (also known as GTIN-8).
 */
public class ImplEAN8 extends LineageUPC {

  private static final String BAR_LENGTH_PATTERN = repeat('1', 3) + repeat('0', 28) +
      repeat('1', 5) + repeat('0', 28) + repeat('1', 3) + repeat('2', 7 + 47);



  ImplEAN8() {
    try {
      setContent("12345670", false, false);
    } catch (BarcodeException ex) {}
  }



  @Override String getBarLengthPattern() { return BAR_LENGTH_PATTERN; }
  @Override int getQuietZoneLeft() { return 7; }
  @Override int getQuietZoneRight() { return 7; }



  /**
   * Sets the EAN-8 number to be encoded in the barcode.
   * <p>
   * The number must be either 8 (with check digit) or 7 (without check digit) in length.
   * <ul>
   *   <li>If the number contains 8 digits, the 8th digit is checked to see if it is a valid check
   *     digit according to the EAN-8 standard. If the check digit is invalid, a
   *     {@code BarcodeException} is thrown.</li>
   *   <li>If the number contains 7 digits and {@code autoComplete} is set to {@code true},
   *     the method calculates the missing check digit and appends it to the specified number. If
   *     {@code autoComplete} is set to {@code false}, a {@code BarcodeException} is thrown.</li>
   * </ul>
   *
   * @param content                the EAN-8 number to be encoded in the barcode
   * @param autoComplete           whether to automatically calculate and append a check digit if
   *                               it is missing
   * @param appendOptionalChecksum has no function, as EAN-8 uses a fixed check digit
   *                               which is not optional
   * @throws BarcodeException      if the content is empty, contains non-numeric characters, is of
   *                               invalid length, or has an invalid check digit
   */
  @Override
  public void setContent(String content, boolean autoComplete, boolean appendOptionalChecksum)
      throws BarcodeException {
    validateNotEmpty(content);
    validateDigits(content);

    if (autoComplete && content.length() == 7) {
      content += calculateModulo10(content);
    } else {
      validateFixedLength(content, 8);
      validateModulo10(content);
    }

    myContent = content;
    myBars = null; // Reset bars to trigger recalculation next time drawing occurs
  }



  @Override
  void encodeLeftPart(StringBuilder sb) {
    for (int i=0; i<4; i++)
      sb.append(encodeA(myContent.charAt(i) - 48));
  }



  @Override
  void encodeRightPart(StringBuilder sb) {
    for (int i=4; i<8; i++)
      sb.append(encodeC(myContent.charAt(i) - 48));
  }



  @Override
  void prepareDrawing() {
    myNumberPart2 = myContent.substring(0, 4);
    myNumberPart3 = myContent.substring(4, 8);
    super.prepareDrawing();
  }

}
