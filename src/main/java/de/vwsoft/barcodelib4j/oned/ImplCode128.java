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
 * Implementation of Code 128.
 * <p>
 * Code 128 is a widely used high-density barcode symbology that encodes all 128 ASCII characters,
 * covering:
 * <ul>
 *   <li>Control characters (ASCII 0-31), such as NUL, SOH, STX, etc.</li>
 *   <li>Digits (0-9)</li>
 *   <li>Upper and lower case letters (A-Z, a-z)</li>
 *   <li>Space character</li>
 *   <li>Punctuation and special characters:
 *     <code>!&quot;#$%&amp;'()*+,-./:;&lt;=&gt;?@[\]^_`{|}~</code></li>
 * </ul>
 * In addition, Function Codes (FNC1 to FNC4) can be used within the provided input to meet
 * specific application requirements.
 * <p>
 * Code 128 uses an internal checksum included within the barcode symbol but not displayed in
 * plaintext representation.
 */
public class ImplCode128 extends Barcode {


  // FNC markers. To better distinguish between invalid characters outside the ASCII range 0-127
  // and FNC markers within the input, we assign 'extra high' values to the latter.

  /** Character constant that can be used as a marker for FNC1 in the input. */
  public static final char FNC1 = '\uFFF1';

  /** Character constant that can be used as a marker for FNC2 in the input. */
  public static final char FNC2 = '\uFFF2';

  /** Character constant that can be used as a marker for FNC3 in the input. */
  public static final char FNC3 = '\uFFF3';

  /** Character constant that can be used as a marker for FNC4 in the input. */
  public static final char FNC4 = '\uFFF4';



  static final int CODESET_ALL = 0;
  static final int CODESET_A = 101;
  static final int CODESET_B = 100;
  static final int CODESET_C = 99;

  private static final int[] BARS = { 1740,1644,1638,1176,1164,1100,1224,1220,1124,1608,1604,1572,
      1436,1244,1230,1484,1260,1254,1650,1628,1614,1764,1652,1902,1868,1836,1830,1892,1844,1842,
      1752,1734,1590,1304,1112,1094,1416,1128,1122,1672,1576,1570,1464,1422,1134,1496,1478,1142,
      1910,1678,1582,1768,1762,1774,1880,1862,1814,1896,1890,1818,1914,1602,1930,1328,1292,1200,
      1158,1068,1062,1424,1412,1232,1218,1076,1074,1554,1616,1978,1556,1146,1340,1212,1182,1508,
      1268,1266,1956,1940,1938,1758,1782,1974,1400,1310,1118,1512,1506,1960,1954,1502,1518,1886,
      1966,1668,1680,1692 };

  private static final int CODE_START_A = 103;
  private static final int CODE_START_B = 104;
  private static final int CODE_START_C = 105;
  private static final int CODE_FNC_1 = 102;   // set A, set B, set C
  private static final int CODE_FNC_2 = 97;    // set A, set B
  private static final int CODE_FNC_3 = 96;    // set A, set B
  private static final int CODE_FNC_4_A = 101; // set A
  private static final int CODE_FNC_4_B = 100; // set B

  private static final int SETC_ONE_DIGIT  = 1;
  private static final int SETC_TWO_DIGITS = 2;
  private static final int SETC_FNC_1      = 3;
  private static final int SETC_UNCODABLE  = 4;

  private final int myCodeSet; // Holds the value of one of the CODESET_x constants



  ImplCode128() {
    this("Code 128", CODESET_ALL);
  }



  ImplCode128(String content, int codeSet) {
    myCodeSet = codeSet;
    try {
      setContent(content, false, false);
    } catch (BarcodeException ex) {}
  }



  /** @hidden */
  @Override
  public boolean supportsAutoCompletion() {
    return myCodeSet == CODESET_A || myCodeSet == CODESET_C;
  }



