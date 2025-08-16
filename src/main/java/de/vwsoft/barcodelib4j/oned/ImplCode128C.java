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
 * Implementation of Code 128 C.
 * <p>
 * Code 128 C is a subset of Code 128, optimized for compact encoding of numeric data. Compactness
 * is achieved by encoding digits in pairs, which allows the encoding to take up only half the
 * space. A minor drawback is that the encoded number must always consist of an even number of
 * digits. However, odd-length numbers can be automatically prefixed with a leading '0' to ensure
 * an even length.
 * <p>
 * Code 128 C uses an internal checksum which is contained only within the barcode symbol and is not
 * displayed in the plaintext representation of the barcode.
 * <p>
 * <b>Note:</b> Encoding the same number with Code 128 and Code 128 C will produce exactly the same
 * barcode. However, Code 128 C can be used, for example, to provide additional validation of
 * numeric values by throwing an exception if the value to be encoded contains non-numeric
 * characters.
 */
public class ImplCode128C extends ImplCode128 {


  ImplCode128C() {
    super("1234567890", CODESET_C);
  }



  /**
   * Sets the number to be encoded in the barcode.
   * <p>
   * If the length of the number is odd and {@code autoComplete} is {@code true}, the method
   * automatically adds a '0' at the beginning, as an odd length number cannot be encoded by
   * Code 128 C according to the requirements of the barcode format.
   * <p>
   * Alternatively, when {@code autoComplete} is set to {@code false}, the method expects that the
   * number is provided with the correct length. In certain scenarios, this can be used to double
   * check the integrity of the number. For example, if it's assumed that the number is even in
   * length, but the actual length turns out to be odd, this will be indicated by the method
   * throwing a {@code BarcodeException}.
   *
   * @param content                the number to be encoded in the Code 128 C barcode
   * @param autoComplete           whether to automatically add '0' at the beginning of the
   *                               number if its length is odd
   * @param appendOptionalChecksum has no function, as Code 128 C uses a fixed internal checksum
   *                               which is not optional
   * @throws BarcodeException      if the content is empty, contains non-numeric characters, or
   *                               the length is odd while {@code autoComplete} is {@code false}
   */
  @Override
  public void setContent(String content, boolean autoComplete, boolean appendOptionalChecksum)
      throws BarcodeException {
    super.setContent(content, autoComplete, appendOptionalChecksum);
  }

}
