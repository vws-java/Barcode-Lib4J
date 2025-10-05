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
 * Implementation of UPC-E.
 */
public class ImplUPCE extends UPCEANFamily {

  private static final String BAR_LENGTH_PATTERN = "1".repeat(3) + "0".repeat(42) +
      "1".repeat(6) + "2".repeat(7 + 47);

  private static final int[] CHARSET_PATTERN = { 56, 52, 50, 49, 44, 38, 35, 42, 41, 37 };



  ImplUPCE() {
    try {
      setContent("12345670", false, false);
    } catch (BarcodeException ex) {}
  }



  @Override String getBarLengthPattern() { return BAR_LENGTH_PATTERN; }
  @Override int getQuietZoneLeft() { return 9; }
  @Override int getQuietZoneRight() { return 7; }



  /**
   * Sets the UPC-E number to be encoded in the barcode.
   * <p>
   * The first character of the UPC-E number must be either '0' or '1'.
   * <p>
   * The number must be either 8 (with check digit) or 7 (without check digit) in length.
   * <ul>
   *   <li>If the number contains 8 digits, the 8th digit is checked to see if it is a valid check
   *     digit according to the UPC-E standard. If the check digit is invalid, a
   *     {@code BarcodeException} is thrown.</li>
   *   <li>If the number contains 7 digits and {@code autoComplete} is set to {@code true},
   *     the method calculates the missing check digit and appends it to the specified number. If
   *     {@code autoComplete} is set to {@code false}, a {@code BarcodeException} is thrown.</li>
   * </ul>
   *
   * @param content                the UPC-E number to be encoded in the barcode
   * @param autoComplete           whether to automatically calculate and append a check digit if
   *                               it is missing
   * @param appendOptionalChecksum has no function, as UPC-E uses a fixed check digit
   *                               which is not optional
   * @throws BarcodeException      if the content is empty, contains non-numeric characters, is of
   *                               invalid length, or has an invalid check digit
   */
  @Override
  public void setContent(String content, boolean autoComplete, boolean appendOptionalChecksum)
      throws BarcodeException {
    validateNotEmpty(content);
    validateDigits(content);

    char firstDigit = content.charAt(0);
    if (firstDigit != '0' && firstDigit != '1')
      throw new BarcodeException(BarcodeException.CONTENT_INVALID,
          "Number must start with 0 or 1; Provided: %s",
          "Nummer muss mit 0 oder 1 beginnen; Aktuell: %s", firstDigit);

    if (autoComplete && content.length() == 7) {
      content += calculateModulo10(convertToUPCA(content));
    } else {
      validateFixedLength(content, 8);
      validateModulo10(convertToUPCA(content));
    }

    myContent = content;
    invalidateDrawing(); // Reset cached bars to force recalculation on the next drawing
  }



  private static String convertToUPCA(String upce) {
    StringBuilder sb = new StringBuilder(12);
    sb.append(upce.charAt(0));

    char lastChar = upce.charAt(6);
    if (lastChar < '3') // (0|1|2)
      sb.append(upce, 1, 3).append(lastChar).append("0000").append(upce, 3, 6);
    else if (lastChar == '3')
      sb.append(upce, 1, 4).append("00000").append(upce, 4, 6);
    else if (lastChar == '4')
      sb.append(upce, 1, 5).append("00000").append(upce.charAt(5));
    else
      sb.append(upce, 1, 6).append("0000").append(lastChar);

    if (upce.length() == 8)
      sb.append(upce.charAt(7)); // the check digit (if present) remains the same

    return sb.toString();
  }



  @Override
  CharSequence encode() {
    StringBuilder sb = new StringBuilder(119);

    sb.append("0".repeat(getQuietZoneLeft()));      // left quiet zone
    sb.append("101");                               // left guards
    int firstDigit = myContent.charAt(0) - 48;
    int pattern = CHARSET_PATTERN[myContent.charAt(7) - 48];
    for (int i=1; i<7; i++) {
      int digit = myContent.charAt(i) - 48;
      sb.append((pattern >> (6 - i) & 1) == firstDigit ? encodeA(digit) : encodeB(digit));
    }
    sb.append("010101");                            // right guards
    sb.append("0".repeat(getQuietZoneRight()));     // right quiet zone
    encodeAddOn(sb);                                // add-on, if present

    return sb;
  }



  @Override
  void prepareDrawing() {
    myNumberPart1 = myContent.substring(0, 1);
    myNumberPart2 = myContent.substring(1, 7);
    myNumberPart4 = myContent.substring(7, 8);
    super.prepareDrawing();
  }

}
