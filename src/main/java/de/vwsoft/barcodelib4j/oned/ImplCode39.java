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
 * Implementation of Code 39 (also known as Code 3 of 9).
 * <p>
 * Code 39 is an alphanumeric barcode format that is widely used in applications such as inventory
 * management and tracking. It is capable of encoding:
 * <ul>
 *   <li>Upper case letters (A-Z)</li>
 *   <li>Digits (0-9)</li>
 *   <li>Space character</li>
 *   <li>Special characters: plus (+), minus (-), dollar ($), slash (/), period (.),
 *     percent (%)</li>
 * </ul>
 */
public class ImplCode39 extends Barcode {

  private static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. $/+%*";
  private static final int[] BARS = { 52, 289, 97, 352, 49, 304, 112, 37, 292, 100, 265, 73, 328,
      25, 280, 88, 13, 268, 76, 28, 259, 67, 322, 19, 274, 82, 7, 262, 70, 22, 385, 193, 448, 145,
      400, 208, 133, 388, 196, 168, 162, 138, 42, 148 };

  String myOptionalChecksum; // Equals 'null' if no optional checksum is used
  boolean myIsOptionalChecksumVisible; // Visibility within the human-readable text



  ImplCode39() {
    this("CODE 39");
  }



  ImplCode39(String content) {
    try {
      setContent(content, false, false);
    } catch (BarcodeException ex) {}
  }



  /** @hidden */
  @Override
  public boolean supportsRatio() {
    return true;
  }



  /** @hidden */
  @Override
  public boolean supportsOptionalChecksum() {
    return true;
  }



  /** @hidden */
  @Override
  public void setOptionalChecksumVisible(boolean visible) {
    myIsOptionalChecksumVisible = visible;
    updateHumanReadableText();
  }



  /** @hidden */
  @Override
  public boolean isOptionalChecksumVisible() {
    return myIsOptionalChecksumVisible;
  }



  @Override
  CharSequence encode() {
    String content = myContent;
    if (myOptionalChecksum != null)
      content += myOptionalChecksum;
    content = '*' + content + '*';

    final String[] narr = { "1".repeat(myRatio.y), "0".repeat(myRatio.y) };
    final String[] wide = { "1".repeat(myRatio.x), "0".repeat(myRatio.x) };

    final int leftQuietZone = getQuietZoneLeft() * myRatio.y;
    final int rightQuietZone = getQuietZoneRight() * myRatio.y;

    StringBuilder sb = new StringBuilder((3 * myRatio.x + (6 + 1) * myRatio.y) * content.length() +
        leftQuietZone + rightQuietZone);

    sb.append("0".repeat(leftQuietZone));
    final int k = content.length() - 1;
    for (int i=0; i<=k; i++) {
      int barMask = BARS[CHARS.indexOf(content.charAt(i))];
      for (int j=0; j<9; j++)
        sb.append((barMask & (1 << (8 - j))) == 0 ? narr[j % 2] : wide[j % 2]);
      if (i < k)
        sb.append(narr[1]);
    }
    sb.append("0".repeat(rightQuietZone));

    return sb;
  }



  @Override
  double calculateModuleFactor() {
    int len = myContent.length() + (myOptionalChecksum != null ? 3 : 2);
    return 7 * len + 3 * len * (double)myRatio.x / myRatio.y - 1 +
        getQuietZoneLeft() + getQuietZoneRight();
  }



  /**
   * Sets the content to be encoded in the barcode.
   * <p>
   * The content is validated against the allowed character set. If {@code autoComplete} is enabled,
   * lowercase letters are automatically converted to uppercase. If {@code appendOptionalChecksum}
   * is enabled, a modulo 43 checksum is calculated and appended.
   *
   * @param content                the content to be encoded in the Code 39 barcode
   * @param autoComplete           whether to convert lowercase letters to uppercase letters
   * @param appendOptionalChecksum whether to append an optional modulo 43 checksum
   * @throws BarcodeException      if the content is empty or contains invalid characters
   */
  @Override
  public void setContent(String content, boolean autoComplete, boolean appendOptionalChecksum)
      throws BarcodeException {
    validateNotEmpty(content);

    if (autoComplete)
      content = content.toUpperCase();

    for (int len=content.length(), i=0; i!=len; i++) {
      char c = content.charAt(i);
      if (c == '*' || CHARS.indexOf(c) < 0)
        throwInvalidCharacter(i);
    }

    myContent = content;
    myOptionalChecksum = appendOptionalChecksum ? calculateOptionalChecksum(myContent) : null;

    updateHumanReadableText();
    invalidateDrawing(); // Reset cached bars to force recalculation on the next drawing
  }



  static String calculateOptionalChecksum(String content) {
    int sum = 0;
    for (int i=content.length()-1; i>=0; i--)
      sum += CHARS.indexOf(content.charAt(i));
    return "" + CHARS.charAt(sum % 43);
  }



  void updateHumanReadableText() {
    myText = myContent;
    if (myOptionalChecksum != null && myIsOptionalChecksumVisible)
      myText += myOptionalChecksum;
  }

}
