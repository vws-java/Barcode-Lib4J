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
 * Implementation of ISBN-13 (International Standard Book Number).
 * <p>
 * ISBN-13 barcodes are based on the GTIN (Global Trade Item Number) system and use the EAN-13
 * barcode symbology (also known as GTIN-13). ISBN-13 is primarily used for books and other
 * publications, ensuring accurate tracking and identification within the global trade system.
 * <p>
 * An ISBN-13 number consists of the following components:
 * <ul>
 *   <li>A prefix of "978" or "979",</li>
 *   <li>A group identifier,</li>
 *   <li>A publisher code,</li>
 *   <li>An item number, and</li>
 *   <li>A single check digit at the end.</li>
 * </ul>
 * The components are separated by hyphens, for example: 978-1-2345-6789-7.
 * <p>
 * <b>Note:</b> The human readable ISBN-13 number is printed below the barcode without hyphens and
 * above the barcode with hyphens. The latter instance is drawn outside the barcode's bounding box,
 * which may require additional space and a downward shift of the Y-position.
 */
public class ImplISBN13 extends ImplEAN13 {


  ImplISBN13() {
    super("978-1-2345-6789-7");
  }



  /**
   * Sets the ISBN-13 number to be encoded in the barcode.
   * <p>
   * The input must conform to the ISBN-13 structure, consisting of only numeric characters and
   * hyphens ('-'). The input must include a prefix, group, publisher, and item number. For example:
   * <pre>    978-1-2345-6789-7</pre>
   * If the provided ISBN-13 number contains a check digit (at the end), the method validates it
   * according to ISBN-13 rules.
   * <p>
   * If the provided ISBN-13 number does not contain a check digit and the {@code autoComplete} flag
   * is set to {@code true}, the method calculates and appends the correct check digit; otherwise, a
   * {@code BarcodeException} is thrown.
   *
   * @param content                the ISBN-13 number to be encoded
   * @param autoComplete           whether to automatically calculate the check digit if it is
   *                               missing
   * @param appendOptionalChecksum has no function, as ISBN-13 uses a fixed check digit
   *                               which is not optional
   * @throws BarcodeException      if the content is empty, does not conform to the proper ISBN-13
   *                               format, contains an invalid check digit, or if
   *                               {@code autoComplete} is {@code false} and the content is
   *                               missing a check digit.
   */
  @Override
  public void setContent(String content, boolean autoComplete, boolean appendOptionalChecksum)
      throws BarcodeException {

    validateNotEmpty(content);

    if ( !content.startsWith("978-") &&
         !content.startsWith("979-") )
      throw new BarcodeException(BarcodeException.CONTENT_INVALID,
          "Invalid prefix; Expected: 978- or 979-",
          "Ung\u00FCltiges Pr\u00E4fix; Erwartet: 978- oder 979-");

    setContentISxN(content, autoComplete, "ISBN");

  }

}
