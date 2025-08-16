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
 * Implementation of Interleaved 2 of 5 (ITF).
 * <p>
 * Interleaved 2 of 5 is a widely used barcode format for compact encoding of numeric data.
 * Compactness is achieved by encoding digits in pairs, allowing the encoding to take up only half
 * the space. A minor drawback is that the encoded number must always consist of an even number of
 * digits. However, odd-length numbers can be automatically prefixed with a leading '0' to ensure
 * an even length.
 * <p>
 * Interleaved 2 of 5 can also be used with an optional check digit for additional error detection
 * and data integrity. The check digit can be appended to the encoded content and can optionally be
 * displayed in the human readable text. When the check digit is used, it changes the total length
 * of the encoded content, which may affect the evenness of the total length. This should be
 * considered when preparing content for encoding.
 * <p>
 * This class extends the abstract class {@link LineageTwoWidth}, as Interleaved 2 of 5 is a
 * type of two-width barcode. See the linked class description for more information.
 * <p>
 * <b>Barcode scanner configuration:</b> When using Interleaved 2 of 5 without a check digit, it is
 * advisable to configure the scanner to expect a constant number of characters. This is because the
 * start and stop patterns are not unique, making the format vulnerable. If the scanner enters or
 * exits the code at a point that resembles a start or stop pattern, the code may be scanned
 * incompletely.
 */
public class ImplITF extends LineageTwoWidth {

  private static final int[] BARS = { 6, 17, 9, 24, 5, 20, 12, 3, 18, 10 }; // Code patterns

  private String myOptionalChecksum; // Equals 'null' if no optional checksum is used
  private boolean myIsOptionalChecksumVisible; // Visibility within the human-readable text



  ImplITF() {
    try {
      setContent("1234567890", false, false);
    } catch (BarcodeException ex) {}
  }



  @Override
  CharSequence encode() {
    String content = myContent;
    if (myOptionalChecksum != null)
      content += myOptionalChecksum;
    final int contentLength = content.length();

    final String[] bars   = { repeat('1', myRatio.y), repeat('1', myRatio.x) };
    final String[] spaces = { repeat('0', myRatio.y), repeat('0', myRatio.x) };

    final int leftQuietZone = getQuietZoneLeft() * myRatio.y;
    final int rightQuietZone = getQuietZoneRight() * myRatio.y;

    StringBuilder sb = new StringBuilder(myRatio.y * 6 + leftQuietZone + rightQuietZone +
        myRatio.x + (2 * myRatio.x + 3 * myRatio.y) * contentLength);

    sb.append(repeat('0', leftQuietZone));                                   // left quiet zone
    sb.append(bars[0]).append(spaces[0]).append(bars[0]).append(spaces[0]);  // start sign

    for (int i=0; i<contentLength; i+=2) {
      final int firstPartOfPair = BARS[content.charAt(i) - 48];
      final int secondPartOfPair = BARS[content.charAt(i + 1) - 48];
      for (int j=4; j>=0; j--) {
        sb.append(bars[(firstPartOfPair >> j) & 1]);
        sb.append(spaces[(secondPartOfPair >> j) & 1]);
      }
    }

    sb.append(bars[1]).append(spaces[0]).append(bars[0]);                    // stop sign
    sb.append(repeat('0', rightQuietZone));                                  // right quiet zone

    return sb;
  }



  @Override
  double calculateModuleFactor() {
    int len = myContent.length();
    if (myOptionalChecksum != null)
      len++;
    double ratio = (double)myRatio.x / myRatio.y;
    return len * (2 * ratio + 3) + ratio + 6 + getQuietZoneLeft() + getQuietZoneRight();
  }



  /**
   * Sets the number to be encoded in the barcode.
   * <p>
   * If the length of the number is odd and {@code autoComplete} is {@code true}, the method
   * automatically adds a '0' at the beginning, as an odd length number cannot be encoded by
   * Interleaved 2 of 5 according to the requirements of the barcode format.
   * <p>
   * Alternatively, when {@code autoComplete} is set to {@code false}, the method expects that the
   * number is provided with the correct length. In certain scenarios, this can be used to double
   * check the integrity of the number. For example, if it's assumed that the number is even in
   * length, but the actual length turns out to be odd, this will be indicated by the method
   * throwing a {@code BarcodeException}.
   * <p>
   * Using an optional check digit affects the total length of the number and can determine whether
   * the total number of digits is even or odd. This should be considered when providing content for
   * encoding.
   *
   * @param content                the number to be encoded in the Interleaved 2 of 5 barcode
   * @param autoComplete           whether to automatically add '0' at the beginning of the
   *                               number if its length is odd
   * @param appendOptionalChecksum whether to append an optional check digit to the number
   * @throws BarcodeException      if the content is empty, contains non-numeric characters, or
   *                               the length is odd while {@code autoComplete} is {@code false}
   */
  @Override
  public void setContent(String content, boolean autoComplete, boolean appendOptionalChecksum)
      throws BarcodeException {
    validateNotEmpty(content);
    validateDigits(content);

    if ( (content.length() % 2 != 0) ^ appendOptionalChecksum ) {
      if (autoComplete)
        content = "0" + content;
      else
        throwContentLengthNotEven(content.length());
    }

    myContent = content;
    myOptionalChecksum = appendOptionalChecksum ? "" + calculateModulo10(myContent) : null;

    updateHumanReadableText();

    myBars = null; // Reset bars to trigger recalculation next time drawing occurs
  }



  private void updateHumanReadableText() {
    myText = myContent;
    if (myOptionalChecksum != null && myIsOptionalChecksumVisible)
      myText += myOptionalChecksum;
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

}
