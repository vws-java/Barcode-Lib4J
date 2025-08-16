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
 * Implementation of EAN-14 (also known as GTIN-14) using GS1-128.
 * <p>
 * This convenience class generates a GS1-128 barcode that contains a 14-digit GTIN as the sole data
 * element. It automatically includes the Application Identifier prefix '01' in both the barcode and
 * the human readable text line, allowing you to specify only the GTIN number itself.
 * <p>
 * <b>Note:</b> Encoding the same GTIN number with this class and with the
 * {@link ImplEAN128 GS1-128} implementation will produce exactly the same barcode.
 */
public class ImplEAN14 extends ImplEAN128 {


  ImplEAN14() {
    super("12345678901231");
  }



  /** @hidden */
  @Override
  public boolean supportsAutoCompletion() {
    return true;
  }



  /**
   * Sets the GTIN number to be encoded in the barcode.
   * <p>
   * The number must be either 14 (with check digit) or 13 (without check digit) in length.
   * <ul>
   *   <li>If the number contains 14 digits, the 14th digit is checked to see if it is a valid check
   *     digit. If the check digit is invalid, a {@code BarcodeException} is thrown.</li>
   *   <li>If the number contains 13 digits and {@code autoComplete} is set to {@code true},
   *     the method calculates the missing check digit and appends it to the specified number. If
   *     {@code autoComplete} is set to {@code false}, a {@code BarcodeException} is thrown.</li>
   * </ul>
   *
   * @param content                the GTIN number to be encoded in the barcode
   * @param autoComplete           whether to automatically calculate and append a check digit if
   *                               it is missing
   * @param appendOptionalChecksum has no function, as GTIN uses a fixed check digit
   *                               which is not optional
   * @throws BarcodeException      if the content is empty, contains non-numeric characters, is of
   *                               invalid length, or has an invalid check digit
   */
  @Override
  public void setContent(String content, boolean autoComplete, boolean appendOptionalChecksum)
      throws BarcodeException {

    if (autoComplete && content.length() == 13) {
      validateDigits(content);
      content += calculateModulo10(content);
    }

    super.setContent("01" + content, false, false);
  }

}
