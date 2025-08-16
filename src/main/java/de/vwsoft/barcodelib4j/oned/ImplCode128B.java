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
 * Implementation of Code 128 B.
 * <p>
 * Code 128 B is a subset of Code 128, capable of encoding ASCII characters with values
 * from 32 to 127, covering:
 * <ul>
 *   <li>Upper and lower case letters (A-Z, a-z)</li>
 *   <li>Digits (0-9)</li>
 *   <li>Space character</li>
 *   <li>Punctuation and special characters:
 *     <code>!&quot;#$%&amp;'()*+,-./:;&lt;=&gt;?@[\]^_`{|}~</code></li>
 * </ul>
 * In addition, Function Codes (FNC1 to FNC4) can be used within the provided input to meet
 * specific application requirements.
 * <p>
 * Code 128 B uses an internal checksum included within the barcode symbol but not displayed in
 * plaintext representation.
 */
public class ImplCode128B extends ImplCode128 {


  ImplCode128B() {
    super("Code 128 B", CODESET_B);
  }



  /**
   * Sets the content to be encoded in the barcode.
   * <p>
   * Validates the provided content to ensure that it contains only ASCII characters with values
   * between 32 and 127, and throws a {@code BarcodeException} if it does not.
   * <p>
   * The provided content can also include Function Codes {@link #FNC1 FNC1} to
   * {@link #FNC4 FNC4} for specific application needs.
   *
   * @param content                the content to be encoded in the Code 128 B barcode
   * @param autoComplete           has no function in this method implementation
   * @param appendOptionalChecksum has no function, as Code 128 B uses a fixed internal checksum
   *                               which is not optional
   * @throws BarcodeException      if the content is empty, or contains characters outside ASCII
   *                               range 32-127
   */
  @Override
  public void setContent(String content, boolean autoComplete, boolean appendOptionalChecksum)
      throws BarcodeException {
    super.setContent(content, autoComplete, appendOptionalChecksum);
  }

}
