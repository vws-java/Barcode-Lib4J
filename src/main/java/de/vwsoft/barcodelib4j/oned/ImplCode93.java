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
 * Implementation of Code 93.
 * <p>
 * Code 93 was developed to provide higher information density and data security than
 * {@link ImplCode39 Code 39}. The barcode format encodes the same 43 characters. It includes a
 * mandatory checksum consisting of 2 digits, which are not displayed in the human-readable text
 * line.
 */
public class ImplCode93 extends Barcode {

  private static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. $/+%abcd*";
  private static final int[] BARS = { 276, 328, 324, 322, 296, 292, 290, 336, 274, 266, 424, 420,
      418, 404, 402, 394, 360, 356, 354, 308, 282, 344, 332, 326, 300, 278, 436, 434, 428, 422, 406,
      410, 364, 358, 310, 314, 302, 468, 466, 458, 366, 374, 430, 294, 474, 470, 306, 350 };



  ImplCode93() {
    this("CODE 93");
  }



  ImplCode93(String content) {
    try {
      setContent(content, false, false);
    } catch (BarcodeException ex) {}
  }



  @Override
  CharSequence encode() {
    StringBuilder sb = new StringBuilder(myContent.length() + 1 + 1 + 2);
    sb.append('*').append(myContent).append(calculateChecksum(myContent)).append('*');

    String value = sb.toString();
    final int len = value.length();

    final int leftQuietZone = getQuietZoneLeft();
    final int rightQuietZone = getQuietZoneRight();
    sb = new StringBuilder(9 * len + 1 + leftQuietZone + rightQuietZone);

    sb.append("0".repeat(leftQuietZone));
    for (int i=0; i<len; i++)
      sb.append(Integer.toBinaryString(BARS[CHARS.indexOf(value.charAt(i))]));
    sb.append('1'); // "termination bar"
    sb.append("0".repeat(rightQuietZone));

    return sb;
  }



  /**
   * Sets the content to be encoded in the barcode.
   *
   * @param content                the content to be encoded in the Code 93 barcode
   * @param autoComplete           whether to automatically convert lowercase letters
   *                               in the content to uppercase letters
   * @param appendOptionalChecksum has no function, as Code 93 uses a mandatory checksum
   *                               which is not optional
   * @throws BarcodeException      if the content is empty or contains invalid characters
   */
  @Override
  public void setContent(String content, boolean autoComplete, boolean appendOptionalChecksum)
      throws BarcodeException {
    validateNotEmpty(content);

    if (autoComplete)
      content = content.toUpperCase();

    final String validChars = CHARS.substring(0, 43);
    for (int len=content.length(), i=0; i!=len; i++)
      if (validChars.indexOf(content.charAt(i)) < 0)
        throwInvalidCharacter(i);

    myContent = content;
    invalidateDrawing(); // Reset cached bars to force recalculation on the next drawing
  }



  private static String calculateChecksum(String content) {

    // calculate 'check character C'
    int sum = 0, count = 0;
    for (int i=content.length()-1; i>=0; i--) {
      sum += (++count) * CHARS.indexOf(content.charAt(i));
      if (count == 20)
        count = 0;
    }
    char checkCharacterC = CHARS.charAt(sum % 47);

    // calculate 'check character K'
    sum = CHARS.indexOf(checkCharacterC);
    count = 1;
    for (int i=content.length()-1; i>=0; i--) {
      sum += (++count) * CHARS.indexOf(content.charAt(i));
      if (count == 15)
        count = 0;
    }
    char checkCharacterK = CHARS.charAt(sum % 47);

    return checkCharacterC + "" + checkCharacterK;
  }

}
