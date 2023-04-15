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
import java.util.regex.*;
import static de.vwsoft.barcode.Barcode.*;


class AI {
  String appId; int maxY; int delimiter; Pattern pattern;
  AI(String appId, int delimiter, String regex) { this(appId, 0, delimiter, regex); }
  AI(String appId, int maxY, int delimiter, String regex) {
    this.appId = appId;
    this.maxY = maxY;
    this.delimiter = delimiter;
    pattern = Pattern.compile('(' + regex.replace('_', '\\') + ')');
  }
  boolean matches(String data) { return pattern.matcher(data).matches(); }
}


public class GS1 {
  public static char CHECKSUM_PLACEHOLDER = '#';

  private static final int L_FNC1 = -1;
  private static final int L_BRACE = -2;

  private static final AI[] APP_IDS = {
    new AI("00", 18, "_d{18}"),
    new AI("01", 14, "_d{14}"),
    new AI("02", 14, "_d{14}"),
    new AI("10", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("11", 6, "_d{6}"),
    new AI("12", 6, "_d{6}"),
    new AI("13", 6, "_d{6}"),
    new AI("15", 6, "_d{6}"),
    new AI("16", 6, "_d{6}"),
    new AI("17", 6, "_d{6}"),
    new AI("20", 2, "_d{2}"),
    new AI("21", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("22", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("235", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,28}"),
    new AI("240", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("241", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("242", L_BRACE, "_d{0,6}"),
    new AI("243", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("250", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("251", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("253", L_FNC1, "_d{13})([_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,17}"),
    new AI("254", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("255", L_BRACE, "_d{13})(_d{0,12}"),
    new AI("30", L_BRACE, "_d{0,8}"),
    new AI("310", 5, 6, "_d{6}"),
    new AI("311", 5, 6, "_d{6}"),
    new AI("312", 5, 6, "_d{6}"),
    new AI("313", 5, 6, "_d{6}"),
    new AI("314", 5, 6, "_d{6}"),
    new AI("315", 5, 6, "_d{6}"),
    new AI("316", 5, 6, "_d{6}"),
    new AI("320", 5, 6, "_d{6}"),
    new AI("321", 5, 6, "_d{6}"),
    new AI("322", 5, 6, "_d{6}"),
    new AI("323", 5, 6, "_d{6}"),
    new AI("324", 5, 6, "_d{6}"),
    new AI("325", 5, 6, "_d{6}"),
    new AI("326", 5, 6, "_d{6}"),
    new AI("327", 5, 6, "_d{6}"),
    new AI("328", 5, 6, "_d{6}"),
    new AI("329", 5, 6, "_d{6}"),
    new AI("330", 5, 6, "_d{6}"),
    new AI("331", 5, 6, "_d{6}"),
    new AI("332", 5, 6, "_d{6}"),
    new AI("333", 5, 6, "_d{6}"),
    new AI("334", 5, 6, "_d{6}"),
    new AI("335", 5, 6, "_d{6}"),
    new AI("336", 5, 6, "_d{6}"),
    new AI("337", 5, 6, "_d{6}"),
    new AI("340", 5, 6, "_d{6}"),
    new AI("341", 5, 6, "_d{6}"),
    new AI("342", 5, 6, "_d{6}"),
    new AI("343", 5, 6, "_d{6}"),
    new AI("344", 5, 6, "_d{6}"),
    new AI("345", 5, 6, "_d{6}"),
    new AI("346", 5, 6, "_d{6}"),
    new AI("347", 5, 6, "_d{6}"),
    new AI("348", 5, 6, "_d{6}"),
    new AI("349", 5, 6, "_d{6}"),
    new AI("350", 5, 6, "_d{6}"),
    new AI("351", 5, 6, "_d{6}"),
    new AI("352", 5, 6, "_d{6}"),
    new AI("353", 5, 6, "_d{6}"),
    new AI("354", 5, 6, "_d{6}"),
    new AI("355", 5, 6, "_d{6}"),
    new AI("356", 5, 6, "_d{6}"),
    new AI("357", 5, 6, "_d{6}"),
    new AI("360", 5, 6, "_d{6}"),
    new AI("361", 5, 6, "_d{6}"),
    new AI("362", 5, 6, "_d{6}"),
    new AI("363", 5, 6, "_d{6}"),
    new AI("364", 5, 6, "_d{6}"),
    new AI("365", 5, 6, "_d{6}"),
    new AI("366", 5, 6, "_d{6}"),
    new AI("367", 5, 6, "_d{6}"),
    new AI("368", 5, 6, "_d{6}"),
    new AI("369", 5, 6, "_d{6}"),
    new AI("37", L_BRACE, "_d{0,8}"),
    new AI("390", 9, L_BRACE, "_d{0,15}"),
    new AI("391", 9, L_BRACE, "_d{3})(_d{0,15}"),
    new AI("392", 9, L_BRACE, "_d{0,15}"),
    new AI("393", 9, L_BRACE, "_d{3})(_d{0,15}"),
    new AI("394", 3, 4, "_d{4}"),
    new AI("400", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("401", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("402", 17, "_d{17}"),
    new AI("403", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("410", 13, "_d{13}"),
    new AI("411", 13, "_d{13}"),
    new AI("412", 13, "_d{13}"),
    new AI("413", 13, "_d{13}"),
    new AI("414", 13, "_d{13}"),
    new AI("415", 13, "_d{13}"),
    new AI("416", 13, "_d{13}"),
    new AI("417", 13, "_d{13}"),
    new AI("420", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("421", L_FNC1, "_d{3})([_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,9}"),
    new AI("422", 3, "_d{3}"),
    new AI("423", L_BRACE, "_d{3})(_d{0,12}"),
    new AI("424", 3, "_d{3}"),
    new AI("425", L_BRACE, "_d{3})(_d{0,12}"),
    new AI("426", 3, "_d{3}"),
    new AI("427", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,3}"),
    new AI("7001", 13, "_d{13}"),
    new AI("7002", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("7003", 10, "_d{10}"),
    new AI("7004", L_BRACE, "_d{0,4}"),
    new AI("7005", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,12}"),
    new AI("7006", 6, "_d{6}"),
    new AI("7007", L_BRACE, "_d{6,12}"),
    new AI("7008", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,3}"),
    new AI("7009", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,10}"),
    new AI("7010", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,2}"),
    new AI("7020", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("7021", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("7022", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("7023", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("703", 9, L_FNC1, "_d{3})([_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,27}"),
    new AI("7040", 4, "_d[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{3}"),
    new AI("710", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("711", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("712", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("713", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("714", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("723", 9, L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{2,30}"),
    new AI("7240", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("8001", 14, "_d{14}"),
    new AI("8002", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("8003", L_FNC1, "_d{14})([_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,16}"),
    new AI("8004", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("8005", 6, "_d{6}"),
    new AI("8006", L_BRACE, "_d{14})(_d{2})(_d{2}"),
    new AI("8007", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,34}"),
    new AI("8008", L_BRACE, "_d{8})(_d{0,4}"),
    new AI("8009", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,50}"),
    new AI("8010", L_BRACE, "[_x23_x2D_x2F_x30-_x39_x41-_x5A]{0,30}"),
    new AI("8011", L_BRACE, "_d{0,12}"),
    new AI("8012", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("8013", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("8017", 18, "_d{18}"),
    new AI("8018", 18, "_d{18}"),
    new AI("8019", L_BRACE, "_d{0,10}"),
    new AI("8020", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,25}"),
    new AI("8026", L_BRACE, "_d{14})(_d{2})(_d{2}"),
    new AI("8110", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,70}"),
    new AI("8111", 4, "_d{4}"),
    new AI("8112", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,70}"),
    new AI("8200", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,70}"),
    new AI("90", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("91", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,90}"),
    new AI("92", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,90}"),
    new AI("93", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,90}"),
    new AI("94", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,90}"),
    new AI("95", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,90}"),
    new AI("96", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,90}"),
    new AI("97", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,90}"),
    new AI("98", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,90}"),
    new AI("99", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,90}")
  };

  private String myNumber, myHumanReadableNumber;


  //---- input may or may not include braces - both variants are accepted. SSCC and GTIN
  // (AI 00, 01, 02) must end in a correct precalculated check digit or a #-sign instead of it.
  public GS1(String number, char fnc1Char) throws IllegalArgumentException {
    checkEmpty(number);

    int len = number.length();
    // truncate possible FNC1 characters at the end of the number.
    while (number.charAt(len - 1) == fnc1Char)
      len --;
    if (len == 0)
      throwIAE(number);

    StringBuilder sbNumber = new StringBuilder(len);
    StringBuilder sbHumanReadableNumber = new StringBuilder(len);
    sbNumber.append(fnc1Char);
    int idx = 0;

    do {
      // truncate possible FNC1 characters at the beginning of the number.
      while (number.charAt(idx) == fnc1Char)
        idx ++;

      int k = 0;
      if (number.charAt(idx) == '(') {
        k = number.indexOf(')', idx + 3);
        if (k < 0 || k > idx + 5) // closing bracket is missing or too far away
          throwIAE(number);
        idx ++;
      }
      AI appId = null;
      for (AI ai : APP_IDS) {
        String s = ai.appId;
        if (s.regionMatches(0, number, idx, s.length())) {
          if (ai.maxY > 0) {
            if (s.length() > idx + len - 1)
              throwIAE(number);
            int n = number.charAt(idx + s.length()) - 48;
            if (n < 0 || n > ai.maxY)
              throwIAE(number);
            s += n;
          }
          sbNumber.append(s);
          sbHumanReadableNumber.append('(').append(s).append(')');
          appId = ai;
          idx += s.length();
          break;
        }
      }
      if (appId == null)
        throwIAE(number);
      if (k > 0) // skip closing bracket
        idx ++;

      if (appId.delimiter > 0) { // ai assumes fixed length of data
        k = idx + appId.delimiter;
        if (k > len)
          throwIAE(number);
      } else {
        k = number.indexOf(fnc1Char, idx);
        if (appId.delimiter == L_BRACE) {
          int n = number.indexOf('(', idx);
          if (n > 0)
            k = k > 0 ? Math.min(k, n) : n;
        }
        if (k < 0)
          k = len;
      }
      String s = number.substring(idx, k);

      // verify integrity of data
      if (appId == APP_IDS[0] || appId == APP_IDS[1] || appId == APP_IDS[2]) {
        checkFixedLength(s, appId.delimiter);
        if (s.charAt(appId.delimiter - 1) == CHECKSUM_PLACEHOLDER) {
          s = s.substring(0, appId.delimiter - 1);
          checkInteger(s);
          s += calculateModulo10(s);
        } else {
          checkInteger(s);
          checkModulo10(s);
        }
      } else if (!appId.matches(s)) {
        throwIAE(number);
      }

      sbNumber.append(s);
      sbHumanReadableNumber.append(s);
      if (k == len)
        break;
      idx = k;
      if (appId.delimiter < 0)
        sbNumber.append(fnc1Char);

    } while (true);

    myNumber = sbNumber.toString();
    myHumanReadableNumber = sbHumanReadableNumber.toString();
  }


  //----
  public String getNumber() { return myNumber; }
  public String getHumanReadableNumber() { return myHumanReadableNumber; }

}
