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
 * Implementation of Codabar.
 * <p>
 * Codabar is a 1D barcode symbology typically used in libraries, blood banks and transportation
 * applications. It is capable of encoding digits (0-9) and the characters plus (+), minus (-),
 * dollar ($), colon (:), slash (/), and period (.), as well as the start/stop characters
 * 'A', 'B', 'C' and 'D'.
 * <p>
 * The encoded data must begin and end with one of the above start/stop characters. These characters
 * can be either different or the same.
 */
public class ImplCodabar extends Barcode {

  private static final String CHARS = "0123456789-$:/.+ABCD";
  private static final int BARS[] = { 3,6,9,96,18,66,33,36,48,72,12,24,69,81,84,21,26,41,11,14 };



  ImplCodabar() {
    try {
      setContent("A12345678A", false, false);
    } catch (BarcodeException ex) {}
  }



  /** @hidden */
  @Override
  public boolean supportsRatio() {
    return true;
  }



  @Override
  CharSequence encode() {
    final String[] bars   = { "1".repeat(myRatio.y), "1".repeat(myRatio.x) };
    final String[] spaces = { "0".repeat(myRatio.y), "0".repeat(myRatio.x) };

    final int leftQuietZone = getQuietZoneLeft() * myRatio.y;
    final int rightQuietZone = getQuietZoneRight() * myRatio.y;

    final int len = myContent.length();

    StringBuilder sb = new StringBuilder(len * (3 * myRatio.x + 5 * myRatio.y) +
        leftQuietZone + rightQuietZone);

    sb.append("0".repeat(leftQuietZone));
    for (int i=0; i<len; i++) {
      final int n = BARS[CHARS.indexOf(myContent.charAt(i))];
      for (int j=6; j>0; ) {
        sb.append(    bars[(n >> (j--)) & 1]  );
        sb.append(  spaces[(n >> (j--)) & 1]  );
      }
      sb.append(bars[n & 1]);
      if (i < len - 1)
        sb.append(spaces[0]);
    }
    sb.append("0".repeat(rightQuietZone));

    return sb;
  }



  @Override
  double calculateModuleFactor() {
    int narrowBarCount = myContent.length() - 1;
    int wideBarCount = 0;

    for (int i=narrowBarCount; i>=0; i--) {
      int n = BARS[CHARS.indexOf(myContent.charAt(i))];
      for (int j=6; j>=0; j--) {
        if (((n >> j) & 1) == 0)
          narrowBarCount++;
        else
          wideBarCount++;
      }
    }

    return narrowBarCount + wideBarCount * (double)myRatio.x / myRatio.y +
        getQuietZoneLeft() + getQuietZoneRight();
  }



  /**
   * Sets the content to be encoded in the barcode.
   * <p>
   * Validates the provided content and ensures that it begins and ends with a valid start/stop
   * character ('A', 'B', 'C' or 'D'). If {@code autoComplete} is set to {@code true}, the method
   * will automatically add 'A' as the start and stop character if one or both are missing.
   *
   * @param content                the content to be encoded in the Codabar barcode
   * @param autoComplete           whether to automatically add start and stop characters if they
   *                               are missing
   * @param appendOptionalChecksum has no function, as Codabar does not support checksums
   * @throws BarcodeException      if the content is empty, invalid, or missing required start/stop
   *                               characters
   */
  @Override
  public void setContent(String content, boolean autoComplete, boolean appendOptionalChecksum)
      throws BarcodeException {
    validateNotEmpty(content);

    // Check and handle the start character
    char startChar = Character.toUpperCase(content.charAt(0));
    if (isStartOrStopChar(startChar))
      content = startChar + content.substring(1);
    else if (autoComplete)
      content = 'A' + content;
    else
      throw new BarcodeException(BarcodeException.CONTENT_INVALID,
          "Value must start with 'A', 'B', 'C' or 'D'",
          "Wert muss mit 'A', 'B', 'C' oder 'D' beginnen");

    // Check and handle the stop character
    char stopChar = Character.toUpperCase(content.charAt(content.length() - 1));
    if (isStartOrStopChar(stopChar))
      content = content.substring(0, content.length() - 1) + stopChar;
    else if (autoComplete)
      content = content + 'A';
    else
      throw new BarcodeException(BarcodeException.CONTENT_INVALID,
          "Value must end with 'A', 'B', 'C' or 'D'",
          "Wert muss mit 'A', 'B', 'C' oder 'D' enden");

    // Recheck if content is empty, excluding the start and stop characters
    validateNotEmpty(content.substring(Math.min(2, content.length())));

    // Validate that all characters (excluding the start and stop characters) are valid
    String validChars = CHARS.substring(0, 16);
    for (int i=1, j=content.length()-1; i!=j; i++)
      if ((validChars.indexOf(content.charAt(i)) < 0))
        throwInvalidCharacter(i);

    myContent = content;
    invalidateDrawing(); // Reset cached bars to force recalculation on the next drawing
  }



  private boolean isStartOrStopChar(char c) {
    return c == 'A' || c == 'B' || c == 'C' || c == 'D';
  }

}
