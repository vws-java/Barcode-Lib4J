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
 * Implementation of Code 11.
 * <p>
 * Code 11 is a numeric barcode format that encodes digits (0-9) and the hyphen (-). It is used
 * primarily in telecommunications.
 * <p>
 * This class extends the abstract class {@link LineageTwoWidth}, as Code 11 is a type of two-width
 * barcode. See the linked class description for more information.
 * <p>
 * Code 11 can be used with an optional checksum. Unlike most other barcode types, the checksum in
 * Code 11 is composed of two characters.
 */
public class ImplCode11 extends LineageTwoWidth {

  private static final String CHARS = "0123456789-";
  private static final int[] BARS = { 1, 17, 9, 24, 5, 20, 12, 3, 18, 16, 4 };

  private String myOptionalChecksum; // Equals 'null' if no optional checksum is used
  private boolean myIsOptionalChecksumVisible; // Visibility within the human-readable text



  ImplCode11() {
    try {
      setContent("1234-5678", false, false);
    } catch (BarcodeException ex) {}
  }



  /** @hidden */
  @Override
  public boolean supportsAutoCompletion() {
    return false;
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
    final String content = myOptionalChecksum != null ? myContent + myOptionalChecksum : myContent;
    final int len = content.length();

    final String[] bars   = { repeat('1', myRatio.y), repeat('1', myRatio.x) };
    final String[] spaces = { repeat('0', myRatio.y), repeat('0', myRatio.x) };

    final String startAndStop = bars[0] + spaces[0] + bars[1] + spaces[1] + bars[0];

    final int leftQuietZone = getQuietZoneLeft() * myRatio.y;
    final int rightQuietZone = getQuietZoneRight() * myRatio.y;

    StringBuilder sb = new StringBuilder(
        myRatio.x * 4 + myRatio.y * 6 +
        len * ((myRatio.x << 1) + (myRatio.y << 2)) +
        myRatio.y +
        leftQuietZone + rightQuietZone);

    sb.append(repeat('0', leftQuietZone));
    sb.append(startAndStop);
    sb.append(spaces[0]); // first intercharacter space
    for (int i=0; i<len; i++) {
      final int n = BARS[CHARS.indexOf(content.charAt(i))];
      sb.append(    bars[(n >> 4) & 1]  );  // bar
      sb.append(  spaces[(n >> 3) & 1]  );  // space
      sb.append(    bars[(n >> 2) & 1]  );  // bar
      sb.append(  spaces[(n >> 1) & 1]  );  // space
      sb.append(    bars[(n     ) & 1]  );  // bar
      sb.append(  spaces[0]             );  // intercharacter space
    }
    sb.append(startAndStop);
    sb.append(repeat('0', rightQuietZone));

    return sb;
  }



  @Override
  double calculateModuleFactor() {
    String content = myContent;
    if (myOptionalChecksum != null)
      content += myOptionalChecksum;

    int narrowBarCount = 3 + (content.length() + 1) + 3;
    int wideBarCount = 2 + 2;
    for (int i=content.length()-1; i>=0; i--) {
      int n = BARS[CHARS.indexOf(content.charAt(i))];
      for (int j=4; j>=0; j--) {
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
   * If the {@code appendOptionalChecksum} parameter is set to {@code true}, the method will
   * calculate and append a checksum to the content. The checksum consists of two characters.
   *
   * @param content                the content to be encoded in the Code 11 barcode
   * @param autoComplete           has no function in this method implementation
   * @param appendOptionalChecksum whether to append an optional checksum to the content
   * @throws BarcodeException      if the content is empty or contains invalid characters
   */
  @Override
  public void setContent(String content, boolean autoComplete, boolean appendOptionalChecksum)
      throws BarcodeException {
    validateNotEmpty(content);

    for (int len=content.length(), i=0; i!=len; i++)
      if (CHARS.indexOf(content.charAt(i)) < 0)
        throwInvalidCharacter(i);

    myContent = content;
    myOptionalChecksum = appendOptionalChecksum ? calculateOptionalChecksum(myContent) : null;

    updateHumanReadableText();

    myBars = null; // Reset bars to trigger recalculation next time drawing occurs
  }



  private static String calculateOptionalChecksum(String content) {

    // 'check character C'
    int sum = 0, count = 0;
    for (int i=content.length()-1; i>=0; i--) {
      sum += (++count) * CHARS.indexOf(content.charAt(i));
      if (count == 10)
        count = 0;
    }
    char checkCharacterC = CHARS.charAt(sum % 11);

    // 'check character K'
    sum = CHARS.indexOf(checkCharacterC);
    count = 1;
    for (int i=content.length()-1; i>=0; i--) {
      sum += (++count) * CHARS.indexOf(content.charAt(i));
      if (count == 9)
        count = 0;
    }
    char checkCharacterK = CHARS.charAt(sum % 11);

    return checkCharacterC + "" + checkCharacterK;
  }



  private void updateHumanReadableText() {
    myText = myContent;
    if (myOptionalChecksum != null && myIsOptionalChecksumVisible)
      myText += myOptionalChecksum;
  }

}
