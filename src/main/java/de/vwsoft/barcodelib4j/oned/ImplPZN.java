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
 * Implementation of PZN (the older 7-digit format).
 * <p>
 * The PZN (Pharmazentralnummer) is a German barcode standard used to identify pharmaceutical
 * products. It consists of 6 digits for the product identifier plus a check digit.
 * <p>
 * In the barcode symbol, the hyphen (-) is included by placing it in front of the number.
 * <p>
 * The human readable text line for the PZN barcode consists of the prefix 'PZN', followed by a
 * space, then a hyphen, and then the 6-digit product identifier, followed by the check digit at the
 * end. For example:
 * <pre>    PZN -1234562</pre>
 * PZN is not a standalone barcode type but uses the {@link ImplCode39 Code 39} format to encode its
 * data. As a result, this class extends the abstract class {@link LineageTwoWidth}, as Code 39 is a
 * type of two-width barcode. See the linked class description for more information.
 */
public class ImplPZN extends ImplCode39 {


  ImplPZN() {
    super("1234562");
  }



  /** @hidden */
  @Override
  public boolean supportsOptionalChecksum() {
    return false;
  }



  /** @hidden */
  @Override
  public void setOptionalChecksumVisible(boolean visible) {
  }



  /** @hidden */
  @Override
  public boolean isOptionalChecksumVisible() {
    return false;
  }



  /**
   * Sets the content to be encoded in the barcode.
   * <p>
   * If the {@code autoComplete} parameter is {@code true} and the length of the content is 6, the
   * method calculates the checksum and appends it to the content, ensuring a total length of 7.
   * If the calculated checksum is 10, a {@code BarcodeException} is thrown because this value is
   * invalid.
   * <p>
   * If {@code autoComplete} is false, the method validates that the content length is 7 and checks
   * if the last digit matches the calculated checksum. If not, a {@code BarcodeException} is
   * thrown.
   *
   * @param content                the PZN to be encoded in the barcode
   * @param autoComplete           whether to automatically calculate and append a check digit if
   *                               it is missing
   * @param appendOptionalChecksum has no function, as PZN uses a fixed check digit
   *                               which is not optional
   * @throws BarcodeException      if the content is empty, contains invalid characters, is of
   *                               incorrect length, has an invalid check digit, or if the
   *                               calculated checksum is 10
   */
  @Override
  public void setContent(String content, boolean autoComplete, boolean appendOptionalChecksum)
      throws BarcodeException {
    validateNotEmpty(content);
    validateDigits(content);

    if (autoComplete && content.length() == 6) {
      int checksum = calculateChecksum(content);
      if (checksum == 10)
        throw new BarcodeException(BarcodeException.CHECKSUM_INVALID,
            "Invalid PZN: Checksum cannot be 10",
            "Ung\u00FCltige PZN: Pr\u00FCfziffer darf nicht 10 sein");
      content += checksum;
    } else {
      validateFixedLength(content, 7);
      validateCheckDigit(content.charAt(6) - 48, calculateChecksum(content));
    }

    myContent = '-' + content;

    updateHumanReadableText();

    myBars = null; // Reset bars to trigger recalculation next time drawing occurs
  }



  private int calculateChecksum(String content) {
    int sum = 0;
    for (int i=5; i>=0; i--)
      sum += (i + 2) * (content.charAt(i) - 48);
    return sum % 11;
  }



  @Override
  void updateHumanReadableText() {
    myText = "PZN " + myContent;
  }

}
