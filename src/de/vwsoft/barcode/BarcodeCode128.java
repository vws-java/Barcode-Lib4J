/*
 * Copyright 2023 by Viktor Wedel, https://www.vwsoft.de/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.vwsoft.barcode;


public class BarcodeCode128 extends Barcode {
  public static final int CODESET_ALL = 0;
  public static final int CODESET_A = 101;
  public static final int CODESET_B = 100;
  public static final int CODESET_C = 99;

  // placeholder characters used to specify FNC1 to FNC4 characters in input
  public static final char FNC1_CHAR = '\ufff1';
  public static final char FNC2_CHAR = '\ufff2';
  public static final char FNC3_CHAR = '\ufff3';
  public static final char FNC4_CHAR = '\ufff4';

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

  private final int myCodeSet; // holds the value of one of the CODESET_x constants


  //----
  public BarcodeCode128(String number) throws IllegalArgumentException {
    this(CODESET_ALL, number);
  }


  //----
  public BarcodeCode128() throws IllegalArgumentException {
    this(CODESET_ALL, "Code 128");
  }


  //----
  public BarcodeCode128(int codeSet, String number) throws IllegalArgumentException {
    myCodeSet = codeSet;
    setNumber(number, false, false);
  }


  //----
  public boolean isCompletionSupported() {
    return myCodeSet == CODESET_A || myCodeSet == CODESET_C;
  }


  //----
  protected String computeBars() {
    final int len = myNumber.length();
    final int leftQuietZone = myIsQuietZonesIncluded ? getQuietZoneLeft() : 0;
    final int rightQuietZone = myIsQuietZonesIncluded ? getQuietZoneRight() : 0;
    final StringBuilder sb = new StringBuilder(len * 15 + leftQuietZone + rightQuietZone);

    sb.append(repeat('0', leftQuietZone));

    int checkSum = 0;
    int checkSumWeight = 1;
    int codeSet = 0;
    int idx = 0;

    while (idx < len) {
      int newCodeSet = myCodeSet == CODESET_ALL ? chooseCodeSet(myNumber, idx, codeSet) :
          myCodeSet;

      int barsIdx;
      if (newCodeSet == codeSet) {
        char c = myNumber.charAt(idx);
        if (c == FNC1_CHAR) {
          barsIdx = CODE_FNC_1;
        } else if (c == FNC2_CHAR) {
          barsIdx = CODE_FNC_2;
        } else if (c == FNC3_CHAR) {
          barsIdx = CODE_FNC_3;
        } else if (c == FNC4_CHAR) {
          barsIdx = codeSet == CODESET_A ? CODE_FNC_4_A : CODE_FNC_4_B;
        } else {
          if (codeSet == CODESET_A) {
            barsIdx = c - ' ';
            if (barsIdx < 0)
              barsIdx += '`';
          } else if (codeSet == CODESET_B) {
            barsIdx = c - ' ';
          } else { // CODESET_C
            barsIdx = Integer.parseInt(myNumber.substring(idx, idx + 2));
            idx ++;
          }
        }
        idx ++;
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
        checkSumWeight ++;
    }

    sb.append(Integer.toBinaryString(BARS[checkSum % 103]));
    sb.append("11000111010" + "11");
    sb.append(repeat('0', rightQuietZone));

    return sb.toString();
  }


  //----
  private static int chooseCodeSet(String number, int idx, int oldCodeSet) {
    int forward = chooseSetCType(number, idx);
    if (forward == SETC_ONE_DIGIT)
      return CODESET_B;
    if (forward == SETC_UNCODABLE) {
      if (idx < number.length()) {
        char c = number.charAt(idx);
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
      forward = chooseSetCType(number, idx + 2);
      if (forward == SETC_UNCODABLE || forward == SETC_ONE_DIGIT)
        return CODESET_B;
      if (forward == SETC_FNC_1)
        return chooseSetCType(number, idx + 3) == SETC_TWO_DIGITS ? CODESET_C : CODESET_B;
      int index = idx + 4;
      while ((forward = chooseSetCType(number, index)) == SETC_TWO_DIGITS)
        index += 2;
      return forward == SETC_ONE_DIGIT ? CODESET_B : CODESET_C;
    }
    if (forward == SETC_FNC_1)
      forward = chooseSetCType(number, idx + 1);
    return forward == SETC_TWO_DIGITS ? CODESET_C : CODESET_B;
  }


  //----
  private static int chooseSetCType(String number, int idx) {
    int last = number.length();
    if (idx >= last)
      return SETC_UNCODABLE;
    char c = number.charAt(idx);
    if (c == FNC1_CHAR)
      return SETC_FNC_1;
    if (!isDigit(c))
      return SETC_UNCODABLE;
    if (idx + 1 >= last)
      return SETC_ONE_DIGIT;
    return isDigit(number.charAt(idx + 1)) ? SETC_TWO_DIGITS : SETC_ONE_DIGIT;
  }


  //----
  public void setNumber(String number, boolean autoComplete, boolean addOptChecksum)
      throws IllegalArgumentException {
    checkEmpty(number);
    final int len = number.length();

    if (myCodeSet == CODESET_C) {
      checkInteger(number);
      if (len % 2 != 0) {
        if (autoComplete)
          number = "0" + number;
        else
          throwNumberLengthNotEven(len);
      }
    } else {
      if (myCodeSet == CODESET_A && autoComplete)
        number = number.toUpperCase();
      StringBuilder sb = new StringBuilder(len);
      for (int i=0; i<len; i++) {
        char c = number.charAt(i);
        if (c != FNC1_CHAR && c != FNC2_CHAR && c != FNC3_CHAR && c != FNC4_CHAR)
          if ((myCodeSet == CODESET_A && c > '_') ||
              (myCodeSet == CODESET_B && c < ' ') ||
              (c > 127))
            throwIAE("Invalid character: " + c);
        sb.append(c < 32 || c > 127 ? ' ' : c);
      }
      myHumanReadableNumber = sb.toString();
    }

    myNumber = number;
    reset();
  }

}
