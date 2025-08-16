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

import static de.vwsoft.barcodelib4j.oned.Barcode.calculateModulo10;
import static de.vwsoft.barcodelib4j.oned.Barcode.findNonDigitPosition;
import static de.vwsoft.barcodelib4j.oned.Barcode.validateModulo10;
import static de.vwsoft.barcodelib4j.oned.Barcode.validateNotEmpty;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * Validates GS1 structured data and generates the corresponding human readable representation. The
 * class performs tasks such as calculating check digits for GTIN and SSCC and verifying the length
 * and format of data for compliance with the respective Application Identifiers (AI).
 * <p>
 * <b>Rules for the input format:</b>
 * <p>
 * The first character in the input may or may not be an FNC1. The input may or may not include
 * round brackets (parentheses), therefore both forms are equally accepted:
 * <pre>
 *    <b>01</b>01234567890128<b>15</b>191231
 *    <b>(01)</b>01234567890128<b>(15)</b>191231
 * </pre>
 * After validation, the human readable text returned by {@link #getText()} includes all necessary
 * parentheses, regardless of the form in which the input was provided. The {@link #getContent()}
 * method returns the data in a strictly formatted form suitable for representation in a barcode.
 * <p>
 * <b>Rules for using FNC1 as AI separator:</b>
 * <ul>
 *   <li>If the schema of a particular AI defines a <b>fixed length</b> for the encoded data,
 *     there's no need to indicate the end of the data. This also applies if the data unit is
 *     positioned at the end of the GS1 data structure. In both cases no FNC1 is required.</li>
 *   <li>If the schema of a particular AI defines a <b>variable length</b> for the encoded data, the
 *     end of the data must be marked with an FNC1. However, if the given AI schema does not allow
 *     round brackets within the data itself, an opening bracket "(" can be conveniently used
 *     instead of an FNC1 to introduce the next AI, as the bracket will be identified by the
 *     algorithm as not being part of the data and therefore replaced by an FNC1 automatically.</li>
 *   <li>If unsure whether an FNC1 should be used in a specific case, it's recommended to use it.
 *     The algorithm removes all unnecessary FNC1 characters to ensure an optimal and space-saving
 *     structure of the encoded data.</li>
 * </ul>
 * <p>
 * <b>Rules for SSCC and GTIN (AI 00, 01, 02):</b>
 * <p>
 * These special numbers must end with either a correct pre-computed check digit or with the
 * placeholder character defined by {@link #CHECKSUM_PLACEHOLDER}. Example:
 * <pre>    (01)0123456789012<b>#</b>(15)191231</pre>
 * If a placeholder is present, the missing check digit is automatically calculated and replaced. If
 * a check digit is already present, it is verified, and a {@link BarcodeException} is thrown if it
 * is incorrect.
 */
public class GS1Validator {

  /**
   * Wildcard character that can be used in <b>AI 00</b> to <b>AI 02</b> (SSCC/GTIN) in place of
   * the checksum to force its automatic calculation. The static variable can be set to any other
   * character instead of the default ('#') if needed.
   */
  public static char CHECKSUM_PLACEHOLDER = '#';

  private static final int L_FNC1 = -1;
  private static final int L_BRACKET = -2;

  private static final AI[] APP_IDS = {
    new AI("00", 18, "_d{18}"),
    new AI("01", 14, "_d{14}"),
    new AI("02", 14, "_d{14}"),
    new AI("10", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("11", 6), // date YYMMDD
    new AI("12", 6), // date YYMMDD
    new AI("13", 6), // date YYMMDD
    new AI("15", 6), // date YYMMDD
    new AI("16", 6), // date YYMMDD
    new AI("17", 6), // date YYMMDD
    new AI("20", 2, "_d{2}"),
    new AI("21", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("22", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("235", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,28}"),
    new AI("240", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("241", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("242", L_BRACKET, "_d{0,6}"),
    new AI("243", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("250", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("251", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("253", L_FNC1, "_d{13})([_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,17}"),
    new AI("254", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("255", L_BRACKET, "_d{13})(_d{0,12}"),
    new AI("30", L_BRACKET, "_d{0,8}"),
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
    new AI("37", L_BRACKET, "_d{0,8}"),
    new AI("390", 9, L_BRACKET, "_d{0,15}"),
    new AI("391", 9, L_BRACKET, "_d{3})(_d{0,15}"),
    new AI("392", 9, L_BRACKET, "_d{0,15}"),
    new AI("393", 9, L_BRACKET, "_d{3})(_d{0,15}"),
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
    new AI("423", L_BRACKET, "_d{3})(_d{0,12}"),
    new AI("424", 3, "_d{3}"),
    new AI("425", L_BRACKET, "_d{3})(_d{0,12}"),
    new AI("426", 3, "_d{3}"),
    new AI("427", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,3}"),
    new AI("7001", 13, "_d{13}"),
    new AI("7002", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("7003", 10), // Expiration date and time (YYMMDDHHMM)
    new AI("7004", L_BRACKET, "_d{0,4}"),
    new AI("7005", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,12}"),
    new AI("7006", 6), // First freeze date (YYMMDD)
    new AI("7007", L_BRACKET, "_d{6,12}"),
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
    new AI("8006", L_BRACKET, "_d{14})(_d{2})(_d{2}"),
    new AI("8007", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,34}"),
    new AI("8008", L_BRACKET), // Date and time of production (YYMMDDHHMMSS)
    new AI("8009", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,50}"),
    new AI("8010", L_BRACKET, "[_x23_x2D_x2F_x30-_x39_x41-_x5A]{0,30}"),
    new AI("8011", L_BRACKET, "_d{0,12}"),
    new AI("8012", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("8013", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("8017", 18, "_d{18}"),
    new AI("8018", 18, "_d{18}"),
    new AI("8019", L_BRACKET, "_d{0,10}"),
    new AI("8020", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,25}"),
    new AI("8026", L_BRACKET, "_d{14})(_d{2})(_d{2}"),
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



  private String myContent; // The "raw" content as encoded in the barcode
  private String myText;    // Human readable representation



  /**
   * Constructs a new instance and validates the specified content.
   *
   * @param content   the content to be validated
   * @param fnc1Char  the FNC1 character used as a separator within the content, and also used in
                      the output returned by {@link #getContent()}
   * @throws BarcodeException if the content is empty or invalid according to the GS1 standards
   */
  public GS1Validator(String content, char fnc1Char) throws BarcodeException {
    final int len = content.length();
    StringBuilder sbContent = new StringBuilder(len);
    StringBuilder sbText = new StringBuilder(len);
    int idx = 0;

    while (true) {
      while (idx != len && content.charAt(idx) == fnc1Char) // Skip FNC1 characters
        idx++;
      if (idx == len)
        break;

      // Step 1: Extract the next Application Identifier (AI)
      AI ai = null;
      boolean embraced = content.charAt(idx) == '(';
      if (embraced)
        idx++; // Skip opening bracket
      for (AI nextAI : APP_IDS) {
        String s = nextAI.appId;
        if (s.regionMatches(0, content, idx, s.length())) {
          idx += s.length();
          if (nextAI.maxY > 0) {
            int n = idx < len ? content.charAt(idx) - 48 : -1;
            if (n < 0 || n > nextAI.maxY)
              throw new BarcodeException(BarcodeException.CONTENT_INVALID,
                  "'n' in AI %sn at position %s must be between 0 and %s",
                  "'n' in AI %sn an Position %s muss zwischen 0 und %s liegen",
                  s, idx, nextAI.maxY);
            s += n;
            idx++; // Skip one more character
          }
          if (embraced) {
            if (idx == len || content.charAt(idx) != ')')
              throw new BarcodeException(BarcodeException.CONTENT_INVALID,
                  "Closing bracket is missing at position %s",
                  "Schlie\u00DFende Klammer fehlt an Position %s", idx);
            idx++; // Skip closing bracket
          }
          sbContent.append(s);
          sbText.append('(').append(s).append(')');
          ai = nextAI;
          break;
        }
      }
      if (ai == null)
        throw new BarcodeException(BarcodeException.CONTENT_INVALID,
            "No valid AI found at position %s",
            "Kein g\u00FCltiger AI an Position %s gefunden", idx);

      // Step 2: Extract the data corresponding to the identified AI
      String data;
      int k;
      if (ai.delimiter > 0) { // AI expects a fixed length of data
        k = Math.min(idx + ai.delimiter, len); // Determine the maximum length to validate
      } else {
        k = content.indexOf(fnc1Char, idx);
        if (ai.delimiter == L_BRACKET) {
          int n = content.indexOf('(', idx);
          if (n > 0)
            k = k > 0 ? Math.min(k, n) : n;
        }
        if (k < 0)
          k = len;
      }
      data = content.substring(idx, k);

      // Step 3: Verify integrity of data
      if (ai.delimiter > 0 && ai.delimiter != data.length())
        throw new BarcodeException(BarcodeException.CONTENT_LENGTH_INVALID,
            "Value of AI %s must consist of %s characters; Provided: %s",
            "Wert von AI %s muss aus %s Zeichen bestehen; Aktuell: %s",
            ai.appId, ai.delimiter, data.length());
      if (ai == APP_IDS[0] || ai == APP_IDS[1] || ai == APP_IDS[2]) {
        if (data.charAt(ai.delimiter - 1) == CHECKSUM_PLACEHOLDER) {
          data = data.substring(0, ai.delimiter - 1);
          validateDigits(data, ai);
          data += calculateModulo10(data);
        } else {
          validateDigits(data, ai);
          validateModulo10(data);
        }
      } else if (!ai.matches(data)) {
        if (ai.isYYMMDD())
          throw new BarcodeException(BarcodeException.CONTENT_INVALID,
              "Value of AI %s is not a real date in YYMMDD format",
              "Wert von AI %s ist kein reales Datum im Format JJMMTT", ai.appId);
        else
          throw new BarcodeException(BarcodeException.CONTENT_INVALID,
              "Value of AI %s does not conform to its schema",
              "Wert von AI %s entspricht nicht dessen Schema", ai.appId);
      }

      sbContent.append(data);
      sbText.append(data);
      if (k == len)
        break;
      idx = k;
      if (ai.delimiter < 0)
        sbContent.append(fnc1Char);
    }

    // Remove trailing FNC1 characters
    for (int i=sbContent.length()-1; i>=0; i--) {
      if (sbContent.charAt(i) != fnc1Char) {
        sbContent.setLength(i + 1);
        break;
      }
    }

    validateNotEmpty(sbContent); // Check this first after all possible FNC1 chars have been removed

    myContent = sbContent.toString();
    myText = sbText.toString();
  }



  private static void validateDigits(String value, AI ai) throws BarcodeException {
    if (findNonDigitPosition(value) >= 0)
      throw new BarcodeException(BarcodeException.CONTENT_NOT_DIGITS,
          "Value of AI %s may contain only digits and an optional checksum placeholder",
          "Wert von AI %s darf nur Ziffern und einen optionalen Pr\u00FCfziffer-Platzhalter " +
              "enthalten", ai.appId);
  }



  /**
   * {@return the validated raw data as it will be encoded in the barcode}
   * <p>
   * Please note that the returned string does not include the leading FNC1 character, which is used
   * to identify the GS1 data structure. This is because its "raw" value may differ from that of the
   * FNC1 character used as a separator. However, a leading FNC1 character is automatically added by
   * {@link ImplEAN128 GS1-128} and {@link de.vwsoft.barcodelib4j.twod.TwoDCode} prior to encoding.
   */
  public String getContent() {
    return myContent;
  }



  /**
   * {@return the validated data as human readable text}
   * <p>
   * Application Identifier (AI) numbers are enclosed in round brackets (parentheses).
   */
  public String getText() {
    return myText;
  }



  private static class AI {

    private static Map<Integer,DateTimeFormatter> dateTimeFormatters = Map.of(

      // AI 11, 12, 13, 15, 16, 17, 7006
      6,    new DateTimeFormatterBuilder().parseStrict().appendPattern("uuMMdd")     // YYMMDD
                .toFormatter().withResolverStyle(ResolverStyle.STRICT),

      // AI 7003
      10,   new DateTimeFormatterBuilder().parseStrict().appendPattern("uuMMddHHmm") // YYMMDDHHMM
                .toFormatter().withResolverStyle(ResolverStyle.STRICT),

      // AI 8008
      8008, new DateTimeFormatterBuilder().parseStrict()
                .appendPattern("uuMMddHH")               // Date (YYMMDD) and Hour (HH)
                .optionalStart()
                    .appendPattern("mm")                 // Minutes (MM), if present
                    .optionalStart()
                        .appendPattern("ss")             // Seconds (SS), if present
                    .optionalEnd()
                .optionalEnd()
                .toFormatter().withResolverStyle(ResolverStyle.STRICT)
    );

    String appId;
    int maxY;
    int delimiter;
    Pattern pattern;

    AI(String appId, int maxY, int delimiter, String regex) {
      this.appId = appId;
      this.maxY = maxY;
      this.delimiter = delimiter;

      StringBuilder sb = new StringBuilder(regex.length() + 2)
          .append('(')    .append(regex)    .append(')');
      for (int i=regex.length(); i!=0; i--)
        if (sb.charAt(i) == '_')
          sb.setCharAt(i, '\\');
      this.pattern = Pattern.compile(sb.toString());
    }

    AI(String appId, int delimiter, String regex) {
      this(appId, 0, delimiter, regex);
    }

    AI(String appId, int delimiter) { // Constructor for date-time-AIs
      this.appId = appId;
      this.delimiter = delimiter;
    }

    boolean isYYMMDD() {
      return pattern == null && delimiter == 6;
    }

    boolean matches(String data) {
      if (pattern != null)
        return pattern.matcher(data).matches();
      try {
        dateTimeFormatters.get(delimiter > 0 ? delimiter : Integer.parseInt(appId)).parse(data);
        return true;
      } catch (DateTimeParseException e) {
        return false;
      }
    }
  }

}
