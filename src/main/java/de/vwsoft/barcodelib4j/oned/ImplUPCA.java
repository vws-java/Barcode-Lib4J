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
 * Implementation of UPC-A.
 */
public class ImplUPCA extends UPCEANFamily {

  private static final String BAR_LENGTH_PATTERN = "1".repeat(10) + "0".repeat(35) +
      "1".repeat(5) + "0".repeat(35) + "1".repeat(10) + "2".repeat(9 + 47);



  ImplUPCA() {
    try {
      setContent("123456789012", false, false);
    } catch (BarcodeException ex) {}
  }



  @Override String getBarLengthPattern() { return BAR_LENGTH_PATTERN; }
  @Override int getQuietZoneLeft() { return 9; }
  @Override int getQuietZoneRight() { return 9; }



  /**
   * Sets the UPC-A number to be encoded in the barcode.
   * <p>
   * The number must be either 12 (with check digit) or 11 (without check digit) in length.
   * <ul>
   *   <li>If the number contains 12 digits, the 12th digit is checked to see if it is a valid check
   *     digit according to the UPC-A standard. If the check digit is invalid, a
   *     {@code BarcodeException} is thrown.</li>
   *   <li>If the number contains 11 digits and {@code autoComplete} is set to {@code true},
   *     the method calculates the missing check digit and appends it to the specified number. If
   *     {@code autoComplete} is set to {@code false}, a {@code BarcodeException} is thrown.</li>
   * </ul>
   *
   * @param content                the UPC-A number to be encoded in the barcode
   * @param autoComplete           whether to automatically calculate and append a check digit if
   *                               it is missing
   * @param appendOptionalChecksum has no function, as UPC-A uses a fixed check digit
   *                               which is not optional
   * @throws BarcodeException      if the content is empty, contains non-numeric characters, is of
   *                               invalid length, or has an invalid check digit
   */
  @Override
  public void setContent(String content, boolean autoComplete, boolean appendOptionalChecksum)
      throws BarcodeException {
    validateNotEmpty(content);
    validateDigits(content);

    if (autoComplete && content.length() == 11) {
      content += calculateModulo10(content);
    } else {
      validateFixedLength(content, 12);
      validateModulo10(content);
    }

    myContent = content;
    invalidateDrawing(); // Reset cached bars to force recalculation on the next drawing
  }



  @Override
  void encodeLeftPart(StringBuilder sb) {
    for (int i=0; i!=6; i++)
      sb.append(encodeA(myContent.charAt(i) - 48));
  }



  @Override
  void encodeRightPart(StringBuilder sb) {
    for (int i=6; i!=12; i++)
      sb.append(encodeC(myContent.charAt(i) - 48));
  }



  @Override
  void prepareDrawing() {
    myNumberPart1 = myContent.substring(0, 1);
    myNumberPart2 = myContent.substring(1, 6);
    myNumberPart3 = myContent.substring(6, 11);
    myNumberPart4 = myContent.substring(11, 12);
    super.prepareDrawing();
  }

}
