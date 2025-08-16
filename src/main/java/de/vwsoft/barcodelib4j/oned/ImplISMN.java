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
 * Implementation of ISMN (International Standard Music Number).
 * <p>
 * ISMN barcodes are based on the GTIN (Global Trade Item Number) system and use the EAN-13
 * barcode symbology (also known as GTIN-13). ISMN is primarily used for printed music and musical
 * scores, ensuring accurate tracking and identification within the global trade system.
 * <p>
 * An ISMN number consists of the following components:
 * <ul>
 *   <li>A prefix of "979" followed by a single digit "0" to indicate a musical work,</li>
 *   <li>A publisher code,</li>
 *   <li>An item number, and</li>
 *   <li>A single check digit at the end.</li>
 * </ul>
 * The components are separated by hyphens, for example: 979-0-1234-5678-5.
 * <p>
 * <b>Note:</b> The human readable ISMN number is printed below the barcode without hyphens and
 * above the barcode with hyphens. The latter instance is drawn outside the barcode's bounding box,
 * which may require additional space and a downward shift of the Y-position.
 */
public class ImplISMN extends ImplEAN13 {


  ImplISMN() {
    super("979-0-1234-5678-5");
  }



  /**
   * Sets the ISMN number to be encoded in the barcode.
   * <p>
   * The input must conform to the ISMN structure, consisting of only numeric characters and
   * hyphens ('-'). The input must include a prefix, publisher, and item number. For example:
   * <pre>    979-0-1234-5678-5</pre>
   * If the provided ISMN number contains a check digit (at the end), the method validates it
   * according to ISMN rules.
   * <p>
   * If the provided ISMN number does not contain a check digit and the {@code autoComplete} flag
   * is set to {@code true}, the method calculates and appends the correct check digit; otherwise, a
   * {@code BarcodeException} is thrown.
   *
   * @param content                the ISMN number to be encoded
   * @param autoComplete           whether to automatically calculate the check digit if it is
   *                               missing
   * @param appendOptionalChecksum has no function, as ISMN uses a fixed check digit
   *                               which is not optional
   * @throws BarcodeException      if the content is empty, does not conform to the proper ISMN
   *                               format, contains an invalid check digit, or if
   *                               {@code autoComplete} is {@code false} and the content is
   *                               missing a check digit.
   */
  @Override
  public void setContent(String content, boolean autoComplete, boolean appendOptionalChecksum)
      throws BarcodeException {

    validateNotEmpty(content);

    if (!content.startsWith("979-0-"))
      throw new BarcodeException(BarcodeException.CONTENT_INVALID,
          "Invalid prefix; Expected: 979-0-",
          "Ung\u00FCltiges Pr\u00E4fix; Erwartet: 979-0-");

    setContentISxN(content, autoComplete, "ISMN");

  }

}