  @Override
  CharSequence encode() {
    final int len = myContent.length();
    final int leftQuietZone = getQuietZoneLeft();
    final int rightQuietZone = getQuietZoneRight();
    final StringBuilder sb = new StringBuilder(len * 15 + leftQuietZone + rightQuietZone);

    sb.append(repeat('0', leftQuietZone));

    int checkSum = 0;
    int checkSumWeight = 1;
    int codeSet = 0;
    int idx = 0;

    while (idx < len) {
      int newCodeSet = myCodeSet == CODESET_ALL ?
          chooseCodeSet(myContent, idx, codeSet) : myCodeSet;

      int barsIdx;
      if (newCodeSet == codeSet) {
        char c = myContent.charAt(idx);
        if (c == FNC1) {
          barsIdx = CODE_FNC_1;
        } else if (c == FNC2) {
          barsIdx = CODE_FNC_2;
        } else if (c == FNC3) {
          barsIdx = CODE_FNC_3;
        } else if (c == FNC4) {
          barsIdx = codeSet == CODESET_A ? CODE_FNC_4_A : CODE_FNC_4_B;
        } else {
          if (codeSet == CODESET_A) {
            barsIdx = c - ' ';
            if (barsIdx < 0)
              barsIdx += '`';
          } else if (codeSet == CODESET_B) {
            barsIdx = c - ' ';
          } else { // CODESET_C
            barsIdx = Integer.parseInt(myContent.substring(idx, idx + 2));
            idx++;
          }
        }
        idx++;
      } else {
        if (codeSet == 0) {
          if (newCodeSet == CODESET_A)
            barsIdx = CODE_START_A;
          else if (newCodeSet == CODESET_B)
            barsIdx = CODE_START_B;
          else
            barsIdx = CODE_START_C;
        } else {
          barsIdx = newCodeSet;
        }
        codeSet = newCodeSet;
      }

      sb.append(Integer.toBinaryString(BARS[barsIdx]));

      checkSum += barsIdx * checkSumWeight;
      if (idx != 0)
        checkSumWeight++;
    }

    sb.append(Integer.toBinaryString(BARS[checkSum % 103]));
    sb.append("11000111010" + "11");
    sb.append(repeat('0', rightQuietZone));

    return sb;
  }



  private static int chooseCodeSet(String content, int idx, int oldCodeSet) {
    int forward = chooseSetCType(content, idx);
    if (forward == SETC_ONE_DIGIT)
      return CODESET_B;
    if (forward == SETC_UNCODABLE) {
      if (idx < content.length()) {
        char c = content.charAt(idx);
        if (c < ' ' || (oldCodeSet == CODESET_A && c < '`'))
          return CODESET_A;
      }
      return CODESET_B;
    }
    if (oldCodeSet == CODESET_C)
      return CODESET_C;
    if (oldCodeSet == CODESET_B) {
      if (forward == SETC_FNC_1)
        return CODESET_B;
      forward = chooseSetCType(content, idx + 2);
      if (forward == SETC_UNCODABLE || forward == SETC_ONE_DIGIT)
        return CODESET_B;
      if (forward == SETC_FNC_1)
        return chooseSetCType(content, idx + 3) == SETC_TWO_DIGITS ? CODESET_C : CODESET_B;
      int index = idx + 4;
      while ((forward = chooseSetCType(content, index)) == SETC_TWO_DIGITS)
        index += 2;
      return forward == SETC_ONE_DIGIT ? CODESET_B : CODESET_C;
    }
    if (forward == SETC_FNC_1)
      forward = chooseSetCType(content, idx + 1);
    return forward == SETC_TWO_DIGITS ? CODESET_C : CODESET_B;
  }



  private static int chooseSetCType(String content, int idx) {
    int last = content.length();
    if (idx >= last)
      return SETC_UNCODABLE;
    char c = content.charAt(idx);
    if (c == FNC1)
      return SETC_FNC_1;
    if (!isDigit(c))
      return SETC_UNCODABLE;
    if (idx + 1 >= last)
      return SETC_ONE_DIGIT;
    return isDigit(content.charAt(idx + 1)) ? SETC_TWO_DIGITS : SETC_ONE_DIGIT;
  }



  /**
   * Sets the content to be encoded in the barcode.
   * <p>
   * Validates the provided content to ensure that it contains only ASCII characters,
   * and throws a {@code BarcodeException} if it does not.
   * <p>
   * The provided content can also include Function Codes {@link #FNC1 FNC1} to
   * {@link #FNC4 FNC4} for specific application needs.
   *
   * @param content                the content to be encoded in the Code 128 barcode
   * @param autoComplete           has no function in this method implementation
   * @param appendOptionalChecksum has no function, as Code 128 uses a fixed internal checksum
   *                               which is not optional
   * @throws BarcodeException      if the content is empty, or contains non-ASCII characters
   */
  @Override
  public void setContent(String content, boolean autoComplete, boolean appendOptionalChecksum)
      throws BarcodeException {
    validateNotEmpty(content);
    final int len = content.length();

    if (myCodeSet == CODESET_C) {
      validateDigits(content);
      if (len % 2 != 0) {
        if (autoComplete)
          content = "0" + content;
        else
          throwContentLengthNotEven(len);
      }
    } else {
      if (myCodeSet == CODESET_A && autoComplete)
        content = content.toUpperCase();
      StringBuilder sb = new StringBuilder(len);
      for (int i=0; i<len; i++) {
        char c = content.charAt(i);
        if (c != FNC1 && c != FNC2 && c != FNC3 && c != FNC4)
          if ((myCodeSet == CODESET_A && c > '_') ||
              (myCodeSet == CODESET_B && c < ' ') ||
              (c > 127))
            throwInvalidCharacter(i);
        sb.append(c < 32 || c > 127 ? ' ' : c);
      }
      myText = sb.toString();
    }

    myContent = content;
    myBars = null; // Reset bars to trigger recalculation next time drawing occurs
  }

}
